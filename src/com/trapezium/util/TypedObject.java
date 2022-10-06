package com.trapezium.util;

public interface TypedObject {
    int getType();
    Object getObject( int type );
}
