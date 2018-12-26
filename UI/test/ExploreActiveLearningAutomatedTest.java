package alice.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ClauseUtils;
import def.JavaSourceCodeInfo;
import def.PredicateDef;
import learn.ActiveLearning;
import learn.Learner;
import learn.Solution;
import alice.menu.DisableFeature;
import alice.menu.GenerateFacts;

public class ExploreActiveLearningAutomatedTest {
	
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
	public String biasType = "After";
	
	public void run(String fileName, List<String> candidates){
		String[] tmp = fileName.split("/");
		folderName = tmp[tmp.length-1].split("\\.")[0];
		System.out.println(folderName);
		
		getGroundTruth(fileName);
		getFeatures(fileName);
		getIterEnabledString(fileName);
//		try{
//		varyExampleNumbersEval(candidates, fileName);
//		} catch(Exception e ){
//			
//		}
		getPrecisions(candidates, fileName);
//		varyFeatures(candidates, fileName);
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
//		HashMap<Integer, Double> precisionMap = getPrecisionEachIteration(2, 2, 2, features, 4);
//		getPrecisionEachIteration(2, 3, features, 5,fileName);
//		for(int i=2;i<5;i++){			
//			getPrecisionEachIteration(2, i, features, 1,fileName);
//			printPrecisionForEachIteration();
//			printPrecisionToFile(i);
//		}
		int noOfIter = 5;
		int noOfLabels = 6;
		for(int i=0;i<5;i++){
			accumulativeIterExamples.put(i, new HashMap<Integer, List<JavaSourceCodeInfo>>());
		}
		
//		for(int i=5;i<noOfLabels;i++){
//			getPrecisionEachIteration(2, i, features, 5,fileName);
//			printPrecisionToFile(i);
//			fileToF1ScoreMapGreedy.clear();
//			fileToF1ScoreMapExhaustive.clear();
//		}
//		
		getPrecisionEachIteration(2, 3, features, 5,fileName);
		printPrecisionToFile(3);
		printPosNegToFileG(fileName);
		printPosNegToFileH(fileName);
		printEnabledStrings(fileName);

	}
	private void printEnabledStrings(String fileName){
		try {
//			fileName = fileName.split("\\.")[0];
			BufferedWriter writer = new BufferedWriter(new FileWriter
					("/home/whirlwind/Documents/"+biasType+"/"+folderName+"/EnabledStrings_"+System.currentTimeMillis()+".txt"));
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
					("/home/whirlwind/Documents/"+biasType+"/"+folderName+"/E__G_"+System.currentTimeMillis()+".txt"));
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
					("/home/whirlwind/Documents/"+biasType+"/"+folderName+"/E__E_"+System.currentTimeMillis()+".txt"));
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
			
			BufferedWriter writer = new BufferedWriter(new FileWriter
					("/home/whirlwind/Documents/"+biasType+"/"+folderName+"/E_"+iter+"_"+System.currentTimeMillis()+".txt"));
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

	public void varyFeatures(List<String> features,String fileName){
		HashMap<Integer, HashMap<Integer, Double>> variousPrecisionForFeatures = new HashMap<Integer, HashMap<Integer,Double>>();
		for(int i=2;i<5;i++){
//			HashMap<Integer, Double> precisionMap = getPrecisionEachIteration(2, i, i, features, 4);
//			variousPrecisionForFeatures.put(i, precisionMap);
		}
		varyFeaturePrecision.put(fileName, variousPrecisionForFeatures);
		printVaryFeaturePrecision();
	}
	
	
	public void printData() throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/whirlwind/Documents/RunData.txt"));
	    
