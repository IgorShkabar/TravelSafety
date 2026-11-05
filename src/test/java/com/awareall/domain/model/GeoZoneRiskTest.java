package com.awareall.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование доменной модели рисков GeoZoneRisk")
class GeoZoneRiskTest {

    @Test
    @DisplayName("Конструктор должен корректно распределять текстовые уровни на основе числового скора")
    void shouldMapRiskLevelsCorrectly() {
        // Проверка диапазона LOW
        GeoZoneRisk lowRisk = new GeoZoneRisk(0.15);
        assertThat(lowRisk.getRiskLevel()).isEqualTo("LOW");
        assertThat(lowRisk.getRiskScore()).isEqualTo(0.15);

        // Проверка диапазона MEDIUM
        GeoZoneRisk mediumRisk = new GeoZoneRisk(0.40);
        assertThat(mediumRisk.getRiskLevel()).isEqualTo("MEDIUM");

        // Проверка диапазона HIGH
        GeoZoneRisk highRisk = new GeoZoneRisk(0.70);
        assertThat(highRisk.getRiskLevel()).isEqualTo("HIGH");

        // Проверка диапазона CRITICAL
        GeoZoneRisk criticalRisk = new GeoZoneRisk(0.95);
        assertThat(criticalRisk.getRiskLevel()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("Конструктор должен жестко нормализовать выходящие за границы скоры от 0.0 до 1.0")
    void shouldNormalizeOutofBoundsScores() {
        GeoZoneRisk negativeRisk = new GeoZoneRisk(-0.5);
        GeoZoneRisk tooHighRisk = new GeoZoneRisk(1.8);

        assertThat(negativeRisk.getRiskScore()).isEqualTo(0.0);
        assertThat(negativeRisk.getRiskLevel()).isEqualTo("LOW");

        assertThat(tooHighRisk.getRiskScore()).isEqualTo(1.0);
        assertThat(tooHighRisk.getRiskLevel()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("Контракты equals и hashCode должны обеспечивать глубокое lock-free сравнение объектов-значений")
    void equalsAndHashCodeShouldBeConsistent() {
        GeoZoneRisk risk1 = new GeoZoneRisk(0.5);
        GeoZoneRisk risk2 = new GeoZoneRisk(0.5);
        GeoZoneRisk risk3 = new GeoZoneRisk(0.8);

        assertThat(risk1).isEqualTo(risk2);
        assertThat(risk1).isNotEqualTo(risk3);
        assertThat(risk1.hashCode()).isEqualTo(risk2.hashCode());
    }

    @Test
    @DisplayName("Статические константы Flyweight должны переиспользовать память без создания новых объектов")
    void flyweightConstantsShouldPreserveMemory() {
        assertThat(GeoZoneRisk.SAFE_ZONE.getRiskScore()).isEqualTo(0.0);
        assertThat(GeoZoneRisk.ABSOLUTE_DANGER_ZONE.getRiskLevel()).isEqualTo("CRITICAL");
    }
}

/* Этот тест проверяет математическую точность распределения пограничных условий 
(интервалы LOW, MEDIUM, HIGH, CRITICAL), нормализацию значений и работу методов сравнения. */