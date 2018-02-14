package controller.issueTransport;

import com.google.gson.JsonObject;

public interface RestJsonCaller {
    JsonObject callRest(String url, String basicAuth, JsonObject jo, String method, int successCode);
}
