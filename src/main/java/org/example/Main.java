package org.example;

import org.example.locks.RWLock;
import org.example.locks.ReaderPreferenceRWLock;
import org.example.locks.SemaphoreNoPreferenceRWLock;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final int[] cnt = {0};

        RWLock rwLock = new SemaphoreNoPreferenceRWLock();

        List<Thread> threads = new ArrayList<>();

        for(int i = 0; i < 100; ++i) {
            int writerId = i;
            Thread writer = new Thread(() -> {
                rwLock.acquireWriterLock();
                ++cnt[0];
                System.out.printf("Writer %d, wrote value: %d\n", writerId, cnt[0]);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                rwLock.releaseWriterLock();
            });

            threads.add(writer);
        }

        for(int i = 0; i < 2; ++i) {
            int readerId = i;
            Thread reader = new Thread(() -> {
                rwLock.acquireReaderLock();
                System.out.printf("Reader %d, read value: %d\n", readerId, cnt[0]);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                rwLock.releaseReaderLock();
            });

            threads.add(reader);
        }

        threads
                .forEach(Thread::start);

        threads
                .forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });


    }
}