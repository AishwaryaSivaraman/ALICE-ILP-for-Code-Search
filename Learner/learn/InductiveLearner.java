package learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import util.ClauseUtils;
import def.JavaSourceCodeInfo;
import def.PredicateDef;

public class InductiveLearner {
	
	List<PredicateDef> remainingPredicates = new ArrayList<PredicateDef>();
	PredicateDef methoddec;	
	List<List<PredicateDef>> positivePickedPredicates = new ArrayList<List<PredicateDef>>();
	List<SATSolutions> satisfyingSolutions = new ArrayList<SATSolutions>();
	List<PredicateDef> previousQueryPredicates = new ArrayList<PredicateDef>();
	boolean containmentAdded = false;
	boolean afterAdded = false;
	HashMap<Integer, List<JavaSourceCodeInfo>> lablledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
	
	public InductiveLearner(List<PredicateDef> remainingPredicates, 
			PredicateDef methoddec, List<PredicateDef> previousQuery, HashMap<Integer, List<JavaSourceCodeInfo>> examples) {
//		this.remainingPredicates.addAll(remainingPredicates);
		this.remainingPredicates = ClauseUtils.predicateListCopy(remainingPredicates);
		this.methoddec = methoddec;
		this.previousQueryPredicates = previousQuery;
		this.lablledExamples = examples;
	}
	
	
	public List<PredicateDef> pickNextPredicates(){
		//iterator and if always comes with the contained/
		List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
		for(int i=0;i<remainingPredicates.size();i++){
			if(remainingPredicates.get(i).predType.equals("iterlike") ||
					remainingPredicates.get(i).predType.equals("iflike") || 
					remainingPredicates.get(i).predType.equals("methodcall") ||
					remainingPredicates.get(i).predType.equals("iter")||
					remainingPredicates.get(i).predType.equals("if") ||
					remainingPredicates.get(i).predType.equals("if") ||
					remainingPredicates.get(i).predType.equals("if"))
			{
//				if(Arrays.toString(remainingPredicates.get(i).getValues().toArray()).length()<120){
//					pickedPredicates.add(remainingPredicates.get(i));
				pickedPredicates.add(new PredicateDef(remainingPredicates.get(i).predType,remainingPredicates.get(i).values));
					remainingPredicates.remove(i);
					break;
//				}
			}
		}
		for(int i=0;i<pickedPredicates.size();i++){
			for(int j=0;j<remainingPredicates.size();j++){
				if(pickedPredicates.get(i).getValues().get(0).equals(remainingPredicates.get(j).getValues().get(1))){
					pickedPredicates.add(new PredicateDef(remainingPredicates.get(i).predType,remainingPredicates.get(i).values));
//					pickedPredicates.add(remainingPredicates.get(j));
					remainingPredicates.remove(j);
					break;
				}
			}				
		}
		return pickedPredicates;
	}
		
	public void newPredicatesToAdd(){
		PrologEngine engine = new PrologEngine();		
		int literalIndex=0;
		double maxGain = 0;
		while(remainingPredicates.size()>2){
			List<PredicateDef> pickedPredicates = pickNextPredicates();
			List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
			pickedPredicatesCopy.addAll(pickedPredicates);
			pickedPredicates.add(methoddec);		
			System.out.println("Picked clause "+Learner.generatePrologQuery(pickedPredicates));
			List<String> matchedSolutions = engine.executeForMethodDec(Learner.generatePrologQuery(pickedPredicates));
			ActiveLearning learning = new ActiveLearning();
			int noNeg = learning.matchNegative(matchedSolutions,lablledExamples);
			if(noNeg==0 && matchedSolutions.size()>0){
				positivePickedPredicates.add(pickedPredicatesCopy);				
			}				
		}		
	}

