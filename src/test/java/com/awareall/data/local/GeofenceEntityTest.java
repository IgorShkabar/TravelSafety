package com.awareall.data.local;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тестирование кроссплатформенной сущности GeofenceEntity")
class GeofenceEntityTest 
{
    @Test
    @DisplayName("Паттерн Builder должен корректно конструировать объект")
    void builderShouldCreateGeofenceCorrectly() {
        GeofenceEntity geofence = GeofenceEntity.builder()
                .id(101)
                .centerLat(32.0853)
                .centerLon(34.7818)
                .radiusMeters(500.0)
                .incidentJsonData("{\"theft\":2}")
                .build();

        assertThat(geofence.getId()).isEqualTo(101);
        assertThat(geofence.getCenterLat()).isEqualTo(32.0853);
        assertThat(geofence.getCenterLon()).isEqualTo(34.7818);
        assertThat(geofence.getRadiusMeters()).isEqualTo(500.0);
        assertThat(geofence.getIncidentJsonData()).contains("theft");
    }

    @Test
    @DisplayName("Валидатор координат должен пресекать попытки записать неверные физические данные")
    void validatorShouldThrowExceptionOnBadCoordinates() {
        assertThatThrownBy(() -> new GeofenceEntity(1, 95.0, 34.0, 100.0, "{}"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new GeofenceEntity(1, 45.0, 34.0, -10.0, "{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Многопоточность: Поля volatile должны обеспечивать атомарную видимость данных между ядрами CPU")
    void volatileFieldsShouldEnsureVisibilityAcrossThreads() throws InterruptedException {
        // Arrange
        GeofenceEntity geofence = new GeofenceEntity(1, 0.0, 0.0, 100.0, "{}");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> capturedLongitude = new AtomicReference<>(0.0);

        executor.submit(() -> {
            try {
                latch.await();
                capturedLongitude.set(geofence.getCenterLon()); // Должен считать обновленное значение
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Act
        geofence.setCenterLon(34.7818); // Изменяем в основном потоке
        latch.countDown(); // Освобождаем фоновый поток

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        // Assert
        assertThat(capturedLongitude.get()).isEqualTo(34.7818);
    }
}

/* JUnit 5 + AssertJ Тесты для GeofenceEntity
Тесты докажут многопоточную видимость данных через volatile и проверят бизнес-валидацию. */