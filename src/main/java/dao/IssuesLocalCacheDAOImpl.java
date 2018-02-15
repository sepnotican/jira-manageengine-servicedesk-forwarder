package dao;

import com.google.inject.Singleton;
import core.Settings;
import entity.TaskModel;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by muzafar on 6/14/17.
 */
@Singleton
public class IssuesLocalCacheDAOImpl extends SQLiteDao implements IssuesLocalCacheDAO {

    private static IssuesLocalCacheDAOImpl self = null;

    private IssuesLocalCacheDAOImpl() {

        String[] sqliteUrlSplitted = Settings.getInstance().getDatabaseURL().split(":");

        File fileDB = new File(sqliteUrlSplitted[2]);
        if (!fileDB.exists())
            createTaskTransferStructure();

    }

    public static IssuesLocalCacheDAOImpl getSelf() {
        if (self == null)
            self = new IssuesLocalCacheDAOImpl();
        return self;
    }

    @Override
    public void createTaskTransferStructure() {
        sqlOpen();
        try {
            String sql;
            sql = "CREATE TABLE IF NOT EXISTS taskJiraCreated (" +
                    " sd_id INTEGER NOT NULL PRIMARY KEY," +
                    " jira_key VARCHAR (14)," +
                    " resolution VARCHAR(1024) " +
                    ")";
            s.addBatch(sql);
            s.executeBatch();

        } catch (SQLException e) {
            showSQLException(e);
        }
        sqlClose();
        logger.warn("Database created.");
    }

    @Override
    public void fillTasksCachedInfo(List<TaskModel> taskModelList) {

        final StringBuilder sql = new StringBuilder("SELECT sd_id, jira_key" +
                " FROM taskJiraCreated" +
                " WHERE sd_id IN (");

        taskModelList.forEach(taskModel -> {
            sql.append(taskModel.getId_sd());
            if (taskModelList.indexOf(taskModel) != taskModelList.size() - 1) sql.append(',');
        });

        sql.append(")");

        try {
            sqlOpen();

            rs = s.executeQuery(sql.toString());

            while (rs.next()) {

                for (int i = taskModelList.size() - 1; i >= 0; i--) {
                    { //fill already added issues
                        if (taskModelList.get(i).getId_sd() == rs.getInt("sd_id")) {
                            taskModelList.get(i).setJiraKey(rs.getString("jira_key"));
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        } finally {
            sqlClose();
        }
    }

    @Override
    public void updateTaskByJiraKey(TaskModel taskModel) {

        String resolution = taskModel.getResolution();
        if (resolution != null)
            resolution = "'" + resolution + "'";
        final String sql = String.format("INSERT OR REPLACE INTO taskJiraCreated VALUES(%d, '%s', %s)"
                , taskModel.getId_sd()
                , taskModel.getJiraKey()
                , resolution);

        try {
            sqlOpen();
            int result = s.executeUpdate(sql);

            if (result > 0)
                logger.info("Local cache has been updated. Issue key: " + taskModel.getJiraKey()
                        + ", sd id: " + taskModel.getId_sd()
                        + ", resolution: " + taskModel.getResolution());

        } catch (SQLException e) {
            showSQLException(e);
        } finally {
            sqlClose();
        }

    }

    @Override
    public void clearTaskCache() {
        final String sql = "DELETE FROM taskJiraCreated";

        try {
            sqlOpen();
            s.execute(sql);
            logger.warn("Task cache in table 'taskJiraCreated' has been cleared");

        } catch (SQLException e) {
            showSQLException(e);
        } finally {
            sqlClose();
        }
    }
}
