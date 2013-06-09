package example.reports;

import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class DegreeObserver implements Control
{
	private final static String PAR_PID = "protocol";
	private static final String PAR_DEAD = "dead";

	private final int pid;
	private final String name;
	private final boolean dead;

	private class Entry
	{
		long nodeId;
		int indegree;
	}

	public DegreeObserver(String prefix)
	{
		this.pid = Configuration.getPid(prefix + "." + PAR_PID);
		this.dead = Configuration.contains(prefix + "." + PAR_DEAD);
		this.name = prefix;
	}

	private int indexOf(Entry[] entry, long id)
	{
		for (int i = 0; i < entry.length; i++)
			if (entry[i].nodeId == id)
				return i;
		return -1;
	}

	public boolean execute()
	{
		Entry[] entry = new Entry[Network.size()];
		for (int i = 0; i < Network.size(); i++){
			entry[i] = new Entry();
			entry[i].indegree = 0;
			entry[i].nodeId = Network.get(i).getID();
		}

		int index = 0;
		int degreeToDead = 0;
		Set<Node> referredDeadNode = new HashSet<Node>();
		for (int i = 0; i < Network.size(); i++){
			Node n = Network.get(i);

			if (n.isUp()){
				Linkable linkable = (Linkable)n.getProtocol(pid);
				for (int j = 0; j < linkable.degree(); j++)
					if ((index = indexOf(entry, linkable.getNeighbor(j).getID())) >= 0)
						entry[index].indegree++;
					else if (dead){
						degreeToDead++;
						referredDeadNode.add(linkable.getNeighbor(j));
					}
			}
		}

		IncrementalStats is = new IncrementalStats();

		for (int i = 0; i < Network.size(); i++){
			System.out.println(CommonState.getTime() + " " + name + ": InDegree " + entry[i].indegree);
			is.add(entry[i].indegree);
		}

		System.out.println(CommonState.getIntTime() + " " + name + ": size " + Network.size() + " " + is);
		if (dead)
			System.out.println(CommonState.getIntTime() + " " + name + ": dead " + degreeToDead + " referred " + referredDeadNode.size());

		return false;
	}
}
