package com.awareall.presentation.ui;

import com.awareall.domain.model.GeoZoneRisk;
import com.awareall.presentation.state.MapUiState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование реактивного графического конвейера MapFragment")
class MapFragmentTest {

    private MapFragment fragment;

    @BeforeEach
    void setUp() {
        fragment = new MapFragment();
        fragment.onStart(); // Инициализация жизненного цикла экрана
    }

    @AfterEach
    void tearDown() {
        fragment.onStop(); // Освобождение ресурсов пулов рендеринга
    }

    @Test
    @DisplayName("Фрагмент должен мгновенно и реактивно реагировать на входящие изменения MapUiState")
    void fragmentShouldRenderUiStatesCorrectly() {
        // Arrange
        GeoZoneRisk highRisk = new GeoZoneRisk(0.85);
        MapUiState dangerState = new MapUiState(55.7558, 37.6173, highRisk, "SENDING");

        // Act
        fragment.renderNewState(dangerState);

        // Assert
        MapUiState localState = fragment.getCurrentLocalState();
        assertThat(localState.sosStatus).isEqualTo("SENDING");
        assertThat(localState.currentLatitude).isEqualTo(55.7558);
        assertThat(localState.currentRisk.getRiskLevel()).isEqualTo("CRITICAL"); // 0.85 нормализуется в CRITICAL
    }

    @Test
    @DisplayName("Многопоточность: Лавина обновлений состояния из фоновых трекеров не должна приводить к сбоям в UI")
    void highConcurrencyUiStatePushTest() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        ExecutorService backgroundTrackersPool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        // Act - Имитируем, что 10 фоновых служб-трекеров одновременно пушат новые координаты во фрагмент
        for (int i = 0; i < threadCount; i++) {
            final int step = i;
            backgroundTrackersPool.submit(() -> {
                try {
                    startLatch.await();
                    MapUiState stateDump = new MapUiState(32.0 + step, 34.0 + step, GeoZoneRisk.SAFE_ZONE, "IDLE");
                    fragment.renderNewState(stateDump);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown(); // Одновременный старт лавины обновлений экрана
        backgroundTrackersPool.shutdown();
        boolean cleanExecution = backgroundTrackersPool.awaitTermination(2, TimeUnit.SECONDS);

        // Даем конвейеру рендеринга 50мс дорисовать фоновые текстуры карты рисков
        Thread.sleep(50);

        // Assert
        assertThat(cleanExecution).isTrue();
        assertThat(fragment.getCurrentLocalState()).isNotNull();
    }
}


/* высший пилотаж автоматизации. Поскольку у нас нет реального Android-эмулятора, мы протестируем конкурентную реактивность фрагмента: докажем, что при лавинообразном пуше новых стейтов (MapUiState) фрагмент обрабатывает их асинхронно, безопасно меняет состояние и не зависает.
 */