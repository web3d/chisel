package com.trapezium.chisel;

public interface RowState {
    void rowReady();
    void rowRunning();
    void rowDone();
}
