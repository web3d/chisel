/*
 * @(#)FactoryChain.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.factory;

import java.util.Vector;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.util.StringUtil;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.chisel.ChiselSet;
import com.trapezium.util.ProgressIndicator;
import com.trapezium.chisel.ChiselDescriptor;
import com.trapezium.chisel.ChiselTable;
import com.trapezium.chisel.ChiselRow;
import com.trapezium.chisel.RowState;
import com.trapezium.chisel.Chisel;
import com.trapezium.chisel.Optimizer;

/*
    The FactoryChain is a list of file processing factories.

    Each factory in the list may be enabled or disabled.

    A FactoryChain is associated with each ChiselRow of the GUI.
*/
public class FactoryChain extends QueuedRequestFactory {
	Vector chainLinks;
	Vector chiselFactories;
	int chiselGroup;

    public void wipeout() {
        super.wipeout();
        int nChainLinks = chainLinks.size();
        for ( int i = 0; i < nChainLinks; i++ ) {
            ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( i );
            cfi.wipeout();
        }
        chainLinks = null;
        int nFactories = chiselFactories.size();
        for ( int i = 0; i < nFactories; i++ ) {
            ChiselFactory f = (ChiselFactory)chiselFactories.elementAt( i );
            f.wipeout();
        }
        chiselFactories = null;
    }
    
	static int idcounter = 1;
	int id;
	public FactoryChain( int chiselGroup ) {
		chainLinks = new Vector();
		this.chiselGroup = chiselGroup;
		id = idcounter;
		idcounter++;
	}

    String factoryName = null;
	public String getFactoryName() {
	    if ( factoryName == null ) {
    		return( getFixedFactoryName() );
    	} else {
    	    return( factoryName );
    	}
	}

	static public String getFixedFactoryName() {
		return( "Factory Chain" );
	}

    /** Add another factory to the list of factories */
	public void addFactory( QueuedRequestFactory factory ) {
		chainLinks.addElement( new ChainedFactoryInstance( factory, chainLinks.size() ));
	}

	public QueuedRequestFactory getFactoryAt( int offset ) {
	    ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( offset );
	    return( cfi.getFactory() );
	}

	public void setFactoryName( String factoryName ) {
	    this.factoryName = factoryName;
	}

	public void setFactoryTitle( String factoryTitle ) {
//	    this.factoryTitle = factoryTitle;
	}

	public void disableChisel( String chiselName ) {
	    for ( int i = 0; i < chainLinks.size(); i++ ) {
	        ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( i );
	        cfi.disableChisel( chiselName );
	    }
	}

	public void removeFactory( String factoryName ) {
	    for ( int i = 0; i < chainLinks.size(); i++ ) {
	        ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( i );
	        QueuedRequestFactory f = cfi.getFactory();
	        if ( f.getFactoryName().compareTo( factoryName ) == 0 ) {
	            chainLinks.removeElementAt( i );
	            return;
	        }
	    }
	}

	public void enableChisel( String chiselName, FactoryResponseListener theListener ) {
	    for ( int i = 0; i < chainLinks.size(); i++ ) {
	        ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( i );
	        cfi.enableChisel( chiselName, theListener );
	    }
	}

	public void insertFactory( QueuedRequestFactory factory, QueuedRequestFactory prev ) {
		if ( prev != null ) {
			for ( int i = 0; i < chainLinks.size(); i++ ) {
				ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( i );
				if ( cfi.getQueuedRequestFactory() == prev ) {
					if ( i == ( chainLinks.size() - 1 )) {
						addFactory( factory );
					} else {
						chainLinks.insertElementAt( new ChainedFactoryInstance( factory, i + 1 ), i + 1 );
					}
					return;
				}
			}
		}

		// didn't find prev or prev is null
		addFactory( factory );
	}


	/** Start the factory chain, make this object the listener for the chained factory
	 *  entries, the original listener is saved in "listener".
	 */
	public void submit( FactoryData factoryData ) {
		if ( chainLinks.size() > 0 ) {
		    factoryData.setError( null );
			factoryData.pushFactoryInfo();
			runFactory( 0, factoryData );
		}
	}

	public int getNumberChainLinks() {
		return( chainLinks.size() );
	}

