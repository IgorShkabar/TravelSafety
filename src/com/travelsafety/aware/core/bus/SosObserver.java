package com.travelsafety.aware.core.bus;

public interface SosObserver 
{
    void onSosTriggered(SosEvent event);
}