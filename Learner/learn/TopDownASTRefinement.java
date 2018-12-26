package learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import util.ClauseUtils;
import def.PredicateDef;

public class TopDownASTRefinement {
	//First add temporal ordering and see if neg is unsat & all pos sat 
	//if temporal fails, add predicates without ordering and chckfor the one that has no neg
	//and all pos. 
	boolean modifiedContainment = false;
	Stack<String> previousIterIds = new Stack<String>();
	HashMap<String, PredicateDef> predicateMap = new HashMap<String, PredicateDef>();
	public List<PredicateDef> remainingPredicates = new ArrayList<PredicateDef>();
	boolean isRandomAdded = false;
	boolean isParentAdded = false;
	
	
	public List<PredicateDef> returnPredicates(){
		isRandomAdded = false;
		HashMap<String, PredicateDef> remainingPredicatemap = new HashMap<String, PredicateDef>();
		HashMap<String, Integer> remainingPredicateId = new HashMap<String, Integer>();
		
		List<PredicateDef> selectedPredicates = new ArrayList<PredicateDef>();
		boolean added = false;
		for(int i=0;i<remainingPredicates.size();i++){
			remainingPredicatemap.put(remainingPredicates.get(i).values.get(0), remainingPredicates.get(i));
			remainingPredicateId.put(remainingPredicates.get(i).values.get(0), i);				
		}
		
		
		for(int i=0;i<previousIterIds.size();i++){
			List<String> availableIds = new ArrayList<String>();
			if(Learner.containedMap.containsKey(previousIterIds.get(i))){
			availableIds = 
					Learner.containedMap.get(previousIterIds.get(i));
			} 
			for(int j=0;j<availableIds.size();j++){
				if(remainingPredicatemap.containsKey(availableIds.get(j)) && !previousIterIds.contains(availableIds.get(j))){
					PredicateDef def = new PredicateDef();
					def.predType = "contained";
					def.values = new ArrayList<String>();
					def.values.add("X");
					def.values.add(previousIterIds.get(i));
					selectedPredicates.add(def);
					selectedPredicates.add(predicateMap.get(previousIterIds.get(i)));
					
					def = new PredicateDef();
					def.predType = "contained";
					def.values = new ArrayList<String>();
					def.values.add(previousIterIds.get(i));
					def.values.add(availableIds.get(j));
					selectedPredicates.add(def);
					selectedPredicates.add(remainingPredicatemap.get(availableIds.get(j)));
					int index = remainingPredicateId.get(availableIds.get(j)).intValue();
					remainingPredicates.remove(index);					
					added = true;
					break;
				}				
			}
			if(added){
				break;
			}
		}
		if(!added){
			selectedPredicates.clear();
			selectedPredicates.addAll(pickFromParentContained());
			if(selectedPredicates.size()<2){
				added = false;
				isParentAdded = false;
			} else{
				added = true;
				isParentAdded = true;
			}
		}
		
		if(!added && !isParentAdded){
			isRandomAdded = true;
			selectedPredicates.addAll(pickRandomPredicates());
		}
		return selectedPredicates;
	}
	
