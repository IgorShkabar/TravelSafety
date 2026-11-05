# AwareAll

## Architecture of "AwareAll" program: 
rapid SOS transmission, risk geofence monitoring, offline map support. Codebase - Java. Packages: domain, data, presentation, core/bus layers. Patterns: Strategy for SOS protocols, Observer for the alarm dispatcher, Singleton/DI-lite for the geolocation manager.  System core: threads, network &amp; interface-based logging.
The solution split into logical layers (core, data, domain, presentation) and contains tests.
