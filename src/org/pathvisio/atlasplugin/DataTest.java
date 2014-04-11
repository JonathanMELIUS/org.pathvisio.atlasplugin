package org.pathvisio.atlasplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class DataTest {
	public static void main(String[]args)  {
		//String experiment = "E-GEOD-18842";
		String experiment = "E-GEOD-18842 E-GEOD-6731";
		String path = "/home/mael/outpute.txt";

		File file = null;
		PrintWriter f0 = null;
		ArrayList<String> queryList = new ArrayList<String>(Arrays.asList(experiment.split(" ")));

		String prop = "propertyValue";
		String pValue ="pValue";
		String tStat = "tStat";
		String header = "Gene_ID";
		try {			
			file = new File(path);
			f0 = new PrintWriter(new FileWriter(file,false));
			for (String query : queryList){
				header += "\t"+query+"-"+prop+"\t"+query+"-"+pValue+"\t"+query+"-"+tStat;
			}
			System.out.println(header);
			//f0.println("id"+"\t"+"Experiment_ID"+"\t"+"propertyValue"+"\t"+"pValue"+"\t"+"tStat");
			f0.println(header);
			f0.close();
			f0 = new PrintWriter(new FileWriter(file, true));

		} catch (IOException e) {
			e.printStackTrace();
		}
		/*Map<String,ArrayList<SparqlResults>> data = 
				new HashMap<String,
				new HashMap<String,ArrayList<SparqlResults>>();
		 */
		Map<String, Map<String, LinkedList<SparqlResults>>> data = 
				new HashMap<String,Map<String,LinkedList<SparqlResults>>>();

		for (String query : queryList){
			System.out.println(query);
			//new SparqlQuery (query,file,data,queryList);
			new SparqlQuery (query,data);
		}	
		//ArrayList<LinkedList> all = new ArrayList<LinkedList>();

		int i = 0;

		for(Entry<String,Map<String,LinkedList<SparqlResults>>> entry : data.entrySet()) {

			String cle = entry.getKey();
			Map<String,LinkedList<SparqlResults>> valeur = entry.getValue();
			ArrayList<LinkedList> resultList = new ArrayList<LinkedList>();
			ArrayList<ArrayList<SparqlResults>> rl = new ArrayList<ArrayList<SparqlResults>>();
			//String line = cle;
			for(Entry<String,LinkedList<SparqlResults>> expEntry : valeur.entrySet()) {
				//System.out.println(expEntry.getKey());
				resultList.add(expEntry.getValue());	
				LinkedList<SparqlResults> ll = expEntry.getValue();
				ArrayList<SparqlResults> ar = new ArrayList<SparqlResults>(ll);
				rl.add(ar);
			}
			/*for (String query : queryList){
				String tmp = "";
				for ( ArrayList<SparqlResults> ar : rl) {
					for (SparqlResults sparql : ar ){
						if (sparql.getExp().equals(query)){
							tmp = "\t"+sparql.getExp()+sparql.getProp()+"\t"+sparql.getPv()+"\t"+sparql.getTs();
						}
						i++;						
					}
				}
				if (tmp.equals("")){
					tmp = "\t"+"NA"+"\t"+"NA"+"\t"+"NA";
				}
				line += tmp;
			}
			//f0.println(line);
			//System.out.println(line);*/
			
			while (listIsEmpty(resultList)){
				String line = cle;

				String[] tab = new String[queryList.size()];
				ArrayList<String> test = new ArrayList<String>(Arrays.asList(tab));
				for (LinkedList<SparqlResults> lili : resultList) {
					i++;
					//System.out.println(lili.size());					
					SparqlResults sparql = lili.pollFirst();
					//System.out.println(sparql);
					if (!(sparql==null)){
						int index = queryList.indexOf(sparql.getExp());
						String lineTest = "\t"+sparql.getExp()+sparql.getProp()+"\t"+sparql.getPv()+"\t"+sparql.getTs();
						test.set(index, lineTest);
					}
					/*
					if (sparql==null){
						//System.out.println("toto");
						line +="\t"+"NA"+"\t"+"NA"+"\t"+"NA";
					}
					else{
						line +="\t"+sparql.getExp()+sparql.getProp()+"\t"+sparql.getPv()+"\t"+sparql.getTs();
					}
					*/
				}
				for (String ss : test){
					if (ss==null){
						line +="\t"+"NA"+"\t"+"NA"+"\t"+"NA"; 
					}
					else line += ss;
				}
				f0.println(line);
				System.out.println(line);	
			}
		}
		System.out.println("i "+i);
		f0.close();
	}
	public static boolean listIsEmpty(ArrayList<LinkedList> all){
		//boolean flag = false;
		int i=0;
		for (LinkedList<String> lili : all){
			if (lili.isEmpty()){
				i++;
			}			
		}
		if (i==all.size()){
			return false;
		}		
		return true;		
	}
}
/*for (SparqlResults results : valeur){
String line = results.getExp();
for (String query_id : queryList){
	if (results.getExp().equals(query_id)){
		//line +="\t"+probe+"\t"+propertyValue+"\t"+pValue+"\t"+tStat;
	}
	else{
		line +="\t"+"N.A"+"\t"+"N.A"+"\t"+"N.A"+"\t"+"N.A";
	}
}
}*/