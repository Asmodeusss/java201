package java201.service;

import java201.data.Task;
import java201.data.TaskError;
import java201.data.Worksheet;

import java.util.List;
import java.util.Set;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
public interface TaskService {

    Worksheet writeTasksInTaskList(String username, List<Task> tasks);

    void saveList(Worksheet list);

    List<Worksheet> getByUsername(String username);

    Worksheet getLatest(String username);

    Set<Task> getAllTasks(int taskListId);

    Task getTaskById(int id);

    void saveErrors(Set <TaskError> errors);

}
