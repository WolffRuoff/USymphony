package com.muhlenberg.models;

public class Client {
    private Long userID;
    private Double amountInvested;
    private String portName;
    
    public Client(Long userID, Double amountInvested) {
        this.userID = userID;
        this.amountInvested = amountInvested;
        this.portName = "";
    }
    public Client(Long userID, Double amountInvested, String portName) {
        this.userID = userID;
        this.amountInvested = amountInvested;
        this.portName = portName;
    }
    
    public Long getUserID() {
        return this.userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Double getAmountInvested() {
        return this.amountInvested;
    }

    public void setAmountInvested(Double amountInvested) {
        this.amountInvested = amountInvested;
    }

    public String getPortName() {
        return this.portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

}
