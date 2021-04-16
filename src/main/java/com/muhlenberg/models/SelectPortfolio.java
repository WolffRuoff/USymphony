package com.muhlenberg.models;

import java.util.ArrayList;

public class SelectPortfolio {
    private String option;
    private ArrayList<Portfolio> portfolioList;

    public SelectPortfolio(String option, ArrayList<Portfolio> portfolioList) {
        this.option = option;
        this.portfolioList = portfolioList;
    }
    
    public String getOption() {
        return this.option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public ArrayList<Portfolio> getPortfolioList() {
        return this.portfolioList;
    }

    public void setPortfolioList(ArrayList<Portfolio> portfolioList) {
        this.portfolioList = portfolioList;
    }


    
}
