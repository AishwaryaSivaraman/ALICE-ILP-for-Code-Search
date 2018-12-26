package learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import def.PredicateDef;

public class InductiveBias {

	public List<List<PredicateDef>> typeBias(List<List<PredicateDef>> nodes){		
		return sortListBasedOnBias("containstype", nodes);
	}
	
	public List<List<PredicateDef>> methodBias(List<List<PredicateDef>> nodes){
		return sortListBasedOnBias("methodcall", nodes);
	}
	
	public List<List<PredicateDef>> structureBias(List<List<PredicateDef>> nodes){		
		return sortListBasedOnBias("containsiterator", nodes);
	}
	
	public List<PredicateDef> eliminiateCommomElementBias(){
		return null;
	}
	
	public List<PredicateDef> uniquePredicateBias(){
		return null;
	}
	
	
	public List<List<PredicateDef>> sortListBasedOnBias(String predType, List<List<PredicateDef>> nodes){
		List<List<PredicateDef>> sortedList = new ArrayList<List<PredicateDef>>();
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		int indexCount = 0;
		
		for(List<PredicateDef> predicates : nodes){
			int count = 
					countPredicate(predicates, predType);		
			countMap.put(indexCount, count);												
			indexCount++;			
		}
		
		Object[] a = countMap.entrySet().toArray();
		Arrays.sort(a, new Comparator() {
		    public int compare(Object o1, Object o2) {
		        return ((Map.Entry<Integer, Integer>) o2).getValue()
		                   .compareTo(((Map.Entry<Integer, Integer>) o1).getValue());
		    }
		});
		
		indexCount = 0;
		for (Object e : a) {
			
			int value = ((Map.Entry<Integer, Integer>) e).getValue();
			int key = ((Map.Entry<Integer, Integer>) e).getKey();
//		    System.out.println( key+ " : "+ value);
		    
		    sortedList.add(indexCount, nodes.get(key));
		    indexCount++;   
		}
			
		return sortedList;		
	}
	
	
	public int countPredicate(List<PredicateDef> definitions, String predType){
		int count = 0;
		for(PredicateDef pred : definitions){
			if(pred.predType.equals(predType)){
				count++;
			}
		}		
		return count;
	}
	
	
	public List<List<PredicateDef>> dropBasedOnFrequency(List<List<PredicateDef>> definitions){
		int max = 0;
		int maxIndex=0;
		for(int i=0;i<definitions.size();i++){
			for(int j=0;j<definitions.get(i).size();j++){
				PredicateDef local = definitions.get(i).get(j);
				if(local.predType.equals("containstype")){
					int tmp = Learner.TypeFrequency.get(local.getValues().get(2));
					if(max<tmp){
						max = tmp;
						maxIndex = j;
					}
				}
				if(local.predType.equals("methodcall")){
					int tmp = Learner.MethodFrequency.get(local.getValues().get(1));
					if(max<tmp){
						max = tmp;
						maxIndex = j;
					}
				}
			}
			
			definitions.get(i).remove(maxIndex);
		}
		
		
		return definitions;
	}
	
	
	
}
