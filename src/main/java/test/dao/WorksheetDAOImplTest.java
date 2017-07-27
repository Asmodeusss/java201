package test.dao;

import java201.dao.WorksheetDAO;
import java201.data.Worksheet;
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
 * Created by jurijs.petrovs on 6/29/2017.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WorksheetDAOImplTest extends SpringTesting {

    @Autowired
    WorksheetDAO worksheetDAO;

    @Test
    public void getByUsername() throws Exception {
    }

    @Test
    public void getLatest() throws Exception {
    }

    @Test
    public void testAdd() throws Exception {
        Worksheet worksheet = new Worksheet();
        worksheet.setWorksheetName("Example TaskListName");
        worksheet.setOwnerName("Example Worksheet Owner");
        worksheet.setWorksheetDate("2017/6/29 12:30:00");
        worksheetDAO.add(worksheet);

        Worksheet worksheetFromDB = worksheetDAO.getByWorksheetName("Example TaskListName");

        assertEquals("Example TaskListName", worksheetFromDB.getWorksheetDate());
        assertEquals("Example Worksheet Owner", worksheetFromDB.getOwnerName());
        assertEquals("2017/6/29 12:30:00", worksheetFromDB.getWorksheetDate());
    }

    @Test
    public void testRemove() throws Exception {
        Worksheet worksheet = new Worksheet();
        worksheet.setWorksheetName("Example TaskListName");
        worksheet.setOwnerName("Example Worksheet Owner");
        worksheet.setWorksheetDate("2017/6/29 12:30:00");
        worksheetDAO.add(worksheet);

        assertNotNull(worksheetDAO.getByWorksheetName("Example TaskListName"));
        int id = worksheetDAO.getByWorksheetName("Example TaskListName").getWorksheetId();
        worksheetDAO.remove(id);
        assertNull(worksheetDAO.getById(id));
    }

    @Test(expected = PersistenceException.class)
    public void testCheckingForUniqueName() {
        Worksheet worksheet = new Worksheet();
        worksheet.setWorksheetDate("NotUniqueTaskListName");
        worksheetDAO.add(worksheet);

        Worksheet duplicateWorksheet = new Worksheet();
        duplicateWorksheet.setWorksheetName("NotUniqueTaskListName");

        worksheetDAO.add(duplicateWorksheet);
    }

}