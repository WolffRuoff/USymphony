package com.muhlenberg.models;

import lombok.Data;

@Data
public class Stock {
    private String symbol;
    private String companyName;
    private double latestPrice;
    private double change;
    private boolean isLargeCap;
}
