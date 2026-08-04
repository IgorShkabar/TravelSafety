package com.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.travelsafety.aware.core.bus.SosEventBus;
import com.travelsafety.aware.domain.SosProtocolStrategy;
import com.travelsafety.aware.domain.model.GeoZoneRisk;
import com.travelsafety.aware.domain.repository.MapRepository;
import com.presentation.state.MapUiState;

public class SafetyViewModel extends ViewModel implements SosEventBus.SosObserver {

    private final MapRepository mapRepository;
    private final SosProtocolStrategy cellularStrategy;
    private final SosProtocolStrategy satelliteStrategy;

    private final MutableLiveData<MapUiState> _uiState = new MutableLiveData<>();
    public final LiveData<MapUiState> uiState = _uiState;

    public SafetyViewModel(MapRepository mapRepository, 
                           SosProtocolStrategy cellular, 
                           SosProtocolStrategy satellite) {
        this.mapRepository = mapRepository;
        this.cellularStrategy = cellular;
        this.satelliteStrategy = satellite;
        
        // Регистрация во внутренней шине событий (Observer)
        SosEventBus.getInstance().register(this);
        
        // Начальное состояние
        _uiState.setValue(new MapUiState(0.0, 0.0, new GeoZoneRisk(0.0), "IDLE"));
    }

    public void onLocationChanged(double lat, double lon) {
        GeoZoneRisk risk = mapRepository.getRiskForCoordinates(lat, lon);
        MapUiState current = _uiState.getValue();
        String currentSos = current != null ? current.sosStatus : "IDLE";
        
        _uiState.postValue(new MapUiState(lat, lon, risk, currentSos));
    }

    // Мгновенный триггер SOS из интерфейса
    public void triggerManualSos(String threatTypeName, boolean hasNetwork) {
        MapUiState state = _uiState.getValue();
        if (state == null) return;

        _uiState.setValue(new MapUiState(state.currentLatitude, state.currentLongitude, state.currentRisk, "SENDING"));

        // Использование паттерна Strategy в зависимости от сети
        SosProtocolStrategy selectedStrategy = hasNetwork ? cellularStrategy : satelliteStrategy;
        
        try {
            selectedStrategy.sendSos(state.currentLatitude, state.currentLongitude, threatTypeName);
            _uiState.setValue(new MapUiState(state.currentLatitude, state.currentLongitude, state.currentRisk, "DELIVERED"));
        } catch (Exception e) {
            _uiState.setValue(new MapUiState(state.currentLatitude, state.currentLongitude, state.currentRisk, "FAILED"));
        }
    }

    @Override
    public void onSosTriggered(SosEventBus.SosEvent event) {
        // Реакция на автоматическое срабатывание геозоны от LocationManager
        if ("HIGH_RISK_ZONE".equals(event.threatType)) {
            // Действие при входе в критическую зону (например, пред-активация SOS)
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        SosEventBus.getInstance().unregister(this);
    }
}

