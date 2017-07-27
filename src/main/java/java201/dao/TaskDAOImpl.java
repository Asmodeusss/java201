package java201.dao;

import java201.data.Task;
import java201.data.TaskError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
@EnableTransactionManagement
@Repository
public class TaskDAOImpl implements TaskDAO {

    @Autowired
    EntityManager entityManager;

    private final Logger logger = LoggerFactory.getLogger(TaskDAOImpl.class);

    @Transactional
    @Override
    public void add(Task task) {
        entityManager.persist(task);
    }

    @Transactional
    @Override
    public Task get(int id) {
        return entityManager.find(Task.class, id);
    }

    @Transactional
    @Override
    public void remove(int id) {
        Task task = entityManager.find(Task.class, id);
        if (task != null) {
            entityManager.remove(task);
        } else {
            logger.error("Cannot remove empty object");
        }
    }

    @Transactional
    @Override
    public Task getByName(String name) {
        List<Task> tasks;
        Query query = entityManager.createQuery("from Task where taskName = :name");
        query.setParameter("name", name);
        tasks = query.getResultList();
        return tasks.get(0);
    }

    @Transactional
    @Override
    public void saveError(TaskError taskError) {
        entityManager.persist(taskError);
    }
}
