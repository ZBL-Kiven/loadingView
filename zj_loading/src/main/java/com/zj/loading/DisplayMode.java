package com.zj.loading;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public enum DisplayMode {
    NONE(0), LOADING(1), NO_DATA(2), NO_NETWORK(3), NORMAL(4);

    final int value;
    public long delay = 0L;

    public DisplayMode delay(long mills) {
        this.delay = mills;
        return this;
    }

    void reset() {
        this.delay = 0L;
    }

    DisplayMode(int value) {
        this.value = value;
    }
}
