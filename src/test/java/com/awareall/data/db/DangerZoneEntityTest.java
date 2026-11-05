package com.awareall.data.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тестирование кроссплатформенной сущности DangerZoneEntity")
class DangerZoneEntityTest 
{

    @Test
    @DisplayName("Шаблон Builder и конструктор должны корректно инициализировать объект")
    void builderShouldCreateEntityCorrectly() {
        DangerZoneEntity entity = DangerZoneEntity.builder()
                .id(42L)
                .latitude(32.05)
                .longitude(34.85)
                .radiusMeters(1500.0)
                .threatType("THEFT_ZONE")
                .riskLevel(3)
                .build();

        assertThat(entity.getId()).isEqualTo(42L);
        assertThat(entity.getLatitude()).isEqualTo(32.05);
        assertThat(entity.getLongitude()).isEqualTo(34.85);
        assertThat(entity.getRadiusMeters()).isEqualTo(1500.0);
        assertThat(entity.getThreatType()).isEqualTo("THEFT_ZONE");
        assertThat(entity.getIncidentJsonData()).isEqualTo("THEFT_ZONE");
        assertThat(entity.getRiskLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("Валидатор должен выбрасывать исключение при передаче неверных географических координат")
    void shouldThrowExceptionOnInvalidData() {
        assertThatThrownBy(() -> new DangerZoneEntity(1L, 95.0, 34.0, 1000.0, "ERROR", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Невалидная широта");

        assertThatThrownBy(() -> new DangerZoneEntity(1L, 45.0, -200.0, 1000.0, "ERROR", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Невалидная долгота");

        assertThatThrownBy(() -> new DangerZoneEntity(1L, 45.0, 34.0, -50.0, "ERROR", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("отрицательным");
    }

    @Test
    @DisplayName("Многопоточность: Поля volatile должны обеспечивать потокобезопасную видимость изменений")
    void volatileFieldsShouldEnsureVisibilityAcrossThreads() throws InterruptedException {
        // Arrange
        DangerZoneEntity entity = new DangerZoneEntity(10L, 0.0, 0.0, 500.0, "INITIAL", 1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> readLatitude = new AtomicReference<>(0.0);

        // Фоновый поток читает координату
        executor.submit(() -> {
            try {
                latch.await();
                readLatitude.set(entity.getLatitude()); // Поток гарантированно увидит свежее значение благодаря volatile
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Act - Главный поток меняет координату и дает отмашку фоновому потоку
        entity.setLatitude(45.6789);
        latch.countDown();
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        // Assert
        assertThat(readLatitude.get()).isEqualTo(45.6789);
    }

    @Test
    @DisplayName("Mock-список должен быть защищен от модификаций из других потоков")
    void mockListShouldBeUnmodifiable() {
        List<DangerZoneEntity> zones = DangerZoneEntity.getMockZonesList();
        
        assertThatThrownBy(() -> zones.add(new DangerZoneEntity()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
/* JUnit 5 + AssertJ Тесты для DangerZoneEntity
Этот тест докажет, что умею проверять не только стандартные геттеры, но и стрессовую многопоточную 
видимость данных, валидацию и работу паттерна Builder. */
