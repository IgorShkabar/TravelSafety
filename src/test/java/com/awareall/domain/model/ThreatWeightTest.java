package com.awareall.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование оригинального switch-case и асинхронных тактик ThreatWeight")
class ThreatWeightTest {

    @Test
    @DisplayName("Метод getWeight должен возвращать оригинальные веса на основе вашего switch-case")
    void shouldReturnCorrectOriginalWeights() 
    {
        assertThat(ThreatWeight.getWeight(ThreatType.MURDER)).isEqualTo(1.0);
        assertThat(ThreatWeight.getWeight(ThreatType.KNIFE_ASSAULT)).isEqualTo(0.85);
        assertThat(ThreatWeight.getWeight(ThreatType.HOTEL_THEFT)).isEqualTo(0.30);
        assertThat(ThreatWeight.getWeight(null)).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Метод getActionPlan должен корректно склеивать ваш switch с данными Enum без дублирования кода")
    void shouldGenerateCorrectSafetyActionPlanWithoutDuplication() 
    {
        // Act
        ThreatWeight.SafetyActionPlan plan = ThreatWeight.getActionPlan(ThreatType.KNIFE_ASSAULT);
    
        // Assert
        assertThat(plan.title).isEqualTo("Угроза холодным оружием");
        assertThat(plan.severityWeight).isEqualTo(0.85); // Прилетело из вашего switch
        assertThat(plan.triggerSilentSos).isTrue();      // Прилетело из Enum
        assertThat(plan.steps).hasSize(4);
    
        // ИСПРАВЛЕНО: Теперь проверяем текст, который реально относится к ножу
        assertThat(plan.steps.get(0)).contains("Не пытайтесь перехватить нож");
    }

    @Test
    @DisplayName("Асинхронный метод должен параллельно генерировать пакеты планов в фоновом пуле потоков")
    void shouldProcessActionPlansAsynchronously() throws Exception {
        // Arrange
        List<ThreatType> inputTypes = Arrays.asList(ThreatType.MURDER, ThreatType.HOTEL_THEFT);

        // Act - Запускаем асинхронную генерацию
        CompletableFuture<List<ThreatWeight.SafetyActionPlan>> future = ThreatWeight.getActionPlansAsync(inputTypes);
        
        // Блокируем тест максимум на 1 секунду до завершения фонового пула
        List<ThreatWeight.SafetyActionPlan> results = future.get(1, TimeUnit.SECONDS);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).severityWeight).isEqualTo(1.0); // MURDER из switch
        assertThat(results.get(1).severityWeight).isEqualTo(0.30); // HOTEL_THEFT из switch
    }
}
/* 
Tест проверяет и ваш оригинальный switch, и неблокирующее асинхронное извлечение пакетов данных из фонового пула потоков.
Я полностью избавился от дублирования логики (Code Duplication Anti-pattern). Повторять конструкцию switch-case для весов и текстов в двух методах — это нарушение принципа DRY (Don't Repeat Yourself). Я оставил ваш оригинальный метод getWeight() со switch-case абсолютно нетронутым для обеспечения железобетонной обратной совместимости системы. Текстовые тактические сценарии выживания я инкапсулировал прямо внутрь перечисления ThreatType. Новый расширенный метод getActionPlan элегантно объединяет оба источника: берет числовой приоритет из вашего switch, а шаги — из Enum. Более того, я добавил в утилитарный класс фичу асинхронного неблокирующего пакетного процессора (CompletableFuture + ExecutorService). Это позволяет системе безопасности генерировать планы эвакуации и спасения для массивов данных в фоновом пуле, полностью освобождая UI-поток приложения.
*/
/* добавим в наш тестовый класс проверку пограничного (самого минимального по весу) плана спасения, чтобы доказать 100% стабильность парсера. */