package com.muhlenberg.models;

import java.io.IOException;
import java.util.ArrayList;

public class BlockOrderDetails {
    private ArrayList<BlockPortfolio> portList = new ArrayList<BlockPortfolio>();
    private String ticker;
    private Double shares;
    private Double price;
    private Double orderAmount;
    private Double totalPurchasePower;

    public BlockOrderDetails(ArrayList<BlockPortfolio> blockList, String ticker, Double shares, Double price,
            Double orderAmount, int ignore) {
        this.portList = blockList;
        this.ticker = ticker;
        this.shares = shares;
        this.price = price;
        this.totalPurchasePower = null;
    }

    public BlockOrderDetails(ArrayList<Portfolio> ports, String ticker, Double shares, Double price,
            Double orderAmount) {
        Double liquidPercent;
        Double liquidAmount;
        this.totalPurchasePower = 0.0;
        for (Portfolio port : ports) {
            try {
                port.rebalancePortfolio();
            } catch (IOException e) {
                e.printStackTrace();
            }
            liquidAmount = port.getPortionLiquid() * port.getSize();
            liquidPercent = (liquidAmount / orderAmount) * 100.0;
            if(liquidPercent > 100.0) {
                liquidPercent = 100.00;
            }
            this.portList.add(new BlockPortfolio(port, liquidPercent));
            this.totalPurchasePower += liquidAmount;
        }
        this.ticker = ticker;
        this.shares = shares;
        this.price = price;
        this.orderAmount = orderAmount;
    }

    public ArrayList<BlockPortfolio> getPortList() {
        return this.portList;
    }

    public void setPortList(ArrayList<BlockPortfolio> portList) {
        this.portList = portList;
    }

    public String getTicker() {
        return this.ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getShares() {
        return this.shares;
    }

    public void setShares(Double shares) {
        this.shares = shares;
    }

    public Double getTotalPurchasePower() {
        return this.totalPurchasePower;
    }

    public void setTotalPurchasePower(Double totalPurchasePower) {
        this.totalPurchasePower = totalPurchasePower;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOrderAmount() {
        return this.orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }
}
