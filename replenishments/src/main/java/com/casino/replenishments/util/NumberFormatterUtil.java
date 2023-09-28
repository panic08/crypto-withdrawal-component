package com.casino.replenishments.util;

public class NumberFormatterUtil {
    public static double formatDouble(double number, int decimalPlaces) {
        double multiplier = Math.pow(10, decimalPlaces);

        return Math.floor(number * multiplier) / multiplier;
    }
}
