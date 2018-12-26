package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import learn.HeuristicBias;
import learn.Learner;
import learn.PrologEngine;
import learn.Solution;
import learn.UserBias;
import def.PredicateDef;

public class ClauseUtils {
	public static HashMap<String, PredicateDef> varPredMap = new HashMap<String, PredicateDef>();
	public static HashMap<String, String> containedValueMap = new HashMap<String, String>();
	public static HashMap<String, PredicateDef> valueContainedMap = new HashMap<String, PredicateDef>();
	
	public static List<PredicateDef> getIntersection(List<PredicateDef> original, List<PredicateDef> subset){
		List<PredicateDef> superset = new ArrayList<PredicateDef>();
		superset.addAll(original);
		for(int i=0;i<subset.size();i++){
			for(int j=0;j<superset.size();j++){
				if(superset.get(j).getPredType().equals(subset.get(i).getPredType())){
					if(superset.get(j).getValues().equals(subset.get(i).getValues())){
						superset.remove(j);
					}
				}
			}
		}			
		return superset;
	}
	
	public static PredicateDef getMethodDecPredicate(List<PredicateDef> predicates){
		PredicateDef def = new PredicateDef();
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("methoddec")){
				def.predType = predicates.get(i).predType;
				def.values = new ArrayList<String>();
				def.values.addAll(predicates.get(i).getValues());
				return def;
			}
		}
		
		return null;
	}
	
	public static HashMap<Integer,Solution>  runToGenerateResults(String query,List<PredicateDef> predicates, 
			List<PredicateDef> intersection,List<PredicateDef> originalClause){
		PrologEngine engine = new PrologEngine();
		if(!PrologEngine.hasConsulted){
			engine.consultFactBase(null);
		}
		return engine.executeQuery(query, predicates,intersection, originalClause);
	}
	
	public static void appendQueryToFile(String query){
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {						
			if(UserBias.fileNameforSession==null || UserBias.fileNameforSession.length()<=0){
				fw = new FileWriter(HeuristicBias.fileNameforSession,true);
			} else{
				fw = new FileWriter(UserBias.fileNameforSession,true);
			}
			bw = new BufferedWriter(fw);			
			
			System.out.println("Appending query ");
			bw.write("\n");
			bw.write(query);
			bw.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static List<PredicateDef> reorderContained(List<PredicateDef> predicates){
		List<PredicateDef> reordered = new ArrayList<PredicateDef>();
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("contained")){
				reordered.add(predicates.get(i));
				predicates.remove(i);
			}
		}
		reordered.addAll(predicates);
		return reordered;
	}
	
	public static List<PredicateDef> putIfLikeAtTheBeg(List<PredicateDef> predicates){
		List<PredicateDef> reordered = new ArrayList<PredicateDef>();
		List<PredicateDef> contained = new ArrayList<PredicateDef>();
		List<PredicateDef> otherPredicates = new ArrayList<PredicateDef>();
		List<PredicateDef> ifLike = new ArrayList<PredicateDef>();
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("contained")){
				contained.add(predicates.get(i));
//				predicates.remove(i);
			} else{
				if(predicates.get(i).predType.equals("iflike") || predicates.get(i).predType.equals("iterlike")){
					ifLike.add(predicates.get(i));
	//				predicates.remove(i);
				} else{
					otherPredicates.add(predicates.get(i));
				}
			} 			
		}				
		reordered.addAll(ifLike);
		reordered.addAll(otherPredicates);
		reordered.addAll(contained);
		return reordered;
	}
	
	public static List<PredicateDef> reorderMutliplePredicate(List<PredicateDef> predicates){
//		System.out.println("Before: ");
//		System.out.println(Learner.generatePrologQuery(predicates));
		
		PredicateDef methodDec = new PredicateDef();
		List<String> methods = new ArrayList<String>();
		List<String> uniqueIds = new ArrayList<String>();
		List<String> duplicateMethodIds = new ArrayList<String>();
		List<String> otherIds = new ArrayList<String>();		
		List<String> addedContainedIds = new ArrayList<String>();
		List<PredicateDef> optimisedPredicate = new ArrayList<PredicateDef>();
		
		for(int i=0;i<predicates.size();i++){			
			if(predicates.get(i).predType.equals("methodcall")){
				if(methods.contains(predicates.get(i).values.get(1))){
					duplicateMethodIds.add(predicates.get(i).values.get(0));
				} else{
					uniqueIds.add(predicates.get(i).values.get(0));
					methods.add(predicates.get(i).values.get(1));
				}
				addedContainedIds.add(predicates.get(i).values.get(0));
			} 			
		}
		
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("methoddec")){
				methodDec = predicates.get(i);
			} else{					
				if(!predicates.get(i).predType.equals("methodcall") && !predicates.get(i).predType.equals("contained") && !addedContainedIds.contains(predicates.get(i).values.get(1))){					
					otherIds.add(predicates.get(i).values.get(0));
				}			
			}
		}
		
		optimisedPredicate.add(methodDec);
		for(int i=0;i<uniqueIds.size();i++){
			optimisedPredicate.add(valueContainedMap.get(uniqueIds.get(i)));
			optimisedPredicate.add(varPredMap.get(uniqueIds.get(i)));			
		}
		
		for(int i=0;i<otherIds.size();i++){
			optimisedPredicate.add(valueContainedMap.get(otherIds.get(i)));
			optimisedPredicate.add(varPredMap.get(otherIds.get(i)));
		}
		
