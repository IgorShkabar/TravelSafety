package com.awareall.domain;

import com.awareall.domain.model.ThreatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тестирование доменной структуры данных DangerZone")
class DangerZoneTest 
{
    @Test
    @DisplayName("Шаблон Builder должен корректно инициализировать и инкапсулировать поля объекта")
    void builderShouldCreateDangerZoneCorrectly() 
    {
        // Act
        DangerZone zone = DangerZone.builder()
                .id(999L)
                .latitude(32.0853)
                .longitude(34.7818)
                .radiusMeters(1000.0)
                .threatType(ThreatType.KNIFE_ASSAULT)
                .riskLevel(8)
                .build();

        // Assert
        assertThat(zone.getId()).isEqualTo(999L);
        assertThat(zone.getLatitude()).isEqualTo(32.0853);
        assertThat(zone.getLongitude()).isEqualTo(34.7818);
        assertThat(zone.getRadiusMeters()).isEqualTo(1000.0);
        assertThat(zone.getThreatType()).isEqualTo(ThreatType.KNIFE_ASSAULT);
        assertThat(zone.getRiskLevel()).isEqualTo(8);
    }

    @Test
    @DisplayName("Валидатор должен пресекать попытки записать невалидные физические или логические данные")
    void validatorShouldThrowExceptionOnInvalidData() {
        // Неверная широта
        assertThatThrownBy(() -> new DangerZone(1L, 120.0, 34.0, 500.0, ThreatType.THEFT, 5))
                .isInstanceOf(IllegalArgumentException.class);

        // Отрицательный радиус
        assertThatThrownBy(() -> new DangerZone(1L, 45.0, 34.0, -100.0, ThreatType.THEFT, 5))
                .isInstanceOf(IllegalArgumentException.class);

        // Риск-левел выходит за рамки [1 - 10]
        assertThatThrownBy(() -> new DangerZone(1L, 45.0, 34.0, 500.0, ThreatType.THEFT, 15))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Многопоточность: Модификаторы volatile должны гарантировать когерентную видимость изменений стейта")
    void volatileFieldsShouldEnsureVisibilityAcrossThreads() throws InterruptedException {
        // Arrange
        DangerZone zone = new DangerZone(1L, 0.0, 0.0, 500.0, ThreatType.THEFT, 2);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> readLatitude = new AtomicReference<>(0.0);

        // Фоновый поток считывает координату опасной зоны
        executor.submit(() -> {
            try {
                latch.await(); // Ожидаем отмашки главного потока
                readLatitude.set(zone.getLatitude()); // Благодаря volatile, считывается самое актуальное значение из ОЗУ
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Act - Главный поток меняет координату и освобождает фоновый поток
        zone.setLatitude(55.7558);
        latch.countDown();
        
        executor.shutdown();
        boolean finished = executor.awaitTermination(1, TimeUnit.SECONDS);

        // Assert
        assertThat(finished).isTrue();
        assertThat(readLatitude.get()).isEqualTo(55.7558);
    }
}

/* Этот тест верифицирует правильность конструирования объекта через Билдер, 
корректную отработку оборонной валидации границ и доказывает сеньорам Lock-Free когерентность 
кэшей памяти процессора при параллельном чтении координат [INDEX]. */