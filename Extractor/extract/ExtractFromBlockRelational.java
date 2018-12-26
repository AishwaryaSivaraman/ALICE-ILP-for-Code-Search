package extractor.extract;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import extractor.info.PackageInfo;
import extractor.info.SelectionInfo;

public class ExtractFromBlockRelational {

	public ASTFactExtractor blockParser = new ASTFactExtractor();
	public ASTParserBlock visitor = new ASTParserBlock();
	
	public SelectedCode selection = new SelectedCode();
	public SelectionInfo info;
	
	public String packageFileName;
	
	public void generatePredicate(String selection){
		UTASTParser searchParser = new UTASTParser();
		CompilationUnit cu = searchParser.parseBlock(selection);
		cu.accept(this.visitor);
        	        	       
//        System.out.println("The query is ");
//        System.out.println(getQueryString(this.visitor.predicatesForSelection));   
                
        
//        System.out.println("The size of String s "+this.visitor.predicatesForSelection.size());
//        System.out.println("The size of PredObject s "+this.visitor.predicateDefiDefs.size());
//        System.out.println("The size of Generalised query s "+this.visitor.generalisedPredicates.size());

        this.selection.predicates = this.visitor.predicateDefiDefs;
        this.selection.predicatesAsString = this.visitor.predicatesForSelection; 
        this.info = info;

	}
	
	public void extractBlock(SelectionInfo info){		
		packageFileName = getfactStringForFile(info.file.getRawLocation().toOSString());
		
		System.out.println("File "+info.file.getName());
		getCoveringMethod(info.file,info.file.getRawLocation().toOSString(), info.startIndex, info.length);
        
        generateTypesForCurrentFile(info.file.getRawLocation().toOSString());
        this.visitor.className = selection.className;
        this.visitor.methodName = selection.methodName;
        this.visitor.addDot = false;
        this.visitor.combineClassAndMethodName();        
    	this.visitor.addMethodDecForBlock();
    	
    	generatePredicate(info.selection);
    	
        PackageInfo.readPackageInfo();
        PackageInfo.readLineInfo();
	}
	
	public String getfactStringForFile(String filePath){
		int count=0;
		for(String packageName: Extractor.packageToFilesMap.keySet()){
			count = count+1;
			if(filePath.startsWith(packageName)){
				return "factsFromExtractor"+count;
			}
		}
		return null;
	}
	public String getQueryString(List<String> queryList){
		StringBuilder builder  = new StringBuilder();
		int i=0;
		for(i=0;i<queryList.size()-1;i++){
			builder.append(queryList.get(i));
			builder.append(",");
		}
		builder.append(queryList.get(i));	
		return builder.toString();
	}
	
	public void getCoveringMethod(IFile file,String path,final int start,int length){
		UTASTParser parser = new UTASTParser();
		final CompilationUnit unit = parser.searchParse(UTFile.getContents(path));		
		unit.accept(new ASTVisitor() {			
			public boolean visit(MethodDeclaration node){
				int lineNumber = unit.getLineNumber(node.getStartPosition());					
				if(lineNumber<=start){
					selection.methodName = node.getName().getFullyQualifiedName();
				}
				return true;
			}
			
			public boolean visit(TypeDeclaration node){
				selection.className = node.getName().getFullyQualifiedName();
				return true;
			}
		});	
	}
	
	public void generateTypesForCurrentFile(String path){
		UTASTParser parser = new UTASTParser();		
		final CompilationUnit unit = parser.searchParse(UTFile.getContents(path));
		
		UTASTSearchTypeVisitor typesOFClass = new UTASTSearchTypeVisitor();
		typesOFClass = buildTypes(unit);								
		this.visitor.types = typesOFClass.variableTypes;
//		this.visitor.setVariableTypes(typesOFClass.variableTypes);
	}
	
	
	public UTASTSearchTypeVisitor buildTypes(CompilationUnit unit){				
		UTASTSearchTypeVisitor searchTypesVisitor = new UTASTSearchTypeVisitor();
		unit.accept(searchTypesVisitor);
		return searchTypesVisitor;
	}

}
