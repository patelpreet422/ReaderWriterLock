package org.example.locks;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class ReaderPreferenceRWLock implements RWLock {
    final ReentrantLock readerMutex = new ReentrantLock();

    /*
    We use Semaphore instead of ReentrantLock because in case of ReentrantLock the thread that locks the lock last owns
    the lock and only the owner of lock can unlock the lock, so it doesn't suit our use case

    we want to restrict any writer once reader acquires the lock and also allow reader to release the lock since it
    initially held the lock but by the time last reader release the lock it may belong to some other thread and using
    Reentrant lock will result in IllegalMonitorStateException, and we use Semaphore
     */
    final Semaphore writerSemaphore = new Semaphore(1);

    long readerCount = 0;

    @Override
    public void acquireReaderLock() {
        readerMutex.lock();
        ++readerCount;
        if(readerCount == 1) {
            try {
                writerSemaphore.acquire(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        readerMutex.unlock();
    }

    @Override
    public void releaseReaderLock() {
        readerMutex.lock();
        --readerCount;

        if(readerCount == 0) {
            writerSemaphore.release(1);
        }

        readerMutex.unlock();
    }

    @Override
    public void acquireWriterLock() {
        try {
            writerSemaphore.acquire(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseWriterLock() {
        writerSemaphore.release(1);
    }
}
