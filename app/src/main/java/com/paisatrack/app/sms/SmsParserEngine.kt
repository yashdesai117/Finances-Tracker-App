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
        // "towards SWIGGY on 24-Oct"
        Pattern.compile("(?:towards|at|paid to|to VPA|to)\\s+([A-Za-z0-9\\s._@\\-&]{2,35}?)(?=\\s+(?:on|ref|upi|avl|bal|clear|using|limit|via|through|ending|a/c|from|dated|\\.|,|$))", Pattern.CASE_INSENSITIVE),
        // "info: SWIGGY"
        Pattern.compile("(?:info|vpa):?\\s*([A-Za-z0-9._@\\-]{2,35})", Pattern.CASE_INSENSITIVE),
        // "VPA swiggy@icici"
        Pattern.compile("VPA\\s+([A-Za-z0-9._@\\-]{2,35})", Pattern.CASE_INSENSITIVE)
    )

    // Patterns for account/card number
    private val ACCOUNT_PATTERNS = listOf(
        Pattern.compile("(?:a/c|acct|account)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with\\s*)?)?([xX*]*\\d{3,4})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:card)\\s*(?:no\\.?)?\\s*(?:ending\\s*(?:with\\s*)?)?([xX*]*\\d{4})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([xX*]{2,}\\d{3,4})", Pattern.CASE_INSENSITIVE)
    )

    // Debit indicator keywords
    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid to", "transferred to", "withdrawn", "purchase of",
        "charged", "sent to", "txn of", "payment of", "debited by"
    )

    // Credit indicator keywords
    private val CREDIT_KEYWORDS = listOf(
        "credited", "received", "deposited", "salary credited", "refund of", "cashback of", "added to"
    )

    // Non-transaction filter (OTPs, promo alerts, failed attempts)
    private val IGNORE_KEYWORDS = listOf(
        "otp", "one time password", "verification code", "secret code", "do not share",
        "request to pay", "declined", "failed", "unsuccessful", "mandate created"
    )

    /**
     * Parses an incoming or historical SMS message.
     * Returns ParsedTransaction if it represents a valid completed bank/financial transaction, or null otherwise.
     */
    fun parse(sender: String, body: String, timestamp: Long = System.currentTimeMillis()): ParsedTransaction? {
        val lowerBody = body.lowercase(Locale.ROOT)

        // 1. Check if this is an OTP or non-completed authorization
        for (ignore in IGNORE_KEYWORDS) {
            if (lowerBody.contains(ignore)) {
                return null
            }
        }

        // 2. Determine Transaction Type (Debit/Expense vs Credit/Income)
        val isDebit = DEBIT_KEYWORDS.any { lowerBody.contains(it) }
        val isCredit = CREDIT_KEYWORDS.any { lowerBody.contains(it) }

        if (!isDebit && !isCredit) {
            // Not a financial transaction
            return null
        }

        val type = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

        // 3. Extract Amount
        val amount = extractAmount(body) ?: return null
        if (amount <= 0.0) return null

        // 4. Extract Merchant / Party
        val merchant = extractMerchant(body, sender)

        // 5. Extract Account / Card info
        val accountName = extractAccount(body, sender)

        // 6. Auto-classify Category
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

        // Clean sender as fallback (e.g. "HDFCBK" -> "HDFC Bank")
        return formatSenderAsMerchant(sender)
    }

    private fun cleanMerchantName(raw: String): String {
        return raw.replace(Regex("^(towards|at|to|info:?)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[.,;:_]+$"), "")
            .trim()
            .split(" ")
            .take(4) // Keep at most 4 words
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
