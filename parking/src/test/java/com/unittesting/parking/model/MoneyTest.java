package com.unittesting.parking.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** A first test to start from. Add yours next to it. */
class MoneyTest {

    @Test
    void formatsEurosAndCents() {
        assertThat(Money.format(960)).isEqualTo("9,60 €");
    }
}
