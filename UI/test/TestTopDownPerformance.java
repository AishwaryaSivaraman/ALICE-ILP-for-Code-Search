package alice.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;

import learn.ActiveLearning;
import learn.Learner;
import learn.Solution;
import util.ClauseUtils;
import alice.menu.BaseAction;
import alice.menu.DisableFeature;
import alice.menu.GenerateFacts;
import def.JavaSourceCodeInfo;
import def.PredicateDef;

public class TestTopDownPerformance{

	List<String> currentGoldenTruth = new ArrayList<String>();
	List<String> currentFeatures = new ArrayList<String>();
	public static HashMap<String, HashMap<Integer, Integer>> fileToPosNegExamplesMap = new HashMap<String, HashMap<Integer,Integer>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToF1ScoreMapGreedy = new HashMap<String, HashMap<Integer,F1Score>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToF1ScoreMapExhaustive = new HashMap<String, HashMap<Integer,F1Score>>();

	public static HashMap<String, HashMap<Integer, HashMap<Integer, Double>>> varyFeaturePrecision = new HashMap<String, HashMap<Integer,HashMap<Integer,Double>>>();
	public static List<String> selectedExample = new ArrayList<String>();
	List<JavaSourceCodeInfo> posExamples = new ArrayList<JavaSourceCodeInfo>();
	List<JavaSourceCodeInfo> negativeExamples = new ArrayList<JavaSourceCodeInfo>();
	
	public static HashMap<String, HashMap<Integer, Integer>> fileToVaryExampleInIterationTwoMapTopDownExhaustive = new HashMap<String, HashMap<Integer,Integer>>();
	public static HashMap<String, HashMap<Integer, Integer>> fileToVaryExampleInIterationTwoMapTopDownGreedy = new HashMap<String, HashMap<Integer,Integer>>();

	public static HashMap<String, HashMap<Integer, F1Score>> fileToFeatureVaryF1MapExhaustive = new HashMap<String, HashMap<Integer,F1Score>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToFeatureVaryF1MapGreedy = new HashMap<String, HashMap<Integer,F1Score>>();
	
	public static HashMap<Integer, Double> iterPrecisionSum = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> iterRecallSum = new HashMap<Integer, Double>();
	
	public static HashMap<Integer, HashMap<Integer, List<Integer>>> posNegThruAllIterG = new HashMap<Integer, HashMap<Integer,List<Integer>>>();	
	public static HashMap<Integer, HashMap<Integer, List<Integer>>> posNegThruAllIterE = new HashMap<Integer, HashMap<Integer,List<Integer>>>();	

	public List<PosNegMap> labelledExamplesData = new ArrayList<PosNegMap>();
	HashMap<Integer, List<JavaSourceCodeInfo>> accumulativeExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
	
	HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> accumulativeIterExamples = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();
	
	public HashMap<Integer, List<String>> iterEnabledStrings = new HashMap<Integer, List<String>>();
	
	public String folderName;
	public String biasType = "TopDown";
	public String baseFolder = "/home/whirlwind/Desktop/ALICE_PERF/";
	
	HashMap<Integer, HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>> examplesAtEachIterAndEachTrial = new HashMap<Integer, HashMap<Integer,HashMap<Integer,List<JavaSourceCodeInfo>>>>();
	HashMap<Integer, HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>> examplesAtEachIterAndEachTrialRandom = new HashMap<Integer, HashMap<Integer,HashMap<Integer,List<JavaSourceCodeInfo>>>>();
	HashMap<Integer, HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>> examplesAtEachIterAndEachTrialAfter = new HashMap<Integer, HashMap<Integer,HashMap<Integer,List<JavaSourceCodeInfo>>>>();
	
	public void run(String fileName, List<String> candidates){
		String[] tmp = fileName.split("/");
		folderName = tmp[tmp.length-1].split("\\.")[0];
		System.out.println(folderName);
		
		getGroundTruth(fileName);
		getFeatures(fileName);
		getIterEnabledString(fileName);

		getPrecisions(candidates, fileName);
	}
	
	public void getIterEnabledString(String fileName){
		String file = fileName.split("[.]txt")[0]+"_Features"+".txt";
		readEnabledFile(file);
	}
	
