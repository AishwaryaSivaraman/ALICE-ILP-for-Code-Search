package extractor.extract;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class UTASTParser {
	/**
	 * 
	 * @param icu
	 * @return
	 */
	public CompilationUnit parse(ICompilationUnit icu) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit c = (CompilationUnit) parser.createAST(null);
		return c;
	}

	/**
	 * 
	 * @param codeText
	 * @return
	 */
	public CompilationUnit searchParse(String codeText) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(codeText.toCharArray());
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	
	
		
	//as of now we care about the statements in a block.
	public CompilationUnit parseBlock(String code){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(code.toCharArray());
		parser.setResolveBindings(true);				
		final Block block =(Block) parser.createAST(null);		
		ASTNode parent = block.getParent();
		while (parent != null && !(parent instanceof CompilationUnit)) {
			parent = parent.getParent();
		}
		final CompilationUnit cu = (CompilationUnit) parent;		
		return cu;
	}
			
	

	/**
	 * 
	 * @param icu
	 * @param monitor
	 * @return
	 */
	public CompilationUnit parse(ICompilationUnit icu, IProgressMonitor monitor) {
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(monitor);
	}
}