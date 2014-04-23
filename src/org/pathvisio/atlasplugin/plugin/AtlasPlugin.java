package org.pathvisio.atlasplugin.plugin;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;

import org.pathvisio.atlasplugin.gui.AtlasWizard;
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
		AbstractQuery atlasQuery;
		AtlasControler atlasControler;
		AtlasWizard wizard;

		public AtlasAction()
		{	
			putValue (NAME, "Atlas Plugin");
			putValue(SHORT_DESCRIPTION, "Test Atlas plugin");
		}

		public void actionPerformed(ActionEvent arg0)
		{	
			atlasQuery = new AtlasQuery();
			atlasControler = new AtlasControler(atlasQuery);
			wizard = new AtlasWizard(desktop,atlasControler);
			atlasQuery.addObserver(wizard);
			wizard.initialization();
			wizard.showModalDialog(desktop.getSwingEngine().getFrame());
		}
	}
}