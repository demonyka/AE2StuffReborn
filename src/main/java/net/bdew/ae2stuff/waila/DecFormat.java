package net.bdew.ae2stuff.waila;

import java.text.DecimalFormat;

final class DecFormat {
    private DecFormat() {
    }

    static String shortFormat(double value) {
        String pattern = value > 10 ? "#,##0" : value > 1 ? "#,##0.0" : "#,##0.00";
        return new DecimalFormat(pattern).format(value).replace(' ', ' ');
    }
}
