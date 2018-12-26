package learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import jpl.Compound;
import jpl.Query;
import jpl.Term;

import org.apache.commons.lang3.StringUtils;
//import org.jpl7.Query;









import def.PredicateDef;


public class PrologEngine {

	public static boolean hasConsulted;
	List<String> matchedSolutions = new ArrayList<String>();
	HashMap<Integer,HashMap<String, String>> solutions = new HashMap<Integer, HashMap<String, String>>();
	HashMap<Integer, Solution> solutionObjects = new HashMap<Integer, Solution>();
	private static final int delay = 0;
	private static final Term delayGoal = new Compound("sleep", new Term[] { new jpl.Integer(delay)});
	
	public void consultFactBase(String currentDirectory){
//		String t1 = "consult('/home/whirlwind/runtime-Critics_Search/NEW_JDT9801/facts.pl')";		
//		executeSelectedQuery(");		
		
		String t2 = "use_module(library(regex)).";
		String t1 = "consult('/home/whirlwind/workspace/factsFromExtractor.pl')";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		System.out.println( t2 + (Query.hasSolution(t2) ? "succeeded" : "failed"));	
		
		System.out.println( t1 + (Query.hasSolution(t1) ? "succeeded" : "failed"));		
		hasConsulted = true;
	}
	
	public void consultFactBaseWithFileName(String consultingFactBaseName){
//		String t2 = "use_module(library(regex)).";
//		String t3 = "use_module(library(tabling)).";
//		String t1 = "consult('/home/whirlwind/workspace/factsFromExtractor.pl')";
		String t1 = "consult('/home/whirlwind/workspace/"+consultingFactBaseName+"')";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
//		Query.hasSolution(t2);
//		Query.hasSolution(t3);
		Query.hasSolution(t1);		
				
		Query.hasSolution("consult('/home/whirlwind/workspace/rules.pl')");
//		System.out.println( t2 + ( ? "succeeded" : "failed"));	
		
//		System.out.println( t1 + (Query.hasSolution(t1) ? "succeeded" : "failed"));		
		hasConsulted = true;
	}
	
	public void consultRules(){
		String consultString = "consult(rules)";
		String consultWithPath = "consult('/home/whirlwind/workspace/rules.pl')";
//		String t2 = "use_module(library(regexp)).";
//		Query.hasSolution(t2);
		Query q1 =new Query(consultString);
//		Query q2 =new Query(consultWithPath);
		
		System.err.println("consultq1 " + (q1.hasSolution() ? "succeeded" : "failed"));
		
		System.out.println("consulted");
//		System.err.println("consultq2 " + (q2.hasSolution() ? "succeeded" : "failed"));
		hasConsulted = true;
	}
	
	public List<String> executeSelectedQuery(String query,List<PredicateDef> predicateList){			
		List<String> matchedResults = new ArrayList<>();		
		Query q = new Query(query);
		while(q.hasMoreSolutions()){
			String currentSolution = q.nextSolution().get("X").toString(); 
//			System.out.println("The solution is "+currentSolution);			
			currentSolution = StringUtils.substringBetween(currentSolution,"(", ")");
			if(!matchedSolutions.contains(currentSolution)){
				matchedSolutions.add(currentSolution);	
//				ResultInfo info = populateSearchInfoObject(currentSolution,query,predicateList);
//				if(info!=null)
//					matchedResults.add(info);
			}				
		}				
		return matchedSolutions;
	}
	
	
	public HashMap<Integer,HashMap<String, String>> executeSelectedQuery(String query){			
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
			String currentSolution = "";
			for(Object key : q.nextSolution().keySet()){
				currentSol.put((String) key, q.nextSolution().get(key).toString());
				 currentSolution += key +"="+q.nextSolution().get(key).toString()+"\n";
			}				
			
			if(!matchedSolutions.contains(currentSolution)){
				matchedSolutions.add(currentSolution);	
				System.out.println("The solution is "+currentSolution);		
				solutions.put(iter, currentSol);
				iter++;
			}							
		}				
		
