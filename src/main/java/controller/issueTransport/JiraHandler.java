package controller.issueTransport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import core.Settings;
import core.Tools;
import entity.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

/**
 * Created by muzafar on 3/10/17.
 */
@Component
public class JiraHandler {

    private static final String JIRA_ISSUE_TYPE_STORY = "Story";
    final private String CUSTOM_FIELD_RESOLUTION = Settings.getSettings().getJiraResolutionField();
    final private String basicAuth;

    @Autowired
    private RestJsonCaller restJsonCaller;

    public JiraHandler() {
        BASE64Encoder enc64 = new BASE64Encoder();
        basicAuth = enc64.encode(Settings.getSettings().getJiraBasicAuth().getBytes());
    }

    public TaskModel getIssueByIDTextSearch(int sd_id) {
        TaskModel result_ = null;

        JsonObject requestBody = new JsonObject();

        requestBody.addProperty("jql",
                String.format("project=B1C AND text ~ \"" + Settings.getSettings().getServicedeskStamp() + "\" ORDER BY Key ASC", Integer.toString(sd_id)));
        requestBody.addProperty("startAt", 0);
        requestBody.addProperty("maxResults", 2);

        JsonArray fieldsArray = new JsonArray();
        fieldsArray.add("id");
        fieldsArray.add("key");
        fieldsArray.add(CUSTOM_FIELD_RESOLUTION);
        fieldsArray.add("status");
        fieldsArray.add("assignee");

        requestBody.add("fields", fieldsArray);

        JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL()
                + "/search", basicAuth, requestBody, "POST", 200);

        if (jo != null) {
            JsonArray issuesArray = jo.getAsJsonArray("issues");

            if (issuesArray.size() > 0) {
                if (issuesArray.size() > 1)
                    Tools.logger.warn("More of 1 result by issue: " + sd_id);
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

                    JsonElement jeResolution = fieldsObject.get(CUSTOM_FIELD_RESOLUTION);
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

    public boolean createIssueInJira(TaskModel taskModel) {
        return createIssueInJira(taskModel);
    }

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

        StringBuilder descriprion = new StringBuilder(taskModel.getDescription());
        descriprion.append("\n\nRequester: ").append(taskModel.getRequester()).append('\n')
                .append(String.format(Settings.getSettings().getServicedeskStamp(), taskModel.getId_sd()))
                .append("\nlink: ")
                .append(Settings.getSettings().getServiceDeskHttpURL())
                .append("/WorkOrder.do?woMode=viewWO&woID=").append(taskModel.getId_sd())
                .append('\n');

        joFields.addProperty("description", descriprion.toString());

        JsonObject joIssuetype = new JsonObject();
        joIssuetype.addProperty("name", JIRA_ISSUE_TYPE_STORY);
        joFields.add("issuetype", joIssuetype);

        JsonElement jpSdId = new JsonPrimitive(String.valueOf(taskModel.getId_sd()));
        joFields.add(Settings.getSettings().getJiraServiceDeskIDField(), jpSdId);

        requestBody.add("fields", joFields);

        if (onlyBuildQuery) {
            System.out.println(requestBody);
            return false;
        }

        JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL() + "/issue", basicAuth,
                requestBody, "POST", 201);

        if (jo == null) {
            Tools.logger.warn("ERROR while creating issue: " + taskModel.getId_sd() + '\n' +
                    "Jira response is null");
            return false;
        }


        if (jo.get("key") != null) {
            taskModel.setJiraKey(jo.get("key").getAsString());
            Tools.logger.info("Issue has been created : " + taskModel.getId_sd());
            return true;
        } else {
            Tools.logger.warn("ERROR while creating issue: " + taskModel.getId_sd() + '\n' + jo.toString());
            return false;
        }
    }

    public void reopenTaskByKey(String jiraKey) {

        JsonObject requestBody = new JsonObject();

        JsonObject joTransition = new JsonObject();
        joTransition.addProperty("id", Settings.getSettings().getJiraTransitionIdResolvedToTodo());

        requestBody.add("transition", joTransition);

        try {
            JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL()
                            + String.format("/issue/%s/transitions?expand=transitions.fields", jiraKey)
                    , basicAuth, requestBody, "POST", 204);
            Tools.logger.info("Jira task reopened: " + jiraKey);
        } catch (Exception e) {
            Tools.logger.warn("Cannot reopen Jira task by key: " + jiraKey
                    + ", error: " + e.getMessage());
        }

    }

    public void addCommentByKey(String jiraKey, String comment) {

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("body", comment);

        JsonObject jo = restJsonCaller.callRest(Settings.getSettings().getJiraHttpsURL()
                        + String.format("/issue/%s/comment", jiraKey)
                , basicAuth, requestBody, "POST", 201);

        if (jo == null) {
            Tools.logger.warn("Cannot add comment to Jira task by key: " + jiraKey
                    + "\n comment = " + comment);
        }

    }
}
