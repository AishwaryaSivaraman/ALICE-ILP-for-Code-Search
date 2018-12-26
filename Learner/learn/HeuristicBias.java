package learn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import def.PredicateDef;

public class HeuristicBias {

	List<String> enabledUserSelection;
	List<String> disabledUserSelection;	
	List<PredicateDef> originalPredicates;
	List<PredicateDef> regexGeneralisedPredicates;
	HashMap<Integer, List<PredicateDef>> iterMapToPred;
	int currentIter = 0;
	public static String fileNameforSession;

	//Bias when you choose the conditions in loop
	//0 is original predicate no generalisation
	//1 is original predicate with regex gen
	//2 is orgiinal pred without temporal ordering
	//3 is regex gen pred without temporal ordering
	//4 first generalisation with user selected bias.
	
	public HeuristicBias(List<String> enabledUserSelection,
			List<String> disabledUserSelection,
			List<PredicateDef> originalPredicates, List<PredicateDef> regexGenPred) {
		super();
		this.enabledUserSelection = enabledUserSelection;
		this.disabledUserSelection = disabledUserSelection;
		this.originalPredicates = new ArrayList<PredicateDef>();		
		this.originalPredicates.addAll(originalPredicates);
		this.regexGeneralisedPredicates = new ArrayList<PredicateDef>();
		this.regexGeneralisedPredicates = regexGenPred;
		
		iterMapToPred = new HashMap<Integer, List<PredicateDef>>();		
		iterMapToPred.put(currentIter, originalPredicates);
		currentIter++;
		iterMapToPred.put(currentIter,regexGenPred);
		currentIter++;
	}


	public void dropTemporalOrdering(){
		List<PredicateDef> droppedOrdering = new ArrayList<PredicateDef>();
		for(int i=0;i<originalPredicates.size();i++){
			if(!originalPredicates.get(i).predType.equals("after")){
				droppedOrdering.add(originalPredicates.get(i));
			}
		}
		
		iterMapToPred.put(currentIter, droppedOrdering);
		currentIter++;
		
		droppedOrdering = new ArrayList<PredicateDef>();
		for(int i=0;i<regexGeneralisedPredicates.size();i++){
			if(!regexGeneralisedPredicates.get(i).predType.equals("after")){
				droppedOrdering.add(regexGeneralisedPredicates.get(i));
			}
		}
		
		iterMapToPred.put(currentIter, droppedOrdering);
		currentIter++;		
	}
		
	
	public void getMethodBiasPredicate(){
		List<PredicateDef> currentIterPred = new ArrayList<PredicateDef>();
		currentIterPred.addAll(iterMapToPred.get(currentIter-1));
		List<PredicateDef> nextIterPred = new ArrayList<PredicateDef>();
		boolean isAdded = false;
		for(int i=0;i<currentIterPred.size();i++){
			if(currentIterPred.get(i).getPredType().equals("methoddec")){
				nextIterPred.add(currentIterPred.get(i));
				continue;
			} else{
				if(currentIterPred.get(i).getPredType().equals("iflike") || currentIterPred.get(i).getPredType().equals("iterlike") ){
					continue;
				} else{
					if(!isAdded){
						if(currentIterPred.get(i).getPredType().equals("methodcall"))
						{
							nextIterPred.addAll(returnAllContainingVar(currentIterPred, currentIterPred.get(i).getValues().get(0)));					
							isAdded = true;
						}
					}
				}
			}			
		}		
		iterMapToPred.put(currentIter, nextIterPred);
	}
	
	public boolean checkForPatternMatch(String regex, String actualString){				
		Pattern pattern = Pattern.compile(regex);   
		Matcher match = pattern.matcher(actualString);
		if(pattern.toString().length()<3){
			return false;
		} else{
			return match.find();
		}
	}
	
	
	public List<PredicateDef> returnAllContainingVar(List<PredicateDef> currentList,String var){
		List<PredicateDef> currentIterPred = new ArrayList<PredicateDef>();
		for(int i=0;i<currentList.size();i++){
			if(currentList.get(i).getValues().toString().contains(var)){
				if(currentList.get(i).getValues().size()>1 && currentList.get(i).predType.equals("contained")
						&& currentList.get(i).getValues().get(0).equals(var)){
					continue;
				} 
				currentIterPred.add(currentList.get(i));
			}
		}
		return currentIterPred;
	}
	
	public void optimiseQuery(Integer count){
		List<PredicateDef> optimised = reorderQueryToOptimise(iterMapToPred.get(count));
		iterMapToPred.put(count, optimised);
		
	}
	
	public List<PredicateDef> reorderQueryToOptimise(List<PredicateDef> slowQuery){
		List<PredicateDef> copy = new ArrayList<PredicateDef>();
		copy.addAll(slowQuery);
		List<PredicateDef> optimisedQuery = new ArrayList<PredicateDef>();
		boolean optimised = false;
		boolean foundAtLeastOne = false;
		PredicateDef methodDec = new PredicateDef();
		List<PredicateDef> listofCondWithNull = new ArrayList<PredicateDef>();
		
		while(!optimised){
			foundAtLeastOne = false;
			for(int i=0;i<copy.size();i++){
				if(copy.get(i).predType.equals("iflike") || copy.get(i).predType.equals("iterlike")){
					if(copy.get(i).values.get(1).contains("null")){
						listofCondWithNull.add(copy.get(i));
						copy.remove(i);
					} else{
						foundAtLeastOne = true;
						optimisedQuery.add(copy.get(i));
						copy.remove(i);
					}				
				}
				if(copy.get(i).predType.equals("methoddec")){
					methodDec = copy.get(i);
					copy.remove(i);
				}
			}			
			if(!foundAtLeastOne){
				optimisedQuery.addAll(copy);
				optimisedQuery.add(methodDec);
				optimisedQuery.addAll(listofCondWithNull);
				optimised = true;
			}
		}		
		
		return optimisedQuery;
	}		
	
	public HashMap<Integer,HashMap<String, String>>  runProglogToGenerateResults(String query){
		PrologEngine engine = new PrologEngine();
		if(!PrologEngine.hasConsulted)
			engine.consultFactBase(null);
		return engine.executeSelectedQuery(query);
	}
	
	public HashMap<Integer,Solution>  runToGenerateResults(String query,List<PredicateDef> predicates, 
			List<PredicateDef> intersection,List<PredicateDef> originalClause){
		PrologEngine engine = new PrologEngine();
//		OptimisedPrologEngine engine = new OptimisedPrologEngine();		
		if(!PrologEngine.hasConsulted){
			engine.consultFactBase(null);
		}
		return engine.executeQuery(query, predicates,intersection, originalClause);
//		return engine.consultFactsAndExecuteQuery(query, predicates);
	}
	
	public void writeQueriesToFile(){
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			String content = "Queries\n";

			fileNameforSession = "/home/whirlwind/workspace/query_"+timeStamp+".txt"; 
			fw = new FileWriter(fileNameforSession);
			bw = new BufferedWriter(fw);
			bw.write(content);
			
			System.out.println("The solutions are: ");
			for(Integer iter: iterMapToPred.keySet()){
				bw.write("Iteration: "+iter);
				bw.write("\n");
				bw.write(Learner.generatePrologQuery(iterMapToPred.get(iter)));
				bw.write("\n");
			}
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
}
