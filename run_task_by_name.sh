#!/usr/bin/env bash

TASK_NAME=$1
MAIN_CLASS="ru.mxjkeee"
case "$TASK_NAME" in
	"task1")
		MAIN_CLASS="$MAIN_CLASS.NumbersFormatter"
		;;
	"task2")
	    MAIN_CLASS="$MAIN_CLASS.ListsVerifier"
		;;

	"task3")
		MAIN_CLASS="$MAIN_CLASS.FileWriter"
		;;
	"task4")
		MAIN_CLASS="$MAIN_CLASS.StringsFormatter"
		;;
	"task5")
		exit 0
		;;
	"task6")
		exit 0
		;;
	"task7")
		MAIN_CLASS="$MAIN_CLASS.pool.ObjectPoolUsage"
		;;
	*)
		echo "Unknown task name: $TASK_NAME"
		exit 1
		;;
esac

echo "mvn exec:java -Dexec.mainClass=\"$MAIN_CLASS\""
mvn clean test
mvn exec:java -Dexec.mainClass=$MAIN_CLASS