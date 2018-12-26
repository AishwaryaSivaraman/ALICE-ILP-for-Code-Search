package alice.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import alice.util.HighlightManager;
import alice.util.TextRangeUtil;

public class EnableFeature extends BaseAction{

	public static List<ISourceRange> enableSelection = new ArrayList<ISourceRange>();
	public static List<String> enabledSelectionStrings = new ArrayList<String>();
	
	@Override
	protected void run(ISelection selection) {			
		System.out.println("Enable Feature");
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
	    {	        
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object obj  = structuredSelection.getFirstElement();
			IFile file  = (IFile) Platform.getAdapterManager().getAdapter(obj, IFile.class);			
	        TextSelection s = (TextSelection) window.getSelectionService().getSelection();		
	        
	        System.out.println("Start line number "+s.getText());
	        System.out.println("Start line number "+s.getLength());
	        String expressionAsString = s.getText().toLowerCase();
	        expressionAsString = expressionAsString.replaceAll("\\s+", "");
			expressionAsString = expressionAsString.replaceAll("\\s", "");
			expressionAsString = expressionAsString.replaceAll(" ", "");
			//REMOVING _ check for other cases.
//			expressionAsString = expressionAsString.replaceAll("_", "");
			System.out.println(expressionAsString);
	        enabledSelectionStrings.add(expressionAsString);
	        ICompilationUnit unit = (ICompilationUnit) JavaCore.create(file);
			ISourceRange iSourceRange = TextRangeUtil.getSelection(unit, s.getStartLine(), 0, s.getEndLine(), 0);			
			iSourceRange = new SourceRange(s.getOffset(), s.getLength());
			enableSelection.add(iSourceRange);
			HighlightManager.highlightIfSelection();			
	   }
		
	}

	
	protected boolean isEnabled(ISelection s) {		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
	    {	        
	        TextSelection selection = (TextSelection) window.getSelectionService().getSelection();
	        if(selection.isEmpty()){
	        	return false;	        	
	        }
	    }
		
		return true;
	}
}
