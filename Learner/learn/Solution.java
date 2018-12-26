package learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import def.PredicateDef;
public class Solution {
	
	public HashMap<String, String> mapVartoSolution = new HashMap<String, String>();
	public List<PredicateDef> queryforSolution = new ArrayList<PredicateDef>();
	public String queryAsString;
	public List<PredicateDef> remainingPredicates = new ArrayList<PredicateDef>();
	public List<PredicateDef> originalSpecialisedPredicates = new ArrayList<PredicateDef>();
	public String substitutedQuery;
}
