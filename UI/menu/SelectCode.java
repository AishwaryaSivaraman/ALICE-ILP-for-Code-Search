package alice.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import extractor.extract.UTASTParser;
import alice.info.SearchResults;
import alice.util.HighlightManager;
import alice.util.TextRangeUtil;

public class SelectCode extends BaseAction{
	
	public static ISourceRange allSelection;
	public static IFile iFile;
	public static Boolean isUserBias;
	public static TextSelection codeSelection;
	
	@Override
	protected void run(ISelection selection) {	
		codeSelection = null;
		EnableFeature.enabledSelectionStrings.clear();
		SearchResults.clear();
		System.out.println("Select color");
	    isUserBias = true;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
	    {	        
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object obj  = structuredSelection.getFirstElement();
			iFile  = (IFile) Platform.getAdapterManager().getAdapter(obj, IFile.class);			
	        TextSelection s = (TextSelection) window.getSelectionService().getSelection();	
	        codeSelection = s;
	        System.out.println("Start line number "+s.getText());
	        System.out.println("Start line number "+s.getLength());
	        ICompilationUnit unit = (ICompilationUnit) JavaCore.create(iFile);
			ISourceRange iSourceRange = TextRangeUtil.getSelection(unit, s.getStartLine(), 0, s.getEndLine()+2, 0);			
			if(allSelection== null){
				iSourceRange = new SourceRange(s.getOffset(), s.getLength());
				allSelection = iSourceRange;
			}		

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