    /** Called when a Factory has finished running, move on to the next Factory */
	public void done( FactoryData fd ) {
		int nextFactoryNumber = fd.getFactoryNumber() + 1;
//		System.out.println( "chain " + id + ", factory " + fd.getFactoryNumber() + " done, chainLinks.size() is " + chainLinks.size() );
//		System.out.println( "fd.getError() is " + fd.getError() );
//		System.out.println( "GlobalProgressIndicator.abortCurrentProcess " + GlobalProgressIndicator.abortCurrentProcess );
//		System.out.println( "nextFactoryNumber " + nextFactoryNumber  + ", chainLinks.size() " + chainLinks.size() );
		if (( fd.getError() == null ) && !fd.errorsCreated() &&
		    !GlobalProgressIndicator.abortCurrentProcess && 
		    (( nextFactoryNumber > 0 ) && ( nextFactoryNumber < chainLinks.size() ))) {
			fd.setFactoryChain( this );
			if ( nextFactoryNumber == ( chainLinks.size() - 1 )) {
			    fd.setNodeVerifyChecksEnabled( true );
			    fd.setUsageChecksEnabled( false );
			} else {
			    fd.setNodeVerifyChecksEnabled( false );
			    fd.setUsageChecksEnabled( false );
			    // here we do additional checks only if the chain after this next one requires it
		        QueuedRequestFactory nextFactory = getFactoryAt( nextFactoryNumber + 1 );
		        if ( nextFactory instanceof ChiselFactory ) {
		            String chiselName = ((ChiselFactory)nextFactory).getChiselName();
                    // At the moment, only the IFS_CoordRemover chisel needs
                    // usage info
                    if ( chiselName != null ) {
//    		            System.out.println( "ChiselName is '" + chiselName + "'" );
    		            if ( chiselName.indexOf( "IFS_CoordRemover" ) > 0 ) {
    		                fd.setUsageChecksEnabled( true );
    		            }
    		        }
    			}
			}
			runFactory( nextFactoryNumber, fd );
		} else {
//			System.out.println( "chain " + id + " is totally done!" );
			fd.popFactoryInfo();
			if ( listener != null ) {
			    listener.done( fd );
			}
		}
	}

    /** Run a factory in the list */
	void runFactory( int n, FactoryData factoryData ) {
//	    System.out.println( "Starting factory " + (n+1) + " of " + chainLinks.size() );
		ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( n );
		if ( !cfi.isEnabled() ) {
//		    System.out.println( "..doing nothing, factory not enabled" );
		    factoryData.setFactoryNumber( n );
		    done( factoryData );
		} else {
    		QueuedRequestFactory qrf = cfi.getQueuedRequestFactory();
//    		System.out.println( ".." + qrf.getClass().getName() );
    		qrf.setListener( this ); // was addListener
    		ProgressIndicator pl = ChiselSet.getProgressIndicator( chiselGroup );
    		if ( pl != null ) {
    		    if ( pl instanceof ChiselTable ) {
    		        ChiselTable ct = (ChiselTable)pl;
    		        Object cr = ct.getRow( 0 );
    		        if ( cr instanceof FactoryResponseListener ) {
    		            FactoryResponseListener frl = (FactoryResponseListener)cr;
                		qrf.addListener( frl );
                	}
            	}
            }
    		factoryData.setFactoryNumber( n );
    		factoryData.setChainDone( false );
    		qrf.submit( factoryData );
    	}
	}

	public void handleRequest( FactoryData factoryData ) {
	}

	public void dump() {
		System.out.println( "Dumping factory chain " + id );
		dump( 1 );
		System.out.println( "Dump complete" );
	}

	void dump( int indentLevel ) {
    	String space = StringUtil.spacer( indentLevel );
    	System.out.println( space + "chain " + id );

		for ( int i = 0; i < chainLinks.size(); i++ ) {
			ChainedFactoryInstance cfi = (ChainedFactoryInstance)chainLinks.elementAt( i );
			QueuedRequestFactory qrf = cfi.getQueuedRequestFactory();
			System.out.println( space + i + ": " + qrf.getClass().getName() + " enabled " + cfi.isEnabled() );
			if ( qrf instanceof FactoryChain ) {
				FactoryChain fc = (FactoryChain)qrf;
				fc.dump( indentLevel+1);
			} else if ( qrf instanceof ChiselFactory ) {
			    ChiselFactory cf = (ChiselFactory)qrf;
			    cf.dump( indentLevel+1 );
			}
		}
	}


