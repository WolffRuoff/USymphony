package com.muhlenberg.models;

import java.util.ArrayList;

public class AddClients {
    private String name;
    private String ticker;
    private ArrayList<Long> clients;
    
    public AddClients(String name, String ticker, ArrayList<Long> clients) {
        this.name = name;
        this.ticker = ticker;
        this.clients = clients;
    }
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return this.ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public ArrayList<Long> getClients() {
        return this.clients;
    }

    public void setClients(ArrayList<Long> clients) {
        this.clients = clients;
    }

}
