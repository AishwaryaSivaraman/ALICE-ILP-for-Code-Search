package alice.test;

public class F1Score {

	public double precision =0.0;
	public double recall = 0.0;
	public double score = 0.0;
	
	public void updatePrecisionAndRecall(double pres, double rec){
		precision = precision + pres;
		recall = recall + rec;
	}
	
	public void averagePresAndRec(int N){
		precision = precision/(N*1.0);
		recall = recall/(N*1.0);
	}
	
	public void calculateF1Score(){
		 score = 2*((precision*recall)/(precision+recall));
	}
}
