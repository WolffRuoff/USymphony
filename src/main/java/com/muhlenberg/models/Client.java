package com.muhlenberg.models;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Client {
    private long userID;
    private float amountInvested;
    private Map<Long, Integer> portfolioAmounts;
}
