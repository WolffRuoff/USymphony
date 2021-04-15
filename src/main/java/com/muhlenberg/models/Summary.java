package com.muhlenberg.models;

public class Summary {

    private Stock[] top10Stocks;
    private Stock[] bottom10Stocks;
    private String name;
    private double portionLiquid;
    private double size;
    private String comparisonTicker;
    private double compPercent;


    public Summary(Portfolio p) {
        this.top10Stocks = p.getTop5();
        this.bottom10Stocks = p.getBottom5();
        this.name = p.getName();
        this.portionLiquid = p.getPortionLiquid();
        this.size = p.getSize();
        this.comparisonTicker= p.getMainComparison();
        this.compPercent = p.getCompPercent();
    }

    public String getComparisonTicker() {
        return this.comparisonTicker;
    }

    public double getCompPercent() {
        return this.compPercent;
    }

    public Stock[] getTop10Stocks() {
        return this.top10Stocks;
    }

    public Stock[] getBottom10Stocks() {
        return this.bottom10Stocks;
    }

    public String getName() {
        return this.name;
    }

    public double getPortionLiquid() {
        return this.portionLiquid * 100;
    }

    public double getSize() {
        return this.size;
    }

}