	private void readEnabledFile(String fileName) {
		iterEnabledStrings.clear();
		File file = new File(fileName);
		try{
			String st;
			BufferedReader br = new BufferedReader(new FileReader(file));
			List<String> features = new ArrayList<String>();
			while ((st = br.readLine()) != null){
				features.add(st.toLowerCase());
			}
			int iterCount = 0;
			for(int i=0;i<features.size();){
				List<String> currentIterFeatures = new ArrayList<String>();
				currentIterFeatures.add(features.get(i));
				i++;
				currentIterFeatures.add(features.get(i));
				i++;
				iterEnabledStrings.put(iterCount, currentIterFeatures);
				iterCount++;
			}
		}catch(Exception e){
			
		}
		
	}

	public void getGroundTruth(String fileName){
		String file = fileName.split("[.]txt")[0]+"_Ground"+".txt";
		readFile(file);
	}
	
	public void getFeatures(String fileName){
		String file = fileName.split("[.]txt")[0]+"_Features"+".txt";
		readFeatureFile(file);
	}
	
	public void getPrecisions(List<String> features,String fileName){
		for(int i=0;i<5;i++){
			accumulativeIterExamples.put(i, new HashMap<Integer, List<JavaSourceCodeInfo>>());
		}
		
		getPrecisionEachIteration(2, 3, features, 5,fileName);
		printPrecisionToFile(3);
		printPosNegToFileG(fileName);
		printPosNegToFileH(fileName);
		printEnabledStrings(fileName);
	}
	
