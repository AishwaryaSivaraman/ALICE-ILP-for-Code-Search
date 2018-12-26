package extractor.extract;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTUtil {

	
	public static List<String> getCandidateFeatures(String selection, HashMap<String, String> types){
		ASTFeatureExtractor featureExtractor = new ASTFeatureExtractor();
		featureExtractor.types = types;
		UTASTParser searchParser = new UTASTParser();
		CompilationUnit cu = searchParser.parseBlock(selection);
		cu.accept(featureExtractor);
		return featureExtractor.candidatefeatures;
	}
	
	
}
