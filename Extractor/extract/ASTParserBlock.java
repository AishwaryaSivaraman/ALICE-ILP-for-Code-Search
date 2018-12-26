package extractor.extract;

import java.awt.image.ReplicateScaleFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.WhileStatement;

import def.PredicateDef;
import extractor.info.MethodInfo;

public class ASTParserBlock extends ASTVisitor {
	
	public StringBuilder builder = new StringBuilder();
	public String currentMethod = null;
	public String className = null;
	public String lastVariable = null;
	public String methodName = null;
	public boolean addDot = true;
	
	public HashMap<String, String> types = new HashMap<String, String>();
	public List<String> predicatesForSelection = new ArrayList<>();
	Stack<String> variableStack = new Stack<String>();
	public List<PredicateDef> predicateDefiDefs = new ArrayList<PredicateDef>();
	public List<PredicateDef> generalisedPredicates = new ArrayList<PredicateDef>();
	public String previous = null;
	int ifCount;
	int whileCount;
	int forCount;
	int methodCallCount;
	int catchTypeCount;
	int typeCount;
	
	boolean generaliseConditionals;
	
	//Todo: 
	//make a stack of last in so that you can get back 
	//need a map between regex and actual conditions
	
	public ASTParserBlock() {
		int ifCount = 0;
		int whileCount =0;
		int forCount =0;
		int methodCallCount = 0;
	}
	
	public boolean visit(MethodDeclaration node){
		System.out.println("Fullyqualified name is "+node.getName().getFullyQualifiedName());
	    this.currentMethod = this.className+":"+node.getName().getFullyQualifiedName().toLowerCase();
		addMethodDecForBlock();
		return true;
	}
	
	public void addMethodDecForBlock(){		
		String predicate = "methoddec("+this.currentMethod+")";//.";		
		if(addDot){
			predicate = predicate +".";
		} else{
			predicate = "methoddec("+"X"+")";//.";
		}
		predicatesForSelection.add(predicate.toLowerCase());
		
		List<String> val = new ArrayList<String>();
		val.add(this.currentMethod.toLowerCase());
		addPredicateDef("methoddec", val);
		addGeneralisePredDef("methoddec", val);
		
		lastVariable = currentMethod;
		variableStack.push(currentMethod);
	}
	
