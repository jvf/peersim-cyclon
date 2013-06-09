package example.cyclon;

import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;

public class CyclonMessage
{
	public Node sender;
	public List<CyclonEntry> list;
	public boolean isRequest;
	
	public long time;

	public CyclonMessage(Node node, List<CyclonEntry> list, boolean isRequest)
	{
		this.sender = node;
		this.list = list;
		this.isRequest = isRequest;
		
		this.time = CommonState.getTime();
	}
}
