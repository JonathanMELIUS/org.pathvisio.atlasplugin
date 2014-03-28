package org.pathvisio.atlasplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class SparqlQuery {
	
	public SparqlQuery (String experiment, File file){
		String sparqlEndpoint = "http://www.ebi.ac.uk/rdf/services/atlas/sparql";
		//E-GEOD-10821 E-GEOD-18842 human 
		//E-GEOD-22322 E-MTAB-901  mouse
		//E-ATMX-34 Arabidopsis
		String sparqlQuery = ""+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX dcterms: <http://purl.org/dc/terms/>"+
				"PREFIX atlasterms: <http://rdf.ebi.ac.uk/terms/atlas/>"+
				"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>"+
				"SELECT DISTINCT ?exp ?type ?propertyValue ?id ?pvalue ?tStat WHERE {"+
				"?experiment a atlasterms:Experiment ."+
				"?experiment dcterms:identifier \""+experiment+"\"^^xsd:string ."+   
				"?experiment dcterms:identifier ?exp ."+
				"?experiment atlasterms:hasAnalysis ?analysis ."+
				"?analysis atlasterms:hasExpressionValue ?value ."+
				"?value atlasterms:hasFactorValue ?factor ."+
				"?factor atlasterms:propertyType ?propertyType ."+    
				"?factor atlasterms:propertyValue ?propertyValue ."+  
				"?value atlasterms:pValue ?pvalue ."+
				"?value atlasterms:tStatistic ?tStat ."+
				"?value atlasterms:isMeasurementOf ?probe ."+
				"?probe atlasterms:dbXref ?dbXref ."+
				"?dbXref rdf:type ?type ."+
				//"?dbXref rdf:type atlasterms:EnsemblDatabaseReference ."+
				//"filter ( ?type = atlasterms:EnsemblDatabaseReference)"+
				"?dbXref dcterms:identifier ?id ."+
				"}";

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();		
		System.out.println(dateFormat.format(date));

		// create the Jena query using the ARQ syntax (has additional support for SPARQL federated queries)
		Query query = QueryFactory.create(sparqlQuery, Syntax.syntaxARQ) ;

		ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(query.toString());

		QueryEngineHTTP httpQuery = new QueryEngineHTTP(sparqlEndpoint,parameterizedSparqlString.asQuery());
		// execute a Select query
		ResultSet results = httpQuery.execSelect();

		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		date = new Date();
		System.out.println(dateFormat.format(date));

		//String path = "/home/mael/outpute.txt";
		//File file = new File(path);
		PrintWriter f0 = null;
		try {
			//file = new File(path);
			f0 = new PrintWriter(new FileWriter(file, true));
			//f0.println("id"+"\t"+"pValue"+"\t"+"tStat");


			int i=0;

			while (results.hasNext()) {
				QuerySolution solution = results.next();
				// get the value of the variables in the select clause
				//String type = solution.get("type").asResource().getLocalName();
				String type = solution.getResource("type").getLocalName();
				if (type.equals("EnsemblDatabaseReference")){
					String id = solution.get("id").asLiteral().getLexicalForm();
					String pValue = solution.get("pvalue").asLiteral().getLexicalForm();
					String tStat = solution.get("tStat").asLiteral().getLexicalForm();
					String exp_id = solution.get("exp").asLiteral().getLexicalForm();
					String propertyValue = solution.get("propertyValue").asLiteral().getLexicalForm();
					f0.println(id+"\t"+exp_id+"\t"+propertyValue+"\t"+pValue+"\t"+tStat);
					i++;
				}
				// print the output to stdout expressionValue +
				
				//System.out.println(type);
				//System.out.println(id + "\t" + pValue + "\t" + tStat);
			}
			f0.close();
			System.out.println("nb"+i);
			
			dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			date = new Date();
			System.out.println(dateFormat.format(date));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
