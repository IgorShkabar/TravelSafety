package com.awareall.presentation.viewmodel;

/* В Android ViewModel и LiveData — это компоненты архитектуры, 
которые помогают управлять состоянием UI и обеспечивают реактивное 
обновление интерфейса при изменении данных.

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;    Code for Android OS  */
//связать эту SafetyViewModel с LocationManagerSingleton, чтобы проверить, как статус автоматически меняется при обновлении координат
//написать файл Main.java, где создадим экземпляры репозиториев, стратегий (Cellular/Satellite) и запустим комплексный тест системы безопасности
//подготовить Main.java, чтобы сымитировать одновременную работу геолокации, смену состояний UI и проверку таймеров безопасности в консоли.

//объединить всё в Main.java, чтобы запустить симуляцию перемещений, входящих SOS-событий и автоматического переключения точности GPS

/* Теперь в SafetyViewModel.java замени старые Android-импорты на собственные новые классы: */

import com.awareall.core.bus.SosEvent;
import com.awareall.core.bus.SosEventBus;
import com.awareall.core.bus.SosObserver;
import com.awareall.core.location.LocationManagerSingleton;
import com.awareall.domain.SosProtocolStrategy;
import com.awareall.domain.model.GeoZoneRisk;
import com.awareall.domain.repository.MapRepository;
import com.awareall.presentation.state.MapUiState;

public class SafetyViewModel extends ViewModel implements SosObserver 
{
    private final MapRepository mapRepository;
    private final SosProtocolStrategy cellularStrategy;
    private final SosProtocolStrategy satelliteStrategy;

    // Для работы таймера и кэша понадобятся новые поля в классе:
    private boolean isAlreadyInDangerZone = false; // Поле класса ViewModel для защиты от спама
    private java.util.Timer deadMansSwitchTimer; // Стандартный Java-таймер для обратного отсчета
    private double cachedLat = 0.0;
    private double cachedLon = 0.0;

    /*** This applies to Android; it is not present in a pure cross-platform architecture: 
    private final MutableLiveData<MapUiState> _uiState = new MutableLiveData<>();   */
    
    // public final LiveData<MapUiState> uiState = _uiState; <!-- This is the public-facing LiveData that UI components can observe -->
    private final LiveData<MapUiState> _uiState = new LiveData<>();
    
    // Вместо MutableLiveData и LiveData использу один универсальный класс
    private final LiveData<String> riskStatus = new LiveData<>("SAFE");
    
    /* Добавь public геттер для UI (для расширяемости) 
    Чтобы архитектура MVVM была правильной, слою интерфейса (UI) нельзя давать прямую возможность 
    изменять состояние через _uiState.setValue(). UI должен только читать данные. Поэтому добавь pubic метод чуть ниже поля: */
    public LiveData<MapUiState>  getUiState() {     return _uiState;    }

    public SafetyViewModel(MapRepository mapRepository, 
                           SosProtocolStrategy cellular, 
                           SosProtocolStrategy satellite) 
    {
        this.mapRepository = mapRepository;
        this.cellularStrategy = cellular;
        this.satelliteStrategy = satellite;
        
        // Регистрация во внутренней шине событий (Observer)
        SosEventBus.getInstance().register(this);
        
        // Начальное состояние
        _uiState.setValue(new MapUiState(0.0, 0.0, new GeoZoneRisk(0.0), "IDLE"));
    }

    public void onLocationChanged(double lat, double lon) 
    {
        GeoZoneRisk risk = mapRepository.getRiskForCoordinates(lat, lon);
        MapUiState current = _uiState.getValue();
        String currentSos = current != null ? current.sosStatus : "IDLE";
        
        /* В Android-компоненте LiveData метод postValue() используется для безопасного обновления
         данных из фонового потока (например, когда координаты пришли от GPS в фоновом режиме), 
         перенаправляя выполнение в главный UI-поток.
        _uiState.postValue(new MapUiState(lat, lon, risk, currentSos));     */

        /* Чтобы сделать реализацию кроссплатформенной, переносимой (не привязанной к Android 
        Looper), реализую postValue() с помощью встроенного в Java механизма синхронизации потоков.
        Обнови класс LiveData.java Добавь метод postValue(T newValue) внутрь класса LiveData<T>. 
        Для безопасности при многопоточности сделай обертку synchronize   
        Как это работает в SafetyViewModelПосле добавления postValue метода в LiveData.java 
        ошибка в методе onLocationChanged исчезнет: */

        // Теперь этот метод определен, безопасен для потоков и полностью автономен!
        _uiState.postValue(new MapUiState(lat, lon, risk, currentSos));
    }

