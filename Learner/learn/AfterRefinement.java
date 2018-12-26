package learn;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import def.PredicateDef;

public class AfterRefinement {

	
	public static List<String> ids = new ArrayList<String>();
	Stack<String> previousIterIds = new Stack<String>();
	HashMap<String, PredicateDef> predicateMap = new HashMap<String, PredicateDef>();
	boolean isRandomAdded = false;
	
	public List<PredicateDef> returnPredicates(List<PredicateDef> remainingPredicates){
		boolean isAdded = false;		
		List<PredicateDef> predicatesToReturn = new ArrayList<PredicateDef>();
		List<String> idChoices = new ArrayList<String>();
		HashMap<String, String> beforeMe = new HashMap<String, String>();
		for(int i=0;i<ids.size();i++){
			if(Learner.afterMap.containsKey(ids.get(i))){
				idChoices.addAll(Learner.afterMap.get(ids.get(i)));
				List<String> localIds = Learner.afterMap.get(ids.get(i));
				for(int j=0;j<localIds.size();j++){
					beforeMe.put(localIds.get(j), ids.get(i));
				}
			}
		}
		for(int i=0;i<idChoices.size();i++){
			for(int j=0;j<remainingPredicates.size();j++){
				if(remainingPredicates.get(j).values.get(0).equals(idChoices.get(i))){
					PredicateDef defParent = new PredicateDef();
					defParent.predType = "contained";
					defParent.values = new ArrayList<String>();
					defParent.values.add("X");
					defParent.values.add(beforeMe.get(idChoices.get(i)));
					predicatesToReturn.add(defParent);
					predicatesToReturn.add(predicateMap.get(beforeMe.get(idChoices.get(i))));
					
					PredicateDef def = new PredicateDef();
					def.predType = "contained";
					def.values = new ArrayList<String>();
					def.values.add("X");
					def.values.add(remainingPredicates.get(j).values.get(0));
					predicatesToReturn.add(def);
					predicatesToReturn.add(remainingPredicates.get(j));
					
					
					PredicateDef defChild = new PredicateDef();
					defChild.predType = "before";
					defChild.values = new ArrayList<String>();
					defChild.values.add(beforeMe.get(idChoices.get(i)));
					defChild.values.add(remainingPredicates.get(j).values.get(0));
					predicatesToReturn.add(defChild);
					remainingPredicates.remove(j);
					isAdded = true;
					break;
				}
			}
			if(isAdded){
				break;
			}
		}
		if(!isAdded){
			isRandomAdded = true;
			predicatesToReturn.addAll((pickRandomPredicates(remainingPredicates)));
		}
		return predicatesToReturn;		
	}
	
	public List<PredicateDef> pickRandomPredicates(List<PredicateDef> remainingPredicates){
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
			if(!original.get(i).predType.equals("contained") && !original.get(i).predType.equals("before")){
				previousIterIds.push(original.get(i).values.get(0));
				predicateMap.put(original.get(i).values.get(0), original.get(i));
			}
		}
	}
	
	public HashMap<String, List<String>> beforeRelationAdded(List<PredicateDef> original){
		HashMap<String, List<String>> beforeMap = new HashMap<String, List<String>>();
		for(int i=0;i<original.size();i++){
			if(original.get(i).predType.equals("before")){
				if(!beforeMap.containsKey(original.get(i).values.get(0))){
					beforeMap.put(original.get(i).values.get(0), new ArrayList<String>());
				}
				beforeMap.get(original.get(i).values.get(0)).add(original.get(i).values.get(1));
			}
		}
		return beforeMap;
	}
	
	public List<PredicateDef> addAfterRelation(List<PredicateDef> original){
		ids.clear();
		List<PredicateDef> predicates = new ArrayList<PredicateDef>();
		HashMap<String, List<String>> beforeMap = beforeRelationAdded(original);
		predicates.addAll(original);
		boolean added = false;
		for(int i=1;i<original.size();i++){
			if(!original.get(i).predType.equals("contained")){
				ids.add(original.get(i).values.get(0));
			}
		}
		
		boolean addedBefore = false;
		for(int i=0;i<ids.size();i++){
			if(Learner.afterMap.containsKey(ids.get(i))){
				for(String key: Learner.afterMap.get(ids.get(i))){
					if(ids.contains(key)){
						added = true;
						PredicateDef def = new PredicateDef();
						def.predType = "before";
						def.values = new ArrayList<String>();
						def.values.add(ids.get(i));
						def.values.add(key);
						if(!(beforeMap.containsKey(ids.get(i)) && beforeMap.get(ids.get(i)).contains(key))){
							predicates.add(def);
							addedBefore = true;
							break;
						}
					}
				}
			}
			if(addedBefore){
				break;
			}
		}
		
		return predicates;
	}
}
