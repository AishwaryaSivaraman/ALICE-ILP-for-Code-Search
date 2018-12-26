package extractor.extract;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import extractor.info.MethodInfo;


public class UTASTFactGeneratorVisitor extends ASTVisitor{	
	public StringBuilder builder = new StringBuilder();
	public List<String> predicatesForSelection = new ArrayList<>();
	public String currentMethod = null;
	public String className = null;
	public List<MethodInfo> methods = new ArrayList<MethodInfo>();
	HashMap<String, String> variableTypes = new HashMap<>();
	CompilationUnit unit;
	
	
	
	public CompilationUnit getUnit() {
		return unit;
	}


	public void setUnit(CompilationUnit unit) {
		this.unit = unit;
	}


	public HashMap<String, String> getVariableTypes() {
		return variableTypes;
	}


	public void setVariableTypes(HashMap<String, String> variableTypes) {
		this.variableTypes = variableTypes;
	}


	public boolean visit(TypeDeclaration node){
//		this.className = node.getName().toString().toLowerCase();
		return true;
	}
	

	public boolean visit(){
		return true;
	}
	
	public boolean visit(FieldAccess node){		
		return true;
	}
	
	public boolean visit(SimpleName node){	
		if(this.currentMethod != null){
			String name = node.getIdentifier().toString();
			if(variableTypes.get(name)!= null){
				builder.append("containstype(");
				builder.append(this.currentMethod.toLowerCase());
				builder.append(",");
				
				builder.append(name.toLowerCase());
				builder.append(",");	
				String replacingBrackets = variableTypes.get(name).toLowerCase();
				if(replacingBrackets.contains("[")){
					replacingBrackets = replacingBrackets.replaceAll("[\\[\\]]", "");					
				}
				
				int count = 0;
				if(Extractor.typeFrequency.containsKey(replacingBrackets)){
					count = Extractor.typeFrequency.get(replacingBrackets);					
				}
				Extractor.typeFrequency.put(replacingBrackets, ++count);
				
				builder.append(replacingBrackets.replaceAll("\\.", ""));
				builder.append(").");
//				builder.append("\n");
				predicatesForSelection.add(builder.toString().toLowerCase());
				builder = new StringBuilder();
			}
		}
		return true;
	}
	
	public void endVisit(VariableDeclarationStatement node){
//		callStack.add("EndVariableDeclaration");		
	}
	
	public boolean visit(IfStatement node){		
		if(this.currentMethod!=null){
		builder.append("containsif(");
		builder.append(this.currentMethod.toLowerCase());
		builder.append(").");
//		builder.append("\n");
		predicatesForSelection.add(builder.toString().toLowerCase());
		builder = new StringBuilder();
		}
		return true;
	}	
	
	public boolean visit(MethodInvocation node){
		if(this.currentMethod != null){
			builder.append("methodcall(");
			builder.append(this.currentMethod.toLowerCase());
			builder.append(",");
			
			builder.append(node.getName().getFullyQualifiedName().toLowerCase());	
			builder.append(").");
//			builder.append("\n");
			String key = node.getName().getFullyQualifiedName().toLowerCase().replaceAll("_", "");
			int count = 0;
			if(Extractor.callFrequency.containsKey(key)){
				count = Extractor.callFrequency.get(key);					
			}
			Extractor.callFrequency.put(key, ++count);
						
			predicatesForSelection.add(builder.toString().toLowerCase());
			builder = new StringBuilder();
		}
		
		return true;
	}	
		
	public boolean visit(Assignment node){
		//maybe useful later to add what type of assignment
					
		return true;
	}
		
	public boolean visit(WhileStatement node){
		if(this.currentMethod!=null){
		builder.append("containsiterator(");
		builder.append(this.currentMethod.toLowerCase());
		builder.append(").");
//		builder.append("\n");
		predicatesForSelection.add(builder.toString().toLowerCase());
		builder = new StringBuilder();
		}
		return true;
	}
	
	public boolean visit(ForStatement node){			
		if(this.currentMethod != null){
		builder.append("containsiterator(");
		builder.append(this.currentMethod.toLowerCase());
		builder.append(").");
		predicatesForSelection.add(builder.toString().toLowerCase());
		builder = new StringBuilder();
		}
		return true;
	}
	
	
	public boolean visit(TryStatement node){
		return true;
	}
		
	public boolean visit(CatchClause node){
		if(this.currentMethod!=null){
		builder.append("catch(");
		builder.append(this.currentMethod.toLowerCase());
		builder.append(",");
		builder.append(node.getException().getType().toString().toLowerCase());
		builder.append(").");
		predicatesForSelection.add(builder.toString().toLowerCase());
		builder = new StringBuilder();
		}
		return true;
	}
	
	public boolean visit(MethodDeclaration node){		
		MethodInfo info = new MethodInfo(node.getName().getFullyQualifiedName().toLowerCase(),(unit.getLineNumber(node.getStartPosition())-1));
		System.out.println("Fullyqualified name is "+node.getName().getFullyQualifiedName());
		this.methods.add(info);
		this.currentMethod = this.className+":"+node.getName().getFullyQualifiedName().toLowerCase();
		return true;
	}
	
	public void endVisit(MethodDeclaration node){
		this.currentMethod = null;
	}
}
