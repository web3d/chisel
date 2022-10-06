package com.trapezium.factory;

public class ChainedFactoryInstance extends QueuedRequestFactory {
	QueuedRequestFactory workerFactory;
	int factoryNumber;
	boolean enabled;

    public void wipeout() {
        super.wipeout();
        workerFactory.wipeout();
        workerFactory = null;
    }
    
	public ChainedFactoryInstance( QueuedRequestFactory workerFactory, int factoryNumber ) {
		this.workerFactory = workerFactory;
		this.factoryNumber = factoryNumber;
		enabled = true;
	}
	

    //
    //  If the instance has a listener use it, otherwise use the workerFactory listener
    //
	public void done( FactoryData data ) {
	    if ( workerFactory instanceof ChiselFactory ) {
	        workerFactory.done( data );
		} else {
			super.done( data );
		}
	}
    
    public QueuedRequestFactory getFactory() {
        return( workerFactory );
    }
    
    public void disableChisel( String chiselName ) {
        String workerClass = workerFactory.getClass().getName();
        String workerClassBase = workerClass.substring( workerClass.lastIndexOf( '.' ) + 1 );
        if ( chiselName.compareTo( workerClassBase ) == 0 ) {
            enabled = false;
        }
    }
    
    public void enableChisel( String chiselName, FactoryResponseListener theListener ) {
        if ( chiselName.compareTo( workerFactory.getClass().getName() ) == 0 ) {
            enabled = true;
            workerFactory.setListener( theListener );
        }
    }
    
    public boolean isEnabled() {
        return( enabled );
    }
    
	public void submit( FactoryData factoryData ) {
		factoryData.setFactoryNumber( factoryNumber );
		workerFactory.submit( factoryData );
	}

	public QueuedRequestFactory getQueuedRequestFactory() {
		return( workerFactory );
	}

	public void handleRequest( FactoryData factoryData ) {
	}
}

