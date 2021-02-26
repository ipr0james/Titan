package net.thenova.titan.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Copyright 2020 ipr0james
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public final class UNumber {

    private static final NumberFormat BALANCE_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);

    static {
        BALANCE_FORMAT.setMinimumFractionDigits(0);
        BALANCE_FORMAT.setMaximumFractionDigits(2);
    }

    /**
     * Format a number into string of number
     *
     * @param amount Amount to be formatted
     * @return Return formatted string
     */
    public static String convert(double amount) {
        amount = Math.round(amount);

        if (amount < 1000.0D) {
            return format(amount);
        }
        if (amount < 1000000.0D) {
            return format(amount / 1000.0D) + "K";
        }
        if (amount < 1.0E9D) {
            return format(amount / 1000000.0D) + "M";
        }
        if (amount < 1.0E12D) {
            return format(amount / 1.0E9D) + "B";
        }
        if (amount < 1.0E15D) {
            return format(amount / 1.0E12D) + "T";
        }
        if (amount < 1.0E18D) {
            return format(amount / 1.0E15D) + "Q";
        }

        return format(amount);
    }

    /**
     * Convert decimal/double value to 2 decimal places
     *
     * @param value Double value to be formatted
     * @return Return string with 2 decimal places
     */
    public static String format(final double value) {
        return BALANCE_FORMAT.format(value);
    }

    /**
     * Convert a string to a BigDecimal value
     *
     * @param input Value to be converted
     * @param def Default to be returned if default is invalid
     * @return Return a BigDecimal object, either the input or default.
     */
    public static BigDecimal toBigDecimal(final String input, final BigDecimal def) {
        if (input == null || input.isEmpty()) {
            return def;
        }

        try {
            return new BigDecimal(input, MathContext.DECIMAL128);
        } catch (final NumberFormatException | ArithmeticException e) {
            return def;
        }
    }

    /**
     * Format a string with the suffix of time
     * Full format look.
     *
     * @param sec Long duration being converted
     * @return Return pretty format string.
     */
    public static String getTimeFull(long sec) {
        long day = 0;
        long min = 0;
        long hr = 0;

        while(sec >= 86400) {
            day++;
            sec = sec - 86400;
        }

        while(sec >= 3600) {
            hr++;
            sec = sec - 3600;
        }

        while(sec >= 60) {
            min++;
            sec = sec - 60;
        }

        String builder = "";

        if(day >= 1) {
            builder += day + ((day == 1) ? " day, " : " days, ");
        }

        if(hr >= 1) {
            builder += hr + ((hr == 1) ? " hour, " : " hours, ");
        }

        if(min >= 1) {
            builder += min + ((min == 1) ? " minute" : " minutes");
        }

        if((day >= 1 || hr >= 1 || min >= 1) && sec >= 1) {
            builder += " and ";
        }

        if(sec >= 1) {
            builder += sec + ((sec == 1) ? " second." : " seconds.");
        }

        return builder;
    }

    /**
     * Format a string with the suffix of time
     * Minimalist format look.
     *
     * @param sec Long duration being converted
     * @return Return pretty format string.
     */
    public static String getTimeShort(long sec) {
        long day = 0;
        long min = 0;
        long hr = 0;

        while (sec >= 86400) {
            day++;
            sec = sec - 86400;
        }

        while (sec >= 3600) {
            hr++;
            sec = sec - 3600;
        }

        while (sec >= 60) {
            min++;
            sec = sec - 60;
        }

        String builder = "";

        if (day >= 1) {
            builder += day + "d, ";
        }

        if (hr >= 1) {
            builder += hr + "h, ";
        }

        if (min >= 1) {
            builder += min + "m, ";
        }

        return builder + sec + "s";
    }

}