    // Мгновенный триггер SOS из интерфейса
    public void triggerManualSos(String threatTypeName, boolean hasNetwork) 
    {
        MapUiState state = _uiState.getValue();
    
    // Эффективное использование полей: если UI-state пустой, берем координаты из кэша
    double lat = (state != null) ? state.currentLatitude : this.cachedLat;
    double lon = (state != null) ? state.currentLongitude : this.cachedLon;
    GeoZoneRisk risk = (state != null) ? state.currentRisk : new GeoZoneRisk(0.0);

    _uiState.setValue(new MapUiState(lat, lon, risk, "SENDING"));

    // Использование паттерна Strategy в зависимости от сети
    SosProtocolStrategy selectedStrategy = hasNetwork ? cellularStrategy : satelliteStrategy;
    
    try 
    {
        // Передаем гарантированно существующие координаты
        selectedStrategy.sendSos(lat, lon, threatTypeName);
        _uiState.setValue(new MapUiState(lat, lon, risk, "DELIVERED"));
    } catch (Exception e) {
        _uiState.setValue(new MapUiState(lat, lon, risk, "FAILED"));
    }
        // MapUiState state = _uiState.getValue();
        if (state == null) return;

        _uiState.setValue(new MapUiState(state.currentLatitude, state.currentLongitude, state.currentRisk, "SENDING"));
       
        try 
        {
            selectedStrategy.sendSos(state.currentLatitude, state.currentLongitude, threatTypeName);
            _uiState.setValue(new MapUiState(state.currentLatitude, state.currentLongitude, state.currentRisk, "DELIVERED"));
        } catch (Exception e) {
            _uiState.setValue(new MapUiState(state.currentLatitude, state.currentLongitude, state.currentRisk, "FAILED"));
        }
    }

    // Имя метода теперь строго совпадает с интерфейсом! Ошибка исчезнет
    @Override
    public void onSosTriggered(SosEvent event) 
    {
        // 1. Логируй критическое событие для отладки
        System.out.println("[SafetyViewModel] Получен сигнал тревоги! Тип: " + event.threatType 
                       + " Координаты: " + event.latitude + ", " + event.longitude);

        // Реакция на автоматическое срабатывание геозоны от LocationManager
        if ("HIGH_RISK_ZONE".equals(event.threatType)) 
        {
            // Базовые действия 2. Защита от повторного срабатывания (если уже обработали эту зону)
            if (isAlreadyInDangerZone)  {   return;  } // Игнорируем повторные события

            isAlreadyInDangerZone = true; // Флаг, что мы уже в опасной зоне             

            // Здесь можно вызвать стратегию SOS автоматически
            triggerManualSos(event.threatType, true); // Положим, есть сеть для отправки

            riskStatus.setValue("DANGER AT: " + event.latitude + ", " + event.longitude);

            // Действие при входе в критическую зону (например, пред-активация SOS)
            /* Эти 3 компонента превращают Application из информатора в реальный инструмент выживания. */
        
            // --- ДОПОЛНИТЕЛЬНЫЕ ДЕЙСТВИЯ ПРИ ВХОДЕ В КРИТИЧЕСКУЮ ЗОНУ ---
            // а) Повышаем точность GPS
            LocationManagerSingleton.getInstance().setHighAccuracyMode(true);

            // б) Запускаем таймер «Dead Man's Switch» на 5 минут (300 секунд)
            startDeadMansSwitchTimer(300); 

            // в) Кэшируем точку в память / на диск
            saveLastKnownDangerZone(event.latitude, event.longitude);
        }
    }
    
    // Не забудь сбросить флаг, когда пользователь покинет зону
    public void onUserExitedDangerZone() 
    {
        this.isAlreadyInDangerZone = false;
        this.riskStatus.setValue("SAFE");
    }
    

    // Реализу внутренние функции-рутины
    // Функция Б: Локальный таймер «Убийцы» (Dead Man's Switch)
    // Использу стандартный java.util.Timer. Если пользователь за 5 минут не сбросит таймер, 
    // сработает автоматическая отправка экстренного сообщения родным.

