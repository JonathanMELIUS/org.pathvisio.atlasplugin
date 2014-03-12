package org.pathvisio.atlasplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

public class AtlasPlugin implements Plugin
{
	private PvDesktop desktop;

//	private static final int NUM_SAMPLE_LINES = 50;

	@Override
	public void init(final PvDesktop desktop)
	{
		this.desktop = desktop;

		AtlasAction atlasAction = new AtlasAction();
		desktop.registerMenuAction ("Plugins", atlasAction);		
	}

	@Override
	public void done() {}

	//private final AtlasAction atlasAction = new AtlasAction();

	private class AtlasAction extends AbstractAction
	{	
//		private ArrayList<String> queryList;

		public AtlasAction()
		{	
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



