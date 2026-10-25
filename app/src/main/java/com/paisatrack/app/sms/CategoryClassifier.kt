package com.paisatrack.app.sms

import java.util.Locale

object CategoryClassifier {

    private val keywordMapping = mapOf(
        "food" to listOf(
            "swiggy", "zomato", "starbucks", "mcdonald", "domino", "pizza", "burger",
            "kfc", "subway", "cafe", "restaurant", "dining", "bakery", "eats", "barbeque",
            "biryani", "chai", "tea", "haldiram", "food"
        ),
        "groceries" to listOf(
            "blinkit", "zepto", "instamart", "bigbasket", "dmart", "grofers", "spencers",
            "supermarket", "provision", "grocery", "groceries", "fresh", "milk", "kirana",
            "nature basket", "country delight"
        ),
        "shopping" to listOf(
            "amazon", "flipkart", "myntra", "ajio", "zara", "h&m", "meesho", "nykaa",
            "tata cliq", "decathlon", "lifestyle", "westside", "pantaloons", "max fashion",
            "retail", "shopping", "clothing", "apparel"
        ),
        "transport" to listOf(
            "uber", "ola", "rapido", "metro", "petrol", "fuel", "indian oil", "iocl",
            "bharat petroleum", "bpcl", "hpcl", "shell", "irctc", "makemytrip", "cleartrip",
            "indigo", "air india", "fastag", "toll", "auto", "cab", "parking"
        ),
        "bills" to listOf(
            "electricity", "bescom", "tneb", "torrent", "water", "broadband", "wifi",
            "airtel", "jio", "vodafone", "idea", "vi", "bsnl", "gas", "igl", "mgl",
            "adani electricity", "tatapower", "dth", "tata sky", "dish tv", "recharge",
            "utility", "rent", "maintenance"
        ),
        "entertainment" to listOf(
            "netflix", "spotify", "bookmyshow", "pvr", "inox", "primevideo", "hotstar",
            "disney", "youtube", "cinema", "movie", "steam", "playstation", "gaming", "apple music"
        ),
        "health" to listOf(
            "apollo", "pharmeasy", "1mg", "tata 1mg", "medplus", "netmeds", "clinic",
            "hospital", "pharmacy", "medical", "diagnostic", "dr.", "doctor", "lab", "dental"
        ),
        "investments" to listOf(
            "zerodha", "groww", "angelone", "upstox", "mutual fund", "sip", "kuvera",
            "coin", "securities", "camsonline", "kfintech", "fixed deposit", "fd", "ppf"
        ),
        "salary" to listOf(
            "salary", "payroll", "stipend", "bonus", "wages", "employer"
        ),
        "cashback" to listOf(
            "cashback", "refund", "reversal", "reward"
        )
    )

    fun classify(merchant: String, body: String, isIncome: Boolean): String {
        if (isIncome) {
            val lowerBody = body.lowercase(Locale.ROOT)
            if (lowerBody.contains("salary") || lowerBody.contains("payroll") || lowerBody.contains("stipend")) {
                return "salary"
            }
            if (lowerBody.contains("cashback") || lowerBody.contains("refund") || lowerBody.contains("reversal")) {
                return "cashback"
            }
            return "salary" // Default income category
        }

        val combinedText = "${merchant.lowercase(Locale.ROOT)} ${body.lowercase(Locale.ROOT)}"

        for ((category, keywords) in keywordMapping) {
            for (kw in keywords) {
                if (combinedText.contains(kw)) {
                    return category
                }
            }
        }

        return "other"
    }
}
