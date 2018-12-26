package extractor.info;

import org.eclipse.core.resources.IFile;

public class SelectionInfo {
	public String selection;
	public int startIndex;
	public int length;
	public IFile file;
	public SelectionInfo(String selection, int startIndex, int length,
			IFile file) {
		super();
		this.selection = selection;
		this.startIndex = startIndex;
		this.length = length;
		this.file = file;
	}
		
	public SelectionInfo(){
		
	}
}
