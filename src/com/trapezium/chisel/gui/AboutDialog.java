package com.trapezium.chisel.gui;
import com.trapezium.chisel.Chisel;
import com.trapezium.chisel.DialogOwner;

import java.util.*;
import java.awt.*;

public class AboutDialog extends OneButtonDialog
{
    AboutPanel ap;

	public AboutDialog(DialogOwner owner, String title)
	{
		super(owner, title, "OK", true);

                setResizable(false);
                
		ap = new AboutPanel(owner, Chisel.appTitle, Chisel.version);
        add(ap);
	
		//{{REGISTER_LISTENERS
		SymContainer aSymContainer = new SymContainer();
		this.addContainerListener(aSymContainer);
		//}}
	}


	class SymContainer extends java.awt.event.ContainerAdapter
	{
		public void componentAdded(java.awt.event.ContainerEvent event)
		{
			Object object = event.getSource();
			if (object == AboutDialog.this)
				AboutDialog_ComponentAdded(event);
		}
	}

	void AboutDialog_ComponentAdded(java.awt.event.ContainerEvent event)
	{
		// to do: code goes here.
	}
}
