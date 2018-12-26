package learn;

import java.util.ArrayList;
import java.util.List;

import def.PredicateDef;

public class Variabilize {

	public String prologVariable = "X";
	public String prologDontCare = "_";
	public List<String> listOfVars;
	
	public List<PredicateDef> variablize(List<PredicateDef> predicates){
			
		List<PredicateDef> modifiedPredicates = new ArrayList<PredicateDef>();
		
		for(PredicateDef def: predicates){
			try{
				def.values.set(0, prologVariable);
				if(def.predType.equals("containstype")){
					def.values.set(1, prologDontCare);
				}
				modifiedPredicates.add(def);
			} catch(Exception e){
				System.out.println("Null Pointer at ");				
			}
		}
		
		return modifiedPredicates;
	}
	
	public List<PredicateDef> variablizePredicates(List<PredicateDef> predicates){
		List<PredicateDef> variblizedPredicates = new ArrayList<PredicateDef>();
		for(int i=0;i<predicates.size();i++){
			if(!predicates.get(i).predType.equals("contains") && !predicates.get(i).predType.equals("after")){
				predicates.get(i).values.set(0, predicates.get(i).values.get(0).toUpperCase());
				variblizedPredicates.add(predicates.get(i));
			}
		}
		return variblizedPredicates;
	}
	
	public List<PredicateDef> variablizeRelational(List<PredicateDef> predicates){
		listOfVars = new ArrayList<String>();
		String methodName ="";
		for(int i=0;i<predicates.size();i++){
			if(predicates.get(i).predType.equals("contains") || predicates.get(i).predType.equals("contains")){
				String variable = predicates.get(i).values.get(0).toUpperCase().replace(":", "_");
				predicates.get(i).values.set(0, variable);
				variable = predicates.get(i).values.get(1).toUpperCase().replace(":", "_");
				predicates.get(i).values.set(1, variable);
			} else{
				String variable = predicates.get(i).values.get(0).toUpperCase().replace(":", "_");
				predicates.get(i).values.set(0, variable);
				listOfVars.add(variable);
			}
			if(predicates.get(i).predType.equals("methoddec")){
//				methodName = predicates.get(i).getValues().get(0);
				methodName = "X";
				predicates.get(i).getValues().set(0, methodName);				
			}
			if(predicates.get(i).predType.equals("contains")){
				predicates.get(i).predType = "contained";
				predicates.get(i).values.set(0, methodName);
			}
		}
		return predicates;
	}	
}
