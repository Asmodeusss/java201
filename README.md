
# Java201 Catridge
### What is cartridge?
Cartrige - is a configuration plugin for the ADOP system. It performs the configuration of the system during the process of loading into the ADOP system.  
The ADOP system is a automated system for CI (Continuous Integration), which contains of different interconnected open source programs. 
Cartridge configurate the system for your particular needs.

### What is the purpose of this cartridge?

The main purpose of this cartridge is to provide automatization for the course [Java201](https://newsource.accenture.com/projects/JAVA201).

How to automatically verify the course:

**1.** Trainee sends an external request by pressing button on a webpage.

**2.** User name\unique ID is passed to the spring servlet on button press.

**3.** Spring servlet sends commands to Jenkins API:
```sh
- Generate_workspace(String user) //Checks if trainee\workspace exist. Creates workspace\registers trainee if needed
- Generate_project (String user, List <String> tasks) //Creates separate project-folders for each task in course
- Load_cartridge (String user, List <String> tasks) //Loads the cartridge for each of the tasks
- Initialize_workspace (String user, List <String> tasks) //Initializes cartridge-script for each of the tasks
- Links_getter (String user, List <String> tasks) //Retrieves links to repositories from Jenkins and sends back as List <String>
- Progress (String user, List <String> tasks) //Evaluates current progress of each task and sends back as double []
- Task_getter () //Currently list of tasks is stored locally in src/task_list
- Activity_getter (String user, List <String> tasks) //Retrieves names of activities
```
**4.** Trainee recieves list of repositories and progress for corresponding tasks.
## Spring servlet
The project is based from [this](http://www.mkyong.com/spring-mvc/gradle-spring-4-mvc-hello-world-example-annotation/
) tutorial.
Additional mapping in the existing WelcomeController "/button" which is called when pressing a link
which looks like a button, in the link we pass the needed trainee identifier, in our case "name.surname" and
then encrypt and decrypt it on receiving the button call. In method buttonPress in WelcomeController we just
call a task generation method from Java201 class and display the received result.

To start the web server locally, you must go in console to this projects folder where you saved it on your machine
and type "gradle jettyRun" and the service should start up.

## Database
Application works with MySQL Database[documentation](https://dev.mysql.com/doc/). Uses Java Mysql connector 5.1.41 version.
To set up for further work, firstly you need to download and install [MySQL Server](https://dev.mysql.com/downloads/mysql/).
Then in you MySQL Workbench or in your IDE database plugin you need to run **database_creation.sql**
All database configuration properties you can find in **application.properties** file. You can change it to your project specification.


## Jenkins jobs

**1.** "Java201InitializeWorkspace" - main job if the cartridge, generates all other jobs, creates gerrit repository. Requires two parameters as input:
- gerritRepositoryName (for creation of a unique repository in gerrit)
- traineeName (to keep track of progress and store trainees' projects separately)

**2.** "Java201BuildAndTest_activity_%Number%" - builds and tests activity and analyzes code quality with Sonarqube. Compares values from Sonarqube - Timemachine page.

**3.** "Clean-up_activity_%Number%" - removes all files related to activity from repository.

**4.** "Remove_gerrit_project" - removes whole gerrit project with delete-project plugin

## DSL code

```sh
dsl(String scriptText)
```

DSL allows executing groovy script inside of a job. In this cartridge dsl() function used to define the generated jobs that do building, testing and static analysis. 
Including a DSL block allows unleashing the power of Groovy inside the Jenkins environment. This opportunity used to parse folder, plugin and project names, evaluate Sonarqube code complexity and more.

## Checking if delete-project plugin is installed.

Additional plugin is needed in order to remove projects from gerrit repository. Existance of this plugin is checked by parsing list of installed plugins: 

```sh
if(
plugin_list.find {
if (it=~ /deleteproject/){
		return true
}else{
		return false
}
}){
		println "Delete-project plugin is already installed, skipping step."
}else{
		println "Installing delete-project plugin"
		"ssh -p 29418 jenkins@gerrit gerrit plugin install -n delete-project.jar https://gerrit-ci.gerritforge.com/job/plugin-delete-project-stable-2.9/lastSuccessfulBuild/artifact/buck-out/gen/plugins/delete-project/delete-project.jar".execute().waitFor()
		println "Plugin installed"
}
```

## Checking if project exists

```sh
if (project_list.find{
if(it ==~ ".*${traineeName}/${gerritRepositoryName}"){
		return true
}else{
		return false
}
}){
		println "Repository with this name already exists! Recreating repository."
		"ssh -p 29418 jenkins@gerrit deleteproject delete --yes-really-delete ${traineeName}/${gerritRepositoryName}".execute().waitFor()
}
```

## Copy from newsource git repository to gerrit

Repository from given linked is cloned and pushed to the newly created gerrit repository.

If repository with given name already exist it will be overwritten.
```sh
ssh -n -o StrictHostKeyChecking=no -p 29418 jenkins@gerrit gerrit create-project ${traineeName}/${gerritRepositoryName}
git clone ssh://jenkins@gerrit:29418/${traineeName}/${gerritRepositoryName}
cd ${gerritRepositoryName}
git init
git remote add source ${gitRepositoryUrl}
git fetch source master
git config user.email \"${traineeName}@accenture.com\"
git config user.name \"${traineeName}\"
git push origin +refs/remotes/source/*:refs/heads/* 
```

## Testing
---
Testing part consist of 4 steps:
 - Build workspace for testing
 - Gradle test
 - Sonarqube analysis
 - Timemachine code complexity comparison

## Gradle test
```sh
    gradle {
      useWrapper true
      makeExecutable true
      rootBuildScriptDir 'activity_01' (name of activity that we need to test)
      tasks 'clean test'
    }
```
## SonarQube analysis
```sh
environmentVariables {
  ...
  env(\'CURR_ACTIVITY\', currActivity)
}
...
configure { myProject ->
  myProject / builders << \'hudson.plugins.sonar.SonarRunnerBuilder\'(plugin:\'sonar@2.2.1\') 
{
properties("""sonar.projectKey=${PROJECT_NAME_KEY}-\\$CURR_ACTIVITY
	sonar.projectName=${PROJECT_NAME}-\\$CURR_ACTIVITY
	sonar.projectVersion=1.0.${B}
	sonar.sources=\\$CURR_ACTIVITY
	sonar.language=java
	sonar.qualitygate=Java201""")
javaOpts()
jdk(\'(Inherit From Job)\')
task()}
}
```
### Dsl scan Timemachine page for code complexity
```sh
def authString = "${USER_PASS}".getBytes().encodeBase64().toString();

URLConnection sonar_url = new URL("http://54.154.224.21/sonar/dashboard/index?id=${PROJECT_NAME_KEY}-${CURR_ACTIVITY}&did=3").openConnection();
sonar_url.setRequestProperty("Authorization", "Basic ${authString}");
def sonarqube_page = new BufferedReader(new InputStreamReader(sonar_url.getInputStream()));
def line
def result=[] //Values of complexity rate are stored here

while ((line = sonarqube_page.readLine())!=null) {
    if (line ==~/Complexity/){
        while (!(line==~"</tr>")){
            line = sonarqube_page.readLine()
                if (line==~/<td width="1%" nowrap.*/){
                    line=line.minus(/<td width="1%" nowrap="nowrap" class="right"><span   >/)
                    line=line.minus("</span></td>")
                    result << line
                }     
        }
    }
}

if (result.size()==2){
		if (result[0]>result[1]){
			throw new javaposse.jobdsl.dsl.DslException("Complexity rate is lower than before!")
		}
}
```

To put it simple, SonarQube makes static analysis of the code, and evaluates its quality.  
Environment variable CURR_ACTIVITY used to cofigure SonarQube, i.e., to guide it to the right activity, that shall be analysed (when the job for testing is executed). 

## Known issues
- It is possible to fail triggering job Java201InitializeWorkspace by deleting one of the task folders and then attempting to run whole cycle again. This is caused by Json slurper retrieval of empty input, not sure why it happens. (Seems to be fixed, but probably can occur again)
- Currently there is no action to remove credentials of trainees from LDAP database. 
- Sonarqube code evaluation quality gate is hardcoded and NOT adjusted to various tasks\activities.
- Each "BuldAndTest" job clones **whole** repository from gerrit. There is no real solution to this since jobs may be built on different slaves in jenkins and its not possible to refer to specific path. Right now this problem is avoided by simply cleaning workspace upon build finish.
- "Generate_workspace" job creates new accounts with password equal to user name.

## Future plans
1. Update for clean-up jobs
2. Fix known issues.
3. Send email to trainee notifying them about their progress.
4. Web integration
5. Add overall progress output for Admin-user

## Used plugins
1. Job-dsl plugin for jenkins: https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin
2. Delete-project for gerrit.
