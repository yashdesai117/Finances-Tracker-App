package com.paisatrack.app.sms

import com.paisatrack.app.data.model.TransactionType
import org.junit.Assert.*
import org.junit.Test

class SmsParserEngineTest {

    @Test
    fun testHdfcUpiDebit() {
        val sms = "Dear Customer, INR 450.00 debited from A/C **1234 on 24-Oct-26 towards SWIGGY. UPI Ref 328492019482. Avl Bal: INR 45,210.00."
        val parsed = SmsParserEngine.parse("VM-HDFCBK", sms)

        assertNotNull("Should parse valid HDFC UPI debit", parsed)
        assertEquals(450.0, parsed!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("food", parsed.categoryId)
        assertTrue(parsed.merchant.contains("Swiggy", ignoreCase = true))
    }

    @Test
    fun testSbiDebitCardSpend() {
        val sms = "Your A/C ending with 5678 is debited by Rs. 2,350.00 on 22-Oct-26 at BLINKIT GROCERY. Avl Bal Rs 18,200.00."
        val parsed = SmsParserEngine.parse("AD-SBIINB", sms)

        assertNotNull("Should parse SBI debit", parsed)
        assertEquals(2350.0, parsed!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("groceries", parsed.categoryId)
    }

    @Test
    fun testSalaryCredit() {
        val sms = "Your A/C **1234 has been credited with INR 75,000.00 on 01-Oct-26 by SALARY / ACME TECH PVT LTD. Avl Bal: INR 92,400.00."
        val parsed = SmsParserEngine.parse("VM-HDFCBK", sms)

        assertNotNull("Should parse salary credit", parsed)
        assertEquals(75000.0, parsed!!.amount, 0.01)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("salary", parsed.categoryId)
    }

    @Test
    fun testIgnoreEazyDinerPromoCredit() {
        // Real message from user's screenshot
        val promoSms = "Hi, Your EazyDiner account has been credited with up to Rs.300 off Pay your restaurant bill on the app and use code EAZYDINER. Tap here: https://w.ezyd.in/EDINER/bmsGI6"
        val parsed = SmsParserEngine.parse("TX-EDINER-S", promoSms)

        assertNull("Promotional discount offers with 'up to', 'off', 'code' must NEVER be parsed as credit", parsed)
    }

    @Test
    fun testIgnoreCouponOffer() {
        val couponSms = "Flat Rs 200 off on your next Domino's order! Use code CRICKET at checkout. Order now!"
        val parsed = SmsParserEngine.parse("VM-DOMINO", couponSms)

        assertNull("Discount coupon must be ignored", parsed)
    }

    @Test
    fun testIgnoreOtpMessage() {
        val otpSms = "124859 is your secret OTP for transaction of INR 500.00 at PayTM. Do NOT share this OTP with anyone."
        val parsed = SmsParserEngine.parse("VM-HDFCBK", otpSms)

        assertNull("OTP message must NOT be recorded as a transaction", parsed)
    }
}
