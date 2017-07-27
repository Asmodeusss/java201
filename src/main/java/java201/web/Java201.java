package java201.web;

import java201.config.WebSecurityConfig;
import java201.data.Task;
import java201.data.TaskError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.BASE64Encoder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;


/**
 * Created by iaroslav.morozov on 11/21/2016.
 */
public class Java201 {
    //Attempts to connect to ADOP platform with provided credentials
    //Might not be needed anymore
    private  int  connect(String user){
        int code=0;
        BASE64Encoder encoder = new BASE64Encoder();
        String log_in=user+":"+user;
        String authString = "Basic " + encoder.encode(log_in.getBytes());
        try {
            URL url = new URL("http://54.154.224.21/");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Authorization", authString);
            code = connection.getResponseCode();
            return code;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }

    //Creates workspace and registers new user
    private int generate_workspace(String user){
        int code=0;
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        try {
            URL url = new URL ("http://54.154.224.21/jenkins/job/"+user+"Java201");
            String authString = "Basic " + encoder.encode(userPass.getBytes());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Authorization", authString);
            code = connection.getResponseCode();
            if (code==404) {
                String params = "WORKSPACE_NAME=" + user + "Java201&ADMIN_USERS=" + user;
                url = new URL("http://54.154.224.21/jenkins/job/Workspace_Management/job/Generate_Workspace/buildWithParameters");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                os.write(params.getBytes());
                os.flush();
                os.close();
                code = connection.getResponseCode();
            }else{
                System.out.println("Workspace "+ user+"Java201 already exists, skipping step.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }

    //Creates separate project folder in workspace
    private int generate_project(String user, List<String> tasks) {
        int code=0;
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        String authString = "Basic " + encoder.encode(userPass.getBytes());
        try {
            URL url;
            HttpURLConnection connection;
            do {
                url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/Project_Management/job/Generate_Project/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);

                code=connection.getResponseCode();
                if (code!=200){
                    Thread.sleep(2000);
                }
            } while (code!=200);


            for (int i = 0; i < tasks.size(); i++) {
                url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);
                code = connection.getResponseCode();
                if (code == 404) {
                    String params = "PROJECT_NAME=" + tasks.get(i) + "&ADMIN_USERS=" + user;
                    url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/Project_Management/job/Generate_Project/buildWithParameters");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(params.getBytes());
                    os.flush();
                    os.close();
                    code = connection.getResponseCode();
                }else{
                    System.out.println("Task "+tasks.get(i)+" project exists, skipping step.");
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return code;
    }

    //Fires Java201InitializeWorkspace, Loads cartridge if it wasn't loaded before
    private int load_cartridge(String user, List<String> tasks){
        int code=0;
        //This supposed to be a link to a cartridge which will be used
        //NOTE: This has to be a name from list of defined cartridges from ADOP
        String cartridge="ssh://jenkins@gerrit:29418/cartridges/my-new-cartridge.git";
//        String cartridge = "ssh://jenkins@gerrit:29418/cartridges/java_201_Jurijs_Petrovs.git";
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        String authString = "Basic " + encoder.encode(userPass.getBytes());
        for (int i=0;i<tasks.size();i++) {
            try {
                URL url;
                HttpURLConnection connection;
                do {
                    url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Cartridge_Management/job/Load_Cartridge/");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);

                    code=connection.getResponseCode();
                    if (code!=200){
                        Thread.sleep(2000);
                    }
                } while (code!=200);

                url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Java201InitializeWorkspace");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);
                code = connection.getResponseCode();

                if (code==404) {
                    String params = "CARTRIDGE_CLONE_URL=" + cartridge;
                    url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Cartridge_Management/job/Load_Cartridge/buildWithParameters");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(params.getBytes());
                    os.flush();
                    os.close();
                    code=connection.getResponseCode();
                }else{
                    System.out.println("Cartridge for task "+tasks.get(i)+" is already LOADED, skipping step.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return code;
    }

    //Fires java201InitializeWorkspace
    private int initialize_workspace(String user, List<String> tasks){
        int code=0;
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        String authString = "Basic " + encoder.encode(userPass.getBytes());
        for (int i=0; i<tasks.size();i++) {
            try {
                URL url;
                HttpURLConnection connection;
                JSONObject obj;
                do {
                    url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Java201InitializeWorkspace/");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);
                    //Debugging
                    //System.out.println("Code J201_INIT:"+ code);
                    code=connection.getResponseCode();
                    if (code!=200){
                        //System.out.println("Waiting for Jenkins to finish building...");
                        Thread.sleep(2000);
                    }
                } while (code!=200);

                url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Java201InitializeWorkspace/lastBuild/api/json");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);
                code = connection.getResponseCode();

                if (code==404) {
                    String params = "gitRepositoryUrl=https://newsource.accenture.com/scm/java201/" + tasks.get(i) + ".git&traineeName=" + user;
                    url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Java201InitializeWorkspace/buildWithParameters");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    os.write(params.getBytes());
                    os.flush();
                    os.close();
                    code = connection.getResponseCode();
                }else{
                    System.out.println("Cartridge for task "+ tasks.get(i)+" is already INITIALIZED, skipping step.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return code;
    }

    //Retrieves and sends back link repos
    private List<String> links_getter(String user, List<String> tasks){
        List <String> links = new ArrayList<String>();
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        String authString = "Basic " + encoder.encode(userPass.getBytes());
        for (int i = 0; i < tasks.size(); i++) {
            try {
                JSONObject obj;
                do {
                    URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Java201InitializeWorkspace/lastBuild/api/json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);

                    BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);

                    obj = new JSONObject(response.toString());
                    if (!(obj.getString("building").equals("false"))) {
                        System.out.println("Waiting for Jenkins to finish building...");
                        Thread.sleep(2000);
                    }
                } while (!(obj.getString("building").equals("false")));

                URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/Java201InitializeWorkspace/lastBuild/consoleText");
                authString = "Basic " + encoder.encode(userPass.getBytes());
                HttpURLConnection  connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);
                BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                String line;
                while ((line=in.readLine())!=null){
                    if (line.matches("http://(.*)")){
                        links.add(line);
                    }
                }

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return links;
    }

    //Evaluates progress of task
    private double [] progress(String user,List<String> tasks){
        int code=0;
        double curr_progress;
        double [] progress= new double[tasks.size()];
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        for (int i=0; i<tasks.size();i++) {
            curr_progress=0;
            List <String> activities = activity_getter(user, tasks.get(i));
            for (int j = 0; j < activities.size(); j++) {
                try {
                    //GradleTestCheck
                    if (activities.get(j).matches("GradleTest_activity_(.*)")) {
                        URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/" + activities.get(j) + "/api/json");

                        String authString = "Basic " + encoder.encode(userPass.getBytes());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("Authorization", authString);

                        BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null)
                            response.append(inputLine);

                        JSONObject obj = new JSONObject(response.toString());
                        if (obj.getString("color").equals("blue")) {
                            curr_progress++;
                        }
                        in.close();
                    }

                    //SonarqubeCheck
                    if (activities.get(j).matches("SonarqubeTest_activity_(.*)")) {
                        URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + tasks.get(i) + "/job/" + activities.get(j) + "/api/json");

                        String authString = "Basic " + encoder.encode(userPass.getBytes());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("Authorization", authString);

                        BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null)
                            response.append(inputLine);

                        JSONObject obj = new JSONObject(response.toString());
                        if (obj.getString("color").equals("blue")) {
                            curr_progress++;
                        }
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (curr_progress != 0) {
                progress[i]=curr_progress/activities.size();
            } else {
                progress[i] = 0;
            }
        }
        return progress;
    }

    private Double averageTaskProgress(Double[] taskProgress){
        Double generalProgress=0.0;
        for (Double currentProgress : taskProgress){
            generalProgress+=currentProgress;
        }
        return generalProgress/taskProgress.length;
    }

    //Get names of tasks (currently from file)
    //Might be a good idea to avoid hardcoding and get task names other way
    private List<String> task_getter(){
        List <String> tasks = new ArrayList<String>();
        File task_list= new File("src/main/resources/task_list");
        try {
            BufferedReader in = new BufferedReader(new FileReader(task_list));
            String line;
            while ((line=in.readLine())!=null){
                tasks.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    //Get activity names
    private List<String> activity_getter(String user, String task){
        List<String> activities= new ArrayList<String>();
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
            try{
                URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + task + "/api/json");
                String authString = "Basic " + encoder.encode(userPass.getBytes());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", authString);

                BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);

                JSONObject obj = new JSONObject(response.toString());
                JSONArray jobs = obj.getJSONArray("jobs");

                for (int i = 0; i < jobs.length(); ++i) {
                    JSONObject job = jobs.getJSONObject(i);
                    if(((job.getString("name")).matches("GradleTest_activity_(.*)"))||(job.getString("name")).matches("SonarqubeTest_activity_(.*)")){
                        activities.add(job.getString("name"));
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return activities;
    }

    private Set<TaskError> getErrorsFromJenkinsBuild (String user, String task){
        Set<TaskError> errorSet = new HashSet<>();
        List <String> activities = activity_getter(user,task);
        BASE64Encoder encoder = new BASE64Encoder();
        String userPass="java201:kyjMtDpUp5YMg3bv";
        try{
            for (int i=0; i<activities.size();i++) {
                //GradleTestCheck
                if (activities.get(i).matches("GradleTest_activity_(.*)")) {
                    URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + task + "/job/" + activities.get(i) + "/api/json");

                    String authString = "Basic " + encoder.encode(userPass.getBytes());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);

                    try (BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null)
                            response.append(inputLine);

                        JSONObject obj = new JSONObject(response.toString());
                        if (obj.getString("color").equals("red")) {
                            URL urlToLastBuild = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + task + "/job/" + activities.get(i) + "/lastBuild/testReport/api/json");
                            authString = "Basic " + encoder.encode(userPass.getBytes());
                            HttpURLConnection urlConnection = (HttpURLConnection) urlToLastBuild.openConnection();
                            urlConnection.setRequestProperty("Authorization", authString);

                            try(BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {

                                StringBuilder responseFromLastBuild = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    responseFromLastBuild.append(line);
                                }

                                JSONObject stacktrace = new JSONObject(responseFromLastBuild.toString());

                                TaskError taskError = new TaskError();
                                taskError.setErrorName(activities.get(i) + " FAILED");
                                JSONObject failedActivityArray = stacktrace.getJSONArray("suites").getJSONObject(0).getJSONArray("cases").getJSONObject(0);
                                String errorStackTrace = failedActivityArray.getString("errorStackTrace");
                                taskError.setErrorDescription(errorStackTrace);
                                errorSet.add(taskError);
                            }
                        }
                    }
                }

                //SonarqubeCheck
                if (activities.get(i).matches("SonarqubeTest_activity_(.*)")) {
                    URL url = new URL("http://54.154.224.21/jenkins/job/" + user + "Java201/job/" + task + "/job/" + activities.get(i) + "/api/json");

                    String authString = "Basic " + encoder.encode(userPass.getBytes());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", authString);

                    BufferedReader in = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);

                    JSONObject obj = new JSONObject(response.toString());
                    if (obj.getString("color").equals("red")) {
                        errorSet.add(new TaskError(activities.get(i)+" FAILED"));
                    }
                    in.close();
                }

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return errorSet;
    }

      /**
     * Method for getting the trainee progress on Java201 tasks
     *
     * @param user - trainee name + "." + surname
     * @return Returned is map where key values are Java201 task gerrit links
     * and values are the percentage of activities done from that task from 0.0 - 1.0
     */
    public static List<Task> getJava201Tasks(String user) {
        Java201 java201 = new Java201();
        List<String> tasks = java201.task_getter();

        java201.generate_workspace(user);
        java201.generate_project(user, tasks);
        java201.load_cartridge(user, tasks);
        java201.initialize_workspace(user, tasks);
        List<String> links = java201.links_getter(user, tasks);
        double[] progress = java201.progress(user, tasks);

        // We should collect all the data and put them
        // in easy to use and passable beans.
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < links.size(); i++) {
            String taskLink = links.get(i);

            Task t = new Task();
            t.setTaskName(findTaskNameFromLink(tasks, taskLink));
            t.setTaskLink(taskLink);
            t.setTaskProgress(progress[i]);
            t.setTaskErrors(java201.getErrorsFromJenkinsBuild(user, tasks.get(i)));
            taskList.add(t);
        }



        return taskList;
    }

    // TODO - I think this shouldn't exist and task names should be returned
    // TODO - from the uri call, mby I'm wrong.
    private static String findTaskNameFromLink(List<String> taskNameList, String taskLink) {
        for (String name : taskNameList) {
            if (taskLink.contains(name)) {
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                return name.replaceAll("-", " ");
            }
        }
        return "No task name found";
    }

    static public void main (String args[]){
        String user= WebSecurityConfig.getAuthentication().getName();
        Java201 java201= new Java201();
        List <String> tasks= java201.task_getter();

        java201.generate_workspace(user);
        java201.generate_project(user, tasks);
        java201.load_cartridge(user, tasks);
        java201.initialize_workspace(user, tasks);
        List <String> links= java201.links_getter(user, tasks);
        for (int i=0; i<links.size();i++){
            System.out.println(links.get(i));
        }
        double [] progress= java201.progress(user, tasks);
        for (int j=0; j<progress.length;j++){
            System.out.println(progress[j]);
        }
    }
}

