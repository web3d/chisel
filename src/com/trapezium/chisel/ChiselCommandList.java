package com.trapezium.chisel;

import com.trapezium.parse.InputStreamFactory;

import adc.parser.*;

import java.util.Vector;
import java.io.File;

/** ChiselCommandList takes an HTML report, and turns it into
 *  a set of chisel operations, complete with option settings.
 *
 *  Each line of the HTML report is processed.
 */
public class ChiselCommandList {
    Vector chisels;
    Vector params;
    int tableLevel;
    ChiselSet[] chiselSets;
    static public boolean windoze = false;

	/** Class constructor */
    public ChiselCommandList( String file, ChiselSet[] sets ) {
        chiselSets = sets;
        
        // use adc classes to parse file
        try {
		    HtmlStreamTokenizer tokenizer = new HtmlStreamTokenizer( 
                InputStreamFactory.getInputStream( new File( file )));
       	    HtmlTag tag = new HtmlTag();
			tableLevel = 0;

		    while (tokenizer.nextToken() != HtmlStreamTokenizer.TT_EOF) {
	    		if (tokenizer.getTokenType() == HtmlStreamTokenizer.TT_TAG) {
					processTag( tokenizer, tag );
				}
            }
        } catch ( Exception e ) {
            System.out.println( "Exception " + e );
            e.printStackTrace();
        }
    }

	void processTag( HtmlStreamTokenizer tokenizer, HtmlTag tag ) throws HtmlException, java.io.IOException {
   		tokenizer.parseTag(tokenizer.getStringValue(), tag);
       	int tagtype = tag.getTagType();
        if ( tagtype == tag.T_TABLE ) {
            if ( tag.isEndTag() ) {
                tableLevel--;
            } else {
                tableLevel++;
                adc.parser.Table table = new adc.parser.Table();
                table.parseTable( tokenizer, new HtmlTag( tag ));
                if ( tableLevel == 1 ) {
                    chisels = new Vector();
                    params = new Vector();
                    int numberRows = table.getRows();
                    
                    // table cell 0 is the header, start with table cell 1
                    for ( int i = 1; i < numberRows; i++ ) {
                        TableCell tc = table.elementAt( i, 0 );
                        HtmlTag cellTag = tc.getCellTag();
                        HtmlTag cellTableTag = null;
						// theoretically, the first element in the table cell
						// is a string description of the chisel, and the
						// next element is the optional complete set of options
						// for that chisel
						StringBuffer cellTitle = new StringBuffer();
						adc.parser.Table cellTable = null;
						for ( int j = 0; j < tc.size(); j++ ) {
						    Object xx = tc.elementAt( j );
						    if ( xx instanceof String ) {
					            cellTitle.append( (String)xx );
					        } else if ( xx instanceof adc.parser.Table ) {
					            cellTable = (adc.parser.Table)xx;
					            cellTableTag = cellTag;
					        }
						}

                        // convert table cell to Chisel
                        Optimizer chisel = createChisel( new String( cellTitle ));
                        if ( chisel != null ) {
                            chisels.addElement( chisel );
                            ParamList pl = createParam( cellTable, tokenizer, cellTableTag );
                            params.addElement( pl );
                            updateChisel( chisel, pl );
                        }
                    }
                }
            }
        } 
    }
    
    /** Update the chisel using the ParamList */
    void updateChisel( Optimizer chisel, ParamList pl ) {
        int n = pl.getNumberParams();
        for ( int i = 0; i < n; i++ ) {
            chisel.setOptionValue( i, pl.getParam( i ));
        }
    }

	/** Get the number of chisel commands extracted from HTML report */
	public int getNumberCommands() {
		if ( chisels != null ) {
			return( chisels.size() );
		} else {
			return( 0 );
		}
	}
	
	public Optimizer elementAt( int offset ) {
	    return( (Optimizer)chisels.elementAt( offset ));
	}
	
	String getChiselName( ChiselSet testSet, String chiselDescription ) {
        for (int i = 0; i < testSet.getNumberChisels(); i++) {
            ChiselDescriptor cd = testSet.getEntry(i);
            Object chisel = cd.getOptionHolder();
            if ( chisel instanceof Optimizer ) {
                Optimizer opt = (Optimizer)chisel;
                String actionMessage = opt.getActionMessage();
                if ( actionMessage != null ) {
                    if ( actionMessage.indexOf( chiselDescription ) >= 0 ) {
                        return( cd.getClassName() );
                    }
                }
            }
        }
        return( null );
    }

	
	/** Convert a table cell entry into its corresponding chisel object */
    Optimizer createChisel( String cellTitle ) {
		// convert the cellTitle to a chisel name
		String chiselName = null;
		for ( int i = 0; i < chiselSets.length; i++ ) {
		    ChiselSet testSet = chiselSets[i];
		    chiselName = getChiselName( testSet, cellTitle );
		    if ( chiselName != null ) {
		        break;
		    }
		}

		if ( chiselName == null ) {
		    System.out.println( "No chisel found for '" + cellTitle + "'" );
		    return( null );
		}

		// now that the name is converted into the class name, create it
		Optimizer result = ChiselDescriptor.createChisel( chiselName );
        return( result );
    }

