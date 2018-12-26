package alice.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import def.JavaSourceCodeInfo;
import learn.ActiveLearning;
import learn.Learner;
import learn.Solution;
import alice.menu.DisableFeature;
import alice.menu.GenerateFacts;

public class ActiveLearningWithNoisyOracle {
	
	List<String> currentGoldenTruth = new ArrayList<String>();
	List<String> currentFeatures = new ArrayList<String>();	
	public static HashMap<String, HashMap<Integer, F1Score>> fileToF1ScoreMap = new HashMap<String, HashMap<Integer,F1Score>>();

	List<JavaSourceCodeInfo> posExamples = new ArrayList<JavaSourceCodeInfo>();
	List<JavaSourceCodeInfo> negativeExamples = new ArrayList<JavaSourceCodeInfo>();
	HashMap<Integer, HashMap<Integer,List<Integer>>> flipData =  new HashMap<Integer, HashMap<Integer,List<Integer>>>();
	HashMap<Integer, Boolean> exception = new HashMap<Integer, Boolean>();
	public HashMap<Integer, List<String>> iterEnabledStrings = new HashMap<Integer, List<String>>();
	HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> accumulativeExamples = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();
	
	public void run(String fileName, List<String> candidates){
		getGroundTruth(fileName);			
		getIterEnabledString(fileName);
		measureNoisyPrecision(candidates, fileName);
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
	
	private void measureNoisyPrecision(List<String> candidates, String fileName) {
		accumulativeExamples.clear();
		precision(5, 0.25, candidates, fileName);
		printPrecisionToFile(25);
		precision(5, 0.50, candidates, fileName);
		printPrecisionToFile(50);
	}
	
	private void printPrecisionToFile(int errorRate) {
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("/home/whirlwind/Documents/"+errorRate+"_Noisy_"+System.currentTimeMillis()+".txt"));
			StringBuilder builder = new StringBuilder();
			builder.append("----------- Score----------------");
			builder.append("\n");
			System.out.println("----------- Score----------------");
			for(String fileName: fileToF1ScoreMap.keySet()){
				System.out.println("Eval for "+fileName);
				builder.append("Eval for "+fileName);
				builder.append("\n");
				HashMap<Integer,F1Score> precisionMap = fileToF1ScoreMap.get(fileName);
				for(Integer key: precisionMap.keySet()){
					builder.append("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);				
					builder.append("\n");
					builder.append("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
					builder.append("\n");
					builder.append("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
					builder.append("\n");
					
					System.out.println("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);
					System.out.println("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
					System.out.println("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
				}
			}
			
			writer.write(builder.toString());
			
			builder  = new StringBuilder();
			for(Integer key : flipData.keySet()){
				System.out.println("At iter "+key);
				builder.append("At trial "+key);
				builder.append("\n");
				for(Integer aliceKey: flipData.get(key).keySet()){
					builder.append("\t At Iter "+aliceKey);
					builder.append("\n");
					for(int i=0;i<flipData.get(key).get(aliceKey).size();i++){
						String toPrint = flipData.get(key).get(aliceKey).get(i)==0?"Pos->Neg":"Neg->Pos";
						System.out.println("\t\t"+ toPrint);
						builder.append("\t\t"+ toPrint);
						builder.append("\n");
					}
				}
				String toPrint = exception.get(key) == true?"exception":"pass";
				System.out.println("At iter "+key+" "+ toPrint);
				builder.append("At trial "+key+" "+ toPrint);
				builder.append("\n");
			}
			writer.write(builder.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void precision(int noOfIteration,double errorRate, List<String> features, String fileName){
		HashMap<Integer, Integer> numberOfTimesKeyUpdates = new HashMap<Integer, Integer>();
		HashMap<Integer, F1Score> precision = new HashMap<Integer, F1Score>();	
		flipData.clear();
		exception.clear();
		fileToF1ScoreMap.clear();
		
		for(int i=0;i<noOfIteration;i++){
			List<String> enabledString = new ArrayList<String>();				
			List<Integer> indexArray = new ArrayList<Integer>();
			if(!accumulativeExamples.containsKey(i)){
				accumulativeExamples.put(i, new HashMap<Integer, List<JavaSourceCodeInfo>>());
			}
//			for(int j=0;j<2;){
//				int index = (int) (Math.random()*(features.size()));
//				if(!indexArray.contains(index)){
//					indexArray.add(index);				
//					enabledString.add(features.get(index));	
//					j++;
//				}
//			}
			enabledString.addAll(iterEnabledStrings.get(i));
			System.out.println(enabledString);
			
			try{
				flipData.put(i, new HashMap<Integer, List<Integer>>());
				HashMap<Integer, F1Score> localPrecision = new HashMap<Integer, F1Score>();
				int noOfExamplesTillNow = 0;
				Learner learn = new Learner();
				learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, 
						GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, 
						enabledString, DisableFeature.disabledSelectionStrings, 
						Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
	        			        	
	        	HashMap<Integer, Solution > solutionFromLearning = new HashMap<Integer, Solution>(
	        			learn.solutionObjects);		        	

	        	int numberOfActiveIteration = 1;
	        	int missLabelledSofar =0;
	        	F1Score scoreGreedy = new F1Score();
	        	scoreGreedy = calculatePrecisionAndRecall(solutionFromLearning);  
	        	localPrecision.put(numberOfActiveIteration, scoreGreedy);
    			
	        	int requiredExamples = 3;
	        	noOfExamplesTillNow = 3;
	        	int missLabel = (int) (Math.ceil(1.0*errorRate*noOfExamplesTillNow));
	        	missLabelledSofar = missLabel;
	        	HashMap<Integer, List<JavaSourceCodeInfo>> returnedExamples = 
	        			returnNExamples(requiredExamples, learn.solutionObjects, missLabel, i,numberOfActiveIteration+1);      			
	        	
	        	ActiveLearning activeLearning = new ActiveLearning();
        		activeLearning.labelledExamples = (HashMap<Integer, List<JavaSourceCodeInfo>>) returnedExamples;     				
        		System.out.println("MissLabel at iter "+numberOfActiveIteration+":"+missLabel);
	        	
	        	while(!((int)scoreGreedy.precision == 1)){		        		
	        		numberOfActiveIteration++;
	        		solutionFromLearning = 
	        				activeLearning.learnASeparatorTopDownExhaustive(
	        						solutionFromLearning.get(0).queryforSolution,
	        						solutionFromLearning.get(0).remainingPredicates,
	        						solutionFromLearning.get(0).originalSpecialisedPredicates);			        	
	        		
	        		scoreGreedy = new F1Score();
	        		scoreGreedy = calculatePrecisionAndRecall(solutionFromLearning);
	        		localPrecision.put(numberOfActiveIteration, scoreGreedy);		        		
	        		activeLearning = new ActiveLearning();		        		
	        		posExamples.clear();
	        		negativeExamples.clear();
	        		if(solutionFromLearning.size() < requiredExamples){	
	        			noOfExamplesTillNow = noOfExamplesTillNow + solutionFromLearning.size();
	        			missLabel = (int) Math.ceil(1.0*errorRate*noOfExamplesTillNow) - missLabelledSofar;
	        			activeLearning.labelledExamples = returnNExamples(solutionFromLearning.size(), solutionFromLearning, missLabel, i,numberOfActiveIteration+1);		       
	        		} else{
	        			noOfExamplesTillNow = noOfExamplesTillNow + requiredExamples;
	        			missLabel = (int) Math.ceil(1.0*errorRate*noOfExamplesTillNow)- missLabelledSofar;
	        			activeLearning.labelledExamples = returnNExamples(requiredExamples, solutionFromLearning, missLabel,i,numberOfActiveIteration+1);		       			        			
	        		}
	        		System.out.println("MissLabel at iter "+numberOfActiveIteration+":"+missLabel);
	        	}
    			
	        	for(Integer key: localPrecision.keySet()){
	        		if(precision.containsKey(key)){
	        			Integer keyUpdateCount = numberOfTimesKeyUpdates.get(key);
	        			precision.get(key).updatePrecisionAndRecall(localPrecision.get(key).precision, localPrecision.get(key).recall);
	        			keyUpdateCount++;		        			
	        			numberOfTimesKeyUpdates.put(key, keyUpdateCount);
	        		} else{
	        			precision.put(key, localPrecision.get(key));
	        			numberOfTimesKeyUpdates.put(key, 1);
	        		}
	        	}
	        	exception.put(i, false);
			} catch(Exception e){
				System.out.println("Exception in learning");
				exception.put(i, true);
				System.out.print(e.getMessage());
			}
			
		}
		
		 for(Integer key: precision.keySet()){
			  precision.get(key).averagePresAndRec(numberOfTimesKeyUpdates.get(key));
			  precision.get(key).calculateF1Score();
		  }
		 fileToF1ScoreMap.put(fileName, precision);
	}

	public void getGroundTruth(String fileName){
		String file = fileName.split("[.]txt")[0]+"_Ground"+".txt";
		readFile(file);
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
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> returnNExamples(int N, HashMap<Integer,Solution> solutions, int missLabelled, int iteration, int aliceIter){
		HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		List<Integer> chosenIndex = new ArrayList<Integer>();	
		flipData.get(iteration).put(aliceIter, new ArrayList<Integer>());
		if(solutions.size() < N){
			N = solutions.size();
		}
		for(int i=0;i<N;){
			int index = (int) (Math.random()*(solutions.size()));
				if(!chosenIndex.contains(index)){
					chosenIndex.add(index);
					JavaSourceCodeInfo info = new JavaSourceCodeInfo();
					info.className = solutions.get(index).mapVartoSolution.get("X");
					int result = 0;
					if(missLabelled>0){
						result = 1;
					} else{
						result = 0;
					}
					if(isPositive(solutions.get(index))){
						if(result ==1){
							System.out.println("Positive is missclassified");
							info.isNoisy = true;
							negativeExamples.add(info);
							missLabelled--;
							flipData.get(iteration).get(aliceIter).add(0);
						} else{
							posExamples.add(info);
						}						
					}else{
						if(result ==1){
							System.out.println("negative is missclassified");
							info.isNoisy = true;
							posExamples.add(info);
							missLabelled--;
							flipData.get(iteration).get(aliceIter).add(1);
						} else{
							negativeExamples.add(info);
						}
//						negativeExamples.add(info);
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
}
