package com.muhlenberg.bot;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Handlebars.SafeString;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
    public boolean isNotClient(String option) {
        if (!option.equals("client")) {
            return true;
        }
        return false;
    }

    public SafeString pieChart(double value) {
        String fillStyle = "";
        int size = 7;

        String pieChart =" <div style=\"width:%dem;height:%dem;border-radius:50%%;margin:auto;background:conic-gradient(%s)\"></div> ";
        double percentage;
        if (value < 1) {
            percentage = value*100;
        }
        else {
            percentage = value;
        }
        fillStyle = String.format("%s %d%%,%s %f%%,", colours.get(3), 0, colours.get(3), percentage);
        fillStyle += String.format("%s 0%%,%s 100%%", colours.get(0), colours.get(0));

        //Return Handlebars SafeString so that handlebars does not auto escape all html characters
        return new Handlebars.SafeString(String.format(pieChart, size, size, fillStyle));
        }


    public SafeString gains(String value) {

    String formatting; 
    
    //Determine if pos or neg
    String cleanStr = value.replaceAll("^[ \t$]+ |[,]", "");
    double d = Double.parseDouble(cleanStr);


    if (d < 0) {
        formatting = "$<span style=\"color:red;\"> %s </span> ";
    }
    else {
        formatting = "$<span style=\"color:green;\"> +%s </span> ";
    }

        //Return Handlebars SafeString so that handlebars does not auto escape all html characters
    return new Handlebars.SafeString(String.format(formatting, value));
    }

    public SafeString monoProgressBar(double val) {

    String progressBar = "<div style=\"border:#999 1px solid;padding:.15em;border-radius:0.4em;\"> <div style=\"background:repeating-linear-gradient(-45deg,rgba(221,221,221,.4),rgba(221,221,221,.4) .5em,rgba(187,187,187,.2) .5em,rgba(187,187,187,.2) 1em);border-radius:0.3em;\"> <div style=\"background:repeating-linear-gradient(-45deg,#a7d4e8,#a7d4e8 .5em,#38b1e8 .5em,#38b1e8 1em);height:.6em;width:%d%%;border-radius:0.3em;\"> </div> </div> </div>";

        int percentage;
        if (val < 1) {
            percentage = (int) Math.round(val * 100);
        }
        else {
            percentage = (int) Math.round(val);
        }
        return new Handlebars.SafeString(String.format(progressBar, percentage));
    };


    public SafeString round(int decimal, double value) {
        /* String times = "1";
        for (int i = 0; i < decimal; i++) {
            times = times + "0";        
        } */
        NumberFormat rounder = NumberFormat.getNumberInstance(Locale.US);
        rounder.setMinimumFractionDigits(decimal);
        rounder.setMaximumFractionDigits(decimal);
        return new Handlebars.SafeString(rounder.format(value));
    };


    public SafeString getChange(int decimal, double current, double purchase) {
        String outputString;
        String times = "1";
        for (int i = 0; i < decimal; i++) {
            times = times + "0";        
        }

        DecimalFormat df = new DecimalFormat("#."+('0'*decimal));
        double val = (current - purchase) / purchase;

        if (val > 0) {
            outputString = "<emoji shortcode = \"chart_with_upwards_trend\"/>  <span style=\"color:green;\"> +"+df.format(val) +"% </span>";
        }
        else if (val < 0) {
            outputString = "<emoji shortcode = \"chart_with_downwards_trend\"/>  <span style=\"color:red;\">"+df.format(val) + "% </span>";
        }
        else {
            outputString = "No Change";
        }

        return new Handlebars.SafeString(outputString);
    };

    // Other helper methods
}