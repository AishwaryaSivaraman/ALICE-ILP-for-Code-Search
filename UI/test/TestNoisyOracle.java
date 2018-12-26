package alice.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import learn.ActiveLearning;
import learn.Learner;
import learn.Solution;
import alice.menu.DisableFeature;
import alice.menu.GenerateFacts;
import def.JavaSourceCodeInfo;

public class TestNoisyOracle{

	List<String> currentGoldenTruth = new ArrayList<String>();
	List<String> currentFeatures = new ArrayList<String>();
	public static HashMap<String, HashMap<Integer, Integer>> fileToPosNegExamplesMap = new HashMap<String, HashMap<Integer,Integer>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToF1ScoreMapGreedy = new HashMap<String, HashMap<Integer,F1Score>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToF1ScoreMapExhaustive = new HashMap<String, HashMap<Integer,F1Score>>();

	public static HashMap<String, HashMap<Integer, HashMap<Integer, Double>>> varyFeaturePrecision = new HashMap<String, HashMap<Integer,HashMap<Integer,Double>>>();
	public static List<String> selectedExample = new ArrayList<String>();
	List<JavaSourceCodeInfo> posExamples = new ArrayList<JavaSourceCodeInfo>();
	List<JavaSourceCodeInfo> negativeExamples = new ArrayList<JavaSourceCodeInfo>();

	public static HashMap<String, HashMap<Integer, F1Score>> fileToFeatureVaryF1MapExhaustive = new HashMap<String, HashMap<Integer,F1Score>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToFeatureVaryF1MapGreedy = new HashMap<String, HashMap<Integer,F1Score>>();
	
	public static HashMap<Integer, Double> iterPrecisionSum = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> iterRecallSum = new HashMap<Integer, Double>();
	
	public static HashMap<Integer, HashMap<Integer, List<Integer>>> posNegThruAllIterG = new HashMap<Integer, HashMap<Integer,List<Integer>>>();	
	public static HashMap<Integer, HashMap<Integer, List<Integer>>> posNegThruAllIterE = new HashMap<Integer, HashMap<Integer,List<Integer>>>();	

	public List<PosNegMap> labelledExamplesData = new ArrayList<PosNegMap>();
	HashMap<Integer, List<JavaSourceCodeInfo>> accumulativeExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
	
	HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> accumulativeIterExamplesLower = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();
	HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>> accumulativeIterExamplesHiger = new HashMap<Integer, HashMap<Integer,List<JavaSourceCodeInfo>>>();
	
	public HashMap<Integer, List<String>> iterEnabledStrings = new HashMap<Integer, List<String>>();
	List<Integer> flipArrayLower = new ArrayList<Integer>();
	List<List<Integer>> flipArrayHigher= new ArrayList<>();
	
	StringBuilder errorBuilder = new StringBuilder();
	
	
	public String folderName;
	public String biasType = "TopDown";
	public String baseFolder = "/home/whirlwind/Desktop/ALICE_NOISY_REDO/";
	
