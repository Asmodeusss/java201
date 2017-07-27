package java201.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jurijs.petrovs on 7/13/2017.
 */
public interface AdminService {

    Set<String> getAllUsers();

    Map<String, List<Object>> createUserInfo(Set<String> allUsers);
}
