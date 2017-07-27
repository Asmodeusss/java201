package java201.data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by rudolfs.kazimirenoks on 14-Dec-16.
 */
@Entity
@Table
public class Task implements Serializable {

    @Id
    @GeneratedValue
    @Column
    private int taskId;

    @Column
    private String taskName;

    @Column
    private String taskLink;

    @Column
    private Double taskProgress;

    @ManyToOne
    @JoinColumn(name = "worksheetId")
    private Worksheet worksheet;

    @OneToMany(mappedBy = "task")
    private Set<TaskError> taskErrors;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskLink() {
        return taskLink;
    }
    public void setTaskLink(String taskLink) {
        this.taskLink = taskLink;
    }

    public Double getTaskProgress() {
        return taskProgress;
    }
    public void setTaskProgress(Double taskProgress) {
        this.taskProgress = taskProgress;
    }

    public Worksheet getWorksheet() {
        return worksheet;
    }

    public void setWorksheet(Worksheet worksheet) {
        this.worksheet = worksheet;
    }

    public Set<TaskError> getTaskErrors() {
        return taskErrors;
    }

    public void setTaskErrors(Set<TaskError> taskErrors) {
        this.taskErrors = taskErrors;
    }

    public int getErrorsCount() {
        return taskErrors.size();
    }
}