package com.muhlenberg.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.symphony.bdk.gen.api.model.V4User;

import java.io.IOException;
import java.lang.Double;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import yahoofinance.YahooFinance;

@RequiredArgsConstructor
@Getter
public class Portfolio implements java.io.Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private @Setter String name;
    private float size;
    private Double portionLiquid;
    private HashMap<Long, Double> clientBreakdown; // userID, client %
    private HashMap<Stock, Double> assets; // Stock, # of shares owned
    private String mainComparison;

    public Portfolio(String name, float size, Double portionLiquid, HashMap<Long, Double> clientBreakdown,
            HashMap<Stock, Double> assets, String mainComparison) {
        this.name = name;
        this.size = size;
        this.portionLiquid = portionLiquid;
        this.clientBreakdown = clientBreakdown;
        this.assets = assets;
        this.mainComparison = mainComparison;
    }

    public Portfolio(String name, HashMap<Long, Double> clients, String mainComp) {
        this.name = name;
        this.size = 0;
        this.portionLiquid = 0.0;
        this.clientBreakdown = clients;
        this.assets = new HashMap<Stock, Double>();
        if ("".equals(mainComp)) {
            this.mainComparison = "^GSPC";
        } else {
            this.mainComparison = mainComp;
        }
    }

    // Provide method to update stock changes. Should be called every time the
    // change is checked. Needed so that top5 and bottom5 changes will still work.
    public void updateStocks() throws IOException {
        ArrayList<String> updateVals = new ArrayList<String>();
        for (Stock stock : assets.keySet()) {
            updateVals.add(stock.getSymbol());
        }
        String[] symbols = updateVals.toArray(new String[updateVals.size()]);
        Map<String, yahoofinance.Stock> stocks = YahooFinance.get(symbols);
        for (Stock stock : assets.keySet()) {
            stock.setLatestPrice(stocks.get(stock.getSymbol()).getQuote().getPrice().doubleValue());
            stock.setChange(stocks.get(stock.getSymbol()).getQuote().getChangeInPercent().doubleValue());
        }
    }

    public double getEvaluation() {

        double val = 0.0;
        for (Map.Entry<Stock, Double> entry : assets.entrySet()) {
            // Add stock price * value of that stock
            val += entry.getKey().getLatestPrice() * entry.getValue();
        }

        return val;

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
            }
        });

        if (list.size() < 5) {
            Stock[] arr = new Stock[list.size()];
            arr = list.toArray(arr);
            return arr;
        } else {
            Stock[] arr = new Stock[5];
            for (int i = 0; i < 5; i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }
    }

    // change so that top movers are not also in bottom movers.
    public Stock[] getTop5() {
        ArrayList<Stock> list = new ArrayList<Stock>();
        for (Stock key : assets.keySet()) {
            list.add(key);
        }
        // Order is swapped in compare to get lower stocks
        Collections.sort(list, new Comparator<Stock>() {
            @Override
            public int compare(Stock c1, Stock c2) {
                return Double.compare(c2.getChange(), c1.getChange());
            }
        });

        if (list.size() < 5) {
            Stock[] arr = new Stock[list.size()];
            arr = list.toArray(arr);
            return arr;
        } else {
            Stock[] arr = new Stock[5];
            for (int i = 0; i < 5; i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }
    }

    public void addAsset(Stock n, Double am) throws IOException {
        // If stock exists, update value
        for (Entry<Stock, Double> stoc : this.assets.entrySet()) {
            if (stoc.getKey().getSymbol().equals(n.getSymbol())) {
                this.assets.put(stoc.getKey(), Double.sum(am, this.assets.get(stoc.getKey())));
                return;
            }
        }
        // Create new asset
        this.assets.put(n, am);
        this.rebalancePortfolio();
    }

    public void removeAsset(Stock n, Double am) throws IOException {
        // Make sure asset exists
        if (this.assets.containsKey(n)) {
            if (Double.compare(am, this.assets.get(n)) >= 0) {
                this.assets.remove(n);
            } else {
                this.assets.put(n, this.assets.get(n) - am);
            }
            this.rebalancePortfolio();
        } else {
            // Return an error that asset doesn't exist
        }
    }

    // Updates portion liquid
    private void rebalancePortfolio() throws IOException {
        updateStocks();
        Double newAmount = 0.0;
        for(Entry<Stock, Double> stoc : this.assets.entrySet()) {
            newAmount += stoc.getKey().getLatestPrice() * stoc.getValue();
        }
        this.portionLiquid = Math.round(((this.size - newAmount) / this.size) * 100.0) / 100.0;
        
    }

    // Absolutely critical to override and return stock array. DO NOT DELETE
    public Stock[] getAssets() {
        int i = 0;
        Stock[] arr = new Stock[assets.size()];

        for (Stock key : assets.keySet()) {
            arr[i] = key;
            i++;
        }
        return arr;
    }

    public void addClient(V4User user, Double am) {
        Long userId = user.getUserId();
        // Make sure client doesn't exist
        if (!this.clientBreakdown.containsKey(userId)) {
            this.clientBreakdown.put(userId, am);
            updateSize(am);
        } else {
            // Client already exists!
        }
    }

    public void removeClient(V4User user) {
        Long userId = user.getUserId();
        // Make sure client exists
        if (this.clientBreakdown.containsKey(userId)) {
            updateSize(this.clientBreakdown.get(userId) * -1);
            this.clientBreakdown.remove(userId);
        } else {
            // Client doesn't exist!
        }
    }

    public void addToClient(V4User user, Double am) {
        Long userId = user.getUserId();
        // Make sure client exists
        if (this.clientBreakdown.containsKey(userId)) {
            this.clientBreakdown.put(userId, Double.sum(am, this.clientBreakdown.get(userId)));
            updateSize(am);
        } else {
            // Client doesn't exist!
        }
    }

    public void removeFromClient(V4User user, Double am) {
        Long userId = user.getUserId();
        // Make sure client exists
        if (this.clientBreakdown.containsKey(userId)) {
            this.clientBreakdown.put(userId, this.clientBreakdown.get(userId) - am);
            if (this.clientBreakdown.get(userId) > am) {
                updateSize(am * -1);
            } else if (this.clientBreakdown.get(userId) <= am) {
                removeClient(user);
            }
        } else {
            // Client doesn't exist!
        }
    }

    // Return % increase compared to main stat
    public double getCompPercent() throws IOException {
        // get total valuation
        double total = getEvaluation();
        double totalPercentIncrease = 0d;
        for (Map.Entry<Stock, Double> entry : assets.entrySet()) {
            // find percentage of portfolio due to given stock
            double percentage = entry.getValue() * entry.getKey().getLatestPrice() / total;
            totalPercentIncrease = totalPercentIncrease + (entry.getKey().getChange() / 100 * percentage);
        }
        // Round output to two decimals
        return Math.round((totalPercentIncrease * 100)
                - YahooFinance.get(this.mainComparison).getQuote().getChangeInPercent().doubleValue());
    }

    public String getMainComparison() {
        return this.mainComparison;
    }

    public void setMainComparison(String mainComparison) {
        this.mainComparison = mainComparison;
    }

    private void updateSize(Double am) {
        this.size += am;
    }

    public Double getClientWorth(V4User user) {
        Long userId = user.getUserId();
        return this.clientBreakdown.get(userId);
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

    public HashMap<Long, Double> getClientBreakdown() {
        return this.clientBreakdown;
    }

    public void setClientBreakdown(HashMap<Long, Double> clientBreakdown) {
        this.clientBreakdown = clientBreakdown;
    }

    public void setAssets(HashMap<Stock, Double> assets) {
        this.assets = assets;
    }

    public HashMap<Stock, Double> getAssetList() {
        return this.assets;
    }
}
