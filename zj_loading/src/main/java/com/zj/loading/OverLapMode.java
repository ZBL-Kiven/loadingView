package com.zj.loading;

public enum OverLapMode {

    FLOATING(10), OVERLAP(100), FO(1000);

    final int value;

    OverLapMode(int i) {
        this.value = i;
    }
}