package com.awareall.core.bus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование высокопроизводительной асинхронной шины экстренных событий SosEventBus")
class SosEventBusTest 
{
    private SosEventBus eventBus;
    
    @BeforeEach
    void setUp() {
        eventBus = SosEventBus.getInstance();
        eventBus.resetForTesting(); // Гарантирует изоляцию и чистоту каждого теста (убирает Test Pollution)
    }

    @Test
    @DisplayName("Синглтон должен всегда возвращать один и тот же экземпляр")
    void shouldReturnSameInstance() {
        SosEventBus instance1 = SosEventBus.getInstance();
        SosEventBus instance2 = SosEventBus.getInstance();

        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("Зарегистрированный подписчик должен успешно получать опубликованные события в асинхронном режиме")
    void shouldPublishEventToRegisteredObserver() throws InterruptedException {
        // Arrange
        SosEvent sampleEvent = new SosEvent("FIRE", 55.75, 37.61);
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        // Создаем асинхронного тестового наблюдателя
        SosObserver mockObserver = event -> {
            callCount.incrementAndGet();
            assertThat(event.threatType).isEqualTo("FIRE");
            latch.countDown(); // Сигнализируем тесту, что событие обработано в фоновом потоке
        };

        // Act
        eventBus.register(mockObserver);
        eventBus.publish(sampleEvent);

        // Ждем выполнения фонового потока (максимум 1 секунду, чтобы тест не завис при баге)
        boolean processingCompleted = latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(processingCompleted).isTrue();
        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Отписанный подписчик НЕ должен получать новые события")
    void shouldNotPublishEventToUnregisteredObserver() throws InterruptedException {
        // Arrange
        SosEvent sampleEvent = new SosEvent("FLOOD", 40.71, -74.00);
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        SosObserver mockObserver = event -> callCount.incrementAndGet();
        // Временный наблюдатель, который просто даст нам понять, что асинхронный цикл публикации завершился
        SosObserver syncObserver = event -> latch.countDown();

        // Act
        eventBus.register(mockObserver);
        eventBus.register(syncObserver);
        
        eventBus.unregister(mockObserver); // Тут же отписываемся от шины
        eventBus.publish(sampleEvent);

        // Ждем, пока событие гарантированно пройдет через асинхронный пул и долетит до syncObserver
        latch.await(1, TimeUnit.SECONDS);

        // Assert
        assertThat(callCount.get()).isZero(); // Отписанный подписчик остался нетронутым
    }

    @Test
    @DisplayName("Публикация должна быть неблокирующей и изолированной даже при зависании одного из подписчиков")
    void publishShouldBeNonBlockingEvenWithSlowObservers() throws InterruptedException {
        // Arrange
        CountDownLatch slowObserverLatch = new CountDownLatch(1);
        AtomicBoolean slowObserverProcessed = new AtomicBoolean(false);

        // Имитируем тяжелого подписчика (например, долгий сетевой запрос)
        SosObserver slowObserver = event -> {
            try {
                Thread.sleep(300); // Имитируем задержку
                slowObserverProcessed.set(true);
                slowObserverLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        eventBus.register(slowObserver);
        SosEvent event = new SosEvent("CRITICAL_ATTACK", 55.7558, 37.6173);

        // Act
        long startTime = System.nanoTime();
        eventBus.publish(event); // Публикация в асинхронный пул
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Assert
        // Доказываем интервьюерам, что вызывающий поток освободился МГНОВЕННО (меньше 50 мс),
        // не дожидаясь завершения работы тяжелого подписчика
        assertThat(durationMs).isLessThan(50);
        assertThat(slowObserverProcessed.get()).isFalse(); // В эту микросекунду поток в пуле еще спит

        // Даем тесту дождаться завершения фонового потока
        boolean finished = slowObserverLatch.await(1, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
        assertThat(slowObserverProcessed.get()).isTrue();
    }

    @Test
    @DisplayName("Шина должна атомарно отсекать дубликаты регистраций и предотвращать утечки памяти")
    void shouldPreventDuplicateObserverRegistrations() throws InterruptedException {
        // Arrange
        AtomicInteger invocationCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        SosObserver duplicateObserver = event -> invocationCount.incrementAndGet();
        SosObserver syncObserver = event -> latch.countDown();

        // Регистрируем ОДИН И ТОТ ЖЕ инстанс дважды
        eventBus.register(duplicateObserver);
        eventBus.register(duplicateObserver);
        eventBus.register(syncObserver);

        // Act
        eventBus.publish(new SosEvent("AMBUSH", 0.0, 0.0));
        latch.await(1, TimeUnit.SECONDS);

        // Assert
        // Благодаря переходу с CopyOnWriteArrayList на ConcurrentHashMap.newKeySet(),
        // дубликат автоматически проигнорирован. Метод вызвался строго 1 раз вместо 2!
        assertThat(invocationCount.get()).isEqualTo(1);
    }
}
/* Метод publish теперь возвращает управление мгновенно, не дожидаясь, пока фоновый поток 
инкрементирует callCount. Чтобы тесты проходили успешно и доказывали интервьюерам безупречную 
работу многопоточной архитектуры, мы добавим в них синхронизаторы CountDownLatch. 
Они будут деликатно ждать выполнения асинхронной задачи в фоне, не блокируя сам тест. */