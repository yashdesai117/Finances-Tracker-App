"use client";

import * as React from "react";
import { Web3Bento, Web3BentoProps } from "@/components/ui/web3-bento";

export function PaisaTrackBento({ isDark }: { isDark: boolean }) {
  // Generate realistic 365-day cumulative spending walk
  const history = React.useMemo(() => {
    const arr: number[] = [];
    let cur = 12000;
    for (let i = 0; i < 365; i++) {
      cur += 400 + Math.sin(i * 0.2) * 200 + (Math.random() - 0.45) * 300;
      arr.push(cur);
    }
    return arr;
  }, []);

  const benchmark = React.useMemo(() => {
    const arr: number[] = [];
    let cur = 11000;
    for (let i = 0; i < 365; i++) {
      cur += 350 + (i * 0.1);
      arr.push(cur);
    }
    return arr;
  }, []);

  const props: Web3BentoProps = {
    account: "yash.fin",
    network: "PaisaTrack UPI",
    history,
    benchmark,
    locale: "en-IN",
    accent: "oklch(0.65 0.24 260)", // Electric Cobalt
    accent2: "oklch(0.72 0.2 145)",  // Emerald Green
    allocation: [
      { symbol: "Shopping", value: 13000 },
      { symbol: "Transport", value: 1499 },
      { symbol: "Food & Dining", value: 4250 },
      { symbol: "Bills & Utilities", value: 2800 },
      { symbol: "Groceries", value: 3494 },
    ],
    cards: [
      {
        id: "c1",
        label: "ICICI Bank Coral",
        last4: "2006",
        holder: "Yash Desai",
        expiry: "08/29",
        balance: "₹23,544 spent",
        color: "oklch(0.55 0.22 265)",
        color2: "oklch(0.42 0.18 290)",
        ink: "#ffffff"
      },
      {
        id: "c2",
        label: "Salary Debit A/C",
        last4: "5512",
        holder: "Yash Desai",
        expiry: "11/28",
        balance: "₹1,499 spent",
        color: "oklch(0.62 0.18 160)",
        color2: "oklch(0.48 0.14 180)",
        ink: "#ffffff"
      },
      {
        id: "c3",
        label: "Reserve Wallet",
        last4: "CASH",
        holder: "Cash Drawer",
        expiry: "Never",
        balance: "₹9,957 left",
        color: "oklch(0.78 0.16 75)",
        color2: "oklch(0.65 0.18 45)",
        ink: "#1e1400"
      }
    ],
    staking: {
      asset: "SIP / Mutual Fund",
      staked: 1,
      apy: 14.8,
      rewards: 0.084,
      usdPrice: 125000
    },
    liquidity: {
      pair: "Burn Velocity",
      fee: "Daily Allowance",
      min: 1200,
      max: 2200,
      price: 1659,
      fees: 9957
    },
    activity: [
      {
        id: "tx1",
        kind: "swap",
        title: "Amazon Shopping",
        detail: "ICICI Credit Card **2006",
        amount: "−₹13,000",
        status: "confirmed",
        time: "Today"
      },
      {
        id: "tx2",
        kind: "receive",
        title: "Cashback Received",
        detail: "ICICI Credit Card **2006",
        amount: "+₹2",
        status: "confirmed",
        time: "Today"
      },
      {
        id: "tx3",
        kind: "bridge",
        title: "Uber Ride to Airport",
        detail: "Debit A/C **5512",
        amount: "−₹1,499",
        status: "confirmed",
        time: "Yesterday"
      },
      {
        id: "tx4",
        kind: "stake",
        title: "Swiggy Food Delivery",
        detail: "UPI ICICI",
        amount: "−₹642",
        status: "confirmed",
        time: "2d ago"
      }
    ]
  };

  return <Web3Bento {...props} />;
}
