package alice.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import learn.Solution;

import org.apache.commons.lang3.StringUtils;

import def.JavaSourceCodeInfo;
import def.PredicateDef;
import extractor.info.ClassInfo;
import extractor.info.VariableLineNumberMapping;

public class ExampleFromPrologResults {

	public static List<JavaSourceCodeInfo> populatecodeInfo(HashMap<String, ClassInfo> classInfoMap,
			HashMap<String, VariableLineNumberMapping> lineInfoForVariables,
			HashMap<Integer,HashMap<String, String>> solutions, 
			HashMap<Integer,Solution> solutionObjects){
		
		List<JavaSourceCodeInfo> sourceCodeInfo = new ArrayList<JavaSourceCodeInfo>();
		
		for(Integer iter: solutionObjects.keySet()){
			HashMap<String, String> currentSolution = solutionObjects.get(iter).mapVartoSolution;
			boolean foundClass = false;			
			JavaSourceCodeInfo info = new JavaSourceCodeInfo();
			int minStartLine = 100000;
			int maxEndLine = 0;
			for(String key : currentSolution.keySet()){
				if(currentSolution.get(key).contains(":")){
//					String className = StringUtils.substringBetween(currentSolution.get(key),"(", ")");
//					className = className.replace(",", ":");
					String className = currentSolution.get(key);
					className = className.replaceAll("\\s", "");
					if(classInfoMap.containsKey(className)){
						info.examplePath = classInfoMap.get(className).getPartialPath();
						info.className = classInfoMap.get(className).getClassName();
						info.methodName = className.split(":")[1];
					}
				} else{
					String variablename = currentSolution.get(key);
					if(lineInfoForVariables.containsKey(variablename)){
						if(lineInfoForVariables.get(variablename).startlineNumber<minStartLine){
							minStartLine = lineInfoForVariables.get(variablename).startlineNumber;
						}
						if(lineInfoForVariables.get(variablename).startlineNumber>maxEndLine){
							maxEndLine = lineInfoForVariables.get(variablename).endLineNumber;
						}
					}
				}
			}
			info.startLineNumber = minStartLine;
			info.endLineNumber = maxEndLine;
			info.query = solutionObjects.get(iter).queryAsString;
			info.queryasPredicates.addAll(solutionObjects.get(iter).queryforSolution);
			info.remainingPredicates.addAll(solutionObjects.get(iter).remainingPredicates);
			info.originalClause.addAll(solutionObjects.get(iter).originalSpecialisedPredicates);
			sourceCodeInfo.add(info);
		}
		return sourceCodeInfo;				
	}				
	
}
