package com.muhlenberg.models;
import java.util.HashMap;
import com.symphony.bdk.gen.api.model.V4User;

import java.lang.Double;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class Portfolio {
    private @Setter String name;
    private float size;
    private Double portionLiquid;
    private HashMap<V4User, Double> clientBreakdown; //userID, client
    private HashMap<Stock, Double> assets;
    //private Map<Float, portHistory> history;


    public Portfolio(String name, float size, Double portionLiquid, HashMap<V4User,Double> clientBreakdown, HashMap<Stock,Double> assets) {
        this.name = name;
        this.size = size;
        this.portionLiquid = portionLiquid;
        this.clientBreakdown = clientBreakdown;
        this.assets = assets;
    }

    public Stock[] getBottom5() {
        ArrayList<Stock> list = new ArrayList<Stock>();
        for (Stock key : assets.keySet()) {
            list.add(key);
        }
        Collections.sort(list, new Comparator<Stock>() {
            @Override
            public int compare(Stock c1, Stock c2) {
                return Double.compare(c1.getChange(), c2.getChange());
            }});
        
        if (list.size() < 5) {
            Stock[] arr = new Stock[list.size()];
            arr = list.toArray(arr);
            return arr;
        }
        else {
            Stock[] arr = new Stock[5];
            for (int i = 0; i < 5; i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }
    }

    //change so that top movers are not also in bottom movers.
    public Stock[] getTop5() {
                ArrayList<Stock> list = new ArrayList<Stock>();
        for (Stock key : assets.keySet()) {
            list.add(key);
        }
        //Order is swapped in compare to get lower stocks
        Collections.sort(list, new Comparator<Stock>() {
            @Override
            public int compare(Stock c1, Stock c2) {
                return Double.compare(c2.getChange(), c1.getChange());
            }});
        
        if (list.size() < 5) {
            Stock[] arr = new Stock[list.size()];
            arr = list.toArray(arr);
            return arr;
        }
        else {
            Stock[] arr = new Stock[5];
            for (int i = 0; i < 5; i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }
    }
    public void addAsset(Stock n, Double am) {
        //If asset already exists add
        if(this.assets.containsKey(n)){
            this.assets.put(n, Double.sum(am, this.assets.get(n)));
        }
        //Create new asset
        else{
            this.assets.put(n, am);
        }
        this.rebalancePortfolio();
    }
    public void removeAsset(Stock n, Double am) {
        //Make sure asset exists
        if(this.assets.containsKey(n)){
            if(Double.compare(am, this.assets.get(n)) >= 0 ) {
                this.assets.remove(n);
            }
            else{
                this.assets.put(n, this.assets.get(n) - am);
            }
            this.rebalancePortfolio();
        }
        else{
            //Return an error that asset doesn't exist
        }
    }
    private void rebalancePortfolio(){
        //rebalances portfolio
    }
    //Absolutely critical to override and return stock array. DO NOT DELETE
    public Stock[] getAssets() {
        int i = 0;
        Stock[] arr  = new Stock[assets.size()];

        for (Stock key : assets.keySet()) {
            arr[i] = key;
            i++;
        }   
        return arr;
    }

    public void addClient(V4User user, Double am) {
        // Make sure client doesn't exist
        if (!this.clientBreakdown.containsKey(user)) {
            this.clientBreakdown.put(user, am);
            updateSize(am);
        } else {
            // Client already exists!
        }
    }

    public void removeClient(V4User user) {
        // Make sure client exists
        if (this.clientBreakdown.containsKey(user)) {
            updateSize(this.clientBreakdown.get(user) * -1);
            this.clientBreakdown.remove(user);
        } else {
            // Client doesn't exist!
        }
    }

    public void addToClient(V4User user, Double am) {
        // Make sure client exists
        if (this.clientBreakdown.containsKey(user)) {
            this.clientBreakdown.put(user, Double.sum(am, this.clientBreakdown.get(user)));
            updateSize(am);
        } else {
            // Client doesn't exist!
        }
    }

    public void removeFromClient(V4User user, Double am) {
        // Make sure client exists
        if (this.clientBreakdown.containsKey(user)) {
            this.clientBreakdown.put(user, this.clientBreakdown.get(user) - am );
            if (this.clientBreakdown.get(user) > am) {
                updateSize(am * -1);
            }
            else if (this.clientBreakdown.get(user) <= am) {
                removeClient(user);
            }
        } else {
            // Client doesn't exist!
        }
    }

    private void updateSize(Double am) {
        this.size += am;
    }

    public Double getClientWorth(V4User user) {
        return this.clientBreakdown.get(user);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSize() {
        return this.size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Double getPortionLiquid() {
        return this.portionLiquid;
    }

    public void setPortionLiquid(Double portionLiquid) {
        this.portionLiquid = portionLiquid;
    }

    public HashMap<V4User,Double> getClientBreakdown() {
        return this.clientBreakdown;
    }

    public void setClientBreakdown(HashMap<V4User,Double> clientBreakdown) {
        this.clientBreakdown = clientBreakdown;
    }

    public void setAssets(HashMap<Stock,Double> assets) {
        this.assets = assets;
    }
}
