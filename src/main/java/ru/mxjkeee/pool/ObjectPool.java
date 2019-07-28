package ru.mxjkeee.pool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;


public class ObjectPool<T> {
    private static final long DEFAULT_POLLING_INTERVAL_MILLIS = 100;
    private static final long DEFAULT_TOTAL_WAIT_TIME_MILLIS = 5000;
    private static final long DEFAULT_TRY_LOCK_MILLIS = 100;

    private final List<T> freeObjects = new LinkedList<>();
    private final List<T> usedObjects;
    private final Lock lock = new ReentrantLock();
    private long totalWaitTimeMillis = DEFAULT_TOTAL_WAIT_TIME_MILLIS;
    private long pollingIntervalMillis = DEFAULT_POLLING_INTERVAL_MILLIS;

    private ThreadLocal<Long> pollingStartTimeMillis = new ThreadLocal<>();

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

    public ObjectPool(List<T> freeObjects, long totalWaitTimeMillis, long pollingIntervalMillis) {
        this(freeObjects, totalWaitTimeMillis);
        this.pollingIntervalMillis = pollingIntervalMillis;
    }

    public T getObject() {
        try {
            lock.tryLock(DEFAULT_TRY_LOCK_MILLIS, MILLISECONDS);
            if (freeObjects.isEmpty()) {
                lock.unlock();
                return waitAndTryGetObjectAgain();
            } else {
                return getFreeObject();
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + currentThread().getId() + " is interrupted");
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
            pollingStartTimeMillis.set(null);
        }
    }

    private T getFreeObject() {
        T object = ((LinkedList<T>) freeObjects).pop();
        usedObjects.add(object);
        return object;
    }

    public void releaseObject(T object) {
        requireNonNull(object);
        try {
            lock.tryLock(DEFAULT_TRY_LOCK_MILLIS, MILLISECONDS);
            removeObjectFromUsedObjects(object);
            ((LinkedList<T>) freeObjects).addLast(object);
        } catch (InterruptedException e) {
            System.out.println("Thread is interrupted");
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private T waitAndTryGetObjectAgain() throws InterruptedException {
        setPollingStartTimeMillis();
        if ((currentTimeMillis() - pollingStartTimeMillis.get()) < totalWaitTimeMillis) {
            sleep(pollingIntervalMillis);
            return getObject();
        } else {
            throw new InterruptedException("Thread " + currentThread().getId() + " is interrupted");
        }
    }

    private void setPollingStartTimeMillis() {
        if (pollingStartTimeMillis.get() == null) {
            pollingStartTimeMillis.set(currentTimeMillis());
        }
    }

    private void checkListIsNotEmpty(List<? extends T> inputList) {
        if (isEmpty(inputList)) {
            throw new IllegalArgumentException("Input list is empty or null: " + inputList);
        }
    }

    private void removeObjectFromUsedObjects(T object) {
        if (!usedObjects.remove(object))
            throw new IllegalArgumentException("Object \"" + object.toString() + "\" is not used at the moment!");
    }
}