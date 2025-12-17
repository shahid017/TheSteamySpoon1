package com.subdue.thesteamyspoon.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    /**
     * Get currency formatter for PKR (Pakistani Rupees)
     */
    fun getPKRFormatter(): NumberFormat {
        val locale = Locale("en", "PK")
        return NumberFormat.getCurrencyInstance(locale)
    }
}

