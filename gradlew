#!/bin/sh

# Gradle start up script
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Resolve APP_HOME
PRG="$0"
while [ -h "$PRG" ]; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")/"$link"
    fi
done
APP_HOME=$(cd "$(dirname "$PRG")/" >/dev/null && pwd)

MAX_FD=maximum
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

warn () { echo "$*"; } >&2
die () { echo; echo "$*"; echo; exit 1; } >&2

if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory"
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1 ; then
        die "ERROR: java not found"
    fi
fi

exec "$JAVACMD" \
    $DEFAULT_JVM_OPTS \
    $JAVA_OPTS \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"