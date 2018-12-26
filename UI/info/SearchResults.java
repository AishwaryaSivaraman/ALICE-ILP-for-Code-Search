package alice.info;

import java.util.ArrayList;
import java.util.List;

import def.JavaSourceCodeInfo;

public class SearchResults {
	public static List<JavaSourceCodeInfo> solutions = new ArrayList<JavaSourceCodeInfo>();
	public static int[] exampleType;
	
	public static void initExampleType(){
		exampleType = new int[solutions.size()];
	}
	
	public static void clear(){
		solutions.clear();
		exampleType = new int[solutions.size()];
	}
	
	public static int exampleTypeAtIndex(int index){
		return exampleType[index];
	}
	
	public static void setOldExamples(int noOfNew){
		for(int i=0;i<solutions.size();i++){
			solutions.get(i).isOld = true;
		}
		
		int[] exampleTypeCopy = new int[exampleType.length];
		System.arraycopy(exampleType, 0, exampleTypeCopy, 0, exampleType.length);
		exampleType = new int[exampleTypeCopy.length+noOfNew];	
//		for(int i=noOfNew-1,j=0;i<exampleType.length;i++,j++){
//			exampleType[i] = exampleTypeCopy[j];
//		}
		System.arraycopy(exampleTypeCopy, 0, exampleType, noOfNew, exampleTypeCopy.length);
	}
}
