package service;

import entity.TaskModel;

public interface JiraHandler {

    TaskModel getIssueByID(int sd_id, QueryMode queryMode) throws IssueNotFoundException;

    boolean createIssueInJira(TaskModel taskModel);

    boolean createIssueInJira(TaskModel taskModel, boolean onlyBuildQuery);

    void reopenTaskByKey(String jiraKey);

    void addCommentByKey(String jiraKey, String comment);

    enum QueryMode {
        BY_TEXTSTAMP,
        BY_SD_ID_CUSTOMFIELD
    }
}
