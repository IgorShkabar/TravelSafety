package com.awareall.domain.police;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class EmergencyConnectorTest 
{
    // 1. Создаем РУЧНОЙ Fake-объект, реализующий наш интерфейс
    // Он полностью имитирует реальную работу в фоновых потоках
    private static class FakeEmergencyConnector implements EmergencyConnector 
    {
        private final AtomicBoolean shouldSucceed;
        private final AtomicBoolean isPriorityCalled = new AtomicBoolean(false);

        public FakeEmergencyConnector(boolean shouldSucceed) {
            this.shouldSucceed = new AtomicBoolean(shouldSucceed);
        }

        @Override
        public CompletableFuture<Boolean> sendInstantSos(String coordinates, String message) {
            // Имитируем асинхронную отправку в фоновом потоке без Mockito
            return CompletableFuture.supplyAsync(() -> shouldSucceed.get());
        }

        @Override
        public CompletableFuture<Boolean> sendPrioritySos(String coordinates, String message, double dangerLevel, boolean isLifeThreatening) {
            return CompletableFuture.supplyAsync(() -> {
                isPriorityCalled.set(true);
                return shouldSucceed.get();
            });
        }

        @Override
        public CompletableFuture<Boolean> checkChannelAvailability() {
            return CompletableFuture.supplyAsync(() -> shouldSucceed.get());
        }

        // Вспомогательный метод для проверки вызова (вместо Mockito.verify)
        public boolean wasPriorityMethodCalled() {
            return isPriorityCalled.get();
        }
    }

    @Test
    @DisplayName("Тест на чистом Java: Успешная асинхронная отправка SOS через Fake")
    public void testSendInstantSos_Success() throws InterruptedException, ExecutionException, TimeoutException {
        // Инициализируем наш ручной Fake-объект в режиме успешной работы
        EmergencyConnector connector = new FakeEmergencyConnector(true);

        CompletableFuture<Boolean> resultFuture = connector.sendInstantSos("55.7558,37.6173", "HELP");
        
        // Получаем результат из потока с безопасным таймаутом
        Boolean success = resultFuture.get(1, TimeUnit.SECONDS);

        assertTrue(success, "Фоновый поток должен вернуть true");
    }

    @Test
    @DisplayName("Тест на чистом Java: Проверка фиксации вызова приоритетного метода")
    public void testSendPrioritySos_ExecutionTracking() throws InterruptedException, ExecutionException, TimeoutException {
        FakeEmergencyConnector connector = new FakeEmergencyConnector(true);

        CompletableFuture<Boolean> resultFuture = connector.sendPrioritySos("0.0,0.0", "CRITICAL", 90.0, true);
        resultFuture.get(1, TimeUnit.SECONDS);

        // Проверяем, что метод действительно выполнялся внутри потока
        assertTrue(connector.wasPriorityMethodCalled(), "Метод отправки приоритетного SOS должен быть вызван");
    }

    @Test
    @DisplayName("Тест на чистом Java: Симуляция падения канала связи")
    public void testCheckChannelAvailability_Offline() throws InterruptedException, ExecutionException, TimeoutException {
        // Создаем объект в режиме симуляции ошибки (канал недоступен)
        EmergencyConnector connector = new FakeEmergencyConnector(false);

        CompletableFuture<Boolean> resultFuture = connector.checkChannelAvailability();
        Boolean isOnline = resultFuture.get(1, TimeUnit.SECONDS);

        assertFalse(isOnline, "Метод должен вернуть false, если симулируется обрыв связи");
    }
}
/* 
Да, без Mockito можно обойтись, и в многопоточных тестах это часто бывает даже надежнее.
Вместо использования библиотек генерации заглушек мы можем применить паттерн Fake 
(поддельный объект) или Stub (заглушка). Мы напишем простейшую ручную реализацию интерфейса прямо
внутри тестового файла (как вложенный класс).
Вот пример того, как протестировать обогащенный интерфейс EmergencyConnector исключительно средствами 
чистого Java Concurrency и JUnit 5:
*/
