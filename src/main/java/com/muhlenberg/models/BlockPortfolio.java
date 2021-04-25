package com.muhlenberg.models;

public class BlockPortfolio {
    private Portfolio portfolio;
    private String name;
    private Double maxPercent;
    
    public BlockPortfolio (Portfolio port, Double maxP) {
        this.portfolio = port;
        this.name = port.getName();
        this.maxPercent = maxP * 100;
    }
    public Portfolio getPortfolio() {
        return this.portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxPercent() {
        return this.maxPercent;
    }

    public void setMaxPercent(Double maxPercent) {
        this.maxPercent = maxPercent;
    }

}
