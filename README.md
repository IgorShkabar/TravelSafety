# AwareAll
protecting individuals from attacks anywhere in the world. Interaction with the police, levels of aggression and crime by country, and the degree of danger.
# Screeshot
<img width="50%" alt="Screenshot From 2026-11-05 19-59-14" src="https://github.com/user-attachments/assets/53c0dc49-2cb3-4150-88a4-276704e25264" />

## Architecture of "AwareAll" program: 
rapid SOS transmission, risk geofence monitoring, offline map support. Codebase - Java. Packages: domain, data, presentation, core/bus layers. Patterns: Strategy for SOS protocols, Observer for the alarm dispatcher, Singleton/DI-lite for the geolocation manager.  System core: threads, network &amp; interface-based logging.
The solution split into logical layers (core, data, domain, presentation) and contains tests.
* <img width="1920" height="1080" alt="Screenshot From 2026-11-05 19-59-14" src="https://github.com/user-attachments/assets/4ad8049e-aa88-4f39-83ec-47954e5a673d" />
<img width="1920" height="1080" alt="Screenshot From 2026-11-05 19-59-14" src="https://github.com/user-attachments/assets/69170b74-db59-48dc-9d1a-946afd58515d" />
📂
<details>
```text
AwareAll/
│
└── src/
    ├── main/
    │   └── java/              <-- here is code
    │       └── com/
    │           └── awareall/
    │               └── core/
    │                   └── bus/
    │                       
    │
    └── test/                  <-- here are tests
        └── java/
            └── com/
                └── awareall/
                    └── core/
                        └── bus/
 ```
 </details>
Clean Architecture + MVVM(Model-View-ViewModel) - division into Presentation, Domain and Data layers.

## Key functionality:
* The algorithm is dynamic: it works for any point on Earth.
* Сode is extensible and cross-platform: It includes a `getUpdateIntervalSeconds()` getter. This allows any external simulation loop (in desktop Java/VS Code) or an actual geolocation service (in a future Android implementation) to dynamically read the required polling frequency.
* Event Bus for the alarm manager.
* Loading zones from a live database.
* The code is organized by areas of responsibility (the geolocation manager calculates the distance, while the UI simply reacts).
* The singleton instantly reconfigures its internal logic for interacting with the hardware or the tracker simulator.
* To avoid putting a load on the processor, using simple math: first, filtering out zones that are physically far away, and then, for the nearby ones, calculating the exact distance.

## 🛠️ How to make B2B software delivery instantaneous
1. Use `jpackage` (built into modern JDKs): since you are on Debian 13, you have a recent version of Java. The `jpackage` utility will bundle your Swing application together with the minimal necessary subset of Java (JRE) directly into the executable file.
