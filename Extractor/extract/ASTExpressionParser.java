package extractor.extract;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

public class ASTExpressionParser extends ASTVisitor{

	public List<String> simpleNames;
	boolean methodCall = false;
	boolean isCast = true;
	List<String> methodCalls = new ArrayList<String>();
	List<String> toignore = new ArrayList<String>();
	
	public ASTExpressionParser() {
		toignore.add("java");
		toignore.add("iterator");
		toignore.add("util");
		toignore.add("list");
		toignore.add("record");
		toignore.add("string");
		
		simpleNames = new ArrayList<String>();
	}
	
	public boolean visit(SimpleName node){
//		System.out.println("Simple Name is "+node.getIdentifier().toString());
		String name = node.getIdentifier().toString().toLowerCase();
		if(!methodCalls.contains(name) && !toignore.contains(name)){
			simpleNames.add(name);
		}
	    return true;
	}
	
	public boolean visit(CastExpression node){
		return true;
	}

	public boolean visit(MethodInvocation node){
		methodCall = true;
		methodCalls.add(node.getName().toString());
		return true;
	}
	
	public void endVisit(MethodInvocation node){
		methodCall = false;
	}
	
}
