package kr.hhplus.be.server.common.lock.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.hhplus.be.server.common.lock.application.LockManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReentrantLockManagerTest {
    
    LockManager lockManager = new ReentrantLockManager();

    /**
     * Lock 테스트, 다수 스레드로 하나의 공유 변수를 증가 시킬 때 일관성이 지켜지는지 검증한다.
     */
    @DisplayName("Lock을 획득하면 동시에 여러 요청이 와도 데이터 일관성이 지켜진다.")
    @Test
    void Lock을_획득하면_동시에_여러요청이_와도_데이터_일관성이_지켜진다() throws Exception {
        // given
        int threadCount = 5000;
        String key = "12345";

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<CompletableFuture<Void>> features = new ArrayList<>();
        SharedData sharedData = new SharedData();

        // when
        for (int i = 0; i < threadCount; i++) {
            features.add(CompletableFuture.runAsync(() -> {
                try {
                    countDownLatch.await();
                    lockManager.lock(key);
                    sharedData.increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lockManager.unlock(key);
                }
            }, executorService));
        }

        countDownLatch.countDown();
        for (Future<Void> feature : features) {
            feature.get();
        }

        // then
        assertThat(sharedData.getData()).isEqualTo(threadCount);
    }

    /**
     * tryLock 메서드 테스트, 정해진 시간동안만 Lock 획득 시도를 하는지 검증한다.
     */
    @Test
    @DisplayName("tryLock 메서드는 정해진 시간동안만 Lock 획득을 대기한다.")
    void tryLock_메서드는_정해진_시간동안만_Lock_획득을_대기한다() throws Exception {
        // given
        String key = "12345";
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            try {
                lockManager.lock(key);
                countDownLatch.countDown(); // Lock 획득 후 신호 보냄
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockManager.unlock(key);
            }
        });
        // when
        thread.start();
        countDownLatch.await(); // 신호 보낼때 까지 대기

        boolean locked = lockManager.tryLock(key, Duration.ofMillis(1000));
        // then
        assertThat(locked).isFalse();
    }

    static class SharedData {
        int data = 0;

        void increment() {
            data++;
        }

        public int getData() {
            return data;
        }
    }


}