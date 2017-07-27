package java201.dao;

import java201.data.Task;
import java201.data.TaskError;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
public interface TaskDAO {

    void add(Task task);

    Task get(int id);

    void remove(int id);

    Task getByName(String name);

    void saveError(TaskError taskError);
}
