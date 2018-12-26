package alice.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import learn.Learner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import extractor.extract.ASTUtil;
import extractor.extract.Extractor;
import alice.menu.BaseAction;
import alice.menu.DisableFeature;
import alice.menu.EnableFeature;
import alice.menu.GenerateFacts;

public class ExploreFeatureAutomatedTest extends BaseAction{

	public static HashMap<String, HashMap<Integer, Integer>> fileToFeatureExamplesMap = new HashMap<String, HashMap<Integer,Integer>>();
	public static HashMap<String, HashMap<Integer, F1Score>> fileToFeatureVaryF1Map = new HashMap<String, HashMap<Integer,F1Score>>();
	public String projectName;
	boolean featureEvaluation = false;
	boolean varyPosNeg = true;
	boolean noisyOracle = false;
	List<String> enabledString = new ArrayList<String>();
	ExploreActiveLearningAutomatedTest activeLearningAutomatedTest = new ExploreActiveLearningAutomatedTest();
	boolean processResults = false;
	
	@Override
	protected void run(ISelection selection) {
		if(processResults){
			ResultsProcessorMain processor = new ResultsProcessorMain();
			processor.run();
		} else{
			exploration(selection);
		}
	}
	
	public void exploration(ISelection selection){
		System.out.println("Automated ALICE Evaluation");
		extractFactsForSelection(selection);
		runTestsOnFolder("/home/whirlwind/workspace/ALICE_UI/resources/"+projectName+"/");
//		runTestOnFile("/home/whirlwind/workspace/ALICE_UI/resources/OLD_WIN3213515/DestroyItem.txt");
		printFeaturesVsAverageExamplesEvaluation();	
	}
	
