package kr.hhplus.be.server.common.infrastructure.lock;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import kr.hhplus.be.server.common.application.lock.LockManager;
import org.springframework.stereotype.Component;

@Component
public class ReentrantLockManager implements LockManager {

    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private ReentrantLock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }

    @Override
    public void lock(String key) {
        getLock(key).lock();
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = lockMap.get(key);
        // isHeldByCurrentThread -> 같은 스레드에서만 unlock
        if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @Override
    public boolean tryLock(String key, Duration timeout) {
        ReentrantLock lock = getLock(key);
        try {
            return lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
