package learn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import def.PredicateDef;

public class Combinations {

	public BigInteger noOfcombinations(int total, int choice){
		BigInteger ret = BigInteger.ONE;
		for(int k=0;k<choice;k++){
			ret = ret.multiply(BigInteger.valueOf(total-k)).divide(BigInteger.valueOf(k+1));
		}		
		return ret;		
	}
	
	void combinationsAsArray(List<List<PredicateDef>> chosenPredicates, List<PredicateDef> predicates, List<PredicateDef> child, int start, int end,
            int index, int r) 
	{
	 if (index == r) 
	 {
		 List<PredicateDef> newChild = new ArrayList<PredicateDef>();
	     for (int j = 0; j < r; j++){
//	         System.out.print(child.get(j).getStringPredicate() + ",");
	         newChild.add(child.get(j));
	     }
//	     System.out.println();
	     
	     chosenPredicates.add(newChild);	 
	     return;	     
	 }
	
	 for (int i = start; i <= end && ((end - i + 1) >= (r - index)); i++) 
	 {
	 	child.add(index,predicates.get(i));
	     combinationsAsArray(chosenPredicates,predicates, child, i + 1, end, index + 1, r);
	 }	
	}
		
}
