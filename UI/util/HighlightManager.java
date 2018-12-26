package alice.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import alice.menu.DisableFeature;
import alice.menu.EnableFeature;
import alice.menu.SelectCode;

public interface HighlightManager {

	public static void highlightIfSelection(){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
	    {	        				
	        IEditorPart editorPart = window.getActivePage().getActiveEditor();
        	
	        if(editorPart != null){
	        	ITextOperationTarget target = (ITextOperationTarget)editorPart.getAdapter(ITextOperationTarget.class);
	        	TextSelection sel = (TextSelection) editorPart.getEditorSite().getSelectionProvider().getSelection();
	        	if(target instanceof ITextViewer){
        		ITextViewer viewer = (ITextViewer)target;
        		StyledText text= viewer.getTextWidget();
        		Device device = Display.getCurrent ();
        		if(SelectCode.allSelection != null){
        			StyleRange range= new StyleRange(SelectCode.allSelection.getOffset(), SelectCode.allSelection.getLength(), device.getSystemColor(SWT.COLOR_INFO_FOREGROUND), device.getSystemColor(SWT.COLOR_DARK_GRAY),SWT.NORMAL );
        			text.setStyleRange(range);
        		}
        		for(int i=0;i<EnableFeature.enableSelection.size();i++){
        			StyleRange range= new StyleRange(EnableFeature.enableSelection.get(i).getOffset(), EnableFeature.enableSelection.get(i).getLength(), device.getSystemColor(SWT.COLOR_INFO_FOREGROUND), device.getSystemColor(SWT.COLOR_DARK_GREEN),SWT.NORMAL );
        			text.setStyleRange(range);
        		}
        		
        		for(int i=0;i<DisableFeature.disableSelection.size();i++){
        			StyleRange range= new StyleRange(DisableFeature.disableSelection.get(i).getOffset(), DisableFeature.disableSelection.get(i).getLength(), device.getSystemColor(SWT.COLOR_INFO_FOREGROUND), device.getSystemColor(SWT.COLOR_DARK_RED),SWT.NORMAL );
        			text.setStyleRange(range);
        		}
//        		editorPart.getEditorSite().getSelectionProvider().setSelection(null);
        	}
        }
	   }
		
	}
}
