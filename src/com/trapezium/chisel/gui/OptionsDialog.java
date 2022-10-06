package com.trapezium.chisel.gui;
import com.trapezium.chisel.DialogOwner;
import com.trapezium.chisel.Chisel;
import com.trapezium.chisel.ChiselProperties;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.node.NodeType;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class OptionsDialog extends BaseDialog
{
	java.awt.List nameList;
	java.awt.List styleList;
	java.awt.List sizeList;

	Checkbox loadWorkspace;
	Checkbox saveInUnixFormat;
	Checkbox automaticClean;
	Checkbox includeWarningsInNextPrev;
	Checkbox allowNurbsExtension;
//	Checkbox includeNonconformancesInNextPrev;

    boolean changed = false;

	public OptionsDialog(DialogOwner owner) {
		super(owner, "optionsDialogTitle");

            setResizable(false);
                
	    Properties props = ChiselProperties.getProperties();

		nameList = new java.awt.List(4);
		String curfont = props.getProperty("font.name");
        String fontlist[] = Toolkit.getDefaultToolkit().getFontList();
        for (int i = 0; i < fontlist.length; i++) {
            nameList.add(fontlist[i], i);
            if (fontlist[i].equalsIgnoreCase(curfont)) {
                nameList.select(i);
            }
        }

		styleList = new java.awt.List(4);
		styleList.add(strings.getString("choicePlain"), 0);
		styleList.add(strings.getString("choiceBold"), 1);
		styleList.add(strings.getString("choiceItalic"), 2);
		styleList.add(strings.getString("choiceBoldItalic"), 3);
		switch (Integer.parseInt(props.getProperty("font.style"))) {
            case Font.PLAIN:
    		    styleList.select(0);
                break;
            case Font.BOLD:
    		    styleList.select(1);
                break;
            case Font.ITALIC:
    		    styleList.select(2);
                break;
            case (Font.BOLD | Font.ITALIC):
    		    styleList.select(3);
                break;
		}

		int cursize = Integer.parseInt(props.getProperty("font.size"));
		sizeList = new java.awt.List(28);
		int index = 0;
		for (int fontsize = 8; fontsize <= 96; fontsize += (fontsize < 24 ? 1 : (fontsize < 40 ? 4 : 8))) {
            sizeList.addItem(String.valueOf(fontsize), index);
            if (fontsize == cursize) {
                sizeList.select(index);
            }
            index++;
        }

        String nameLabel = strings.getString("font.nameLabel");
        String styleLabel = strings.getString("font.styleLabel");
        String sizeLabel = strings.getString("font.sizeLabel");

		OptionsPanel op = new OptionsPanel(nameLabel, styleLabel, sizeLabel);
		add("North", op);

        Container c2 = new Panel();
        c2.setLayout( new PaddedGridLayout( 5, 1, 0, 0, 16, 18 ));

		loadWorkspace = new Checkbox( strings.getString("loadWorkspaceLabel"), "true".equalsIgnoreCase(props.getProperty("workspace.reload")) );
		c2.add(loadWorkspace);

		saveInUnixFormat = new Checkbox( strings.getString( "saveInUnixFormat" ), "true".equalsIgnoreCase(props.getProperty("workspace.saveInUnixFormat")));
		c2.add( saveInUnixFormat );
		String initialValue = props.getProperty("workspace.automaticClean");
		boolean ival = true;
		if ( initialValue != null ) {
		    ival = "true".equalsIgnoreCase(props.getProperty("workspace.automaticClean"));
		}
		automaticClean = new Checkbox( strings.getString( "automaticClean" ), ival );
		c2.add( automaticClean );
		initialValue = props.getProperty("workspace.includeWarningsInNextPrev");
		ival = false;
		if ( initialValue != null ) {
		    ival = "true".equalsIgnoreCase(initialValue);
		}
		includeWarningsInNextPrev = new Checkbox( strings.getString( "includeWarnings" ), ival );
		c2.add( includeWarningsInNextPrev );
		initialValue = props.getProperty("workspace.allowNurbsExtension");
		ival = false;
		if ( initialValue != null ) {
		    ival = "true".equalsIgnoreCase(initialValue);
		}
		allowNurbsExtension = new Checkbox( strings.getString( "allowNurbsExtension" ), ival );
		c2.add( allowNurbsExtension );
		initialValue = props.getProperty("workspace.includeNonconformancesInNextPrev");
/*		ival = false;
		if ( initialValue != null ) {
		    ival = "true".equalsIgnoreCase(initialValue);
		}
		includeNonconformancesInNextPrev = new Checkbox( strings.getString( "includeNonconformances" ), ival );
		c2.add( includeNonconformancesInNextPrev );*/
		add("Center", c2);
    }

	public boolean hasChanged() {
	    return changed;
	}

	protected void execute() {
	    Properties props = ChiselProperties.getProperties();

	    boolean lw = loadWorkspace.getState();
	    boolean oldlw = "true".equalsIgnoreCase(props.getProperty("workspace.reload"));
        if (oldlw != lw) {
            props.put("workspace.reload", String.valueOf(lw));
            changed = true;
        }
        boolean newVal = saveInUnixFormat.getState();
        boolean oldVal = "true".equalsIgnoreCase(props.getProperty("workspace.saveInUnixFormat"));
        if (oldVal != newVal) {
            props.put("workspace.saveInUnixFormat", String.valueOf(newVal));
            changed = true;
        }
        boolean newAC = automaticClean.getState();
        boolean oldAC = "true".equalsIgnoreCase(props.getProperty("workspace.automaticClean"));
        if ( props.getProperty( "workspace.automaticClean" ) == null ) {
            oldAC = true;
        }
        if (oldAC != newAC) {
            props.put("workspace.automaticClean", String.valueOf(newAC));
            changed = true;
            Chisel.autoClean = newAC;
        }
        boolean newWarning = includeWarningsInNextPrev.getState();
        boolean oldWarning = "true".equalsIgnoreCase(props.getProperty("workspace.includeWarningsInNextPrev"));
        if ( oldWarning != newWarning ) {
            props.put( "workspace.includeWarningsInNextPrev", String.valueOf( newWarning ));
        }
        Chisel.includeWarningsInSearch = newWarning;
        
        boolean newNurbs = allowNurbsExtension.getState();
        boolean oldNurbs = VRML97.isNurbsEnabled();
        if ( oldNurbs != newNurbs ) {
            props.put( "workspace.allowNurbsExtension", String.valueOf( newNurbs ));
            if ( newNurbs ) {
			    VRML97.enableNurbs();
			    NodeType.enableNurbs();
			} else {
			    VRML97.disableNurbs();
			}
		}
        
/*        boolean newNonconformance = includeNonconformancesInNextPrev.getState();
        boolean oldNonconformance = "true".equalsIgnoreCase(props.getProperty("workspace.includeNonconformancesInNextPrev"));
        if ( oldNonconformance != newNonconformance ) {
            props.put( "workspace.includeNonconformancesInNextPrev", String.valueOf( newNonconformance ));
        }
        Chisel.includeNonconformancesInSearch = newNonconformance;*/

	    String oldfn = props.getProperty("font.name");
	    String fn = nameList.getSelectedItem();
	    if (fn == null) fn = oldfn;
	    if (!oldfn.equals(fn)) {
    		props.put("font.name", fn);
    		changed = true;
        }

        String oldfsize = props.getProperty("font.size");
        String fsize = sizeList.getSelectedItem();
        if (fsize == null) fsize = oldfsize;
	    if (!oldfsize.equals(fsize)) {
    		props.put("font.size", fsize);
    		changed = true;
    	}

	    String oldstyle = props.getProperty("font.style");
	    String style = styleList.getSelectedItem();
        if (style == null) style = oldstyle;
	    if (!oldstyle.equals(style)) {
    		int fs = Font.PLAIN;
    		if (style.equals(strings.getString("choiceBold"))) { // MLo (was incorrectly capitalised)
    			fs = Font.BOLD;
    		} else if (style.equals(strings.getString("choiceItalic"))) { // MLo (was incorrectly capitalised)
    			fs = Font.ITALIC;
    		} else if (style.equals(strings.getString("choiceBoldItalic"))) { // MLo (was incorrectly capitalised)
    			fs = Font.BOLD | Font.ITALIC;
    	    }
    		props.put("font.style", String.valueOf(fs));
            changed = true;
        }
	}

	Color getDialogBackground() {
	    return getBackground();
	}

    static final int vmargin = 6;
    static final int hmargin = 16;
    static final int vgap = 2;
    static final int hgap = 8;
	class OptionsPanel extends Panel {
        Label nameLabel;
        Label styleLabel;
        Label sizeLabel;
	    public OptionsPanel(String name, String style, String size) {

	        //setBackground(getDialogBackground().brighter());

	        nameLabel = new Label(name);
	        styleLabel = new Label(style);
	        sizeLabel = new Label(size);

	        add(nameLabel);
	        add(styleLabel);
	        add(sizeLabel);

	        add(nameList);
	        add(styleList);
	        add(sizeList);

	        setLayout(null);
	    }

	    public Dimension getPreferredSize() {
	        return getMinimumSize();
	    }

	    public Dimension getMinimumSize() {
	        FontMetrics fm = getFontMetrics(getFont());
	        return new Dimension( 48 * fm.charWidth('X') + 2 * hmargin + 2 * hgap, 7 * fm.getHeight() + 2 * vmargin);
	    }

	    public void doLayout() {
	        Dimension size = getSize();
	        FontMetrics fm = getFontMetrics(getFont());
	        int labelht = fm.getHeight();
	        int w = size.width - (2 * hmargin) - (2 * hgap);
	        int col1_width = w / 2;
	        int col2_width = w / 3;
	        int col3_width = w - col1_width - col2_width;

	        int x1 = hmargin;
	        int x2 = x1 + col1_width + hgap;
	        int x3 = x2 + col2_width + hgap;

	        nameLabel.setBounds(x1, vmargin, col1_width, labelht);
	        styleLabel.setBounds(x2, vmargin, col2_width, labelht);
	        sizeLabel.setBounds(x3, vmargin, col3_width, labelht);

            int y = vmargin + labelht + vgap;
	        int listht = size.height - vmargin - y;

	        nameList.setBounds(x1, y, col1_width, listht);
	        styleList.setBounds(x2, y, col2_width, listht);
	        sizeList.setBounds(x3, y, col3_width, listht);
	    }
    }
}
