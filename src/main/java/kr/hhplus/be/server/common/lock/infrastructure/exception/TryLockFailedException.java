package kr.hhplus.be.server.common.lock.infrastructure.exception;

public class TryLockFailedException extends RuntimeException {

    public TryLockFailedException(String key) {
        super("TryLock failed: " + key);
    }
}