	private void createFolderIfNotExists(String folder){
		File file = new File(folder);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }               
	}
	private void printEnabledStrings(String fileName){
		try {
			createFolderIfNotExists(baseFolder+biasType);
			createFolderIfNotExists(baseFolder+biasType+"/"+folderName);
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(baseFolder+biasType+"/"+folderName+"/EnabledStrings_"+System.currentTimeMillis()+".txt"));
			StringBuilder builder = new StringBuilder();
			for(Integer key: iterEnabledStrings.keySet()){
				for(String feature: iterEnabledStrings.get(key)){
					builder.append(feature);
					builder.append("\n");
				}				
			}
			writer.write(builder.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void printPosNegToFileG(String fileName) {
		try {
			fileName = fileName.split("\\.")[0];
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(baseFolder+biasType+"/"+folderName+"/E__G_"+System.currentTimeMillis()+".txt"));
			StringBuilder builder = new StringBuilder();
			for(Integer key: posNegThruAllIterG.keySet()){
				builder.append("-----Iter"+key+"-------");
				builder.append("\n");
				for(Integer iter: posNegThruAllIterG.get(key).keySet()){
					builder.append("\t\t"+iter+": pos - "+posNegThruAllIterG.get(key).get(iter).get(0));
					builder.append("\n");
					builder.append("\t\t\t"+": neg - "+posNegThruAllIterG.get(key).get(iter).get(1));
					builder.append("\n");
				}
			}			
			writer.write(builder.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void printPosNegToFileH(String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(baseFolder+biasType+"/"+folderName+"/E__E_"+System.currentTimeMillis()+".txt"));
			StringBuilder builder = new StringBuilder();
			for(Integer key: posNegThruAllIterE.keySet()){
				builder.append("-----Iter"+key+"-------");
				builder.append("\n");
				for(Integer iter: posNegThruAllIterE.get(key).keySet()){
					builder.append("\t\t"+iter+": pos - "+posNegThruAllIterE.get(key).get(iter).get(0));
					builder.append("\n");
					builder.append("\t\t\t"+": neg - "+posNegThruAllIterE.get(key).get(iter).get(1));
					builder.append("\n");
				}
			}			
			writer.write(builder.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	private void printPrecisionToFile(int iter) {
		// TODO Auto-generated method stub
		try {
			iterPrecisionSum.clear();
			iterRecallSum.clear();
			createFolderIfNotExists(baseFolder+biasType);
			createFolderIfNotExists(baseFolder+biasType+"/"+folderName);
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(baseFolder+biasType+"/"+folderName+"/E_"+iter+"_"+System.currentTimeMillis()+".txt"));
			StringBuilder builder = new StringBuilder();
			builder.append("----------- Greedy Score----------------");
			builder.append("\n");
			System.out.println("----------- Greedy Score----------------");
			for(String fileName: fileToF1ScoreMapGreedy.keySet()){
				System.out.println("Eval for "+fileName);
				builder.append("Eval for "+fileName);
				builder.append("\n");
				HashMap<Integer,F1Score> precisionMap = fileToF1ScoreMapGreedy.get(fileName);
				for(Integer key: precisionMap.keySet()){
					builder.append("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);				
					builder.append("\n");
					builder.append("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
					builder.append("\n");
					builder.append("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
					builder.append("\n");
					if(iterPrecisionSum.containsKey(key)){
						double sum = iterPrecisionSum.get(key);
						sum = sum + precisionMap.get(key).precision;
						iterPrecisionSum.put(key, sum);
						sum = iterRecallSum.get(key);
						sum = sum + precisionMap.get(key).recall;
						iterRecallSum.put(key, sum);
					} else{
						iterPrecisionSum.put(key, precisionMap.get(key).precision);
						iterRecallSum.put(key, precisionMap.get(key).recall);
					}
					System.out.println("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);
					System.out.println("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
					System.out.println("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
				}
			}
			
			builder.append("---SUM FOR ITER GREEDY-----");
			builder.append("\n");
			for(Integer key : iterPrecisionSum.keySet()){
				builder.append("Sum Precision Iter "+key+" "+iterPrecisionSum.get(key));
				builder.append("\n");
				builder.append("Sum Recall Iter "+key+" "+iterRecallSum.get(key));
				builder.append("\n");
			}
			
			iterPrecisionSum.clear();
			iterRecallSum.clear();
			
			System.out.println("----------- Exhaustive Score----------------");
			builder.append("----------- Exhaustive Score----------------");
			builder.append("\n");
			
			for(String fileName: fileToF1ScoreMapExhaustive.keySet()){
				System.out.println("Eval for "+fileName);
				builder.append("Eval for "+fileName);
				builder.append("\n");
				
				HashMap<Integer,F1Score> precisionMap = fileToF1ScoreMapExhaustive.get(fileName);
				for(Integer key: precisionMap.keySet()){
					builder.append("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);
					builder.append("\n");
					builder.append("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
					builder.append("\n");
					builder.append("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
					builder.append("\n");	
					if(iterPrecisionSum.containsKey(key)){
						double sum = iterPrecisionSum.get(key);
						sum = sum + precisionMap.get(key).precision;
						iterPrecisionSum.put(key, sum);
						sum = iterRecallSum.get(key);
						sum = sum + precisionMap.get(key).recall;
						iterRecallSum.put(key, sum);
					} else{
						iterPrecisionSum.put(key, precisionMap.get(key).precision);
						iterRecallSum.put(key, precisionMap.get(key).recall);
					}
					System.out.println("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);					
					System.out.println("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
					System.out.println("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
				}
			}
			
			builder.append("---SUM FOR ITER Exhaustive-----");
			builder.append("\n");
			for(Integer key : iterPrecisionSum.keySet()){
				builder.append("Sum Precision Iter "+key+" "+iterPrecisionSum.get(key));
				builder.append("\n");
				builder.append("Sum Recall Iter "+key+" "+iterRecallSum.get(key));
				builder.append("\n");
			}
			
			writer.write(builder.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public List<Integer> returnNFeatureIndices(int size, int N){
		List<Integer> indices = new ArrayList<Integer>();
		for(int i=0;i<N;){
			int index = (int) (Math.random()*(size));
			if(!indices.contains(index)){
				indices.add(index);
				i++;
			}
		}
		return indices;
	}
	
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> returnNExamples(int N, HashMap<Integer,Solution> solutions){
		HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		List<Integer> chosenIndex = new ArrayList<Integer>();		
		if(solutions.size() < N){
			N = solutions.size();
		}
		
		for(int i=0;i<N;){
			int index = (int) (Math.random()*(solutions.size()));
				if(!chosenIndex.contains(index)){
					chosenIndex.add(index);
					JavaSourceCodeInfo info = new JavaSourceCodeInfo();
					info.className = solutions.get(index).mapVartoSolution.get("X");					
					if(isPositive(solutions.get(index))){
						posExamples.add(info);
					}else{
						negativeExamples.add(info);
					}
					i++;
				}
		}		
		
		labelledExamples.put(1, posExamples);
		labelledExamples.put(2, negativeExamples);
		return labelledExamples;
	}
	
	public boolean isPositive(Solution sol){
		for(String pos: currentGoldenTruth){
			if(sol.mapVartoSolution.get("X").equals(pos)){
				return true;
			}
		}
		return false;
	}
	
	public List<JavaSourceCodeInfo> getPositives(int noOfPos){
		List<JavaSourceCodeInfo> posExamples = new ArrayList<JavaSourceCodeInfo>();
		for(int i=0;i<noOfPos;i++){
			JavaSourceCodeInfo info = new JavaSourceCodeInfo();
			info.className = currentGoldenTruth.get(i);
			posExamples.add(info);
		}
		return posExamples;
	}
	
	public int returnPositive(HashMap<Integer,Solution> solutions){		
		int noOfPos = 0;
		for(Integer key : solutions.keySet()){
			if(currentGoldenTruth.contains(solutions.get(key).mapVartoSolution.get("X"))){
				noOfPos++;				
			}
		}
		return noOfPos;
	}
	
	public List<JavaSourceCodeInfo> getNegatives(int noOfNeg, HashMap<Integer,Solution> solutions){
		List<JavaSourceCodeInfo> negExamples = new ArrayList<JavaSourceCodeInfo>();
		for(int i=0;i<noOfNeg;){
			Integer index = (int) (Math.random()*(solutions.size()));
			if(!currentGoldenTruth.contains(solutions.get(index).mapVartoSolution.get("X"))){
				JavaSourceCodeInfo info = new JavaSourceCodeInfo();
				info.className = solutions.get(index).mapVartoSolution.get("X");
				negExamples.add(info);
				i++;
			}
		}
		return negExamples;
	}
	
	public void readFile(String fileName){
		currentGoldenTruth.clear();
		File file = new File(fileName);
		try{
			String st;
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((st = br.readLine()) != null){
				currentGoldenTruth.add(st);
			}
		}catch(Exception e){
			
		}
	}
	
	public void readFeatureFile(String fileName){
		currentFeatures.clear();
		File file = new File(fileName);
		try{
			String st;
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((st = br.readLine()) != null){
				currentFeatures.add(st.toLowerCase());
			}
		}catch(Exception e){
			
		}
	}
	
	public void averageSolutions(int noOfFeatures, int noOfIterations, List<String> features){
		double precision = 0.0;
		try{
			  for(int i=0;i<noOfIterations;i++){
				List<String> enabledString = new ArrayList<String>();
				List<Integer> indexArray = new ArrayList<Integer>();
		
				for(int j=0;j<noOfFeatures;){
					int index = (int) (Math.random()*(features.size()));
					if(!indexArray.contains(index)){
						indexArray.add(index);				
						enabledString.add(features.get(index).replaceAll("\\\\", ""));	
						j++;
					}
				}				
				Learner learn = new Learner();
		        try{
		        	learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, enabledString, DisableFeature.disabledSelectionStrings, Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
		        	//need method to calculate Precision at each iteration
//	        		precision = precision + calculatePrecision(learn.solutionObjects);
		        }catch(Exception e){			      
		        	System.out.println("Exception in learning.");
		        	i--;
		        	continue;
		        }			       			    					
		    }
		} catch(Exception e){
			System.out.println("Exception ");
		}
	}
	
	public F1Score calculatePrecisionAndRecall(HashMap<Integer, Solution> solutions){
		int relevantCount = 0;	
		for(Integer key: solutions.keySet()){
			if(currentGoldenTruth.contains(solutions.get(key).mapVartoSolution.get("X"))){
				relevantCount++;
			}
		}	
		F1Score score = new F1Score();
		score.precision = (relevantCount*1.0)/(solutions.size()*1.0);
		score.recall = (relevantCount*1.0)/(currentGoldenTruth.size()*1.0);
		
		return score;
	}
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> createCopyOfMap(HashMap<Integer, List<JavaSourceCodeInfo>> examples){
		HashMap<Integer, List<JavaSourceCodeInfo>> copyMap = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		for(Integer key : examples.keySet()){
			List<JavaSourceCodeInfo> listInfo = new ArrayList<JavaSourceCodeInfo>();
			for(int i=0;i<examples.get(key).size();i++){
				JavaSourceCodeInfo info = new JavaSourceCodeInfo();
				info.className = examples.get(key).get(i).className;
				listInfo.add(info);
			}
			copyMap.put(key, listInfo);
		}
		return copyMap;
	}
	
	public HashMap<Integer,List<JavaSourceCodeInfo>> returnErrorLabelSet(Object e){
		HashMap<Integer,List<JavaSourceCodeInfo>> source = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		iteration it = (iteration)e;
		negativeExamples.clear();
		posExamples.clear();		
		for(int i=0;i<it.neg.length;i++){
			JavaSourceCodeInfo info = new JavaSourceCodeInfo();
			info.className = it.neg[i];
			negativeExamples.add(info);
		}
		for(int i=0;i<it.pos.length;i++){
			JavaSourceCodeInfo info = new JavaSourceCodeInfo();
			info.className = it.pos[i];
			posExamples.add(info);
		}		
		source.put(1, posExamples);
		source.put(2, negativeExamples);
		return source;
	}
	
	
	public void getPrecisionEachIteration(int noOfFeatures, int totalExamplesMarked, List<String> features, int noOfIterations, String fileName){
		HashMap<Integer, List<HashMap<Integer,F1Score>>> convergeIterationF1Map = new HashMap<Integer, List<HashMap<Integer,F1Score>>>();
		
		HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> exampleAtLocalIter = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();
		HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> exampleAtLocalIterRandom = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();
		HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> exampleAtLocalIterAfter = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();		
		HashMap<Integer, HashMap<Integer,Integer>> examplesreturnedByAlice = new HashMap<Integer, HashMap<Integer,Integer>>();
		ParseExampleFile exampleFile = new ParseExampleFile();
		exampleFile.run();
		List<Double> perfValuesEachIter = new ArrayList<Double>();		
		StringBuilder builder = new StringBuilder();
		StringBuilder perfBuilder = new StringBuilder();
		
		long startTimeNano;
		long taskTimeNano;
		try{
			  for(int i=0;i<noOfIterations;i++){
				examplesAtEachIterAndEachTrial.put(i, new HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>());				
				examplesreturnedByAlice.put(i, new HashMap<Integer, Integer>());
				
				System.out.println("At Iteration "+i);
				List<String> enabledString = new ArrayList<String>();
				enabledString.addAll(iterEnabledStrings.get(i));
				
				builder.append(enabledString);
				builder.append("\n");
				
				System.out.println(enabledString);
				
				Learner learn = new Learner();
		        try{
		        	accumulativeExamples.clear();
		        	posExamples.clear();
		        	negativeExamples.clear();
		        	
		        	HashMap<Integer, F1Score> localPrecision = new HashMap<Integer, F1Score>();
		        	
		        	int numberOfActiveIteration = 1;
		        	
		        	startTimeNano = System.nanoTime( );
		        	learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, enabledString, DisableFeature.disabledSelectionStrings, Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
		        	taskTimeNano  = System.nanoTime( ) - startTimeNano;
		        	perfValuesEachIter.add((double) taskTimeNano);
		        	
		        	
		        	HashMap<Integer, Solution > solutionFromLearning = 
		        			new HashMap<Integer, Solution>(learn.solutionObjects);		
		        	
		        	examplesreturnedByAlice.get(i).put(numberOfActiveIteration, solutionFromLearning.size());

		        	builder.append("Trial "+i);
		        	builder.append("\n");
		        	
		        	perfBuilder.append("Trial "+i);
		        	perfBuilder.append("\n");
		        	perfBuilder.append("Iteration: "+numberOfActiveIteration);
		        	perfBuilder.append("\n");
		        	
		        	perfBuilder.append("Time:"+taskTimeNano);
		        	perfBuilder.append("\n");
		        	builder.append("Iteration: "+numberOfActiveIteration);
		        	builder.append("\n");
		        	builder.append(solutionFromLearning.get(0).queryAsString);
		        	builder.append("\n");
		        	
		        	int requiredExamples = totalExamplesMarked;	        		
		        	F1Score score = new F1Score();
		        	score = calculatePrecisionAndRecall(solutionFromLearning);  
		        	
        			localPrecision.put(numberOfActiveIteration, score);
        			
        			requiredExamples = 3;
        			System.out.println("The size of returned examples is "+solutionFromLearning.size());        			
        			
//        			HashMap<Integer, List<JavaSourceCodeInfo>> returnedExamples = returnNExamples(requiredExamples, learn.solutionObjects);  
//        			examplesAtEachIterAndEachTrial.get(i).put(numberOfActiveIteration, createCopyOfMap(returnedExamples));
        			
        			ActiveLearning activeLearning = new ActiveLearning();
        			HashMap<Integer, List<JavaSourceCodeInfo>> exampleLabel = returnErrorLabelSet(((trial)exampleFile.listOfExpts.get(folderName+".txt").listOfTrials.get(i)).listOfIterations.get(numberOfActiveIteration-1));
    				
	        		activeLearning.labelledExamples = exampleLabel;        				
	        		ActiveLearning.previousWasContainment = false;        			
        			        		        			
	        		System.out.println("Running for TopDown ");
	        			        		
		        	while(!((int)score.precision == 1)){		        		
		        		numberOfActiveIteration++;
		        		
		        		startTimeNano = System.nanoTime( );
		        		solutionFromLearning = activeLearning.learnASeparatorTopDownExhaustive(
		        				solutionFromLearning.get(0).queryforSolution,
		        				solutionFromLearning.get(0).remainingPredicates,
		        				solutionFromLearning.get(0).originalSpecialisedPredicates);	
		        		taskTimeNano  = System.nanoTime( ) - startTimeNano;
		        		perfValuesEachIter.add((double) taskTimeNano);
		        		
		        		score = new F1Score();
		        		score = calculatePrecisionAndRecall(solutionFromLearning);
		        		localPrecision.put(numberOfActiveIteration, score);
		        		activeLearning = new ActiveLearning();
		        		posExamples.clear();
		        		negativeExamples.clear();
		        		
		        		examplesreturnedByAlice.get(i).put(numberOfActiveIteration, solutionFromLearning.size());

		        		
		        		builder.append("Iteration: "+numberOfActiveIteration);
			        	builder.append("\n");
			        	builder.append(solutionFromLearning.get(0).queryAsString);
			        	builder.append("\n");			        
			        	
			        	perfBuilder.append("Iteration: "+numberOfActiveIteration);
			        	perfBuilder.append("\n");
			        	
			        	perfBuilder.append("Time:"+taskTimeNano);
			        	perfBuilder.append("\n");
			        	
			        	exampleLabel = returnErrorLabelSet(((trial)exampleFile.listOfExpts.get(folderName+".txt").listOfTrials.get(i)).listOfIterations.get(numberOfActiveIteration-1));	    				
	        			activeLearning.labelledExamples = exampleLabel;
			        	examplesAtEachIterAndEachTrial.get(i).put(numberOfActiveIteration, createCopyOfMap(activeLearning.labelledExamples));
		        	}			        			        	
		        			        			        	
		        	int numberofIter = localPrecision.size();
		        	if(!convergeIterationF1Map.containsKey(numberofIter)){
		        		convergeIterationF1Map.put(numberofIter, new ArrayList<HashMap<Integer,F1Score>>());
		        	} 		        	
		        	convergeIterationF1Map.get(numberofIter).add(localPrecision);
		        	
		        }catch(Exception e){	
		        	e.printStackTrace();
		        	System.out.println("exception in learning");
		        	i--;
		        	continue;
		        }		        		      
		    }
			 
			writePerfToFile(perfBuilder.toString(), fileName,"TopDownParentPerf");			
			writeToFile(builder.toString(), fileName,"TopDownParent"); 
			writeExamplesToFile(examplesAtEachIterAndEachTrial,fileName,"TopDown",examplesreturnedByAlice);
			writeExamplesToFile(examplesAtEachIterAndEachTrialAfter,fileName,"After",examplesreturnedByAlice);
			writeExamplesToFile(examplesAtEachIterAndEachTrialRandom,fileName,"Random",examplesreturnedByAlice);
			writeIterationPrecisionsToFile(convergeIterationF1Map, fileName,"TopDownParent");		 
			System.out.println("The time taken for this file is "+ perfValuesEachIter.stream().mapToDouble(Double::doubleValue).sum());
		} catch(Exception e){
			System.out.println("Exception");
		}
	}

	
	private void writeExamplesToFile(
			HashMap<Integer, HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>> map,
			String fileName, String folder, HashMap<Integer, HashMap<Integer, Integer>> examplesreturnedByAlice) {
		try {
		for(Integer trial : map.keySet()){			
			createFolderIfNotExists(baseFolder+"ExampleLog");
			createFolderIfNotExists(baseFolder+"ExampleLog/"+folder);
//			createFolderIfNotExists(baseFolder+"ExampleLog/"+folder+"/Iter_"+key);
			BufferedWriter writer;
			
				writer = new BufferedWriter(
						new FileWriter
					(baseFolder+"ExampleLog/"+folder+"/ExampleLogData"+".txt", true));
				writer.write(fileName);
				writer.write("\n");
				writer.write("Start Trial : "+trial);
				writer.write("\n");
				for(Integer iter: map.get(trial).keySet()){
					writer.write("Start Iter : "+iter);
					writer.write("\n");
					writer.write("Number of returned examples : "+examplesreturnedByAlice.get(trial).get(iter));
					writer.write("\n");
					for(Integer exampleType: map.get(trial).get(iter).keySet()){
						String parent = "";
						if(exampleType==1){
							parent = "pos";
						} else{
							parent = "neg";
						}
						StringBuilder builder = new StringBuilder();
						for(int i=0;i<map.get(trial).get(iter).get(exampleType).size();i++){
							builder.append(map.get(trial).get(iter).get(exampleType).get(i).className);
							builder.append(",");
						}
						
						writer.write(parent+":"+builder.toString());
						writer.write("\n");
					}
					
					writer.write("End Iter : "+iter);
					writer.write("\n");
				}
				writer.write("End Trial : "+trial);
				writer.write("\n");
				writer.close();
			} 			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeIterationPrecisionsToFile(
			HashMap<Integer, List<HashMap<Integer, F1Score>>> convergeIterationF1Map, 
			String fileName, String folder) {		
		try {
			for(Integer key: convergeIterationF1Map.keySet()){
				
				createFolderIfNotExists(baseFolder+"IterationPrecision");
				createFolderIfNotExists(baseFolder+"IterationPrecision/"+folder);
				createFolderIfNotExists(baseFolder+"IterationPrecision/"+folder+"/Iter_"+key);
				BufferedWriter writer = new BufferedWriter(
						new FileWriter
					(baseFolder+"IterationPrecision/"+folder+"/Iter_"+key+"/RunData"+".txt", true));
				writer.write(fileName);
				writer.write("\n");
				for(int i=0;i<convergeIterationF1Map.get(key).size();i++){
					StringBuilder builder = new StringBuilder();
					builder.append("Iteration_Start");
					builder.append("\n");
					for(Integer iterKey : convergeIterationF1Map.get(key).get(i).keySet()){
						String toPrint = convergeIterationF1Map.get(key).get(i).get(iterKey).precision
								+","+convergeIterationF1Map.get(key).get(i).get(iterKey).recall
								+","+convergeIterationF1Map.get(key).get(i).get(iterKey).score;
						builder.append(toPrint);
						builder.append("\n");						
					}
					builder.append("Iteration_End");
					builder.append("\n");
					writer.append(builder);
				}				
				writer.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	private void writeToFile(String string, String fileName, String folder) {
		try {
			createFolderIfNotExists(baseFolder);
			createFolderIfNotExists(baseFolder+folder);
			createFolderIfNotExists(baseFolder+folder+"/"+folderName);
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(baseFolder+folder+"/"+folderName+"/RunData"+System.currentTimeMillis()+".txt"));
			writer.write(fileName+"\n");
			writer.write(string);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	
	private void writePerfToFile(String string, String fileName, String folder) {
		try {
			createFolderIfNotExists(baseFolder);
			createFolderIfNotExists(baseFolder+folder);
			createFolderIfNotExists(baseFolder+folder+"/"+folderName);
			BufferedWriter writer = new BufferedWriter(new FileWriter
					(baseFolder+folder+"/"+folderName+"/RunData"+System.currentTimeMillis()+".txt"));
			writer.write(fileName+"\n");
			writer.write(string);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

}
