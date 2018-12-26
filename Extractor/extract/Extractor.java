package extractor.extract;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragment;

import extractor.info.ClassInfo;
import extractor.info.MethodInfo;
import extractor.info.PackageInfo;

public class Extractor{
	
	List<IResource> allJavaFiles = new ArrayList<IResource>();
	HashMap<String, ClassInfo> classInfoMap= new HashMap<String, ClassInfo>();
	HashMap<String, List<Integer>> lineInformationForPredicates = new HashMap<String, List<Integer>>();
	public static HashMap<String, Integer>  typeFrequency = new HashMap<String, Integer>();
	public static HashMap<String, Integer>  callFrequency = new HashMap<String, Integer>();
	public static HashMap<String, List<IResource>> packageToFilesMap = new HashMap<String, List<IResource>>();
	
	
	public void Extract(IProject project, IPath path, IWorkspaceRoot iWorkspaceRoot){		
		getAllFiles(path, iWorkspaceRoot);
		convertAllJavaFilesToFacts(project.getLocationURI().getRawPath().toString());
//		writeToFilesInBatch(project.getLocationURI().getRawPath().toString());
		
//		System.out.println("The path outside "+path.toOSString());
		generateMapOfPackageToFiles(path,getPackages(project),iWorkspaceRoot);
//		writeToFilesInBatch(project.getLocationURI().getRawPath().toString());
    	writePackageInfoToFile(project.getLocationURI().getRawPath().toString());
    	writePackageLineInfoToFile(project.getLocationURI().getRawPath().toString());
	}	

	public UTASTSearchTypeVisitor buildTypes(CompilationUnit unit){				
		UTASTSearchTypeVisitor searchTypesVisitor = new UTASTSearchTypeVisitor();
		unit.accept(searchTypesVisitor);
		return searchTypesVisitor;
	}
	
