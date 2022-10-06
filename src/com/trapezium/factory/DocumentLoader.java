package com.trapezium.factory;

/** Used for reloading serialized Scene/TokenEnumerator from undo/redo level */
public interface DocumentLoader {
    public void reload( Object scene );
}
