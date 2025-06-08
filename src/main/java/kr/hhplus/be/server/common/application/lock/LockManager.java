package kr.hhplus.be.server.common.application.lock;

import java.time.Duration;

/**
 * LockManager 인터페이스
 */
public interface LockManager {

    void lock(String key);
    void unlock(String key);
    boolean tryLock(String key, Duration timeout);

}
