package repository;

import java.util.List;

/**
 * Created by muzafar on 6/14/17.
 */
public interface IssuesLocalCacheRepo {

    IssuesLocalCacheRepo instance = IssuesLocalCacheRepoImpl.getSelf();

    void createTaskTransferStructure();

    void fillCachedJiraID(List<TaskModel> taskModelList);

    void updateTaskByJiraKey(TaskModel taskModel);

    void clearTaskCache();
}
