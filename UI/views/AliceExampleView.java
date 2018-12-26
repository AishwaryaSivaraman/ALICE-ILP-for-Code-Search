package alice.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.text.TextSelection;

import def.JavaSourceCodeInfo;
import alice.info.SearchResults;
import alice.menu.ActiveLearningUIHandler;
import alice.util.TextRangeUtil;

public class AliceExampleView extends ViewPart  {

	public static TableViewer viewer;
	public static int updateCount =0;
	public static boolean clear = false;
	
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		System.out.println("Hello World");
		createViewer(parent);          
	}
		
	public static void updateViewer(List<JavaSourceCodeInfo> sourceCodeInfo){						
//		ModelProvider.INSTANCE.setResults(Learner.RESULTS.getSearchInfo());	
		IWorkbenchPage wp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IViewPart myView=wp.findView("ALICE.exampleview");
		wp.hideView(myView);
		
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("ALICE.exampleview");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(clear){
			clearTable();
			clear = false;
		}
		ModelProvider.INSTANCE.setResults(SearchResults.solutions);
		viewer.setInput(ModelProvider.INSTANCE.getResults());				
		viewer.refresh();			
		includeRadio();					
		if(updateCount>0)
		{		changeOldRecords();}
		updateCount++;
		viewer.refresh();		
	}
	
	public static void changeOldRecords(){	
		final TableItem[] items = viewer.getTable().getItems();
		for( int i=0;i<items.length;i++){
			StyleRange range = new StyleRange();
			
			if(SearchResults.solutions.get(i).isOld && SearchResults.exampleType[i]==2){
				items[i].setGrayed(true);
				items[i].setChecked(false);
				items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));											
			} 
			if(!SearchResults.solutions.get(i).isOld){
				items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			}
			
			if(SearchResults.solutions.get(i).isOld && SearchResults.exampleType[i]==0){
				items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			}
			
			if(SearchResults.solutions.get(i).isOld && SearchResults.exampleType[i]==1){
				items[i].setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
			}			
		}
	}
	
	public static void clearTable(){		
//		TableEditor editor = new TableEditor(viewer.getTable());		
//		final TableItem[] items = viewer.getTable().getItems();
//		for( int i=0;i<items.length;i++){
//			viewer.clear(i);			
//		}
	}
	
	public static void includeRadio(){
		
		final TableItem[] items = viewer.getTable().getItems();
		TableEditor editor = new TableEditor(viewer.getTable());		
		editor.dispose();		
		
		for( int i=0;i<items.length;i++){				  
				  editor = new TableEditor(items[i].getParent());
				  final int currentI = i;
			      Button button = new Button(viewer.getTable(), SWT.CHECK);
			      button.setSelection(false);
			      button.pack();
			      editor.minimumWidth = button.getSize().x;
			      editor.horizontalAlignment = SWT.CENTER;
			      
//			      button.setEnabled(false);
//			      button.setText("Pos");
			      
			      editor.setEditor(button, items[i], 0);		
			      button.addSelectionListener(new SelectionListener() {				
					@Override
					public void widgetSelected(SelectionEvent e) {
						System.out.println("In positive selection");
						Button btn = (Button)e.getSource();
						
						if(btn.getSelection()){
							SearchResults.exampleType[currentI]=1;
							
						}
						System.out.println("Value for  "+items[currentI].getText(2)+" is "+SearchResults.exampleType[currentI]);
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
//			}
		}
//		
		for( int i=0;i<items.length;i++){	
//			if(!Learner.RESULTS.getSearchInfo().get(i).isOld()){
				 editor = new TableEditor(items[i].getParent());
				final int currentI = i;
			      Button button = new Button(viewer.getTable(), SWT.CHECK);
			      button.setSelection(false);
			      button.pack();
			      editor.minimumWidth = button.getSize().x;
			      editor.horizontalAlignment = SWT.CENTER;
//			      button.setText("Neg");
//			      button.setEnabled(false);
			     
			      editor.setEditor(button, items[i], 1);	
			      button.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							System.out.println("In negative selection");
							Button btn = (Button)e.getSource();
							if(btn.getSelection()){
								SearchResults.exampleType[currentI]=2;								
							}							
//							System.out.println("Value for  "+items[currentI].getText(2)+" is "+Learner.RESULTS.getExampleTypeAtIndex(currentI));
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
//			}
			
		}
	}
	
	
		
	private void createViewer(Composite parent) {
			ActiveLearningUIHandler lCustomAction = new ActiveLearningUIHandler();
			lCustomAction.setText("Search Again");
			getViewSite().getActionBars().getMenuManager().add(lCustomAction);
			
	        viewer = new TableViewer(parent,SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);	              
	        createColumns(parent, viewer);
	        final Table table = viewer.getTable();
	        table.setHeaderVisible(true);
	        table.setLinesVisible(true);
	        
	        table.addListener(SWT.Selection, new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					// TODO Auto-generated method stub
					JavaSourceCodeInfo info = null;
					TableItem[] selection = table.getSelection();
					int currentSelection =0;
					for(int i=0;i<selection.length;i++){
						info = (JavaSourceCodeInfo)selection[i].getData();
	
					}
					selectFileAndCode(info);
				}
				
			});
	              
	        viewer.setContentProvider(new ArrayContentProvider());
	        // get the content for the viewer, setInput will call getElements in the
	        // contentProvider
	        viewer.setInput(ModelProvider.INSTANCE.getResults());
	        // make the selection available to other views
	        getSite().setSelectionProvider(viewer);
	        // set the sorter for the table
	    		       	        
	        // define layout for the viewer
	        GridData gridData = new GridData();
	        gridData.verticalAlignment = GridData.FILL;
	        gridData.horizontalSpan = 2;
	        gridData.grabExcessHorizontalSpace = true;
	        gridData.grabExcessVerticalSpace = true;
	        gridData.horizontalAlignment = GridData.FILL;
	        viewer.getControl().setLayoutData(gridData);
	}
	
	private void selectFileAndCode(JavaSourceCodeInfo info) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IPath path = new Path(info.getExamplePath());
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		try {
			IDE.openEditor(page, file);
			ICompilationUnit unit = (ICompilationUnit) JavaCore.create(file);
			ISourceRange range = TextRangeUtil.getSelection(unit, info.getStartLineNumber(), 0, info.getEndLineNumber()+1, 0);
			ISelection selection = new TextSelection(range.getOffset(),range.getLength());
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getSelectionProvider().setSelection(selection);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
	       String[] titles = {"Positive Example","Negative Example", "File Path","Method Name"};
	       int[] bounds = {800};
	       TableViewerColumn col = createTableViewerColumn(titles[0], 100, 0);     
	       
	       col.setLabelProvider(new ColumnLabelProvider(){
	    	   @Override
	           public String getText(Object element) {           
	    		 return "";
	           } 
	       });
	       
	       col = createTableViewerColumn(titles[1], 100, 1);     
	       
	       col.setLabelProvider(new ColumnLabelProvider(){
	    	   @Override
	           public String getText(Object element) {           
	    		 return "";
	           } 
	       });
	       
	       col = createTableViewerColumn(titles[2], bounds[0], 2);     
	       col.setLabelProvider(new ColumnLabelProvider() {
	            @Override
	            public String getText(Object element) {           
	            	JavaSourceCodeInfo info = (JavaSourceCodeInfo) element;
	                return info.getExamplePath();
	            }
	        });      
	       
	       col = createTableViewerColumn(titles[3], bounds[0], 3);
	       col.setLabelProvider(new ColumnLabelProvider() {
	           @Override
	           public String getText(Object element) {           
	        	   JavaSourceCodeInfo info = (JavaSourceCodeInfo) element;
	               return info.getMethodName();
	           }
	       });      
	      
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
	    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
	    final TableColumn column = viewerColumn.getColumn();
	    column.setText(title);
	    column.setWidth(bound);
	    column.setResizable(true);
	    column.setMoveable(true);
	    return viewerColumn;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}

enum ModelProvider {
	INSTANCE;
	private List<JavaSourceCodeInfo> results;
	private ModelProvider(){
		results = new ArrayList<JavaSourceCodeInfo>();
	}
	
	public List<JavaSourceCodeInfo> getResults(){		
		return results;
	}

	public void setResults(List<JavaSourceCodeInfo> results) {
		this.results = results;
	}		
}
