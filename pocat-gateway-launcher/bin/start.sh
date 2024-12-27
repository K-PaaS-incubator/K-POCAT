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

GATEWAY_HOME="$( cd "$( dirname "$0" )"&& cd .. && pwd -P )"
LOG_HOME="${GATEWAY_HOME}/logs"
PID_FILE="${GATEWAY_HOME}/bin/gateway.pid"

if [ ! -d "${LOG_HOME}" ]; then
  mkdir "${LOG_HOME}"
fi

COMMAND=$1
CONSOLE_OUT="${GATEWAY_HOME}/logs/gateway.out"

if [ ! -f "${CONSOLE_OUT}" ]; then
  touch "${CONSOLE_OUT}"
fi

LAUNCHER_CLASS_NAME="io.pocat.gateway.GatewayLauncher"
EXECUTABLE_LAUNCHER_JAR="gateway-launcher.jar"
GATEWAY_OPTS="-Dio.pocat.gateway.home=\"${GATEWAY_HOME}\""
GATEWAY_OPTS="${GATEWAY_OPTS} -Dio.pocat.gateway.libs=\"${GATEWAY_HOME}/libs\""
GATEWAY_OPTS="${GATEWAY_OPTS} -Dio.pocat.gateway.config.home=\"${GATEWAY_HOME}/config\""
GATEWAY_OPTS="${GATEWAY_OPTS} -Dio.pocat.gateway.routes.home=\"${GATEWAY_HOME}/config/routes\""
GATEWAY_OPTS="${GATEWAY_OPTS} -Dio.pocat.gateway.files.home=\"${GATEWAY_HOME}/files\""

JAVA_OPTS="${JAVA_OPTS} ${GATEWAY_OPTS}"

if [ "${COMMAND}" = "background" ]; then
  RUN_COMMAND="nohup ${JAVA_EXECUTE} ${JAVA_OPTS} -jar \"${GATEWAY_HOME}/bin/${EXECUTABLE_LAUNCHER_JAR}\" ${LAUNCHER_CLASS_NAME} >> \"${CONSOLE_OUT}\" 2>&1 &"
elif [ "${COMMAND}"  = "foreground" ]; then
  RUN_COMMAND="${JAVA_EXECUTE} ${JAVA_OPTS} -jar \"${GATEWAY_HOME}/bin/${EXECUTABLE_LAUNCHER_JAR}\" ${LAUNCHER_CLASS_NAME} >> \"${CONSOLE_OUT}\" 2>&1"
else
  echo "INVALID COMMAND [${COMMAND}]."
  exit 1;
fi

echo "Gateway start with options"
echo "Java : ${JAVA_EXECUTE}"
echo "Gateway Home : ${GATEWAY_HOME}"
echo "${RUN_COMMAND}"
eval "${RUN_COMMAND}"
RESULT="$?"
if [ "${RESULT}" = 0 ]; then
  PID="$!"
  echo ${PID} > "${PID_FILE}"
fi