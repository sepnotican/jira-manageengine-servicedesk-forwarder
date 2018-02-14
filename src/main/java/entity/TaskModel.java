package entity;

/**
 * Created by muzafar on 2/28/17.
 */
public class TaskModel {
//

    private int id_sd = 0;
    private String jiraKey = null;
    private String requester = null;
    private String assingnee = null;
    private String summary = null;
    private String description = null;
    private String resolution = null;
    private int jiraStatus = -1;
//    private boolean addedToJira;

    public TaskModel() {
    }

    public TaskModel(int id_sd, String jiraKey) {
        this.id_sd = id_sd;
        this.jiraKey = jiraKey;
    }

    public TaskModel(int id_sd, String jiraKey, String requester, String assingnee, String summary, String description, String resolution, int jiraStatus) {
        this.id_sd = id_sd;
        this.jiraKey = jiraKey;
        this.requester = requester;
        this.assingnee = assingnee;
        this.summary = summary;
        this.description = description;
        this.resolution = resolution;
        this.jiraStatus = jiraStatus;
    }

    public int getJiraStatus() {
        return jiraStatus;
    }

    public void setJiraStatus(int jiraStatus) {
        this.jiraStatus = jiraStatus;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }

    public int getId_sd() {
        return id_sd;
    }

    public void setId_sd(int id_sd) {
        this.id_sd = id_sd;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getAssingnee() {
        return assingnee;
    }

    public void setAssingnee(String assingnee) {
        this.assingnee = assingnee;
    }

    public boolean jiraKeyIsEmpty() {
        return jiraKey == null;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
