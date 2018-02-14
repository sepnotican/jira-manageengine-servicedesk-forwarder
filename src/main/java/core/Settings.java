package core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by muzafar on 2/10/17.
 */
public class Settings {

    private static final String APP_ROOT_DIR = System.getProperty("user.dir");
    private static final String SETTING_FILE_NAME = APP_ROOT_DIR + "/settings.json";
    private static Logger logger = Tools.logger;
    private static Settings settings = null;

    private String jiraBasicAuth = null;
    private int jiraStatusIdResolved;
    private int jiraTransitionIdResolvedToTodo;
    private String jiraHttpsURL = null;

    private String serviceDeskHttpURL = null;
    private String servicedeskForwardViewName = null;
    private String serviceDeskTechnichianKey = null;
    private String servicedeskStamp = "";
    private String jiraResolutionField = null;
    private String jiraServiceDeskIDField = null;

    private int servicedeskTransportTimeoutSec = -1;

    private boolean moduleIssueTransortActive = false;
    private boolean closeTaskInSDWhenJiraClosed;
    private boolean reopenTaskInJiraWhenSDReopen;
    private String databaseURL = "";

    private Settings() {
    }

    public static Settings getSettings() {
        if (settings == null)
            settings = new Settings();
        return settings;
    }

    public static String getAppRootDir() {
        return APP_ROOT_DIR;
    }

    public String getJiraResolutionField() {
        return jiraResolutionField;
    }

    public void setJiraResolutionField(String jiraResolutionField) {
        this.jiraResolutionField = jiraResolutionField;
    }

    public void createSettings() {

        getSettings();
        settings.setServiceDeskTechnichianKey("aaaa-sss-ddd-ffffff");
        settings.setServiceDeskHttpURL("http://servicedesk.example.com");
        settings.setJiraHttpsURL("https://jira.example.com/rest/api/2");
        settings.setJiraBasicAuth("jira_user:passwd");
        settings.setServicedeskTransportTimeoutSec(180);
        settings.setServicedeskForwardViewName("JIRA_AUTOCREATE_FILTER_NAME");
        settings.setJiraResolutionField("customfield_100");
        settings.setJiraServiceDeskIDField("customfield_200");
        settings.setServicedeskStamp("servicedesk ##%s##");
        settings.setJiraStatusIdResolved(5);
        settings.setJiraTransitionIdResolvedToTodo(191);
        settings.setDatabaseURL("jdbc:sqlite:base.db");

        Settings.settings.setModuleIssueTransortActive(false);

        Settings.settings.save();
    }

    public String getServicedeskStamp() {
        return servicedeskStamp;
    }

    public void setServicedeskStamp(String servicedeskStamp) {
        this.servicedeskStamp = servicedeskStamp;
    }

    public int getJiraStatusIdResolved() {
        return jiraStatusIdResolved;
    }

    public void setJiraStatusIdResolved(int jiraStatusIdResolved) {
        this.jiraStatusIdResolved = jiraStatusIdResolved;
    }

    public String getJiraServiceDeskIDField() {
        return jiraServiceDeskIDField;
    }

    public void setJiraServiceDeskIDField(String jiraServiceDeskIDField) {
        this.jiraServiceDeskIDField = jiraServiceDeskIDField;
    }

    public int getJiraTransitionIdResolvedToTodo() {
        return jiraTransitionIdResolvedToTodo;
    }

    public void setJiraTransitionIdResolvedToTodo(int jiraTransitionIdResolvedToTodo) {
        this.jiraTransitionIdResolvedToTodo = jiraTransitionIdResolvedToTodo;
    }

    public synchronized void load() {
        FileInputStream fin = null;
        BufferedReader br = null;

        try {
            fin = new FileInputStream(SETTING_FILE_NAME);
            br = new BufferedReader(new InputStreamReader(fin));

            int i;
            StringBuilder settings = new StringBuilder();
            while ((i = br.read()) > -1) {
                settings.append((char) (i));
            }

            Gson gson = new Gson();
            Settings.settings = gson.fromJson(settings.toString(), Settings.class);

        } catch (FileNotFoundException e) {
            logger.info("Run with --createSettings");
            System.exit(1);
        } catch (IOException e) {
            Tools.logger.error(e.getMessage());
        } finally {
            try {
                br.close();
                fin.close();
            } catch (NullPointerException | IOException e) {
                Tools.logger.error(e.getMessage());
            }
        }
    }

    public String getServicedeskForwardViewName() {
        return servicedeskForwardViewName;
    }

    public void setServicedeskForwardViewName(String servicedeskForwardViewName) {
        this.servicedeskForwardViewName = servicedeskForwardViewName;
    }

    public String getServiceDeskTechnichianKey() {
        return serviceDeskTechnichianKey;
    }

    public void setServiceDeskTechnichianKey(String serviceDeskTechnichianKey) {
        this.serviceDeskTechnichianKey = serviceDeskTechnichianKey;
    }

    public String getJiraBasicAuth() {
        return jiraBasicAuth;
    }

    public void setJiraBasicAuth(String jiraBasicAuth) {
        this.jiraBasicAuth = jiraBasicAuth;
    }

    public String getServiceDeskHttpURL() {
        return serviceDeskHttpURL;
    }

    public void setServiceDeskHttpURL(String serviceDeskHttpURL) {
        this.serviceDeskHttpURL = serviceDeskHttpURL;
    }

    public String getJiraHttpsURL() {
        return jiraHttpsURL;
    }

    public void setJiraHttpsURL(String jiraHttpsURL) {
        this.jiraHttpsURL = jiraHttpsURL;
    }

    public int getServicedeskTransportTimeoutSec() {
        return servicedeskTransportTimeoutSec;
    }

    public void setServicedeskTransportTimeoutSec(int servicedeskTransportTimeoutSec) {
        this.servicedeskTransportTimeoutSec = servicedeskTransportTimeoutSec;
    }

    public boolean isModuleIssueTransortActive() {
        return moduleIssueTransortActive;
    }

    public void setModuleIssueTransortActive(boolean moduleIssueTransortActive) {
        this.moduleIssueTransortActive = moduleIssueTransortActive;
    }

    public void save() {

        FileOutputStream fout = null;

        try {
            fout = new FileOutputStream(SETTING_FILE_NAME);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            byte[] bytes = gson.toJson(this).getBytes();

            fout.write(bytes);

        } catch (IOException e) {
            Tools.logger.error(e.getMessage());
        } finally {
            try {
                fout.flush();
                fout.close();
            } catch (NullPointerException | IOException e) {
                Tools.logger.error(e.getMessage());
            }
        }
    }

    public boolean isCloseTaskInSDWhenJiraClosed() {
        return closeTaskInSDWhenJiraClosed;
    }

    public void setCloseTaskInSDWhenJiraClosed(boolean closeTaskInSDWhenJiraClosed) {
        this.closeTaskInSDWhenJiraClosed = closeTaskInSDWhenJiraClosed;
    }

    public boolean isReopenTaskInJiraWhenSDReopen() {
        return reopenTaskInJiraWhenSDReopen;
    }

    public void setReopenTaskInJiraWhenSDReopen(boolean reopenTaskInJiraWhenSDReopen) {
        this.reopenTaskInJiraWhenSDReopen = reopenTaskInJiraWhenSDReopen;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }
}
