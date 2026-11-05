package com.awareall.domain.crime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CountryCrimeStatTest 
{
    // Константы конфигурации многопоточности
    private static final int CONCURRENT_THREADS_COUNT = 200;
    private static final int FIXED_POOL_SIZE = 32;
    private static final long THREAD_TERMINATION_TIMEOUT_SECONDS = 5L;

    // Константы для тестовых данных
    private static final String INPUT_LOWERCASE_COUNTRY = "fr";
    private static final String EXPECTED_UPPERCASE_COUNTRY = "FR";
    private static final double TEST_CRIME_INDEX = 45.5;
    private static final double TEST_SAFETY_INDEX = 54.5;
    private static final long INITIAL_INCIDENTS_ZERO = 0L;
    private static final long INITIAL_INCIDENTS_SPECIFIC = 10L;

    @Test
    @DisplayName("Проверка конструктора: корректное приведение кода страны к верхнему регистру")
    public void testCountryCodeNormalization() {
        CountryCrimeStat stat = new CountryCrimeStat(
                INPUT_LOWERCASE_COUNTRY, 
                TEST_CRIME_INDEX, 
                TEST_SAFETY_INDEX, 
                INITIAL_INCIDENTS_SPECIFIC
        );

        assertEquals(EXPECTED_UPPERCASE_COUNTRY, stat.getCountryCode(), 
                "Код страны должен автоматически переводиться в верхний регистр");
        assertEquals(TEST_CRIME_INDEX, stat.getCrimeIndex());
        assertEquals(TEST_SAFETY_INDEX, stat.getSafetyIndex());
        assertEquals(INITIAL_INCIDENTS_SPECIFIC, stat.getIncidentCount());
    }

    @Test
    @DisplayName("Проверка конструктора: защита от null значений кода страны")
    public void testCountryCodeNullSafety() {
        CountryCrimeStat stat = new CountryCrimeStat(
                null, 
                TEST_CRIME_INDEX, 
                TEST_SAFETY_INDEX, 
                INITIAL_INCIDENTS_ZERO
        );

        assertNotNull(stat.getCountryCode(), "Код страны не должен быть null");
        assertTrue(stat.getCountryCode().isEmpty(), "При передачи null строка должна инициализироваться как пустая");
    }

    @Test
    @DisplayName("Проверка атомарности: 200 потоков одновременно увеличивают счетчик инцидентов")
    public void testConcurrentIncidentIncrements() throws InterruptedException {
        CountryCrimeStat stat = new CountryCrimeStat(
                EXPECTED_UPPERCASE_COUNTRY, 
                TEST_CRIME_INDEX, 
                TEST_SAFETY_INDEX, 
                INITIAL_INCIDENTS_ZERO
        );

        ExecutorService executor = Executors.newFixedThreadPool(FIXED_POOL_SIZE);
        // Защелка для синхронного старта всех фоновых задач
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < CONCURRENT_THREADS_COUNT; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Потоки блокируются и ждут общую команду старта
                    stat.incrementIncidents();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Одновременный запуск ("выстрел") всех потоков
        executor.shutdown();
        boolean finishedCleanly = executor.awaitTermination(THREAD_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertTrue(finishedCleanly, "Потоки зависли, возможна блокировка ресурсов!");
        
        // Если бы использовался примитив long вместо AtomicLong, значение было бы меньше CONCURRENT_THREADS_COUNT
        assertEquals(CONCURRENT_THREADS_COUNT, stat.getIncidentCount(), 
                "Произошла потеря инкрементов счетчика в конкурентной среде");
    }
}

