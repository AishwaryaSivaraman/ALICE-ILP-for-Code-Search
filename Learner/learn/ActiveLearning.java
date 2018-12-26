package learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import util.ClauseUtils;
import util.PredicateMapUtil;
import def.JavaSourceCodeInfo;
import def.PredicateDef;

public class ActiveLearning {
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
	List<PredicateDef> previousIterQuery = new ArrayList<PredicateDef>();
	List<PredicateDef> remainingPredicates = new ArrayList<PredicateDef>();
	List<PredicateDef> originalClause = new ArrayList<PredicateDef>();
	PredicateDef methodDef;
	public static boolean previousWasContainment = false;
	// 3 cases:
	//when given negative example do FOIL	
	public HashMap<Integer, Solution> specializeQuery(HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples){
		this.labelledExamples.putAll(labelledExamples);	
//		this.previousIterQuery.addAll(labelledExamples.get(1).get(0).queryasPredicates);
//		this.remainingPredicates.addAll(labelledExamples.get(1).get(0).remainingPredicates);
//		this.originalClause.addAll(labelledExamples.get(1).get(0).originalClause);
//		methodDef = ClauseUtils.getMethodDecPredicate(previousIterQuery);
//		InductiveLearner learner = new InductiveLearner(remainingPredicates, methodDef,this.previousIterQuery);
////		learner.newPredicatesToAdd();
////		learner.newPredicatesToAddBySAT();
//		//TODOO FIX THIS
////		learner.satAssignmentForAddedPredicates();
//		learner.sortByNoOfPosCovered();
////		System.out.println(learner.positivePickedPredicates);
////		return chooseOnePredicateTdeoAdd(learner.positivePickedPredicates);
//		
//		return chooseAPredicateWithMaxExamples(learner.satisfyingSolutions.get(0).predicates);
//		if(labelledExamples.containsKey(1) && !labelledExamples.get(1).isEmpty()){
//			return learnASeparatorTopDownGreedy(labelledExamples.get(1).get(0).queryasPredicates, labelledExamples.get(1).get(0).remainingPredicates, labelledExamples.get(1).get(0).originalClause);
//		} else{
//			return learnASeparatorTopDownGreedy(labelledExamples.get(2).get(0).queryasPredicates, labelledExamples.get(2).get(0).remainingPredicates, labelledExamples.get(2).get(0).originalClause);
//		}
//		
		return learnASeparatorTopDownExhaustive(
				labelledExamples.get(1).get(0).queryasPredicates,
				labelledExamples.get(1).get(0).remainingPredicates,
				labelledExamples.get(1).get(0).originalClause);	
	}
	
	public HashMap<Integer, Solution> learnASeparatorTopDownGreedy(List<PredicateDef> previousIterquery, List<PredicateDef> remaining, List<PredicateDef> original){
		this.previousIterQuery.clear();
		this.remainingPredicates.clear();
		this.originalClause.clear();
		this.previousIterQuery.addAll(previousIterquery);
		this.remainingPredicates.addAll(remaining);
		this.originalClause.addAll(original);
		methodDef = ClauseUtils.getMethodDecPredicate(previousIterQuery);
		InductiveLearner learner = new InductiveLearner(remainingPredicates, methodDef, this.previousIterQuery,labelledExamples);
		if(!ActiveLearning.previousWasContainment){
		}
		
		learner.checkSatByContainment();
		boolean isSame = false;
		if(learner.containmentAdded){
			isSame = PredicateMapUtil.checkIfTwoPredicateListsAreEqual(learner.satisfyingSolutions.get(0).predicates,this.previousIterQuery);		
		}
		if(learner.containmentAdded && !isSame){
			ActiveLearning.previousWasContainment = true;
			return runForContainment(learner.satisfyingSolutions.get(0).predicates);
		} else{
			ActiveLearning.previousWasContainment = false;
			learner.satAssignmentForAddedPredicatesTopDownGreedy();
			return chooseAPredicateWithMaxExamples(learner.satisfyingSolutions.get(0).predicates);
		}
	}
	
	public HashMap<Integer, Solution> learnASeparatorRandom(List<PredicateDef> previousIterquery, List<PredicateDef> remaining, List<PredicateDef> original, boolean isGreedy){
		this.previousIterQuery.clear();
		this.remainingPredicates.clear();
		this.originalClause.clear();
		this.previousIterQuery.addAll(previousIterquery);
		this.remainingPredicates.addAll(remaining);
		this.originalClause.addAll(original);
		methodDef = ClauseUtils.getMethodDecPredicate(previousIterQuery);
		List<PredicateDef> copyOfRemaining = ClauseUtils.predicateListCopy(remainingPredicates);
		InductiveLearner learner = new InductiveLearner(copyOfRemaining, methodDef, this.previousIterQuery,labelledExamples);		
		//call random greedy picker. 
		learner.satAssignmentForAddedPredicatesRandom(isGreedy);
		learner.sortByNoOfPosCovered();
		return chooseAPredicateWithMaxExamples(learner.satisfyingSolutions.get(0).predicates);	
	}
	
