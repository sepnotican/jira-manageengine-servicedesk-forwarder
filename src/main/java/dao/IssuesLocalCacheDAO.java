package dao;

import entity.TaskModel;

import java.util.List;

/**
 * Created by muzafar on 6/14/17.
 */
public interface IssuesLocalCacheDAO {

    IssuesLocalCacheDAO instance = IssuesLocalCacheDAOImpl.getSelf();

    void createTaskTransferStructure();

    void fillTasksCachedInfo(List<TaskModel> taskModelList);

    void updateTaskByJiraKey(TaskModel taskModel);

    void clearTaskCache();
}
