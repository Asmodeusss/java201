package java201.web;

import java.io.IOException;
import java.util.*;

import java201.config.WebSecurityConfig;
import java201.data.Task;
import java201.data.Worksheet;
import java201.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Controller
public class WelcomeController {

	private final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

	@Autowired
	@Qualifier(value = "taskService")
	TaskService taskService;

	@RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
	public ModelAndView index() {
		String userName = WebSecurityConfig.getAuthentication().getName();
//		Uncomment after implementation of method getByUserName
		if(taskService.getByUsername(userName).size()>0){
			ModelAndView modelAndView = new ModelAndView("results");
			Worksheet lastWorksheet = taskService.getLatest(userName);
			modelAndView.getModelMap().addAttribute("worksheet", lastWorksheet);
			return modelAndView;
		}

		ModelAndView modelAndView = new ModelAndView("welcome");


		logger.debug("index() is executed!");

        modelAndView.getModelMap().addAttribute("title", "Java201");
        modelAndView.getModelMap().addAttribute("msg", "Java201 tasks for trainees");

		// Testing if I can get my passed params back
        modelAndView.getModelMap().addAttribute("name", userName);


		BASE64Encoder encoder = new BASE64Encoder();
        modelAndView.getModelMap().addAttribute("user", encoder.encode(userName.getBytes()));


        return modelAndView;
	}

	@RequestMapping(value = "/button", method = RequestMethod.POST)
	public ModelAndView buttonPress() {
		String userName = WebSecurityConfig.getAuthentication().getName();
		logger.debug("Button press executed");
		logger.debug("Executing Java201 jenkins job generation");
		List<Task> taskList;
		taskList = Java201.getJava201Tasks(userName);

		taskService.saveList(taskService.writeTasksInTaskList(userName, taskList));
		Worksheet worksheet = taskService.getLatest(userName);
		ModelAndView modelAndView = new ModelAndView("results");
		modelAndView.getModelMap().addAttribute("worksheet", worksheet);
		return modelAndView;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginPage(Map<String, Object> model){
		return "login";
	}


	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String indexPage(){
		return "login";
	}



}