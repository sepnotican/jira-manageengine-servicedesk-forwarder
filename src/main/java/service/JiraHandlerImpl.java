package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import controller.issueTransport.RestJsonCaller;
import core.Settings;
import entity.TaskModel;
import org.apache.log4j.Logger;

import java.util.Base64;

/**
 * Created by muzafar on 3/10/17.
 */
@Singleton
public class JiraHandlerImpl implements JiraHandler {

    private static final Logger logger = Logger.getLogger(JiraHandlerImpl.class);
    private static final Settings settings = Settings.getSettings();
    private static final String JIRA_CUSTOMFIELD_JSON_PREFIX = "customfield_";
    private static final String JIRA_CUSTOMFIELD_JQL_TEMPLATE = "cf[%s]"; //%s - custom field id

    private static final String JIRA_ISSUE_TYPE_STORY = "Story";

    private static final String JQL_QUERY_TEMPLATE_GET_ISSUE_BY_TEXTSTAMP
            = "project=" + settings.getJiraDefaultProject() + " AND text ~ \"%s\" ORDER BY Key ASC"; //%s - servicedesk stamp;
    private static final String JQL_QUERY_TEMPLATE_GET_ISSUE_BY_CUSTOMFIELD
            = "project=" + settings.getJiraDefaultProject() + " AND "
            + String.format(JIRA_CUSTOMFIELD_JQL_TEMPLATE, settings.getJiraServiceDeskIDField())
            + " ~ \"%d\" ORDER BY Key ASC"; //%d - servicedesk id;
    final private String basicAuth;

    @Inject
    private RestJsonCaller restJsonCaller;

    public JiraHandlerImpl() {
        Base64.Encoder encoder = Base64.getEncoder();
        basicAuth = encoder.encodeToString(Settings.getSettings().getJiraBasicAuth().getBytes());
    }

    @Override
    public TaskModel getIssueByID(int sd_id, QueryMode queryMode) {
        TaskModel result_ = null;

        JsonObject requestBody = new JsonObject();

        String jql;
        if (queryMode == QueryMode.BY_TEXTSTAMP) {
            final String sdStamp = String.format(Settings.getSettings().getServicedeskStamp(), Integer.toString(sd_id));
            jql = String.format(JQL_QUERY_TEMPLATE_GET_ISSUE_BY_TEXTSTAMP, sdStamp);
        } else {
            jql = String.format(JQL_QUERY_TEMPLATE_GET_ISSUE_BY_CUSTOMFIELD, sd_id);
        }

        requestBody.addProperty("jql", jql);
        requestBody.addProperty("startAt", 0);
        requestBody.addProperty("maxResults", 2);

        JsonArray fieldsArray = new JsonArray();
        fieldsArray.add("id");
        fieldsArray.add("key");
        fieldsArray.add(JIRA_CUSTOMFIELD_JSON_PREFIX + Settings.getSettings().getJiraResolutionField());
        fieldsArray.add("status");
        fieldsArray.add("assignee");

        requestBody.add("fields", fieldsArray);

        JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL()
                + "/search", basicAuth, requestBody, "POST", 200);

        if (jo != null) {
            JsonArray issuesArray = jo.getAsJsonArray("issues");

            if (issuesArray.size() > 0) {
                if (issuesArray.size() > 1)
                    logger.warn("More of 1 result by issue: " + sd_id);
                JsonObject joIssue = issuesArray.get(0).getAsJsonObject();
                JsonElement jsonElement = joIssue.get("key");
                if (jsonElement != null) {
                    //key
                    String jiraKey = jsonElement.getAsString();
                    TaskModel taskModel = new TaskModel(sd_id, jiraKey);
                    //resolution
                    JsonObject fieldsObject = joIssue.getAsJsonObject("fields");
                    String assigneeName = null;
                    if (!fieldsObject.get("assignee").isJsonNull()) {
                        JsonObject assigneeObject = fieldsObject.getAsJsonObject("assignee");
                        if (assigneeObject != null && !assigneeObject.isJsonNull())
                            assigneeName = assigneeObject.get("displayName").getAsString();
                        taskModel.setAssingnee(assigneeName);
                    }
                    //checking status
                    int statusCode = fieldsObject.getAsJsonObject("status").get("id").getAsInt();
                    taskModel.setJiraStatus(statusCode);

                    JsonElement jeResolution = fieldsObject.get(JIRA_CUSTOMFIELD_JSON_PREFIX + Settings.getSettings().getJiraResolutionField());
                    if (jeResolution != null && !jeResolution.isJsonNull()) {
                        String resolution = jeResolution.getAsString();
                        taskModel.setResolution(resolution);
                    }


                    result_ = taskModel;
                }
            }
        }

