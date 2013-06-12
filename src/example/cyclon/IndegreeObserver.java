package example.cyclon;

import java.util.*;
import java.util.Map.Entry;

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

	private Map<Long, Integer> indegreeMap = new HashMap<Long, Integer>();
	private final String name;
	private int pid;
	private int deadcnt;
	private int cycle = 0;
	
	public IndegreeObserver(String prefix){
		this.name = prefix;
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		deadcnt = 0;
	}

	@Override
	public boolean execute() {


		deadcnt = 0;
		indegreeMap.clear();
		for (int i = 0; i < Network.size(); i++) {
			// get node
			Node n = Network.get(i);
			if (n.isUp()) {
				// is alive, get neighbors
				Linkable link = (Linkable) n.getProtocol(pid);
				for (int k = 0; k < link.degree(); k++) {
					// for each neighbor increase counter in indegreemap
					long id = link.getNeighbor(k).getID();
					if (indegreeMap.containsKey(id)) {
						// node is already in map
						int count = indegreeMap.get(id).intValue() + 1;
						indegreeMap.put(id, (Integer) count);
					} else {
						// node is not in map, add node and of course
						// increment
						// count by one
						indegreeMap.put(id, new Integer(1));
					}
				}
			} else {
				// is dead
				deadcnt++;
			}
		}

//		ArrayList<Integer> values = new ArrayList<Integer>();
		TreeMap<Integer, Integer> hist = new TreeMap<Integer, Integer>();


		// now use IncrementalStats for - well - statistics
		IncrementalStats stats = new IncrementalStats();
		for (Entry<Long, Integer> e : indegreeMap.entrySet()) {
			stats.add(e.getValue());
//			values.add(e.getValue());
			if (CommonState.getTime() == CommonState.getEndTime()-1) {
				if (!hist.containsKey(e.getValue())) {
					hist.put(e.getValue(), new Integer(1));
				} else {
					int tmp = hist.get(e.getValue()).intValue() + 1;
					hist.put(e.getValue(), tmp);
				}
			}
		}

//		/* Histogram */
//
//		ArrayList<Integer> k0 = new ArrayList<Integer>();
//		ArrayList<Integer> k1 = new ArrayList<Integer>();
//		ArrayList<Integer> k2 = new ArrayList<Integer>();
//		ArrayList<Integer> k3 = new ArrayList<Integer>();
//		ArrayList<Integer> k4 = new ArrayList<Integer>();
//		ArrayList<Integer> k5 = new ArrayList<Integer>();
//		ArrayList<Integer> k6 = new ArrayList<Integer>();
//		ArrayList<Integer> k7 = new ArrayList<Integer>();
//		ArrayList<Integer> k8 = new ArrayList<Integer>();
//		ArrayList<Integer> k9 = new ArrayList<Integer>();
//
//		for (Integer value : values) {
//			if (value < 10)
//				k0.add(value);
//			else if (value < 20)
//				k1.add(value);
//			else if (value < 30)
//				k2.add(value);
//			else if (value < 40)
//				k3.add(value);
//			else if (value < 50)
//				k4.add(value);
//			else if (value < 60)
//				k5.add(value);
//			else if (value < 70)
//				k6.add(value);
//			else if (value < 80)
//				k7.add(value);
//			else if (value < 90)
//				k8.add(value);
//			else
//				k9.add(value);
//		}
//
//
//
//		/* print histogram peersim style */
//		System.out.println(name + ": " + CommonState.getIntTime() +
//				" "+ k0.size() +
//				" "+ k1.size() +
//				" "+ k2.size() +
//				" "+ k3.size() +
//				" "+ k4.size() +
//				" "+ k5.size() +
//				" "+ k6.size() +
//				" "+ k7.size() +
//				" "+ k8.size() +
//				" "+ k9.size() );
//
//		/* print histogram as csv */
//		System.out.println(k0.size() + ";" + k1.size() + ";" + k2.size() + ";" + k3.size() + ";" + k4.size() + ";"
//				+ k5.size() + ";" + k6.size() + ";" + k7.size() + ";" + k8.size() + ";" + k9.size());



		/* Output */
		System.out.println(name + ":  " + CommonState.getIntTime() +
				"  size: " + Network.size() +
		 		"  min: " + stats.getMin() +
				"  max: " + stats.getMax() +
				"  avg: " + stats.getAverage() );

		if (CommonState.getTime() == CommonState.getEndTime()-1){
			System.out.println("\n" + name + ": frequency distribution");
			for (Entry entry : hist.entrySet()) {
				System.out.println(entry.getKey().toString() + " " + entry.getValue().toString());
			}
		}

		return false;
	}

}
