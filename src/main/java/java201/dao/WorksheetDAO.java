package java201.dao;

import java201.data.Worksheet;
import java.util.List;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
public interface WorksheetDAO {

    Worksheet getLatest(String username);

    void add(Worksheet list);

    void remove(int id);

    Worksheet getById(int id);

    Worksheet getByWorksheetName(String name);

    List<Worksheet> getByUsername(String username);

    List<Worksheet> getAllWorksheets();

    Worksheet getBestUserWorksheet(String username);
}
