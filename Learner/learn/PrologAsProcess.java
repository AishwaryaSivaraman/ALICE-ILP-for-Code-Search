package learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import util.PredicateMapUtil;
import def.PredicateDef;

public class PrologAsProcess {
	public static HashMap<Integer, Solution> solutionObjects = new HashMap<Integer, Solution>();
	public static HashMap<Integer,HashMap<String, String>> solutions = new HashMap<Integer, HashMap<String, String>>();

	
	public static void writeToFile(String query){		
		
		String preamble = ":- [-factbase]. \n"+
								":- use_module(library(regexp)).\n"+
								":- table(contained/2).\n"+
								":- table(iflike/2).\n"+
								":- table(iterlike/2).\n"+
								":- table(before/2).\n"+
								"iflike(X,Y) :- if(X,Z), regexp(Y,Z,[]).\n"+
								"iterlike(X,Y) :- iterator(X,Z), regexp(Y,Z,[]).\n"+
								"contained(X,Y) :- contains(X,Y). \n"+
								"contained(X,Z) :- contains(X,Y), contained(Y,Z).\n"+
								"before(X,Y) :- after(Y,X).\n"+
								"before(X,Z) :- after(Y,X), before(Y,Z).\n";
		
//		String goal = "goal(X) :-"+query+".\n";
//		System.out.println("The query is "+query);
		
		String goal = "goal("+returnStringofIds()+") :-"+query+".\n";
//		String output = "start_up :- forall(goal(X), (write(X), nl)),halt.";
		String output = "start_up :- forall(goal("+returnStringofIds()+"), (write(["+returnStringofIds()+"]), nl)),halt.";
		PrintWriter writer;
		try {
			writer = new PrintWriter("process.pl", "UTF-8");
			writer.println(preamble);
			writer.println(goal);
			writer.println(output);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static String returnStringofIds(){
		int i;
		StringBuilder builder = new StringBuilder();
		for(i=0; i<PredicateMapUtil.variableIds.size()-1;i++){
			builder.append(PredicateMapUtil.variableIds.get(i));
			builder.append(",");
		}
		builder.append(PredicateMapUtil.variableIds.get(i));
		return builder.toString();
	}
	
	public static void callProcess(){
		try {
			List<String> args  = new ArrayList<String>();
			args.add("/bin/bash");
			args.add("script.sh");
			ProcessBuilder builder = new ProcessBuilder(args);
			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader reader =
					new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new 
	                 InputStreamReader(p.getErrorStream()));
			
			String s;
					while ((s = reader.readLine()) != null) {
					}
					while ((s = stdError.readLine()) != null) {
					}
					
			
			int val = p.waitFor();
			System.out.println("Process exited with code = " + val);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<Integer, Solution> solutions(String query, List<PredicateDef> predicates,
			List<PredicateDef> intersection,List<PredicateDef> original){
		solutionObjects.clear();
		List<String> methodNames = new ArrayList<String>();
		File file = new File("/home/whirlwind/answer.txt");
		Integer iter = 0;
		  String st;
		  try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
			while ((st = br.readLine()) != null){
			    String[] variableValues = st.substring(st.indexOf("[") + 1, st.indexOf("]")).split(",");
		    	String methodName = variableValues[0];
				if(!methodNames.contains(methodName)){
					methodNames.add(methodName);
					Solution sol = new Solution();
					String currentSolution = "";
					HashMap<String, String> currentSol = populateVariableIdWithValue(variableValues);
					sol.mapVartoSolution.putAll(currentSol);
					sol.queryAsString = query;
					if(predicates != null){
						sol.queryforSolution.addAll(predicates);
						sol.originalSpecialisedPredicates.addAll(original);
						sol.remainingPredicates.addAll(intersection);
					}
					solutionObjects.put(iter, sol);
					solutions.put(iter, currentSol);
					iter++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		  
//	   solutionObjects.put(methodNames.size(), null);
	   return solutionObjects;
	}
	
	public static HashMap<String, String> populateVariableIdWithValue(String[] solution){
		HashMap<String, String> solutionMap = new HashMap<String, String>();
		for(int i=0;i<PredicateMapUtil.variableIds.size();i++){
			solutionMap.put(PredicateMapUtil.variableIds.get(i), solution[i]);
		}
		return solutionMap;
	}
}