	public HashMap<Integer, Solution> learnASeparatorTopDownExhaustive(List<PredicateDef> previousIterquery, List<PredicateDef> remaining, List<PredicateDef> original){
		this.previousIterQuery.clear();
		this.remainingPredicates.clear();
		this.originalClause.clear();
		this.previousIterQuery.addAll(previousIterquery);
		this.remainingPredicates.addAll(remaining);
		this.originalClause.addAll(original);
		methodDef = ClauseUtils.getMethodDecPredicate(previousIterQuery);
		List<PredicateDef> copyOfRemaining = ClauseUtils.predicateListCopy(remainingPredicates);
		InductiveLearner learner = new InductiveLearner(copyOfRemaining, methodDef, this.previousIterQuery,labelledExamples);
		if(!ActiveLearning.previousWasContainment){
			
		}		
		learner.checkSatByContainment();
		boolean isSame = false;
		if(learner.containmentAdded){
		 isSame = PredicateMapUtil.checkIfTwoPredicateListsAreEqual(learner.satisfyingSolutions.get(0).predicates,this.previousIterQuery);
		}
		learner.satAssignmentForAddedPredicatesTopDownExhaustive();
		learner.sortByNoOfPosCovered();
		if(learner.satisfyingSolutions.get(0).predicates.size()>3 && !learner.satisfyingSolutions.get(0).isContainment && !isSame){
			return runForParentContainment(learner.satisfyingSolutions.get(0).predicates);
		}
		
		if(learner.satisfyingSolutions.get(0).isContainment && !isSame){
			ActiveLearning.previousWasContainment = true;
			return runForContainment(learner.satisfyingSolutions.get(0).predicates);
		} else{
			ActiveLearning.previousWasContainment = false;
			return chooseAPredicateWithMaxExamples(learner.satisfyingSolutions.get(0).predicates);				
		}		
	}
	
	public HashMap<Integer, Solution> learnASeparatorTopologicalOrder(List<PredicateDef> previousIterquery, List<PredicateDef> remaining, List<PredicateDef> original, boolean isGreedy){
		this.previousIterQuery.clear();
		this.remainingPredicates.clear();
		this.originalClause.clear();
		this.previousIterQuery.addAll(previousIterquery);
		this.remainingPredicates.addAll(remaining);
		this.originalClause.addAll(original);
		methodDef = ClauseUtils.getMethodDecPredicate(previousIterQuery);
		InductiveLearner learner = new InductiveLearner(remainingPredicates, methodDef, this.previousIterQuery,labelledExamples);
		learner.checkSatByAfter();
		boolean isSame = false;
		if(learner.afterAdded){
			isSame = PredicateMapUtil.checkIfTwoPredicateListsAreEqual(learner.satisfyingSolutions.get(0).predicates,this.previousIterQuery);
		}
		
		learner.predicatedToAddBasedOnAfter(isGreedy);
		learner.sortByNoOfPosCovered();
		if(learner.satisfyingSolutions.get(0).isContainment && !isSame){
			ActiveLearning.previousWasContainment = true;
			return runForContainment(learner.satisfyingSolutions.get(0).predicates);
		} else{
			ActiveLearning.previousWasContainment = false;
			return chooseAPredicateWithMaxExamples(learner.satisfyingSolutions.get(0).predicates);				
		}	
	}
	
	
	public List<String> getNegativeExampleAsValues(HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples){
		List<JavaSourceCodeInfo> negatives = labelledExamples.get(2);
		List<String> negativeMethods = new ArrayList<String>();
		for(int j=0;j<negatives.size();j++){
			negativeMethods.add(negatives.get(j).className);
		}
		return negativeMethods;
	}
	
	public List<String> getPositiveExampleAsValues(HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples){
		List<JavaSourceCodeInfo> positives = labelledExamples.get(1);
		List<String> positiveMethods = new ArrayList<String>();
		for(int j=0;j<positives.size();j++){
			positiveMethods.add(positives.get(j).className);
		}
		return positiveMethods;
	}
		
