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
    private static final String SETTING_FILE_NAME = APP_ROOT_DIR + "/instance.json";
    private static Logger logger = Logger.getLogger(Settings.class);
    private static Settings instance = null;

    private String jiraBasicAuth = null;
    private int jiraStatusIdResolved;
    private int jiraTransitionIdResolvedToTodo;
    private String jiraHttpsURL = null;
    private String jiraDefaultProject = null;

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
    private boolean checkIssuesInJiraByTextstampBeforeCreate;
    private String databaseURL = "";

    private Settings() {
    }

    public static Settings getInstance() {
        if (instance == null)
            instance = new Settings();
        return instance;
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

    public boolean isCheckIssuesInJiraByTextstampBeforeCreate() {
        return checkIssuesInJiraByTextstampBeforeCreate;
    }

    public void setCheckIssuesInJiraByTextstampBeforeCreate(boolean checkIssuesInJiraByTextstampBeforeCreate) {
        this.checkIssuesInJiraByTextstampBeforeCreate = checkIssuesInJiraByTextstampBeforeCreate;
    }

    public void createSettings() {

        getInstance();
        instance.setServiceDeskTechnichianKey("aaaa-sss-ddd-ffffff");
        instance.setServiceDeskHttpURL("http://servicedesk.example.com");
        instance.setJiraHttpsURL("https://jira.example.com/rest/api/2");
        instance.setJiraBasicAuth("jira_user:passwd");
        instance.setServicedeskTransportTimeoutSec(180);
        instance.setServicedeskForwardViewName("JIRA_AUTOCREATE_FILTER_NAME");
        instance.setJiraResolutionField("100100");
        instance.setJiraServiceDeskIDField("200200");
        instance.setServicedeskStamp("servicedesk ##%s##"); //%s - Servicedesk Task ID
        instance.setJiraStatusIdResolved(5);
        instance.setJiraTransitionIdResolvedToTodo(191);
        instance.setDatabaseURL("jdbc:sqlite:base.db");
        instance.setJiraDefaultProject("DEV");
        instance.setCheckIssuesInJiraByTextstampBeforeCreate(true);

        Settings.instance.setModuleIssueTransortActive(false);

        Settings.instance.save();
    }

    public String getJiraDefaultProject() {
        return jiraDefaultProject;
    }

    public void setJiraDefaultProject(String jiraDefaultProject) {
        this.jiraDefaultProject = jiraDefaultProject;
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
            StringBuilder stringBuilder = new StringBuilder();
            while ((i = br.read()) > -1) {
                stringBuilder.append((char) (i));
            }

            Gson gson = new Gson();
            Settings.instance = gson.fromJson(stringBuilder.toString(), Settings.class);

        } catch (FileNotFoundException e) {
            logger.info("Run with --createSettings");
            System.exit(-1);
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fin != null)
                    fin.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
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
            logger.error(e.getMessage());
        } finally {
            try {
                if (fout != null) {
                    fout.flush();
                    fout.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
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
