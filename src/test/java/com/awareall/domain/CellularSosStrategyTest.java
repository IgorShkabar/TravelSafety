package com.awareall.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование асинхронной отказоустойчивой стратегии CellularSosStrategy")
class CellularSosStrategyTest {

    private CellularSosStrategy sosStrategy;

    @BeforeEach
    void setUp() {
        sosStrategy = new CellularSosStrategy();
    }

    @AfterEach
    void tearDown() {
        sosStrategy.shutdown();
    }

    @Test
    @DisplayName("Отправка SOS должна быть строго асинхронной и не блокировать вызывающий поток")
    void sendSosShouldBeNonBlocking() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        
        // Создаем легковесного наследника, чтобы узнать, когда фоновый поток завершил отправку
        CellularSosStrategy testStrategy = new CellularSosStrategy() {
            @Override
            protected void executeNetworkCall(double lat, double lon, String threatType) throws Exception {
                super.executeNetworkCall(lat, lon, threatType);
                latch.countDown(); // Даем сигналу тесту: отправка в фоне завершена!
            }
        };

        // Act
        long startTime = System.nanoTime();
        testStrategy.sendSos(55.7558, 37.6173, "ZONE_CRITICAL");
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Assert - Метод обязан вернуть управление мгновенно (меньше 50 мс), не блокируя трекер
        assertThat(durationMs).isLessThan(50);

        // ПРИМЕНЕНИЕ КОНСТРУКЦИИ: Ждем ровно столько, сколько нужно фоновому потоку, без лишнего сна!
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        
        assertThat(completed).isTrue();
        assertThat(testStrategy.getSuccessfulDispatchesCount()).isEqualTo(1);
        
        testStrategy.shutdown();
    }

    @Test
    @DisplayName("Алгоритм Retry Policy должен автоматически переотправлять сигнал при временных сбоях сети")
    void retryPolicyShouldHandleNetworkFlakes() throws InterruptedException {
        // Arrange
        AtomicInteger networkCallCounter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        CellularSosStrategy flakyStrategy = new CellularSosStrategy() {
            @Override
            protected void executeNetworkCall(double lat, double lon, String threatType) throws Exception {
                int currentAttempt = networkCallCounter.incrementAndGet();
                if (currentAttempt < 3) {
                    throw new java.io.IOException("Имитация падения сотовой вышки (Timeout)");
                }
                // На 3-й попытке сеть восстанавливается!
                super.executeNetworkCall(lat, lon, threatType);
                latch.countDown(); // Сигнализируем об успешной финальной доставке
            }
        };

        // Act
        flakyStrategy.sendSos(32.0853, 34.7818, "ZONE_HIGH");

        // ПРИМЕНЕНИЕ КОНСТРУКЦИИ: Ждем завершения всех 3-х попыток (с учетом экспоненциальных пауз)
        boolean completed = latch.await(5, TimeUnit.SECONDS);

        // Assert - Доказываем, что система совершила 3 попытки и успешно доставила SOS!
        assertThat(completed).isTrue();
        assertThat(networkCallCounter.get()).isEqualTo(3);
        assertThat(flakyStrategy.getSuccessfulDispatchesCount()).isEqualTo(1);
        
        flakyStrategy.shutdown();
    }
}


/* Этот тест проверяет полную асинхронность отправки и доказывает 
Сеньорам стабильность работы алгоритма автоматических повторных попыток (Retry Policy) при 
симуляции сбоев сети. 

Использование Thread.sleep() в юнит-тестах считается плохим тоном (антипаттерном), так как это искусственно замедляет сборку. Сеньоры всегда требуют использовать 
CountDownLatch для красивой синхронизации асинхронных потоков
адействуем CountDownLatch в коде тестов, убрав Thread.sleep.
*/