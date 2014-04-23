package org.pathvisio.atlasplugin.utils;

import java.util.ArrayList;

import org.pathvisio.gexplugin.ImportInformation;

public interface ObservableAtlas {
	public void addObserver(ObserverAtlas obs);
	public void notifyObservers(ArrayList<String> idExperiment);
	public void notifyObservers(ImportInformation importInformation);
	public void notifyObservers(int progress);
	public void delOneObserver(ObserverAtlas obs);
	public void delAllObservers();
}
