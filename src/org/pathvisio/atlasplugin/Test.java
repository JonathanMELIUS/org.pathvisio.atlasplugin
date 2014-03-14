package org.pathvisio.atlasplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	private static String gene_ID = "Gene ID";	
	public static void main(String[]args)  {
		ArrayList<String> selectedTissues = new ArrayList<String>(Arrays.asList("heart","liver"));
		ArrayList<String> result = new ArrayList<String>();
		String organQuery="&queryFactorValues=heart";
		/*for (String organ : selectedTissues){
			organQuery += "&queryFactorValues="+organ;
		}*/	

		URL url = null;
		try {
			url = new URL("http://www.ebi.ac.uk/gxa/experiments/E-MTAB-513.tsv?"+
					"accessKey=&serializedFilterFactors="+
					"&queryFactorType=ORGANISM_PART" +
					"&rootContext=&heatmapMatrixSize=50"+
					"&displayLevels=false&displayGeneDistribution=false" +
					"&geneQuery=&exactMatch=true&_exactMatch=on&_geneSetMatch=on"+
					organQuery+
					"&_queryFactorValues=1" +
					"&specific=true" +
					"&_specific=on" +
					"&cutoff=0.5");
		}
		catch (MalformedURLException e1) {
			e1.printStackTrace();
		}			

		try {
			
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream("/home/mael/dodo");		
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			ArrayList<Integer> indexList = new ArrayList<Integer>();
			String line;
			boolean dataRow = false;
			while ((line = br.readLine()) != null )
			{	
				String tmp = "";
				if (dataRow){
					Pattern p = Pattern.compile("\t");
					Matcher m = p.matcher(line);
					String replace = m.replaceAll("\t0");
					//System.out.println("1-----"+replace);
					ArrayList<String> data = new ArrayList<String>(Arrays.asList(replace.split("\t")));
					for (int index : indexList){
						tmp +=data.get(index)+"\t";
					}
					//System.out.println("data"+tmp);
					
				}
				if ( line.contains(gene_ID) ) {
					ArrayList<String> data = new ArrayList<String>(Arrays.asList(line.split("\t")));
					for ( String header : data){
						if ( header.equals(gene_ID) || selectedTissues.contains(header) ) {
							int index = data.lastIndexOf(header);
							indexList.add(index);
							tmp +=header+"\t";
							//System.out.println(index);
						}    					
					}
					dataRow = true;
					//System.out.println(tmp);
				}
				if (tmp!="")result.add(tmp);
			}
			br.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String ligne : result){
			System.out.println(ligne);
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream("/home/mael/fefe"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    for (String club : result)
	        pw.println(club);
	    pw.close();
		
	}
}

