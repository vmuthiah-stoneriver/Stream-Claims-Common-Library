########### Batch Configuration - Environment ###########
export BATCH_RUN_PATH=/software/stream/support/StreamBatch
export JAVA_HOME=/opt/was08/AppServer/java/jre/bin
############## Environment Setup ######################

export BATCH_INVOKER=com.client.iip.integration.BatchJobLauncher
export CP=$BATCH_RUN_PATH/resources:$BATCH_RUN_PATH/security:$BATCH_RUN_PATH/StreamBatch.jar

## Grab all command line arguments at once
export CMD_LINE_ARGS=$*

for i in $(ls $BATCH_RUN_PATH/lib/*.jar)
do
  CP=$CP:$i
done

echo $CP

### Launch the Batch
$JAVA_HOME/java -Dlog4j.configuration=%BATCH_RUN_PATH%\log4j.xml -cp $CP $BATCH_INVOKER $CMD_LINE_ARGS
