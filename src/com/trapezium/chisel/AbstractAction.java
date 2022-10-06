
package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;


public abstract class AbstractAction implements ActionListener
{
	/**
	 * Text to be displayed on text button or in menu
	 */
	public static final String NAME = "Name";

	/**
	 * Verbose description for help, or advanced info
	 */
	public static final String LONG_DESCRIPTION = "LongDescription";

	/**
	 * Short description to be displayed in tooltips etc
	 */
	public static final String SHORT_DESCRIPTION = "ShortDescription";

	/**
	 * Key string for returning small (16x16) icon to be displayd on button
	 */
	public static final String SMALL_ICON = "SmallIcon";

	/**
	 * mpEDIT additon: Small Icon in disabled state
	 */

	public static final String SMALL_ICON_DISABLED = "SmallIconDisabled";

	/**
	 * mpEDIT addition: provide emacs-like action identifier
	 */
	public static final String ID_STRING = "IdString";

	/**
	 * Identifier passed to PropertyChangeListeners when enabled flag has changed
	 */
	public static final String ENABLED = "enabled";

	static private ResourceBundle strings = ChiselResources.getDefaultResourceBundle();

	private boolean enabled = true;
	private PropertyChangeSupport propertyChangeSupport;

	protected String idString;
	protected String name;
	protected String shortDescription;

	public AbstractAction(String aIdString)
	{
		idString = aIdString;
		propertyChangeSupport = new PropertyChangeSupport(this);
		try
		{
			name = strings.getString("action." + idString + ".name");
			shortDescription = strings.getString("action." + idString + ".short");
		} catch ( MissingResourceException e )
			{
				System.out.println("No description for action " + idString);
			}
	}
	
	public abstract void actionPerformed( ActionEvent e );

	public String getText( String key )
	{
		if ( key.equals(NAME) )
			return name;
		else if ( key.equals(ID_STRING) )
			return idString;
		else if ( key.equals(SHORT_DESCRIPTION) )
			return shortDescription;
		return idString;
	}

	public void setText( String key, String text )
	{
		if ( key.equals(NAME) )
			setName(text);
		else if ( key.equals(SHORT_DESCRIPTION) )
			setShortDescription(text);
		else
			throw new RuntimeException("Value " + key + " cannot be set");
	}

	public String getIdString()
	{
		return idString;
	}

	public String getShortDescription()
	{
		return shortDescription;
	}

	public void setShortDescription( String text )
	{
		propertyChangeSupport.firePropertyChange( SHORT_DESCRIPTION,
			shortDescription, text );
		shortDescription = text;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String text)
	{
		propertyChangeSupport.firePropertyChange( NAME,
			name, text );
		name = text;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( boolean b )
	{
		propertyChangeSupport.firePropertyChange( ENABLED, 
			new Boolean(enabled), new Boolean(b) );
		enabled = b;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
