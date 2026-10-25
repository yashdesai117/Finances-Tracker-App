# PaisaTrack: Automated SMS Financial Tracker & Home Screen Widget

**PaisaTrack** is an ultra-premium personal finance application designed for Android and the Web. It automatically categorizes all your expenses and income directly from your incoming bank and UPI SMS messages, and maintains an **always-visible Home Screen Widget & Floating Pill** so you always know your monthly spend and avoid unnecessary expenses.

---

## 🌟 Ultra-Premium Bento UI Highlights

1. **Bento Hero Tile & 30-Day Activity Strip**:
   - Giant typographic net spend with active budget progress.
   - Interactive 30-day vertical daily spend strip: Tap or hover any day of the month to view its exact spend.
   - Live daily safe allowance pill.

2. **Speedometer Budget Velocity Gauge**:
   - Live S-curve semicircle speedometer arc with dynamic needle.
   - Visualizes your burn rate and indicates whether you are *On Track*, in *Caution*, or *Pacing Too Fast*.

3. **3D Interactive Fanned Bank Card Stack**:
   - Interactive card stack featuring your parsed accounts (e.g. **ICICI Coral Credit Card \*\*2006**, **Salary Debit \*\*5512**, and **Cash Wallet**).
   - Golden EMV chip, holographic sheen, and elevation spring physics: click any card to bring it forward.

4. **100% Complete Category Allocation**:
   - Segmented proportional bar visualizer.
   - Complete breakdown displaying **every category** (Shopping, Groceries, Food & Dining, Transport, Bills & Utilities, Entertainment, Health, Investments, Education, Travel, Other) with active spending, percentage of monthly spend, and target budgets.
   - **Tap-to-Recategorize**: Tap any transaction to change its category with 1 tap.

5. **Dual Light & Dark Themes**:
   - Switch between sleek Obsidian Dark and crisp Minimal Light mode with the top-bar sun/moon toggle.

---

## 💻 React + shadcn + Tailwind Web Companion

The repository includes a modern React + TypeScript + Tailwind web companion located in `web/` with full **shadcn project structure**.

### Directory Structure:
```
web/
├── components.json         # shadcn CLI configuration
├── index.html
├── package.json
├── tailwind.config.js      # shadcn theme color tokens
├── tsconfig.json           # @/* path aliasing
├── vite.config.ts
└── src/
    ├── index.css           # shadcn CSS variables & OKLCH color definitions
    ├── lib/
    │   └── utils.ts        # shadcn cn() class merger
    └── components/
        └── ui/             # Standard shadcn component library path
            ├── web3-bento.tsx       # Web3 Bento Grid
            ├── paisatrack-bento.tsx # PaisaTrack INR Edition Bento
            ├── demo.tsx             # Demo container
            └── theme-toggle.tsx     # Theme toggle component
```

### Why `/components/ui` is Important
In shadcn/ui and modern React architecture, `/components/ui` serves as the centralized, un-opinionated primitive library directory. Placing atomic, reusable UI components here allows shadcn CLI commands (like `npx shadcn@latest add button`) to automatically resolve dependencies and maintain a consistent token system across your application.

### Running the Web Dashboard Locally
From the project root:
```bash
npm run web
```
Or inside `web/`:
```bash
npm run dev
```

### Setting up shadcn CLI in Any Project
If you ever need to initialize shadcn from scratch:
```bash
# 1. Initialize Vite + React + TS
npx create-vite@latest my-app --template react-ts

# 2. Install Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# 3. Initialize shadcn
npx shadcn@latest init
```

---

## 📱 How to Build the Android APK

### Option A: Free Cloud Build via GitHub Actions (Recommended)
1. Push code to your GitHub repository:
   ```bash
   git push origin main
   ```
2. GitHub automatically triggers `.github/workflows/build-apk.yml`.
3. Go to the **Actions** tab on GitHub, click the latest workflow run, and download `PaisaTrack-Debug-APK` directly to your phone.

### Option B: Local Android Build
Run `setup-and-build.ps1` or open the project in **Android Studio** and click **Build APK**.
