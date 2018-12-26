package learn;

import java.util.ArrayList;
import java.util.List;

import def.PredicateDef;

public class RandomRefinement {

	public List<PredicateDef> pickPredicates(List<PredicateDef> predicates){
		List<PredicateDef> pickedPredicates = new ArrayList<PredicateDef>();
		int index = (int) (Math.random()*(predicates.size()));
		
		PredicateDef def = new PredicateDef();
		def.predType = "contained";
		def.values = new ArrayList<String>();
		def.values.add("X");
		def.values.add(predicates.get(index).values.get(0));
		pickedPredicates.add(def);
		pickedPredicates.add(predicates.get(index));
		predicates.remove(index);
		return pickedPredicates;
	}
}
