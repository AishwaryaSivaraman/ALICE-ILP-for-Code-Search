package extractor.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
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

public class ASTFactExtractor extends ASTVisitor {
	
	public StringBuilder builder = new StringBuilder();
	public String currentMethod = null;
	public String className = null;
	public String lastVariable = null;
	public String methodName = null;
	public boolean addDot = true;
	public String combinedName = null;
	public CompilationUnit unit;
	
	public HashMap<String, String> types = new HashMap<String, String>();
	public List<String> predicatesForSelection = new ArrayList<>();
	Stack<String> variableStack = new Stack<String>();
	public List<PredicateDef> predicateDefiDefs = new ArrayList<PredicateDef>();
	public HashMap<String, List<Integer>> mapLineNumbersToPredicats = new HashMap<String, List<Integer>>();
	public List<String> allMethodNames = new ArrayList<String>();
	public List<PredicateDef> containsList = new ArrayList<PredicateDef>();
	public List<PredicateDef> afterList = new ArrayList<PredicateDef>();
	
	public String previous = null;
	int ifCount;
	int whileCount;
	int forCount;
	int methodCallCount;
	int catchTypeCount;
	int typeCount;
	
	
	//Todo: 
	//make a stack of last in so that you can get back 
	
	public ASTFactExtractor() {
		int ifCount = 0;
		int whileCount =0;
		int forCount =0;
		int methodCallCount = 0;
		 
	}
	
	
	public boolean visit(MethodDeclaration node){
//		System.out.println("Fullyqualified name is "+node.getName().getFullyQualifiedName());
	    this.currentMethod = this.className+":"+node.getName().getFullyQualifiedName().toLowerCase();
		allMethodNames.add(currentMethod.toLowerCase());
	    addMethodDecForBlock();
		addMapInfoForLineNumbers(this.currentMethod.toLowerCase(), 
				node.getStartPosition(),
				unit.getLineNumber(node.getStartPosition()),
				unit.getLineNumber(node.getStartPosition()+node.getLength()),
				node.getLength());
		return true;
	}
	
	public void addMethodDecForBlock(){		
		String predicate = "methoddec("+this.currentMethod+")";//.";
		if(addDot){
			predicate = predicate +".";
		}
		predicatesForSelection.add(predicate.toLowerCase());
		
		List<String> val = new ArrayList<String>();
		val.add(this.currentMethod.toLowerCase());
		addPredicateDef("methoddec", val);
		
		lastVariable = currentMethod;
		variableStack.push(currentMethod);
	}
	
	public void endVisit(MethodDeclaration node){
		
		this.combinedName = this.currentMethod;
		this.currentMethod = null;
		variableStack.pop();
	}
	
	public void addPredicateDef(String predType, List<String> values){
		//define preddef
		PredicateDef def = new PredicateDef();
		def.predType = predType;
		List<String> vals = new ArrayList<String>();
		vals.addAll(values);
		def.values = values;	
		predicateDefiDefs.add(def);
		
	}
	
	public void generaliseConditionals(Expression expr, String predType){		
		ASTExpressionParser expParser = new ASTExpressionParser();
		expr.accept(expParser);
	}
	
	public void addMapInfoForLineNumbers(String varName, int startPosition, int startLineNumber, int endLineNumber,int length){
		String var = varName.replaceAll("_", "");
		mapLineNumbersToPredicats.put(var, new ArrayList<Integer>());
		mapLineNumbersToPredicats.get(var).add(startPosition);
		mapLineNumbersToPredicats.get(var).add(startLineNumber);
		mapLineNumbersToPredicats.get(var).add(endLineNumber);
		mapLineNumbersToPredicats.get(var).add(length);				
	}
	
