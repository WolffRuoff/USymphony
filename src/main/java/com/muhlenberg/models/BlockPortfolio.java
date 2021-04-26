package com.muhlenberg.models;

public class BlockPortfolio {
    private Portfolio portfolio;
    private String name;
    private Double percent;
    
    public BlockPortfolio (Portfolio port, Double maxP) {
        this.portfolio = port;
        this.name = port.getName();
        this.percent = maxP;
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

    public Double getPercent() {
        return this.percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

}
