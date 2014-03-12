package org.pathvisio.atlasplugin;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}
	private AtlasPlugin plugin;

	 @Override
	 public void start(BundleContext context) throws Exception {
	    plugin = new AtlasPlugin(); 
	    context.registerService(Plugin.class.getName(), plugin, null);
	 }

	 @Override
	 public void stop(BundleContext context) throws Exception {
	    plugin.done();	 
	 }
}