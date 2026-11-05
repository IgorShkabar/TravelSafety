package com.awareall.presentation.viewmodel;

import com.awareall.core.bus.SosEvent;
import com.awareall.core.bus.SosEventBus;
import com.awareall.domain.SosProtocolStrategy;
import com.awareall.domain.model.GeoZoneRisk;
import com.awareall.domain.repository.MapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SafetyViewModelTest {

    private SafetyViewModel viewModel;

    // 1. Создаем правильные заглушки для зависимостей конструктора
    // заглушку репозитория с реализацией ОБОИХ методов
    private final MapRepository mockRepository = new MapRepository() {
        @Override
        public GeoZoneRisk getRiskForCoordinates(double lat, double lon) {
            return new GeoZoneRisk(4.5); // Возвращаем тестовый риск
        }

        @Override
        public byte[] getOfflineTile(int x, int y, int zoom) {
            return new byte[0]; // Заглушка для компиляции: возвращаем пустой массив
        }
    };


    // Заглушка для сотовой связи
    private final SosProtocolStrategy mockCellularStrategy = new SosProtocolStrategy() {
        @Override
        public void sendSos(double lat, double lon, String threatType) {
            // Имитация успешной отправки
        }
    };

    // Заглушка для спутниковой связи
    private final SosProtocolStrategy mockSatelliteStrategy = new SosProtocolStrategy() {
        @Override
        public void sendSos(double lat, double lon, String threatType) {
            // Имитация успешной отправки
        }
    };

    @BeforeEach
    void setUp() {
        // 2. Теперь передаем ВСЕ ТРИ созданные переменные в конструктор! Ошибки исчезнут.
        viewModel = new SafetyViewModel(mockRepository, mockCellularStrategy, mockSatelliteStrategy);
    }

    @Test
    @DisplayName("При срабатывании SOS-геозоны координаты должны успешно сохраниться в кэш")
    void shouldCacheCoordinatesWhenSosGeoZoneTriggered() {
        // Arrange
        double expectedLat = 45.1234;
        double expectedLon = 34.5678;
        
        // Создаем событие входа в опасную зону
        SosEvent zoneEvent = new SosEvent("HIGH_RISK_ZONE", 45.1234, 34.5678);

        // Act
        // Публикуем событие в шину. Наша ViewModel его ловит и кэширует точку
        SosEventBus.getInstance().publish(zoneEvent);

        // Assert
        // Проверяем работу геттеров кэша
        assertThat(viewModel.getCachedLat()).isEqualTo(expectedLat);
        assertThat(viewModel.getCachedLon()).isEqualTo(expectedLon);

        // Clean up
        viewModel.onCleared();
    }

    @Test
    @DisplayName("Когда пользователь объявляет безопасность, статус должен вернуться в SAFE")
    void shouldReturnToSafeWhenUserDeclaresSafety() {
        // Arrange
        SosEvent dangerEvent = new SosEvent("HIGH_RISK_ZONE", 10.0, 20.0);
        SosEventBus.getInstance().publish(dangerEvent); // Входим в зону опасности

        // Act
        viewModel.onUserDeclaresSafety();

        // Assert
        assertThat(viewModel.getRiskStatus().getValue()).isEqualTo("SAFE");
        
        // Clean up
        viewModel.onCleared();
    }
}
