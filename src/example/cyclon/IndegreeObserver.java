package example.cyclon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

/**
 * Indegree Observer for Cyclon
 * 
 * @author Tobias Hoeppner <t.hoeppner@fu-berlin.de>
 * @date 06/10/2013
 * @version 0.9
 *
 */
public class IndegreeObserver implements Control {
	
	private final static String PAR_PID = "protocol";
		
	private Map<Long, Integer> indegreeMap;
	private final String name;
	private int pid;
	private int deadcnt;
	private Map<Integer, Integer> hist;
	
	public IndegreeObserver(String prefix){
		this.name = prefix;
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		deadcnt = 0;
		this.hist = new TreeMap<Integer, Integer>();
		this.indegreeMap = new HashMap<Long, Integer>();
	}

	@Override
	public boolean execute() {
		deadcnt = 0;
		indegreeMap.clear();
		for(int i = 0; i < Network.size(); i++){
			// get node
			Node n = Network.get(i);
			if (n.isUp()){
				// is alive, get neighbors
				Linkable link = (Linkable) n.getProtocol(pid);
				for (int k = 0; k < link.degree(); k++){
					// for each neighbor increase counter in indegreemap
					long id = link.getNeighbor(k).getID();
					if(indegreeMap.containsKey(id)){
						// node is already in map
						int count = indegreeMap.get(id).intValue() + 1;
						indegreeMap.put(id, (Integer) count);
					} else {
						// node is not in map, add node and of course increment
						// count by one
						indegreeMap.put(id, new Integer(1));
					}
				}
			} else {
				// is dead
				deadcnt++;
			}
		}

		ArrayList<Integer> values = new ArrayList<Integer>();

		// now use IncrementalStats for - well - statistics
		IncrementalStats stats = new IncrementalStats();
		for(Entry<Long, Integer> e : indegreeMap.entrySet()){
			stats.add(e.getValue());
			values.add(e.getValue());
			
			if(!hist.containsKey(e.getValue())){
				// value is not in histogramm
				hist.put(e.getValue(), new Integer(1));
			} else {
				// value is in histogramm
				int tmp = hist.get(e.getValue()).intValue() + 1;
				hist.put(e.getValue(), (Integer)tmp);
			}
			
		}
		
		

//		/* quartiles (4-quantiles) */
//		int max = (int) stats.getMax();
//		int onequarter = max/4;
//		ArrayList<Integer> q1 = new ArrayList<Integer>(onequarter+1);
//		ArrayList<Integer> q2 = new ArrayList<Integer>(onequarter+1);
//		ArrayList<Integer> q3 = new ArrayList<Integer>(onequarter+1);
//		ArrayList<Integer> q4 = new ArrayList<Integer>(onequarter+1);
//
//		for (Integer value : values){
//			if (value < onequarter)
//				q1.add(value);
//			else if (value < 2*onequarter)
//				q2.add(value);
//			else if (value < 3*onequarter)
//				q3.add(value);
//			else
//				q4.add(value);
//		}
//
//
//		/* print histogram */
//		System.out.println(name + ":  " + CommonState.getIntTime() +
//				"  q1: " + q1.size() +
//				"  q2: " + q2.size() +
//				"  q3: " + q3.size() +
//				"  q4: " + q4.size());

		// Output
//		System.out.println(name + ":  " + CommonState.getIntTime() + "  size: " + Network.size()
//				+ "  deadcnt: " + deadcnt + "  min: " + stats.getMin() + "  max: " + stats.getMax()
//				+ "  avg: " + stats.getAverage() + "  mincnt: " + stats.getMinCount() + "  maxcnt: " + stats.getMaxCount());
		
		if(CommonState.getIntTime() == 99){
			System.out.println("histogram\nindeg count");
			for(Entry<Integer, Integer> e : hist.entrySet()){
				System.out.println(e.getKey() + " " + e.getValue());
			}
		}
		
		return false;
	}

}
