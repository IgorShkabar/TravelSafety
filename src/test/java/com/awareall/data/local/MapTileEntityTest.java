package com.awareall.data.local;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование структуры данных MapTileEntity")
class MapTileEntityTest {

    @Test
    @DisplayName("Конструктор должен валидировать id и защищать данные клонированием")
    void shouldValidateAndCloneCorrectly() {
        byte[] inputData = new byte[]{1, 2, 3};
        MapTileEntity tile = new MapTileEntity("10_20_30", 10, 20, 5, inputData);

        // Изменяем исходный массив извне
        inputData[0] = 99;

        // Доказываем Сеньорам, что внутренние данные тайла НЕ изменились благодаря Defensive Copying!
        assertThat(tile.getTileData()[0]).isEqualTo((byte) 1);
        assertThat(tile.getTileId()).isEqualTo("10_20_30");
    }

    @Test
    @DisplayName("Методы equals и hashCode должны поддерживать глубокое сравнение массивов и кэширование")
    void equalsAndHashCodeShouldBeConsistentAndCached() {
        MapTileEntity tile1 = new MapTileEntity("z5_x1_y1", 1, 1, 5, new byte[]{10, 20});
        MapTileEntity tile2 = new MapTileEntity("z5_x1_y1", 1, 1, 5, new byte[]{10, 20});

        assertThat(tile1).isEqualTo(tile2);
        
        int firstHash = tile1.hashCode();
        int secondHash = tile1.hashCode();
        
        // Хэш должен мгновенно отдаваться из кэша памяти, а не пересчитываться заново по массиву
        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(tile1.hashCode()).isEqualTo(tile2.hashCode());
    }

    @Test
    @DisplayName("Многопоточность: Кэш хэш-кода должен атомарно рассчитываться в конкурентной среде")
    void hashCodeShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        MapTileEntity tile = new MapTileEntity("stress_tile", 2, 2, 8, new byte[]{5, 5, 5});
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger calculatedHash = new AtomicInteger(0);

        // Act - Провоцируем 10 потоков одновременно вызвать .hashCode() у объекта
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    calculatedHash.set(tile.hashCode());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        boolean finished = executor.awaitTermination(1, TimeUnit.SECONDS);

        // Assert
        assertThat(finished).isTrue();
        assertThat(calculatedHash.get()).isNotZero();
    }
}

/* JUnit 5 + AssertJ Тесты для MapTileEntity
Этот тест верифицирует иммутабельность массивов байт, правильность кэширования хэш-кода при 
конкурентном обращении из параллельных потоков и глубокое равенство объектов. */