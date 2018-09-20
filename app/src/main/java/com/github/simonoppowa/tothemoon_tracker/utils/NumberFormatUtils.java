package com.github.simonoppowa.tothemoon_tracker.utils;

public class NumberFormatUtils {

    public static String format2Decimal(double price) {
        String formattedNum = String.format("%.2f", price);
        return formattedNum.replace(".", ",");
    }
}
