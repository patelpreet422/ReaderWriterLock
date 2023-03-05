package org.example.locks;

import org.example.locks.RWLock;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreNoPreferenceRWLock implements RWLock {
    final Semaphore semaphore = new Semaphore(Integer.MAX_VALUE);

    @Override
    public void acquireReaderLock() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseReaderLock() {
        semaphore.release();
    }

    @Override
    public void acquireWriterLock() {
        try {
            semaphore.acquire(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseWriterLock() {
      semaphore.release(Integer.MAX_VALUE);
    }
}
