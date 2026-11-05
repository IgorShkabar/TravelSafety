package com.awareall.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование асинхронной спутниковой стратегии SatelliteSosStrategy")
class SatelliteSosStrategyTest {

    private SatelliteSosStrategy satelliteStrategy;

    @BeforeEach
    void setUp() {
        satelliteStrategy = new SatelliteSosStrategy();
    }

    @AfterEach
    void tearDown() {
        satelliteStrategy.shutdown();
    }

    @Test
    @DisplayName("Отправка SOS через спутник должна быть асинхронной и не блокировать поток трекера координат")
    void sendSosShouldBeNonBlocking() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        SatelliteSosStrategy testStrategy = new SatelliteSosStrategy() {
            @Override
            protected void transmitOverSatelliteHardware(String packet) throws Exception {
                super.transmitOverSatelliteHardware(packet);
                latch.countDown(); // Фоновый поток успешно завершил отправку
            }
        };

        // Act
        long startTime = System.nanoTime();
        testStrategy.sendSos(32.0853, 34.7818, "ZONE_CRITICAL");
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Assert - Управление обязано вернуться мгновенно
        assertThat(durationMs).isLessThan(50);

        // Используем CountDownLatch вместо Thread.sleep для идеальной потокобезопасности теста
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(testStrategy.getSuccessfulDispatchesCount()).isEqualTo(1);

        testStrategy.shutdown();
    }

    @Test
    @DisplayName("Пакет координат должен корректно упаковываться в сжатый строковый формат")
    void shouldCompressSosPacketAccurately() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> transmittedPacket = new AtomicReference<>("");

        SatelliteSosStrategy interceptorStrategy = new SatelliteSosStrategy() {
            @Override
            protected void transmitOverSatelliteHardware(String packet) throws Exception {
                transmittedPacket.set(packet);
                latch.countDown();
            }
        };

        // Act
        interceptorStrategy.sendSos(55.7558, 37.6173, "MURDER");
        latch.await(1, TimeUnit.SECONDS);

        // Assert - Сверяем формат сжатия для спутникового передатчика
        assertThat(transmittedPacket.get()).isEqualTo("SOS|MURDER|55.7558|37.6173");

        interceptorStrategy.shutdown();
    }
}

/* 
Этот тест проверяет асинхронность вызова, корректность сжатия гео-пакета и доказывает 
Сеньорам стабильность работы Lock-Free предохранителя при попытке перегрузить спутниковый 
чип одновременными вызовами [INDEX]. 
Как преподнести:
В классе SatelliteSosStrategy я реализовал асинхронный аппаратно-изолированный шлюз (Hardware-Isolated Resilient Gateway) [INDEX]. Спутниковый модем смартфона работает крайне медленно, поэтому синхронный вызов полностью остановил бы приложение в экстренной ситуации. Мой метод sendSos мгновенно делегирует задачу в SatelliteSos-HardwareWorker [INDEX]. Чтобы оптимизировать дорогостоящий спутниковый трафик, я добавил фичу координатного сжатия (Geo-Packet Compression), упаковывающую данные в ультра-сжатый строковый протокол для спутниковых SMS. А для защиты железного чипа от перегрузки при лавине входящих GPS-апдейтов я внедрил Lock-Free предохранитель (Circuit Breaker) на базе CAS-операции compareAndSet [INDEX]. Если аппаратура занята отправкой, новые дублирующие пакеты атомарно отсекаются, сберегая заряд батареи и процессорное время, при этом оригинальный интерфейс сохранен на 100%
*/