	public HashMap<Integer, Solution> chooseOnePredicateToAdd(List<List<PredicateDef>> pickedPredicates) {
		Random randomizer = new Random();
		int randomIndex = randomizer.nextInt(pickedPredicates.size());
		List<PredicateDef> reorderd = pickedPredicates.get(randomIndex);
		this.previousIterQuery.addAll(reorderd);
		List<PredicateDef> intersection = ClauseUtils.getIntersection(this.remainingPredicates, pickedPredicates.get(randomIndex));
		String query = Learner.generatePrologQuery(this.previousIterQuery);
		ClauseUtils.appendQueryToFile(query);
		PrologAsProcess.writeToFile(query);
		PrologAsProcess.callProcess();
		return PrologAsProcess.solutions(query,this.previousIterQuery,intersection,this.originalClause);		
//		return ClauseUtils.runToGenerateResults(query, this.previousIterQuery,intersection, this.originalClause);		
	}
	
	public List<PredicateDef> removeMethodDec(List<PredicateDef> predicates){
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("methoddec")){
				predicates.remove(i);
				return predicates;
			}
		}
		return predicates;
	}
	public HashMap<Integer, Solution> chooseAPredicateWithMaxExamples(List<PredicateDef> pickedPredicates) {
		this.previousIterQuery.addAll(removeMethodDec(pickedPredicates));
		List<PredicateDef> intersection = ClauseUtils.getIntersection(this.remainingPredicates, pickedPredicates);
		String query = Learner.generatePrologQuery(this.previousIterQuery);
//		System.out.println("Finalised query :"+query);
		ClauseUtils.appendQueryToFile(query);
		PrologAsProcess.writeToFile(query);
		PrologAsProcess.callProcess();
		return PrologAsProcess.solutions(query,this.previousIterQuery,intersection,this.originalClause);		
//		return ClauseUtils.runToGenerateResults(query, this.previousIterQuery,intersection, this.originalClause);		
	}
	
	public HashMap<Integer, Solution> runForContainment(List<PredicateDef> pickedPredicates) {
		this.previousIterQuery.clear();
		this.previousIterQuery.addAll(pickedPredicates);
		List<PredicateDef> intersection = ClauseUtils.getIntersection(this.remainingPredicates, pickedPredicates);
		String query = Learner.generatePrologQuery(this.previousIterQuery);
		System.out.println("Finalised query :"+query);
		ClauseUtils.appendQueryToFile(query);
		PrologAsProcess.writeToFile(query);
		PrologAsProcess.callProcess();
		return PrologAsProcess.solutions(query,this.previousIterQuery,intersection,this.originalClause);		
//		return ClauseUtils.runToGenerateResults(query, this.previousIterQuery,intersection, this.originalClause);		
	}
	
	public HashMap<Integer, Solution> runForParentContainment(List<PredicateDef> pickedPredicates) {
		System.out.println("This is for parents");
		List<PredicateDef> copy = new ArrayList<PredicateDef>();
		copy.addAll(pickedPredicates);
		String addedId = copy.get(copy.size()-1).values.get(1);
		for(int i=0;i<this.previousIterQuery.size();i++){
			if(addedId.equals(this.previousIterQuery.get(i).values.get(0))){
				copy.add(this.previousIterQuery.get(i));
				this.previousIterQuery.remove(i);
			}
		}
		
		removeMethodDec(this.previousIterQuery);
		
		for(int i=0;i<this.previousIterQuery.size();i++){
			if(addedId.equals(this.previousIterQuery.get(i).values.get(1))){
				this.previousIterQuery.remove(i);
			}
		}
		
		copy.addAll(this.previousIterQuery);
//		this.previousIterQuery.addAll(pickedPredicates);
		List<PredicateDef> intersection = ClauseUtils.getIntersection(this.remainingPredicates, pickedPredicates);
		String query = Learner.generatePrologQuery(copy);
		System.out.println("Finalised query :"+query);
		ClauseUtils.appendQueryToFile(query);
		PrologAsProcess.writeToFile(query);
		PrologAsProcess.callProcess();
		return PrologAsProcess.solutions(query,copy,intersection,this.originalClause);		
//		return ClauseUtils.runToGenerateResults(query, this.previousIterQuery,intersection, this.originalClause);			
	}
	
	public int matchNegative(List<String> solutions, HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples){
		List<JavaSourceCodeInfo> negatives = labelledExamples.get(2);
		int count=0;
		for(int i=0;i<solutions.size();i++){
			String[] classandmethods = solutions.get(i).split(",");
			String joinclassmethod = classandmethods[0].trim()+":"+classandmethods[1].trim();
			for(int j=0;j<negatives.size();j++){
				if(joinclassmethod.equals(negatives.get(j).className)){
					return ++count;
				}
			}
		}
		return count;
	}
	
}
