package com.awareall.presentation.viewmodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

class ViewModelTest {

    @Test
    @DisplayName("Базовый метод onCleared не должен выбрасывать исключений при вызове")
    void onClearedShouldNotThrowExceptions() {
        // Создаем минимальную анонимную реализацию абстрактного класса для теста
        ViewModel concreteViewModel = new ViewModel() {};

        // Проверяем, что вызов пустого метода проходит успешно и безопасно
        assertThatCode(concreteViewModel::onCleared).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Классы-наследники должны иметь возможность успешно переопределять onCleared")
    void childClassesShouldBeAbleToOverrideOnCleared() {
        // Переменная-индикатор для отслеживания вызова
        AtomicBoolean isClearedCalled = new AtomicBoolean(false);

        // Имитируем реальный класс-наследник (как ваша SafetyViewModel)
        ViewModel testViewModel = new ViewModel() {
            @Override
            public void onCleared() {
                isClearedCalled.set(true); // Меняем флаг при очистке ресурсов
            }
        };

        // Act
        testViewModel.onCleared();

        // Assert
        assertThat(isClearedCalled.get()).isTrue();
    }
}
