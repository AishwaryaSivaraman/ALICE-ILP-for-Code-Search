package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import learn.Learner;
import def.PredicateDef;

public class PredicateMapUtil {
	
	public static HashMap<String, String> predicateValueIdMap = new HashMap<String, String>();
	public static HashMap<String, PredicateDef> idPredicateMap = new HashMap<String, PredicateDef>();
	public static List<String> variableIds = new ArrayList<String>();
	
	public static PredicateDef methodDec = new PredicateDef();
	
	public static void updatePredicateValueIDMap(List<PredicateDef> original){		
		for(int i=0;i<original.size();i++){
			if(original.get(i).values.size()>1){
//				String replaceBackSlack =original.get(i).values.get(1).replaceAll("\\\\", ""); 
				String replaceBackSlack =original.get(i).values.get(1);
				replaceBackSlack = replaceBackSlack.replaceAll("\"", "");
				if(!predicateValueIdMap.containsKey(replaceBackSlack)){
					predicateValueIdMap.put(replaceBackSlack, original.get(i).values.get(0));
				}
			}
			if(original.get(i).predType.equals("methoddec")){
				methodDec =original.get(i);
			}
		}
	}
	
	public static void removeContainedInAfter(){
		for(String key: Learner.afterMap.keySet()){
			List<String> ids = new ArrayList<String>();
			ids.addAll(Learner.afterMap.get(key));
			for(int i=0;i<ids.size();i++){
				if(Learner.containedMap.containsKey(key)){
					if(Learner.containedMap.get(key).contains(ids.get(i))){
						ids.remove(i);
						i = -1;
					}
				}
			}
			Learner.afterMap.put(key, ids);
		}
	}
	
	public static void updateidPredicateMap(List<PredicateDef> original){		
		for(int i=0;i<original.size();i++){
			if(original.get(i).values.size()>1){
				if(!original.get(i).predType.equals("after") && !original.get(i).predType.equals("contained")){
					idPredicateMap.put(original.get(i).values.get(0), original.get(i));
				}
			}
		}
	}
	
	public static void populateVariableIds(List<PredicateDef> query){
		variableIds.clear();
		for(int i=0;i<query.size();i++){
			if(!variableIds.contains(query.get(i).values.get(0))){
				variableIds.add(query.get(i).values.get(0));
			}
		}
	}
	
	public static HashMap<String, List<String>> returnAllFirstContainedChildren(List<PredicateDef> original){		
		HashMap<String, List<String>> firstChildren = new HashMap<String, List<String>>();
		String previousContainsID1 = "";
		String previousContainsID2 = "";
		for(int i=0;i<original.size();i++){			
			if(original.get(i).predType.equals("contains")){
				String currentContainID1 = original.get(i).values.get(0).toUpperCase();
				String currentContainID2 = original.get(i).values.get(1).toUpperCase();				
				if(!firstChildren.containsKey(currentContainID1)){
					List<String> idList = new ArrayList<String>();
					idList.add(currentContainID2);
					firstChildren.put(currentContainID1, idList);
				} else{
					firstChildren.get(currentContainID1).add(currentContainID2);
				}								
			}
		}
		return firstChildren;
	}
	
	public static HashMap<String, List<String>> populateAllChildren(HashMap<String, List<String>> firstChildren){				
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		Queue<String> childrenQueue = new LinkedList<String>();
		for(String id: firstChildren.keySet()){
			map.put(id, new ArrayList<String>());
			childrenQueue.addAll(firstChildren.get(id));
			while(!childrenQueue.isEmpty()){
				String currentId = childrenQueue.poll();
				map.get(id).add(currentId);
				if(firstChildren.containsKey(currentId)){
					childrenQueue.addAll(firstChildren.get(currentId));
				}
			}
		}
		return map;
	}
	
	public static void printMyChildren(HashMap<String, List<String>> map){
		for(String id: map.keySet()){
			System.out.println(id);
			System.out.println(map.get(id));
		}
	}
	
	public static HashMap<String, List<String>> returnAllFirstAfterChildren(List<PredicateDef> original){		
		HashMap<String, List<String>> firstChildren = new HashMap<String, List<String>>();
		for(int i=0;i<original.size();i++){			
			if(original.get(i).predType.equals("after")){
				String currentContainID1 = original.get(i).values.get(1).toUpperCase();
				String currentContainID2 = original.get(i).values.get(0).toUpperCase();				
				if(!firstChildren.containsKey(currentContainID1)){
					List<String> idList = new ArrayList<String>();
					idList.add(currentContainID2);
					firstChildren.put(currentContainID1, idList);
				} else{
					firstChildren.get(currentContainID1).add(currentContainID2);
				}								
			}
		}
		return firstChildren;
	}
	
	
	public static boolean checkIfTwoPredicateListsAreEqual(List<PredicateDef> listOne, List<PredicateDef> listTwo){
		if(listOne.size() != listTwo.size()){
			return false;
		} else{
			List<String> idOne = new ArrayList<String>();
			List<String> valuesOne = new ArrayList<String>();
			List<String> idTwo = new ArrayList<String>();
			List<String> valuesTwo = new ArrayList<String>();
			for(int i=1;i<listOne.size();i++){
				idOne.add(listOne.get(i).values.get(0));
				idTwo.add(listTwo.get(i).values.get(0));
				valuesOne.add(listOne.get(i).values.get(1));
				valuesTwo.add(listTwo.get(i).values.get(1));
			}
			if(idOne.equals(idTwo) && valuesOne.equals(valuesTwo)){
				return true;
			}
		}
		return false;
	}
}
