package example.cyclon;

import peersim.core.Node;

/**
 * @author Jens V. Fischer
 */
public class CyclonEntry {
	public Node node;
	public long age;
	public boolean removed;

	public CyclonEntry(Node node, long age) {
		this.node = node;
		this.age = age;
	}

	public CyclonEntry(Node node, long age, boolean removed) {
		this.node = node;
		this.age = age;
		this.removed = removed;
	}
}
