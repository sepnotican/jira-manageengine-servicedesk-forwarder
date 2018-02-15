package controller.issueTransport;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import core.Settings;
import dao.IssuesLocalCacheDAO;
import entity.TaskModel;
import org.apache.log4j.Logger;
import service.IssueNotFoundException;
import service.JiraHandler;
import service.ServiceDeskHandler;

import java.util.List;

/**
 * Created by sepnotican on 14.06.17.
 */
@Singleton
public class IssuesTransportController {

    private static final Logger logger = Logger.getLogger(IssuesTransportController.class);

    @Inject
    private JiraHandler jiraHandler;
    @Inject
    private ServiceDeskHandler serviceDeskHandler;

    private Settings settings;

    public IssuesTransportController() {
        settings = Settings.getInstance();
    }

    public void checkForChanges() {

        //1 check actual tasks from SD
        List<TaskModel> tasksFromSD = serviceDeskHandler.get_requests(0, 1000);

        //2 load cached fields from local DB
        IssuesLocalCacheDAO.instance.fillTasksCachedInfo(tasksFromSD);

        for (TaskModel taskSD : tasksFromSD) {

            TaskModel jiraResult = null;

            try {
                jiraResult = jiraHandler.getIssueByID(taskSD.getId_sd(), JiraHandler.QueryMode.BY_SD_ID_CUSTOMFIELD);

                if (jiraResult == null
                        && settings.isCheckIssuesInJiraByTextstampBeforeCreate())
                    jiraResult = jiraHandler.getIssueByID(taskSD.getId_sd(), JiraHandler.QueryMode.BY_TEXTSTAMP);
            } catch (IssueNotFoundException e) {
                logger.error(e.getMessage());
                continue;
            }


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
                        logger.warn("Creating issue in jira failed: " + taskSD.getId_sd());
                }
            } else if (settings.isCloseTaskInSDWhenJiraClosed()
                    && jiraResult != null
                    && (jiraResult.getResolution() != null
                    && taskSD.getResolution() == null
                    && jiraResult.getJiraStatus() == settings.getJiraStatusIdResolved())) {
                //need check solution and Resolved status
                taskSD.setResolution(jiraResult.getResolution());
                //update SD task - set resolution and status = Closed
                if (serviceDeskHandler.completeRequest(
                        jiraResult.getId_sd()
                        , jiraResult.getResolution()
                        , jiraResult.getAssingnee()))
                    updateIssueInLocalCache(taskSD);

            } else if (settings.isReopenTaskInJiraWhenSDReopen()
                    && jiraResult != null
                    && jiraResult.getJiraStatus() == settings.getJiraStatusIdResolved()) {
                //task in active filter SD and closed in Jira = reopen.
                jiraHandler.reopenTaskByKey(jiraResult.getJiraKey());
                jiraHandler.addCommentByKey(jiraResult.getJiraKey()
                        , "Task reopened by service desk. Check it!");
                taskSD.setResolution(null);
                updateIssueInLocalCache(taskSD);
            }
        }
    }

    private boolean createIssueInJira(TaskModel taskModel) {
        serviceDeskHandler.fillIssueByID(taskModel);
        return jiraHandler.createIssueInJira(taskModel);
    }

    private void updateIssueInLocalCache(TaskModel taskModel) {
        IssuesLocalCacheDAO.instance.updateTaskByJiraKey(taskModel);
    }

}
