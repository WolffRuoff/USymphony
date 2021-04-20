package com.muhlenberg.models;

public class Client {
    private Long userID;
    private Double amountInvested;
    
    public Client(Long userID, Double amountInvested) {
        this.userID = userID;
        this.amountInvested = amountInvested;
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

    //private Map<Long, Integer> portfolioAmounts;

}
