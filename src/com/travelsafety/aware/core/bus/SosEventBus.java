package com.travelsafety.aware.core.bus;

import java.util.ArrayList;
import java.util.List;

настроить связку (регистрацию) нашей ViewModel в SosEventBus, чтобы проверить всю цепочку вызовов в работе
написать метод, который при старте приложения будет регистрировать (register) вашу ViewModel в SosEventBus, чтобы она начала получать реальные сигналы

// Паттерн Observer / Event Bus для диспетчера тревог
public class SosEventBus 
{
    private static final SosEventBus INSTANCE = new SosEventBus();
    private final List<SosObserver> observers = new ArrayList<>();

    private SosEventBus() {}

    public static SosEventBus getInstance() 
    {
        return INSTANCE;
    }

    public synchronized void register(SosObserver observer) 
    {
        observers.add(observer);
    }

    public synchronized void unregister(SosObserver observer) 
    {
        observers.remove(observer);
    }

    public synchronized void publish(SosEvent event) 
    {
        for (SosObserver observer : observers) 
            {
            observer.onSosTriggered(event);
        }
    }

    

    
}