	public void endVisit(MethodDeclaration node){
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
	
	public void addGeneralisePredDef(String predType, List<String> values){
		//define preddef
		PredicateDef def = new PredicateDef();
		def.predType = predType;
		List<String> vals = new ArrayList<String>();
		vals.addAll(values);
		def.values = values;	
		generalisedPredicates.add(def);		
	}
	
	public List<String> reorderBasedOnSize(List<String> original){
		 Collections.sort(original, new Comparator<String>() {
		        @Override
		        public int compare(String a, String b) {
		            return Integer.compare(b.length(),a.length());//specifying compare type that is compare with length
		        }
	    });
		return original;		
	}
	
	public String generaliseConditionals(Expression expr){		
		ASTExpressionParser expParser = new ASTExpressionParser();		
		expr.accept(expParser);
		String expressionAsString = expr.toString();
		
		expressionAsString = expressionAsString.replaceAll("\\s+", "");
		expressionAsString = expressionAsString.replaceAll("\\s", "");
		expressionAsString = expressionAsString.replaceAll(" ", "");
		if(expressionAsString.length()>200){
			return ("\""+expressionAsString+"\"").toLowerCase();
		}
		
		if(expressionAsString.contains("*")){						
			expressionAsString = expressionAsString.replaceAll("\\*", ".*");		
		}
		
		if(expressionAsString.contains("\\n")){					
			expressionAsString = expressionAsString.replaceAll("\\\\n", ".*");		
		}
		
		if(expressionAsString.contains("\\r")){					
			expressionAsString = expressionAsString.replaceAll("\\\\r", ".*");		
		}
		
		if(expressionAsString.contains("\\")){						
			expressionAsString = expressionAsString.replaceAll("\\\\", ".*");		
		}
		
		if(expressionAsString.contains("|")){						
			expressionAsString = expressionAsString.replaceAll("\\|", ".*");		
		}
				
		if(expressionAsString.contains("[")){			
			String[] bracketSubstrings = StringUtils.substringsBetween(expressionAsString,"[","]");
			for(String bracketSubstring : bracketSubstrings ){
//				String escapedBracketExpression = "[^["+bracketSubstring+"^]]";
				String escapedBracketExpression = "\\\\["+bracketSubstring+"\\\\]";
				expressionAsString = expressionAsString.replace("["+bracketSubstring+"]",escapedBracketExpression);							
			}			
		}
		
		if(expressionAsString.contains("(") && expressionAsString.contains(")")){						
			expressionAsString= expressionAsString.replaceAll("\\(", "[\\(]").replaceAll("\\)","[\\)]");				
		}
		
		
		if(expressionAsString.contains("+")){						
			expressionAsString = expressionAsString.replaceAll("\\+", "[+]");		
		}
		
		if(expressionAsString.contains("\"")){						
			expressionAsString = expressionAsString.replaceAll("\"", ".*");		
		}

		
		
		expParser.simpleNames = reorderBasedOnSize(expParser.simpleNames);
		
		for(int i=0;i<expParser.simpleNames.size();i++){
			if(!expParser.simpleNames.get(i).equals("i")){
				if(expParser.simpleNames.get(i).length()>1){
					expressionAsString = expressionAsString.toLowerCase().replace(expParser.simpleNames.get(i), ".*");
				} else{
					expressionAsString = expressionAsString.toLowerCase().replace(expParser.simpleNames.get(i), ".");
				}
			} else{
				for(int j=0;j<expressionAsString.length();j++){
					if(expressionAsString.charAt(j) == 'i'){
						if(j+1 != expressionAsString.length() && !Character.isAlphabetic(expressionAsString.charAt(j+1))){
							expressionAsString = expressionAsString.substring(0, j)+".*"+expressionAsString.substring(j+1, expressionAsString.length());
						} else{
							if(j+1 == expressionAsString.length()){
								expressionAsString = expressionAsString.substring(0, j)+".*";
							}
						}
					}
				}
			}
		}				
					
		return ("\""+expressionAsString+"\"").toLowerCase();
	}
	
	
	public boolean visit(IfStatement node){		
		if(currentMethod != null){
		    String variable = "if_"+ifCount++; 
		    String expression =  "\""+ node.getExpression().toString().replaceAll("\"","")+"\"";
		    expression = expression.replaceAll("\\s+", "");
			expression = expression.replaceAll("\\s", "");
			expression = expression.replaceAll(" ", "");
			expression = expression.replaceAll("_", "");
//			expression = expression.replaceAll("\"", "");
			
		    String ifPredicate = "if("+variable+","+expression+")";//.";
		    if(addDot){
		    	ifPredicate = ifPredicate +".";
			}
			predicatesForSelection.add(ifPredicate.toLowerCase());
						
			String regexExpression = generaliseConditionals(node.getExpression());
			
			
			List<String> vals = new ArrayList<String>();
			vals.add(variable.toLowerCase());
			vals.add(expression.toLowerCase());
			addPredicateDef("if", vals);
			
			vals = new ArrayList<String>();
			vals.add(variable.toLowerCase());
			vals.add(regexExpression);			
			if(regexExpression.length()<200){
			addGeneralisePredDef("iflike", vals);
			} else{
				addGeneralisePredDef("if", vals);
			}
			
			String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
			 if(addDot){
				 containsPredicate = containsPredicate +".";
			}
			predicatesForSelection.add(containsPredicate.toLowerCase());
			
			vals = new ArrayList<String>();
			vals.add(lastVariable);
			vals.add(variable);
			addPredicateDef("contains", vals);
			addGeneralisePredDef("contains", vals);
			
			addAfterPredicate(variable);
			previous = variable;
			
			lastVariable = variable;
			variableStack.push(lastVariable);
			System.out.println("here inside if" +variableStack);
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
			addGeneralisePredDef("after", vals);
		}
	}
	
	public void endVisit(IfStatement node){
		System.out.println(variableStack);
		if(currentMethod != null){
			variableStack.pop();
			lastVariable = variableStack.peek();
		}
	}
	
	public boolean visit(ForStatement node){
		try{
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
		
		
		List<String> vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expression.toLowerCase());
		addPredicateDef("iterator", vals);
		
		String expressionString = generaliseConditionals(node.getExpression());
		
		vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expressionString);
		if(expressionString.length()<200){
		addGeneralisePredDef("iterlike", vals);
		}else{
			addGeneralisePredDef("iter", vals);
		}
		
				
		String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
		 if(addDot){
			 containsPredicate = containsPredicate +".";
		}
		predicatesForSelection.add(containsPredicate.toLowerCase());
		
		vals = new ArrayList<String>();
		vals.add(lastVariable);
		vals.add(variable);
		addPredicateDef("contains", vals);
		addGeneralisePredDef("contains", vals);
		
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
			variableStack.pop();
			lastVariable = variableStack.peek();
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
		
		List<String> vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expression.toLowerCase());
		addPredicateDef("iterator", vals);
		