	/** Create the parameter controls for a particular chisel
	 *
	 *  @param options the Table of options from the interior of the HTML
	 *     report table cell
	 *  @param tokenizer used for parsing the HTML
	 *  @param tag not sure why we need this...
	 *
	 *  @return the ParamList extracted from the HTML
	 */
    ParamList createParam( adc.parser.Table table, HtmlStreamTokenizer tokenizer, HtmlTag tag ) throws HtmlException, java.io.IOException {
		ParamList params = new ParamList();
		if ( table != null ) {
            int numberRows = table.getRows();
            for ( int i = 0; i < numberRows; i++ ) {
				TableCell tc2 = table.elementAt( i, 1 );
				Object xx2 = tc2.elementAt( 0 );
				if ( xx2 instanceof String ) {
				    params.addValue( xx2 );
				}
			}
	 	}
        return( params );
    }
    
    public static void main(String[] args) {
        Vector fileList = new Vector();
        if ( args.length > 1 ) {
            if ( args[1].indexOf( "-w" ) >= 0 ) {
                ChiselCommandList.windoze = true;
            }
            for ( int i = 1; i < args.length; i++ ) {
                fileList.addElement( args[i] );
            }
        } else if ( args.length == 0 ) {
            System.out.println( "" );
            System.out.println( "Usage:" );
            System.out.println( "  java com.trapezium.chisel.ChiselCommandList <html report> <file1> [<file2> ...]" );
            System.out.println( "" );
            System.out.println( "This generates a shell script for chiseling files from the command line." );
            System.out.println( "" );
            System.exit( 0 );
        }
        System.out.println( "# ChiselCommandList shell script generator" );
        ChiselCommandList xx = new ChiselCommandList( args[0], ChiselSet.createChiselSets() );
        for ( int i = 0; i < fileList.size(); i++ ) {
            xx.generateShellScript( (String)fileList.elementAt( i ));
        }
        System.exit( 0 );
    }
    
    public void generateShellScript( String fileToProcess ) {
        int n = getNumberCommands();
        System.out.println( "# " + n + " commands" );
        if ( windoze ) {
            System.out.println( "copy " + fileToProcess + " " + fileToProcess + ".x" );
        } else {
            System.out.println( "cp " + fileToProcess + " " + fileToProcess + ".x" );
        }
        for ( int i = 0; i < n; i++ ) {
            generateCommand( fileToProcess, (Optimizer)chisels.elementAt( i ), (ParamList)params.elementAt( i ));
        }
    }
    
    void generateCommand( String fileToProcess, Optimizer chisel, ParamList params ) {
        if ( windoze ) {
            System.out.println( "chisel " + fileToProcess + ".x -chisel " + getChiselName( chisel ) + " " + params.getParamList() );
            System.out.println( "move " + fileToProcess + ".x.chiseled " + fileToProcess + ".x" );
        } else {
            System.out.println( "java -mx128m com.trapezium.chisel.Chisel " + fileToProcess + ".x -chisel " + getChiselName( chisel ) + " " + params.getParamList() );
            System.out.println( "mv " + fileToProcess + ".x.chiseled " + fileToProcess + ".x" );
        }
    }

    String getChiselName( Optimizer chisel ) {
        String name = chisel.getClass().getName();
        if ( name.indexOf( "com.trapezium.chisel." ) >= 0 ) {
            name = name.substring( 17 );
        }
        return( name );
    }
    class ParamList {
        Vector valueList;
        
        ParamList() {
            valueList = new Vector();
        }
        
        void addValue( Object value ) {
            valueList.addElement( value );
        }
        
        int getNumberParams() {
            return( valueList.size() );
        }
        
        String getParam( int offset ) {
            return((String)valueList.elementAt( offset ));
        }
        
        String getParamList() {
            int size = valueList.size();
            if ( size == 0 ) {
                return( "" );
            } else {
                StringBuffer sb = new StringBuffer();
                for ( int i = 0; i < size; i++ ) {
                    sb.append( "-o" + i + " " + (String)valueList.elementAt( i ) + " " );
                }
                return( new String( sb ));
            }
        }           
    }
}
