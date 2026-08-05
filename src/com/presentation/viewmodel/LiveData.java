package com.presentation.viewmodel;

/* Шаг 1: Созда свой аналог LiveData (Паттерн Наблюдатель)В Android MutableLiveData позволяет 
 записывать данные, 
а LiveData — тольо читакть их. объедини это в один компактный класс LiveData<T> */

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LiveData<T> 
{
    private T value;
    private final List<Consumer<T>> observers = new ArrayList<>();

    public LiveData() {}

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

    public T getValue() 
    {
        return value;
    }

    // Аналог observe() из Android. Вместо LifecycleOwner используем стандартный Consumer из Java 8
    public void observe(Consumer<T> observer) 
    {
        observers.add(observer);
        if (value != null) {
            observer.accept(value); // Сразу отдаем текущее значение при подписке
        }
    }
}

