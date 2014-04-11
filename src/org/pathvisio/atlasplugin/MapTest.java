package org.pathvisio.atlasplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MapTest {
	

	public static void main(String[]args)  {
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("toto", "3");
		data.put("toto", "2");
		System.out.println(data);
		SparqlResults s = new SparqlResults("One", "One", "One", "One", "One");
		SparqlResults ss = new SparqlResults("Two", "Two", "Two", "Two", "Two");
		SparqlResults sss = new SparqlResults("Three", "Three", "Three", "Three", "Three");
		String init[] = { "One", "Two", "Three", "One", "Two", "Three", "Three"};

		// create one list
		/*
		LinkedList ll = new LinkedList(Arrays.asList(init));
		LinkedList lk = new LinkedList(Arrays.asList("One", "Two", "Three", "One", "Two"));
		LinkedList lm = new LinkedList(Arrays.asList("One", "Two", "Three"));
		*/
		
		LinkedList ll = new LinkedList(Arrays.asList(s));
		LinkedList lk = new LinkedList(Arrays.asList(s,ss));
		LinkedList lm = new LinkedList(Arrays.asList(s,ss,sss));
		
		ArrayList<LinkedList> all = new ArrayList<LinkedList>();
		all.add(lm);
		all.add(lk);
		all.add(ll);
		//String toto = (String) ll.pollFirst();

		//System.out.println(ll+""+lk+""+lm);
		//while (	 !ll.isEmpty() ||  !lk.isEmpty() || !lm.isEmpty() ){
		
		while (listIsEmpty(all)){
			String line ="E-ATMX-34";
			//System.out.println(ll+" "+lk+" "+lm);
			for (LinkedList<SparqlResults> lili : all) {
				SparqlResults toto = lili.pollFirst();
				//System.out.println(toto);
				if (toto==null ){
					line +="\t"+"NA";
				}
				else{
					line +="\t"+toto;
				}
			}			
			System.out.println(line);
			/*ll.pollFirst();
			lk.pollFirst();
			lm.pollFirst();*/
			//System.out.println(ll+" "+lk+" "+lm);			
		}
		//System.out.println(ll+" "+lk+" "+lm);
		/*
		System.out.println(ll);
		ll.pollFirst();
		System.out.println(ll);
		ll.pollFirst();
		System.out.println(ll);
		ll.pollFirst();
		System.out.println(ll);*/
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
