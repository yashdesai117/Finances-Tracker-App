package com.paisatrack.app.sms

import com.paisatrack.app.data.model.TransactionType
import java.util.Locale
import java.util.regex.Pattern

object SmsParserEngine {

    // Regex patterns for amounts
    // e.g. Rs. 450, Rs 1,250.00, INR 899.50, ₹340
    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:(?:RS\\.?|INR|₹)\\s*(\\d[\\d,]*(?:\\.\\d{1,2})?))|(\\d[\\d,]*(?:\\.\\d{1,2})?)\\s*(?:RS\\.?|INR|₹)",
        Pattern.CASE_INSENSITIVE
    )

    // Patterns for merchant extraction
    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?:towards|at|paid to|to VPA|to)\\s+([A-Za-z0-9\\s._@\\-&]{2,35}?)(?=\\s+(?:on|ref|upi|avl|bal|clear|using|limit|via|through|ending|a/c|from|dated|\\.|,|$))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:info|vpa):?\\s*([A-Za-z0-9._@\\-]{2,35})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("VPA\\s+([A-Za-z0-9._@\\-]{2,35})", Pattern.CASE_INSENSITIVE)
    )

    // Patterns for account/card number
    private val ACCOUNT_PATTERNS = listOf(
        Pattern.compile("(?:a/c|acct|account)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with\\s*)?)?([xX*]*\\d{3,4})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:card)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with\\s*)?)?([xX*]*\\d{4})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([xX*]{2,}\\d{3,4})", Pattern.CASE_INSENSITIVE)
    )

    // Debit indicator keywords (Completed expenses)
    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid to", "transferred to", "withdrawn", "purchase of",
        "charged", "sent to", "txn of", "payment of", "debited by"
    )

    // Credit indicator keywords (Completed real deposits/income)
    private val CREDIT_KEYWORDS = listOf(
        "credited", "deposited", "salary credited", "refund of", "cashback of", "added to your bank",
        "has been credited to your", "received in your a/c"
    )

    // Mandatory bank/financial indicators for credit messages (to reject fake promo credits)
    private val BANK_CREDIT_INDICATORS = listOf(
        "a/c", "acct", "account", "ending", "card", "upi ref", "imps", "neft", "rtgs",
        "avl bal", "available bal", "clear bal", "balance", "bank", "salary", "payroll"
    )

    // Aggressive filters for promotional alerts, discount coupons, fake credits, loans, and OTPs
    private val PROMO_AND_SPAM_KEYWORDS = listOf(
        // Coupon / Discount words
        "off", "up to", "coupon", "voucher", "discount", "use code", "code:", "apply code",
        "code eazydiner", "promo code", "promotional", "cashback offer", "reward points",
        "loyalty points", "bonus points", "bonus cash", "game cash", "claim now", "claim your",
        "hurry", "valid till", "expires on", "tap here", "click here", "bit.ly", "t.co",
        "w.ezyd.in", "free delivery", "order now", "flat rs", "save rs", "save up to",
        "win up to", "earn up to", "gift card", "spin and win",
        // Loan / Marketing
        "pre-approved", "apply for loan", "loan of rs", "instant loan", "zero interest",
        "credit limit increased", "upgrade your card", "insurance",
        // OTPs & Non-completed authorizations
        "otp", "one time password", "verification code", "secret code", "do not share",
        "request to pay", "declined", "failed", "unsuccessful", "mandate created"
    )

    /**
     * Parses an incoming or historical SMS message.
     * Returns ParsedTransaction if it represents a valid completed bank/financial transaction, or null otherwise.
     */
    fun parse(sender: String, body: String, timestamp: Long = System.currentTimeMillis()): ParsedTransaction? {
        val lowerBody = body.lowercase(Locale.ROOT)

        // 1. Check if this is promotional, coupon discount, spam, or OTP
        for (spam in PROMO_AND_SPAM_KEYWORDS) {
            if (lowerBody.contains(spam)) {
                return null
            }
        }

        // 2. Check for debit vs credit
        val isDebit = DEBIT_KEYWORDS.any { lowerBody.contains(it) }
        val isCreditCandidate = CREDIT_KEYWORDS.any { lowerBody.contains(it) }

        if (!isDebit && !isCreditCandidate) {
            return null
        }

        // 3. Strict verification for credit/income messages:
        // Must contain an actual bank account, card, reference number, or balance update.
        // This guarantees that app coupon credits like "Your EazyDiner account credited with up to Rs 300" are rejected.
        val type: TransactionType
        if (isDebit) {
            type = TransactionType.EXPENSE
        } else {
            val hasBankContext = BANK_CREDIT_INDICATORS.any { lowerBody.contains(it) }
            if (!hasBankContext) {
                // No bank account or transaction reference found; reject as marketing credit
                return null
            }
            type = TransactionType.INCOME
        }

        // 4. Extract Amount
        val amount = extractAmount(body) ?: return null
        if (amount <= 0.0) return null

        // 5. Extract Merchant / Party
        val merchant = extractMerchant(body, sender)

        // 6. Extract Account / Card info
        val accountName = extractAccount(body, sender)

        // 7. Auto-classify Category
        val categoryId = CategoryClassifier.classify(merchant, body, isIncome = (type == TransactionType.INCOME))

        return ParsedTransaction(
            amount = amount,
            type = type,
            merchant = merchant,
            accountName = accountName,
            timestamp = timestamp,
            rawSms = body,
            sender = sender,
            categoryId = categoryId,
            isAutoCategorized = true
        )
    }

    private fun extractAmount(body: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(body)
        if (matcher.find()) {
            val raw = matcher.group(1) ?: matcher.group(2) ?: return null
            val clean = raw.replace(",", "").trim()
            return clean.toDoubleOrNull()
        }
        return null
    }

    private fun extractMerchant(body: String, sender: String): String {
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim() ?: ""
                val clean = cleanMerchantName(candidate)
                if (clean.isNotBlank() && clean.length > 1) {
                    return clean
                }
            }
        }

        return formatSenderAsMerchant(sender)
    }

    private fun cleanMerchantName(raw: String): String {
        return raw.replace(Regex("^(towards|at|to|info:?)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[.,;:_]+$"), "")
            .trim()
            .split(" ")
            .take(4)
            .joinToString(" ")
            .capitalizeWords()
    }

    private fun extractAccount(body: String, sender: String): String {
        for (pattern in ACCOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val acc = matcher.group(1)?.trim()
                if (!acc.isNullOrBlank()) {
                    return "A/C $acc"
                }
            }
        }
        return formatSenderAsMerchant(sender)
    }

    private fun formatSenderAsMerchant(sender: String): String {
        val clean = sender.replace(Regex("^[A-Za-z]{2}-"), "").trim()
        return when {
            clean.contains("HDFC", ignoreCase = true) -> "HDFC Bank"
            clean.contains("SBI", ignoreCase = true) -> "SBI"
            clean.contains("ICICI", ignoreCase = true) -> "ICICI Bank"
            clean.contains("AXIS", ignoreCase = true) -> "Axis Bank"
            clean.contains("KOTAK", ignoreCase = true) -> "Kotak Bank"
            clean.contains("PAYTM", ignoreCase = true) -> "Paytm"
            clean.contains("GPAY", ignoreCase = true) || clean.contains("GOOGLE", ignoreCase = true) -> "Google Pay"
            clean.contains("PHONEPE", ignoreCase = true) -> "PhonePe"
            clean.contains("CRED", ignoreCase = true) -> "Cred"
            clean.isNotBlank() -> clean.uppercase(Locale.ROOT)
            else -> "Bank Account"
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }
}
