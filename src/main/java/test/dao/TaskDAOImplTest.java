package test.dao;

import java201.dao.TaskDAO;
import java201.data.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import test.SpringTesting;

import javax.persistence.PersistenceException;

import static org.junit.Assert.*;

/**
 * Created by jurijs.petrovs on 6/28/2017.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskDAOImplTest extends SpringTesting {

    @Autowired
    TaskDAO taskDAO;


    @Test
    public void testAdd() throws Exception {
        Task task = new Task();
        task.setTaskName("Example Task");
        task.setTaskLink("Example Link");
        task.setTaskProgress(1.0);
        taskDAO.add(task);
        Task taskFromDB = taskDAO.getByName("Example Task");

        assertEquals("Example Task", taskFromDB.getTaskName());
        assertEquals("Example Link", taskFromDB.getTaskLink());
    }

    @Test
    public void testRemove() {
        Task task = new Task();
        task.setTaskName("Example Task");
        task.setTaskLink("Example Link");
        task.setTaskProgress(1.0);
        taskDAO.add(task);

        assertNotNull(taskDAO.getByName("Example Task"));
        int id = taskDAO.getByName("Example Task").getTaskId();
        taskDAO.remove(id);
        assertNull(taskDAO.get(id));
    }

    @Test(expected = PersistenceException.class)
    public void testCheckingForUniqueName() {
        Task task = new Task();
        task.setTaskName("NotUniqueTaskName");
        taskDAO.add(task);

        Task duplicateTask = new Task();
        duplicateTask.setTaskName("NotUniqueTaskName");

        taskDAO.add(duplicateTask);
    }

}