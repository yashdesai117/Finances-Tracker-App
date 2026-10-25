// PaisaTrack Web Simulator Engine
// Implements exact SMS parsing & auto-categorization matching the Kotlin Android app

const DEFAULT_CATEGORIES = {
  food: { name: "Food & Dining", emoji: "🍔", color: "#F59E0B", defaultBudget: 8000 },
  groceries: { name: "Groceries", emoji: "🛒", color: "#10B981", defaultBudget: 6000 },
  shopping: { name: "Shopping", emoji: "🛍️", color: "#EC4899", defaultBudget: 5000 },
  transport: { name: "Transport & Fuel", emoji: "🚗", color: "#3B82F6", defaultBudget: 4000 },
  bills: { name: "Bills & Utilities", emoji: "💡", color: "#8B5CF6", defaultBudget: 6000 },
  entertainment: { name: "Entertainment", emoji: "🎬", color: "#F43F5E", defaultBudget: 2500 },
  health: { name: "Health & Medical", emoji: "💊", color: "#14B8A6", defaultBudget: 2000 },
  investments: { name: "Investments", emoji: "📈", color: "#6366F1", defaultBudget: 5000 },
  salary: { name: "Salary & Income", emoji: "💼", color: "#22C55E", defaultBudget: 0, isIncome: true },
  cashback: { name: "Cashback & Refunds", emoji: "🎁", color: "#A855F7", defaultBudget: 0, isIncome: true },
  other: { name: "Other Expenses", emoji: "📦", color: "#64748B", defaultBudget: 1500 }
};

const KEYWORD_MAP = {
  food: ["swiggy", "zomato", "starbucks", "mcdonald", "domino", "pizza", "burger", "kfc", "subway", "cafe", "restaurant", "dining", "bakery", "eats", "barbeque", "biryani", "chai", "tea", "haldiram", "food"],
  groceries: ["blinkit", "zepto", "instamart", "bigbasket", "dmart", "grofers", "spencers", "supermarket", "provision", "grocery", "groceries", "fresh", "milk", "kirana", "nature basket", "country delight"],
  shopping: ["amazon", "flipkart", "myntra", "ajio", "zara", "h&m", "meesho", "nykaa", "tata cliq", "decathlon", "lifestyle", "westside", "pantaloons", "max fashion", "retail", "shopping", "clothing", "apparel"],
  transport: ["uber", "ola", "rapido", "metro", "petrol", "fuel", "indian oil", "iocl", "bharat petroleum", "bpcl", "hpcl", "shell", "irctc", "makemytrip", "cleartrip", "indigo", "air india", "fastag", "toll", "auto", "cab"],
  bills: ["electricity", "bescom", "tneb", "torrent", "water", "broadband", "wifi", "airtel", "jio", "vodafone", "idea", "vi", "bsnl", "gas", "igl", "mgl", "adani electricity", "tatapower", "dth", "tata sky", "recharge", "rent", "maintenance"],
  entertainment: ["netflix", "spotify", "bookmyshow", "pvr", "inox", "primevideo", "hotstar", "disney", "youtube", "cinema", "movie", "steam", "playstation", "gaming"],
  health: ["apollo", "pharmeasy", "1mg", "tata 1mg", "medplus", "netmeds", "clinic", "hospital", "pharmacy", "medical", "doctor", "lab", "dental"],
  investments: ["zerodha", "groww", "angelone", "upstox", "mutual fund", "sip", "kuvera", "coin", "securities", "fixed deposit"],
  salary: ["salary", "payroll", "stipend", "bonus", "wages"],
  cashback: ["cashback", "refund", "reversal"]
};

// Initial state with some demo historical data for realistic experience
let monthlyBudget = 35000;
let transactions = [
  { id: 1, amount: 450, type: "EXPENSE", categoryId: "food", merchant: "Swiggy", accountName: "HDFC A/C **1234", timestamp: Date.now() - 3600000 * 2, isAuto: true },
  { id: 2, amount: 1240, type: "EXPENSE", categoryId: "groceries", merchant: "Blinkit", accountName: "SBI A/C **5678", timestamp: Date.now() - 3600000 * 18, isAuto: true },
  { id: 3, amount: 2199, type: "EXPENSE", categoryId: "shopping", merchant: "Amazon India", accountName: "ICICI Card **9012", timestamp: Date.now() - 3600000 * 36, isAuto: true },
  { id: 4, amount: 75000, type: "INCOME", categoryId: "salary", merchant: "Acme Tech Payroll", accountName: "HDFC A/C **1234", timestamp: Date.now() - 3600000 * 72, isAuto: true }
];

