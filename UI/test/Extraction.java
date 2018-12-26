package alice.test;
import java.util.HashMap;

import extractor.extract.ExtractFromBlockRelational;
import extractor.info.SelectionInfo;
import extractor.info.UserBiasInfo;


public class Extraction {
	
	public static ExtractFromBlockRelational extractFromBlockRelational;
	public HashMap<String, String> types = new HashMap<String, String>();
	public void craftSelectionInfo(String selection, String methodName, String className,String path){        
			extractFromBlockRelational = new ExtractFromBlockRelational();
			extractFromBlockRelational.generateTypesForCurrentFile(path);			
			extractFromBlockRelational.visitor.methodName = methodName;
			extractFromBlockRelational.visitor.className = className;
			extractFromBlockRelational.visitor.addDot = false;
			extractFromBlockRelational.visitor.combineClassAndMethodName();
			extractFromBlockRelational.visitor.addMethodDecForBlock();		
			this.types = extractFromBlockRelational.visitor.types;
			extractFromBlockRelational.generatePredicate(selection);			
	}
}
