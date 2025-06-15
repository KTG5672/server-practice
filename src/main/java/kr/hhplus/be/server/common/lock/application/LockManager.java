package kr.hhplus.be.server.common.lock.application;

import java.time.Duration;

/**
 * LockManager 인터페이스
 */
public interface LockManager {

    void lock(String key);
    void unlock(String key);
    boolean tryLock(String key, Duration timeout);

}
