package example.cyclon;

import java.util.ArrayList;
import java.util.Random;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Cleanable;
import peersim.core.Linkable;
import peersim.core.Node;

/**
 * @author Jens V. Fischer
 */
public class Cyclon implements CDProtocol, Linkable, Cloneable, Cleanable {

	private static final String PAR_CACHESIZE = "cache";
	private static final String PAR_SHUFFLELENGHT = "shufflelength";

	private final int maxCacheSize;
	private final int shufflelenght;

	private ArrayList<CyclonEntry> cache;
	private CyclonEntry maxAgeEntry;

	private boolean firstCycle = true;
	private Node mySelf;

	public Cyclon(String prefix) {
		this.maxCacheSize = Configuration.getInt(prefix + "." + PAR_CACHESIZE, 20);
		this.shufflelenght = Configuration.getInt(prefix + "." + PAR_SHUFFLELENGHT, 20);
		cache = new ArrayList<CyclonEntry>(20);
	}

	@Override
	public Object clone() {
		Cyclon cyclon = null;
		try {
			cyclon = (Cyclon) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens
		cyclon.cache = new ArrayList<CyclonEntry>(20);

		return cyclon;
	}

	@Override
	public void nextCycle(Node node, int protocolID) {

		if (firstCycle){
			this.mySelf = node;
			firstCycle = false;
		} else {
			/* 1. Increment the age of all neighbours */
			incrementAges();

			CyclonEntry entry = this.maxAgeEntry;

			/* 2. Select Q (oldest neighbour) and l-1 other random nodes from cache */
			/* 3. Replace Q's entry with P (age 0) */
			ArrayList<CyclonEntry> subset = addPtoSubset(node);
			ArrayList<CyclonEntry> subsetToSend = addRandomNeighbours(subset, shufflelenght - 1);

			/* 4. Send the updated subset to Q */
			/* 5. Receive from Q a subset of no more that i of its own entries */
			ArrayList<CyclonEntry> subsetToIntegrate = sendSubsetToQ(subsetToSend, protocolID);

			/*
			 * 6. Discard entries pointing at P and entries already contained in P’s cache. 7. Update P’s cache to include
			 * all remaining entries, by firstly using empty cache slots (if any), and secondly replacing entries among the
			 * ones sent to Q.
			 */
			integrateNeighboursP(node, subsetToIntegrate, subsetToSend);

			if (cache.isEmpty())
				cache.add(entry);
		}

	}

	private void incrementAges() {
		assert !cache.isEmpty() : "cache empty in incrementAges()";
		CyclonEntry currentMax = null;
		long maxAge = 0;
		for (CyclonEntry entry : cache) {
			entry.age++;
			if (entry.age >= maxAge) {
				maxAge = entry.age;
				currentMax = entry;
			}
		}
		this.maxAgeEntry = currentMax;
		assert maxAgeEntry != null : "no maxAgeEntry in incrementAges()";

	}

	private ArrayList<CyclonEntry> addPtoSubset(Node self) {
		ArrayList<CyclonEntry> subset = new ArrayList<CyclonEntry>(shufflelenght);

		/* remove Q from cache */
		boolean bool =  cache.contains(this.maxAgeEntry);
		assert cache.remove(this.maxAgeEntry);

		/* add P (self) to subset */
		assert subset.add(new CyclonEntry(self, 0));
		return subset;
	}

	private ArrayList<CyclonEntry> addRandomNeighbours(ArrayList<CyclonEntry> subset, int length) {

		Random random = new Random();
		int neighborsToSelect = Math.min(cache.size(), length);
		for (int i = 0; i < neighborsToSelect; i++) {
			CyclonEntry entry = cache.get(random.nextInt(cache.size()));
			assert entry != null : "retrieval problem";
			subset.add(entry);
		}

		assert subset.size() <= shufflelenght : "subset().size > shufflelength in addRandomNeighbours";
		assert (cache.size() > shufflelenght ? subset.size()==shufflelenght : true);
		return subset;
	}


	private ArrayList<CyclonEntry> sendSubsetToQ(ArrayList<CyclonEntry> subset, int protocolID) {
		if (this.maxAgeEntry != null && this.maxAgeEntry.node.isUp()) {
			Cyclon q = (Cyclon) this.maxAgeEntry.node.getProtocol(protocolID);
			return q.receiveSubset(subset, cache);
		}
		return new ArrayList(0);
	}


	/*
	 * Like in basic shuffling, the receiving node Q replies by sending back a random subset of at most l of its
	 * neighbors, and updates its own cache to accommodate all received entries. It does not increase, though, any
	 * entry’s age until its own turn comes to initiate a shuffle.
	 */
	public ArrayList<CyclonEntry> receiveSubset(ArrayList<CyclonEntry> subsetReceived, ArrayList<CyclonEntry> cacheFromP) {
		ArrayList<CyclonEntry> subsetToSend = this.addRandomNeighbours(new ArrayList<CyclonEntry>(shufflelenght),
				shufflelenght);
		integrateNeighboursQ(subsetReceived, subsetToSend);
		return subsetToSend;
	}

	private void integrateNeighboursQ(ArrayList<CyclonEntry> subsetReceived, ArrayList<CyclonEntry> subsetSent) {

		/* should only be the case if sending failed */
		if (subsetReceived.isEmpty())
			return;

		for (CyclonEntry entryToInsert : subsetReceived) {
			if (!cacheContainsNode(this.mySelf) && !cacheContainsNode(entryToInsert.node)) {
				if (cache.size() < maxCacheSize)
					cache.add(entryToInsert);
				else {
					CyclonEntry entryToRemove = subsetSent.remove(0);
					
					/*
					 * There are cases, where the old neighbour from the subsetSent is not found in the current cache.
					 * That should not be the case! 
					 * Dirty fix: Only add new neighbour if the removal of a sent neighbour was successful. 
					 * ToDo: find out why there are neighbours who in the subsetSent which are not in the cache 
					 */
					if (cache.remove(entryToRemove))
						cache.add(entryToInsert);
				}
			}
			assert cache.size() <= maxCacheSize : "cache.size(): " + cache.size() + "  > maxCacheSize: " + maxCacheSize
					+ " in integrateNeighbours";
		}

	}

	private void integrateNeighboursP(Node self, ArrayList<CyclonEntry> subsetReceived,
			ArrayList<CyclonEntry> subsetSent) {

		/* remove self from the subsentSent, because self will never be in the cache */
		if (!subsetSent.isEmpty() && subsetSent.get(0).node.equals(self))
			subsetSent.remove(0);

		/* should only be the case if sending failed */
		if (subsetReceived.isEmpty())
			return;

		for (CyclonEntry entryToInsert : subsetReceived) {
			if (!entryToInsert.node.equals(self) && !cacheContainsNode(entryToInsert.node)) {
				if (cache.size() < maxCacheSize)
					cache.add(entryToInsert);
				else {
					
					/* see above */
					CyclonEntry entryToRemove = subsetSent.remove(0);
					if (cache.remove(entryToRemove))
						cache.add(entryToInsert);
				}
			}
			assert cache.size() <= maxCacheSize : "cache.size(): " + cache.size() + "  > maxCacheSize: " + maxCacheSize
					+ " in integrateNeighbours";
		}
	}


	private boolean cacheContainsNode(Node n) {
		for (CyclonEntry entry : cache) {
			if (entry.node.equals(n))
				return true;
		}
		return false;
	}

	/* implementing Linkable */

	@Override
	public int degree() {
		return cache.size();
	}

	@Override
	public Node getNeighbor(int i) {
		return cache.get(i).node;
	}

	@Override
	public boolean addNeighbor(Node neighbour) {
		if (cache.size() < maxCacheSize) {
			return cache.add(new CyclonEntry(neighbour, 0));
		}
		else
			return false;


	}

	@Override
	public boolean contains(Node neighbor) {
		return cacheContainsNode(neighbor);
	}

	@Override
	public void pack() {

	}

	/* implementing Cleanable */

	@Override
	public void onKill() {
		maxAgeEntry = null;
		cache = null;
	}

	public int getCacheSize() {
		return cache.size();
	}
}
