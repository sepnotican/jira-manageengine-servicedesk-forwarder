package controller.issueTransport;

import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sepnotican on 07.02.2017.
 * <p>
 * Class for build 'headless' XML for ServiceDesk
 */
@Singleton
public class HeadlessXMLBuilder {

    public String makeTag(String tag, String value) {
        return "<" + tag + ">" + value + "</" + tag + ">";
    }

    public String processParams(Map<String, Object> map) {

        StringBuilder result = new StringBuilder();

        for (HashMap.Entry<String, Object> kv : map.entrySet()) {
            if (kv.getValue() instanceof String)
                result.append(makeTag("parameter", makeTag("name", kv.getKey()) + makeTag("value", (String) kv.getValue())));

        }

        return result.toString();

    }

}
