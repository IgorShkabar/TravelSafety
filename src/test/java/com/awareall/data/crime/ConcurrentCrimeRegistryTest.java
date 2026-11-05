package com.awareall.data.crime;

import com.awareall.domain.crime.CountryCrimeStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentCrimeRegistryTest 
{

// Константы для конфигурации многопоточности и таймаутов
private static final int STRESS_TEST_THREADS = 100;
private static final int ATOMIC_TEST_THREADS = 200;
private static final int BATCH_LOAD_ELEMENTS_COUNT = 500;
private static final int FIXED_POOL_SIZE_FOR_ATOMIC_TEST = 32;
private static final long THREAD_TERMINATION_TIMEOUT_SECONDS = 5L;

// Константы для тестовых данных (индексы и веса)
private static final double BASE_TEST_CRIME_INDEX = 60.0;
private static final double BASE_TEST_SAFETY_INDEX = 40.0;
private static final double ACCELERATED_CRIME_INDEX_START = 10.0;
private static final double ACCELERATED_SAFETY_INDEX = 90.0;
private static final int CRIME_INDEX_MODULO_STEP = 50;

private static final long INITIAL_INCIDENTS_COUNT = 5L;
private static final long ZERO_INITIAL_INCIDENTS = 0L;

private static final double TEST_LOCAL_FACTOR = 1.5;
private static final double TEST_LIVE_THREAT_WEIGHT = 2.0;

// Константы для кодов стран
private static final String COUNTRY_CODE_US = "US";
private static final String COUNTRY_CODE_GB = "GB";
private static final String BATCH_COUNTRY_CODE_PREFIX = "CN";
private static final String SAMPLE_BATCH_INDEX_1 = "CN100";
private static final String SAMPLE_BATCH_INDEX_2 = "CN499";
private static final int EXPECTED_INCIDENTS_FOR_SAMPLE_2 = 499;

private ConcurrentCrimeRegistry registry;

@BeforeEach
public void setUp() {
registry = new ConcurrentCrimeRegistry();
}

@Test
@DisplayName("Стресс-тест: Параллельное чтение и запись одной локации из 100 потоков")
public void testConcurrentReadWriteStress() throws InterruptedException {
ExecutorService executor = Executors.newFixedThreadPool(STRESS_TEST_THREADS);
CountDownLatch latch = new CountDownLatch(1);
registry.updateStat(new CountryCrimeStat(
        COUNTRY_CODE_US, 
        BASE_TEST_CRIME_INDEX, 
        BASE_TEST_SAFETY_INDEX, 
        INITIAL_INCIDENTS_COUNT
));

for (int i = 0; i < STRESS_TEST_THREADS; i++) {
    final int index = i;
    executor.submit(() -> {
        try {
            latch.await(); 
            registry.updateStat(new CountryCrimeStat(
                    COUNTRY_CODE_US, 
                    BASE_TEST_CRIME_INDEX + index, 
                    BASE_TEST_SAFETY_INDEX, 
                    index
            ));
            registry.calculateDangerWeight(COUNTRY_CODE_US, TEST_LOCAL_FACTOR);
            registry.calculateDynamicDangerLevel(COUNTRY_CODE_US, TEST_LIVE_THREAT_WEIGHT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}

latch.countDown(); 
executor.shutdown();
boolean finished = executor.awaitTermination(THREAD_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);

assertTrue(finished, "Потоки не успели завершить работу, возможна блокировка!");
assertNotNull(registry.getStat(COUNTRY_CODE_US), "Данные не должны быть null после параллельной записи");

}

@Test
@DisplayName("Проверка атомарности: параллельное увеличение счетчика инцидентов в статистике")
public void testAtomicIncidentCounterIntegrity() throws InterruptedException {
CountryCrimeStat stat = new CountryCrimeStat(
COUNTRY_CODE_GB,
BASE_TEST_CRIME_INDEX,
BASE_TEST_SAFETY_INDEX,
ZERO_INITIAL_INCIDENTS
);
registry.updateStat(stat);
ExecutorService executor = Executors.newFixedThreadPool(FIXED_POOL_SIZE_FOR_ATOMIC_TEST);
CountDownLatch latch = new CountDownLatch(1);

for (int i = 0; i < ATOMIC_TEST_THREADS; i++) {
    executor.submit(() -> {
        try {
            latch.await();
            registry.getStat(COUNTRY_CODE_GB).incrementIncidents();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}

latch.countDown();
executor.shutdown();
assertTrue(executor.awaitTermination(THREAD_TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS));

assertEquals(ATOMIC_TEST_THREADS, registry.getStat(COUNTRY_CODE_GB).getIncidentCount(),
        "Потеряны инкременты инцидентов при одновременном обращении!");

}

@Test
@DisplayName("Проверка parallelBatchLoad: корректность многопоточного импорта списка данных")
public void testParallelBatchLoadPerformanceAndSafety() {
List<CountryCrimeStat> rawData = new ArrayList<>();
for (int i = 0; i < BATCH_LOAD_ELEMENTS_COUNT; i++) {
    rawData.add(new CountryCrimeStat(
            BATCH_COUNTRY_CODE_PREFIX + i, 
            ACCELERATED_CRIME_INDEX_START + (i % CRIME_INDEX_MODULO_STEP), 
            ACCELERATED_SAFETY_INDEX, 
            i
    ));
}

registry.parallelBatchLoad(rawData);

assertNotNull(registry.getStat(SAMPLE_BATCH_INDEX_1));
assertNotNull(registry.getStat(SAMPLE_BATCH_INDEX_2));
assertEquals(EXPECTED_INCIDENTS_FOR_SAMPLE_2, registry.getStat(SAMPLE_BATCH_INDEX_2).getIncidentCount());

}

@Test
@DisplayName("Защита от сбоев: передача null в метод пакетной загрузки")
public void testParallelBatchLoadWithNull() {
assertDoesNotThrow(() -> registry.parallelBatchLoad(null));
}

}

/*
4. Многопоточные тесты (JUnit 5 + Concurrency Verification)
Интервьюеры обращают колоссальное внимание на то, как кандидат 
тестирует гонки данных (Race Conditions). 
Напишем стресс-тест для ConcurrentCrimeRegistry, имитирующий 
одновременную работу сотен потоков.Создаем тест в src/test/java/com/awareall/data/crime/ConcurrentCrimeRegistryTest.java:
 */