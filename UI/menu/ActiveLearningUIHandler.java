package alice.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import learn.ActiveLearning;
import learn.Learner;
import learn.Solution;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import def.JavaSourceCodeInfo;
import extractor.info.PackageInfo;
import alice.info.SearchResults;
import alice.util.ExampleFromPrologResults;
import alice.views.AliceExampleView;
public class ActiveLearningUIHandler extends Action implements IWorkbenchAction
{
	private static final String ID = "alice.menu.ActiveLearningUIHandler";
	HashMap<String, HashMap<Integer, List<JavaSourceCodeInfo>>> mappedQuerytoExamples = new HashMap<String, HashMap<Integer,List<JavaSourceCodeInfo>>>(); 
	
	public ActiveLearningUIHandler() {
		setId(ID);
	}
	
	public void run() {	
		System.out.println("Hello world");
		getCheckedExamples();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void clusterQueries(List<JavaSourceCodeInfo> examples, int type){
		for(int i=0;i<examples.size();i++){
			if(mappedQuerytoExamples.containsKey(examples.get(i).query)){
				mappedQuerytoExamples.get(examples.get(i).query).get(type).add(examples.get(i));
			} else{
				mappedQuerytoExamples.put(examples.get(i).query, new HashMap<Integer, List<JavaSourceCodeInfo>>());
				mappedQuerytoExamples.get(examples.get(i).query).put(1, new ArrayList<JavaSourceCodeInfo>());
				mappedQuerytoExamples.get(examples.get(i).query).put(2, new ArrayList<JavaSourceCodeInfo>());
				mappedQuerytoExamples.get(examples.get(i).query).get(type).add(examples.get(i));
			}
		}					
	}
	
	public void getCheckedExamples(){
		List<JavaSourceCodeInfo> positiveExamples = new ArrayList<JavaSourceCodeInfo>();
		List<JavaSourceCodeInfo> negativeExamples = new ArrayList<JavaSourceCodeInfo>();
		
		
		final TableItem [] items =  AliceExampleView.viewer.getTable().getItems();
		TableEditor editor =  new TableEditor(AliceExampleView.viewer.getTable());
//		
		for(int i=0;i<items.length;i++){		
			if(SearchResults.exampleTypeAtIndex(i) == 1){
				System.out.println("Positive Example : "+SearchResults.solutions.get(i).methodName);	
				System.out.println("Query : "+SearchResults.solutions.get(i).query);
				if(!SearchResults.solutions.get(i).isOld){
					positiveExamples.add(SearchResults.solutions.get(i));
				}
			} else{
				if(SearchResults.exampleTypeAtIndex(i) == 2){
					System.out.println("Negative Example Intersection: "+Learner.generatePrologQuery(SearchResults.solutions.get(i).remainingPredicates));					
					System.out.println("Query : "+SearchResults.solutions.get(i).query);
					if(!SearchResults.solutions.get(i).isOld){
						negativeExamples.add(SearchResults.solutions.get(i));
					}
				}
			}			
		}
		
		clusterQueries(positiveExamples, 1);
		clusterQueries(negativeExamples, 2);

		System.out.println(mappedQuerytoExamples);
		String biggestQuery ="";
		int max=0;
		for(String query: mappedQuerytoExamples.keySet()){
			if(query.length()>max){
				max = query.length();
				biggestQuery = query;				
			}			
		}
		
		ActiveLearning activeLearning = new ActiveLearning();
//		for(String query : mappedQuerytoExamples.keySet()){
//			HashMap<Integer,Solution> solutionObjects = activeLearning.specializeQuery(mappedQuerytoExamples.get(query));
		//need try catch here
			try{
			HashMap<Integer,Solution> solutionObjects = activeLearning.specializeQuery(mappedQuerytoExamples.get(biggestQuery));
			List<JavaSourceCodeInfo> sourceCodeInfo = ExampleFromPrologResults.populatecodeInfo(PackageInfo.classInfoMap, 
	        		PackageInfo.lineInfoForVariables, null,solutionObjects);
			SearchResults.setOldExamples(sourceCodeInfo.size());
			
			List<JavaSourceCodeInfo> old = new ArrayList<JavaSourceCodeInfo>();
			old.addAll(SearchResults.solutions);
			SearchResults.solutions.clear();
			SearchResults.solutions.addAll(sourceCodeInfo);
			SearchResults.solutions.addAll(old);
			
			System.out.printf("Size of solution %d and exampletype %d is ",SearchResults.solutions.size(),SearchResults.exampleType.length);
			System.out.println();
		AliceExampleView.clear = true;
		AliceExampleView.updateViewer(SearchResults.solutions);
		}catch (Exception e){
			Display display = PlatformUI.getWorkbench().getDisplay();
		    Shell shell = new Shell(display);
		    int style = SWT.ICON_ERROR;
		    
		    MessageBox messageBox = new MessageBox(shell, style);
		    messageBox.setMessage("Alice couldnt find a separator for the marked positive and negative labels! Please label again.");		    
		    int rc = messageBox.open();		    
		}
//		Stack<String> predicates = new Stack<String>();
//		predicates.addAll(Learner.orginalPredicateList);
//		
//		SpecializationOperator operator = new SpecializationOperator(positiveExamples,negativeExamples,predicates);
//		operator.specizlize();
//		
//		CriticsOverlaySearchPredicate.updateViewer();
	}
	
	
}
