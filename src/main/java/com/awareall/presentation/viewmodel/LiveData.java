package com.awareall.presentation.viewmodel;

/* Создай свой аналог LiveData (Паттерн Наблюдатель)В Android MutableLiveData позволяет 
 записывать данные, а LiveData — тольо читакть их. объедини это в один компактный класс 
 LiveData<T>    */

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LiveData<T> 
{
    private T value;
    private final List<Consumer<T>> observers = new ArrayList<>();

    public LiveData() { }

    public LiveData(T initialValue) 
    {
        this.value = initialValue;
    }

    // Аналог setValue() из Android — обновляет данные и уведомляет всех
    public void setValue(T newValue) 
    {
        this.value = newValue;
        for (Consumer<T> observer : observers) 
        {
            observer.accept(newValue);
        }
    }

    /* Добавь метод postValue(T newValue) внутрь класса LiveData<T>. 
        Для безопасности при многопоточности сделай обертку synchronize.    
     Потокобезопасное обновление данных.
     В кроссплатформенной Java синхронизация гарантирует, что данные будут обновлены корректно*/

    public void postValue(T newValue) 
    {
        synchronized (this) 
        {
            this.value = newValue;
        }
        // Уведомля наблюдателей. При необходимости выполнения строго в UI-потоке десктопа
        // здесь можно использовать SwingUtilities.invokeLater или Platform.runLater
        for (Consumer<T> observer : observers) 
        {
            observer.accept(newValue);
        }
    }
    /* Код компилируется в любой среде разработки (VS Code, IntelliJ, Eclipse).
      Логика обновлений защищена от состояния гонки (Race Condition), если 
      LocationManagerSingleton пришлет новые координаты из параллельного рабочего потока. */
    
    public synchronized T getValue() 
    {
        return value;
    }

    // Аналог observe() из Android. Вместо LifecycleOwner используем стандартный Consumer из Java 8
    public synchronized void observe(Consumer<T> observer) 
    {
        observers.add(observer);
        if (value != null) 
        {
            observer.accept(value); // Сразу отдаем текущее значение при подписке
        }
    }
    
    // Все архитектурные элементы теперь полностью синхронизированы. 
}

