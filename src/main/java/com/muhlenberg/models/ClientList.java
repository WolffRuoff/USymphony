package com.muhlenberg.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientList {
    private ArrayList<Client> clients;

    public ClientList(HashMap<Long, Double> clientBreakdown) {
        this.clients = new ArrayList<Client>();
        for (Map.Entry<Long, Double> entry : clientBreakdown.entrySet()) {
            this.clients.add(new Client(entry.getKey(), entry.getValue()));
        }
    }
    public ArrayList<Client> getClients() {
        return this.clients;
    }

    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }

}
