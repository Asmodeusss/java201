package java201.service;

import java201.dao.TaskDAO;
import java201.dao.WorksheetDAO;
import java201.data.Task;
import java201.data.TaskError;
import java201.data.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
@Service("taskService")
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private WorksheetDAO worksheetDAO;

    @Override
    public Worksheet writeTasksInTaskList(String username, List<Task> tasks) {
        Worksheet worksheet = new Worksheet();
        worksheet.setTasks(tasks);
        worksheet.setOwnerName(username);
        worksheet.setWorksheetProgress(getAverageProgress(tasks));
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        worksheet.setWorksheetDate(dateFormat.format(date));
        String taskListName = "Java201_" + username + "_" + worksheet.getWorksheetDate();
        worksheet.setWorksheetName(taskListName);
        return worksheet;
    }

    @Override
    public void saveList(Worksheet list) {
        worksheetDAO.add(list);
        for (Task task : list.getTasks()) {
            task.setWorksheet(list);
            taskDAO.add(task);
            for (TaskError taskError : task.getTaskErrors()) {
                taskError.setTask(task);
                taskDAO.saveError(taskError);
            }
        }
    }


    @Override
    public List<Worksheet> getByUsername(String username) {
        return worksheetDAO.getByUsername(username);
    }

    @Override
    public Worksheet getLatest(String username) {
        return worksheetDAO.getLatest(username);
    }

    @Override
    public Set<Task> getAllTasks(int taskListId) {
        return null;
    }

    @Override
    public Task getTaskById(int id) {
        return taskDAO.get(id);
    }

    @Override
    public void saveErrors(Set<TaskError> errors) {
        for (TaskError taskError : errors) {
            taskDAO.saveError(taskError);
        }
    }

    public double getAverageProgress(List<Task> list) {
        double progress = 0;
        for (Task task : list) {
            progress += task.getTaskProgress();
        }
        if (progress == 0) {
            return 0;
        } else {
            return progress/list.size();
        }
    }

}
