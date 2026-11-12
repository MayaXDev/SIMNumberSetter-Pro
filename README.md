# SIM Number Setter Pro `v1.1` (Cyber Edition)

<p align="center">
  <img src="https://img.shields.io/badge/Release-v1.1-00F5FF?style=for-the-badge&logo=android&logoColor=black" />
  <img src="https://img.shields.io/badge/Dual--Engine-LSPosed%20%2B%20Root%20Fallback-7B2CBF?style=for-the-badge&logo=codeforces&logoColor=white" />
  <img src="https://img.shields.io/badge/Android-8.0%20to%2016%20(API%2026--36)-00FF66?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Telegram-@MayaXDev-26A5E4?style=for-the-badge&logo=telegram&logoColor=white" />
  <img src="https://img.shields.io/badge/Developer-@Itcalldeveloper-9D4EDD?style=for-the-badge&logo=telegram&logoColor=white" />
</p>

<p align="center">
  <img src="logo_512.png" width="160" alt="SIM Number Setter Pro Logo" style="border-radius: 24px;" />
</p>

---

## ⚡ Overview

**SIM Number Setter Pro** (`com.mayaxdev.simnumbersetter`) is an open-source, high-performance Android telephony utility engineered to program, write, and permanently bind your subscriber phone number (MSISDN) directly into internal SIM card hardware memory (`EF_MSISDN`).

Eliminates the **"Unknown SIM Number" / "Unknown MSISDN"** status across all Android devices, enabling instant OTP, SMS, and device binding in:
- **Banking, Payment & UPI Apps:** PhonePe, Google Pay, Paytm, BHIM, Cred, Navi, Amazon Pay, YONO SBI, iMobile, Kotak 811
- **RCS & Carrier Provisioning:** Google Messages RCS chat features, VoLTE / VoNR network registration
- **Android Settings:** `Settings > About Phone > Status Information > SIM Status`

Engineered and maintained by **[@MayaXDev](https://t.me/MayaXDev)** & **[@Itcalldeveloper](https://t.me/Itcalldeveloper)**.

---

## 🚀 Key Features

| Feature | Technical Architecture |
|---|---|
| **Universal Android Support** | Full support from **Android 8.0 (API 26)** all the way to **Android 14, Android 15, and Android 16 (Baklava)**. |
| **Dual-Vector Telephony Engine** | Primary execution via **LSPosed Framework Hook** with an automatic zero-delay fallback to **Direct Root Telephony Provider** (`content://telephony/siminfo`). Works even if LSPosed is inactive! |
| **Dual-SIM Slot Selection** | Independent hardware slot programming for **SIM 1 (Slot 0)** and **SIM 2 (Slot 1)**. |
| **Zero-Reboot Telephony Cycler** | Hot-restarts `com.android.phone` via root shell in < 2 seconds, immediately refreshing the SIM hardware cache without restarting the phone. |
| **Integrated Telephony Monitor** | Real-time diagnostic console to inspect SQLite `siminfo` records and telephony logcat with one click. |
| **One-Click Privilege Auto-Grant** | Automatically configures and grants `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`, and `DUMP` permissions over Superuser (APatch / KernelSU / Magisk). |
| **Legacy Conflict Shield** | Detects conflicting obsolete packages and provides a 1-click uninstaller. |
| **100% Cyber Aesthetic** | High-contrast AMOLED cyber theme (`#0A0E17`), neon accents (`#00F5FF`, `#00FF66`, `#7B2CBF`), and custom vector iconography. |

---

## 🛠️ How to Use (Step-by-Step)

### Option 1: Direct Root Mode (Fastest — No LSPosed Required)
1. Install and launch **SIM Number Setter Pro**.
2. Grant Superuser access when prompted (APatch / KernelSU / Magisk).
3. Tap **"Auto-Grant All Permissions (1-Click)"**.
4. Select your SIM: **SIM 1 (Slot 0)** or **SIM 2 (Slot 1)**.
5. Enter your phone number in international format (e.g., `+91XXXXXXXXXX`).
6. Tap **"SAVE NUMBER TO SIM"**.
7. Tap **"⚡ Auto-Restart Telephony Stack"** to apply changes instantly.

### Option 2: LSPosed Hook Mode
1. Open **LSPosed Manager**.
2. Under **Modules**, find and enable **SIM Number Setter Pro**.
3. Select scope:
   - `Phone (com.android.phone)`
   - `System Framework (android)`
4. Launch app and tap **"⚡ Auto-Restart Telephony Stack"** to attach hooks without a reboot.
5. Save your number to SIM.

---

## 🔍 Verification

Once programmed:
1. Open **Phone Settings** → **About Phone** → **SIM Status**: your number will now be displayed.
2. Open **PhonePe / Google Pay / Paytm**: the SIM number will be detected automatically and OTP verification will succeed.

---

## 📱 Official Channels & Support

- 📢 **Official Telegram Channel:** [@MayaXDev](https://t.me/MayaXDev)
- 💻 **Developer Hub:** [@Itcalldeveloper](https://t.me/Itcalldeveloper)
- 📦 **Releases & Source:** [GitHub Repository](https://github.com/MayaXDev/SIMNumberSetter-Pro)

---

## 📄 License & Open-Source Integrity
Licensed under the [GNU General Public License v3.0](LICENSE). 100% Open Source and engineered by MayaXDev.