// SMS Parser Engine implementation in JS
function parseSms(sender, body) {
  const lower = body.toLowerCase();

  // 1. Ignore OTPs and non-completed authorization
  const ignoreWords = ["otp", "one time password", "verification code", "secret code", "do not share", "request to pay", "declined", "failed"];
  for (const w of ignoreWords) {
    if (lower.includes(w)) {
      return { success: false, reason: "Ignored: OTP / Non-transaction alert" };
    }
  }

  // 2. Debit vs Credit
  const debitWords = ["debited", "spent", "paid to", "transferred to", "withdrawn", "purchase of", "charged", "sent to", "txn of", "payment of", "debited by"];
  const creditWords = ["credited", "received", "deposited", "salary credited", "refund of", "cashback of", "added to"];

  const isDebit = debitWords.some(w => lower.includes(w));
  const isCredit = creditWords.some(w => lower.includes(w));

  if (!isDebit && !isCredit) {
    return { success: false, reason: "No debit or credit transaction indicators found" };
  }

  const type = isDebit ? "EXPENSE" : "INCOME";

  // 3. Extract Amount
  // matches: Rs. 450, Rs 1,250.00, INR 899.50, ₹340
  const amountRegex = /(?:(?:RS\.?|INR|₹)\s*(\d[\d,]*(?:\.\d{1,2})?))|(\d[\d,]*(?:\.\d{1,2})?)\s*(?:RS\.?|INR|₹)/i;
  const match = body.match(amountRegex);
  if (!match) {
    return { success: false, reason: "Could not detect transaction amount" };
  }
  const rawAmt = match[1] || match[2];
  const amount = parseFloat(rawAmt.replace(/,/g, ''));
  if (isNaN(amount) || amount <= 0) {
    return { success: false, reason: "Invalid transaction amount" };
  }

  // 4. Extract Merchant
  let merchant = "";
  const merchantRegex = /(?:towards|at|paid to|to VPA|to)\s+([A-Za-z0-9\s._@\-&]{2,35}?)(?=\s+(?:on|ref|upi|avl|bal|clear|using|limit|via|through|ending|a\/c|from|dated|\.|,|$))/i;
  const mMatch = body.match(merchantRegex);
  if (mMatch && mMatch[1].trim().length > 1) {
    merchant = cleanMerchant(mMatch[1].trim());
  } else {
    merchant = cleanSender(sender);
  }

  // 5. Account Name
  let accountName = "Bank Account";
  const accRegex = /(?:a\/c|acct|account|card)\s*(?:no\.?)?\s*(?:ending\s*(?:with\s*)?)?([xX*]*\d{3,4})/i;
  const aMatch = body.match(accRegex);
  if (aMatch && aMatch[1]) {
    accountName = `A/C ${aMatch[1]}`;
  } else {
    accountName = cleanSender(sender);
  }

  // 6. Category Classifier
  let categoryId = "other";
  if (type === "INCOME") {
    if (lower.includes("salary") || lower.includes("payroll")) categoryId = "salary";
    else if (lower.includes("cashback") || lower.includes("refund")) categoryId = "cashback";
    else categoryId = "salary";
  } else {
    const combined = `${merchant.toLowerCase()} ${lower}`;
    for (const [cat, words] of Object.entries(KEYWORD_MAP)) {
      if (words.some(kw => combined.includes(kw))) {
        categoryId = cat;
        break;
      }
    }
  }

  return {
    success: true,
    amount,
    type,
    merchant,
    accountName,
    categoryId,
    timestamp: Date.now()
  };
}

function cleanMerchant(str) {
  return str.replace(/^(towards|at|to|info:?)\s+/i, '')
    .replace(/[.,;:_]+$/, '')
    .trim()
    .split(/\s+/)
    .slice(0, 4)
    .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ');
}

function cleanSender(sender) {
  const clean = sender.replace(/^[A-Za-z]{2}-/, '').trim().toUpperCase();
  if (clean.includes("HDFC")) return "HDFC Bank";
  if (clean.includes("SBI")) return "SBI";
  if (clean.includes("ICICI")) return "ICICI Bank";
  if (clean.includes("AXIS")) return "Axis Bank";
  if (clean.includes("KOTAK")) return "Kotak Bank";
  if (clean.includes("PAYTM")) return "Paytm";
  if (clean.includes("GPAY")) return "Google Pay";
  if (clean.includes("PHONEPE")) return "PhonePe";
  return clean || "Bank";
}

// Format currency
function formatINR(num) {
  return "₹" + Math.round(num).toLocaleString('en-IN');
}

