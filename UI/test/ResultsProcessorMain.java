package alice.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ResultsProcessorMain {

	String baseFolder = "/home/whirlwind/Desktop/ALICE_NOISY_REDO/IterationPrecision/";
	List<String> biasFolder = new ArrayList<String>();
	HashMap<Integer, HashMap<Integer, List<F1Score>>> iterPrecisionMap = new HashMap<Integer, HashMap<Integer, List<F1Score>>>();
	
	HashMap<Integer, List<Double>> averagePrecisionOverallIter = new HashMap<Integer, List<Double>>();
	HashMap<Integer, List<Double>> averageRecallOverallIter = new HashMap<Integer, List<Double>>();
	HashMap<Integer, List<Double>> averageF1OverallIter = new HashMap<Integer, List<Double>>();
	
	HashMap<Integer, List<Double>> medianPrecisionOverallIter = new HashMap<Integer, List<Double>>();
	HashMap<Integer, List<Double>> medianRecallOverallIter = new HashMap<Integer, List<Double>>();
	HashMap<Integer, List<Double>> medianF1OverallIter = new HashMap<Integer, List<Double>>();
	
	HashMap<String, F1Score> fileWithScore = new HashMap<String, F1Score>();
	HashMap<Integer, List<F1Score>> allF1ScoreValuesInIter = new HashMap<Integer, List<F1Score>>();
	
	int totalTrials=0;
	String currentFileName = "GetLeadingComments.txt";
	List<String> fileNames = new ArrayList<String>();
	
	int count =0;
	public static void Main(String[] args){
		ResultsProcessorMain main = new ResultsProcessorMain();
		main.run();
	}
	
	public void run(){
		
//		biasFolder.add("TopDownParent");
		biasFolder.add("TopDownParent_0.1");
		biasFolder.add("TopDownParent_0.2");
		biasFolder.add("TopDownParent_0.4");
//		biasFolder.add("TopDownParent_1");
//		biasFolder.add("TopDownParent_2");
//		biasFolder.add("TopDownParent_3");
//		biasFolder.add("TopDownParent_4");
//		biasFolder.add("TopDown_VaryExamples_2");
//		biasFolder.add("TopDown_VaryExamples_4");
//		biasFolder.add("TopDown_VaryExamples_5");
		totalTrials = 0;
//		biasFolder.add("AfterParent");
		totalTrials = 0;
//		biasFolder.add("RandomParent");
		for(int j=0;j<biasFolder.size();j++){
			currentFileName = "";
			readFile(false,j);		
			
			for(int i=0;i<fileNames.size();i++){
				currentFileName = fileNames.get(i);
				readFile(true,j);
			}
		}
	}
	
	public void readFile(boolean subFiles, int index){
//		for(int i=0;i<biasFolder.size();i++){
			totalTrials = 0;
			readPrecisionAtIters(baseFolder+biasFolder.get(index),subFiles);
//		}
	}
	
	public void readPrecisionAtIters(String folderName, boolean subFiles){
		averageF1OverallIter.clear();
		averagePrecisionOverallIter.clear();
		averageRecallOverallIter.clear();
		medianF1OverallIter.clear();
		medianPrecisionOverallIter.clear();
		medianRecallOverallIter.clear();
		allF1ScoreValuesInIter.clear();
		
		for(int i=1;i<10;i++){
			String iterName = folderName+"/Iter_"+i+"/";
			count =0;
			iterPrecisionMap.clear();
			averageF1OverallIter.put(i-1, new ArrayList<Double>());
			averagePrecisionOverallIter.put(i-1, new ArrayList<Double>());
			averageRecallOverallIter.put(i-1, new ArrayList<Double>());
			medianF1OverallIter.put(i-1, new ArrayList<Double>());
			medianPrecisionOverallIter.put(i-1, new ArrayList<Double>());
			medianRecallOverallIter.put(i-1, new ArrayList<Double>());
			allF1ScoreValuesInIter.put(i-1, new ArrayList<F1Score>());
			if(checkIfDirExists(iterName)){
				if(!subFiles){
					readPrecisionFile(iterName,i);
					currentFileName="";
				} else{
					readPrecisionFilePerPatch(iterName,i);
				}
				findAverageAndMedianPerIteration(folderName);
			}
		}
		findoverAllAverageAndMedian(folderName);		
	}
	
	private void findoverAllAverageAndMedian(String folderString){
		StringBuilder builder = new StringBuilder();
		builder.append("Overall Average across all converge data");
		builder.append("\n");
		builder.append("Total trials : "+totalTrials);
		builder.append("\n");
//		for(Integer key : averagePrecisionOverallIter.keySet()){
//			builder.append("Average precision in iter "+key+" : "+average(averagePrecisionOverallIter.get(key)));
//			builder.append("\n");
//			builder.append("Average recall in iter "+key+" : "+average(averageRecallOverallIter.get(key)));
//			builder.append("\n");
//			builder.append("Average f1 in iter "+key+" : "+average(averageF1OverallIter.get(key)));
//			builder.append("\n");
//		}
		for(Integer key : allF1ScoreValuesInIter.keySet()){
			builder.append("Average precision in iter "+key+" : "+average(allF1ScoreValuesInIter.get(key),"p"));
			builder.append("\n");
			builder.append("Average recall in iter "+key+" : "+average(allF1ScoreValuesInIter.get(key),"r"));
			builder.append("\n");
			builder.append("Average f1 in iter "+key+" : "+average(allF1ScoreValuesInIter.get(key),"f"));
			builder.append("\n");
		}
		
		builder.append("\n");
		String fileName = folderString+"/collatedData_"+currentFileName+".txt";
		writeStringtofile(builder.toString(), fileName);
		
//		for(Integer key : medianF1OverallIter.keySet()){
//			builder.append("Median precision in iter "+key+" : "+median(medianPrecisionOverallIter.get(key)));
//			builder.append("\n");
//			builder.append("Median recall in iter "+key+" : "+median(medianRecallOverallIter.get(key)));
//			builder.append("\n");
//			builder.append("Median f1 in iter "+key+" : "+median(medianF1OverallIter.get(key)));
//			builder.append("\n");
//		}
	}
	
	private double average(List<Double> values){
		double average = 0.0;
		for(int i=0;i<values.size();i++){
			average = average + values.get(i);
		}
		return average/(values.size()*1.0);
	}
	
	
	private void findAverageAndMedianPerIteration(String folderString) {
		StringBuilder builder = new StringBuilder();
		builder.append("For: "+folderString);
		builder.append("\n");
		for(Integer key : iterPrecisionMap.keySet()){
			builder.append("Datapoints that converge in "+ key +" iterations ");
			builder.append("\n");
			builder.append("Number of trials in this iteration "+ count);
			totalTrials = totalTrials + count;
			builder.append("\n");
			for(Integer iterCount : iterPrecisionMap.get(key).keySet()){
				double value = average(iterPrecisionMap.get(key).get(iterCount),"p");
				allF1ScoreValuesInIter.get(iterCount).addAll(iterPrecisionMap.get(key).get(iterCount));
				builder.append("\t Average Precision at iter: "+iterCount+": "+value);			
				builder.append("\n");
				averagePrecisionOverallIter.get(iterCount).add(value);
				value = average(iterPrecisionMap.get(key).get(iterCount),"r");
				builder.append("\t Average Recall at iter: "+iterCount+": "+ value);							
				builder.append("\n");
				averageRecallOverallIter.get(iterCount).add(value);
				value = average(iterPrecisionMap.get(key).get(iterCount),"f");
				builder.append("\t Average F1 at iter: "+iterCount+": "+ value);							
				builder.append("\n");
				averageF1OverallIter.get(iterCount).add(value);
				builder.append("\n");
				
				value = median(iterPrecisionMap.get(key).get(iterCount),"p");
				builder.append("\t Median Precision at iter: "+iterCount+": "+ value);							
				builder.append("\n");
				medianPrecisionOverallIter.get(iterCount).add(value);
				value  = median(iterPrecisionMap.get(key).get(iterCount),"r");
				builder.append("\t Median Recall at iter: "+iterCount+": "+ value);							
				builder.append("\n");
				medianRecallOverallIter.get(iterCount).add(value);
				value = median(iterPrecisionMap.get(key).get(iterCount),"f");
				builder.append("\t Median F1 at iter: "+iterCount+": "+ value);							
				builder.append("\n");
				medianF1OverallIter.get(iterCount).add(value);
				builder.append("\n");
			}
			builder.append("\n");
		}
		
		String fileName = folderString+"/collatedData_"+currentFileName+".txt";
		System.out.print(fileName);
		writeStringtofile(builder.toString(), fileName);
		System.out.println(builder.toString());
	}

	public double average(List<F1Score> values, String averageOf){
		double average = 0.0;
		for(int i=0;i<values.size();i++){
			if(averageOf.equals("p")){
				average = values.get(i).precision + average;
			} else{
				if(averageOf.equals("r")){
					average = values.get(i).recall + average;
				} else{
					values.get(i).calculateF1Score();
					average = values.get(i).score + average;
				}
			}
		}
		return (average/(values.size())*1.0);
	}
	
	public double median(List<F1Score> values, String medianOf){
		List<Double> medianData = new ArrayList<Double>();
		for(int i=0;i<values.size();i++){
			if(medianOf.equals("p")){
				medianData.add(values.get(i).precision);
			} else{
				if(medianOf.equals("r")){
					medianData.add(values.get(i).recall);
				} else{
					values.get(i).calculateF1Score();
					medianData.add(values.get(i).score);
				}
			}
		}
		return median(medianData);		
	}
	
	public double median(List<Double> medianData){
		Collections.sort(medianData);
		int n = medianData.size();
		if (n% 2 != 0)
	        return (double)medianData.get(n/2);
	     
	        return (double)(medianData.get((n - 1) / 2) + medianData.get(n / 2)) / 2.0;
	}
	
	public void readPrecisionFile(String iterName, int iter){
		File folder = new File(iterName);
		File[] listOfFiles = folder.listFiles();
		boolean startReading = false;
		boolean startofLine = false;
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		            String line;
		            while ((line = br.readLine()) != null) {		            	
		            	if(line.equals("Iteration_Start")){
			            		count++;
		            			int iterCount = 0;
		            			if(!iterPrecisionMap.containsKey(iter)){
		            				iterPrecisionMap.put(iter, new HashMap<Integer, List<F1Score>>());
		            			}
			            		line = br.readLine(); 
			            		while(!line.equals("Iteration_End")){
			            			String[] split = line.split(",");
			            			F1Score score = new F1Score();
			            			score.precision = Double.parseDouble(split[0]);
			            			score.recall = Double.parseDouble(split[1]);
			            			score.calculateF1Score();
			            			if(!iterPrecisionMap.get(iter).containsKey(iterCount)){
		            					iterPrecisionMap.get(iter).put(iterCount, new ArrayList<F1Score>());
			            			}
			            			iterPrecisionMap.get(iter).get(iterCount).add(score);
			            			iterCount++;
			            			line = br.readLine(); 
			            		}
			            	}	
			            	if(line.contains("/home/")){
			            		startReading = true;
			            		String[] tmp = line.split("/");
			            		currentFileName = tmp[tmp.length-1];
			            		if(!fileNames.contains(currentFileName)){
			            			fileNames.add(currentFileName);
			            		}
			            	}
		            	}	
		            System.out.println("Size of fileNames "+fileNames.size());
		            System.out.println(fileNames);
		        } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
		
	}
	
	
	public void readPrecisionFilePerPatch(String iterName, int iter){
		File folder = new File(iterName);
		File[] listOfFiles = folder.listFiles();
		boolean startReading = false;
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		            String line;
		            while ((line = br.readLine()) != null) {		            	
		            	while(startReading || line.contains(currentFileName)){
		            		if(!startReading){
		            			line = br.readLine(); 
		            		}
			            	if(line.equals("Iteration_Start")){
			            		count++;
		            			int iterCount = 0;
		            			if(!iterPrecisionMap.containsKey(iter)){
		            				iterPrecisionMap.put(iter, new HashMap<Integer, List<F1Score>>());
		            			}
			            		line = br.readLine(); 
			            		while(!line.equals("Iteration_End")){
			            			String[] split = line.split(",");
			            			F1Score score = new F1Score();
			            			score.precision = Double.parseDouble(split[0]);
			            			score.recall = Double.parseDouble(split[1]);
			            			score.calculateF1Score();
			            			if(!iterPrecisionMap.get(iter).containsKey(iterCount)){
		            					iterPrecisionMap.get(iter).put(iterCount, new ArrayList<F1Score>());
			            			}
			            			iterPrecisionMap.get(iter).get(iterCount).add(score);
			            			iterCount++;
			            			line = br.readLine(); 
			            		}
			            	}
			            	line = br.readLine(); 
			            	if(line == null){
			            		break;
			            	}
			            	if(line.equals("Iteration_Start")){
			            		startReading = true;
			            	} else{
			            		startReading = false;
			            	}			            	
		            	}
		            }
		        } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
		
	}
	
	public void writeStringtofile(String content, String fileName){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,true));
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean checkIfDirExists(String filename){
		File file = new File(filename);
		return file.exists();
	}
}
