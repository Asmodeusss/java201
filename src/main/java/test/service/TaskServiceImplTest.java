package test.service;

import java201.data.Task;
import java201.data.Worksheet;
import java201.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringRunner;
import test.SpringTesting;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by jurijs.petrovs on 6/28/2017.
 */
@RunWith(SpringRunner.class)
public class TaskServiceImplTest extends SpringTesting {

    @Autowired
    @Qualifier(value = "taskService")
    TaskService taskService;

    @Test
    public void testWriteTasksInTaskList() throws Exception {
        List<Task> taskList = new ArrayList<>();
        Task task1 = new Task();
        Task task2 = new Task();
        taskList.add(task1);
        taskList.add(task2);
        String username = "DefaultUser";
        Object object = taskService.writeTasksInTaskList(username, taskList);
        assertTrue(object instanceof Worksheet);
    }

}