	public void extractFactsForSelection(ISelection selection){
		IStructuredSelection sel = (IStructuredSelection) selection;
        Object firstElement = sel.getFirstElement();
        if (firstElement instanceof IAdaptable)
        {        
            IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);           
            projectName = project.getName().trim().toString();
            IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();                                              
        }
	}
	
	public void printFeaturesVsAverageExamplesEvaluation(){
		for(String fileName: fileToFeatureExamplesMap.keySet()){
			System.out.println("Eval for "+fileName);
			HashMap<Integer,Integer> featureExampleMap = fileToFeatureExamplesMap.get(fileName);
			for(Integer featureNumber: featureExampleMap.keySet()){
				System.out.println("#Features : "+featureNumber+" --- #Examples "+featureExampleMap.get(featureNumber));
				System.out.println("#Features : "+featureNumber+" --- F1 Score "+fileToFeatureVaryF1Map.get(fileName).get(featureNumber).score);
				System.out.println("#Features : "+featureNumber+" --- Precision "+fileToFeatureVaryF1Map.get(fileName).get(featureNumber).precision);
				System.out.println("#Features : "+featureNumber+" --- Recall "+fileToFeatureVaryF1Map.get(fileName).get(featureNumber).recall);				
			}
		}
	}
	
	public void runTestsOnFolder(String path){
		List<String> ignoreFiles= new ArrayList<String>();
//		ignoreFiles.add("Release.txt");
//		ignoreFiles.add("Showdroptargeteffect.txt");
//		ignoreFiles.add("VerifyText.txt");
//		ignoreFiles.add("GetNextToken.txt");
//		ignoreFiles.add("SetSync.txt");
//		ignoreFiles.add("FixFocus.txt");
//		ignoreFiles.add("SetAntiAlias.txt");
		ignoreFiles.add("GetFont.txt");
//		ignoreFiles.add("SetItems.txt");
//		ignoreFiles.add("WMKeyDown.txt");
		
		List<String> operateOnFiles = new ArrayList<String>();
//		operateOnFiles.add("GetLeadingComments.txt");
//		operateOnFiles.add("GetNextToken.txt");
//		operateOnFiles.add("GetNextChar.txt");
//		operateOnFiles.add("GetFont.txt");
//		operateOnFiles.add("WMKeyDown.txt");
//		operateOnFiles.add("DestroyItem.txt");
		operateOnFiles.add("SetItems.txt");
//		operateOnFiles.add("GetSelectionText.txt");
//		operateOnFiles.add("SetAntiAlias.txt");
		
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	System.out.println(file.getName());
		    	if(!file.getName().contains("Ground") && !file.getName().contains("Full") && !file.getName().contains("Features")){
		    		if(!ignoreFiles.contains(file.getName())){
		    			if(operateOnFiles.contains(file.getName())){
		    				
		    			}
		    			runTestOnFile(file.getAbsolutePath().toString());
		    		}
		    	}
		    }
		}
	}
	
	public void runTestOnFile(String path){
		Util util = new Util();
		String sel = util.readFileInput(path);
		activeLearningAutomatedTest = new ExploreActiveLearningAutomatedTest();
		activeLearningAutomatedTest.getGroundTruth(path);
		
		Extraction extraction = new Extraction();
		String file = path.split("[.]txt")[0]+"_Full"+".txt";
		extraction.craftSelectionInfo(sel, "method", "class",file);
		

		List<String> candidateFeatures = ASTUtil.getCandidateFeatures(sel,extraction.types);
		System.out.println("Candidates are ");
		System.out.println(candidateFeatures);
		HashMap<Integer,Integer> featuresExamplesMap = new HashMap<Integer, Integer>();
		
		int maxNumberOfFeatures = 5;
		if(candidateFeatures.size() < maxNumberOfFeatures){
			maxNumberOfFeatures = candidateFeatures.size();
		}
		
		if(featureEvaluation){
			int noOfRuns = 10;
			fileToFeatureVaryF1Map.put(path, new HashMap<Integer, F1Score>());
			for(int i=1;i<maxNumberOfFeatures;i++){
				fileToFeatureVaryF1Map.get(path).put(i, new F1Score());
			}
			
			for(int j=0;j<noOfRuns;j++){
				enabledString.clear();
				for(int i=1;i<maxNumberOfFeatures;i++){
					System.out.println("Running for iter ------" + i);
					int averageExampels = averagePrecision(i, 1, candidateFeatures,path);
					System.out.println("-----------");
					if(featuresExamplesMap.containsKey(i)){
						int val = featuresExamplesMap.get(i) + averageExampels;
						featuresExamplesMap.put(i, val);
					} else{
						featuresExamplesMap.put(i, averageExampels);
					}
				}
			}
			for(Integer key : featuresExamplesMap.keySet()){
				int val = featuresExamplesMap.get(key);
				featuresExamplesMap.put(key, val/noOfRuns);
			}
			
			for(Integer key: fileToFeatureVaryF1Map.get(path).keySet()){
				fileToFeatureVaryF1Map.get(path).get(key).averagePresAndRec(noOfRuns);
				fileToFeatureVaryF1Map.get(path).get(key).calculateF1Score();
			}
			
		} else{
			if(varyPosNeg){
//				TestInductiveBias bias = new TestInductiveBias();
//				TestOnlyPosAndNeg bias = new TestOnlyPosAndNeg();
//				bias.run(path, candidateFeatures);
//				TestMultipleLabels bias = new TestMultipleLabels();
//				TestCritics bias = new TestCritics();
//				TestNoisyOracle bias = new TestNoisyOracle();
				TestTopDownPerformance bias = new TestTopDownPerformance();
				bias.run(path, candidateFeatures);
				
//				activeLearningAutomatedTest.run(path, candidateFeatures);
			} else{
				if(noisyOracle){
					ActiveLearningWithNoisyOracle activeLearningWithNoisyOracle = new ActiveLearningWithNoisyOracle();
					activeLearningWithNoisyOracle.run(path, candidateFeatures);
				}
			}
		}
		fileToFeatureExamplesMap.put(path, featuresExamplesMap);
	}
	
	public int averagePrecision(int noOfFeatures, int noOfIterations, List<String> features, String path){
		int sum = 0;
		HashMap<Integer, List<String>> enabledStringsAtIter = new HashMap<Integer, List<String>>();
		try{
			  for(int i=0;i<noOfIterations;i++){
				List<Integer> indexArray = new ArrayList<Integer>();
				int afterFeatures = noOfFeatures - enabledString.size();				
				for(int j=0;j<afterFeatures;){
					
					int index = (int) (Math.random()*(features.size()));
//					String value = features.get(index).replaceAll("\\\\", "");
//					 value = value.replaceAll("\"", "");
					String value = features.get(index);
					if(!enabledString.contains(value)){
						indexArray.add(index);				
						enabledString.add(value);	
						j++;
					}
				}
				
//				System.out.println("Enabled Features are : ");
//				System.out.println(enabledString);
//				enabledString.clear();
//				enabledString.add("commentarray");
//				if(!checkIfInHistory(enabledStringsAtIter, enabledString)){
					enabledStringsAtIter.put(i,enabledString);
//					i++;
					Learner learn = new Learner();
			        Learner.factBaseName = "factsFromExtractor19.pl";	
			        try{
			        	learn.init(Extraction.extractFromBlockRelational.visitor.predicateDefiDefs, GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, enabledString, DisableFeature.disabledSelectionStrings, Extraction.extractFromBlockRelational.visitor.generalisedPredicates);
			        }catch(Exception e){		
			        	System.out.println("Features are in exception");
			        	System.out.println(enabledString);
			        	System.out.println("Exception in learning.");
			        	i--;
			        	continue;
			        }
			        
			        F1Score score = new F1Score();
			        score = activeLearningAutomatedTest.calculatePrecisionAndRecall(learn.solutionObjects);
			        if(Double.isNaN(score.precision)){
			        	System.out.println("Features are ");
			        	System.out.println(enabledString);
			        }
			        fileToFeatureVaryF1Map.get(path).get(noOfFeatures).updatePrecisionAndRecall(score.precision, score.recall);
			    	sum = sum+ learn.solutionObjects.size();			
		    }
		  return sum/noOfIterations;
		} catch(Exception e){
			System.out.println("Features are in exception");
        	System.out.println(enabledString);
			System.out.println("Exception ");
		}
	return 0;
	}
	
	public boolean checkIfInHistory(HashMap<Integer, List<String>> enabledIterMap,List<String> enabledStrings){
		for(Integer iter: enabledIterMap.keySet()){
			if(enabledIterMap.get(iter).equals(enabledStrings)){
				return true;
			}
		}
		return false;
	}
	protected boolean isEnabled(ISelection s) {		
		IStructuredSelection sel = (IStructuredSelection) s;
        Object firstElement = sel.getFirstElement();
        if (firstElement instanceof IAdaptable)
        {
            IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
            if(project!=null){
            	IPath path =  project.getFullPath();
            	if(path == null){
            		return false;
            	}           	
            } else{
            	return false;
            }                        
        }
		return true;
	}		
}
