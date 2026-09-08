# AstroGauge

An IoT-based astrophotography assistant that combines real-time environmental measurements, weather data, and telescope specifications to estimate how suitable current conditions are for astrophotography.

## Overview

AstroGauge combines a small environmental sensing system with an Android application to help astrophotography enthusiasts evaluate observing conditions before starting an imaging session.

A NodeMCU ESP8266 collects temperature, humidity, and ambient light measurements using a DHT11 sensor and an LDR. The measurements are transmitted over Wi-Fi to the Android application, which combines them with cloud-cover information retrieved from a weather API and user-provided telescope specifications.

The application processes these inputs and produces a suitability score from 1–10 representing how favorable the current conditions are for astrophotography.

## How It Works

```text
        ┌──────────────────┐
        │    DHT11 Sensor  │
        │ Temperature      │
        │ Humidity         │
        └────────┬─────────┘
                 │
        ┌────────▼─────────┐
        │       LDR        │
        │  Ambient Light   │
        └────────┬─────────┘
                 │
                 ▼
        ┌──────────────────┐
        │  ESP8266 /       │
        │  NodeMCU         │
        └────────┬─────────┘
                 │ Wi-Fi
                 ▼
        ┌──────────────────┐
        │  Android App     │
        │                  │
        │  Sensor Data     │
        │  Weather Data    │
        │  Telescope Specs │
        └────────┬─────────┘
                 │
                 ▼
        ┌──────────────────┐
        │ Astrophotography │
        │   Suitability    │
        │     Score 1–10   │
        └──────────────────┘
Features
Real-time temperature measurement
Real-time humidity measurement
Ambient light measurement
Wi-Fi communication between the ESP8266 and Android application
Cloud-cover data retrieved from a weather API
Telescope specification input
Astrophotography suitability scoring from 1–10
Android-based interface for viewing and processing collected data
Hardware
NodeMCU ESP8266
DHT11 temperature and humidity sensor
Light-dependent resistor (LDR)
Breadboard
Jumper wires
Software
Android Application
Java
Android Studio
Embedded System
ESP8266
Arduino IDE
System Architecture

AstroGauge consists of two primary components.

1. Environmental Sensing System

The ESP8266 interfaces with the DHT11 and LDR to collect local environmental measurements. The device uses Wi-Fi to make the collected data available to the Android application.

2. Android Application

The Android application receives the sensor measurements, retrieves cloud-cover information through a weather API, accepts telescope specifications, and combines these inputs to calculate an astrophotography suitability score.

Suitability Score

The application produces a score between 1 and 10.

A higher score represents conditions that are more favorable for astrophotography based on the environmental and weather inputs available to the application.

Project Structure
AstroGauge/
├── app/                 # Android application
├── gradle/              # Gradle configuration
├── build.gradle
├── settings.gradle
└── README.md
Running the Project
Android Application
Clone the repository.
Open the project in Android Studio.
Build and run the application on an Android device or emulator.
ESP8266
Open the ESP8266 firmware in Arduino IDE.
Install the required ESP8266 board support and sensor libraries.
Connect the DHT11 and LDR according to the circuit configuration.
Configure the required Wi-Fi settings.
Upload the firmware to the NodeMCU ESP8266.
Limitations

AstroGauge is a prototype intended to demonstrate the integration of embedded sensing, mobile software, weather information, and domain-specific scoring.

The suitability score is an estimation rather than a professional astrophotography forecast. Its accuracy depends on the quality of the available sensor measurements and weather information.

Future Improvements
Replace the prototype hardware with a more compact sensor enclosure
Improve the astrophotography scoring model
Add additional environmental measurements
Add historical measurements and trends
Improve weather forecasting and location support
Add charts for sensor and weather data
Provide recommendations based on the selected telescope and observing conditions
Contributors
Amaaan Khan — Team Lead
Vinit Tarase
Maaz Khan
Azeem Patel
Sakshi Jha
