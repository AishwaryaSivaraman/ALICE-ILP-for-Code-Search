package extractor.info;

public class MethodInfo {

	String methodName;
	int startLineNumber;
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public int getStartLineNumber() {
		return startLineNumber;
	}
	public void setStartLineNumber(int startLineNumber) {
		this.startLineNumber = startLineNumber;
	}
	public MethodInfo(String methodName, int startLineNumber) {
		super();
		this.methodName = methodName;
		this.startLineNumber = startLineNumber;
	}	
}
