package alice.util;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class TextRangeUtil {

	public static ISourceRange getSelection(ICompilationUnit unit, int startLine,int startColumn,int endLine, int endColumn){
		IDocument document;
		try {
			document = new Document(unit.getSource());
			int offset= getOffset(document, startLine, startColumn);
	        int end= getOffset(document, endLine, endColumn);
	        return new SourceRange(offset, end - offset);	        
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	  
		
	}
	
	private static int getOffset(IDocument document, int line, int column) throws BadLocationException {
        int r= document.getLineInformation(line - 1).getOffset();
        IRegion region= document.getLineInformation(line - 1);
        int lineTabCount= calculateTabCountInLine(document.get(region.getOffset(), region.getLength()), column);
        r += (column - 1) - (lineTabCount * getTabWidth()) + lineTabCount;
        return r;

    }
	
	 private static final int getTabWidth(){

	        return 4;

	    }
	 
	 public static int calculateTabCountInLine(String lineSource, int lastCharOffset){

	        int acc= 0;

	        int charCount= 0;

	        for(int i= 0; charCount < lastCharOffset - 1; i++){

	            if ('\t' == lineSource.charAt(i)){

	                acc++;

	                charCount += getTabWidth();

	            }   else

	                charCount += 1;

	        }

	        return acc;

	    }
}