package ru.mxjkeee.pool;

import lombok.SneakyThrows;

import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static ru.mxjkeee.Constants.CONSOLE_DELIMITER;

public class ObjectPoolUsage {

    private static ObjectPool<Integer> integerPool = new ObjectPool<>(rangeClosed(1, 10).boxed().collect(toList()));

    public static boolean runTask() {
        System.out.println("Starting task with ObjectPool...");
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("With 10 threads:\n");
        range(0, 10).parallel()
                .forEach(i -> System.out.println("Thread " + i + " has id " + getUniqueId()));
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("With 50 threads:\n");
        range(0, 50).parallel()
                .forEach(i -> System.out.println("Thread " + i + " has id " + getUniqueId()));
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("End of task with ObjectPool");
        return true;
    }

    @SneakyThrows(InterruptedException.class)
    private static Integer getUniqueId() {
        Integer id = integerPool.getObject();
        sleep(300);
        integerPool.releaseObject(id);
        return id;
    }
}
