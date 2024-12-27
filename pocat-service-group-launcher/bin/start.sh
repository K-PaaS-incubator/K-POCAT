#!/bin/sh

JAVA_EXECUTE="$(which java)"

if [ -z "${JAVA_EXECUTE}" ]; then
  if [ -z "${JAVA_HOME}" ]; then
    if [ -z "${JRE_HOME}" ]; then
      echo "JAVA_HOME or JRE_HOME not found."
      exit 1
    else
      JAVA_EXECUTE="${JRE_HOME}/bin/java"
    fi
  else
    JAVA_EXECUTE="${JAVA_HOME}/bin/java"
  fi
fi

SERVICE_CONTAINER_HOME="$( cd "$( dirname "$0" )"&& cd .. && pwd -P )"
LOG_HOME="${SERVICE_CONTAINER_HOME}/logs"
PID_FILE="${SERVICE_CONTAINER_HOME}/bin/servicegroup.pid"

if [ ! -d "${LOG_HOME}" ]; then
  mkdir "${LOG_HOME}"
fi

COMMAND=$1
CONSOLE_OUT="${SERVICE_CONTAINER_HOME}/logs/service-container.out"

if [ ! -f "${CONSOLE_OUT}" ]; then
  touch "${CONSOLE_OUT}"
fi

LAUNCHER_CLASS_NAME="io.pocat.platform.launcher.ServiceContainerLauncher"
EXECUTABLE_LAUNCHER_JAR="container-launcher.jar"
SERVICE_CONTAINER_OPTS=""
SERVICE_CONTAINER_OPTS="${SERVICE_CONTAINER_OPTS} -Dio.pocat.container.home=\"${SERVICE_CONTAINER_HOME}\""
SERVICE_CONTAINER_OPTS="${SERVICE_CONTAINER_OPTS} -Dio.pocat.container.services.home=\"${SERVICE_CONTAINER_HOME}/services\""
SERVICE_CONTAINER_OPTS="${SERVICE_CONTAINER_OPTS} -Dio.pocat.container.config.home=\"${SERVICE_CONTAINER_HOME}/config\""
SERVICE_CONTAINER_OPTS="${SERVICE_CONTAINER_OPTS} -Dio.pocat.container.libs=\"${SERVICE_CONTAINER_HOME}/libs\""

JAVA_OPTS="${JAVA_OPTS} ${SERVICE_CONTAINER_OPTS}"

if [ "${COMMAND}" = "background" ]; then
  RUN_COMMAND="nohup ${JAVA_EXECUTE} ${JAVA_OPTS} -jar \"${SERVICE_CONTAINER_HOME}/bin/${EXECUTABLE_LAUNCHER_JAR}\" ${LAUNCHER_CLASS_NAME} >> \"${CONSOLE_OUT}\" 2>&1 &"
elif [ "${COMMAND}"  = "foreground" ]; then
  RUN_COMMAND="${JAVA_EXECUTE} ${JAVA_OPTS} -jar \"${SERVICE_CONTAINER_HOME}/bin/${EXECUTABLE_LAUNCHER_JAR}\" ${LAUNCHER_CLASS_NAME} >> \"${CONSOLE_OUT}\" 2>&1"
else
  echo "INVALID COMMAND [${COMMAND}]."
  exit 1;
fi

echo "Service container start with options"
echo "Java : ${JAVA_EXECUTE}"
echo "Home : ${SERVICE_CONTAINER_HOME}"
echo "Config Directory : ${SERVICE_CONTAINER_HOME}/config"
echo "Service home directory: ${SERVICE_CONTAINER_HOME}/services"
echo "${RUN_COMMAND}"
eval "${RUN_COMMAND}"
RESULT="$?"
if [ "${RESULT}" = 0 ]; then
  PID="$!"
  echo ${PID} > "${PID_FILE}"
fi