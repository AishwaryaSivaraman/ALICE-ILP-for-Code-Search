package extractor.extract;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ASTMethodVisitor extends ASTVisitor{
	public String methodCall;
	
	public ASTMethodVisitor(){
		methodCall = null;
	}
	
	public boolean visit(MethodInvocation node){					
		methodCall = node.getName().getFullyQualifiedName().toLowerCase();
		return true;
	}
}
