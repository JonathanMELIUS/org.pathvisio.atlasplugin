package org.pathvisio.atlasplugin.plugin;

/**
* Choose the way to query the Expression Atlas database.
* @author Jonathan Melius
*/

public class AtlasControler {
	
	private AbstractQuery query;
	
	public AtlasControler(AbstractQuery query){
		this.query=query;
	}
	
	public void queryID(){
		query.queryID();
	}
	public void queryExp(String txtOutput, String expInput){
		query.queryExperiment(txtOutput, expInput);
	}
	public void setAbstractQuery(AbstractQuery query){
		this.query=query;
	}
}
