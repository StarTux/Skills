package com.cavetale.skills.util;

import java.text.DecimalFormat;

public final class Text {
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.####");

    public static String formatDouble(double in) {
        return DOUBLE_FORMAT.format(in);
    }

    private Text() { }
}
