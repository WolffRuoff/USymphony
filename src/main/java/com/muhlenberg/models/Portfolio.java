package com.muhlenberg.models;
import java.util.HashMap;

import com.symphony.bdk.gen.api.model.V4User;

import java.lang.Double;
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
}
