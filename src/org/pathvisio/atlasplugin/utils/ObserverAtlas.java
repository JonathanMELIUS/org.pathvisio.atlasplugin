package org.pathvisio.atlasplugin.utils;

import java.util.ArrayList;

import org.pathvisio.gexplugin.ImportInformation;

public interface ObserverAtlas {
	public void update(ArrayList<String> idExperiment);
	public void update(ImportInformation importInformation);
	public void update(int progress);
}
