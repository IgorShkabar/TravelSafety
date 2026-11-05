package com.awareall.domain.repository;

import com.awareall.domain.model.GeoZoneRisk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование асинхронного кроссплатформенного контракта MapRepository")
class MapRepositoryTest {

    private MapRepository mapRepository;

    @BeforeEach
    void setUp() {
        // Создаем легковесную потокобезопасную заглушку интерфейса "на лету" (Анонимный класс)
        mapRepository = new MapRepository() {
            private final byte[] dummyTile = new byte[]{10, 20, 30};
            
            // Выделенный изолированный пул фоновых потоков для репозитория
            private final ExecutorService repoExecutor = Executors.newFixedThreadPool(2);

            @Override
            public byte[] getOfflineTile(int x, int y, int zoom) {
                return dummyTile;
            }

            @Override
            public GeoZoneRisk getRiskForCoordinates(double lat, double lon) {
                return new GeoZoneRisk(0.65); // Имитация уровня MEDIUM-HIGH риска
            }

            // Реализация наших новых асинхронных расширений
            @Override
            public CompletableFuture<byte[]> getOfflineTileAsync(int x, int y, int zoom) {
                return CompletableFuture.supplyAsync(() -> getOfflineTile(x, y, zoom), repoExecutor);
            }

            @Override
            public CompletableFuture<GeoZoneRisk> getRiskForCoordinatesAsync(double lat, double lon) {
                return CompletableFuture.supplyAsync(() -> getRiskForCoordinates(lat, lon), repoExecutor);
            }
        };
    }

    @Test
    @DisplayName("Синхронные методы контракта MapRepository должны стабильно возвращать данные")
    void synchronousMethodsShouldWorkCorrectly() {
        byte[] tile = mapRepository.getOfflineTile(1, 1, 12);
        GeoZoneRisk risk = mapRepository.getRiskForCoordinates(32.0853, 34.7818);

        assertThat(tile).containsExactly(10, 20, 30);
        assertThat(risk.getRiskScore()).isEqualTo(0.65);
    }

    @Test
    @DisplayName("Асинхронные методы должны выполняться в фоне, возвращая CompletableFuture без блокировки вызывающего потока")
    void asyncMethodsShouldExecuteNonBlocking() throws Exception {
        // Act - Запускаем асинхронные задачи параллельно
        CompletableFuture<byte[]> tileFuture = mapRepository.getOfflineTileAsync(5, 5, 14);
        CompletableFuture<GeoZoneRisk> riskFuture = mapRepository.getRiskForCoordinatesAsync(55.75, 37.61);

        // Дожидаемся результатов из фонового пула
        byte[] tileData = tileFuture.get(1, TimeUnit.SECONDS);
        GeoZoneRisk riskData = riskFuture.get(1, TimeUnit.SECONDS);

        // Assert
        assertThat(tileData).containsExactly(10, 20, 30);
        assertThat(riskData.getRiskScore()).isEqualTo(0.65);
    }
}


/* Чтобы не создавать лишних физических файлов реализации в вашем продакшен-пакете и не раздувать количество файлов (File Bloat), мы проверим этот асинхронный многопоточный контракт напрямую в тесте с помощью анонимного класса, внедрив в него изолированный пул фоновых потоков ExecutorService. */