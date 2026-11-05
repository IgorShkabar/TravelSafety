package com.awareall.presentation.viewmodel;

public class LiveDataTest {
    
}


/* Да, абсолютно. Так как этот класс теперь управляет потоками, нам нужно протестировать: 
Что setValue работает мгновенно.
Что postValue действительно обновляет данные (а если выберем Swing/JavaFX, можно проверить асинхронность). */