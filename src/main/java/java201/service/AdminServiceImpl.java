package java201.service;

import java201.dao.WorksheetDAO;
import java201.data.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jurijs.petrovs on 7/13/2017.
 */
@Service("adminService")
public class AdminServiceImpl implements AdminService {

    @Autowired
    private WorksheetDAO worksheetDAO;

    @Override
    public Set<String> getAllUsers() {
        Set<String> allUsers = new HashSet<>();
        List<Worksheet> allWorksheets = worksheetDAO.getAllWorksheets();
        for (Worksheet worksheet : allWorksheets) {
            allUsers.add(worksheet.getOwnerName());
        }
        return allUsers;
    }

    @Override
    public Map<String, List<Object>> createUserInfo(Set<String> allUsers) {
        Map<String, List<Object>> userInfo = new HashMap<>();
        for (String user : allUsers) {
            int userAttempts = worksheetDAO.getByUsername(user).size();
            Double lastUserWorksheetProgress = worksheetDAO.getLatest(user).getWorksheetProgress();
            userInfo.put(user, Arrays.asList(user, userAttempts, lastUserWorksheetProgress));
        }
        return userInfo;
    }
}
