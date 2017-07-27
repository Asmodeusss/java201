package java201.data;

import javax.persistence.*;
import java.util.List;

/**
 * Created by jurijs.petrovs on 6/27/2017.
 */
@Entity
@Table
public class Worksheet {

    @Id
    @GeneratedValue
    @Column
    private int worksheetId;

    @Column(unique = true)
    private String worksheetName;

    @Column
    private String ownerName;

    @Column
    private String worksheetDate;

    @Column
    private Double worksheetProgress;

    @OneToMany(mappedBy = "worksheet")
    private List<Task> tasks;

    public int getWorksheetId() {
        return worksheetId;
    }

    public void setWorksheetId(int worksheetId) {
        this.worksheetId = worksheetId;
    }

    public String getWorksheetName() {
        return worksheetName;
    }

    public void setWorksheetName(String worksheetName) {
        this.worksheetName = worksheetName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getWorksheetDate() {
        return worksheetDate;
    }

    public void setWorksheetDate(String worksheetDate) {
        this.worksheetDate = worksheetDate;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Double getWorksheetProgress() {
        return worksheetProgress;
    }

    public void setWorksheetProgress(Double worksheetProgress) {
        this.worksheetProgress = worksheetProgress;
    }

}