	public List<PredicateDef> pickNextPredicates(String solutionName){
		//iterator and if always comes with the contained/
		List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
		for(int i=0;i<remainingPredicates.size();i++){
			if(remainingPredicates.get(i).predType.equals("iterlike") ||
					remainingPredicates.get(i).predType.equals("iflike") || 
					remainingPredicates.get(i).predType.equals("methodcall") ||
					remainingPredicates.get(i).predType.equals("type")||
					remainingPredicates.get(i).predType.equals("catch"))
			{
//				if(Arrays.toString(remainingPredicates.get(i).getValues().toArray()).length()<120){
					pickedPredicates.add(new PredicateDef(remainingPredicates.get(i).predType,remainingPredicates.get(i).values));
					remainingPredicates.remove(i);
					break;
//				}
			}
		}
		for(int i=0;i<pickedPredicates.size();i++){
			for(int j=0;j<remainingPredicates.size();j++){
				if(pickedPredicates.get(i).getValues().get(0).equals(remainingPredicates.get(j).getValues().get(1))){
					remainingPredicates.get(j).values.set(0, solutionName);
					pickedPredicates.add(remainingPredicates.get(j));
					remainingPredicates.remove(j);
					break;
				}
			}				
		}
		return pickedPredicates;
	}
	
	
	public List<PredicateDef> subsValue(List<PredicateDef> predicates, String solName){
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("contained")){
				predicates.get(i).values.set(0, solName);
			}
			if(predicates.get(i).predType.equals("methoddec")){
				predicates.get(i).values.set(0, solName);
			}
		}
		return predicates;
	}
	
	public void newPredicatesToAddBySAT(){
		PrologEngine engine = new PrologEngine();		
		int literalIndex=0;
		double maxGain = 0;
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		while(remainingPredicates.size()>2){
			boolean isNegativeSAT =false;
			pickedPredicatesCopy = new ArrayList<PredicateDef>();
			ActiveLearning learning  = new ActiveLearning();
			List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
			List<PredicateDef> pickedPredicates = pickNextPredicates();
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			pickedPredicates.add(methoddec);
			for(int i=0;i<negativeSolutions.size();i++){														
//				methoddec.values.set(0, negativeSolutions.get(i));					
				pickedPredicates = subsValue(pickedPredicates,negativeSolutions.get(i));
//				pickedPredicates = ClauseUtils.putIfLikeAtTheBeg(pickedPredicates);
				System.out.println("Picked clause "+Learner.generatePrologQuery(pickedPredicates));
//				int matchedSolutions = engine.executeForSAT(Learner.generatePrologQuery(pickedPredicates));				
				PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
				PrologAsProcess.callProcess();
				HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);
				
				if(solutionObjects.size()>0){
					isNegativeSAT = true;
				}
			}
			if(!isNegativeSAT){
				positivePickedPredicates.add(pickedPredicatesCopy);				
			}		
		}		
	}
	
	public void satAssignmentForAddedPredicatesRandom(boolean isGreedy){
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		ActiveLearning learning = new ActiveLearning();
		List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
		List<String> positiveSolutions = learning.getPositiveExampleAsValues(lablledExamples);
		while(remainingPredicates.size()>0){
			pickedPredicatesCopy = new ArrayList<PredicateDef>();
			List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();						
			pickedPredicates.clear();
			pickedPredicates.add(methoddec);
            RandomRefinement randomRefinement = new RandomRefinement();            
			pickedPredicates.addAll(randomRefinement.pickPredicates(remainingPredicates));
//			System.out.println("Random picking query ");						
//			System.out.println(Learner.generatePrologQuery(pickedPredicates));
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
			PrologAsProcess.callProcess();
			HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);			
			Map<String, Integer> posNegCount = isFoundInSolution(negativeSolutions, positiveSolutions, solutionObjects);
			if(posNegCount.get("neg") == 0){						
				positivePickedPredicates.add(pickedPredicatesCopy);				
				SATSolutions satSolutions = new SATSolutions();
				satSolutions.noofPosCovered = posNegCount.get("pos");
				satSolutions.predicates.addAll(pickedPredicatesCopy);
				satisfyingSolutions.add(satSolutions);
				if(isGreedy || posNegCount.get("pos").intValue() == positiveSolutions.size()){
					System.out.println("breaking");
					break;
				}
			}		
		}	
	}
	
	public void checkSatByAfter(){
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		ActiveLearning learning = new ActiveLearning();
		List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
		List<String> positiveSolutions = learning.getPositiveExampleAsValues(lablledExamples);
		pickedPredicatesCopy = new ArrayList<PredicateDef>();
		List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
		AfterRefinement afterRefinement = new AfterRefinement();
		pickedPredicates.addAll(afterRefinement.addAfterRelation(previousQueryPredicates));
		if(pickedPredicates.size()>previousQueryPredicates.size()){
//			System.out.println("Query with before relation: ");
//			System.out.println(Learner.generatePrologQuery(pickedPredicates));	
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
			PrologAsProcess.callProcess();
			HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);			
			Map<String, Integer> posNegCount = isFoundInSolution(negativeSolutions, positiveSolutions, solutionObjects);
			if(posNegCount.get("neg") == 0){		
				afterAdded = true;
				positivePickedPredicates.add(pickedPredicatesCopy);				
				SATSolutions satSolutions = new SATSolutions();
				satSolutions.noofPosCovered = posNegCount.get("pos");
				satSolutions.predicates.addAll(pickedPredicatesCopy);
				satSolutions.isContainment = true;
				satisfyingSolutions.add(satSolutions);
			}
		}
	}
	
	public void checkSatByContainment(){
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		ActiveLearning learning = new ActiveLearning();
		List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
		List<String> positiveSolutions = learning.getPositiveExampleAsValues(lablledExamples);
		pickedPredicatesCopy = new ArrayList<PredicateDef>();
		List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
		TopDownASTRefinement refinementOfClause = new TopDownASTRefinement();
		pickedPredicates.addAll(refinementOfClause.addContainmentRelationAsSeparator(previousQueryPredicates));
		if(refinementOfClause.modifiedContainment){
			pickedPredicates = ClauseUtils.reorderForContainment(pickedPredicates);
//			System.out.println("Query After containment: ");
//			System.out.println(Learner.generatePrologQuery(pickedPredicates));
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
			PrologAsProcess.callProcess();
			HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);			
			Map<String, Integer> posNegCount = isFoundInSolution(negativeSolutions, positiveSolutions, solutionObjects);
			if(posNegCount.get("neg") == 0){		
				if(refinementOfClause.modifiedContainment){
					containmentAdded = true;
				}
				positivePickedPredicates.add(pickedPredicatesCopy);				
				SATSolutions satSolutions = new SATSolutions();
				satSolutions.noofPosCovered = posNegCount.get("pos");
				satSolutions.predicates.addAll(pickedPredicatesCopy);
				satSolutions.isContainment = true;
				satisfyingSolutions.add(satSolutions);
			}
		} 					
	}
	
	public void satAssignmentForAddedPredicatesTopDownGreedy(){
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		ActiveLearning learning = new ActiveLearning();
		List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
		List<String> positiveSolutions = learning.getPositiveExampleAsValues(lablledExamples);
		while(remainingPredicates.size()>1){
			pickedPredicatesCopy = new ArrayList<PredicateDef>();
			List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
			TopDownASTRefinement refinementOfClause = new TopDownASTRefinement();	
			refinementOfClause.remainingPredicates = remainingPredicates;
			pickedPredicates.clear();
			pickedPredicates.add(methoddec);
			pickedPredicates.addAll(refinementOfClause.returnPredicates());
//			System.out.println("Random picking query ");
						
//			System.out.println(Learner.generatePrologQuery(pickedPredicates));
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
			PrologAsProcess.callProcess();
			HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);			
			Map<String, Integer> posNegCount = isFoundInSolution(negativeSolutions, positiveSolutions, solutionObjects);
			if(posNegCount.get("neg") == 0){						
				positivePickedPredicates.add(pickedPredicatesCopy);				
				SATSolutions satSolutions = new SATSolutions();
				satSolutions.noofPosCovered = posNegCount.get("pos");
				satSolutions.predicates.addAll(pickedPredicatesCopy);
				satisfyingSolutions.add(satSolutions);
				break;
			}		
		}		
	}
	
	public void satAssignmentForAddedPredicatesTopDownExhaustive(){
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		ActiveLearning learning  = new ActiveLearning();
		List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
		List<String> positiveSolutions = learning.getPositiveExampleAsValues(lablledExamples);
		List<PredicateDef> copyOfremaining  = ClauseUtils.predicateListCopy(remainingPredicates);
		while(remainingPredicates.size()>0){
			pickedPredicatesCopy = new ArrayList<PredicateDef>();
			List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
			TopDownASTRefinement refinementOfClause = new TopDownASTRefinement();			
			refinementOfClause.remainingPredicates = remainingPredicates;			
			pickedPredicates.clear();
			pickedPredicates.add(methoddec);
			refinementOfClause.populatePreviousIterIds(previousQueryPredicates);			
			pickedPredicates.addAll(refinementOfClause.returnPredicates());
						
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
			PrologAsProcess.callProcess();
			HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);			
			Map<String, Integer> posNegCount = isFoundInSolution(negativeSolutions, positiveSolutions, solutionObjects);
			if(posNegCount.get("neg") == 0){		
				if(!refinementOfClause.isRandomAdded && !refinementOfClause.isParentAdded){
					pickedPredicatesCopy.remove(1);
					pickedPredicatesCopy.remove(1);
				} else{
					if(refinementOfClause.isParentAdded && !refinementOfClause.isRandomAdded){
						pickedPredicatesCopy.remove(4);
					}
				}
				positivePickedPredicates.add(pickedPredicatesCopy);				
				SATSolutions satSolutions = new SATSolutions();
				satSolutions.noofPosCovered = posNegCount.get("pos");
				satSolutions.predicates.addAll(pickedPredicatesCopy);
				satisfyingSolutions.add(satSolutions);	
				if(posNegCount.get("pos").intValue() == positiveSolutions.size()){
					System.out.println("breaking");
					break;
				}
			}		
		}		
		if(satisfyingSolutions.size()==0){
			remainingPredicates.clear();
			remainingPredicates.addAll(copyOfremaining);
			satAssignmentForAddedPredicatesRandom(false);
		}
	}
	
	
	public void sortByNoOfPosCovered(){
		Collections.sort(satisfyingSolutions, new Comparator<SATSolutions>() {
			@Override
			public int compare(SATSolutions o1, SATSolutions o2) {
				return o2.noofPosCovered - o1.noofPosCovered;
			}			
		});
	}
	
	public HashMap<String, Integer> isFoundInSolution(List<String> negative,List<String> positive, HashMap<Integer,Solution> solution){			
		int negCount = 0;
		int posCount = 0;
		
		for(Integer key : solution.keySet()){
			for(int i=0;i<negative.size();i++){
				if(solution.get(key).mapVartoSolution.get("X").equals(negative.get(i))){
					negCount ++;
				}
			}
			
			for(int i=0;i<positive.size();i++){
				if(solution.get(key).mapVartoSolution.get("X").equals(positive.get(i))){
					posCount ++;
				}
			}
		}
		HashMap<String, Integer> posNegCount = new HashMap<String, Integer>();
		posNegCount.put("pos", posCount);
		posNegCount.put("neg", negCount);
		return posNegCount;
	}
	
	public void predicatedToAddBasedOnAfter(boolean isGreedy){
		List<PredicateDef> pickedPredicatesCopy = new ArrayList<PredicateDef>();
		ActiveLearning learning  = new ActiveLearning();
		List<String> negativeSolutions = learning.getNegativeExampleAsValues(lablledExamples);
		List<String> positiveSolutions = learning.getPositiveExampleAsValues(lablledExamples);
		while(remainingPredicates.size()>1){
			pickedPredicatesCopy = new ArrayList<PredicateDef>();
			List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
			AfterRefinement refinementOfClause = new AfterRefinement();		
			refinementOfClause.populatePreviousIterIds(previousQueryPredicates);
			pickedPredicates.clear();
			pickedPredicates.add(methoddec);
			pickedPredicates.addAll(refinementOfClause.returnPredicates(remainingPredicates));
						
//			System.out.println(Learner.generatePrologQuery(pickedPredicates));
			
			pickedPredicatesCopy = ClauseUtils.predicateListCopy(pickedPredicates);	
			PrologAsProcess.writeToFile(Learner.generatePrologQuery(pickedPredicates));
			PrologAsProcess.callProcess();
			HashMap<Integer, Solution> solutionObjects = PrologAsProcess.solutions(Learner.generatePrologQuery(pickedPredicates),null,null,null);			
			Map<String, Integer> posNegCount = isFoundInSolution(negativeSolutions, positiveSolutions, solutionObjects);
			if(posNegCount.get("neg") == 0){
				if(!refinementOfClause.isRandomAdded){
					pickedPredicatesCopy.remove(1);
					pickedPredicatesCopy.remove(1);
				}
				positivePickedPredicates.add(pickedPredicatesCopy);				
				SATSolutions satSolutions = new SATSolutions();
				satSolutions.noofPosCovered = posNegCount.get("pos");
				satSolutions.predicates.addAll(pickedPredicatesCopy);
				satisfyingSolutions.add(satSolutions);				
				if(isGreedy || posNegCount.get("pos").intValue() == positiveSolutions.size()){
					System.out.println("breaking");
					break;
				}
			}		
		}
	}
}
