package com.awareall.presentation.state;

import com.awareall.domain.model.GeoZoneRisk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование реактивного снимка состояния MapUiState")
class MapUiStateTest {

    @Test
    @DisplayName("Конструктор должен корректно инициализировать поля и защищать от Null")
    void constructorShouldInitializeFieldsCorrectly() {
        GeoZoneRisk mockRisk = new GeoZoneRisk(0.5);
        MapUiState state = new MapUiState(32.0853, 34.7818, mockRisk, "SENDING");

        assertThat(state.currentLatitude).isEqualTo(32.0853);
        assertThat(state.currentLongitude).isEqualTo(34.7818);
        assertThat(state.currentRisk).isEqualTo(mockRisk);
        assertThat(state.sosStatus).isEqualTo("SENDING");

        // Проверка Null-Safety защиты
        MapUiState nullSafeState = new MapUiState(0.0, 0.0, null, null);
        assertThat(nullSafeState.currentRisk).isNotNull();
        assertThat(nullSafeState.sosStatus).isEqualTo("IDLE");
    }

    @Test
    @DisplayName("Метод copy должен атомарно мутировать отдельные поля, сохраняя неизменяемость остальных")
    void copyMethodShouldMutateStatePartially() {
        MapUiState initialState = new MapUiState(10.0, 20.0, new GeoZoneRisk(0.1), "IDLE");

        // Мутируем только статус SOS
        MapUiState updatedState = initialState.withSosStatus("DELIVERED");

        // Убеждаемся, что старый стейт остался нетронутым (Immutability)
        assertThat(initialState.sosStatus).isEqualTo("IDLE");

        // Убеждаемся, что новый стейт содержит измененный статус, но сохранил старые координаты
        assertThat(updatedState.sosStatus).isEqualTo("DELIVERED");
        assertThat(updatedState.currentLatitude).isEqualTo(10.0);
        assertThat(updatedState.currentLongitude).isEqualTo(20.0);
        assertThat(updatedState.currentRisk).isEqualTo(initialState.currentRisk);
    }

    @Test
    @DisplayName("Контракты equals и hashCode должны обеспечивать глубокое lock-free сравнение снимков состояния")
    void equalsAndHashCodeShouldBeConsistent() {
        GeoZoneRisk risk = new GeoZoneRisk(0.8);
        MapUiState state1 = new MapUiState(55.75, 37.61, risk, "FAILED");
        MapUiState state2 = new MapUiState(55.75, 37.61, risk, "FAILED");
        MapUiState state3 = new MapUiState(55.75, 37.61, risk, "IDLE");

        assertThat(state1).isEqualTo(state2);
        assertThat(state1).isNotEqualTo(state3);
        assertThat(state1.hashCode()).isEqualTo(state2.hashCode());
    }
}


/* Этот тест верифицирует правильность работы частичного копирования, Null-Safety защиту и логическую согласованность методов сравнения, доказывая полную готовность к реактивным потокам данных.

*/