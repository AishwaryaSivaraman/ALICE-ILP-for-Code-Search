package extractor.info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PackageInfo {
	public static HashMap<String, ClassInfo> classInfoMap= new HashMap<String, ClassInfo>();
	public static HashMap<String, List<Integer>> lineInformationForPredicates = new HashMap<String, List<Integer>>();
	public static HashMap<String, VariableLineNumberMapping> lineInfoForVariables = new HashMap<String, VariableLineNumberMapping>();

	public static void readPackageInfo(){
		File currDir = new File(".");
	    String path = currDir.getAbsolutePath();
	    System.out.println(path);
	    System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
	    String fileLocation = "/home/whirlwind/workspace/packageinfo.txt";
		System.out.println(fileLocation);
		File file = new File(fileLocation);
	    
	    try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	           // process the line.
	        	String[] info = line.split("--");
	        	ClassInfo classInfo = new ClassInfo();
	        	classInfo.setClassName(info[0]);
//	        	classInfo.setMethods(new ArrayList<MethodInfo>());
	        	classInfo.setPath(info[1]);	        	
//	        	MethodInfo methodInfo = new MethodInfo(info[2],Integer.parseInt(info[3]));
//	        	classInfo.getMethods().add(methodInfo);
	        	classInfo.setPartialPath(info[2]);
//	        	if(PackageInfo.classInfoMap.containsKey(info[0])){
//	        		PackageInfo.classInfoMap.get(info[0]).getMethods().add(methodInfo);
//	        	} else{
        		PackageInfo.classInfoMap.put(info[0], classInfo);
//	        	}
	        	
	        	
	        }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void readLineInfo(){
		File currDir = new File(".");
	    String path = currDir.getAbsolutePath();
	    System.out.println(path);
	    System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
	    String fileLocation = "/home/whirlwind/workspace/lineInfo.txt";
		System.out.println(fileLocation);
		File file = new File(fileLocation);
	    
	    try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	           // process the line.
	        	String[] info = line.split("--");
	        	VariableLineNumberMapping mapping = new VariableLineNumberMapping();
	        	mapping.variableName = info[0];
	        	mapping.startPosition = Integer.parseInt(info[1]);
	        	mapping.startlineNumber = Integer.parseInt(info[2]);
	        	mapping.endLineNumber = Integer.parseInt(info[3]);
	        	mapping.length = Integer.parseInt(info[4]);
	        	
        		PackageInfo.lineInfoForVariables.put(info[0], mapping);
//	        	}	        		        	
	        }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