	HashMap<Integer, HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>> examplesAtEachIterAndEachTrial = new HashMap<Integer, HashMap<Integer,HashMap<Integer,List<JavaSourceCodeInfo>>>>();
	
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
			accumulativeIterExamplesHiger.put(i, new HashMap<Integer, List<JavaSourceCodeInfo>>());
			accumulativeIterExamplesLower.put(i, new HashMap<Integer, List<JavaSourceCodeInfo>>());
			examplesAtEachIterAndEachTrial.put(i, new HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>());			
		}
		
		double erroRate = 0.10;
		for(int i=1;i<4;i++){						
			System.out.println("Running for error rate "+erroRate);
			getPrecisionEachIteration(2, 3, features, 5,fileName, erroRate);			
			erroRate = erroRate*2;
		}
		
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
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> returnNoisyExamples(int N, 
			HashMap<Integer,Solution> solutions, boolean iterZero, double errorRate){
		HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		flipArrayHigher.clear();
		flipArrayLower.clear();
		
		List<Solution> selectedSolutions = new ArrayList<Solution>();
		
		List<Integer> chosenIndex = new ArrayList<Integer>();		
		if(solutions.size() < N){
			N = solutions.size();
		}
		int missLabelled = (int) Math.rint(errorRate*N);
		int missLabelledHigh = (int) Math.rint(errorRate*2*N);
		
		System.out.println("Number to missLabel "+missLabelled);
		
		for(int i=0;i<N;){
			int index = (int) (Math.random()*(solutions.size()));
			if(!chosenIndex.contains(index)){
				chosenIndex.add(index);				
				selectedSolutions.add(solutions.get(index));
				i++;
			}			
		}		
		List<Integer> indiceLow = new ArrayList<Integer>();
		List<Integer> indiceHigh = new ArrayList<Integer>();
		for(int i=0;i<missLabelledHigh;){
			int index = (int) (Math.random()*(selectedSolutions.size()));
			if(!indiceHigh.contains(index)){
				indiceHigh.add(index);
				if(missLabelled>0){
					indiceLow.add(index);
				}
				missLabelled--;
				i++;
			}				
		}			
		
		if(iterZero){
			accumulativeIterExamplesLower.put(0, createCopyOfMap(returnFlippedData(selectedSolutions, indiceLow)));
			List<Integer> tmp = new ArrayList<Integer>();
			tmp.addAll(flipArrayLower);
			accumulativeIterExamplesHiger.put(0, createCopyOfMap(returnFlippedData(selectedSolutions, indiceHigh)));		
			List<Integer> tmp2 = new ArrayList<Integer>();
			tmp2.addAll(flipArrayLower);
			flipArrayHigher.add(tmp2);
			flipArrayLower.clear();
			flipArrayLower.addAll(tmp);
			
		} else{
			return returnFlippedData(selectedSolutions, indiceLow);
		}
		return labelledExamples;
	}
	
	public HashMap<Integer,List<JavaSourceCodeInfo>> returnFlippedData(List<Solution> selectedSolutions, List<Integer> indixArray){
		HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		posExamples.clear();
		negativeExamples.clear();
		flipArrayLower.clear();
		
		for(int i=0;i<selectedSolutions.size();i++){
			JavaSourceCodeInfo info = new JavaSourceCodeInfo();
			info.className = selectedSolutions.get(i).mapVartoSolution.get("X");			
			if(isPositive(selectedSolutions.get(i))){
				if(indixArray.contains(i)){
					System.out.println("Positive is missclassified");
					info.isNoisy = true;
					negativeExamples.add(info);						
					flipArrayLower.add(0);
				} else{
					flipArrayLower.add(2);
					posExamples.add(info);
				}						
			}else{
				if(indixArray.contains(i)){
					System.out.println("negative is missclassified");
					info.isNoisy = true;
					posExamples.add(info);
					flipArrayLower.add(1);
				} else{
					flipArrayLower.add(2);
					negativeExamples.add(info);
				}
			}
		}
		
		labelledExamples .put(1, posExamples);
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
	
	public int returnSizeOfLabels (HashMap<Integer,List<JavaSourceCodeInfo>> labels){
		int count = 0;
		for(Integer key: labels.keySet()){
			count = count + labels.get(key).size();
		}
		return count;
	}

	public HashMap<Integer,List<JavaSourceCodeInfo>> returnErrorLabelSet(Object e, double errorRate){
		HashMap<Integer,List<JavaSourceCodeInfo>> source = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		iteration it = (iteration)e;
		negativeExamples.clear();
		posExamples.clear();
		int errorNeeded = (int) Math.round(errorRate * 3);
		if(errorNeeded>0){
			 for(int i=0;i<it.neg.length;i++){
					JavaSourceCodeInfo info = new JavaSourceCodeInfo();
					info.className = it.neg[i];			
					if(errorNeeded>0){
						posExamples.add(info);
						errorNeeded--;
					} else{
						negativeExamples.add(info);
					}
			}
			for(int i=0;i<it.pos.length;i++){
				JavaSourceCodeInfo info = new JavaSourceCodeInfo();
				info.className = it.pos[i];
				if(errorNeeded>0){
					negativeExamples.add(info);
					errorNeeded--;
				} else{
					posExamples.add(info);
				}
			}
		} else{
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
		}
		source.put(1, posExamples);
		source.put(2, negativeExamples);
		return source;
	}
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> noisyExample(HashMap<Integer,Solution> solutions, int noOfExamples, int noExampleSoFar, int errorSoFar, double errorRate){
		HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		posExamples.clear();
		negativeExamples.clear();
		if(solutions.size()<noOfExamples){
			noOfExamples = solutions.size();
		}
		noExampleSoFar = noExampleSoFar + noOfExamples;
		int totalError = (int) Math.round(noExampleSoFar*errorRate);
		int errorNeeded = totalError - errorSoFar;
		
		List<Integer> chosenIndex = new ArrayList<Integer>();
		List<Solution> selectedSolutions = new ArrayList<Solution>();
		for(int i=0;i<noOfExamples;){
			int index = (int) (Math.random()*(solutions.size()));
			if(!chosenIndex.contains(index)){
				chosenIndex.add(index);				
				selectedSolutions.add(solutions.get(index));
				i++;
			}	
		}
		
		for(int i=0;i<selectedSolutions.size();i++){
			JavaSourceCodeInfo info = new JavaSourceCodeInfo();
			info.className = selectedSolutions.get(i).mapVartoSolution.get("X");			
			if(isPositive(selectedSolutions.get(i))){
				if(errorNeeded>0){
					System.out.println("Changing pos as neg");
					negativeExamples.add(info);
					errorNeeded--;
				} else{
					posExamples.add(info);
				}
			} else{
				if(errorNeeded>0){
					System.out.println("Changing neg as pos");
					posExamples.add(info);
					errorNeeded--;
				} else{
					negativeExamples.add(info);
				}
			}
		}
		
		return labelledExamples;
	}
	
	public void getPrecisionEachIteration(int noOfFeatures, int totalExamplesMarked, 
			List<String> features, int noOfIterations, String fileName, double errorRate){
		HashMap<Integer, List<HashMap<Integer,F1Score>>> convergeIterationF1Map = new HashMap<Integer, List<HashMap<Integer,F1Score>>>();
		
		HashMap<Integer,HashMap<Integer, List<Integer>>> flipDataForAll = new HashMap<Integer, HashMap<Integer,List<Integer>>>();
	
		HashMap<Integer, Integer> exception = new HashMap<Integer, Integer>();
		ParseExampleFile exampleFile = new ParseExampleFile();
		exampleFile.run();
		
		flipDataForAll.clear();
		posNegThruAllIterE.clear();
		posNegThruAllIterG.clear();
		fileToF1ScoreMapExhaustive.clear();
		fileToF1ScoreMapGreedy.clear();
		int numberOfExampleMarkesSoFar = 0;
		int numberOfErrorsMadeSoFar = 0;
		StringBuilder builder = new StringBuilder();
		errorBuilder = new StringBuilder();
		try{
			  for(int i=0;i<noOfIterations;i++){
			    numberOfExampleMarkesSoFar = 0;
				numberOfErrorsMadeSoFar = 0;
				int numberOfActiveIteration = 1;	
				System.out.println("At Iteration "+i);
				List<String> enabledString = new ArrayList<String>();
																
				enabledString.clear();
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
		        	
		        	learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, enabledString, DisableFeature.disabledSelectionStrings, Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
		  		        	
		        	HashMap<Integer, Solution > solutionFromLearning = 
		        			new HashMap<Integer, Solution>(learn.solutionObjects);		        	

		        	builder.append("Trial "+i);
		        	builder.append("\n");
		        	
		        	errorBuilder.append("Trial "+i);
		        	errorBuilder.append("\n");
		        	
		        	builder.append("Iteration: "+numberOfActiveIteration);
		        	builder.append("\n");
		        	builder.append(solutionFromLearning.get(0).queryAsString);
		        	builder.append("\n");		        		        	
		        	
		        	int requiredExamples = totalExamplesMarked;	        		
		        	F1Score score = new F1Score();
		        	score = calculatePrecisionAndRecall(solutionFromLearning);  
		        	
        			localPrecision.put(numberOfActiveIteration, score);
        			
        			requiredExamples = totalExamplesMarked;
        			numberOfExampleMarkesSoFar = numberOfExampleMarkesSoFar + requiredExamples;
        			numberOfErrorsMadeSoFar = (int) Math.round(numberOfExampleMarkesSoFar*errorRate);        			
        			System.out.println("The size of returned examples is "+solutionFromLearning.size());        			
        			
        			if(Double.compare(errorRate, 0.4)<0){	
        				HashMap<Integer, List<JavaSourceCodeInfo>> exampleLabel = returnErrorLabelSet(((trial)exampleFile.listOfExpts.get(folderName+".txt").listOfTrials.get(i)).listOfIterations.get(numberOfActiveIteration-1), errorRate);
        				accumulativeIterExamplesHiger.put(i, exampleLabel);
        			}
        			
        			errorBuilder.append("Number of example so far in iter "+ numberOfActiveIteration+" is "+numberOfExampleMarkesSoFar);
		        	errorBuilder.append("\n");
		        	errorBuilder.append("Number of error so far in iter "+ numberOfActiveIteration+" is "+numberOfErrorsMadeSoFar);
		        	errorBuilder.append("\n");
		        	
        			ActiveLearning activeLearning = new ActiveLearning();
	        		activeLearning.labelledExamples = accumulativeIterExamplesHiger.get(i);        				
	        		ActiveLearning.previousWasContainment = false;        			        			        		        			
	        		
		        	while(!((int)score.precision == 1)){		        		
		        		numberOfActiveIteration++;
		        		
		        		solutionFromLearning = activeLearning.learnASeparatorTopDownExhaustive(
		        				solutionFromLearning.get(0).queryforSolution,
		        				solutionFromLearning.get(0).remainingPredicates,
		        				solutionFromLearning.get(0).originalSpecialisedPredicates);	
		        				        		
		        		score = new F1Score();
		        		score = calculatePrecisionAndRecall(solutionFromLearning);
		        		localPrecision.put(numberOfActiveIteration, score);
		        		activeLearning = new ActiveLearning();
		        		posExamples.clear();
		        		negativeExamples.clear();
		        		
		        		builder.append("Iteration: "+numberOfActiveIteration);
			        	builder.append("\n");
			        	builder.append(solutionFromLearning.get(0).queryAsString);
			        	builder.append("\n");
			        	
		        		activeLearning.labelledExamples = noisyExample(solutionFromLearning, requiredExamples, numberOfExampleMarkesSoFar, numberOfErrorsMadeSoFar, errorRate);
		        		if(solutionFromLearning.size() < requiredExamples){	
		        			numberOfExampleMarkesSoFar = numberOfExampleMarkesSoFar + solutionFromLearning.size();
		        		} else{
		        			numberOfExampleMarkesSoFar =  numberOfExampleMarkesSoFar + requiredExamples;
		        		}
		        		
		        		numberOfErrorsMadeSoFar = (int) Math.round(numberOfExampleMarkesSoFar*errorRate);
		        		
		        		errorBuilder.append("Number of example so far in iter "+ numberOfActiveIteration+" is "+numberOfExampleMarkesSoFar);
			        	errorBuilder.append("\n");
			        	errorBuilder.append("Number of error so far in iter "+ numberOfActiveIteration+" is "+numberOfErrorsMadeSoFar);
			        	errorBuilder.append("\n");
			        	
			        	examplesAtEachIterAndEachTrial.get(i).put(numberOfActiveIteration, createCopyOfMap(activeLearning.labelledExamples));	        					        		
		        	}	
		        		
		        	
		        	int numberofIter = localPrecision.size();
		        	if(!convergeIterationF1Map.containsKey(numberofIter)){
		        		convergeIterationF1Map.put(numberofIter, new ArrayList<HashMap<Integer,F1Score>>());
		        	} 		        	
		        	convergeIterationF1Map.get(numberofIter).add(localPrecision);
		        	exception.put(i, -1);
		        }catch(Exception e){	
		        	exception.put(i, numberOfActiveIteration);
		        	System.out.println("exception in learning");
		        	continue;
		        }		        		      
		    }
			  			  
		    System.out.println("Total Example Marked "+numberOfExampleMarkesSoFar);
        	System.out.println("Total Errors Made "+numberOfErrorsMadeSoFar);
			  
        	writeErrorToFile(errorBuilder.toString(), fileName,"TopDownParentError_"+errorRate); 
			writeToFile(builder.toString(), fileName,"TopDownParent_"+errorRate); 
			writeExamplesToFile(examplesAtEachIterAndEachTrial,fileName,"TopDown_"+errorRate);			
			writeIterationPrecisionsToFile(convergeIterationF1Map, fileName,"TopDownParent_"+errorRate);		 
			writeExceptionToFile(exception, fileName, "TopDownParent_"+errorRate);
//			writeFlipDataToFile(flipDataForAll, fileName, "TopDownParent_"+errorRate);
			
		} catch(Exception e){
			System.out.println("Exception");
		}
	}

	

	private void writeExceptionToFile(
			 HashMap<Integer,Integer> exceptionAtIter, String fileName,
			String folder) {
		createFolderIfNotExists(baseFolder+"ExceptionLog");
		createFolderIfNotExists(baseFolder+"ExceptionLog/"+folder);
		BufferedWriter writer;				
		try {
			writer = new BufferedWriter(
					new FileWriter
				(baseFolder+"ExceptionLog/"+folder+"/ExceptionLogData"+".txt", true));
			writer.write(fileName);
			writer.write("\n");
			for(Integer key: exceptionAtIter.keySet()){
				writer.write("Exception value at trial "+key+": "+exceptionAtIter.get(key));
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private void writeFlipDataToFile(
			HashMap<Integer,HashMap<Integer, List<Integer>>> flipData, String fileName,
			String folder) {
		createFolderIfNotExists(baseFolder+"FlipLog");
		createFolderIfNotExists(baseFolder+"FlipLog/"+folder);
		BufferedWriter writer;				
		try {
			writer = new BufferedWriter(
					new FileWriter
				(baseFolder+"FlipLog/"+folder+"/ExceptionLogData"+".txt", true));
			writer.write(fileName);
			writer.write("\n");
			for(Integer key: flipData.keySet()){
				writer.write("Flip data for trial "+key+": ");
				writer.write("\n");
				for(Integer iter: flipData.get(key).keySet()){
					writer.write("\tFlip data for iteration "+iter+": ");
					writer.write("\n");
					for(int i=0;i<flipData.get(key).get(iter).size();i++){
						writer.write("\t\tFlipped from ");
						if(flipData.get(key).get(iter).get(i)==0){
							writer.write("pos->neg");
						} else{
							if(flipData.get(key).get(iter).get(i)==1){
								writer.write("neg->pos");
							} else{
								if(flipData.get(key).get(iter).get(i)==2){
									writer.write("no change");
								}
							}
						}
						writer.write("\n");
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private void writeExamplesToFile(
			HashMap<Integer, HashMap<Integer, HashMap<Integer, List<JavaSourceCodeInfo>>>> map,
			String fileName, String folder) {
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
	
	private void writeErrorToFile(String string, String fileName, String folder) {
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
