package learn;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ClauseUtils;
import util.PredicateMapUtil;
import def.PredicateDef;


public class Learner {

	//get the facts and apply IB to generate equivalence classes?
	
	//I think EQ 
	static public HashMap<String, Integer> TypeFrequency;
	static public HashMap<String, Integer> MethodFrequency;
	public static HashMap<String, List<String>> containedMap = new HashMap<String, List<String>>();
	public static HashMap<String, List<String>> afterMap = new HashMap<String, List<String>>();
	
	List<PredicateDef> originalPredicates = new ArrayList<PredicateDef>();
	List<PredicateDef> regexGeneralisedPredicates = new ArrayList<PredicateDef>();
	List<String> predicateString = new ArrayList<String>();
	public HashMap<Integer,HashMap<String, String>> solutions;
	public HashMap<Integer, Solution> solutionObjects;
	
	PrologEngine engine = new PrologEngine();
	public static String factBaseName;
	
	public void init(List<PredicateDef> def,Map type, Map method, List<String> enabledStrings, List<String> disabledStrings,
			List<PredicateDef> regexGenPred) throws Exception{
		
		TypeFrequency = new HashMap<String, Integer>();
		TypeFrequency.putAll(type);
		
		MethodFrequency = new HashMap<String, Integer>();
		MethodFrequency.putAll(method);
		
//		System.out.println("Original Query : ");		
//		System.out.println(generatePrologQuery(def));
		
//		System.out.println("Regex generalised query : ");		
//		System.out.println(generatePrologQuery(regexGenPred));
		
		
		Variabilize variabilize = new Variabilize();		
//		this.predicates = variabilize.variablize(def);
		List<PredicateDef> originalCopy = ClauseUtils.predicateListCopy(def);
		
		HashMap firstChildren = PredicateMapUtil.returnAllFirstContainedChildren(def);
		Learner.containedMap = PredicateMapUtil.populateAllChildren(firstChildren);
//		PredicateMapUtil.printMyChildren(containedMap);
		
		firstChildren = PredicateMapUtil.returnAllFirstAfterChildren(def);
		Learner.afterMap = PredicateMapUtil.populateAllChildren(firstChildren);
		PredicateMapUtil.removeContainedInAfter();
		
//		PredicateMapUtil.printMyChildren(afterMap);
		
		this.originalPredicates = variabilize.variablizePredicates(def);
		this.regexGeneralisedPredicates = variabilize.variablizePredicates(regexGenPred);
		
		
		PredicateMapUtil.updateidPredicateMap(this.regexGeneralisedPredicates);
		PredicateMapUtil.updatePredicateValueIDMap(this.originalPredicates);
		
		//call the user bias
		
		if(enabledStrings.size()>0){
			UserBias bias = new UserBias(enabledStrings, disabledStrings, originalCopy, this.originalPredicates,this.regexGeneralisedPredicates);
	//		bias.dropTemporalOrdering();
	//		bias.keepUserSelectedExample();
			bias.generateClauseFromUserFeatures();
	//		bias.optimiseQuery(4);
			Integer count  =0;
			for(Integer iter : bias.iterMapToPred.keySet()){
	//			System.out.println("Clause in iter "+iter);
	//			System.out.println(generatePrologQuery(bias.iterMapToPred.get(iter)));
				count = iter;
			}	
			List<PredicateDef> reordered = ClauseUtils.putContainedFirst(bias.iterMapToPred.get(count));
			reordered = ClauseUtils.reorderMutliplePredicate(reordered);
			bias.iterMapToPred.put(count, reordered);
			bias.writeQueriesToFile();
			String withoutContained = generatePrologQuery(ClauseUtils.returnWithoutContained(reordered));
		//		solutions = bias.runProglogToGenerateResults(generatePrologQuery(bias.iterMapToPred.get(count)));		
			List<PredicateDef> intersection = ClauseUtils.getIntersection(bias.iterMapToPred.get(count-1),bias.iterMapToPred.get(count));
		
//		solutionObjects =  bias.runToGenerateOptResults(withoutContained,generatePrologQuery(reordered), bias.iterMapToPred.get(count),intersection,bias.iterMapToPred.get(0));

//		solutionObjects = bias.runToGenerateResults(generatePrologQuery(reordered), bias.iterMapToPred.get(count),intersection,bias.iterMapToPred.get(0));
//			System.out.println("First Feature query : ");
//			System.out.println(Learner.generatePrologQuery(reordered));
			PredicateMapUtil.populateVariableIds(reordered);
			PrologAsProcess.writeToFile(generatePrologQuery(reordered));
			PrologAsProcess.callProcess();
			solutionObjects = PrologAsProcess.solutions(generatePrologQuery(reordered),bias.iterMapToPred.get(count),intersection,bias.iterMapToPred.get(0));
		} else{
			System.out.println("In heuristic Search");
			HeuristicBias bias = new HeuristicBias(enabledStrings, disabledStrings, this.originalPredicates,this.regexGeneralisedPredicates);
			bias.dropTemporalOrdering();
			bias.getMethodBiasPredicate();
			bias.optimiseQuery(4);
			Integer count  =0;
			for(Integer iter : bias.iterMapToPred.keySet()){
				System.out.println("Clause in iter "+iter);
				System.out.println(generatePrologQuery(bias.iterMapToPred.get(iter)));
				count = iter;
			}	
			List<PredicateDef> reordered = ClauseUtils.putIfLikeAtTheBeg(bias.iterMapToPred.get(count));
			bias.iterMapToPred.put(count, reordered);
			bias.writeQueriesToFile();
//			solutions = bias.runProglogToGenerateResults(generatePrologQuery(bias.iterMapToPred.get(count)));		
			List<PredicateDef> intersection = ClauseUtils.getIntersection(bias.iterMapToPred.get(count-1),bias.iterMapToPred.get(count));
			
			solutionObjects = bias.runToGenerateResults(generatePrologQuery(reordered), bias.iterMapToPred.get(count),intersection,bias.iterMapToPred.get(0));		
		}
	}
	
	
	public void writeListTofile(List<String> solutions){		
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			String content = "This is the content to write into file\n";

			fw = new FileWriter("/home/whirlwind/workspace/examples.txt");
			bw = new BufferedWriter(fw);
			bw.write(content);
			
//			System.out.println("The solutions are: ");
			for(String sol: solutions){
				bw.write(sol);
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
	
	public void trimPredicates(){
		List<PredicateDef> def = new ArrayList<PredicateDef>();
		boolean isFound=false;
		for(int i=0;i<this.originalPredicates.size();i++){
			isFound=false;
			for(int j=0;j<def.size();j++){
				if(def.get(j).getStringPredicate().equals(this.originalPredicates.get(i).getStringPredicate())){
					isFound = true;
				}
			}
			if(!isFound){
				def.add(this.originalPredicates.get(i));
			}
		}
		
		this.originalPredicates.clear();
		this.originalPredicates.addAll(def);
	}
		
	public List<List<PredicateDef>> getGeneralPredicates(List<List<PredicateDef>> nodes){		
		List<List<PredicateDef>> runningNode = new ArrayList<List<PredicateDef>>();
		for(int i=0;i<nodes.size();i++){
			runningNode.addAll(removeDuplicates(runningNode, generateGeneralisations(nodes.get(i))));
		}		
		return runningNode;
	}
	
	public List<List<PredicateDef>> removeDuplicates(List<List<PredicateDef>> nodes,List<List<PredicateDef>> toAdd){
		for(int i=0;i<toAdd.size();i++){
			boolean isthere = false;
			for(int j=0;j<nodes.size();j++){
				if(generatePrologQuery(nodes.get(j)).equals(toAdd.get(i))){
					isthere = true;
					break;
				}
			}
			if(!isthere){
				nodes.add(toAdd.get(i));				
			}
		}
		return nodes;
	}
	
	public GeneralisedOutput findMoreExamples() {
		GeneralisedOutput output = new GeneralisedOutput();
		List<String> solutions = new ArrayList<String>();
		List<List<PredicateDef>>biasedGeneralisations = new ArrayList<List<PredicateDef>>();
		biasedGeneralisations.add(this.originalPredicates);
		int nodeIndex = 0;
		boolean isFound=false;
		int noOfIteration = 0;
		while(solutions.size()<2){
			List<List<PredicateDef>> generalisations = getGeneralPredicates(biasedGeneralisations);
			noOfIteration++;
			System.out.println("The generalisations are ");		
			//after generalisations filter by bias and recompute the example set
			InductiveBias bias = new InductiveBias();
			List<List<PredicateDef>> gen = bias.structureBias(generalisations);
			
//			List<List<PredicateDef>> gen = bias.methodBias (generalisations);
//			List<List<PredicateDef>> gen = bias.typeBias(generalisations);
			
			if(gen.size()>5){
				biasedGeneralisations = gen.subList(0, 5);			
			}
			
			System.out.println("The generalisation iteration is "+noOfIteration);
			System.out.println("The number of predicates at this point "+biasedGeneralisations.get(0).size());
			
			for(int k=0;k<1;k++){
				System.out.println("The example for query "+ generatePrologQuery(biasedGeneralisations.get(0)));
				solutions = engine.executeSelectedQuery(generatePrologQuery(biasedGeneralisations.get(0)),biasedGeneralisations.get(k));						
				if(solutions.size()>2){
					output.solutions = solutions;
					output.generalisedOutput = biasedGeneralisations.get(k);
					isFound=true;
					nodeIndex=k;
					return output;
				}
			}
			
			if(isFound){
				break;
			}
		}	

		//here when solution is more then return the string
		return output;
		
	}
	
	public GeneralisedOutput findMoreExamples2() {
		GeneralisedOutput output = new GeneralisedOutput();
		List<String> solutions = new ArrayList<String>();
		List<List<PredicateDef>>biasedGeneralisations = new ArrayList<List<PredicateDef>>();
		biasedGeneralisations.add(this.originalPredicates);
		int nodeIndex = 0;
		boolean isFound=false;
		int noOfIteration = 0;
		while(solutions.size()<2){
//			List<List<PredicateDef>> generalisations = getGeneralPredicates(biasedGeneralisations);

			//after generalisations filter by bias and recompute the example set
			InductiveBias bias = new InductiveBias();
			List<List<PredicateDef>> gen = bias.dropBasedOnFrequency(biasedGeneralisations);

			noOfIteration++;
			System.out.println("The generalisations are ");		
			
//			List<List<PredicateDef>> gen = bias.methodBias (generalisations);
//			List<List<PredicateDef>> gen = bias.typeBias(generalisations);
			int size = 0;
			if(gen.size()>5){
				biasedGeneralisations = gen.subList(0, 5);			
				size = 5;
			} else{
				size = biasedGeneralisations.size();
			}
			
			
			System.out.println("The generalisation iteration is "+noOfIteration);
			System.out.println("The number of predicates at this point "+biasedGeneralisations.get(0).size());
			
			for(int k=0;k<size;k++){
				System.out.println("The example for query "+ generatePrologQuery(biasedGeneralisations.get(0)));
				solutions = engine.executeSelectedQuery(generatePrologQuery(biasedGeneralisations.get(0)),biasedGeneralisations.get(k));						
				if(solutions.size()>2){
					output.solutions = solutions;
					output.generalisedOutput = biasedGeneralisations.get(k);
					isFound=true;
					nodeIndex=k;
					return output;
				}
			}
			
			if(isFound){
				break;
			}
		}	

		//here when solution is more then return the string
		return output;
		
	}


	public List<List<PredicateDef>> generateGeneralisations(List<PredicateDef> predicates){
		Combinations comb = new Combinations();
		BigInteger noOfNodes = comb.noOfcombinations(predicates.size(), predicates.size()-1);
		
		List<List<PredicateDef>> nodes = new ArrayList<List<PredicateDef>>();		
		comb.combinationsAsArray(nodes,predicates, new ArrayList<PredicateDef>(), 0, predicates.size()-1, 0, predicates.size()-1);				
		
		return nodes;
	}
	
	
	public void printNodes(List<List<PredicateDef>> nodes){
		for(List<PredicateDef> intermediateNodes: nodes){
			System.out.print("[");
			for(PredicateDef pred : intermediateNodes){
				System.out.print(pred.getStringPredicate());
			}
			System.out.println("]");
		}
	}
	
	public static String generatePrologQuery(List<PredicateDef> def){
		StringBuilder builder = new StringBuilder();
		
		int i=0;
		for(;i<def.size()-1;i++){  
			builder.append(def.get(i).getStringPredicate());
			builder.append(",");
		}
		
		builder.append(def.get(i).getStringPredicate());
		return builder.toString();
	}
			
}

