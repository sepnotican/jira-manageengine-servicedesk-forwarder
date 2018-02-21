package handlers.servicedesk;

import repository.TaskModel;

import java.util.ArrayList;

public interface ServiceDeskHandler {
    ArrayList<TaskModel> get_requests(int from, int limit);

    void fillIssueByID(TaskModel taskModel);

    String getFilterIdByName(String filterName);

    String formatHtmlContent(String income);

    boolean completeRequest(int sd_id, String resolution, String assingneeName);

    boolean closeRequest(int sd_id);
}