// UI Refresh
function refreshUI() {
  const totalExpense = transactions
    .filter(t => t.type === "EXPENSE")
    .reduce((sum, t) => sum + t.amount, 0);

  const totalIncome = transactions
    .filter(t => t.type === "INCOME")
    .reduce((sum, t) => sum + t.amount, 0);

  const remainingBudget = Math.max(0, monthlyBudget - totalExpense);
  const percentUsed = monthlyBudget > 0 ? Math.min(100, Math.round((totalExpense / monthlyBudget) * 100)) : 0;

  const now = new Date();
  const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
  const daysLeft = Math.max(1, daysInMonth - now.getDate() + 1);
  const dailyBurnRate = remainingBudget / daysLeft;

  // 1. Update Home Screen Widget
  const widgetAmount = document.getElementById("widget-amount");
  const widgetBudgetInfo = document.getElementById("widget-budget-info");
  const widgetBurnRate = document.getElementById("widget-burn-rate");
  const widgetProgress = document.getElementById("widget-progress");
  const widgetStatus = document.getElementById("widget-status");

  widgetAmount.textContent = formatINR(totalExpense);
  widgetBudgetInfo.textContent = `Budget: ${formatINR(monthlyBudget)}`;
  widgetBurnRate.textContent = `${formatINR(dailyBurnRate)}/day left`;
  widgetProgress.style.width = `${percentUsed}%`;

  widgetStatus.className = "widget-status-badge";
  if (percentUsed > 95) {
    widgetStatus.classList.add("danger");
    widgetStatus.textContent = `● Over Budget (${percentUsed}%)`;
    widgetProgress.style.backgroundColor = "var(--crimson)";
  } else if (percentUsed > 75) {
    widgetStatus.classList.add("caution");
    widgetStatus.textContent = `● Caution (${percentUsed}%)`;
    widgetProgress.style.backgroundColor = "var(--amber)";
  } else {
    widgetStatus.textContent = `● On Track (${percentUsed}%)`;
    widgetProgress.style.backgroundColor = "var(--emerald)";
  }

  // 2. Update Dashboard Overview Cards
  document.getElementById("dash-total-spend").textContent = formatINR(totalExpense);
  document.getElementById("dash-total-income").textContent = formatINR(totalIncome);
  document.getElementById("dash-net-savings").textContent = formatINR(Math.max(0, totalIncome - totalExpense));

  // 3. Update Category Breakdown
  const catList = document.getElementById("category-spend-list");
  catList.innerHTML = "";

  const categoryTotals = {};
  for (const t of transactions) {
    if (t.type === "EXPENSE") {
      categoryTotals[t.categoryId] = (categoryTotals[t.categoryId] || 0) + t.amount;
    }
  }

  const sortedCats = Object.entries(categoryTotals).sort((a, b) => b[1] - a[1]);
  if (sortedCats.length === 0) {
    catList.innerHTML = `<div style="color:var(--text-muted);font-size:13px;padding:8px 0;">No expenses recorded yet.</div>`;
  } else {
    for (const [catId, spent] of sortedCats) {
      const cat = DEFAULT_CATEGORIES[catId] || DEFAULT_CATEGORIES.other;
      const budget = cat.defaultBudget;
      const percent = budget > 0 ? Math.min(100, Math.round((spent / budget) * 100)) : 0;

      const item = document.createElement("div");
      item.className = "category-item";
      item.innerHTML = `
        <div class="cat-header">
          <div class="cat-name-icon">
            <span>${cat.emoji}</span>
            <span>${cat.name}</span>
          </div>
          <div class="cat-amount">${formatINR(spent)} ${budget > 0 ? `<span style="font-size:11px;color:var(--text-muted)">(${percent}%)</span>` : ''}</div>
        </div>
        <div class="cat-bar-track">
          <div class="cat-bar-fill" style="width:${budget > 0 ? percent : 50}%; background-color:${percent > 90 ? 'var(--crimson)' : cat.color};"></div>
        </div>
      `;
      catList.appendChild(item);
    }
  }

  // 4. Update Transactions List
  const txnContainer = document.getElementById("recent-txns-list");
  txnContainer.innerHTML = "";
  for (const t of [...transactions].sort((a, b) => b.timestamp - a.timestamp).slice(0, 8)) {
    const cat = DEFAULT_CATEGORIES[t.categoryId] || DEFAULT_CATEGORIES.other;
    const isExpense = t.type === "EXPENSE";

    const item = document.createElement("div");
    item.className = "txn-item";
    item.innerHTML = `
      <div class="txn-left">
        <div class="txn-icon">${cat.emoji}</div>
        <div>
          <div class="txn-merchant">${t.merchant}</div>
          <div class="txn-meta">${cat.name} • ${t.accountName} ${t.isAuto ? '• ⚡ SMS' : ''}</div>
        </div>
      </div>
      <div class="txn-amount ${isExpense ? 'expense' : 'income'}">
        ${isExpense ? '-' : '+'} ${formatINR(t.amount)}
      </div>
    `;
    txnContainer.appendChild(item);
  }
}

