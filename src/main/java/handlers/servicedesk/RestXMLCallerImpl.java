package handlers.servicedesk;

import com.google.inject.Singleton;
import core.Settings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
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
@Singleton
public class RestXMLCallerImpl implements RestXMLCaller {
    private static final Logger logger = Logger.getLogger(RestXMLCallerImpl.class);

    private final HttpClient httpclient = HttpClients.createDefault();

    @Override
    public Document callRestDOM(String module, String operation_name, String input_data) {

        String url_txt = Settings.getInstance().getServiceDeskHttpURL() + "/" + module + "?TECHNICIAN_KEY=" + Settings.getInstance().getServiceDeskTechnichianKey();

        InputStream instream = null;
        try {

            url_txt += "&OPERATION_NAME=" + operation_name + (input_data == null ? "" : "&INPUT_DATA=" + URLEncoder.encode(input_data, "UTF-8"));

            HttpGet httpget = new HttpGet(url_txt);
            httpget.addHeader("charset", "utf-8");
            httpget.addHeader("Content-Type", "text/xml;charset=UTF-8");
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 404) {
                logger.error("Got 404 response code. Module = ".concat(module));
                return null;
            }

            if (entity != null) {

                instream = entity.getContent();

                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                return documentBuilder.parse(instream);
            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        return null;

    }
}



