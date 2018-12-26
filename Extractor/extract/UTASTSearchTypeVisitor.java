package extractor.extract;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class UTASTSearchTypeVisitor extends ASTVisitor{
	
	public  HashMap<String,String> variableTypes = new HashMap<>();
	public  HashMap<String,String> methodTypes = new HashMap<>();
	String[] primitiveTypes = new String[]{"int","boolean","double","long","float","String","Double","Integer","char"};
	
	String currentVar;
	String currentType;
	//variable declarations
	//function return types
	//current class type
	//
	//contextual type in If,While,etc	
	
	public String returnNormalizedType(Type node){
		String type = null;
		if(!node.isPrimitiveType()){
			if(node.isArrayType()){	
				ArrayType arrayType = (ArrayType) node;				
				type = arrayType.getElementType().toString()+"array";
			}else{
				if(node.isQualifiedType()){
					QualifiedType qType = (QualifiedType) node;
					type = qType.getName().toString();
//					type = node.toString();
				} else{
					type = node.toString();
				}
			}
			type.replaceAll("\\.", "").toLowerCase();
		}		
		return type;
	}
	
	public boolean visit(SingleVariableDeclaration node) {		
		String type = returnNormalizedType(node.getType());
		if(type != null){
			variableTypes.put(node.getName().toString(), type);
		}
		return true;
	} 
	
	public boolean visit(FieldDeclaration node){			
//		currentType = node.getType().toString();
		currentType = returnNormalizedType(node.getType());
		return true;
	}
	
	public void endVisit(FieldDeclaration node){
		currentType = null;
	}
	
	public boolean visit(VariableDeclarationExpression node){		
//		currentType = node.getType().toString();
		currentType = returnNormalizedType(node.getType());
		return true;
	}
	
	public void endVisit(VariableDeclarationExpression node){
		currentType = null;
	}
	
	public boolean visit(VariableDeclarationFragment node){
		currentVar = node.getName().toString();
		if(currentType!=null && currentType.length()>1){
				variableTypes.put(currentVar, currentType);
		}				
		return true;
	}
	
	public boolean visit(VariableDeclarationStatement node){
//		currentType = node.getType().toString();	
		currentType = returnNormalizedType(node.getType());
		return true;
	}
	public void endVisit(VariableDeclarationStatement node){
		currentType = null;			
	}
	
	public boolean visit(MethodDeclaration node){
		currentVar = node.getName().toString();
		return true;
	}
}