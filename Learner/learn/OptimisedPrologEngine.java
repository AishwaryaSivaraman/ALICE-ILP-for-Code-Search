package learn;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

//import org.jpl7.Query;



import jpl.Query;
import def.PredicateDef;

public class OptimisedPrologEngine {

	boolean hasConsulted;
	List<String> matchedSolutions = new ArrayList<String>();
	HashMap<Integer,HashMap<String, String>> solutions = new HashMap<Integer, HashMap<String, String>>();
	HashMap<Integer, Solution> solutionObjects = new HashMap<Integer, Solution>();
	
	
	public void consultFactBase(String path){
//		String t1 = "consult('/home/whirlwind/runtime-Critics_Search/NEW_JDT9801/facts.pl')";		
//		executeSelectedQuery(");		
		String t2 = "use_module(library(regex)).";
		
		System.out.println( t2 + (Query.hasSolution(t2) ? "succeeded" : "failed"));			

		String t1 = "consult('"+path+"')";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		System.out.println( t1 + (Query.hasSolution(t1) ? "succeeded" : "failed"));		
//		hasConsulted = true;
	}
	
	public HashMap<Integer,Solution> consultFactsAndExecuteQuery(String query,List<PredicateDef> predicates){
		String directory = "/home/whirlwind/workspace/facts/";
		File dir = new File(directory);
		File [] files = dir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			public int compare( File a, File b ) {
		        return a.getName().compareTo( b.getName() );
		    }
		});
	    for (int i = 0; i < files.length; i++){
	        if (files[i].isFile()){ 
		        	if(files[i].getAbsolutePath().toString().contains(Learner.factBaseName)){
		            System.out.println(files[i]);
		            consultFactBase(files[i].getAbsolutePath());
		    		System.out.println("Query is "+ query);
		    		try{
		    			executeQuery(query, predicates);
		    		}catch(Exception e){
		    			System.out.println("Error in file "+files[i]);
		    		}
		        }
	        }
	    }
	    System.out.println("The size of the solution object is "+solutionObjects.size());
	    return solutionObjects;
	}
	
	public HashMap<Integer,Solution> executeQuery(String query,List<PredicateDef> predicates){			
		List<String> matchedResults = new ArrayList<>();		
		Query q = new Query(query);
		
		Integer iter = 0;
		try{
		q.allSolutions();
		}catch(Exception e){
			System.out.println(e.toString());
		}
		while(q.hasMoreSolutions()){
			HashMap<String, String> currentSol = new HashMap<String, String>();
			Solution sol = new Solution();
			String currentSolution = "";
			for(Object key : q.nextSolution().keySet()){
				currentSol.put((String) key, q.nextSolution().get(key).toString());
				 currentSolution += key +"="+q.nextSolution().get(key).toString()+"\n";
			}				
			
			if(!matchedSolutions.contains(currentSolution)){
				matchedSolutions.add(currentSolution);	
				System.out.println("The solution is "+currentSolution);		
				sol.mapVartoSolution.putAll(currentSol);
				sol.queryAsString = query;
				sol.queryforSolution.addAll(predicates);
				solutionObjects.put(iter, sol);
				solutions.put(iter, currentSol);
				iter++;
			}							
		}				
		
		return solutionObjects;
	}
}
