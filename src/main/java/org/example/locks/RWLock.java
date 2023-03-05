package org.example.locks;

public interface RWLock {
    void acquireReaderLock();
    void releaseReaderLock();
    void acquireWriterLock();
    void releaseWriterLock();
}
