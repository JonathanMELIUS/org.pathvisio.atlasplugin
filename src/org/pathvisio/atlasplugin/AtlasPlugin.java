package org.pathvisio.atlasplugin;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.gexplugin.GexTxtImporter;
import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.SwingEngine;

public class AtlasPlugin implements Plugin
{
	private PvDesktop desktop;

	private static final int NUM_SAMPLE_LINES = 50;

	@Override
	public void init(final PvDesktop desktop)
	{
		this.desktop = desktop;

		AtlasAction atlasAction = new AtlasAction(desktop);
		desktop.registerMenuAction ("Plugins", atlasAction);		
	}

	@Override
	public void done() {}

	//private final AtlasAction atlasAction = new AtlasAction();

	private class AtlasAction extends AbstractAction
	{	
		private final PvDesktop desktop;
		private ArrayList<String> queryList;

		public AtlasAction(PvDesktop desktop)
		{	
			this.desktop = desktop;
			putValue (NAME, "Altas Plugin");
			putValue(SHORT_DESCRIPTION, "Test Altas plugin");
		}

		public void actionPerformed(ActionEvent arg0)
		{				
			if ( desktop.getSwingEngine().getEngine().getActivePathway() == null)
			{
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Open a pathway");
			}
			else if(!desktop.getSwingEngine().getGdbManager().getCurrentGdb().isConnected())
			{
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Open a Gene Database");
			}
			else 
			{
				AtlasWizard wizard = new AtlasWizard(desktop);
				wizard.showModalDialog(desktop.getSwingEngine().getFrame());
			}		
		}
	}	
}



