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
    fun testIciciCreditCard() {
        val sms = "Alert: You have spent INR 1,899.00 on your ICICI Bank Credit Card ending 9012 at AMAZON INDIA on 20-Oct-26."
        val parsed = SmsParserEngine.parse("VK-ICICIB", sms)

        assertNotNull("Should parse ICICI Credit Card spend", parsed)
        assertEquals(1899.0, parsed!!.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("shopping", parsed.categoryId)
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
    fun testIgnoreOtpMessage() {
        val otpSms = "124859 is your secret OTP for transaction of INR 500.00 at PayTM. Do NOT share this OTP with anyone."
        val parsed = SmsParserEngine.parse("VM-HDFCBK", otpSms)

        assertNull("OTP message must NOT be recorded as a transaction", parsed)
    }

    @Test
    fun testIgnorePromoSpam() {
        val promoSms = "Get pre-approved personal loan of Rs 5,00,000 at zero interest. Click here to apply now!"
        val parsed = SmsParserEngine.parse("AX-LOANS", promoSms)

        assertNull("Promotional text must be ignored", parsed)
    }
}