    private void startDeadMansSwitchTimer(int seconds) 
    {
        // На всякий случай отменяем старый таймер, если он почему-то работал
        if (deadMansSwitchTimer != null) 
        {
            deadMansSwitchTimer.cancel();
        }

        deadMansSwitchTimer = new java.util.Timer();
        deadMansSwitchTimer.schedule(new java.util.TimerTask() 
        {
        @Override
        public void run() 
        {
            System.out.println("[CRITICAL] Время вышло! Пользователь не подтвердил безопасность.");
            // Отправляем жесткий SOS-сигнал родным / на сервер по резервному каналу (например, SMS)
            triggerManualSos("AUTOMATIC_DEAD_MAN_TRIGGER", false); 
        }
        }, seconds * 1000L); // Переводим секунды в миллисекунды

        System.out.println("[SafetyViewModel] Таймер Dead Man's Switch запущен на " + seconds + " секунд.");
    }

    // Функция В: Кэширование последней точки
    // В реальном приложении можно использовать SharedPreferences или Room DB для сохранения
    // Для прототипа пишy координаты в поля класса. 
    // Но на практике (чтобы защитить данные при выключении телефона) их стоит сохранять в 
    // java.util.prefs.Preferences или текстовый файл-кэш.

    private void saveLastKnownDangerZone(double latitude, double longitude) 
    {
        this.cachedLat = latitude;
        this.cachedLon = longitude;
    
        // Эмуляция записи на постоянный диск устройства
        System.out.println("[SafetyViewModel] Кэширование последней опасной точки: [Cache] Координаты " 
                + latitude + ", " + longitude + " успешно сохранены в энергонезависимый кэш приложения.");
    }

    // Кнопка «Я в безопасности» (Обязательный элемент!)
    // Раз добавил таймер «Убийцы», пользователю жизненно необходим метод для его отмены, 
    // когда он выйдет из опасной зоны или поймет, что тревога ложная. Добавь этот метод для UI:
    public void onUserDeclaresSafety() 
    {
        System.out.println("[SafetyViewModel] User вручную подтвердил, что он в безопасности.");
    
        // 1. Сбрасываем таймер обратного отсчета
        if (deadMansSwitchTimer != null) 
        {
            deadMansSwitchTimer.cancel();
            deadMansSwitchTimer = null;
            System.out.println("[SafetyViewModel] Таймер Dead Man's Switch успешно остановлен.");
        }

        // 2. Возвращаем GPS-трекер в экономный режим
        LocationManagerSingleton.getInstance().setHighAccuracyMode(false);

        // 3. Сбрасываем флаги и обновляем UI статус
        this.isAlreadyInDangerZone = false;
        this.riskStatus.setValue("SAFE");
    }

    @Override
    public void onCleared() 
    {
        /* super.onCleared(); так как в моем кастомном базовом классе этот метод пока пустой и вызывать его не обязательно */
        SosEventBus.getInstance().unregister(this);

        System.out.println("[SafetyViewModel] ViewModel очищена, подписка отвязана от SosEventBus.");
        SosEventBus.getInstance().unregister(this);
    }  
    /* Гибкость жизненного цикла: теперь любой внешний контроллер экрана (будь то десктопное окно
     на Swing/JavaFX или консольный менеджер) сможет вызвать viewModel.onCleared(), когда экран 
     закроется. Это гарантирует отсутствие утечек памяти, так как объект ViewModel вовремя 
     отпишется от глобального синглтона SosEventBus. */  

    public LiveData<String> getRiskStatus() 
    {
        return riskStatus;
    }    

    /* OBSOLETE code: старый @Override onEvent(SosEvent event). Я перенес всю логику на 
    onSosTriggered(SosEvent event) в соответствии с моим новым интерфейсом SosObserver, 
    метод onEvent стал лишним и будет вызывать ошибки..  
    @Override
    public void onEvent(SosEvent event) 
    {
        // My setValue() работает точно так же, как в Android!
        riskStatus.setValue("DANGER AT: " + event.latitude + ", " + event.longitude);
    }       */
}       

// реализовать таймер обратного отсчета (Dead Man's Switch) — если пользователь зашел в зону и потерял сознание/связь, система сама отправит сигнал через заданное время. Или настроим метод triggerManualSos, чтобы он имитировал отправку пакетов по сети