        return result_;
    }

    @Override
    public boolean createIssueInJira(TaskModel taskModel) {
        return createIssueInJira(taskModel, false);
    }

    @Override
    public boolean createIssueInJira(TaskModel taskModel, boolean onlyBuildQuery) {

        JsonObject requestBody = new JsonObject();
        JsonObject joFields = new JsonObject();

        JsonObject joProject = new JsonObject();
        joProject.addProperty("key", "B1C");
        joFields.add("project", joProject);

        JsonObject joAssingee = new JsonObject();
        joAssingee.addProperty("name", taskModel.getAssingnee());
        joFields.add("assignee", joAssingee);

        String summary = taskModel.getSummary();
        summary = "##" + Integer.toString(taskModel.getId_sd()) + "## " + summary;
        joFields.addProperty("summary", summary);

        String descriprion = taskModel.getDescription() + "\n\nRequester: " + taskModel.getRequester() + '\n' +
                String.format(Settings.getSettings().getServicedeskStamp(), taskModel.getId_sd()) +
                "\nlink: " +
                Settings.getSettings().getServiceDeskHttpURL() +
                "/WorkOrder.do?woMode=viewWO&woID=" + taskModel.getId_sd() +
                '\n';

        joFields.addProperty("description", descriprion);

        JsonObject joIssuetype = new JsonObject();
        joIssuetype.addProperty("name", JIRA_ISSUE_TYPE_STORY);
        joFields.add("issuetype", joIssuetype);

        JsonElement jpSdId = new JsonPrimitive(String.valueOf(taskModel.getId_sd()));
        joFields.add(JIRA_CUSTOMFIELD_JSON_PREFIX + Settings.getSettings().getJiraServiceDeskIDField(), jpSdId);

        requestBody.add("fields", joFields);

        if (onlyBuildQuery) {
            System.out.println(requestBody);
            return false;
        }

        JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL() + "/issue", basicAuth,
                requestBody, "POST", 201);

        if (jo == null) {
            logger.error("ERROR while creating issue: " + taskModel.getId_sd() + '\n' +
                    "Jira response is null");
            return false;
        }


        if (jo.get("key") != null) {
            taskModel.setJiraKey(jo.get("key").getAsString());
            logger.info("Issue has been created : " + taskModel.getId_sd());
            return true;
        } else {
            logger.error("ERROR while creating issue: " + taskModel.getId_sd() + '\n' + jo.toString());
            return false;
        }
    }

    @Override
    public void reopenTaskByKey(String jiraKey) {

        JsonObject requestBody = new JsonObject();

        JsonObject joTransition = new JsonObject();
        joTransition.addProperty("id", Settings.getSettings().getJiraTransitionIdResolvedToTodo());

        requestBody.add("transition", joTransition);

        try {
            JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL()
                            + String.format("/issue/%s/transitions?expand=transitions.fields", jiraKey)
                    , basicAuth, requestBody, "POST", 204);
            logger.info("Jira task reopened: " + jiraKey);
        } catch (Exception e) {
            logger.warn("Cannot reopen Jira task by key: " + jiraKey
                    + ", error: " + e.getMessage());
        }

    }

    @Override
    public void addCommentByKey(String jiraKey, String comment) {

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("body", comment);

        JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL()
                        + String.format("/issue/%s/comment", jiraKey)
                , basicAuth, requestBody, "POST", 201);

        if (jo == null) {
            logger.warn("Cannot add comment to Jira task by key: " + jiraKey
                    + "\n comment = " + comment);
        }

    }
}
