#!/bin/bash

# Method to
# 1. clone a project in the repository and switch to a given branch.
# 2. run all tests in all activities in a project
# Parameter 1:  branch name.
# Parameter 2:  project name.
# Parameter 3:  list of activities separated by space.
git_clone_switch_execute () {
  DEV_BRANCH=$1;
  PROJECT=$2;
  ACTIVITY_LIST=$3;
  INNER_SRC_URL_PREFIX="https://sooraj.a.nair@innersource.accenture.com/scm/java201/";
  GIT_URL="$INNER_SRC_URL_PREFIX$PROJECT";

  # Clone if $PROJECT doesn't exist.
  if [ ! -d "$PROJECT" ]; then
    echo "$PROJECT directory does not exist; proceeding with clone.."
    git clone $GIT_URL
    sleep 20
  fi

  cd $PROJECT
  git fetch -q origin
  sleep 10

  # Delete local branch before checkout.
  echo "Deleting local branch $DEV_BRANCH."
  git checkout master
  sleep 10
  git branch -d $DEV_BRANCH
  sleep 10

  # Check if the remote repository exists before continuing.
  git ls-remote --exit-code origin $DEV_BRANCH
  if [[ $? != 0 ]]; then
    echo "WARNING: $DEV_BRANCH branch does not exist in $PROJECT repo; Hence skipping $PROJECT activities."
	cd ..
	return;
  fi
  git checkout $DEV_BRANCH
  sleep 10

  # Iterate and execute the tests in the activities.
  echo "Starting to execute activities in $PROJECT"
  for ACTIVITY in $ACTIVITY_LIST
  do
    echo "Running tests for $ACTIVITY"
	cd $ACTIVITY
	
    if ! [[ -f ./gradlew.bat ]]; then
	  # Generate gradle wrapper
      gradle init	  
	  gradle wrapper
	  sleep 15
    fi
	./gradlew.bat clean test
	cd ..
  done
  echo "Finished executing activities in $PROJECT"
  cd ..
}


#input parameter validation
DEV_BRANCH="";
if [ $# -gt 0 ]; then
  DEV_BRANCH=$1;
else
  echo "dev branch name in git needed as input parameter"
  return;
fi

git_clone_switch_execute $DEV_BRANCH "m1-core1-xml-config" "activity_01 activity_02 activity_03 activity_04 activity_05";

git_clone_switch_execute $DEV_BRANCH "m1-core2-java-config" "activity_01 activity_02 activity_03";

git_clone_switch_execute $DEV_BRANCH "m1-core3-xml-java-config-mix" "activity_01";

git_clone_switch_execute $DEV_BRANCH "m2-rest" "activity_01 activity_02";

git_clone_switch_execute $DEV_BRANCH "m3-mvc" "activity_01 activity_02 activity_11";

git_clone_switch_execute $DEV_BRANCH "m4-security" "activity_01 activity_02";

git_clone_switch_execute $DEV_BRANCH "m5-data" "activity_01 activity_02 activity_03";

git_clone_switch_execute $DEV_BRANCH "m6-aop" "activity_01 activity_02 activity_03";

git_clone_switch_execute $DEV_BRANCH "m7-batch" "activity_01 activity_02";

git_clone_switch_execute $DEV_BRANCH "m8-boot" "activity_01";
