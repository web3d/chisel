package com.trapezium.cdk;

/*
	This simple extension of the java.awt.Frame class
	contains all the elements necessary to act as the
	main window of an application.
 */

import java.awt.*;

public class CDK extends Frame
{
    // used to contain all chisel related info, and generate the Chisel
    ChiselGenerator chiselGenerator;
    
    // the current option (inside chiselGenerator) the GUI is modifying
    OptionInfo optionInfo;
    
	public CDK()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		chiselGenerator = new ChiselGenerator();
		optionInfo = null;
		
		//{{INIT_CONTROLS
		setLayout(null);
		setVisible(false);
		setSize(701,517);
		setBackground(new Color(-16715009));
		openFileDialog1 = new java.awt.FileDialog(this);
		openFileDialog1.setMode(FileDialog.LOAD);
		openFileDialog1.setTitle("Open");
		//$$ openFileDialog1.move(48,468);
		label1 = new java.awt.Label("Author:");
		label1.setBounds(24,24,120,24);
		label1.setFont(new Font("Dialog", Font.BOLD, 12));
		add(label1);
		label2 = new java.awt.Label("Chisel description:");
		label2.setBounds(24,48,120,24);
		label2.setFont(new Font("Dialog", Font.BOLD, 12));
		add(label2);
		label3 = new java.awt.Label("Description:");
		label3.setBounds(384,180,72,24);
		add(label3);
		label4 = new java.awt.Label("File name:");
		label4.setBounds(24,72,120,24);
		label4.setFont(new Font("Dialog", Font.BOLD, 12));
		add(label4);
		codevName = new java.awt.TextField();
		codevName.setBounds(144,24,240,24);
		add(codevName);
		chiselDesc = new java.awt.TextField();
		chiselDesc.setBounds(144,48,240,24);
		add(chiselDesc);
		shortDescription = new java.awt.TextField();
		shortDescription.setBounds(144,72,240,24);
		add(shortDescription);
		nodeToModify = new java.awt.TextField();
		nodeToModify.setBounds(144,96,240,24);
		add(nodeToModify);
		nodeYNgroup = new CheckboxGroup();
		nodeYes = new java.awt.Checkbox("yes", nodeYNgroup, false);
		nodeYes.setBounds(240,156,48,24);
		add(nodeYes);
		nodeNo = new java.awt.Checkbox("no", nodeYNgroup, false);
		nodeNo.setBounds(240,180,48,24);
		add(nodeNo);
		routeYNgroup = new CheckboxGroup();
		routeYes = new java.awt.Checkbox("yes", routeYNgroup, false);
		routeYes.setBounds(240,216,48,24);
		add(routeYes);
		routeNo = new java.awt.Checkbox("no", routeYNgroup, false);
		routeNo.setBounds(240,240,48,24);
		add(routeNo);
		optionDescription = new java.awt.TextField();
		optionDescription.setBounds(480,180,228,24);
		add(optionDescription);
		valueTypeGroup = new CheckboxGroup();
		onValue = new java.awt.Checkbox("On value", valueTypeGroup, false);
		onValue.setBounds(384,204,108,24);
		add(onValue);
		offValue = new java.awt.Checkbox("Off value", valueTypeGroup, false);
		offValue.setBounds(384,228,108,24);
		add(offValue);
		numericValue = new java.awt.Checkbox("Numeric value", valueTypeGroup, false);
		numericValue.setBounds(384,252,108,24);
		add(numericValue);
		defaultPrompt = new java.awt.Label("Default:");
		defaultPrompt.setBounds(408,288,72,12);
		add(defaultPrompt);
		minPrompt = new java.awt.Label("Minimum:");
		minPrompt.setBounds(408,312,72,12);
		add(minPrompt);
		maxPrompt = new java.awt.Label("Maximum:");
		maxPrompt.setBounds(408,336,72,12);
		add(maxPrompt);
		incPrompt = new java.awt.Label("Increment:");
		incPrompt.setBounds(408,360,72,12);
		add(incPrompt);
		option = new java.awt.Label("Option");
		option.setBounds(384,156,60,24);
		option.setFont(new Font("Dialog", Font.BOLD, 12));
		add(option);
		defaultValue = new java.awt.TextField();
		defaultValue.setBounds(480,276,60,24);
		add(defaultValue);
		minValue = new java.awt.TextField();
		minValue.setBounds(480,300,60,24);
		add(minValue);
		maxValue = new java.awt.TextField();
		maxValue.setBounds(480,324,60,24);
		add(maxValue);
		incValue = new java.awt.TextField();
		incValue.setBounds(480,348,60,24);
		add(incValue);
		button1 = new java.awt.Button();
		button1.setLabel("1");
		button1.setBounds(336,180,24,24);
		button1.setForeground(new Color(0));
		button1.setBackground(new Color(-11032414));
		add(button1);
		button2 = new java.awt.Button();
		button2.setLabel("2");
		button2.setBounds(336,204,24,24);
		button2.setForeground(new Color(0));
		button2.setBackground(new Color(-11032414));
		add(button2);
		button3 = new java.awt.Button();
		button3.setLabel("3");
		button3.setBounds(336,228,24,24);
		button3.setForeground(new Color(0));
		button3.setBackground(new Color(-11032414));
		add(button3);
		button4 = new java.awt.Button();
		button4.setLabel("4");
		button4.setBounds(336,252,24,24);
		button4.setForeground(new Color(0));
		button4.setBackground(new Color(-11032414));
		add(button4);
		button5 = new java.awt.Button();
		button5.setLabel("5");
		button5.setBounds(336,276,24,24);
		button5.setForeground(new Color(0));
		button5.setBackground(new Color(-11032414));
		add(button5);
		button6 = new java.awt.Button();
		button6.setLabel("6");
		button6.setBounds(336,300,24,24);
		button6.setForeground(new Color(0));
		button6.setBackground(new Color(-11032414));
		add(button6);
		button7 = new java.awt.Button();
		button7.setLabel("7");
		button7.setBounds(336,324,24,24);
		button7.setForeground(new Color(0));
		button7.setBackground(new Color(-11032414));
		add(button7);
		button8 = new java.awt.Button();
		button8.setLabel("8");
		button8.setBounds(336,348,24,24);
		button8.setForeground(new Color(0));
		button8.setBackground(new Color(-11032414));
		add(button8);
		genChisel = new java.awt.Button();
		genChisel.setLabel("Generate Chisel Code");
		genChisel.setBounds(24,468,360,24);
		genChisel.setFont(new Font("Dialog", Font.BOLD|Font.ITALIC, 14));
		genChisel.setForeground(new Color(0));
		genChisel.setBackground(new Color(-1860127));
		add(genChisel);
		label5 = new java.awt.Label("Does this chisel modify ROUTEs?");
		label5.setBounds(36,228,192,24);
		label5.setFont(new Font("Dialog", Font.BOLD, 12));
		add(label5);
		label7 = new java.awt.Label("Does this chisel modify Nodes?");
		label7.setBounds(36,168,192,24);
		label7.setFont(new Font("Dialog", Font.BOLD, 12));
		add(label7);
		label8 = new java.awt.Label("Node to modify:");
		label8.setBounds(24,96,120,24);
		label8.setFont(new Font("Dialog", Font.BOLD, 12));
		add(label8);
		genResult = new java.awt.Label("");
		genResult.setBounds(420,468,252,24);
		add(genResult);
		setTitle("Chisel Developer Kit 1.01, 4 June 99");
		//}}
		
