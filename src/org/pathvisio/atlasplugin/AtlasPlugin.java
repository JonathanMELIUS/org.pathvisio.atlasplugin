package org.pathvisio.atlasplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
 * @author Simon Jupp
 * @date 11/09/2013
 * Functional Genomics Group EMBL-EBI
 *
 * Example of querying the Gene Expression Atlas SPARQL endpoint from Java
 * using the Jena API (http://jena.apache.org)
 *
 */
public class AtlasPlugin implements Plugin{
	private PvDesktop desktop;

	@Override
	public void init(PvDesktop desktop) {
		this.desktop = desktop;
		AtlasAction action = new AtlasAction();
		desktop.registerMenuAction ("Plugins", action);
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub

	}
	private class AtlasAction extends AbstractAction
	{
		public AtlasAction()
		{	
			putValue (NAME, "Atlas Plugin");
			putValue(SHORT_DESCRIPTION, "Test Atlas plugin");
		}

		public void actionPerformed(ActionEvent arg0)
		{	
			AtlasWizard wizard = new AtlasWizard(desktop);
			wizard.showModalDialog(desktop.getSwingEngine().getFrame());
		}
	}
}