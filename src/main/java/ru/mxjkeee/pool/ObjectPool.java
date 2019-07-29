package ru.mxjkeee.pool;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;


public class ObjectPool<T> {
    private static final long DEFAULT_TOTAL_WAIT_TIME_MILLIS = 5000;
    private static final long DEFAULT_TRY_LOCK_MILLIS = 500;

    private final List<T> freeObjects = new LinkedList<>();
    private final List<T> usedObjects;
    private final Lock lock = new ReentrantLock();
    private final Condition hasFreeObjects = lock.newCondition();
    @Getter
    private long totalWaitTimeMillis = DEFAULT_TOTAL_WAIT_TIME_MILLIS;

    public ObjectPool(List<T> freeObjects) {
        checkListIsNotEmpty(freeObjects);
        System.out.println("Initial objects " + freeObjects);
        this.freeObjects.addAll(freeObjects);
        this.usedObjects = new ArrayList<>(freeObjects.size());
    }

    public ObjectPool(List<T> freeObjects, long totalWaitTimeMillis) {
        this(freeObjects);
        this.totalWaitTimeMillis = totalWaitTimeMillis;
    }

    @SneakyThrows({InterruptedException.class, TimeoutException.class})
    public List<T> getFreeObjects() {
        try {
            tryLockOrThrowException();
            return unmodifiableList(freeObjects);
        } finally {
            lock.unlock();
        }
    }

    @SneakyThrows({InterruptedException.class, TimeoutException.class})
    public List<T> getUsedObjects() {
        try {
            tryLockOrThrowException();
            return unmodifiableList(usedObjects);
        } finally {
            lock.unlock();
        }
    }

    public T getObject() {
        try {
            tryLockOrThrowException();
            while (freeObjects.isEmpty())
                awaitOrThrowException(hasFreeObjects);
            return getFreeObject();
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void releaseObject(T object) {
        requireNonNull(object);
        try {
            tryLockOrThrowException();
            removeObjectFromUsedObjects(object);
            ((LinkedList<T>) freeObjects).addLast(object);
            hasFreeObjects.signal();
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private T getFreeObject() {
        T object = ((LinkedList<T>) freeObjects).pop();
        usedObjects.add(object);
        return object;
    }

    private void removeObjectFromUsedObjects(T object) {
        if (!usedObjects.remove(object))
            throw new IllegalArgumentException("Object \"" + object.toString() + "\" is not used at the moment!");
    }

    private void tryLockOrThrowException() throws InterruptedException, TimeoutException {
        if (!lock.tryLock(DEFAULT_TRY_LOCK_MILLIS, MILLISECONDS)) {
            throw new TimeoutException("Unable to acquire lock on object pool");
        }
    }

    private void awaitOrThrowException(Condition condition) throws InterruptedException {
        if(!condition.await(totalWaitTimeMillis, MILLISECONDS)) {
            throw new RuntimeException("Unable to get object during the " + totalWaitTimeMillis + " millis");
        }
    }

    private void checkListIsNotEmpty(List<? extends T> inputList) {
        if (isEmpty(inputList)) {
            throw new IllegalArgumentException("Input list is empty or null: " + inputList);
        }
    }
}
