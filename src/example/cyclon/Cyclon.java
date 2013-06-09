package example.cyclon;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class Cyclon implements Linkable, EDProtocol, CDProtocol
{
	private static final String PAR_CACHE = "cache";
	private static final String PAR_L = "l";
	private static final String PAR_TRANSPORT = "transport";
	private static final long TIMEOUT = 2000;

	private final int size;
	private final int l;
	private final int tid;

	private List<CyclonEntry> cache = null;

	public Cyclon(String n)
	{
		this.size = Configuration.getInt(n + "." + PAR_CACHE);
		this.l = Configuration.getInt(n + "." + PAR_L);
		this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

		cache = new ArrayList<CyclonEntry>(size);
	}

	//-------------------------------------------------------------------
	private void increaseAgeAndSort()
	{
		//for (CyclonEntry ce : cache)
		//	ce.increase();

		Collections.sort(cache, new CyclonEntry());
	}

	private CyclonEntry selectNeighbor()
	{
		try{			
			int i = cache.size()-1;

			if (cache.get(i).removed && (CommonState.getTime() - cache.get(i).timeRemoved) >= TIMEOUT){
				cache.remove(i);
				i--;
			}

			while (cache.get(i).removed)
				i--;
			
			return cache.get(i);
			//return cache.get(CommonState.r.nextInt(cache.size()));
		} catch (Exception e){
			return null;
		}
	}

	private List<CyclonEntry> selectNeighbors(int l, Node rnode, boolean selectedAtRequest)
	{
		int dim = Math.min(l, cache.size()-1);
		List<CyclonEntry> list = new ArrayList<CyclonEntry>(dim);

		List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < cache.size(); i++)
			if (!cache.get(i).removed)
				tmp.add(i);
			else if ((CommonState.getTime() - cache.get(i).timeRemoved) >= TIMEOUT){
				//System.out.println("TIMEOUT " + CommonState.getTime());
				cache.get(i).reuseNode();
				tmp.add(i);
			}

		int sup = Math.min(dim, tmp.size());
		for (int i = 0; i < sup; i++){
			CyclonEntry ce = cache.get((tmp.remove(CommonState.r.nextInt(tmp.size())).intValue()));
			ce.removeNode(rnode, selectedAtRequest, false);
			list.add(ce);
		}

		return list;
	}

	private List<CyclonEntry> discardEntries(Node n, List<CyclonEntry> list)
	{
		int index = 0;
		List<CyclonEntry> newList = new ArrayList<CyclonEntry>();
		for (CyclonEntry ce : list)
			if (!ce.n.equals(n) && (index = indexOf(ce.n)) < 0)
				newList.add(ce);
			//Duplicate, take the newest one
			else if (index >= 0)
				cache.get(index).age = Math.max(ce.age, cache.get(index).age);

		return newList;
	}

	private int getFirstDeleted(Node rnode, boolean selectedAtRequest)
	{
		for (int i = cache.size()-1 ; i >= 0; i--)
			if (cache.get(i).removed && cache.get(i).nodeSended.equals(rnode) && cache.get(i).selectedAtRequest == selectedAtRequest)
				return i;

		return -1;
	}
	
	private int indexOf(Node rnode)
	{
		for (int i = cache.size()-1; i >= 0; i--)
			if (cache.get(i).n.equals(rnode))
				return i;
		
		return -1;
	}

	private void insertReceivedItems(List<CyclonEntry> list, Node rnode, boolean selectedAtRequest)
	{
//		if (CommonState.getNode().getID() == 9784)
//			System.err.println(rnode.getID() + " " + selectedAtRequest);
		
		if (list.isEmpty()){
			System.err.println("Empty");
			cache.remove(indexOf(rnode));
			return;
		}
		
		if (selectedAtRequest)
			try{
			cache.set(indexOf(rnode) , new CyclonEntry(list.remove(0)));
			} catch (Exception e){
				System.err.println(CommonState.getNode().getID() + " " + rnode.getID());
				e.printStackTrace();
			}
		
		for (CyclonEntry ce : list){
			// firstly using empty cache slots
			if (cache.size() < size)
				cache.add(new CyclonEntry(ce.n, ce.age));
			// secondly replacing entries among the ones sent to rnode
			else{
				int index = getFirstDeleted(rnode, selectedAtRequest);
//				if (CommonState.getNode().getID() == 9784){
//					System.err.println("REPLACE " + cache.get(index).n.getID() + " " + cache.get(index).selectedAtRequest + " " + selectedAtRequest);
//				}
//				if (index < 0){
//					System.err.println("PROBLEM " + CommonState.getNode().getID() + " " + cache.size() + " " + rnode.getID());
//					return;
//				}
				//cache.set(indexOf(sentList.remove(index)), new CyclonEntry(ce.n, ce.age));
				if (index > 0)
					cache.set(index, new CyclonEntry(ce.n, ce.age));
			}
		}

		for (CyclonEntry ce : cache){
			if (ce.nodeSended != null && ce.nodeSended.equals(rnode) && ce.selectedAtRequest == selectedAtRequest){
//				if (CommonState.getNode().getID() == 9784)
//					System.err.println("REUSE " + ce.n.getID());
				ce.reuseNode();
			}
		}
	}
	//-------------------------------------------------------------------


	public Object clone()
	{
		Cyclon cyclon = null;
		try { cyclon = (Cyclon) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		cyclon.cache = new ArrayList<CyclonEntry>();

		return cyclon;
	}

	public boolean addNeighbor(Node neighbour)
	{
		if (contains(neighbour))
			return false;

		if (cache.size() >= size)
			return false;

		CyclonEntry ce = new CyclonEntry(neighbour, CommonState.getTime());
		cache.add(ce);

		return true;
	}

	public boolean contains(Node neighbour)
	{
		for (CyclonEntry ne : cache)
			if (ne.n.equals(neighbour))
				return true;

		return false;
	}

	public int degree()
	{
		return cache.size();
	}

	public Node getNeighbor(int i)
	{
		return cache.get(i).n;
	}

	public void pack() {}

	public void onKill() {}

	public void processEvent(Node node, int pid, Object event)
	{
		CyclonMessage message = (CyclonMessage) event;

		List<CyclonEntry> nodesToSend = null;
		if (message.isRequest){
			nodesToSend = selectNeighbors(message.list.size(), message.sender, false);

			CyclonMessage msg = new CyclonMessage(node, nodesToSend, false);
			Transport tr = (Transport) node.getProtocol(tid);
			tr.send(node, message.sender, msg, pid);
			
//			if (node.getID() == 9784){
//				System.err.println(message.isResuest + " SEND " + message.node.getID());
//				for (CyclonEntry ce1 : nodesToSend){
//					if (ce1.removed)
//						System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
//				}
//				System.err.println();
//			}
		}

		// 5. Discard entries pointing to P, and entries that are already in P’s cache.
	 	List<CyclonEntry> list = discardEntries(node, message.list);

		// 6. Update P’s cache to include all remaining entries, by firstly using empty
		//    cache slots (if any), and secondly replacing entries among the ones originally
		//    sent to Q.

//		if (node.getID() == 9784){
//			System.err.println(message.isResuest + " P " + message.node.getID());
//			for (CyclonEntry ce1 : cache){
//				if (ce1.removed)
//					System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
//			}
//			System.err.println();
//		}

		insertReceivedItems(list, message.sender, !message.isRequest);

//		if (node.getID() == 9784){//480
//			for (CyclonEntry ce1 : cache){
//				if (ce1.removed)
//					System.err.print(ce1.n.getID() + "," + ce1.nodeSended.getID() + " " + ce1.removed + " ");
//			}
//			System.err.println("XX\n" + cache.size());
//		}

		// 1. Increase by one the age of all neighbors.
		increaseAgeAndSort();
	}

	public void nextCycle(Node node, int protocolID)
	{
		// 1. Increase by one the age of all neighbors.
		//increaseAgeAndSort();

		// 2. Select neighbor Q with the highest age among all neighbors...
		CyclonEntry ce = selectNeighbor();
		if (ce == null){
			System.err.println(node.getID() + ": no Peer");
			return;
		}
		ce.removeNode(ce.n, true, true);
		
		//    and l − 1 other random neighbors.
		List<CyclonEntry> nodesToSend = selectNeighbors(l-1, ce.n, true);

		// 3. Replace Q’s entry with a new entry of age 0 and with P’s address.
		nodesToSend.add(0, new CyclonEntry(node, CommonState.getTime()));

		// 4. Send the updated subset to peer Q.
		CyclonMessage message = new CyclonMessage(node, nodesToSend, true);
		Transport tr = (Transport) node.getProtocol(tid);
		tr.send(node, ce.n, message, protocolID);
	}
	
	public String toString()
	{
		String s = "[";
		for (CyclonEntry ce : cache)
			s += "(" + ce.n.getID() + "," + ce.age + ")";
		s += "]";
		return s;
	}
}
