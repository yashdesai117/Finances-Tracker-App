# PaisaTrack: Automated SMS Financial Tracker & Home Screen Widget

**PaisaTrack** is a native Android application designed to give you effortless, continuous awareness of your spending. It automatically categorizes all your expenses and income directly from your incoming bank and UPI SMS messages, and maintains an **always-visible Home Screen Widget** so you always know your monthly spend and avoid unnecessary expenses.

---

## 🌟 Key Features

1. **Always-Visible Android Home Screen Widget**:
   - Displays your **Monthly Spend till date** (e.g., `₹ 24,580`) in large, crisp typography.
   - Progress bar towards your monthly budget target.
   - **Daily Burn Rate**: Shows your remaining safe spending allowance per day (e.g. `₹ 650/day left`).
   - Visual status badge: 🟢 *On Track* (<75%), 🟡 *Caution* (75-95%), or 🔴 *Over Budget* (>95%).
   - **Quick Add (`+`) button**: Log cash expenses directly from your home screen in 2 seconds.
   - Updates instantaneously whenever a new bank SMS arrives.

2. **Automatic SMS Financial Parsing**:
   - Supports Indian banks and UPI ecosystems: **HDFC, SBI, ICICI, Axis, Kotak, PNB, Canara, GPay, PhonePe, Paytm, Cred, Amazon Pay, and credit cards**.
   - Extracts amount, debit vs credit, account/card number, and merchant name.
   - Auto-categorizes into 10+ standard buckets:
     - 🍔 **Food & Dining** (Swiggy, Zomato, cafes, restaurants)
     - 🛒 **Groceries** (Blinkit, Zepto, BigBasket, DMart, Instamart)
     - 🛍️ **Shopping** (Amazon, Flipkart, Myntra, retail)
     - 🚗 **Transport & Fuel** (Uber, Ola, Rapido, Petrol pumps, Metro)
     - 💡 **Bills & Utilities** (Electricity, Gas, Broadband, Mobile, Rent)
     - 🎬 **Entertainment** (Netflix, Prime, BookMyShow, Spotify)
     - 💊 **Health & Medical** (Pharmacies, Clinics, Apollo, 1mg)
     - 📈 **Investments** (Zerodha, Groww, SIPs, Mutual Funds)
     - 💼 **Salary & Income** (Salary credits, dividends, refunds)
     - 📦 **Other / Cash**

3. **100% Offline & Private**:
   - PaisaTrack requests **NO internet permission** in its Android manifest.
   - None of your bank SMS, messages, or financial figures are ever uploaded anywhere. Everything stays securely on your phone.

4. **Past SMS Backfill (60-day Import)**:
   - On first setup, tap "Scan Past SMS" in the app to analyze previous transactions and start with an accurate monthly total immediately.

---

## 🚀 How to Try the Interactive Simulator Right Now

We've included a zero-setup interactive web companion and simulator:
1. Open the folder `web-simulator/` in Windows File Explorer.
2. Double-click **`index.html`** to open it in Chrome or Edge.
3. You can click any sample bank SMS (HDFC, SBI, Swiggy, Blinkit, Salary) or paste your own SMS to see how the parser works and watch the **Home Screen Widget** recalculate in real-time!

---

## 📱 How to Build the Android APK

Since you are not from a tech background, we've provided multiple easy ways:

### Option A: Automated 1-Click Script (Recommended)
1. Right-click `setup-and-build.ps1` and select **"Run with PowerShell"** (or open PowerShell in this folder and run `.\setup-and-build.ps1`).
2. The script will automatically verify/install OpenJDK 17 via Windows `winget` and build the debug APK.
3. Your installable APK will be ready in `output/PaisaTrack.apk`!

### Option B: Free Cloud Build via GitHub Actions
1. Upload/push this repository to your GitHub account.
2. GitHub will automatically run the pre-configured `.github/workflows/build-apk.yml` workflow.
3. Download the compiled `PaisaTrack-Debug-APK` directly from the GitHub Actions tab to your phone!

### Option C: Open in Android Studio
1. Open Android Studio -> Select **Open** -> Choose `Finances Tracker App`.
2. Click the green **Run** button (or `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`).

---

## 📲 How to Install & Add the Widget to Your Phone

1. **Transfer the APK**: Send `output/PaisaTrack.apk` to your phone via USB cable, WhatsApp, Telegram, or Google Drive.
2. **Install**: Tap the `.apk` on your phone and tap **Install** (allow "Install unknown apps" from your file manager or browser if prompted).
3. **Grant SMS Permission**: Open PaisaTrack, tap the 📩 icon in the top right, and tap **"Grant SMS Permission"**. (Optional: Tap "Scan Past 60 Days SMS" to import existing expenses).
4. **Add the Widget to your Home Screen**:
   - Go to your Android phone's Home Screen.
   - Long-press any empty area on the screen.
   - Tap **Widgets**.
   - Find **PaisaTrack** and drag the **"Monthly Spend Tracker"** widget onto your home screen.
   - That's it! Your monthly spend, progress bar, and daily safe allowance will always be right there in front of you.
