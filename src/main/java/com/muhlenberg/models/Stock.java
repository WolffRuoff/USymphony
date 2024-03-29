package com.muhlenberg.models;

public class Stock implements java.io.Serializable{
    private static final long serialVersionUID = 6529685098267757690L;
    private String symbol;
    private String companyName;
    private double purchasePrice;
    private double latestPrice;
    private double change;
    private boolean isLargeCap;


    public Stock(String symbol, String companyName, double purchasePrice, double latestPrice, double change, boolean isLargeCap) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.purchasePrice = purchasePrice;
        this.latestPrice = latestPrice;
        this.change = change;
        this.isLargeCap = isLargeCap;
    }


    public double getPurchasePrice() {
        return this.purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getLatestPrice() {
        return this.latestPrice;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public double getChange() {
        return this.change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public boolean isIsLargeCap() {
        return this.isLargeCap;
    }

    public boolean getIsLargeCap() {
        return this.isLargeCap;
    }

    public void setIsLargeCap(boolean isLargeCap) {
        this.isLargeCap = isLargeCap;
    }

}
