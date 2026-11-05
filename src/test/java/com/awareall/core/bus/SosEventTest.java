package com.awareall.core.bus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SosEventTest 
{

    @Test
    @DisplayName("Конструктор с 3 параметрами должен корректно инициализировать поля и задавать дефолтную зону")
    void shouldInitializeWithThreeParameters() 
    {
        // Arrange
        String expectedThreat = "FIRE";
        double expectedLat = 55.7558;
        double expectedLon = 37.6173;

        // Act
        SosEvent event = new SosEvent(expectedThreat, expectedLat, expectedLon);

        // Assert
        assertThat(event.zoneType).isEqualTo("HIGH_RISK_ZONE");
        assertThat(event.threatType).isEqualTo(expectedThreat);
        assertThat(event.latitude).isEqualTo(expectedLat);
        assertThat(event.longitude).isEqualTo(expectedLon);
    }

    @Test
    @DisplayName("Конструктор с 4 параметрами должен корректно сохранять все переданные значения")
    void shouldInitializeWithFourParameters() 
    {
        // Arrange
        String expectedZone = "SAFE_ZONE";
        String expectedThreat = "NONE";
        double expectedLat = 40.7128;
        double expectedLon = -74.0060;

        // Act
        SosEvent event = new SosEvent(expectedZone, expectedThreat, expectedLat, expectedLon);

        // Assert
        assertThat(event.zoneType).isEqualTo(expectedZone);
        assertThat(event.threatType).isEqualTo(expectedThreat);
        assertThat(event.latitude).isEqualTo(expectedLat);
        assertThat(event.longitude).isEqualTo(expectedLon);
    }
}
/* В современных версиях Java (начиная с JUnit 5) тестовые классы и тестовые методы 
не обязаны быть public. Модификатор по умолчанию (package-private) — это официальная рекомендация 
и стандарт хорошего тона */