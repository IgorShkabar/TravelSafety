package com.awareall.core.location;

import com.awareall.core.bus.SosEventBus;
import com.awareall.core.bus.SosObserver;
import com.awareall.data.db.DangerZoneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование Lock-Free менеджера локаций и интеграции с картой рисков")
class LocationManagerSingletonTest 
{
    private LocationManagerSingleton manager;

    @BeforeEach
    void setUp() 
    {
        manager = LocationManagerSingleton.getInstance();
        manager.resetForTesting(); // Гарантирует, что каждый тест стартует с абсолютно нулевыми счетчиками!
    }


    @Test
    @DisplayName("При входе в опасную зону менеджер должен автоматически публиковать событие SOS в шину с корректным уровнем риска")
    void shouldPublishSosEventWhenUserEntersDangerZone() {
        // Arrange
        AtomicBoolean isSosReceived = new AtomicBoolean(false);

        // Подписываем тестового наблюдателя на шину событий
        SosObserver testObserver = event -> {
            // Исправлено: новый менеджер отправляет динамический тип "ZONE_CRITICAL" для дефолтной зоны Тель-Авива
            if ("ZONE_CRITICAL".equals(event.threatType)) {
                isSosReceived.set(true);
            }
        };
        SosEventBus.getInstance().register(testObserver);

        try {
            // Act - Передаем координаты центра дефолтной зоны (Тель-Авив: 32.0853, 34.7818)
            manager.updateLocation(32.0853, 34.7818);

            // Assert
            assertThat(isSosReceived.get()).isTrue();
        } finally {
            // Clean up - обязательно отписываемся, чтобы не аффектить другие тесты
            SosEventBus.getInstance().unregister(testObserver);
        }
    }

    @Test
    @DisplayName("setHighAccuracyMode должен корректно переключать интервалы опроса в ручном режиме")
    void shouldChangeIntervalOnAccuracyModeToggle() {
        manager.setHighAccuracyMode(true);
        assertThat(manager.getUpdateIntervalSeconds()).isEqualTo(1);
        assertThat(manager.isHighAccuracyMode()).isTrue();

        manager.setHighAccuracyMode(false);
        assertThat(manager.getUpdateIntervalSeconds()).isEqualTo(60);
        assertThat(manager.isHighAccuracyMode()).isFalse();
    }

    @Test
    @DisplayName("Проверка Lock-Free многопоточности: дебаунсинг алертов и адаптивное управление точностью GPS под нагрузкой")
    void testGeoZoneRiskIntegrationAndDebouncing() throws InterruptedException {
        // Arrange
        List<DangerZoneEntity> dbZones = new ArrayList<>();
        DangerZoneEntity zone = new DangerZoneEntity();
        zone.setId(777);
        zone.setLatitude(55.7558);
        zone.setLongitude(37.6173);
        zone.setRadiusMeters(500);
        zone.setIncidentJsonData("{\"risk_score\":0.95}"); // По вашей модели GeoZoneRisk это уровень CRITICAL
        dbZones.add(zone);
        
        manager.updateZonesFromDatabase(dbZones);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        // Act - Посылаем конкурентные апдейты координат из разных потоков одновременно
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // Синхронный старт барьера
                    manager.updateLocation(55.7558, 37.6173);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Даем отмашку всем потокам ринуться вперед
        executor.shutdown();
        boolean finishedCleanly = executor.awaitTermination(2, TimeUnit.SECONDS);

        // Assert
        assertThat(finishedCleanly).isTrue();

        LocationManagerSingleton.TelemetrySnapshot snapshot = manager.getTelemetry();

        // Проверяем фичу ДЕБАУНСИНГА: несмотря на 5 одновременных запросов, алерт в шину ушел строго 1 раз
        assertThat(snapshot.totalAlerts).isEqualTo(1);
        
        // Проверяем фичу АДАПТИВНОГО ТРЕКИНГА: так как зона критическая (score=0.95), GPS ушел в максимальную точность
        assertThat(manager.isHighAccuracyMode()).isTrue();
        assertThat(manager.getUpdateIntervalSeconds()).isEqualTo(1);
    }
}

/* 
Нам нужно протестировать критически важную вещь: 
когда фоновый поток GPS отправляет координаты, которые входят в радиус опасной зоны, 
синглтон обязан автоматически сгенерировать SosEvent и выбросить его в шину. 

Чтобы этот тест скомпилировался, убедитесь, что ваши классы GeofenceEntity и DangerZoneEntity 
имеют геттеры вроде getCenterLon(), getCenterLat() или getLatitude() (в коде я скорректировал 
вызовы под ваши строки).

тест проверяет бизнес-логику (функциональное поведение приложения при входе в дефолтную зону). 
тест проверяет инженерную устойчивость (многопоточность, алгоритм дебаунсинга и адаптивное переключение 
точности под нагрузкой). тестовый класс, использующий синтаксис AssertJ (assertThat) и 
аннотации @DisplayName, которые так любят на код-ревью.

Поймать сначала Race Condition, а затем Test Pollution на примере синглтона — это бесценный опыт, 
который очень часто гоняют на собеседованиях.
*/
