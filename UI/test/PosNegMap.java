package alice.test;

import java.util.HashMap;

public class PosNegMap {

	public HashMap<Integer, Integer> posCountAtIter = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> negCountAtIter = new HashMap<Integer, Integer>();

	public HashMap<Integer, F1Score> scoreAtGreedy = new HashMap<Integer, F1Score>();
	public HashMap<Integer, F1Score> scoreAtExhuas = new HashMap<Integer, F1Score>();

	public HashMap<Integer, String> queryForIterGreedy = new HashMap<Integer, String>();
	public HashMap<Integer, String> queryForIterExh = new HashMap<Integer, String>();

	public double precision_greedy =0.0;
	public double recall_greedy = 0.0;
	public double precision_exh =0.0;
	public double recall_exh = 0.0;
}
