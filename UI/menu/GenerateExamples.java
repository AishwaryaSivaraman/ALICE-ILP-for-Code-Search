package alice.menu;

import java.util.HashMap;
import java.util.List;

import learn.Learner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import def.JavaSourceCodeInfo;
import alice.info.SearchResults;
import alice.util.ExampleFromPrologResults;
import alice.views.AliceExampleView;
import extractor.extract.ExtractFromBlock;
import extractor.extract.ExtractFromBlockRelational;
import extractor.extract.Extractor;
import extractor.info.PackageInfo;
import extractor.info.SelectionInfo;
import extractor.info.UserBiasInfo;

public class GenerateExamples extends BaseAction{

	@Override
	protected void run(ISelection selection) {
		// TODO Auto-generated method stub
		System.out.println("Generate More Examples");
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
	    {	        
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object obj  = structuredSelection.getFirstElement();
			IFile file  = (IFile) Platform.getAdapterManager().getAdapter(obj, IFile.class);			
	        TextSelection s = (TextSelection) window.getSelectionService().getSelection();	
	        
//	        ExtractFromBlock eBlock = new ExtractFromBlock();	        
//	        eBlock.extractBlock(file, s.getText(), s.getStartLine(), s.getLength());
	        
	        ExtractFromBlockRelational eRBlock = new ExtractFromBlockRelational();
	        
	        TextSelection codeSelection;
	        if(SelectCode.isUserBias){
	        	codeSelection = SelectCode.codeSelection;
	        } else{
	        	codeSelection = s;
	        }

	        System.out.println(codeSelection.getText());

	        SelectionInfo info = new UserBiasInfo(codeSelection.getText(),codeSelection.getStartLine(), codeSelection.getLength(), file, EnableFeature.enabledSelectionStrings, DisableFeature.disabledSelectionStrings);
	        eRBlock.extractBlock(info);
	        
//	        eBlock.getCoveringMethod(file, file.getRawLocation().toOSString(), s.getStartLine(), s.getLength());
	        
	        
//	        System.out.println(eBlock.visitor.predicates);
//	        System.out.println(eBlock.visitor.predicatesForMethod);	        
//	        getCoveringMethod(file,file.getRawLocation().toOSString(), s.getStartLine(), s.getLength());	       
	        Learner learn = new Learner();
	        Learner.factBaseName = eRBlock.packageFileName;
	        try {
				learn.init(eRBlock.visitor.predicateDefiDefs, GenerateFacts.TypeFrequency,GenerateFacts.MethodFrequency, EnableFeature.enabledSelectionStrings, DisableFeature.disabledSelectionStrings, eRBlock.visitor.generalisedPredicates);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        System.out.println(learn.solutions);
	       //generateTypesForCurrentFile(file.getRawLocation().toOSString());
	      //convert selection to logic facts	
	       List<JavaSourceCodeInfo> sourceCodeInfo = ExampleFromPrologResults.populatecodeInfo(PackageInfo.classInfoMap, 
	        		PackageInfo.lineInfoForVariables, learn.solutions,learn.solutionObjects);
	       SearchResults.clear();
	       SearchResults.solutions.addAll(sourceCodeInfo);
	       SearchResults.initExampleType();
	       System.out.println("Number of examples returned are :"+sourceCodeInfo.size());
	       AliceExampleView.updateViewer(sourceCodeInfo);
	    }
	}
	
	protected boolean isEnabled(ISelection s) {		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
	    {	        
	        if(!SelectCode.isUserBias){
	        	return false;
	        }
	    }
		
		return true;
	}
	
	
		

}