	/** Add a chisel to the list of chisels.
	 *
	 *  @param  chiselName  the class name of the chisel to add
	 *  @param  chiselType  category of chisel, to prevent chisels form interfering with each other
	 *  @param  parser      for validating the file
	 *  @param  baseFilePath  path to file, needed for some chisels
	 *  @param  nameWithoutPath  file name without path, needed for some chisels
	 */
	public void addChisel( String chiselName, int chiselType, QueuedRequestFactory parser, String baseFilePath, String nameWithoutPath, RowState rowState ) {
	    if ( chiselFactories == null ) {
	        chiselFactories = new Vector();
	        addChiselFactory( chiselName, chiselType, baseFilePath, nameWithoutPath, rowState );
	    } else {
	        // add the chisel to the first compatible chisel factory
	        for ( int i = 0; i < chiselFactories.size(); i++ ) {
	            ChiselFactory testFactory = (ChiselFactory)chiselFactories.elementAt( i );
	            if ( testFactory.isCompatible( chiselType )) {
	                testFactory.addChisel( chiselName );
	                return;
	            }
	        }

	        // didn't find a compatible factory
	        addFactory( parser );
	        addChiselFactory( chiselName, chiselType, baseFilePath, nameWithoutPath, rowState );
	    }
    }


    void setFactoryTitles() {
        int numberFactories = chiselFactories.size();
        if ( numberFactories > 1 ) {
            for ( int i = 0; i < numberFactories; i++ ) {
                ChiselFactory f = (ChiselFactory)chiselFactories.elementAt( i );
                f.setFactoryTitle( "Phase " + (i+1) + " of " + numberFactories + "... " );
            }
        }
    }
    
    /** Add an optimizer to the factory chain.
     *
     *  @param theOptimizer the Optimizer to add
     *  @param parser parsing required after optimization to check syntax
     *  @param baseFilePath path to file
     *  @param nameWithoutPath needed by some optimizers
     */
	public void addOptimizer( Optimizer theOptimizer, QueuedRequestFactory parser, String baseFilePath, String nameWithoutPath ) {
	    if ( chiselFactories == null ) {
	        chiselFactories = new Vector();
	        addChiselFactory( theOptimizer, baseFilePath, nameWithoutPath );
	    } else {
	        // didn't find a compatible factory
	        addFactory( parser );
	        addChiselFactory( theOptimizer, baseFilePath, nameWithoutPath );
	    }
    }

	/** Add a factory to the list of factories */
	public void addChiselFactory( String chiselName, int chiselType, String baseFilePath, String nameWithoutPath, RowState rowState ) {
//	    System.out.println( "addChiselFactory '" + chiselName + "'" );
        ChiselFactory chiselFactory = new ChiselFactory( chiselType, ChiselSet.getProgressIndicator( chiselGroup ), rowState );
        addFactory( chiselFactory, chiselName, baseFilePath, nameWithoutPath );
    }
    
    void addChiselFactory( Optimizer theOptimizer, String baseFilePath, String nameWithoutPath ) {
        ChiselFactory chiselFactory = new ChiselFactory( ChiselDescriptor.NOTYPE_CHISEL, ChiselSet.getProgressIndicator( chiselGroup ), null );
        addFactory( chiselFactory, theOptimizer, baseFilePath, nameWithoutPath );
    }
    
    void addFactory( ChiselFactory chiselFactory, Object chiselName, String baseFilePath, String nameWithoutPath ) {
        addFactory( chiselFactory );
        chiselFactories.addElement( chiselFactory );
        chiselFactory.setListener( getListener() );
        if ( chiselName instanceof String ) {
            chiselFactory.addChisel( (String)chiselName );
        } else if ( chiselName instanceof Optimizer ) {
            chiselFactory.addOptimizer( (Optimizer)chiselName );
        }
        chiselFactory.setBaseFilePath( baseFilePath );
        chiselFactory.setBaseFileName( nameWithoutPath );
        setFactoryTitles();
    }
}
