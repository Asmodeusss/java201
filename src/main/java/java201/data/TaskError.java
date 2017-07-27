package java201.data;

import javax.persistence.*;

/**
 * Created by jurijs.petrovs on 7/6/2017.
 */
@Entity
@Table
public class TaskError {

    @Id
    @GeneratedValue
    @Column
    private int errorId;

    @Column
    private String errorName;

    @Column(columnDefinition = "text")
    private String errorDescription;

    @ManyToOne
    @JoinColumn(name = "taskId")
    private Task task;

    public TaskError() {
    }

    public TaskError(String errorName) {
        this.errorName = errorName;
    }

    public int getErrorId() {
        return errorId;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
