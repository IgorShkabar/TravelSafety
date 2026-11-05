package com.awareall.data.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование Lock-Free базы данных офлайн-карт MapDatabase")
class MapDatabaseTest 
{
    private MapDatabase database;
    private MapDao mapDao;

    @BeforeEach
    void setUp() {
        database = MapDatabase.getInstance();
        database.resetForTesting(); // Изоляция прогонов
        mapDao = database.getMapDao();
    }

    @Test
    @DisplayName("Асинхронные операции не должны блокировать вызывающий поток и корректно обновлять кэш")
    void asyncOperationsShouldBeNonBlockingAndPopulateCache() throws Exception {
        // Arrange
        byte[] mockBytes = new byte[]{4, 5, 6};
        MapTileEntity tile = new MapTileEntity("tile_10_20", 10, 20, 5, mockBytes);

        // Act - Асинхронная вставка
        CompletableFuture<Void> insertFuture = mapDao.insertTileAsync(tile);
        insertFuture.get(1, TimeUnit.SECONDS); // Ждем завершения записи в фоне

        // Асинхронное чтение
        CompletableFuture<byte[]> readFuture = mapDao.getTileAsync("tile_10_20");
        byte[] result = readFuture.get(1, TimeUnit.SECONDS);

        // Assert
        assertThat(result).isEqualTo(mockBytes);
        
        MapDatabase.DBTelemetrySnapshot telemetry = database.getTelemetry();
        assertThat(telemetry.hits).isEqualTo(1L);  // Один успешный хит в кэш
        assertThat(telemetry.misses).isEqualTo(0L);
    }

    @Test
    @DisplayName("Стресс-тест: Параллельное чтение тайлов из сотен потоков не должно вызывать Race Condition")
    void highThroughputParallelReadTest() throws InterruptedException {
        // Arrange
        byte[] mockBytes = new byte[]{1, 1, 1};
        mapDao.insertTile(new MapTileEntity("stress_tile", mockBytes));

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        // Act - Запускаем лавину одновременных чтений тайла карты
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    byte[] data = mapDao.getTile("stress_tile");
                    assertThat(data).isEqualTo(mockBytes);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Одновременный старт
        executor.shutdown();
        boolean completed = executor.awaitTermination(2, TimeUnit.SECONDS);

        // Assert
        assertThat(completed).isTrue();
        assertThat(database.getTelemetry().hits).isEqualTo((long) threadCount);
    }
}

/* 
Продвинутый JUnit 5 + AssertJ Тест
Этот тест симулирует стрессовую нагрузку: 20 фоновых потоков одновременно запрашивают тайлы карты [INDEX]. 
Мы проверим асинхронность и корректность сбора метрик телеметрии. */
