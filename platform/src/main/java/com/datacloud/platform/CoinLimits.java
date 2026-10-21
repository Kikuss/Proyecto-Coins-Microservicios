package com.datacloud.platform;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoinLimits {

    @Value("${coins.max-balance:10000}")
    private int maxBalance;

    public int getMaxBalance() {
        return maxBalance;
    }
}
