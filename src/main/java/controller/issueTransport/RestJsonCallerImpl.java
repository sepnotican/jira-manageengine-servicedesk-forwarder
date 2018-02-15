package controller.issueTransport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Singleton;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import service.IssueNotFoundException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Zafar on 07.02.2017.
 */
@Singleton
public class RestJsonCallerImpl implements RestJsonCaller {

    private static final Logger logger = Logger.getLogger(RestJsonCallerImpl.class);
    private CloseableHttpClient httpclient = null;

    public RestJsonCallerImpl() {

        // Trust own CA and all self-signed certs
        SSLContext sslcontext;
        try {
            sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{
                            "TLSv1",
                            "TLSv1.1",
                            "TLSv1.2"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public JsonObject callRest(String url, String basicAuth, JsonObject jo, String method, int successCode)
            throws IssueNotFoundException {

        JsonObject result = null;

        CloseableHttpResponse response = null;
        HttpRequestBase httpRequest = null;
        try {
            if (method.equals("GET")) {
                httpRequest = new HttpGet(url);
            } else if (method.equals("POST")) {
                HttpPost httpPost = new HttpPost(url);
                HttpEntity entity = new ByteArrayEntity(jo.toString().getBytes());
                httpPost.setEntity(entity);
                httpRequest = httpPost;
            } else throw new IllegalArgumentException("GET or POST method only acceptable");
            httpRequest.addHeader("Content-Type", "application/json");
            httpRequest.addHeader("Authorization", "Basic " + basicAuth);
            httpRequest.addHeader("charset", "utf-8");

            response = httpclient.execute(httpRequest);
            HttpEntity entity = response.getEntity();

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            if (entity != null) {

                InputStream instream = entity.getContent();
                BufferedInputStream bis = new BufferedInputStream(instream);

                int i = bis.read();
                while (i != -1) {
                    buf.write((byte) i);
                    i = bis.read();
                }

                bis.close();
                buf.flush();

            }
            if (response.getStatusLine().getStatusCode() != successCode) {
                logger.error("Response code is not correct. Expected. :" +
                        successCode + ", got: " + response.getStatusLine().getStatusCode()
                        + " message : \n" + buf.toString());
                return null;
            }

            if (buf.toString() != null && buf.size() > 0) {
                JsonElement element = new JsonParser().parse(buf.toString());
                if (element.isJsonObject())
                    result = element.getAsJsonObject();
            }
            EntityUtils.consume(entity);

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new IssueNotFoundException(e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
        }

        return result;
    }


    private static class TrustEverythingTrustManager implements X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    }

    private static class VerifyEverythingHostnameVerifier implements HostnameVerifier {

        public boolean verify(String string, SSLSession sslSession) {
            return true;
        }
    }

}

