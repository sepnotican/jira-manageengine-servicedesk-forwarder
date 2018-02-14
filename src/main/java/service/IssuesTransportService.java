package service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import controller.issueTransport.JiraHandler;
import controller.issueTransport.ServiceDeskHandler;
import core.Settings;
import core.Tools;
import dao.IssuesLocalCache;
import entity.TaskModel;

import java.util.List;

/**
 * Created by sepnotican on 14.06.17.
 */
@Singleton
public class IssuesTransportService {

    @Inject
    private JiraHandler jiraHandler;
    @Inject
    private ServiceDeskHandler serviceDeskHandler;

    public void checkForChanges() {

        //1 check actual tasks from SD
        List<TaskModel> tasksFromSD = serviceDeskHandler.get_requests(0, 1000);

        //2 load cached fields from local DB
        IssuesLocalCache.instance.fillTasksCachedInfo(tasksFromSD);

        tasksFromSD.forEach(taskSD -> {

            TaskModel jiraResult = jiraHandler.getIssueByIDTextSearch(taskSD.getId_sd());
            //3 not cached - need to create
            if (taskSD.getJiraKey() == null) {
                if (jiraResult != null) {
                    //3.1 found, just save key and resolution by sd_id
                    updateIssueInLocalCache(jiraResult);
                } else {
                    //  3.2 if not found in Jira - create new in Jira
                    if (createIssueInJira(taskSD))
                        updateIssueInLocalCache(taskSD); //3.3. make presist flag in local DB
                    else
                        Tools.logger.warn("Creating issue in jira failed: " + taskSD.getId_sd());
                }
            } else if (Settings.getSettings().isCloseTaskInSDWhenJiraClosed()
                    && (jiraResult.getResolution() != null
                    && taskSD.getResolution() == null
                    && jiraResult.getJiraStatus() == Settings.getSettings().getJiraStatusIdResolved())) {
                //need check solution and Resolved status
                taskSD.setResolution(jiraResult.getResolution());
                //update SD task - set resolution and status = Closed
                if (serviceDeskHandler.completeRequest(
                        jiraResult.getId_sd()
                        , jiraResult.getResolution()
                        , jiraResult.getAssingnee()))
                    updateIssueInLocalCache(taskSD);

            } else if (Settings.getSettings().isReopenTaskInJiraWhenSDReopen()
                    && jiraResult.getJiraStatus() == Settings.getSettings().getJiraStatusIdResolved()) {
                //task in active filter SD and closed in Jira = reopen.
                jiraHandler.reopenTaskByKey(jiraResult.getJiraKey());
                jiraHandler.addCommentByKey(jiraResult.getJiraKey()
                        , "Task reopened by service desk. Check it!");
                taskSD.setResolution(null);
                updateIssueInLocalCache(taskSD);

            }
        });


    }

    private boolean createIssueInJira(TaskModel taskModel) {
        serviceDeskHandler.fillIssueByID(taskModel);
        return jiraHandler.createIssueInJira(taskModel);
    }

    private void updateIssueInLocalCache(TaskModel taskModel) {
        IssuesLocalCache.instance.updateTaskByJiraKey(taskModel);
    }

}
