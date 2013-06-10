package example.cyclon;

import java.util.HashMap;
import java.util.Map;
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
	private int pid;
	private int deadcnt;
	
	public IndegreeObserver(String prefix){
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		deadcnt = 0;
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
		// now use IncrementalStats for - well - statistics
		IncrementalStats stats = new IncrementalStats();
		for(Entry<Long, Integer> e : indegreeMap.entrySet()){
			stats.add(e.getValue());
		}
		
		// TODO create histogram
		
		// Output
		System.out.println(CommonState.getIntTime() + " " + Network.size()
				+ " " + deadcnt + " " + stats.getMin() + " " + stats.getMax()
				+ " " + stats.getAverage());
		return false;
	}

}
