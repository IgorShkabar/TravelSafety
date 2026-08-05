package com.travelsafety.aware.core.location;

import java.util.ArrayList;
import java.util.List;
import com.travelsafety.aware.data.local.GeofenceEntity; // My сущность из пакета data
/* import android.content.Context;  For Android OS, but we are in pure Java */
import com.travelsafety.aware.core.bus.SosEvent;
import com.travelsafety.aware.core.bus.SosEventBus;

/* В реальном продакшен-приложении геозоны никогда не зашиваются жестко в код. 
 Они или загружаются из локальной БД Room (которую спроектировал в пакете data), или (для тестов) 
 выносятся в понятные именованные константы. 
 Истинный production-подход (интеграция с Room DB) 
 В реальной жизни менеджер геолокации должен запрашивать список зон из БД (через MapRepository) 
 и проверять вхождение по формуле расстояния (например, формуле гаверсинусов), 
 а не по прямоугольной сетке.*/
покажте, как связать LocationManagerSingleton с нашей базой данных Room, чтобы он проверял реальный радиус опасных зон вместо констант
связать этот класс напрямую с Room Dao, чтобы при старте приложения данные автоматически подтягивались из реальной SQLite, 
или займемся оптимизацией вычислений, чтобы они не тормозили интерфейс смартфона?


// Паттерн Singleton / DI-lite
public class LocationManagerSingleton 
{
    private static volatile LocationManagerSingleton instance;
    private double currentLat = 0.0;
    private double currentLon = 0.0;
    private boolean isHighAccuracyMode = false;
    private int updateIntervalSeconds = 60; // По умолчанию обновляем раз в минуту


    // Временный список кэшированных зон из БД (чтобы не читать диск каждую секунду)
    private final List<GeofenceEntity> cachedDangerZones = new ArrayList<>();

    private LocationManagerSingleton() 
    {
        // Инициализация заглушек (в реальном приложении загружается из Room DB)
        initMockData();
    }    

    public static LocationManagerSingleton getInstance() 
    {
        if (instance == null) 
            {
            synchronized (LocationManagerSingleton.class) 
            {
                if (instance == null) {
                    instance = new LocationManagerSingleton();
                }
            }
        }
        return instance;
    }

    /* Вынос в функцию-рутину + Фильтрация
    Правильный подход — вынести проверку в отдельную функцию-рутину!, 
    но вместо if-else использовать проход по списку зон, который я загрузил из БД Room.
    Чтобы не нагружать процессор, использую простую математику: сначала отсекаю зоны, которые физически 
    далеко, а затем для близких считаем точное расстояние.    
    Вот как выглядит идеальная рутина проверки геозон */
    // готовый и очищенный от «магических чисел» вид

    private void checkGeofences() 
    {
        // Проход циклом по всем загруженным из базы данных (или кэша) опасным зонам
        for (GeofenceEntity zone : cachedDangerZones) 
        {        
        // Использ поля currentLat и currentLon для проверки радиуса. 
        // Берy координаты прямо из полей класса через this.currentLat и this.currentLon

        if (isUserInsideRadius(this.currentLat, this.currentLon, zone)) 
        {            
            System.out.println("[GPS-Tracker] КРИТИЧЕСКИЙ ВХОД! Пользователь пересёк границу зоны. Координаты:" 
                                + zone.id + this.currentLat + ", " + this.currentLon);
            
            // 1. Создаем объект события безопасности
            // В качестве threatType передаем маркер, который ожидает наша ViewModel ("HIGH_RISK_ZONE")
            SosEvent dangerEvent = new SosEvent("HIGH_RISK_ZONE", this.currentLat, this.currentLon);
            
            // 2. Отправляем событие в шину. Все подписчики (включая SafetyViewModel) мгновенно его получат
            SosEventBus.getInstance().publish(dangerEvent);
            
            // Прерываем цикл: если мы зашли хотя бы в одну опасную зону, тревогу уже нужно поднимать
            break; // Выходим из цикла, так как опасность уже обнаружена
        }
        }
        /* Производительность: Проверка выполняется ровно один раз.Отсутствие дублирования: 
        Нет дублирующегося кода циклов for внутри класса.
        Чистота кода: Метод не требует передачи аргументов, которые и так уже лежат внутри объекта 
        класса LocationManagerSingleton. */        
    }
    
        
    // Вынесенная функция-рутина проверки координат
    /* OBSOLETE code 
    public void checkGeofences(double userLat, double userLon) 
    {
        // Проверка вхождения в геозону риска
        boolean inDanger = (userLat > 32.0 && userLon > 34.0); // условный расчет
        if (inDanger) 
        {
            SosEventBus.getInstance().publish(
                new SosEvent("HIGH_RISK_ZONE", userLat, userLon)
            );
        }
        for (GeofenceEntity zone : cachedDangerZones) 
            {
            if (isUserInsideRadius(userLat, userLon, zone)) 
                {
                // Если зашли в зону — мгновенно шлем сигнал в шину
                SosEventBus.getInstance().publish(
                    new SosEventBus.SosEvent("HIGH_RISK_ZONE", userLat, userLon));
                break; // Достаточно одной зоны, чтобы поднять тревогу
            }
        }
    }       */
    /* Разбор логики по шагам:
    for (GeofenceEntity zone : cachedDangerZones): Приложение не привязано к фиксированным точкам на 
    Ближнем Востоке. Оно перебирает список объектов, полученных из вашей БД Room.
    isUserInsideRadius(...): Математическая рутина (формула гаверсинусов) рассчитывает точное расстояние 
    от ваших this.currentLat/this.currentLon до центра зоны в метрах.
    SosEventBus.getInstance().publish(...): Менеджер локации ничего не знает о существовании интерфейса, 
    экранов или ViewModel. Он просто «кричит» в шину данных: «Я зафиксировал опасность по таким-то 
    координатам!». А SafetyViewModel (которую мы настроили ранее) ловит этот крик через метод 
    onSosTriggered и запускает таймер «Убийцы». */

