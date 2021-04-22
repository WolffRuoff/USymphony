package com.muhlenberg.models;

public class OrderDetails {
    private String portName;
    private String ticker;
    private Double shares;
    private Double price;
    private Double orderAmount;
    
    public OrderDetails(Portfolio port, String ticker, Double shares, Double price, Double orderAmount) {
        this.portName = port.getName();
        this.ticker = ticker;
        this.shares = Math.round(shares * 1000.0) / 1000.0;
        this.price = Math.round(price * 100.0) / 100.0;
        this.orderAmount = Math.round(orderAmount * 100.0) / 100.0;
    }
    
    public String getPortName() {
        return this.portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
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
