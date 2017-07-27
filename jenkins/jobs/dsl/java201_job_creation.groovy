import hudson.model.*

// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"

// Variables
def projectNameKey = projectFolderName.toLowerCase().replace("/", "-")

// Jobs
def initializeWorkspace = freeStyleJob(projectFolderName + "/Java201InitializeWorkspace")

initializeWorkspace.with {
	description("This job copies git repository into Gerrit repository and creates jobs that will test activities and that will trigger when user pushes any changes.")
        
	parameters {
		stringParam("gitRepositoryUrl", "https://newsource.accenture.com/scm/java201/m1-core1-xml-config.git", "Enter the repository URL that will be cloned to gerrit. !The name shall not contain spaces!")
		stringParam("traineeName", "Iaroslav.Morozov")
	}

	environmentVariables {
		env('WORKSPACE_NAME', workspaceFolderName)
		env('PROJECT_NAME', projectFolderName)
		env('PROJECT_NAME_KEY', projectNameKey)
	}

        wrappers{
			preBuildCleanup()
            credentialsBinding {
                usernamePassword('USER_PASS', 'a4f1b3e8-84c1-4abc-85dd-32e608839dad')

			}
        }

	steps {
		//Create lists with all plugins and projects
		//JENKINS SHELL AND GROOVY SHELL WORK FROM DIFFERENT DIRECTORIES
		shell("""
			ssh -n -o StrictHostKeyChecking=no -p 29418 jenkins@gerrit gerrit plugin ls > \${WORKSPACE}/plugin_list.txt
			ssh -n -o StrictHostKeyChecking=no -p 29418 jenkins@gerrit gerrit ls-projects >  \${WORKSPACE}/project_list.txt
			""")

		dsl ('''

			//For parsing SonarQube quality gate data
			import groovy.json.JsonSlurper
			//Needed for jenkins plugin list scanning
			import jenkins.model.Jenkins

			def workspacePath="${WORKSPACE}"
			File plugin_list = new File("${workspacePath}/plugin_list.txt")
			File project_list = new File("${workspacePath}/project_list.txt")
			def gerritRepositoryName= "${gitRepositoryUrl}"
			gerritRepositoryName=gerritRepositoryName.minus("https://newsource.accenture.com/scm/java201/")
			gerritRepositoryName=gerritRepositoryName.minus(".git")
	

			//<-------------------------------------->
			//Step 0.
			//Checking if "delete project" plugin is already installed
			if(plugin_list.find {it=~ /deleteproject/}){
					println "Delete-project plugin is already installed, skipping step."
			}else{
					println "Installing delete-project plugin"
					"ssh -p 29418 jenkins@gerrit gerrit plugin install -n delete-project.jar https://gerrit-ci.gerritforge.com/job/plugin-delete-project-stable-2.9/lastSuccessfulBuild/artifact/buck-out/gen/plugins/delete-project/delete-project.jar".execute().waitFor()
			}
			//<-------------------------------------->

			//Step 1.
			//Check if project with such name already exists
			//If so - push
			if (project_list.find{
			if(it ==~ ".*${traineeName}Java201/${gerritRepositoryName}"){
					return true
			}else{
					return false
			}
			}){
				println "Repository with this name already exists!"
				"git clone ssh://jenkins@gerrit:29418/${traineeName}Java201/${gerritRepositoryName}".execute(null, new File("${workspacePath}/")).waitFor()
			}else{
				println "Creating repository"
				"ssh -n -o StrictHostKeyChecking=no -p 29418 jenkins@gerrit gerrit create-project --parent ${traineeName}Java201/${gerritRepositoryName}/permissions ${traineeName}Java201/${gerritRepositoryName}".execute().waitFor()
				
				def folder = new File("${workspacePath}/folder")
				if(!folder.exists()) {
				folder.mkdirs()
				}
				
				"git clone ssh://jenkins@gerrit:29418/java201repo/${gerritRepositoryName}".execute(null, new File("${workspacePath}/")).waitFor()
				"git remote set-url origin ssh://jenkins@gerrit:29418/${traineeName}Java201/${gerritRepositoryName}".execute(null, new File("${workspacePath}/${gerritRepositoryName}")).waitFor()
				"git config user.email \\"${traineeName}@accenture.com\\"".execute(null, new File("${workspacePath}/${gerritRepositoryName}")).waitFor()
				"git config user.name \\"${traineeName}\\"".execute(null, new File("${workspacePath}/${gerritRepositoryName}")).waitFor()
				"git push origin master".execute(null, new File("${workspacePath}/${gerritRepositoryName}")).waitFor()
			}
			//Gerrit rep link, will be used in web integration
			println "http://${traineeName}@54.154.224.21/gerrit/${traineeName}Java201/${gerritRepositoryName}"

			//<-------------------------------------->
			//Step 1.5
			//Create Java201 profile for SonarQube
			def sout = new StringBuilder()
			def str
			def slurper
			def list
			//There seems to be a problem calling this API from 2 or more jobs at the same time, so I added wait time.
			while (sout==null || sout.toString().equals("")){
				def proc_url="http://54.154.224.21/sonar/api/qualitygates/list"
				def process="curl -u ${USER_PASS} -X GET ${proc_url}".execute()
				process.consumeProcessOutput(sout, null)
				process.waitFor()
				str=sout.toString()
              	if (str.equals("")){
                	sleep (1000)
                }else{
                	slurper = new JsonSlurper().parseText(str)
					list = slurper.qualitygates
                }
			}

			if(!(list.find{
			if(it.name =="Java201"){
				return true
			}
			})){
			//Java201 profile does not exist
			println "Creating Java201 profile"
			proc_url="http://54.154.224.21/sonar/api/qualitygates/create?name=Java201"
			"curl -u ${USER_PASS} -X POST ${proc_url}".execute().waitFor()

			sout = new StringBuilder()
			proc_url="http://54.154.224.21/sonar/api/qualitygates/list"
			process="curl -u ${USER_PASS} -X GET ${proc_url}".execute()
			process.consumeProcessOutput(sout, null)
			process.waitFor()

			sout = new StringBuilder()
			proc_url= "http://54.154.224.21/sonar/api/qualitygates/show?name=Java201"
			//Get data of Java201 profile
			process="curl -u ${USER_PASS} -X GET ${proc_url}".execute()
			process.consumeProcessOutput(sout, null)
			process.waitFor()
			str= sout.toString()
			//Parse output and extract ID
			slurper = new JsonSlurper().parseText(str)
			def id = slurper.id

			//Set quality gate conditions for Java201 profiles
			//Note that these values have to be adjusted according to Java201 requirements (I don't know them)
			println "Adding conditions to Java201 profile"
			//Major issues condition >15
			proc_url= "http://54.154.224.21/sonar/api/qualitygates/create_condition?error=5&gateId=${id}&metric=major_violations&op=GT"
			"curl -u ${USER_PASS} -X POST ${proc_url}".execute().waitFor()

			//Minor issues condition >50
			proc_url= "http://54.154.224.21/sonar/api/qualitygates/create_condition?error=20&gateId=${id}&metric=minor_violations&op=GT"
			"curl -u ${USER_PASS} -X POST ${proc_url}".execute().waitFor()
			}

			//<-------------------------------------->
			// Step 2.
			// Create jobs for building, testing and executing statistical analysis:

			// Folders:
			def workspaceFolderName = "${WORKSPACE_NAME}"
			def projectFolderName = "${PROJECT_NAME}"

			// Variables
			def projectNameKey = projectFolderName.toLowerCase().replace("/", "-")
			// **The git repo variables will be changed to the users' git repositories manually in the Jenkins jobs**
			def referenceAppGitUrl = "http://java201@54.154.224.21/gerrit"

			println "Creating Build and Test, Clean-up jobs"
			def proc ='sh -c ls -d */'.execute(null, new File("${workspacePath}/${gerritRepositoryName}"))
			proc.in.eachLine { line ->
				if (line ==~ "activity_.*"){

					//<-------------------------------------->
					//Gradle tests
					job("${projectFolderName}/GradleTest_${line}") {
						description("This job performs Gradle tests for ${line}.")
						scm {
							git {
								remote {
									url("${referenceAppGitUrl}/${traineeName}Java201/${gerritRepositoryName}")
									credentials('a4f1b3e8-84c1-4abc-85dd-32e608839dad')
								}
								branch('*/master')
							}
						}

						// Environment variable 'CURR_ACTIVITY' is needed so that SonarQube analized the right activity.
						environmentVariables {
							env('WORKSPACE_NAME', workspaceFolderName)
							env('PROJECT_NAME', projectFolderName)
							env('PROJECT_NAME_KEY', projectNameKey)
							env('CURR_ACTIVITY', line)
						}

						wrappers {
							credentialsBinding {
								usernamePassword('USER_PASS', 'a4f1b3e8-84c1-4abc-85dd-32e608839dad')
							}
						}

						label('java8')

						triggers{
							gerrit {
								events{ refUpdated() }
								project("${traineeName}Java201/${gerritRepositoryName}", 'plain:master')
								configure { node ->
									node / serverName('ADOP Gerrit')
								}
							}
						}

						steps{
							gradle {
								useWrapper true
								makeExecutable true
								rootBuildScriptDir "${line}"
								tasks 'clean test\'
							}
						}

						publishers {
							wsCleanup()
						}
					}

					//<-------------------------------------->
					//Sonarqube tests
					job("${projectFolderName}/SonarqubeTest_${line}") {
						description("This job performs Sonarqube tests for ${line}.")
						scm {
							git {
								remote {
									url("${referenceAppGitUrl}/${traineeName}Java201/${gerritRepositoryName}")
									credentials('a4f1b3e8-84c1-4abc-85dd-32e608839dad')
								}
								branch('*/master')
							}
						}

						// Environment variable 'CURR_ACTIVITY' is needed so that SonarQube analized the right activity.
						environmentVariables {
							env('WORKSPACE_NAME', workspaceFolderName)
							env('PROJECT_NAME', projectFolderName)
							env('PROJECT_NAME_KEY', projectNameKey)
							env('CURR_ACTIVITY', line)
						}

						wrappers {
							credentialsBinding {
								usernamePassword('USER_PASS', 'a4f1b3e8-84c1-4abc-85dd-32e608839dad')
							}
						}

						label('java8')

						triggers{
							gerrit {
								events{ refUpdated() }
								project("${traineeName}Java201/${gerritRepositoryName}", 'plain:master')
								configure { node ->
									node / serverName('ADOP Gerrit')
								}
							}
						}

						steps{
							//Evaluation of Sonarqube results WIP still needs adjustments
							dsl('\''
								def authString = "${USER_PASS}".getBytes().encodeBase64().toString();

								URLConnection sonar_url = new URL("http://54.154.224.21/sonar/dashboard/index?id=${PROJECT_NAME_KEY}-${CURR_ACTIVITY}&did=3").openConnection();
								sonar_url.setRequestProperty("Authorization", "Basic ${authString}");
								def sonarqube_page = new BufferedReader(new InputStreamReader(sonar_url.getInputStream()));
								def line
								def result=[] //Values of complexity rate are stored here

								//Keep in mind - it is possible to completely avoid this way of complexity value analysis
								//and just use built in condition for quaility gate profile, but it's not possible to compare it with
								//initial commit.
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
														//Fail build
											throw new javaposse.jobdsl.dsl.DslException("Complexity rate is lower than before!")
										}
								}
							'\'')
						}

						configure { myProject ->
							myProject / builders << 'hudson.plugins.sonar.SonarRunnerBuilder'(plugin: 'sonar@2.2.1') {
								properties("""
									sonar.projectKey=${PROJECT_NAME_KEY}-\\$CURR_ACTIVITY
									sonar.projectName=${PROJECT_NAME}-\\$CURR_ACTIVITY
									sonar.projectVersion=1.0.${B}
									sonar.sources=\\$CURR_ACTIVITY
									sonar.language=java
									sonar.qualitygate=Java201
								""")
								javaOpts()
								jdk('(Inherit From Job)')
								task()
							}
						}

						publishers {
							wsCleanup()
						}
					}
				}
			}

			job ("$projectFolderName/Remove_gerrit_project"){
					description("This job removes your project from gerrit")
					environmentVariables{
						env('GERRIT_REP_NAME', gerritRepositoryName)
									env('TRAINEE_NAME', traineeName)
					}
					steps{
						dsl('\''
						"ssh -p 29418 jenkins@gerrit deleteproject delete --yes-really-delete ${TRAINEE_NAME}Java201/${GERRIT_REP_NAME}".execute().waitFor()
						"ssh -p 29418 jenkins@gerrit deleteproject delete --yes-really-delete ${TRAINEE_NAME}Java201/${GERRIT_REP_NAME}/permissions".execute().waitFor()
						"ssh -p 29418 jenkins@gerrit deleteproject delete --yes-really-delete ${TRAINEE_NAME}Java201/${GERRIT_REP_NAME}/permissions-with-review".execute().waitFor()
						'\'')
					}
			}
    	''')
		//Cleaning lists upon completion
		shell("""
			rm \${WORKSPACE}/plugin_list.txt
			rm \${WORKSPACE}/project_list.txt
		""")
	}

}