//		for(int i=0;i<duplicateMethodIds.size();i++){
//			optimisedPredicate.add(valueContainedMap.get(duplicateMethodIds.get(i)));
//			optimisedPredicate.add(varPredMap.get(duplicateMethodIds.get(i)));			
//		}
				
		
//		System.out.println("Before Opt Size : "+predicates.size());
//		System.out.println("After Opt Size : "+optimisedPredicate.size());
//		System.out.println("After: ");		
//		System.out.println(Learner.generatePrologQuery(optimisedPredicate));
		
		return optimisedPredicate;				
	}
	
	public static List<PredicateDef> putContainedFirst(List<PredicateDef> predicates){
		List<PredicateDef> reordered = new ArrayList<PredicateDef>();
		List<PredicateDef> contained = new ArrayList<PredicateDef>();
		List<PredicateDef> otherPredicates = new ArrayList<PredicateDef>();
		List<PredicateDef> ifLike = new ArrayList<PredicateDef>();
		List<PredicateDef> conditionContained = new ArrayList<PredicateDef>();

		
		PredicateDef methodDec = new PredicateDef();
		for(int i=0;i<predicates.size();i++){
			varPredMap.put(predicates.get(i).values.get(0), predicates.get(i));
			if(predicates.get(i).predType.equals("contained")){
				containedValueMap.put(predicates.get(i).values.get(0), predicates.get(i).values.get(1));
				valueContainedMap.put(predicates.get(i).values.get(1), predicates.get(i));				
				if((predicates.get(i).values.get(1).contains("ITERATOR") || predicates.get(i).values.get(1).contains("IF"))){
					conditionContained.add(predicates.get(i));
				} else{
					contained.add(predicates.get(i));					
				}
			} else{
				if(predicates.get(i).predType.equals("iflike") || predicates.get(i).predType.equals("iterlike")){
					ifLike.add(predicates.get(i));
				} else{
					if(predicates.get(i).predType.equals("methoddec")){
						methodDec = predicates.get(i);
					} else{						
						otherPredicates.add(predicates.get(i));
					}
				}
			} 			
		}				
		reordered.add(methodDec);
		for(int i=0;i<contained.size();i++){
			reordered.add(contained.get(i));
			String containedVar = contained.get(i).values.get(1);
			reordered.add(varPredMap.get(containedVar));
		}
		
		for(int i=0;i<conditionContained.size();i++){
			reordered.add(conditionContained.get(i));
			String containedVar = conditionContained.get(i).values.get(1);
			reordered.add(varPredMap.get(containedVar));
		}
//		reordered.addAll(contained);
//		reordered.addAll(otherPredicates);
//		reordered.addAll(conditionContained);
//		reordered.addAll(ifLike);
		return reordered;
	}
	
	public static List<PredicateDef> returnWithoutContained(List<PredicateDef> predicates){
		List<PredicateDef> withoutContained = new ArrayList<PredicateDef>();
		for(int i=0;i<predicates.size();i++){
			if(!predicates.get(i).predType.equals("contained")){
				withoutContained.add(predicates.get(i));
			}
		}
		return withoutContained;
	}
	
	public static List<PredicateDef> predicateListCopy(List<PredicateDef> original){
		List<PredicateDef> copy = new ArrayList<PredicateDef>();
		for(int i=0;i<original.size();i++){
			PredicateDef def = new PredicateDef(original.get(i).predType,original.get(i).values);
			copy.add(def);
		}
		return copy;
	}
	
	public static HashMap<Integer, Solution> returnSubstitutedQueries(List<PredicateDef> predicates, HashMap<Integer,Solution> matchingResults){
		List<String> substitutedQueries = new ArrayList<String>();
		List<PredicateDef> predicatesCopy = predicateListCopy(predicates);
		for(Integer number : matchingResults.keySet()){
			for(int j=0;j<predicatesCopy.size();j++){
				if(predicatesCopy.get(j).predType.equals("contained")){
					String var = predicatesCopy.get(j).values.get(0);					
					predicatesCopy.get(j).values.set(0,matchingResults.get(number).mapVartoSolution.get(var));
					var = predicatesCopy.get(j).values.get(1);	
					predicatesCopy.get(j).values.set(1,matchingResults.get(number).mapVartoSolution.get(var));
				} else{
					String var = predicatesCopy.get(j).values.get(0);
					predicatesCopy.get(j).values.set(0,matchingResults.get(number).mapVartoSolution.get(var));
				}
			}
			matchingResults.get(number).substitutedQuery = Learner.generatePrologQuery(predicatesCopy);
		}		
		return matchingResults;
	}

	public static List<PredicateDef> reorderForContainment(List<PredicateDef> predicates){
		List<PredicateDef> modifiedPredicates = new ArrayList<PredicateDef>();
		List<PredicateDef> containedInStructure = new ArrayList<PredicateDef>();
		modifiedPredicates.add(predicates.get(0));
		for(int i=1;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("contained") && predicates.get(i).values.get(0).equals("X")){
				modifiedPredicates.add(predicates.get(i));
				modifiedPredicates.add(predicates.get(i+1));
				i++;
			} else{
				if(predicates.get(i).predType.equals("contained") && !predicates.get(i).values.get(0).equals("X")){					
					containedInStructure.add(predicates.get(i));
					containedInStructure.add(predicates.get(i+1));
					i++;
					
				}
			}
		}
		modifiedPredicates.addAll(containedInStructure);
		return modifiedPredicates;
	}
}