	public void writePackageInfoToFile(String projectPath){
		BufferedWriter output = null;
		String fileLocation = projectPath+"/packageinfo.txt";
		fileLocation = "/home/whirlwind/workspace/packageinfo.txt";
		File file = new File(fileLocation);
		try {
			output = new BufferedWriter(new FileWriter(file));
			StringBuilder builder = new StringBuilder();
			for(String key : PackageInfo.classInfoMap.keySet()){
				String tmp = key+"--"+PackageInfo.classInfoMap.get(key).getPath()+"--"+PackageInfo.classInfoMap.get(key).getPartialPath();
				
//				List<MethodInfo> methods = PackageInfo.classInfoMap.get(key).getMethods();
//				for(int i=0;i<methods.size();i++){
//					String tmp2 = ":"+methods.get(i).getMethodName()+":"+ methods.get(i).getStartLineNumber();
//					builder.append(tmp+tmp2);
//					tmp2 = ":"+PackageInfo.classInfoMap.get(key).getPartialPath();
//					builder.append(tmp2);
//					builder.append("\n");	
//				}	
				builder.append(tmp);
				builder.append("\n");
			}
			output.write(builder.toString());
			output.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void writePackageLineInfoToFile(String projectPath){
		BufferedWriter output = null;
		String fileLocation = projectPath+"/packageinfo.txt";
		fileLocation = "/home/whirlwind/workspace/lineInfo.txt";
		File file = new File(fileLocation);
		try {
			output = new BufferedWriter(new FileWriter(file));
			StringBuilder builder = new StringBuilder();
			for(String key : PackageInfo.lineInformationForPredicates.keySet()){
				String tmp = key;
				//+"--";
				//+PackageInfo.lineInformationForPredicates.get(key).getPath()+"--"+PackageInfo.classInfoMap.get(key).getPartialPath();
				for(int i=0;i<PackageInfo.lineInformationForPredicates.get(key).size();i++){
					tmp = tmp +"--"+PackageInfo.lineInformationForPredicates.get(key).get(i);
				}
				
//				List<MethodInfo> methods = PackageInfo.classInfoMap.get(key).getMethods();
//				for(int i=0;i<methods.size();i++){
//					String tmp2 = ":"+methods.get(i).getMethodName()+":"+ methods.get(i).getStartLineNumber();
//					builder.append(tmp+tmp2);
//					tmp2 = ":"+PackageInfo.classInfoMap.get(key).getPartialPath();
//					builder.append(tmp2);
//					builder.append("\n");	
//				}	
				builder.append(tmp);
				builder.append("\n");
			}
			output.write(builder.toString());
			output.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void convertAllJavaFilesToFacts(String projectPath){
		BufferedWriter output = null;
		String fileLocation = projectPath+"/factsFromExtractor.pl";
		fileLocation = "/home/whirlwind/workspace/factsFromExtractor.pl";
		String userDir = System.getProperty("user.dir");
		fileLocation = userDir+"/factbase.pl";
		File file = new File(fileLocation);
		try {
			output = new BufferedWriter(new FileWriter(file));
			StringBuilder builder = new StringBuilder();
//			output.write("iflike(X,Y) :- if(X,Z), Z =~ Y.");
//			output.write("\n");
//			output.write("iterlike(X,Y) :- iterator(X,Z), Z =~ Y.\n");
//			output.write("contained(X,Y) :-contains(X,Y).\n");
//			output.write("contained(X,Z) :- contains(X,Y),contained(Y,Z).\n");
			ASTFactExtractor visitor = new ASTFactExtractor();
			
			for(int i=0;i<allJavaFiles.size();i++){								
//				UTASTFactGeneratorVisitor visitor = new UTASTFactGeneratorVisitor();
				
				UTASTParser parser = new UTASTParser();
				IResource res = allJavaFiles.get(i);
				final CompilationUnit unit = parser.searchParse(UTFile.getContents(res.getRawLocation().toOSString()));
//				visitor.setUnit(unit);
				String nameOFPath = res.getProjectRelativePath().removeFileExtension().toString().replaceAll("/", "").replaceAll(" ", "");
				if(res.getFullPath().toString().contains("GC")){
				}
				visitor.className = nameOFPath;
				visitor.allMethodNames.clear();
				
				UTASTSearchTypeVisitor typesOFClass = new UTASTSearchTypeVisitor();
				typesOFClass = buildTypes(unit);								
				
				visitor.types = typesOFClass.variableTypes;
				visitor.unit = unit;
				
				unit.accept(visitor);				
				
				for(int j =0;j<visitor.allMethodNames.size();j++){
				ClassInfo info = new ClassInfo();
				info.setClassName(visitor.className);
//				info.setMethods(visitor.methods);
				info.setPartialPath(res.getFullPath().toOSString());
				info.setPath(res.getRawLocation().toOSString());
//				if(visitor.combinedName != null){
//					info.combinedName = visitor.combinedName.toLowerCase();				
//				} else{
//					System.out.println("Method Name is "+visitor.methodName);
//					System.out.println("Class Name is "+visitor.className);
//					info.combinedName = visitor.combinedName;
//				}
				info.combinedName = visitor.allMethodNames.get(j);
//				this.classInfoMap.put(visitor.className, info);
				this.classInfoMap.put(info.combinedName, info);				
				}
				
				this.lineInformationForPredicates.putAll(visitor.mapLineNumbersToPredicats);				
				HashSet<String> set = new HashSet<>();
				set.addAll(visitor.predicatesForSelection);
				Iterator<String> it = set.iterator();
				while(it.hasNext()){
					String tmp = it.next();
					tmp = tmp.replaceAll("_", "");
					builder.append(tmp);
					builder.append("\n");
				}
				output.write(builder.toString());
				builder = new StringBuilder();
				visitor.predicatesForSelection.clear();
				visitor.variableStack.clear();
			}			
			System.out.println("Done Writing");
			PackageInfo.classInfoMap.clear();
			PackageInfo.lineInformationForPredicates.clear();
			PackageInfo.lineInformationForPredicates.putAll(lineInformationForPredicates);
			PackageInfo.classInfoMap.putAll(this.classInfoMap);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	
	
	public void getAllFiles(IPath path,IWorkspaceRoot iWorkspaceRoot){
		IContainer  container =  iWorkspaceRoot.getContainerForLocation(path);
		
		try{
			IResource[] iResources;
	        iResources = container.members();
	        for (IResource iR : iResources){
	            // for c files
	            if ("java".equalsIgnoreCase(iR.getFileExtension())){
	            	allJavaFiles.add(iR);
//	            	String className = iR.getName().split("\\.")[0];
//	            	classFilepathMap.put(className.toLowerCase(), iR.getRawLocation().toOSString());

	            }	            	
	            if (iR.getType() == IResource.FOLDER){
	                IPath tempPath = iR.getLocation();
	                getAllFiles(tempPath,iWorkspaceRoot);
	            }
	        }
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public List<IPath> getPackages(IProject project) {
		List<IPath> listOfPaths = new ArrayList<IPath>();
		String previousPath =" ";
		try{
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment aPackage : packages) {
				if(!(aPackage instanceof JarPackageFragmentRoot)){
					try{
						if(aPackage.getResource().getFullPath().toOSString().contains(project.getName())){
							
							String currentPath = aPackage.getResource().getFullPath().toOSString();
							if(!currentPath.startsWith(previousPath)){						
								listOfPaths.add(aPackage.getResource().getFullPath());
								previousPath = currentPath;
							}
						}
					}catch(Exception e){
						
					}
				}
				
			}
		}catch (Exception e){
			
		}
		return listOfPaths;
		
	}
	
	public void generateMapOfPackageToFiles(IPath fullPath,List<IPath> packagePaths,IWorkspaceRoot iWorkspaceRoot){
		for(int i=0;i<packagePaths.size();i++){
			allJavaFiles.clear();
			IPath relativePath = packagePaths.get(i);
			String[] splitPath = packagePaths.get(i).toOSString().trim().split("/");
			String pathWithoutParentFolder = "";
			for(int j=2;j<splitPath.length;j++){
				pathWithoutParentFolder = pathWithoutParentFolder+"/"+splitPath[j];
			}
			IPath packageFullPath = new Path(fullPath.toOSString()+pathWithoutParentFolder);
			
			getAllFiles(packageFullPath, iWorkspaceRoot);
			List<IResource> files = new ArrayList<IResource>(allJavaFiles);			
			packageToFilesMap.put(packageFullPath.toOSString(), files);
		}
		
}
	
	public void writeToFilesInBatch(String path){
		Arrays.stream(new File("/home/whirlwind/workspace/facts/").listFiles()).forEach(File::delete);
		
		StringBuilder builder = new StringBuilder();			
		ASTFactExtractor visitor = new ASTFactExtractor();
		int count=0;
		for(String packageName: packageToFilesMap.keySet()){
			count = count+1;
			List<IResource> javaFilesInPackage = packageToFilesMap.get(packageName);
			for(int i=0;i<javaFilesInPackage.size();i++){												
				UTASTParser parser = new UTASTParser();
				IResource res = javaFilesInPackage.get(i);
				final CompilationUnit unit = parser.searchParse(UTFile.getContents(res.getRawLocation().toOSString()));
				String nameOFPath = res.getProjectRelativePath().removeFileExtension().toString().replaceAll("/", "").replaceAll(" ", "");			
				visitor.className = nameOFPath;
				visitor.allMethodNames.clear();
				visitor.unit = unit;
				unit.accept(visitor);
				for(int j =0;j<visitor.allMethodNames.size();j++){
					ClassInfo info = new ClassInfo();
					info.setClassName(visitor.className);
					info.setPartialPath(res.getFullPath().toOSString());
					info.setPath(res.getRawLocation().toOSString());
					info.combinedName = visitor.allMethodNames.get(j);
					this.classInfoMap.put(info.combinedName, info);				
				}
				
				this.lineInformationForPredicates.putAll(visitor.mapLineNumbersToPredicats);				
				HashSet<String> set = new HashSet<>();
				set.addAll(visitor.predicatesForSelection);
				Iterator<String> it = set.iterator();
				while(it.hasNext()){
					String tmp = it.next();
					tmp = tmp.replaceAll("_", "");
					builder.append(tmp);
					builder.append("\n");
				}
				
				visitor.predicatesForSelection.clear();
				visitor.variableStack.clear();
			}
			
				writeToFile(builder.toString(),count);
				builder = new StringBuilder();
		}						
		System.out.println("Done Writing");
		PackageInfo.classInfoMap.clear();
		PackageInfo.lineInformationForPredicates.clear();
		PackageInfo.lineInformationForPredicates.putAll(lineInformationForPredicates);
		PackageInfo.classInfoMap.putAll(this.classInfoMap);		
	}
	
	public void writeToFile(String content,int count){
		BufferedWriter output = null;		
		String fileLocation = "/home/whirlwind/workspace/facts/factsFromExtractor"+count+".pl";
		File file = new File(fileLocation);
		try {
			output = new BufferedWriter(new FileWriter(file));
			StringBuilder builder = new StringBuilder();
			output.write("iflike(X,Y) :- if(X,Z), Z =~ Y.");
			output.write("\n");
			output.write("iterlike(X,Y) :- iterator(X,Z), Z =~ Y.\n");
			output.write("contained(X,Y) :-contains(X,Y).\n");
			output.write("contained(X,Z) :- contains(X,Y),contained(Y,Z).\n");
			output.write(content);									
			System.out.println("Done Writing");			
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}