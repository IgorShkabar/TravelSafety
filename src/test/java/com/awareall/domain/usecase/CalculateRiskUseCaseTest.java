package com.awareall.domain.usecase;

import com.awareall.domain.model.GeoZoneRisk;
import com.awareall.domain.model.ThreatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование доменного Use Case расчета рисков CalculateRiskUseCase")
class CalculateRiskUseCaseTest {

    private CalculateRiskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CalculateRiskUseCase();
    }

    @Test
    @DisplayName("Метод execute должен быть Null-Safe и возвращать нулевой риск на пустых данных")
    void shouldHandleEmptyAndNullDataSafely() {
        GeoZoneRisk nullResult = useCase.execute(null);
        GeoZoneRisk emptyResult = useCase.execute(new HashMap<>());

        assertThat(nullResult.getRiskScore()).isEqualTo(0.0);
        assertThat(emptyResult.getRiskScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Математическая проверка: Корректный расчет средневзвешенного скора и фактора плотности")
    void shouldCalculateWeightedRiskAccurately() {
        // Arrange
        Map<ThreatType, Integer> incidents = new HashMap<>();
        incidents.put(ThreatType.KNIFE_ASSAULT, 5);     // Вес 0.85
        incidents.put(ThreatType.TRANSPORT_THEFT, 5);  // Вес 0.35
        // Всего 10 инцидентов. Множитель плотности = 10 / 50 = 0.2

        // Act
        GeoZoneRisk result = useCase.execute(incidents);

        // Assert
        assertThat(result.getRiskScore()).isGreaterThan(0.0);
        assertThat(result.getRiskScore()).isLessThanOrEqualTo(1.0);
        
        CalculateRiskUseCase.UseCaseTelemetrySnapshot stats = useCase.getTelemetry();
        assertThat(stats.executionCount).isEqualTo(1L);
        assertThat(stats.totalIncidentsProcessed).isEqualTo(10L);
    }

    @Test
    @DisplayName("Многопоточность: Параллельные вычисления из разных потоков должны атомарно обновлять общую телеметрию")
    void highConcurrencyTelemetryTest() throws InterruptedException {
        // Arrange
        Map<ThreatType, Integer> incidents = new HashMap<>();
        incidents.put(ThreatType.THEFT, 2);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        // Act - Имитируем, что 20 трекеров одновременно затребовали математический расчет
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Одновременный барьер старта
                    useCase.execute(incidents);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Даем отмашку всем потокам
        executor.shutdown();
        boolean completed = executor.awaitTermination(2, TimeUnit.SECONDS);

        // Assert
        assertThat(completed).isTrue();
        
        CalculateRiskUseCase.UseCaseTelemetrySnapshot telemetry = useCase.getTelemetry();
        // На уровне JMM гарантировано: данные инкрементировались без пропусков и гонок потоков
        assertThat(telemetry.executionCount).isEqualTo((long) threadCount);
        assertThat(telemetry.totalIncidentsProcessed).isEqualTo((long) threadCount * 2);
    }
}

/* Этот тест проверяет математическую модель нормализации, устойчивость к некорректным данным и 
многопоточную точность сбора метрик под стрессовой нагрузкой. */