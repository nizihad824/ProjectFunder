package de.unidue.inf.is.domain;

import java.math.BigDecimal;

public class Donation {
    final String name;
    final BigDecimal amount;

    public Donation(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
