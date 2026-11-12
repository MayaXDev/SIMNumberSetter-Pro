# SIM Number Setter Pro `v1.1` Cyber Edition
<p align="center">
  <img src="https://img.shields.io/badge/Release-v1.1--MayaXDev-00F5FF?style=for-the-badge&logo=android&logoColor=black" />
  <img src="https://img.shields.io/badge/Dual--Engine-LSPosed%20%2B%20Root%20Fallback-7B2CBF?style=for-the-badge&logo=codeforces&logoColor=white" />
  <img src="https://img.shields.io/badge/Android-8.0%20--%2014%20(API%2026--34)-00FF66?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Telegram-@MayaXDev-26A5E4?style=for-the-badge&logo=telegram&logoColor=white" />
  <img src="https://img.shields.io/badge/Developer-@Itcalldeveloper-9D4EDD?style=for-the-badge&logo=telegram&logoColor=white" />
</p>

---

## ⚡ Overview

**SIM Number Setter Pro** (`com.mayaxdev.simnumbersetter`) is an open-source, dual-vector Android telephony utility designed to read, write, and permanently program your phone number (MSISDN) directly into the SIM card hardware chip (`EF_MSISDN`).

Eliminates the pervasive **"Unknown SIM Number" / "Unknown MSISDN"** status across Android, unlocking instantaneous OTP and SMS-based device verification in:
- **Banking & UPI Apps:** PhonePe, Google Pay, Paytm, BHIM, Cred, Navi, Amazon Pay
- **RCS & Carrier Provisioning:** Google Messages RCS chats, VoLTE / VoNR network registration
- **Android Settings:** `Settings > About Phone > Status Information > SIM Status`

Engineered and maintained by **[@MayaXDev](https://t.me/MayaXDev)** & **[@Itcalldeveloper](https://t.me/Itcalldeveloper)**.

---

## 🚀 Key Pro Capabilities

| Feature | Description |
|---|---|
| **Dual-Vector Architecture** | Primary execution via **LSPosed Xposed Hook** with an automatic, zero-delay failover to **Direct Root Telephony Provider** (`content://telephony/siminfo`). Works even if LSPosed is inactive or unhooked! |
| **Dual SIM Independent Slot Selector** | Independent programming for **SIM 1 (Slot 0)** and **SIM 2 (Slot 1)** with zero physical SIM swapping required. |
| **Zero-Reboot Telephony Cycler** | Hot-restarts `com.android.phone` via root shell in < 2 seconds, immediately refreshing the SIM hardware cache without restarting the phone. |
| **One-Click Pro Privileges & Permissions** | Auto-grants `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, and `DUMP` permissions over root. |
| **E.164 Strict Format Validator** | Real-time input sanitizer enforcing E.164 international numbering (`+<CountryCode><SubscriberNumber>`). |
| **Live GitHub Update Radar** | Built-in one-tap update checker fetching the latest builds directly from GitHub Releases. |
| **100% Vector Cyber Design** | Deep dark cybernetic theme (`#0A0E17`), high-contrast neon accents (`#00F5FF`, `#00FF66`, `#7B2CBF`), and custom SIM hardware vector iconography. |

---

## 🛠️ Step-by-Step Operator Guide

### Path A: Direct Root Mode (Fastest — Zero Setup)
1. Install and launch **SIM Number Setter Pro**.
2. Tap **"AUTO-GRANT PRIVILEGES"** when prompted for Superuser access (APatch / KernelSU / Magisk).
3. Select your target slot: **SIM 1 (Slot 0)** or **SIM 2 (Slot 1)**.
4. Input your mobile number with country code (e.g. `+919876543210` or `+12025550143`).
5. Tap **"WRITE & SAVE TO SIM CARD"**. The app executes direct root NVRAM commit and refreshes the telephony stack.

### Path B: LSPosed System Framework Hook Mode
1. Open **LSPosed Manager** (`Zygisk`).
2. Navigate to **Modules** → select **SIM Number Setter Pro**.
3. Toggle **Enable Module** and check the following scopes:
   - `Phone (com.android.phone)`
   - `System Framework (android)`
4. Tap **"Auto-Restart Telephony Stack"** inside the app. The hook will bind instantly (`Telephony Hook: Connected & Ready`).
5. Write your number and save.

---

## 📦 Downloads & Releases

Pre-compiled and signed binaries are available in [Releases](https://github.com/MayaXDev/SIMNumberSetter-Pro/releases):

- **Production APK:** `SIMNumberSetter_v1.1_MayaXDev_Release.apk` (~4.6 MB, Proguard optimized, zipaligned).
- **Direct Build:** `SIMNumberSetter_v1.1_MayaXDev.apk` (~5.7 MB, debuggable).

---

## 🏗️ Building From Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34, Build-Tools 34.0.0)
- Gradle 8.2+

```bash
# Clone the repository
git clone https://github.com/MayaXDev/SIMNumberSetter-Pro.git
cd SIMNumberSetter-Pro

# Compile Debug Build
./gradlew assembleDebug

# Compile Production Release Build
./gradlew assembleRelease
```
Compiled APK artifacts will be generated in `app/build/outputs/apk/`.

---

## 🤝 Community & Support

Join our official channels for releases, system modules, and kernel patches:

- **Official Channel:** [https://t.me/MayaXDev](https://t.me/MayaXDev) (`@MayaXDev`)
- **Developer Hub:** [https://t.me/Itcalldeveloper](https://t.me/Itcalldeveloper) (`@Itcalldeveloper`)

---

## 📄 License

This project is licensed under the [GNU General Public License v3.0](LICENSE). Free and open-source for the developer and reverse-engineering community.
