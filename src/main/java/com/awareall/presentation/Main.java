package com.awareall.presentation;

import com.awareall.core.location.LocationManagerSingleton;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class Main 
{
    public static void main(String[] args)
    {
        System.out.println("AwareAll app is running");

        // Получаем экземпляр нашего менеджера локации
        LocationManagerSingleton locationManager = LocationManagerSingleton.getInstance();
        
        System.out.println("\n--- Test 1: User is far (Coordinates of Paris) ---");
        locationManager.updateLocation(48.8566, 2.3522);
        
        System.out.println("\n--- Test 2: User enters a dangerous zone (Tel Aviv) ---");
        // Передаем координаты внутри радиуса 5 км от точки 32.0853, 34.7818
        locationManager.updateLocation(32.0850, 34.7810); 
        frame.setVisible(true);        
    }
}
//перейти к написанию тестового сценария в Main.java, чтобы запустить симуляцию ходьбы и проверить, как срабатывает автоматика!
//Теперь, когда у нас полностью готовы LocationManagerSingleton, SosEventBus, SosEvent, SosObserver и SafetyViewModel, все части пазла MVVM-архитектуры соединены!
// создайте тестовый класс Main.java, в котором мы пропишем пару реальных шагов пользователя на карте (смена координат) и увидим в консоли VS Code, как автоматически оживает вся цепочка безопасности

// написать Main.java, чтобы запустить симуляцию перемещения по координатам и увидеть, 
// как все созданные нами архитектурные слои работают вместе.

// написать эмуляцию движения пользователя в Main.java, чтобы сымитировать, как при изменении координат автоматически включается точный GPS и 
// запускается таймер «Убийцы»? Или проверим работу triggerManualSos?
// Если у вас возникнут вопросы по работе SosEventBus или по проектированию реальной базы данных, дайте знать.
// написать быстрый тест для проверки нашего LocationManagerSingleton прямо в методе main
// Куда дальше двигаетесь по логике — займётесь шиной событий SosEventBus или перейдёте к реализации самой базы данных
//