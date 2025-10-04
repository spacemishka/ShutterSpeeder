# 📸 ShutterSpeeder

**ShutterSpeeder** is a lightweight and fast Android app prototype designed to evaluate the correctness of shutter speeds with the Shutter Lover made by Sébastian Roy (DOES NOT WORK WITH the Baby Shutter Tester). -> https://www.ebay.de/usr/photography_electronics

## 🚀 Features

- Communicates over a serial interface (UART / USB-Serial) with the Shutter Lover 
- Shows shutter speed and Curtain Speeds  
- Tolerances can be used to use coloured indication   

## 🧱 Project Structure

ShutterSpeeder/
├── app/ # Main Android app module
├── build/ # Build artifacts
├── gradle/ # Gradle scripts and configuration
├── .github/ # GitHub workflows (CI/CD)
├── gradlew / gradlew.bat # Gradle wrapper scripts
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── ... # Additional configuration files

## 🧰 Requirements

- **Android Studio** (version X.X or newer)  
- **Android SDK** with at least **API Level Y**  
- **Gradle** (via wrapper)  
- A device or emulator with camera support  

## ⚙️ Installation & Setup

1. Clone the repository  
   ```bash
   git clone https://github.com/spacemishka/ShutterSpeeder.git
   cd ShutterSpeeder
   ```
Open the project in Android Studio

Sync Gradle and build the project

Install and run the app on a device or emulator



<img width="960" height="1280" alt="image" src="https://github.com/user-attachments/assets/d146f122-af86-4509-beb3-817aeb84ad7d" />
<img width="960" height="1280" alt="image" src="https://github.com/user-attachments/assets/9751dd57-0a52-4540-a4cb-b96a861991ae" />

