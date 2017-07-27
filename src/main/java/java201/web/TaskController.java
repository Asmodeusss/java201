package java201.web;

import java201.config.WebSecurityConfig;
import java201.data.Task;
import java201.data.TaskError;
import java201.data.Worksheet;
import java201.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.util.Set;


/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "/results", method = RequestMethod.GET)
    public ModelAndView resultPage() {
        String user = WebSecurityConfig.getAuthentication().getName();
        ModelAndView modelAndView = new ModelAndView("results");
        Worksheet lastWorksheet = taskService.getLatest(user);

        modelAndView.getModelMap().addAttribute("worksheet", lastWorksheet);
        return modelAndView;
    }

    @RequestMapping(value = "/errors", method = RequestMethod.GET)
    public ModelAndView errorsPage(@RequestParam int id) {
        ModelAndView modelAndView = new ModelAndView("errors");
        Task task = taskService.getTaskById(id);
        Set<TaskError> errorSet = task.getTaskErrors();
        modelAndView.getModelMap().addAttribute("newLineChar", '\n');
        modelAndView.getModelMap().addAttribute("tabChar", '\t');
        modelAndView.getModelMap().addAttribute("errorSet", errorSet);
        return modelAndView;
    }
}

