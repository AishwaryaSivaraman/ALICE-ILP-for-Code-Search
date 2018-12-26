package extractor.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.WhileStatement;

import def.PredicateDef;
import extractor.info.MethodInfo;

public class ASTFeatureExtractor extends ASTVisitor {
	
	List<String> candidatefeatures = new ArrayList<String>();
	ASTMethodVisitor methodVisitor = new ASTMethodVisitor();
	HashMap<String, String> types = new HashMap<String, String>();
	
	public String checkIfMethodCall(String expression){
		methodVisitor = new ASTMethodVisitor();
		UTASTParser searchParser = new UTASTParser();
		CompilationUnit cu = searchParser.parseBlock(expression);
		cu.accept(methodVisitor);
		return methodVisitor.methodCall;
	}
	
	public String removeSpecialChars(String input){
		String methodCall = checkIfMethodCall(input);		
			String expressionAsString = input.toLowerCase();
			expressionAsString = expressionAsString.replaceAll("\"", "");
	        expressionAsString = expressionAsString.replaceAll("\\s+", "");
			expressionAsString = expressionAsString.replaceAll("\\s", "");
			expressionAsString = expressionAsString.replaceAll(" ", "");
			expressionAsString = expressionAsString.replaceAll("_", "");			
			if(!candidatefeatures.contains(expressionAsString)){
				candidatefeatures.add(expressionAsString);
			}
			return expressionAsString;		
	}
	public boolean visit(IfStatement node){	
		removeSpecialChars(node.getExpression().toString());		
		return true;
	}
	
	
	public boolean visit(ForStatement node){		
		removeSpecialChars(node.getExpression().toString());
		return true;
	}
	
	
	public boolean visit(WhileStatement node){	
		removeSpecialChars(node.getExpression().toString());
		return true;
	}
			
	public boolean visit(MethodInvocation node){					
		String expression = node.getName().getFullyQualifiedName().toLowerCase();
		removeSpecialChars(expression);
		return true;
	}	
	
	public boolean visit(SimpleName node){
		String name = node.getIdentifier().toString();
		if(types.containsKey(name)){
			removeSpecialChars(types.get(name));
		}
		return true;
	}
}