	public List<PredicateDef> pickFromParentContained(){
		isParentAdded = false;
		HashMap<String, PredicateDef> remainingPredicatemap = new HashMap<String, PredicateDef>();
		HashMap<String, Integer> remainingPredicateId = new HashMap<String, Integer>();
		
		List<PredicateDef> selectedPredicates = new ArrayList<PredicateDef>();
		boolean added = false;
		for(int i=0;i<remainingPredicates.size();i++){
			remainingPredicatemap.put(remainingPredicates.get(i).values.get(0), remainingPredicates.get(i));
			remainingPredicateId.put(remainingPredicates.get(i).values.get(0), i);				
		}
		
		for(int i=0;i<previousIterIds.size();i++){
			List<String> availableIds = new ArrayList<String>();
			for(String key : Learner.containedMap.keySet()){
				if(key!="X"){
					if(Learner.containedMap.get(key).contains(previousIterIds.get(i))){
						availableIds.add(key);
					}
				}					
			}
			for(int j=0;j<availableIds.size();j++){
				if(remainingPredicatemap.containsKey(availableIds.get(j)) && !previousIterIds.contains(availableIds.get(j))){
					PredicateDef def = new PredicateDef();
					def.predType = "contained";
					def.values = new ArrayList<String>();
					def.values.add("X");
					def.values.add(availableIds.get(j));
					selectedPredicates.add(def);
					selectedPredicates.add(remainingPredicatemap.get(availableIds.get(j)));
					
					
					
					def = new PredicateDef();
					def.predType = "contained";
					def.values = new ArrayList<String>();					
					def.values.add(availableIds.get(j));		
					def.values.add(previousIterIds.get(i));
					selectedPredicates.add(def);
					selectedPredicates.add(predicateMap.get(previousIterIds.get(i)));
										
					int index = remainingPredicateId.get(availableIds.get(j)).intValue();
					remainingPredicates.remove(index);	
					isParentAdded = true;
					added = true;
					break;
				}				
			}
			if(added){
				break;
			}
			
		}		
		return selectedPredicates;
	}
	
	public List<PredicateDef> pickRandomPredicates(){
		List<PredicateDef> selectedPredicates = new ArrayList<PredicateDef>();
		int index = (int) (Math.random()*(remainingPredicates.size()));
		
		PredicateDef def = new PredicateDef();
		def.predType = "contained";
		def.values = new ArrayList<String>(2);
		def.values.add("X");
		def.values.add(remainingPredicates.get(index).values.get(0));		
		selectedPredicates.add(def);
		selectedPredicates.add(remainingPredicates.get(index));
		remainingPredicates.remove(index);		
		return selectedPredicates;
	}
	
	public void populatePreviousIterIds(List<PredicateDef> original){
		for(int i =1;i<original.size();i++){
			if(!original.get(i).predType.equals("contained")){
				previousIterIds.push(original.get(i).values.get(0));
				predicateMap.put(original.get(i).values.get(0), original.get(i));
			}
		}
	}
	
	public List<PredicateDef> addContainmentRelationAsSeparator(List<PredicateDef> original){
		populatePreviousIterIds(original);
		String originalQuery = Learner.generatePrologQuery(original);
		HashMap<String, String> containUpdateMap = new HashMap<String, String>();
		List<PredicateDef> predicates = new ArrayList<PredicateDef>();
		predicates.add(original.get(0));
		boolean added = false;
		for(int i=1;i<original.size();i++){
			if(!original.get(i).predType.equals("contained")){
				if(!containUpdateMap.containsKey(original.get(i).values.get(0))){
					added = false;
					for(int j=1;j<original.size();j++){
						if(!original.get(j).predType.equals("contained") && j!=i){
							String iId = original.get(i).values.get(0);
							String jId = original.get(j).values.get(0);
							if(Learner.containedMap.containsKey(iId)){
								if(Learner.containedMap.get(iId).contains(jId)){
									containUpdateMap.put(jId, iId);
									added = true;
									modifiedContainment = true;
								} 
							}
						}
					}
					if(!added){
						containUpdateMap.put(original.get(i).values.get(0), "X");
					}
				}
//				previousIterIds.push(original.get(i).values.get(0));
			}
		}
		for(int i=1;i<original.size();i++){
			if(!original.get(i).predType.equals("contained")){
				PredicateDef def = new PredicateDef();
				def.predType = "contained";
				def.values = new ArrayList<String>();
				if(containUpdateMap.containsKey(original.get(i).values.get(0))){
					def.values.add(containUpdateMap.get(original.get(i).values.get(0)));
				} else{
					def.values.add("X");
				}
				def.values.add(original.get(i).values.get(0));
				predicates.add(def);
				predicates.add(original.get(i));
			}
		}
		if(Learner.generatePrologQuery(predicates).toLowerCase().equals(originalQuery.toLowerCase())){
			modifiedContainment = false;
		}
		return predicates;
	}
	
	
	
}
