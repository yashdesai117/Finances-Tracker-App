import React, { useState, useEffect } from "react";
import { Web3Bento } from "@/components/ui/web3-bento";
import { PaisaTrackBento } from "@/components/ui/paisatrack-bento";

export function App() {
  const [isDark, setIsDark] = useState(true);
  const [activeTab, setActiveTab] = useState<"paisatrack" | "web3">("paisatrack");

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [isDark]);

  return (
    <div className={`min-h-screen w-full transition-colors duration-300 ${isDark ? "dark bg-background text-foreground" : "bg-[#f8fafc] text-[#0f172a]"}`}>
      {/* Top Navbar */}
      <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/80 backdrop-blur-md">
        <div className="mx-auto flex max-w-[1120px] items-center justify-between px-4 py-3 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary text-primary-foreground font-black shadow-sm">
              ₹
            </div>
            <div>
              <h1 className="text-sm font-bold tracking-tight">PAISATRACK</h1>
              <p className="text-[10px] text-muted-foreground uppercase tracking-widest font-mono">Ultra-Premium Bento UI</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* Bento Mode Switcher */}
            <div className="flex rounded-full border border-border bg-card p-1">
              <button
                onClick={() => setActiveTab("paisatrack")}
                className={`rounded-full px-3 py-1 text-xs font-semibold transition-all ${
                  activeTab === "paisatrack"
                    ? "bg-primary text-primary-foreground shadow-sm"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                PaisaTrack INR
              </button>
              <button
                onClick={() => setActiveTab("web3")}
                className={`rounded-full px-3 py-1 text-xs font-semibold transition-all ${
                  activeTab === "web3"
                    ? "bg-primary text-primary-foreground shadow-sm"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                Web3 Crypto
              </button>
            </div>

            {/* Light / Dark Mode Toggle */}
            <button
              onClick={() => setIsDark(!isDark)}
              className="flex h-9 w-9 items-center justify-center rounded-full border border-border bg-card text-sm shadow-sm transition-transform hover:scale-105 active:scale-95"
              aria-label="Toggle theme"
            >
              {isDark ? "☀️" : "🌙"}
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="mx-auto flex max-w-[1120px] flex-col items-center justify-center px-4 py-8 sm:px-6">
        {activeTab === "paisatrack" ? (
          <PaisaTrackBento isDark={isDark} />
        ) : (
          <Web3Bento />
        )}

        {/* Feature Highlights Footer */}
        <section className="mt-12 w-full rounded-2xl border border-border bg-card p-6 shadow-sm">
          <h2 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
            Ultra-Premium Architecture
          </h2>
          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div className="rounded-xl border border-border/60 bg-muted/30 p-4">
              <p className="font-semibold text-sm">✨ 3D Fanned Card Stack</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Holographic glare sheen, EMV golden chip, and interactive elevation click-to-front stack.
              </p>
            </div>
            <div className="rounded-xl border border-border/60 bg-muted/30 p-4">
              <p className="font-semibold text-sm">⚡ Speedometer Pacing Gauge</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Real-time SVG arc needle showing daily safe allowance vs velocity threshold.
              </p>
            </div>
            <div className="rounded-xl border border-border/60 bg-muted/30 p-4">
              <p className="font-semibold text-sm">📊 100% Category Breakdown</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Segmented proportional allocation bar with every category and 1-tap recategorization.
              </p>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