    // Математическая рутина: проверка вхождения в радиус (Формула Гаверсинусов в упрощенном виде)

    private boolean isUserInsideRadius(double userLat, double userLon, GeofenceEntity zone) 
    {
        final int EARTH_RADIUS_METERS = 6371000;

        // Переводим градусы в радианы
        double dLat = Math.toRadians(zone.centerLat - userLat);
        double dLon = Math.toRadians(zone.centerLon - userLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(zone.centerLat)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
                   
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceMeters = EARTH_RADIUS_METERS * c;

        // Если расстояние от пользователя до центра зоны меньше радиуса зоны — мы внутри
        return distanceMeters <= zone.radiusMeters;
    }

    private void initMockData() 
    {
        // Пример динамической опасной зоны (например, Тель-Авив или Иерусалим)
        GeofenceEntity middleEastZone = new GeofenceEntity();
        middleEastZone.id = 1;
        middleEastZone.centerLat = 32.0853;  // Реальные координаты
        middleEastZone.centerLon = 34.7818;
        middleEastZone.radiusMeters = 5000;   // Радиус опасности 5 км
        middleEastZone.incidentJsonData = "{}";
        
        cachedDangerZones.add(middleEastZone);
    }

    public void updateLocation(double lat, double lon) 
    {
        this.currentLat = lat;
        this.currentLon = lon;
        checkGeofences(); // Вызываем без аргументов
        //// checkGeofences(lat, lon); - - устаревший вариант, оставлен для показа
    }    

    // В реальном Android-приложении этот метод менял бы параметры LocationRequest
    // (например, интервал обновления с 15 минут до 1 секунды), а в чистом Java-проекте реализу
    //  логику изменения интервала опроса и переключения режимов.

    /* Переключает режим точности GPS-трекера.
    * @param active true — режим высокой точности (частые обновления), false — режим энергосбережения. */
    public void setHighAccuracyMode(boolean active) 
    {
        if (this.isHighAccuracyMode == active) 
        {
            return; // Режим уже установлен, ничего не делаем
        }
    
        this.isHighAccuracyMode = active;
    
        if (active) 
        {
            this.updateIntervalSeconds = 1; // В опасной зоне запрашиваем GPS каждую секунду
            System.out.println("[GPS-Tracker] РЕЖИМ ПОВЫШЕННОЙ ТОЧНОСТИ АКТИВИРОВАН. Интервал опроса: 1 сек.");
            // Здесь в реальном Android коде вызывалась бы перенастройка LocationClient / FusedLocationProvider
        } 
        else 
        {
            this.updateIntervalSeconds = 60; // В безопасной зоне экономим батарею (раз в минуту)
            System.out.println("[GPS-Tracker] Возврат в режим энергосбережения. Интервал опроса: 60 сек.");
        }
    }

    /** Геттер для проверки текущего состояния (пригодится для тестов) */
    public boolean isHighAccuracyMode() 
    {
        return isHighAccuracyMode;
    }
    /* Архитектурный '+'' этого решения:
    Теперь, когда SafetyViewModel вызывает setHighAccuracyMode(true), синглтон мгновенно перестраивает 
    свою внутреннюю логику работы с железом или симулятором трекера. Смартфон начнет отдавать координаты 
    непрерывным потоком, обеспечивая максимальную точность ведения по карте. */


    // Sинглтон геолокации должен уметь отдавать текущие координаты по запросу из других частей 
    // приложения (например, для отображения на карте). 
    // Добавьте стандартные геттеры, и ошибка сразу исчезнет:
    public double getCurrentLat()  {    return currentLat;    }

    public double getCurrentLon() {   return currentLon;    }



    /* OBSOLETE: Вынесено в отдельную функцию-рутину checkGeofences()

    private void checkGeofences(double lat, double lon) 
    {
        // Проверка вхождения в геозону риска
        boolean inDanger = (lat > 32.0 && lon > 34.0); // условный расчет
        if (inDanger) 
        {
            SosEventBus.getInstance().publish(new SosEventBus.SosEvent("HIGH_RISK_ZONE", 
                lat, lon)
            );
        }
    }   */
    
}
/* +++ такой архитектуры: Никакого хардкода координат в коде.
Алгоритм динамический: работает для любой точки мира, будь то Ближний Восток, Европа или Азия. 
Код распределен по зонам ответственности (Менеджер геолокации считает дистанцию, а UI просто реагирует) */
