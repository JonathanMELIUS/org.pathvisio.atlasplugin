// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.atlasplugin;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pathvisio.atlasplugin.plugin.AtlasPlugin;
import org.pathvisio.desktop.plugin.Plugin;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}
	//private Naje naje;
	private AtlasPlugin atlasPlugin;

	 @Override
	 public void start(BundleContext context) throws Exception {
		atlasPlugin = new AtlasPlugin();
	    context.registerService(Plugin.class.getName(), atlasPlugin, null);
	 }

	 @Override
	 public void stop(BundleContext context) throws Exception {
		atlasPlugin.done();
	 }
}