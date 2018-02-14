package controller.issueTransport;

import core.Settings;
import core.Tools;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;


/**
 * Created by Zafar on 07.02.2017.
 */
@Component
public class RestXMLCallerImpl {

    private final HttpClient httpclient = HttpClients.createDefault();

    public Document callRestDOM(String module, String operation_name, String input_data) {

        String url_txt = Settings.getSettings().getServiceDeskHttpURL() + "/" + module + "?TECHNICIAN_KEY=" + Settings.getSettings().getServiceDeskTechnichianKey();

        InputStream instream = null;
        try {

            url_txt += "&OPERATION_NAME=" + operation_name + (input_data == null ? "" : "&INPUT_DATA=" + URLEncoder.encode(input_data, "UTF-8"));

            HttpGet httpget = new HttpGet(url_txt);
            httpget.addHeader("charset", "utf-8");
            httpget.addHeader("Content-Type", "text/xml;charset=UTF-8");
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 404) {
                Tools.logger.error("Got 404 response code. Module = ".concat(module));
                return null;
            }

            if (entity != null) {

                instream = entity.getContent();

                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(instream); //DOM

                return document;
            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            Tools.logger.error(e.getMessage());
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                Tools.logger.error(e.getMessage());
            }
        }

        return null;

    }
}



