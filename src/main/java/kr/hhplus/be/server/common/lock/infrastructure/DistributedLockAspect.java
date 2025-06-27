package kr.hhplus.be.server.common.lock.infrastructure;

import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.common.lock.infrastructure.exception.TryLockFailedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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

        String lockKey = getLockKey(joinPoint, distributedLock);
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

    private String getLockKey(ProceedingJoinPoint joinPoint,
        DistributedLock distributedLock) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        // 1. SpEL 표현식 파서를 생성
        ExpressionParser parser = new SpelExpressionParser();
        // 2. SpEL이 값을 조회할 때 사용할 컨텍스트(변수 저장소) 생성
        EvaluationContext context = new StandardEvaluationContext();
        // 3. 현재 AOP 타겟 메서드의 파라미터 이름과 값들을 꺼냄
        String[] paramNames = methodSignature.getParameterNames();
        // 4. 각 파라미터 이름에 대해 context 값 매핑
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        // 5. 파싱하여 문자열로 반환
        return parser.parseExpression(distributedLock.key()).getValue(context, String.class);
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
