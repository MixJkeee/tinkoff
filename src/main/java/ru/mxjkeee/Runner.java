package ru.mxjkeee;

import ru.mxjkeee.pool.ObjectPoolUsage;

import static io.vavr.API.*;
import static java.lang.Boolean.FALSE;
import static ru.mxjkeee.Constants.CONSOLE_DELIMITER;

public class Runner {
    private static final String TASK_1 = "task1";
    private static final String TASK_2 = "task2";
    private static final String TASK_3 = "task3";
    private static final String TASK_4 = "task4";
    private static final String TASK_5 = "task5";
    private static final String TASK_6 = "task6";
    private static final String TASK_7 = "task7";

    private static int tasksCounter;

    public static void main(String[] args) {
        for (String arg : args) {
            boolean result = Match(arg).of(
                    Case($(TASK_1::equalsIgnoreCase), NumbersFormatter::runTask),
                    Case($(TASK_2::equalsIgnoreCase), ListsVerifier::runTask),
                    Case($(TASK_3::equalsIgnoreCase), FileWriter::runTask),
                    Case($(TASK_4::equalsIgnoreCase), StringsFormatter::runTask),
                    Case($(TASK_5::equalsIgnoreCase), FALSE),
                    Case($(TASK_6::equalsIgnoreCase), FALSE),
                    Case($(TASK_7::equalsIgnoreCase), ObjectPoolUsage::runTask),
                    Case($(), () -> {
                        throw new IllegalArgumentException(arg);
                    })
            );
            incrementTasksCounter(result);
        }
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("Tasks run count: " + tasksCounter);
    }

    private static void incrementTasksCounter(boolean taskCompleted) {
        if (taskCompleted) {
            tasksCounter++;
        }
    }

}
