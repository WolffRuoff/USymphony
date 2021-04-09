package com.muhlenberg.models;
import java.util.HashMap;
import java.util.Map;

import com.symphony.bdk.gen.api.model.V4User;

import java.lang.Double;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