	public boolean visit(IfStatement node){		
		if(currentMethod != null){
		    String variable = "if_"+ifCount++; 
		    String expression =  "\""+ node.getExpression().toString().replaceAll("\"","")+"\"";
		    expression = expression.replaceAll("\\s+", "");
			expression = expression.replaceAll("\\s", "");
			expression = expression.replaceAll(" ", "");
		    
		    String ifPredicate = "if("+variable+","+expression+")";//.";
		    if(addDot){
		    	ifPredicate = ifPredicate +".";
			}
			predicatesForSelection.add(ifPredicate.toLowerCase());
			
			addMapInfoForLineNumbers(variable.toLowerCase(), 
					node.getStartPosition(),
					unit.getLineNumber(node.getStartPosition()), 
					unit.getLineNumber(node.getStartPosition()+node.getLength()),
					node.getLength());
									
			List<String> vals = new ArrayList<String>();
			vals.add(variable.toLowerCase());
			vals.add(expression.toLowerCase());
			addPredicateDef("if", vals);
			
			String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
			 if(addDot){
				 containsPredicate = containsPredicate +".";
			}
			predicatesForSelection.add(containsPredicate.toLowerCase());
			
			vals = new ArrayList<String>();
			vals.add(lastVariable);
			vals.add(variable);
			addPredicateDef("contains", vals);
			
			addAfterPredicate(variable);
			previous = variable;
			
			lastVariable = variable;
			variableStack.push(lastVariable);
			builder = new StringBuilder();
		}
		return true;
	}
	
	public void addAfterPredicate(String currentVariable){
//		previous = predicateDefiDefs.get(predicateDefiDefs.size()-1).values.get(0);
		if(previous != null){
			String afterPredicate = "after("+currentVariable+","+previous+")";//.";
			 if(addDot){
				 afterPredicate = afterPredicate +".";
			}
			predicatesForSelection.add(afterPredicate.toLowerCase());
			
			List<String> vals = new ArrayList<String>();
			vals.add(currentVariable);
			vals.add(previous);
			addPredicateDef("after", vals);
		} else{
			System.out.println("Previous was null");
		}
	}
	
	public void endVisit(IfStatement node){
		if(currentMethod != null){
			if(variableStack.size()>1){
			variableStack.pop();
			lastVariable = variableStack.peek();
			}
		}
	}
	
