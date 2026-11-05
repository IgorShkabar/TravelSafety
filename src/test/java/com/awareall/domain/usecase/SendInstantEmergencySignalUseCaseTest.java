package com.awareall.domain.usecase;

import com.awareall.data.crime.ConcurrentCrimeRegistry;
import com.awareall.domain.crime.CountryCrimeStat;
import com.awareall.domain.police.EmergencyConnector;
import com.awareall.domain.model.ThreatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SendInstantEmergencySignalUseCaseTest {

    // Тестовые константы для исключения магических чисел
    private static final String LOW_RISK_COUNTRY = "GREEN_ZONE";
    private static final String HIGH_RISK_COUNTRY = "RED_ZONE";
    private static final String TEST_COORDINATES = "55.7558,37.6173";

    private static final double MINIMAL_CRIME_INDEX = 0.0;
    // Выставляем завышенный индекс, чтобы базовая опасность + вес MURDER железно пробили порог 70.0
    private static final double CRITICAL_HIGH_CRIME_INDEX = 1000.0;
    private static final double SAFETY_INDEX_PLACEHOLDER = 50.0;
    private static final long INITIAL_INCIDENTS_ZERO = 0L;

    private static final long TEST_AWAIT_TIMEOUT_SECONDS = 10L;
    private static final long BACKGROUND_THREAD_PAUSE_MS = 100L;
    // Сетевой лаг в 5 секунд гарантированно превысит таймаут 1 секунду
    private static final long FORCED_NETWORK_LAG_MS = 5000L; 

    private static final String EXPECTED_SUCCESS_MSG = "SOS Sent via Fast Protocol!";
    private static final String EXPECTED_TIMEOUT_MSG = "SOS Failed due to network timeout or error";

    private ConcurrentCrimeRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new ConcurrentCrimeRegistry();
        // Наполняем реестр нашими тестовыми зонами
        registry.updateStat(new CountryCrimeStat(LOW_RISK_COUNTRY, MINIMAL_CRIME_INDEX, SAFETY_INDEX_PLACEHOLDER, INITIAL_INCIDENTS_ZERO));
        registry.updateStat(new CountryCrimeStat(HIGH_RISK_COUNTRY, CRITICAL_HIGH_CRIME_INDEX, SAFETY_INDEX_PLACEHOLDER, INITIAL_INCIDENTS_ZERO));
    }

    // РУЧНОЙ FAKE 1: Имитация идеально работающей скоростной сети
    private static class FastChannelsFake implements EmergencyConnector {
        @Override
        public CompletableFuture<Boolean> sendInstantSos(String coordinates, String message) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> sendPrioritySos(String coordinates, String message, double dangerLevel, boolean isLifeThreatening) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> checkChannelAvailability() {
            return CompletableFuture.completedFuture(true);
        }
    }

    // РУЧНОЙ FAKE 2: Имитация полностью зависшего сетевого сокета
    private static class LaggingChannelsFake implements EmergencyConnector {
        @Override
        public CompletableFuture<Boolean> sendInstantSos(String coordinates, String message) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(FORCED_NETWORK_LAG_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            });
        }

        @Override
        public CompletableFuture<Boolean> sendPrioritySos(String coordinates, String message, double dangerLevel, boolean isLifeThreatening) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(FORCED_NETWORK_LAG_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            });
        }

        @Override
        public CompletableFuture<Boolean> checkChannelAvailability() {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Test
    @DisplayName("Успешный сценарий: Проверка корректной сборки anyOf и фонового обновления статистики")
    public void testExecute_SuccessScenario() throws Exception {
        EmergencyConnector connectorFake = new FastChannelsFake();
        SendInstantEmergencySignalUseCase useCase = new SendInstantEmergencySignalUseCase(connectorFake, registry);

        // Запуск UseCase с передачей ThreatType.RAPE
        CompletableFuture<String> resultFuture = useCase.execute(LOW_RISK_COUNTRY, TEST_COORDINATES, ThreatType.RAPE);
        String actualResponse = resultFuture.get(TEST_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Проверяем результат
        assertEquals(EXPECTED_SUCCESS_MSG, actualResponse);

        // Проверяем параллельный инкремент инцидента в фоновом потоке
        TimeUnit.MILLISECONDS.sleep(BACKGROUND_THREAD_PAUSE_MS);
        assertEquals(1L, registry.getStat(LOW_RISK_COUNTRY).getIncidentCount(), 
                "Фоновая задача должна была параллельно увеличить счетчик инцидентов для страны");
    }

    @Test
    @DisplayName("Тест адаптивного таймаута: Угроза MURDER в опасной зоне активирует лимит в 1 сек и отсекает зависшую сеть")
    public void testExecute_AdaptiveTimeoutTriggered() throws Exception {
        EmergencyConnector laggingConnector = new LaggingChannelsFake();
        SendInstantEmergencySignalUseCase useCase = new SendInstantEmergencySignalUseCase(laggingConnector, registry);

        // Запуск UseCase с критической угрозой ThreatType.MURDER в опасной стране
        CompletableFuture<String> resultFuture = useCase.execute(HIGH_RISK_COUNTRY, TEST_COORDINATES, ThreatType.MURDER);
        String actualResponse = resultFuture.get(TEST_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Проверяем, что зависшая на 5 секунд сеть была отсечена адаптивным таймаутом в 1 секунду
        assertEquals(EXPECTED_TIMEOUT_MSG, actualResponse, 
                "Система должна была прервать выполнение по адаптивному таймауту в 1 секунду и вернуть дефолтное сообщение");
    }
}
