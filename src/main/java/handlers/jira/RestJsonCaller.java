package handlers.jira;

import com.google.gson.JsonObject;
import handlers.jira.IssueNotFoundException;

public interface RestJsonCaller {
    JsonObject callRest(String url, String basicAuth, JsonObject jo, String method, int successCode) throws IssueNotFoundException;
}
