package java201.dao;

import java201.data.Worksheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
@EnableTransactionManagement
@Repository
public class WorksheetDAOImpl implements WorksheetDAO {

    @PersistenceContext
    EntityManager entityManager;

    private final Logger logger = LoggerFactory.getLogger(WorksheetDAOImpl.class);

    @Transactional
    @Override
    public List<Worksheet> getByUsername(String username) {
        List<Worksheet> tasklists;
        Query query = entityManager.createQuery("from Worksheet where ownerName = :username");
        query.setParameter("username", username);
        tasklists = query.getResultList();
        return tasklists;
    }

    @Transactional
    @Override
    public List<Worksheet> getAllWorksheets() {
        List<Worksheet> allWorksheets;
        Query query = entityManager.createQuery("from Worksheet");
        allWorksheets = query.getResultList();
        return allWorksheets;
    }

    @Override
    public Worksheet getBestUserWorksheet(String username) {
        List<Worksheet> allUserWorksheets;
        Query query = entityManager.createQuery("from Worksheet Order By worksheetProgress");
        allUserWorksheets = query.getResultList();
        return allUserWorksheets.get(0);
    }

    @Transactional
    @Override
    public Worksheet getLatest(String username) {
        List<Worksheet> allWorksheets = getByUsername(username);
        if (allWorksheets.size() > 0) {
            return allWorksheets.get(allWorksheets.size() - 1);
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public void add(Worksheet list) {
        entityManager.persist(list);
    }

    @Transactional
    @Override
    public void remove(int id) {
        Worksheet worksheet = entityManager.find(Worksheet.class, id);
        if (worksheet != null) {
            entityManager.remove(worksheet);
        } else {
            logger.error("Attempt to remove empty object");
        }
    }

    @Transactional
    @Override
    public Worksheet getById(int id) {
        return entityManager.find(Worksheet.class, id);
    }

    @Transactional
    @Override
    public Worksheet getByWorksheetName(String name) {
        List<Worksheet> worksheets;
        Query query = entityManager.createQuery("from Worksheet where worksheetName = :name");
        query.setParameter("name", name);
        worksheets = query.getResultList();
        return worksheets.get(0);
    }
}