		String expressionString = generaliseConditionals(node.getExpression());
		
		vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expressionString);
		if(expressionString.length()<200){
		addGeneralisePredDef("iterlike", vals);
		}else{
			addGeneralisePredDef("iter", vals);
		}
		
		String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
		if(addDot){
			 containsPredicate = containsPredicate +".";
		}
		predicatesForSelection.add(containsPredicate.toLowerCase());
		
		vals = new ArrayList<String>();
		vals.add(lastVariable);
		vals.add(variable);
		addPredicateDef("contains", vals);
		addGeneralisePredDef("contains", vals);
		
		addAfterPredicate(variable);
		previous = variable;
		lastVariable = variable;
		variableStack.push(lastVariable);
		}
		return true;
	}
	
	public void endVisit(WhileStatement node){
		if(currentMethod != null){
		variableStack.pop();
		lastVariable = variableStack.peek();
		}
	}		

	public boolean visit(MethodInvocation node){	
		if(currentMethod != null){
		String variable = "methodcall_"+methodCallCount++;
		String expression = node.getName().getFullyQualifiedName().toLowerCase().replace("_", "");
		String methodCallPredicate = "methodcall("+variable+","+expression+")";//.";
		if(addDot){
			methodCallPredicate = methodCallPredicate +".";
		}
		predicatesForSelection.add(methodCallPredicate.toLowerCase());
		
		List<String> vals = new ArrayList<String>();
		vals.add(variable);
		vals.add(expression);
		addPredicateDef("methodcall", vals);
		addGeneralisePredDef("methodcall", vals);
		
		String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
		if(addDot){
			 containsPredicate = containsPredicate +".";
		}
		predicatesForSelection.add(containsPredicate.toLowerCase());
		
		vals = new ArrayList<String>();
		vals.add(lastVariable);
		vals.add(variable);
		addPredicateDef("contains", vals);
		addGeneralisePredDef("contains", vals);
		
		addAfterPredicate(variable);
		previous = variable;
		}
		return true;
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
				
				List<String> vals = new ArrayList<String>();
				vals.add(variable);
				vals.add(typeValue);
				addPredicateDef("type", vals);
				addGeneralisePredDef("type", vals);
				
				String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
				if(addDot){
					 containsPredicate = containsPredicate +".";
				}
				predicatesForSelection.add(containsPredicate.toLowerCase());
				
				vals = new ArrayList<String>();
				vals.add(lastVariable);
				vals.add(variable);
				addPredicateDef("contains", vals);	
				addGeneralisePredDef("contains", vals);
				
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
			
			List<String> vals = new ArrayList<String>();
			vals.add(variable);
			vals.add(catchExpression);
			addPredicateDef("catch", vals);
			addGeneralisePredDef("catch", vals);
			
			String containsPredicate = "contains("+lastVariable+","+variable+")";//.";
			if(addDot){
				 containsPredicate = containsPredicate +".";
			}
			predicatesForSelection.add(containsPredicate.toLowerCase());
			
			vals = new ArrayList<String>();
			vals.add(lastVariable);
			vals.add(variable);
			addPredicateDef("contains", vals);
			addGeneralisePredDef("contains", vals);
			
			addAfterPredicate(variable);
			previous = variable;			
		}
		return true;
	}
	
	public void combineClassAndMethodName(){
//		this.methodName = this.className + ":"+this.methodName;
		this.methodName = "X";
		this.currentMethod = this.methodName.toLowerCase();
	}
}
