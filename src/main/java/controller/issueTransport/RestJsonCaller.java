package controller.issueTransport;

import com.google.gson.JsonObject;
import service.IssueNotFoundException;

public interface RestJsonCaller {
    JsonObject callRest(String url, String basicAuth, JsonObject jo, String method, int successCode) throws IssueNotFoundException;
}