	    StringBuilder builder = new StringBuilder();
		for(int i=0;i<labelledExamplesData.size();i++){
			System.out.println("At iteration "+i);
			builder.append("At iteration "+i);
			builder.append("\n");
			for(Integer key : labelledExamplesData.get(i).posCountAtIter.keySet()){
				System.out.println("\t#Labels: "+key+"positive count"+labelledExamplesData.get(i).posCountAtIter.get(key));
				builder.append("\t#Labels: "+key+"positive count: "+labelledExamplesData.get(i).posCountAtIter.get(key));
				builder.append("\n");
				System.out.println("\t       : "+key+"negative count"+labelledExamplesData.get(i).negCountAtIter.get(key));
				builder.append("\t       "+key+"negative count"+labelledExamplesData.get(i).negCountAtIter.get(key));
				builder.append("\n");
				builder.append("\t Greedy Query: ");
				builder.append(labelledExamplesData.get(i).queryForIterGreedy.get(key));
				builder.append("\n");
				builder.append("\t Exhaus Query: ");
				builder.append(labelledExamplesData.get(i).queryForIterExh.get(key));
				builder.append("\n");
				builder.append("\tGreedy Precision and Recall "+labelledExamplesData.get(i).scoreAtGreedy.get(key).precision+","+labelledExamplesData.get(i).scoreAtGreedy.get(key).recall);
				builder.append("\n");
				builder.append("\tExhaus Precision and Recall "+labelledExamplesData.get(i).scoreAtExhuas.get(key).precision+","+labelledExamplesData.get(i).scoreAtExhuas.get(key).recall);
				builder.append("\n");
			}
		}
		System.out.println(builder.toString());
		writer.write(builder.toString());
		writer.close();
	}
	public void printVaryFeaturePrecision(){
		for(String fileName: varyFeaturePrecision.keySet()){
			System.out.println("Eval for "+fileName);
			HashMap<Integer,HashMap<Integer,Double>> featuremap = varyFeaturePrecision.get(fileName);
			for(Integer feature: featuremap.keySet()){
				HashMap<Integer,Double> precisionMap = featuremap.get(feature);
				System.out.println("#No Pos & Neg: "+feature);
				for(Integer key: precisionMap.keySet()){
					System.out.println("#Iteration: "+key+" --- #Precision "+precisionMap.get(key));
				}
			}
		}
	}
	public void printPrecisionForEachIteration(){
		System.out.println("----------- Greedy Score----------------");
		for(String fileName: fileToF1ScoreMapGreedy.keySet()){
			System.out.println("Eval for "+fileName);
			HashMap<Integer,F1Score> precisionMap = fileToF1ScoreMapGreedy.get(fileName);
			for(Integer key: precisionMap.keySet()){
				System.out.println("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);
				System.out.println("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
				System.out.println("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
			}
		}
		
		System.out.println("----------- Exhaustive Score----------------");
		for(String fileName: fileToF1ScoreMapExhaustive.keySet()){
			System.out.println("Eval for "+fileName);
			HashMap<Integer,F1Score> precisionMap = fileToF1ScoreMapExhaustive.get(fileName);
			for(Integer key: precisionMap.keySet()){
				System.out.println("#Iteration: "+key+" --- #Precision "+precisionMap.get(key).precision);
				System.out.println("\t\t "+key+" --- #Recall "+precisionMap.get(key).recall);
				System.out.println("\t\t "+key+" --- #F1Score "+precisionMap.get(key).score);
			}
		}
	}
	
	public void printNoOfExamplesForPosNegEvaluation(){
		for(String fileName: fileToVaryExampleInIterationTwoMapTopDownExhaustive.keySet()){
			System.out.println("Eval for "+fileName);
			HashMap<Integer,Integer> featureExampleMapExhaustive = fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(fileName);
			HashMap<Integer,Integer> featureExampleMapGreedy = fileToVaryExampleInIterationTwoMapTopDownGreedy.get(fileName);
			for(Integer featureNumber: featureExampleMapExhaustive.keySet()){
				System.out.println("#Pos & Neg : "+featureNumber+" --- #Examples Exhaustive & Greedy "+featureExampleMapExhaustive.get(featureNumber)+ " "+ featureExampleMapGreedy.get(featureNumber));
				System.out.println("#Features : "+featureNumber+" --- F1 Score Exhaustive "+fileToFeatureVaryF1MapExhaustive.get(fileName).get(featureNumber).score);
				System.out.println("#Features : "+featureNumber+" --- F1 Score Greedy "+fileToFeatureVaryF1MapGreedy.get(fileName).get(featureNumber).score);
			}
		}
	}	
	
	public void varyExampleNumbersEval(List<String> features, String file) throws IOException{
		int maxNo = 6;
		int noOfIter = 2;
		fileToFeatureVaryF1MapExhaustive.put(file, new HashMap<Integer, F1Score>());
		fileToFeatureVaryF1MapGreedy.put(file, new HashMap<Integer, F1Score>());

		for(int i=0;i<maxNo;i++){
			fileToFeatureVaryF1MapExhaustive.get(file).put(i, new F1Score());
			fileToFeatureVaryF1MapGreedy.get(file).put(i, new F1Score());
		}
		fileToVaryExampleInIterationTwoMapTopDownExhaustive.put(file, new HashMap<Integer, Integer>());
		fileToVaryExampleInIterationTwoMapTopDownGreedy.put(file, new HashMap<Integer, Integer>());
		
		for(int j=0;j<noOfIter;j++){
			System.out.println("---Running for iteration"+j);			
			List<Integer> indices = returnNFeatureIndices(features.size(), 2);
//			indices.clear();
//			indices.add(4);
//			indices.add(5);
			negativeExamples.clear();
			posExamples.clear();
			labelledExamplesData.add(new PosNegMap());
			
			for(int i=1;i< maxNo;i++){
				System.out.println("---Running for pos+neg #"+i);
				if(varyNoOfExamples(indices, i, features,file,j) == 1){
					labelledExamplesData.remove(j);
					j--;
					break;
				}
//				posNegExamplesMap.put(i, averageExamples);
			}
			
		}
		
		for(Integer key : fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(file).keySet()){
			int val = fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(file).get(key);
			fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(file).put(key, val/noOfIter);
			val = fileToVaryExampleInIterationTwoMapTopDownGreedy.get(file).get(key);
			fileToVaryExampleInIterationTwoMapTopDownGreedy.get(file).put(key, val/noOfIter);
		}
		
		for(Integer key: fileToFeatureVaryF1MapExhaustive.get(file).keySet()){
			fileToFeatureVaryF1MapExhaustive.get(file).get(key).averagePresAndRec(noOfIter);
			fileToFeatureVaryF1MapExhaustive.get(file).get(key).calculateF1Score();
			fileToFeatureVaryF1MapGreedy.get(file).get(key).averagePresAndRec(noOfIter);
			fileToFeatureVaryF1MapGreedy.get(file).get(key).calculateF1Score();						
		}
//		for(Integer key: allposNegExamplesMap.keySet()){
//			int val = allposNegExamplesMap.get(key);
//			int avg = val/noOfIter;
//			allposNegExamplesMap.put(key,avg);
//		}
		
//		fileToPosNegExamplesMap.put(file, allposNegExamplesMap);
		printData();
//		printNoOfExamplesForPosNegEvaluation();
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
	
	public int varyNoOfExamples(List<Integer> indices, int totalExamplesMarked, List<String> features, String path, int iter){
		int sumExhaustive = 0;
		int sumGreedy = 0;
		try{			  				
			List<String> enabledString = new ArrayList<String>();
			for(Integer index : indices){
				enabledString.add(features.get(index));
			}
			
			System.out.println(enabledString);
			
			Learner learn = new Learner();
	        try{
	        	learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, 
	        			GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, 
	        			enabledString, 
	        			DisableFeature.disabledSelectionStrings, 
	        			Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
		        System.out.println("The number of examples returned from features "+learn.solutionObjects.size());
	        	int learnSum = learn.solutionObjects.size();
	        	F1Score learnF1Score = new F1Score();
	        	learnF1Score = calculatePrecisionAndRecall(learn.solutionObjects);		        
	        
	        	if(learn.solutionObjects.size() < (6)){		        				        		
//	        		sumExhaustive = sumExhaustive + learn.solutionObjects.size();
//	        		sumGreedy = sumGreedy + learn.solutionObjects.size();
//	        		fileToFeatureVaryF1MapExhaustive.get(path).get(totalExamplesMarked).updatePrecisionAndRecall(learnF1Score.precision, learnF1Score.recall);
//			        fileToFeatureVaryF1MapGreedy.get(path).get(totalExamplesMarked).updatePrecisionAndRecall(learnF1Score.precision, learnF1Score.recall);			        
	        		return 1;
	        	} else{
	        		fileToFeatureVaryF1MapExhaustive.get(path).get(0).updatePrecisionAndRecall(learnF1Score.precision, learnF1Score.recall);
			        fileToFeatureVaryF1MapGreedy.get(path).get(0).updatePrecisionAndRecall(learnF1Score.precision, learnF1Score.recall);
			        
	        		int requiredExamples =totalExamplesMarked - (posExamples.size()+negativeExamples.size());	        		
	        		List<PredicateDef> previous = new ArrayList<PredicateDef>();
	        		previous.addAll(ClauseUtils.predicateListCopy(learn.solutionObjects.get(0).queryforSolution));
	        		List<PredicateDef> remaining = new ArrayList<PredicateDef>();
	        		remaining.addAll(ClauseUtils.predicateListCopy(learn.solutionObjects.get(0).remainingPredicates));
	        		List<PredicateDef> original = new ArrayList<PredicateDef>();
	        		original.addAll(ClauseUtils.predicateListCopy(learn.solutionObjects.get(0).originalSpecialisedPredicates));
	        		
	        		ActiveLearning activeLearningGreedy = new ActiveLearning();
	        		activeLearningGreedy.labelledExamples = returnNExamples(requiredExamples, learn.solutionObjects);
//		        	HashMap<Integer, Solution > solutionFromLearningGreedy = activeLearningGreedy.learnASeparatorTopDownGreedy(learn.solutionObjects.get(0).queryforSolution,learn.solutionObjects.get(0).remainingPredicates,learn.solutionObjects.get(0).originalSpecialisedPredicates);
	        		labelledExamplesData.get(iter).posCountAtIter.put(totalExamplesMarked, activeLearningGreedy.labelledExamples.get(1).size());
	        		labelledExamplesData.get(iter).negCountAtIter.put(totalExamplesMarked, activeLearningGreedy.labelledExamples.get(2).size());
	        		
	        		HashMap<Integer, Solution > solutionFromLearningGreedy = activeLearningGreedy.learnASeparatorRandom(learn.solutionObjects.get(0).queryforSolution,learn.solutionObjects.get(0).remainingPredicates,learn.solutionObjects.get(0).originalSpecialisedPredicates, true);		        	
	        		labelledExamplesData.get(iter).queryForIterGreedy.put(totalExamplesMarked, solutionFromLearningGreedy.get(0).queryAsString);
	        		sumGreedy = sumGreedy + solutionFromLearningGreedy.size();
		        	F1Score scoreGreedy = new F1Score();
	        		scoreGreedy = calculatePrecisionAndRecall(solutionFromLearningGreedy);
	        		fileToFeatureVaryF1MapGreedy.get(path).get(totalExamplesMarked).updatePrecisionAndRecall(scoreGreedy.precision, scoreGreedy.recall);			        	        		        		
	        		labelledExamplesData.get(iter).scoreAtGreedy.put(totalExamplesMarked,scoreGreedy);
	        		
		        	ActiveLearning activeLearningExhaustive = new ActiveLearning();
//		        	activeLearningExhaustive.labelledExamples = returnNExamples(requiredExamples, learn.solutionObjects);
		        	activeLearningExhaustive.labelledExamples = activeLearningGreedy.labelledExamples;
//		        	HashMap<Integer, Solution > solutionFromLearningExhaustive = activeLearningExhaustive.learnASeparatorTopDownExhaustive(previous,remaining,original);	        			        		
		        	HashMap<Integer, Solution > solutionFromLearningExhaustive = activeLearningExhaustive.learnASeparatorRandom(previous,remaining,original, false);	        			        		
		        	labelledExamplesData.get(iter).queryForIterExh.put(totalExamplesMarked, solutionFromLearningExhaustive.get(0).queryAsString);
		        	sumExhaustive = sumExhaustive + solutionFromLearningExhaustive.size();	        			        		    					    				        		
	        		
	        		F1Score scoreExh = new F1Score();
	        		scoreExh = calculatePrecisionAndRecall(solutionFromLearningExhaustive);
	        		fileToFeatureVaryF1MapExhaustive.get(path).get(totalExamplesMarked).updatePrecisionAndRecall(scoreExh.precision, scoreExh.recall);
	        		labelledExamplesData.get(iter).scoreAtExhuas.put(totalExamplesMarked,scoreExh);
	        		
	        	}	
	        	
	        	System.out.println("Greedy Sum "+sumGreedy);
        		System.out.println("Exhaustive Sum "+sumExhaustive);
        		
        		
	        	if(fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).containsKey(totalExamplesMarked)){
					int valEx = fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).get(totalExamplesMarked);
					fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).put(totalExamplesMarked, valEx+sumExhaustive);    					
					valEx = fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).get(0);
					fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).put(0, valEx+learnSum);    					
					int valGreedy = fileToVaryExampleInIterationTwoMapTopDownGreedy.get(path).get(totalExamplesMarked);
					fileToVaryExampleInIterationTwoMapTopDownGreedy.get(path).put(totalExamplesMarked, valGreedy+sumGreedy);    								
					valGreedy = fileToVaryExampleInIterationTwoMapTopDownGreedy.get(path).get(0);
					fileToVaryExampleInIterationTwoMapTopDownGreedy.get(path).put(0, valGreedy+learnSum);    					
	        	} else{
	        		fileToVaryExampleInIterationTwoMapTopDownGreedy.get(path).put(0, learnSum);    					
					fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).put(0, learnSum);					
					fileToVaryExampleInIterationTwoMapTopDownGreedy.get(path).put(totalExamplesMarked, sumGreedy);    					
					fileToVaryExampleInIterationTwoMapTopDownExhaustive.get(path).put(totalExamplesMarked, sumExhaustive);					
				}	        	
	        }catch(Exception e){			      
	        	System.out.println("Exception in learning.");	        	
	        }			       			    							    
		} catch(Exception e){
			System.out.println("Exception ");
		}
		return 0;
	}
	
	public HashMap<Integer, List<JavaSourceCodeInfo>> returnNExamples(int N, HashMap<Integer,Solution> solutions){
		HashMap<Integer, List<JavaSourceCodeInfo>> labelledExamples = new HashMap<Integer, List<JavaSourceCodeInfo>>();
		List<Integer> chosenIndex = new ArrayList<Integer>();		
		if(solutions.size() < N){
			N = solutions.size();
		}
		
		for(int i=0;i<N;){
			int index = (int) (Math.random()*(solutions.size()));
//			if(i == N-1){
//				if(posExamples.size() == 0){
//					JavaSourceCodeInfo info = new JavaSourceCodeInfo();
//					info.className = currentGoldenTruth.get(0);
//					posExamples.add(info);
//					i++;
//				}
//			} else{
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
//			}			
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
	
	public void getPrecisionEachIteration(int noOfFeatures, int totalExamplesMarked, List<String> features, int noOfIterations, String fileName){
		HashMap<Integer, F1Score> precision = new HashMap<Integer, F1Score>();	
		HashMap<Integer, F1Score> precisionExh = new HashMap<Integer, F1Score>();	
		HashMap<Integer, Integer> numberOfTimesKeyUpdates = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> numberOfTimesKeyUpdatesExh = new HashMap<Integer, Integer>();
		posNegThruAllIterE.clear();
		posNegThruAllIterG.clear();
		fileToF1ScoreMapExhaustive.clear();
		fileToF1ScoreMapGreedy.clear();
//		iterEnabledStrings.clear();
		
		StringBuilder builder = new StringBuilder();
		
		try{
			
			  for(int i=0;i<noOfIterations;i++){
				System.out.println("At Iteration "+i);
				List<String> enabledString = new ArrayList<String>();				
				List<Integer> indexArray = new ArrayList<Integer>();		
				for(int j=0;j<noOfFeatures;){
					int index = (int) (Math.random()*(features.size()));
					if(!indexArray.contains(index)){
						indexArray.add(index);				
						enabledString.add(features.get(index));	
						j++;
					}
				}			
								
//				enabledString.addAll(currentFeatures);
				
//				iterEnabledStrings.put(i, enabledString);
				enabledString.clear();
				enabledString.addAll(iterEnabledStrings.get(i));
				builder.append(enabledString);
				builder.append("\n");
				
				System.out.println(enabledString);
//				enabledString.clear();
//				enabledString.add("range!=null");
//				enabledString.add("commentarray");
				
				Learner learn = new Learner();
		        try{
		        	accumulativeExamples.clear();
		        	posExamples.clear();
		        	negativeExamples.clear();
		        	HashMap<Integer, F1Score> localPrecisionGreedy = new HashMap<Integer, F1Score>();
		        	HashMap<Integer, F1Score> localPrecisionExhaustive = new HashMap<Integer, F1Score>();
		        	int numberOfActiveIterationGreedy = 1;
		        	int numberOfActiveIterationExh = 1;
		        	learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, enabledString, DisableFeature.disabledSelectionStrings, Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
		        	HashMap<Integer, Solution > firstSol = new HashMap<Integer, Solution>(learn.solutionObjects);		        	
		        	HashMap<Integer, Solution > solutionFromLearning = new HashMap<Integer, Solution>(learn.solutionObjects);		        	

		        	builder.append("Trial "+i);
		        	builder.append("\n");
		        	
		        	builder.append("Iteration: "+numberOfActiveIterationGreedy);
		        	builder.append("\n");
		        	builder.append(solutionFromLearning.get(0).queryAsString);
		        	builder.append("\n");
		        	int requiredExamples = totalExamplesMarked;	        		
		        	F1Score scoreGreedy = new F1Score();
		        	scoreGreedy = calculatePrecisionAndRecall(solutionFromLearning);  
		        	
		        	F1Score scoreExh = new F1Score();
		        	scoreExh.precision = scoreGreedy.precision;
		        	scoreExh.recall = scoreGreedy.recall;
		        	if(scoreExh.precision == 1){
		        		System.out.println("Breaking because it is precision 1 at iter 0");
//		        		i--;
//		        		continue;
		        	}
		        	
        			localPrecisionGreedy.put(numberOfActiveIterationGreedy, scoreGreedy);
        			localPrecisionExhaustive.put(numberOfActiveIterationExh, scoreExh);
        			
        			
        			requiredExamples = totalExamplesMarked - accumulativeIterExamples.get(i).size();
        			HashMap<Integer, List<JavaSourceCodeInfo>> returnedExamples = returnNExamples(requiredExamples, learn.solutionObjects);  
        			HashMap<Integer, List<JavaSourceCodeInfo>>  oldExamples = createCopyOfMap(accumulativeIterExamples.get(i));
        			if(oldExamples.containsKey(1)){
        				oldExamples.get(1).addAll(returnedExamples.get(1));
        				oldExamples.get(2).addAll(returnedExamples.get(2));
        			} else{
        				oldExamples = createCopyOfMap(returnedExamples);
        			}
        			accumulativeIterExamples.put(i, oldExamples);
        			HashMap<Integer, List<Integer>> posNegMap = new HashMap<Integer, List<Integer>>();
        			posNegMap.put(numberOfActiveIterationGreedy, new ArrayList<Integer>());
        			posNegMap.get(numberOfActiveIterationGreedy).add(oldExamples.get(1).size());
        			posNegMap.get(numberOfActiveIterationGreedy).add(oldExamples.get(2).size());
        			
        			
        			HashMap<Integer, List<JavaSourceCodeInfo>> firstExamples = createCopyOfMap(accumulativeIterExamples.get(i));
        			HashMap<Integer, List<JavaSourceCodeInfo>> firstExamplesCopy = createCopyOfMap(firstExamples);
        			
        			ActiveLearning activeLearning = new ActiveLearning();
	        		activeLearning.labelledExamples = (HashMap<Integer, List<JavaSourceCodeInfo>>) firstExamples.clone();        				
	        		ActiveLearning.previousWasContainment = false;
        			
//	        		while(!((int)scoreGreedy.precision == 1)){		        		
//		        		numberOfActiveIterationGreedy++;
//		        		solutionFromLearning = 
//		        				activeLearning.learnASeparatorTopDownGreedy(
//		        						solutionFromLearning.get(0).queryforSolution,
//		        						solutionFromLearning.get(0).remainingPredicates,
//		        						solutionFromLearning.get(0).originalSpecialisedPredicates);			        	
//		        	
////		        		solutionFromLearning = 
////        				activeLearning.learnASeparatorRandom(
////        						solutionFromLearning.get(0).queryforSolution,
////        						solutionFromLearning.get(0).remainingPredicates,
////        						solutionFromLearning.get(0).originalSpecialisedPredicates,true);			        	
//		        		
////		        		solutionFromLearning = 
////		        				activeLearning.learnASeparatorTopologicalOrder(
////		        						solutionFromLearning.get(0).queryforSolution,
////		        						solutionFromLearning.get(0).remainingPredicates,
////		        						solutionFromLearning.get(0).originalSpecialisedPredicates,true);			        	
////		        		
//		        		scoreGreedy = new F1Score();
//		        		scoreGreedy = calculatePrecisionAndRecall(solutionFromLearning);
//		        		localPrecisionGreedy.put(numberOfActiveIterationGreedy, scoreGreedy);		        		
//		        		activeLearning = new ActiveLearning();		        		
//		        		posExamples.clear();
//		        		negativeExamples.clear();
//		        		
//		        		builder.append("Greedy Iteration: "+numberOfActiveIterationGreedy);
//			        	builder.append("\n");
//			        	builder.append(solutionFromLearning.get(0).queryAsString);
//			        	builder.append("\n");
//			        	
//		        		if(solutionFromLearning.size() < requiredExamples){		        			
//		        			activeLearning.labelledExamples = returnNExamples(solutionFromLearning.size(), solutionFromLearning);		       
//		        		} else{
//		        			activeLearning.labelledExamples = returnNExamples(totalExamplesMarked, solutionFromLearning);		       			        			
//		        		}
//		        		posNegMap.put(numberOfActiveIterationGreedy, new ArrayList<Integer>());
//	        			posNegMap.get(numberOfActiveIterationGreedy).add(activeLearning.labelledExamples.get(1).size());
//	        			posNegMap.get(numberOfActiveIterationGreedy).add(activeLearning.labelledExamples.get(2).size());	        			
//		        	}		
        			
	        		posNegThruAllIterG.put(i, posNegMap);
        			
        			
        			HashMap<Integer, List<Integer>> posNegMapE = new HashMap<Integer, List<Integer>>();
        			posNegMapE.put(numberOfActiveIterationExh, new ArrayList<Integer>());
        			posNegMapE.get(numberOfActiveIterationExh).add(oldExamples.get(1).size());
        			posNegMapE.get(numberOfActiveIterationExh).add(oldExamples.get(2).size());
        			
        			ActiveLearning.previousWasContainment = false;
        			activeLearning.labelledExamples = firstExamplesCopy;
        			solutionFromLearning = firstSol;
		        	while(!((int)scoreExh.precision == 1)){		        		
		        		numberOfActiveIterationExh++;
		        		
		        		solutionFromLearning = activeLearning.learnASeparatorTopDownExhaustive(
		        				solutionFromLearning.get(0).queryforSolution,
		        				solutionFromLearning.get(0).remainingPredicates,
		        				solutionFromLearning.get(0).originalSpecialisedPredicates);	
		        		
//		        		solutionFromLearning = 
//		        				activeLearning.learnASeparatorRandom(
//		        						solutionFromLearning.get(0).queryforSolution,
//		        						solutionFromLearning.get(0).remainingPredicates,
//		        						solutionFromLearning.get(0).originalSpecialisedPredicates,false);
		        		
//		        		solutionFromLearning = 
//		        				activeLearning.learnASeparatorTopologicalOrder(
//		        						solutionFromLearning.get(0).queryforSolution,
//		        						solutionFromLearning.get(0).remainingPredicates,
//		        						solutionFromLearning.get(0).originalSpecialisedPredicates,false);			        	
//		        		
		        		
		        		scoreExh = new F1Score();
		        		scoreExh = calculatePrecisionAndRecall(solutionFromLearning);
		        		localPrecisionExhaustive.put(numberOfActiveIterationExh, scoreExh);
		        		activeLearning = new ActiveLearning();
		        		posExamples.clear();
		        		negativeExamples.clear();
		        		
		        		builder.append("Exhaustive Iteration: "+numberOfActiveIterationExh);
			        	builder.append("\n");
			        	builder.append(solutionFromLearning.get(0).queryAsString);
			        	builder.append("\n");
			        	
		        		if(solutionFromLearning.size() < requiredExamples){		        			
		        			activeLearning.labelledExamples = returnNExamples(solutionFromLearning.size(), solutionFromLearning);		       
		        		} else{
		        			activeLearning.labelledExamples = returnNExamples(totalExamplesMarked, solutionFromLearning);		       			        			
		        		}
		        		
		        		posNegMapE.put(numberOfActiveIterationExh, new ArrayList<Integer>());
	        			posNegMapE.get(numberOfActiveIterationExh).add(activeLearning.labelledExamples.get(1).size());
	        			posNegMapE.get(numberOfActiveIterationExh).add(activeLearning.labelledExamples.get(2).size());
		        	}	
		        	
		        	posNegThruAllIterE.put(i, posNegMapE);
		        	
		        	for(Integer key: localPrecisionGreedy.keySet()){
		        		if(precision.containsKey(key)){
		        			Integer keyUpdateCount = numberOfTimesKeyUpdates.get(key);
		        			precision.get(key).updatePrecisionAndRecall(localPrecisionGreedy.get(key).precision, localPrecisionGreedy.get(key).recall);
		        			keyUpdateCount++;		        			
		        			numberOfTimesKeyUpdates.put(key, keyUpdateCount);
		        		} else{
		        			precision.put(key, localPrecisionGreedy.get(key));
		        			numberOfTimesKeyUpdates.put(key, 1);
		        		}
		        	}
		        	
		        	for(Integer key: localPrecisionExhaustive.keySet()){
		        		if(precisionExh.containsKey(key)){
		        			Integer keyUpdateCount = numberOfTimesKeyUpdatesExh.get(key);
		        			precisionExh.get(key).updatePrecisionAndRecall(localPrecisionExhaustive.get(key).precision, localPrecisionExhaustive.get(key).recall);
		        			keyUpdateCount++;		        			
		        			numberOfTimesKeyUpdatesExh.put(key, keyUpdateCount);
		        		} else{
		        			precisionExh.put(key, localPrecisionExhaustive.get(key));
		        			numberOfTimesKeyUpdatesExh.put(key, 1);
		        		}
		        	}
		        }catch(Exception e){			      
		        	System.out.println("Exception in learning.");
		        	accumulativeIterExamples.get(i).clear();
		        	i--;
		        	continue;
		        }		        		      
		    }
			  
			writeToFile(builder.toString(), fileName);  
		  for(Integer key: precision.keySet()){
			  precision.get(key).averagePresAndRec(numberOfTimesKeyUpdates.get(key));
			  precision.get(key).calculateF1Score();
		  }
		  fileToF1ScoreMapGreedy.put(fileName, precision);

		  for(Integer key: precisionExh.keySet()){
			  precisionExh.get(key).averagePresAndRec(numberOfTimesKeyUpdatesExh.get(key));
			  precisionExh.get(key).calculateF1Score();
		  }
		  fileToF1ScoreMapExhaustive.put(fileName, precisionExh);

		} catch(Exception e){
			System.out.println("Exception ");
		}
	}

	private void writeToFile(String string, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter
					("/home/whirlwind/Documents/"+biasType+"/"+folderName+"/RunData"+System.currentTimeMillis()+".txt"));
			writer.write(fileName+"\n");
			writer.write(string);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		  
	}
	
}
