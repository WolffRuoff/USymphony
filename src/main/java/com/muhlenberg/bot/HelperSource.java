package com.muhlenberg.bot;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Handlebars.SafeString;

import java.util.List;

public class HelperSource {
    private static final List<String> colours = List.of("#3d85c6", "#e69138", "#a64d79", "#6aa84f");

    public boolean isPos(double context) {
        if (context > 0 ) {
            return true;
        }
        else { return false;}
    }
    public boolean isNeg(double context) {
        if (context < 0 ) {
            return true;
        }
        else { return false;}
    }

    public SafeString pieChart(double value) {
        String fillStyle = "";
        int size = 7;

        String pieChart =" <div style=\"width:%dem;height:%dem;border-radius:50%%;margin:auto;background:conic-gradient(%s)\"></div> ";

        double percentage = value;
        fillStyle = String.format("%s %d%%,%s %f%%,", colours.get(3), 0, colours.get(3), percentage);
        fillStyle += String.format("%s 0%%,%s 100%%", colours.get(0), colours.get(0));

        //Return Handlebars SafeString so that handlebars does not auto escape all html characters
        return new Handlebars.SafeString(String.format(pieChart, size, size, fillStyle));
        }
    // Other helper methods
}