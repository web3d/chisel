package com.trapezium.factory;

public interface FactoryResponseListener {
	void done( FactoryData result );
	void update( FactoryData result );
	void setNumberOfLines( int n );
	void setPolygonCount( int n );
	void setAction( String action );
	void setText( String text );
	void setFactoryData( FactoryData fd );
	void setLinePercent( int percentThousands );
}
