package com.zj.loading;

public enum OverLapMode {

    FLOATING(10), OVERLAP(0), FO(100);

    final int value;

    OverLapMode(int i) {
        this.value = i;
    }
}