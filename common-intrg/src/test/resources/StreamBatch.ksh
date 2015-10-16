########### Batch Configuration - Environment ###########
export BATCH_RUN_PATH=/usr/claimstream/Batch
export JAVA_HOME=/opt/IBM/WebSphere/AppServer/java/bin/java
REM ############## Environment Setup ######################

export BATCH_INVOKER=com.client.iip.integration.BatchJobLauncher
export CP=$BATCH_RUN_PATH\resources;$BATCH_RUN_PATH\security;$BATCH_RUN_PATH\StreamBatch.jar

## Grab all command line arguments at once
export CMD_LINE_ARGS=$*

export CP =
for i in 'ls ./lib/*.jar'
do
  CP=$CP;$i
done

### Launch the Batch
$JAVA_HOME -cp $CP $BATCH_INVOKER $CMD_LINE_ARGS