		return solutions;
	}
	
	public static void delay() {
		new Query(delayGoal).hasSolution();
	}
	
	public HashMap<Integer,Solution> executeQuery(String query,List<PredicateDef> predicates,
			List<PredicateDef> intersection,List<PredicateDef> original){
		System.out.println("Query is :"+query);
		List<String> matchedResults = new ArrayList<>();
		List<String> matchedResultsForMethods = new ArrayList<>();
		Query q = new Query(query);
		Integer iter = 0;

//		try{		
//			q.allSolutions();
//		}catch(Exception e){
//			System.out.println(e.toString());
//		}
//		System.out.println("in here done");

//		java.util.Hashtable[] solutions = q.allSolutions();
//		
//		for(int i=0;i<solutions.length;i++){
//			System.out.println(solutions[i].keySet());
//		}
		java.util.Hashtable solution;
//		java.util.Hashtable[] solutions = q.nSolutions(query, 20);
//		while (q.hasMoreSolutions()) {
//			solution = q.nextSolution();
//			System.out.println(solution.keySet());
//		}
		
		
//		for(int i=0;i<solutions.length;i++){
//			System.out.println(solutions[i].get(key));
//		}
		
		while(q.hasMoreSolutions()){
			HashMap<String, String> currentSol = new HashMap<String, String>();
			Solution sol = new Solution();
			String currentSolution = "";
			String methodName="";
			solution = q.nextSolution();
			for(Object key : solution.keySet()){
				currentSol.put((String) key, q.nextSolution().get(key).toString());
				 currentSolution += key +"="+q.nextSolution().get(key).toString()+"\n";
				 if(q.nextSolution().get(key).toString().contains(":")){
						methodName = q.nextSolution().get(key).toString(); 
						methodName = StringUtils.substringBetween(currentSolution,"(", ")");
				}
			}				
			
			if(!matchedResultsForMethods.contains(methodName)){
				matchedResultsForMethods.add(methodName);	
//				System.out.println("The solution is "+currentSolution);		
				sol.mapVartoSolution.putAll(currentSol);
				sol.queryAsString = query;
				sol.queryforSolution.addAll(predicates);
				sol.originalSpecialisedPredicates.addAll(original);
				sol.remainingPredicates.addAll(intersection);
				solutionObjects.put(iter, sol);
				solutions.put(iter, currentSol);
				iter++;
			}							
		}				
		
		delay();
		
//		System.out.println("Number of solution "+solutions.length);
		return solutionObjects;
	}
	
	public HashMap<Integer,Solution> executeQueryForEval(String query,List<PredicateDef> predicates,
			List<PredicateDef> intersection,List<PredicateDef> original){
		
		query = query+".";
		System.out.println("Query is :"+query);
		List<String> matchedResults = new ArrayList<>();
		List<String> matchedResultsForMethods = new ArrayList<>();
		Query q = new Query(query);
		Integer iter = 0;

		java.util.Hashtable solution;

		int count=0;
		
		String methodName;
//		java.util.Hashtable[] solutions = new Hashtable<Object, Object>[10000];
		List<java.util.Hashtable> solutions = new ArrayList<Hashtable>();
//		for(int i=0;i<solutions.length;i++){
//			for(Object key : solutions[i].keySet()){
//				//System.out.println(key+":"+sol.get(key));
//				if(solutions[i].get(key).toString().contains(":")){
//					methodName = solutions[i].get(key).toString(); 
//					methodName = StringUtils.substringBetween(solutions[i].get(key).toString(),"(", ")").split(",")[0].trim()+StringUtils.substringBetween(solutions[i].get(key).toString(),"(", ")").split(",")[1].trim();
//					if(!matchedResults.contains(methodName)){
//						matchedResults.add(methodName);
//					}
//			 }
//		  }
//		}
		while(q.hasMoreSolutions()){
			count++;
//			java.util.Hashtable sol = q.nextSolution();
			solutions.add(q.nextSolution());
		}
		
		for(int i=0;i<solutions.size();i++){
			for(Object key : solutions.get(i).keySet()){
				//System.out.println(key+":"+sol.get(key));
				if(solutions.get(i).get(key).toString().contains(":")){
					methodName = solutions.get(i).get(key).toString(); 
					methodName = StringUtils.substringBetween(solutions.get(i).get(key).toString(),"(", ")").split(",")[0].trim()+StringUtils.substringBetween(solutions.get(i).get(key).toString(),"(", ")").split(",")[1].trim();
					if(!matchedResults.contains(methodName)){
						matchedResults.add(methodName);
					}
			 }
		  }
		}
//		while(q.hasMoreSolutions()){
//			count ++;
//			java.util.Hashtable sol = q.nextSolution();
//			for(Object key : sol.keySet()){
//				//System.out.println(key+":"+sol.get(key));
//				if(q.nextSolution().get(key).toString().contains(":")){
//					methodName = q.nextSolution().get(key).toString(); 
//					methodName = StringUtils.substringBetween(currentSolution,"(", ")").split(",")[0].trim()+StringUtils.substringBetween(currentSolution,"(", ")").split(",")[1].trim();
//					currentSol.put(key, methodName);
//			 }
//			}
////			System.out.println(count);
//		}

//		delay();
//		java.util.Hashtable muraliSolution = new Hashtable(100000);
//
//		while(q.hasMoreSolutions()){
//			java.util.Hashtable sol = q.nextSolution();
//			for(Object key : sol.keySet()){
//				muraliSolution.put(key,sol.get(key));
//			}
//		}

//		System.out.println("Count Number of Murali Solution "+muraliSolution.size());
//		System.out.println("Number of solution "+solutions.length);
		System.out.println("Count Number of solution "+solutions.size());
//		System.out.println("Count Number of solution method "+count);
		System.out.println("Count Number of solution method "+matchedResults.size());
		solutionObjects.put(count,null);
		return solutionObjects;
	}
	
	public List<String> executeForMethodDec(String query){
		List<String> matchedResults = new ArrayList<>();		
		Query q = new Query(query);
		Integer iter = 0;
		try{
		q.allSolutions();
		}catch(Exception e){
			System.out.println(e.toString());
		}
		while(q.hasMoreSolutions()){
			String currentSolution = "";
			for(Object key : q.nextSolution().keySet()){
				if(q.nextSolution().get(key).toString().contains(":")){
					currentSolution = q.nextSolution().get(key).toString(); 
					currentSolution = StringUtils.substringBetween(currentSolution,"(", ")");
				}
//				 currentSolution += key +"="+q.nextSolution().get(key).toString()+"\n";
			}				
			
			if(!matchedResults.contains(currentSolution)){
				matchedResults.add(currentSolution);	
				System.out.println("The solution is "+currentSolution);		
			}							
		}				
		
		return matchedResults;
	}
	
	public int executeForSAT(String query){
		List<String> matchedResults = new ArrayList<>();		
		Query q = new Query(query);
		Integer iter = 0;
		int solCount=0;
		try{
		q.allSolutions();
		}catch(Exception e){
			System.out.println(e.toString());
		}
		while(q.hasMoreSolutions()){
			String currentSolution = "";
			for(Object key : q.nextSolution().keySet()){
				if(q.nextSolution().get(key).toString().contains(":")){
					currentSolution = q.nextSolution().get(key).toString(); 
					currentSolution = StringUtils.substringBetween(currentSolution,"(", ")");
				}
			}				
			solCount++;
			if(!matchedResults.contains(currentSolution)){
				matchedResults.add(currentSolution);	
				System.out.println("The solution is "+currentSolution);		
			}							
		}				
		
		return solCount;
	}

	public HashMap<Integer,Solution> executeWithoutContained(String originalQuery, String query,List<PredicateDef> predicates,
			List<PredicateDef> intersection,List<PredicateDef> original){
		System.out.println("Query is :"+query);
		List<String> matchedResults = new ArrayList<>();
		List<String> matchedResultsForMethods = new ArrayList<>();
		Query q = new Query(query);
		Integer iter = 0;

		int count = 0;
		while(q.hasMoreSolutions()){
			HashMap<String, String> currentSol = new HashMap<String, String>();
			String currentSolution = "";
			Solution sol = new Solution();
			String methodName="";
			count++;
			for(Object key : q.nextSolution().keySet()){				
				 currentSolution += key +"="+q.nextSolution().get(key).toString()+"\n";
				 if(q.nextSolution().get(key).toString().contains(":")){
						methodName = q.nextSolution().get(key).toString(); 
						methodName = StringUtils.substringBetween(currentSolution,"(", ")").split(",")[0].trim()+StringUtils.substringBetween(currentSolution,"(", ")").split(",")[1].trim();
						currentSol.put((String) key, methodName);
				 } else{
					 currentSol.put((String) key, q.nextSolution().get(key).toString());
				}
			}
			
//			if(!matchedResultsForMethods.contains(methodName)){
//				matchedResultsForMethods.add(methodName);	
//				System.out.println("The solution is "+currentSolution);		
				sol.mapVartoSolution.putAll(currentSol);
				sol.queryAsString = originalQuery;
				sol.queryforSolution.addAll(predicates);
				sol.originalSpecialisedPredicates.addAll(original);
				sol.remainingPredicates.addAll(intersection);
				solutionObjects.put(iter, sol);
				solutions.put(iter, currentSol);
				iter++;
//			}
		}
		
		System.out.println("Number of solutions: "+count);
		
		return solutionObjects;
	}
	

	public HashMap<Integer, Solution> executeForContainedSat(HashMap<Integer, Solution> matchedResults){
		HashMap<Integer, Solution> satisfyingSolutions = new HashMap<Integer, Solution>();
		Integer count = 0;
		for(Integer number: matchedResults.keySet()){
			Query q = new Query(matchedResults.get(number).substitutedQuery);
			while(q.hasMoreSolutions()){
				satisfyingSolutions.put(count, matchedResults.get(number));
			}
		}
		return satisfyingSolutions;
	}
}
