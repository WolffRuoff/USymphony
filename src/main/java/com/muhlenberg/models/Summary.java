package com.muhlenberg.models;

public class Summary {

    private Stock[] top10Stocks;
    private Stock[] bottom10Stocks;
    private String name;
    private double portionLiquid;
    private double size;
    private String comparisonTicker;
    private double compPercent;
    private String evaluation;
    private String unrealizedGains;

    public Summary(Portfolio p) {
        //Update the portfolio values before creating summary
        try{
            p.updateStocks();
            this.compPercent = p.getCompPercent();
        } catch (Exception e) {
            System.out.println(e);
        }

        this.unrealizedGains = String.format("%1$,.2f", p.getUnrealized());
        this.top10Stocks = p.getTop5();
        this.bottom10Stocks = p.getBottom5();
        this.name = p.getName();
        this.portionLiquid = Math.round( (1d - p.getPortionLiquid()) * 100) / 100;
        this.size = p.getSize();
        this.comparisonTicker= p.getMainComparison();
        this.evaluation = String.format("%1$,.2f", p.getEvaluation());
    }


    public String getUnrealizedGains() {
        return this.unrealizedGains;
    }
    

    public String getEvaluation() {
        return this.evaluation;
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