		//{{INIT_MENUS
		mainMenuBar = new java.awt.MenuBar();
		menu1 = new java.awt.Menu("File");
		miNew = new java.awt.MenuItem("New");
		miNew.setShortcut(new MenuShortcut(java.awt.event.KeyEvent.VK_N,false));
		menu1.add(miNew);
		miOpen = new java.awt.MenuItem("Open...");
		miOpen.setShortcut(new MenuShortcut(java.awt.event.KeyEvent.VK_O,false));
		menu1.add(miOpen);
		miSave = new java.awt.MenuItem("Save");
		miSave.setShortcut(new MenuShortcut(java.awt.event.KeyEvent.VK_S,false));
		menu1.add(miSave);
		miSaveAs = new java.awt.MenuItem("Save As...");
		menu1.add(miSaveAs);
		menu1.addSeparator();
		miExit = new java.awt.MenuItem("Exit");
		menu1.add(miExit);
		mainMenuBar.add(menu1);
		menu2 = new java.awt.Menu("Edit");
		miCut = new java.awt.MenuItem("Cut");
		miCut.setShortcut(new MenuShortcut(java.awt.event.KeyEvent.VK_X,false));
		menu2.add(miCut);
		miCopy = new java.awt.MenuItem("Copy");
		miCopy.setShortcut(new MenuShortcut(java.awt.event.KeyEvent.VK_C,false));
		menu2.add(miCopy);
		miPaste = new java.awt.MenuItem("Paste");
		miPaste.setShortcut(new MenuShortcut(java.awt.event.KeyEvent.VK_V,false));
		menu2.add(miPaste);
		mainMenuBar.add(menu2);
		menu3 = new java.awt.Menu("Help");
		mainMenuBar.setHelpMenu(menu3);
		miAbout = new java.awt.MenuItem("About..");
		menu3.add(miAbout);
		mainMenuBar.add(menu3);
		setMenuBar(mainMenuBar);
		//$$ mainMenuBar.move(12,468);
		//}}
		
		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		miOpen.addActionListener(lSymAction);
		miAbout.addActionListener(lSymAction);
		miExit.addActionListener(lSymAction);
		button1.addActionListener(lSymAction);
		button2.addActionListener(lSymAction);
		button3.addActionListener(lSymAction);
		button4.addActionListener(lSymAction);
		button5.addActionListener(lSymAction);
		button6.addActionListener(lSymAction);
		button7.addActionListener(lSymAction);
		button8.addActionListener(lSymAction);
		optionDescription.addActionListener(lSymAction);
		SymMouse aSymMouse = new SymMouse();
		onValue.addMouseListener(aSymMouse);
		numericValue.addMouseListener(aSymMouse);
		SymKey aSymKey = new SymKey();
		optionDescription.addKeyListener(aSymKey);
		minValue.addKeyListener(aSymKey);
		defaultValue.addKeyListener(aSymKey);
		maxValue.addKeyListener(aSymKey);
		incValue.addKeyListener(aSymKey);
		genChisel.addMouseListener(aSymMouse);
		offValue.addMouseListener(aSymMouse);
		nodeYes.addMouseListener(aSymMouse);
		nodeNo.addMouseListener(aSymMouse);
		routeYes.addMouseListener(aSymMouse);
		routeNo.addMouseListener(aSymMouse);
		nodeToModify.addKeyListener(aSymKey);
		//}}
        button1_ActionPerformed(null);
        nodeYes.setState( true );
        routeNo.setState( true );
    }
	
	public CDK(String title)
	{
		this();
		setTitle(title);
	}
	
    /**
     * Shows or hides the component depending on the boolean flag b.
     * @param b  if true, show the component; otherwise, hide the component.
     * @see java.awt.Component#isVisible
     */
    public void setVisible(boolean b)
	{
		if(b)
		{
			setLocation(50, 50);
		}	
		super.setVisible(b);
	}
	
	static public void main(String args[])
	{
		(new CDK()).setVisible(true);
	}
	
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();
		
		super.addNotify();
	
		if (fComponentsAdjusted)
			return;
	
		// Adjust components according to the insets
		setSize(insets().left + insets().right + d.width, insets().top + insets().bottom + d.height);
		Component components[] = getComponents();
		for (int i = 0; i < components.length; i++)
		{
			Point p = components[i].getLocation();
			p.translate(insets().left, insets().top);
			components[i].setLocation(p);
		}
		fComponentsAdjusted = true;
	}
	
	// Used for addNotify check.
	boolean fComponentsAdjusted = false;
	
	//{{DECLARE_CONTROLS
	java.awt.FileDialog openFileDialog1;
	java.awt.Label label1;
	java.awt.Label label2;
	java.awt.Label label3;
	java.awt.Label label4;
	java.awt.TextField codevName;
	java.awt.TextField chiselDesc;
	java.awt.TextField shortDescription;
	java.awt.TextField nodeToModify;
	java.awt.Checkbox nodeYes;
	CheckboxGroup nodeYNgroup;
	java.awt.Checkbox nodeNo;
	java.awt.Checkbox routeYes;
	CheckboxGroup routeYNgroup;
	java.awt.Checkbox routeNo;
	java.awt.TextField optionDescription;
	java.awt.Checkbox onValue;
	CheckboxGroup valueTypeGroup;
	java.awt.Checkbox offValue;
	java.awt.Checkbox numericValue;
	java.awt.Label defaultPrompt;
	java.awt.Label minPrompt;
	java.awt.Label maxPrompt;
	java.awt.Label incPrompt;
	java.awt.Label option;
	java.awt.TextField defaultValue;
	java.awt.TextField minValue;
	java.awt.TextField maxValue;
	java.awt.TextField incValue;
	java.awt.Button button1;
	java.awt.Button button2;
	java.awt.Button button3;
	java.awt.Button button4;
	java.awt.Button button5;
	java.awt.Button button6;
	java.awt.Button button7;
	java.awt.Button button8;
	java.awt.Button genChisel;
	java.awt.Label label5;
	java.awt.Label label7;
	java.awt.Label label8;
	java.awt.Label genResult;
	//}}
	
	//{{DECLARE_MENUS
	java.awt.MenuBar mainMenuBar;
	java.awt.Menu menu1;
	java.awt.MenuItem miNew;
	java.awt.MenuItem miOpen;
	java.awt.MenuItem miSave;
	java.awt.MenuItem miSaveAs;
	java.awt.MenuItem miExit;
	java.awt.Menu menu2;
	java.awt.MenuItem miCut;
	java.awt.MenuItem miCopy;
	java.awt.MenuItem miPaste;
	java.awt.Menu menu3;
	java.awt.MenuItem miAbout;
	//}}
	
	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == CDK.this)
				CDK_WindowClosing(event);
		}
	}
	
	void CDK_WindowClosing(java.awt.event.WindowEvent event)
	{
		setVisible(false);	// hide the Frame
		dispose();			// free the system resources
		System.exit(0);		// close the application
	}
	
	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == miOpen)
				miOpen_Action(event);
			else if (object == miAbout)
				miAbout_Action(event);
			else if (object == miExit)
				miExit_Action(event);
			else if (object == button1)
				button1_ActionPerformed(event);
			else if (object == button2)
				button2_ActionPerformed(event);
			else if (object == button3)
				button3_ActionPerformed(event);
			else if (object == button4)
				button4_ActionPerformed(event);
			else if (object == button5)
				button5_ActionPerformed(event);
			else if (object == button6)
				button6_ActionPerformed(event);
			else if (object == button7)
				button7_ActionPerformed(event);
			else if (object == button8)
				button8_ActionPerformed(event);
			else if (object == optionDescription)
				optionDescription_EnterHit(event);
		}
	}
	
	void miAbout_Action(java.awt.event.ActionEvent event)
	{
		//{{CONNECTION
		// Action from About Create and show as modal
		(new AboutDialog(this, true)).setVisible(true);
		//}}
	}
	
	void miExit_Action(java.awt.event.ActionEvent event)
	{
		//{{CONNECTION
		// Action from Exit Create and show as modal
		(new QuitDialog(this, true)).setVisible(true);
		//}}
	}
	
	void miOpen_Action(java.awt.event.ActionEvent event)
	{
		//{{CONNECTION
		// Action from Open... Show the OpenFileDialog
		int		defMode			= openFileDialog1.getMode();
		String	defTitle		= openFileDialog1.getTitle();
		String defDirectory	= openFileDialog1.getDirectory();
		String defFile			= openFileDialog1.getFile();

		openFileDialog1 = new java.awt.FileDialog(this, defTitle, defMode);
		openFileDialog1.setDirectory(defDirectory);
		openFileDialog1.setFile(defFile);
		openFileDialog1.setVisible(true);
		//}}
	}

	void button1_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 1 ));
		
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #1");
		//}}
	}

	void button2_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 2 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #2");
		//}}
	}

	void button3_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 3 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #3");
		//}}
	}

	void button4_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 4 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #4");
		//}}
	}

	void button5_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 5 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #5");
		//}}
	}

	void button6_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 6 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #6");
		//}}
	}

	void button7_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 7 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #7");
		//}}
	}

	void button8_ActionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		setOptionInfo( chiselGenerator.getOptionInfo( 8 ));
			 
		//{{CONNECTION
		// Set the text for Label...
		option.setText("Option #8");
		//}}
	}
	
	void setOptionInfo( OptionInfo oi ) {
	    optionInfo = oi;
	    if ( optionInfo.getType() == OptionInfo.OptionBoolTrue ) {
	        onValue.setState( true );
	    } else if ( optionInfo.getType() == OptionInfo.OptionBoolFalse ) {
	        offValue.setState( true );
	    } else if ( optionInfo.getType() == OptionInfo.OptionNumeric ) {
   	        numericValue.setState( true );
   	    } else {
   	        numericValue.setState( false );
   	        onValue.setState( true );
   	        optionInfo.setBool( true );
   	    }
	    optionDescription.setText( optionInfo.getDescription() );
	    minValue.setText( Integer.toString( optionInfo.getMinVal() ));
	    maxValue.setText( Integer.toString( optionInfo.getMaxVal() ));
	    incValue.setText( Integer.toString( optionInfo.getIncVal() ));
	    defaultValue.setText( Integer.toString( optionInfo.getDefaultVal() ));
	}

	void optionDescription_EnterHit(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
	    optionInfo.setDescription( optionDescription.getText() );
	}

	class SymMouse extends java.awt.event.MouseAdapter
	{
		public void mouseReleased(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == genChisel)
				genChisel_MouseReleased(event);
		}

		public void mousePressed(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == onValue)
				radioButton1_MouseClicked(event);
			else if (object == numericValue)
				radioButton2_MouseClicked(event);
			else if (object == offValue)
				offValue_MousePressed(event);
			else if (object == nodeYes)
				nodeYes_MousePressed(event);
			else if (object == nodeNo)
				nodeNo_MousePressed(event);
			else if (object == routeYes)
				routeYes_MousePressed(event);
			else if (object == routeNo)
				routeNo_MousePressed(event);
		}
	}

	void radioButton1_MouseClicked(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		optionInfo.setBool( true );
	}

	void radioButton2_MouseClicked(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		optionInfo.setNumeric();
	}

	class SymKey extends java.awt.event.KeyAdapter
	{
		public void keyReleased(java.awt.event.KeyEvent event)
		{
			Object object = event.getSource();
			if (object == optionDescription)
				t_KeyTyped(event);
			else if (object == minValue)
				minValue_KeyReleased(event);
			else if (object == defaultValue)
				defaultValue_KeyReleased(event);
			else if (object == maxValue)
				maxValue_KeyReleased(event);
			else if (object == incValue)
				incValue_KeyReleased(event);
			else if (object == nodeToModify)
				nodeToModify_KeyReleased(event);
		}
	}

	void t_KeyTyped(java.awt.event.KeyEvent event)
	{
		// to do: code goes here.
	    optionInfo.setDescription( optionDescription.getText() );
	}


	void minValue_KeyReleased(java.awt.event.KeyEvent event)
	{
		// to do: code goes here.
		optionInfo.setMinVal( minValue.getText() );
			 
	}

	void defaultValue_KeyReleased(java.awt.event.KeyEvent event)
	{
		// to do: code goes here.
		optionInfo.setDefaultVal( defaultValue.getText() );
	}

	void maxValue_KeyReleased(java.awt.event.KeyEvent event)
	{
		// to do: code goes here.
		optionInfo.setMaxVal( maxValue.getText() );
	}

	void incValue_KeyReleased(java.awt.event.KeyEvent event)
	{
		// to do: code goes here.
		optionInfo.setIncVal( incValue.getText() );
	}

	void genChisel_MouseReleased(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
	    chiselGenerator.genChisel( codevName.getText(), shortDescription.getText(), chiselDesc.getText() );
	    genResult.setText( "generated '" + chiselGenerator.getGeneratedFileName() + "'" );
	}
	
	void offValue_MousePressed(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		optionInfo.setBool( false );
	}


	void nodeYes_MousePressed(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		chiselGenerator.setNodeModifier( true );
	}

	void nodeNo_MousePressed(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		chiselGenerator.setNodeModifier( false );
	}

	void routeYes_MousePressed(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		chiselGenerator.setRouteModifier( true );
	}

	void routeNo_MousePressed(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		chiselGenerator.setRouteModifier( false );
	}

	void nodeToModify_KeyReleased(java.awt.event.KeyEvent event)
	{
		// to do: code goes here.
		chiselGenerator.setNodeType( nodeToModify.getText() );
	}
}
