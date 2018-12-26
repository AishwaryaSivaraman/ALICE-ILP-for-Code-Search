package alice.menu;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import extractor.extract.Extractor;

public class GenerateFacts extends BaseAction{

	public static HashMap<String, Integer> TypeFrequency = new HashMap<String, Integer>();
	public static HashMap<String, Integer> MethodFrequency = new HashMap<String, Integer>();
	
	@Override
	protected void run(ISelection selection) {
		// TODO Auto-generated method stub
		System.out.println("Generate Facts");
		IStructuredSelection sel = (IStructuredSelection) selection;
        Object firstElement = sel.getFirstElement();
        if (firstElement instanceof IAdaptable)
        {        
            IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);                        
            IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();            
            if(myWorkspaceRoot!=null){
            	Extractor extractor = new Extractor();
            	long startTimeNano = System.nanoTime( );
            	extractor.Extract(project, project.getLocation(), myWorkspaceRoot);
            	long taskTimeNano  = System.nanoTime( ) - startTimeNano;
            	System.out.println("Time to extract "+taskTimeNano);
            	TypeFrequency = new HashMap<String, Integer>();
            	TypeFrequency.putAll(Extractor.typeFrequency);
            	
            	MethodFrequency = new HashMap<String, Integer>();
            	MethodFrequency.putAll(Extractor.callFrequency);
            	
            	System.out.println("Mappings "+Extractor.typeFrequency);
            	System.out.println("Mappings "+Extractor.callFrequency);
            }                                    
        }
	}
	
	protected boolean isEnabled(ISelection s) {		
		IStructuredSelection sel = (IStructuredSelection) s;
        Object firstElement = sel.getFirstElement();
        if (firstElement instanceof IAdaptable)
        {
            IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
            if(project!=null){
            	IPath path =  project.getFullPath();
            	if(path == null){
            		return false;
            	}           	
            } else{
            	return false;
            }                        
        }
		return true;
	}	

}
