package example.cyclon;

import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.*;
import peersim.util.IncrementalStats;

public class CacheObserver implements Control {
	private static final String PAR_PROT = "protocol";

	private final String name;
	private final int pid;


	public CacheObserver(String prefix) {
		this.name = prefix;
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	public boolean execute() {

		IncrementalStats is = new IncrementalStats();

		for (int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			Cyclon cyclon = (Cyclon) n.getProtocol(pid);
			is.add(cyclon.getCacheSize());
		}

		System.out.print(name + ":  ");
		System.out.print("time: " +  peersim.core.CommonState.getTime() + "  ");
//		System.out.print("min: " + is.getMin() + "  ");
//		System.out.print("max: " + is.getMax() + "  ");
		System.out.print("avg: " + is.getAverage() + " ");
		System.out.println();

		return false;
	}
}
