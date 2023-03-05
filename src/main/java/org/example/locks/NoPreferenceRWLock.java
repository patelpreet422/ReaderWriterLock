package org.example.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NoPreferenceRWLock implements RWLock {
    final ReentrantLock mutex = new ReentrantLock();

    /*
    here we if there are multiple thread waiting on a condition and condition.signalAll is invokes
    then threads are invoked in FIFO order or whatever order condition variable maintains which is not knows to use
    and hence in this implementation it is not knows who is given preference
     */
    final Condition condition = mutex.newCondition();

    long count = 0;

    @Override
    public void acquireReaderLock() {
        mutex.lock();
        try {
            while (count < 0) {
                condition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ++count;
        mutex.unlock();
    }

    @Override
    public void releaseReaderLock() {
        mutex.lock();
        --count;

        if(count == 0) {
            condition.signalAll();
        }

        mutex.unlock();
    }

    @Override
    public void acquireWriterLock() {
//        implements busy wait algorithm
//        while(true) {
//            mutex.lock();
//            if (count == 0) {
//                count = -1;
//                return;
//            } else {
//                mutex.unlock();
//            }
//        }

        // implement using condition variable so that we wait appropriately and not burn CPU cycles
        mutex.lock();
        try {
            while (count != 0) {
                condition.await();
            }
            count = -1;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mutex.unlock();
    }

    @Override
    public void releaseWriterLock() {
        mutex.lock();
        count = 0;
        condition.signalAll();
        mutex.unlock();
    }
}
