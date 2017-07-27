package java201.web;

import java201.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Created by jurijs.petrovs on 7/13/2017.
 */
@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "/admin")
    public ModelAndView adminPage() {
        ModelAndView modelAndView = new ModelAndView("admin");
        Map<String, List<Object>> userInfo = adminService.createUserInfo(adminService.getAllUsers());
        modelAndView.getModelMap().addAttribute("userInfo", userInfo);
        return modelAndView;
    }
}
