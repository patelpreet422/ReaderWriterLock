package org.example.locks;

import org.example.locks.RWLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreNoPreferenceRWLock implements RWLock {
    final ReentrantLock mutex = new ReentrantLock();
    final Condition readerCountIsZero = mutex.newCondition();

    long count = 0;

    @Override
    public void acquireReaderLock() {
        mutex.lock();
        ++count;
        mutex.unlock();
    }

    @Override
    public void releaseReaderLock() {
        mutex.lock();
        --count;

        if(count == 0) {
            readerCountIsZero.signal();
        }

        mutex.unlock();
    }

    @Override
    public void acquireWriterLock() {
//        implements busy wait algorithm
//        while(true) {
//            mutex.lock();
//            if (count == 0) {
//                count = Integer.MAX_VALUE;
//                return;
//            } else {
//                mutex.unlock();
//            }
//        }

        // implement using condition variable so that we wait appropriately and not burn CPU cycles
        mutex.lock();
        try {
            while (count != 0) {
                readerCountIsZero.await();
            }
            count = Integer.MAX_VALUE;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseWriterLock() {
        count = 0;
        readerCountIsZero.signal();
        mutex.unlock();
    }
}
