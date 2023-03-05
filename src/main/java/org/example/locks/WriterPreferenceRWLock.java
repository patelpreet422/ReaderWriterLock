package org.example.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
NOTE:
    when we say the thread is holding the lock we do not explicitly mean that thread should hold the lock
    it may or may not hold the lock but the important idea is that the once the thread enters the critical the invariant
    is established

    like in this case look at acquireWriterLock() method, when client calls this method we should hold the lock isn't it?
    but if you look carefully we are releasing the lock when we exit the method!

    The reason we are able to do this is because of condition variable you see condition variable are
    suitable for such use cases where a condition needs to be meet before we enter the critical section,
    condition variable allows us to use mutex that this condition variable is associated to
    help us maintain the variables needed to form the condition and then use these conditions as the barrier that dictates
    when and when not allows the access to critical section. This is the most important realisation to understand and
    how to make use of condition variable and mutex in Java.

    condition.signal
        - means signal the places where condition.await is called as we have updated some variable and that may lead to
            some condition to be true, and they can execute now if they want
    condition.await
        - avoid CPU busy wait and check for the condition after some time usually when condition.signal is called somewhere
 */
public class WriterPreferenceRWLock implements RWLock {
    final ReentrantLock mutex = new ReentrantLock();
    final Condition condition = mutex.newCondition();

    // number of active readers
    long readerCount = 0;

    // number of writer waiting for acquiring the lock
    long writerWaiting = 0;

    //to prevent one writer to wait for another writer if first writer is already holding writing

    boolean writerActive = false;

    @Override
    public void acquireReaderLock() {
       mutex.lock();

       // reader can only acquire the lock if there are no active writer and no writer waiting for the lock
       try {
           while (writerWaiting > 0 || writerActive) {
               condition.await();
           }
       } catch (InterruptedException e) {
           throw new RuntimeException(e);
       }

       ++readerCount;
        mutex.unlock();
    }

    @Override
    public void releaseReaderLock() {
        mutex.lock();
        --readerCount;

        if(readerCount == 0) {
            condition.signal();
        }

        mutex.unlock();
    }

    @Override
    public void acquireWriterLock() {
        mutex.lock();
        ++writerWaiting;

        try {
            while (readerCount > 0 || writerActive) {
                condition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        writerActive = true;
        --writerWaiting;

        mutex.unlock();
    }

    @Override
    public void releaseWriterLock() {
       mutex.lock();
       writerActive = false;
       condition.signalAll();
       mutex.unlock();
    }
}
