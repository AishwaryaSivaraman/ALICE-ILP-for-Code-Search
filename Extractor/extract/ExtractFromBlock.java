package extractor.extract;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import extractor.info.SelectionInfo;

public class ExtractFromBlock {
	
	public ASTQueryGeneratorVisitor visitor = new ASTQueryGeneratorVisitor();
	public SelectedCode selection = new SelectedCode();
	
	
	public void extractBlock(SelectionInfo info){
		
		System.out.println("File "+info.file.getName());
		getCoveringMethod(info.file,info.file.getRawLocation().toOSString(), info.startIndex, info.length);
        
        generateTypesForCurrentFile(info.file.getRawLocation().toOSString());
		this.visitor.setClassName(selection.className);
        this.visitor.setMethodName(selection.methodName);
        this.visitor.combineClassAndMethodName();
        
        UTASTParser searchParser = new UTASTParser();
		CompilationUnit cu = searchParser.parseBlock(info.selection);
		cu.accept(this.visitor);        	        	       
        System.out.println("The query is ");
        System.out.println(getQueryString(this.visitor.predicatesForMethod));                
        this.selection.predicates = this.visitor.predicates;
        this.selection.predicatesAsString = this.visitor.predicatesForMethod;        
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
		
		this.visitor.setVariableTypes(typesOFClass.variableTypes);
	}
	
	
	public UTASTSearchTypeVisitor buildTypes(CompilationUnit unit){				
		UTASTSearchTypeVisitor searchTypesVisitor = new UTASTSearchTypeVisitor();
		unit.accept(searchTypesVisitor);
		return searchTypesVisitor;
	}

}


