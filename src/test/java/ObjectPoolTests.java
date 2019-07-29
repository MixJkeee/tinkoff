import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mxjkeee.pool.ObjectPool;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectPoolTests {

    @Test
    @DisplayName("Проверка метода getObject()")
    void checkGetObjectMethod() {
        ObjectPool<String> stringObjectPool = new ObjectPool<>(singletonList("test"));
        stringObjectPool.getObject();
        assertThat(stringObjectPool.getFreeObjects()).isEmpty();
        assertThat(stringObjectPool.getUsedObjects()).hasSize(1).contains("test");
    }

    @Test
    @DisplayName("Проверка метода releaseObject()")
    void checkReleaseObjectMethod() {
        ObjectPool<Integer> integerObjectPool = new ObjectPool<>(asList(1, 2));
        integerObjectPool.releaseObject(integerObjectPool.getObject());
        assertThat(integerObjectPool.getUsedObjects()).isEmpty();
        assertThat(integerObjectPool.getFreeObjects()).hasSize(2).contains(1, 2);
    }

    @Test
    @DisplayName("Невозможно передать null в качестве списка элементов при инициализации")
    void checkNullList() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> new ObjectPool<Integer>(null));
        assertThat(exception.getMessage()).isEqualTo("Input list is empty or null: null");
    }

    @Test
    @DisplayName("Невозможно передать пустой список в качестве списка элементов при инициализации")
    void checkEmptyList() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> new ObjectPool<Integer>(emptyList()));
        assertThat(exception.getMessage()).isEqualTo("Input list is empty or null: []");
    }

    @Test
    @DisplayName("Проверка таймаута ожидания свободного объекта")
    void checkTimeoutExceptionWhenTryingToGetObject() {
        long totalWaitTimeMillis = 1000;
        ObjectPool<String> stringPool = new ObjectPool<>(singletonList("test"), totalWaitTimeMillis);
        stringPool.getObject();
        Throwable exception = assertThrows(RuntimeException.class, () -> {
                    stringPool.getObject();
                    sleep(totalWaitTimeMillis);
                }
        );
        assertThat(exception.getMessage()).isEqualTo("Unable to get object during the " + stringPool.getTotalWaitTimeMillis() + " millis");
    }

    @Test
    @DisplayName("Проверка ошибки при попытке вызвать releaseObject() с произвольным объектом")
    void checkReleaseObjectException() {
        ObjectPool<String> objectPool = new ObjectPool<>(asList("test1", "test2"));
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> objectPool.releaseObject("test3"));
        assertThat(exception.getMessage()).isEqualTo("Object \"test3\" is not used at the moment!");
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 50})
    @DisplayName("Проверка, что N потоков получают уникальный ID из пула с N элементами без возврата их в пулл")
    void checkObjectPoolWitoutReleasing(int objectsCount) {
        List<Integer> objectsInPool = rangeClosed(1, objectsCount).boxed().collect(toList());
        ObjectPool<Integer> objectPool = new ObjectPool<>(objectsInPool);
        List<Integer> resultList = rangeClosed(1, objectsCount).parallel()
                .mapToObj(i -> objectPool.getObject())
                .sorted()
                .collect(toList());
        assertThat(resultList).isEqualTo(objectsInPool);
    }


    @ParameterizedTest
    @ValueSource(ints = {10, 50})
    @DisplayName("Проверка, что N потоков получают уникальный ID из пула с N элементами с возвратом их в пулл")
    void checkObjectsPoolWithReleasing(int objectsCount) {
        List<Integer> objectsInPool = rangeClosed(1, objectsCount).boxed().collect(toList());
        ObjectPool<Integer> objectPool = new ObjectPool<>(objectsInPool);
        List<Integer> resultList = rangeClosed(1, objectsCount).parallel()
                .mapToObj(i -> getObjectFromPoolWithDelay(objectPool, 300))
                .sorted()
                .collect(toList());
        assertThat(resultList).isEqualTo(objectsInPool);
    }

    @SneakyThrows(InterruptedException.class)
    private static void sleep(long millis) {
        Thread.sleep(millis);
    }

    private static <T> T getObjectFromPoolWithDelay(ObjectPool<T> objectPool, long delayMillis) {
        T object = objectPool.getObject();
        sleep(delayMillis);
        objectPool.releaseObject(object);
        return object;
    }
}