	public boolean visit(ForStatement node){
		try{
		if(currentMethod != null){
		String variable = "iterator_"+whileCount++;
//		System.out.println("The for excep is "+node.getExpression().toString().replaceAll("'", ""));
		String expression = "\""+ node.getExpression().toString().replaceAll("\"","")+"\"";
		expression = expression.replaceAll("\\s+", "");
		expression = expression.replaceAll("\\s", "");
		expression = expression.replaceAll(" ", "");
		
		String loopPredicate = "iterator("+variable+","+expression+")";//.";
		 if(addDot){
			 loopPredicate = loopPredicate +".";
		}
		predicatesForSelection.add(loopPredicate.toLowerCase());
		addMapInfoForLineNumbers(variable.toLowerCase(), 
				node.getStartPosition(),
				unit.getLineNumber(node.getStartPosition()), 
				unit.getLineNumber(node.getStartPosition()+node.getLength()),
				node.getLength());
		
		List<String> vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expression.toLowerCase());
		addPredicateDef("iterator", vals);
		
		
		String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
		 if(addDot){
			 containsPredicate = containsPredicate +".";
		}
		predicatesForSelection.add(containsPredicate.toLowerCase());
		
		vals = new ArrayList<String>();
		vals.add(lastVariable);
		vals.add(variable);
		addPredicateDef("contains", vals);
		
		
		addAfterPredicate(variable);
		previous = variable;
		lastVariable = variable;
		variableStack.push(lastVariable);
		}
		}catch(Exception e){
			System.out.println(e);
		}
		return true;
	}
	
	public void endVisit(ForStatement node){
		if(currentMethod != null){
			if(variableStack.size()>1){
			variableStack.pop();
			lastVariable = variableStack.peek();
			}
		}
	}
	
	public boolean visit(WhileStatement node){	
		if(currentMethod != null){
		String variable = "iterator_"+whileCount++;
		String expression = "\""+ node.getExpression().toString().replaceAll("\"","")+"\"";
		expression = expression.replaceAll("\\s+", "");
		expression = expression.replaceAll("\\s", "");
		expression = expression.replaceAll(" ", "");
		
		String loopPredicate = "iterator("+variable+","+expression+")";//.";
		if(addDot){
			loopPredicate = loopPredicate +".";
		}
		predicatesForSelection.add(loopPredicate.toLowerCase());
		addMapInfoForLineNumbers(variable.toLowerCase(), 
				node.getStartPosition(),
				unit.getLineNumber(node.getStartPosition()), 
				unit.getLineNumber(node.getStartPosition()+node.getLength()),
				node.getLength());
		
		
		List<String> vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expression.toLowerCase());
		addPredicateDef("iterator", vals);
		
		String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
		if(addDot){
			 containsPredicate = containsPredicate +".";
		}
		predicatesForSelection.add(containsPredicate.toLowerCase());
		
		vals = new ArrayList<String>();
		vals.add(lastVariable);
		vals.add(variable);
		addPredicateDef("contains", vals);
		
		
		addAfterPredicate(variable);
		previous = variable;
		lastVariable = variable;
		variableStack.push(lastVariable);
		}
		return true;
	}
	
	public void endVisit(WhileStatement node){
		if(currentMethod != null){
			if(variableStack.size()>1){
				variableStack.pop();
				lastVariable = variableStack.peek();
			}
		}
	}		

	public boolean visit(SimpleName node){
		if(this.currentMethod != null){
			String name = node.getIdentifier().toString();
			if(types.containsKey(name)){
				String variable = "type_"+ typeCount++;			
				String typeValue = types.get(name).toLowerCase();
				String typePredicate = "type("+variable+","+typeValue+")";
				if(addDot){
					typePredicate = typePredicate +".";
				}
				predicatesForSelection.add(typePredicate.toLowerCase());
				addMapInfoForLineNumbers(variable.toLowerCase(), 
						node.getStartPosition(),
						unit.getLineNumber(node.getStartPosition()), 
						unit.getLineNumber(node.getStartPosition()+node.getLength()),
						node.getLength());
				
				List<String> vals = new ArrayList<String>();
				vals.add(variable);
				vals.add(typeValue);
				addPredicateDef("type", vals);
				
				String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
				if(addDot){
					 containsPredicate = containsPredicate +".";
				}
				predicatesForSelection.add(containsPredicate.toLowerCase());
				
				vals = new ArrayList<String>();
				vals.add(lastVariable);
				vals.add(variable);
				addPredicateDef("contains", vals);								
//				addAfterPredicate(variable);
//				previous = variable;		
			}
		}
		return true;
	}
	
	public boolean visit(CatchClause node){		
		if(currentMethod != null){
			String variable = "catchtype_"+ catchTypeCount++;			
			String catchExpression = node.getException().getType().toString().toLowerCase();
			String catchPredicate = "catch("+variable+","+catchExpression+")";
			if(addDot){
				catchPredicate = catchPredicate +".";
			}
			predicatesForSelection.add(catchPredicate.toLowerCase());
			addMapInfoForLineNumbers(variable.toLowerCase(), 
					node.getStartPosition(),
					unit.getLineNumber(node.getStartPosition()), 
					unit.getLineNumber(node.getStartPosition()+node.getLength()),
					node.getLength());
			
			List<String> vals = new ArrayList<String>();
			vals.add(variable);
			vals.add(catchExpression);
			addPredicateDef("catch", vals);
			
			String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
			if(addDot){
				 containsPredicate = containsPredicate +".";
			}
			predicatesForSelection.add(containsPredicate.toLowerCase());
			
			vals = new ArrayList<String>();
			vals.add(lastVariable);
			vals.add(variable);
			addPredicateDef("contains", vals);
			
			
			addAfterPredicate(variable);
			previous = variable;			
		}
		return true;
	}
	
	public boolean visit(MethodInvocation node){	
		if(currentMethod != null){
		String variable = "methodcall_"+methodCallCount++;
		String expression = node.getName().getFullyQualifiedName().toLowerCase().toLowerCase();
		String methodCallPredicate = "methodcall("+variable+","+expression+")";//.";
		if(addDot){
			methodCallPredicate = methodCallPredicate +".";
		}
		predicatesForSelection.add(methodCallPredicate.toLowerCase());
		addMapInfoForLineNumbers(variable.toLowerCase(), 
				node.getStartPosition(),
				unit.getLineNumber(node.getStartPosition()), 
				unit.getLineNumber(node.getStartPosition()+node.getLength()),
				node.getLength());
		
		List<String> vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expression);
		addPredicateDef("methodcall", vals);
		
		String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
		if(addDot){
			 containsPredicate = containsPredicate +".";
		}
		predicatesForSelection.add(containsPredicate.toLowerCase());
		
		vals = new ArrayList<String>();
		vals.add(lastVariable);
		vals.add(variable);
		addPredicateDef("contains", vals);
		
		
		addAfterPredicate(variable);
		previous = variable;
		}
		return true;
	}
	
	public void combineClassAndMethodName(){
		this.methodName = this.className + ":"+this.methodName;
		this.currentMethod = this.methodName;
	}
}
