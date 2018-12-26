package extractor.info;

import java.util.List;

import org.eclipse.core.resources.IFile;

public class UserBiasInfo extends SelectionInfo{
	public List<String> enabledFeatures;
	public List<String> disabledFeatures;
	
	public UserBiasInfo(String selection, int startIndex, int length,
			IFile file, List<String> enabledFeatures,
			List<String> disabledFeatures) {
		super(selection, startIndex, length, file);
		this.enabledFeatures = enabledFeatures;
		this.disabledFeatures = disabledFeatures;
	}
	
	
}