// Event Listeners Setup
document.addEventListener("DOMContentLoaded", () => {
  const smsInput = document.getElementById("sms-input");
  const senderInput = document.getElementById("sender-input");
  const parseBtn = document.getElementById("btn-parse-sms");
  const resultBox = document.getElementById("parse-result-box");
  const budgetInput = document.getElementById("setting-budget-input");
  const updateBudgetBtn = document.getElementById("btn-update-budget");

  // Sample SMS click handlers
  document.querySelectorAll(".chip-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      smsInput.value = btn.getAttribute("data-sms");
      senderInput.value = btn.getAttribute("data-sender") || "VM-HDFCBK";
      triggerParse();
    });
  });

  function triggerParse() {
    const body = smsInput.value.trim();
    const sender = senderInput.value.trim() || "BANK";
    if (!body) return;

    const parsed = parseSms(sender, body);
    resultBox.style.display = "block";

    if (parsed.success) {
      const cat = DEFAULT_CATEGORIES[parsed.categoryId] || DEFAULT_CATEGORIES.other;
      resultBox.innerHTML = `
        <div style="color:var(--emerald);font-weight:700;margin-bottom:8px;">✅ Detected Valid Financial Transaction!</div>
        <div class="inspector-grid">
          <div class="inspector-item"><div class="inspector-label">Type</div><div class="inspector-value" style="color:${parsed.type === 'EXPENSE' ? 'var(--text-primary)' : 'var(--emerald)'}">${parsed.type}</div></div>
          <div class="inspector-item"><div class="inspector-label">Amount</div><div class="inspector-value">${formatINR(parsed.amount)}</div></div>
          <div class="inspector-item"><div class="inspector-label">Merchant</div><div class="inspector-value">${parsed.merchant}</div></div>
          <div class="inspector-item"><div class="inspector-label">Category</div><div class="inspector-value">${cat.emoji} ${cat.name}</div></div>
          <div class="inspector-item"><div class="inspector-label">Account</div><div class="inspector-value">${parsed.accountName}</div></div>
        </div>
      `;

      // Add to transaction store & update widget immediately
      transactions.unshift({
        id: Date.now(),
        amount: parsed.amount,
        type: parsed.type,
        categoryId: parsed.categoryId,
        merchant: parsed.merchant,
        accountName: parsed.accountName,
        timestamp: parsed.timestamp,
        isAuto: true
      });

      refreshUI();
    } else {
      resultBox.innerHTML = `
        <div style="color:var(--crimson);font-weight:700;">❌ Not Recorded</div>
        <div style="font-size:13px;color:var(--text-secondary);margin-top:4px;">${parsed.reason}</div>
      `;
    }
  }

  parseBtn.addEventListener("click", triggerParse);

  // Budget Update
  updateBudgetBtn.addEventListener("click", () => {
    const newBudget = parseFloat(budgetInput.value);
    if (!isNaN(newBudget) && newBudget > 0) {
      monthlyBudget = newBudget;
      refreshUI();
      alert(`Monthly budget updated to ${formatINR(monthlyBudget)}. Notice how the Home Screen Widget updated immediately!`);
    }
  });

  // Modal for Quick Add Cash
  const modal = document.getElementById("cash-modal");
  const openModalBtn = document.getElementById("widget-btn-add");
  const cancelModalBtn = document.getElementById("modal-cancel-btn");
  const saveCashBtn = document.getElementById("modal-save-btn");

  openModalBtn.addEventListener("click", () => {
    modal.classList.add("active");
  });

  cancelModalBtn.addEventListener("click", () => {
    modal.classList.remove("active");
  });

  saveCashBtn.addEventListener("click", () => {
    const amt = parseFloat(document.getElementById("cash-amount").value);
    const merchant = document.getElementById("cash-merchant").value.trim() || "Cash Expense";
    const cat = document.getElementById("cash-category").value;

    if (!isNaN(amt) && amt > 0) {
      transactions.unshift({
        id: Date.now(),
        amount: amt,
        type: "EXPENSE",
        categoryId: cat,
        merchant: merchant,
        accountName: "Cash",
        timestamp: Date.now(),
        isAuto: false
      });
      refreshUI();
      modal.classList.remove("active");
      document.getElementById("cash-amount").value = "";
      document.getElementById("cash-merchant").value = "";
    }
  });

  refreshUI();
});
