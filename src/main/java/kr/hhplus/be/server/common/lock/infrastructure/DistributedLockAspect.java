package kr.hhplus.be.server.common.lock.infrastructure;

import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.common.lock.infrastructure.exception.TryLockFailedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
public class DistributedLockAspect {

    private final RedissonClient redissonClient;


    public DistributedLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around(value = "@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);

        if (!acquired) {
            throw new TryLockFailedException(lockKey);
        }

        try {
            if (!isTransactionActive()) {
                return proceedAndUnlock(joinPoint, lock);
            }
            TransactionSynchronizationManager.registerSynchronization(getSynchronization(lock));
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (isTransactionActive()) {
                throw throwable;
            }
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                throw throwable;
            }
        }

        return joinPoint.proceed();
    }

    private TransactionSynchronization getSynchronization(RLock lock) {
        return new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        };
    }

    private boolean isTransactionActive() {
        return TransactionSynchronizationManager.isSynchronizationActive();
    }

    private Object proceedAndUnlock(ProceedingJoinPoint joinPoint, RLock lock) throws Throwable {
        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }

}
