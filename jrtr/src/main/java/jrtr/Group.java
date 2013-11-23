package jrtr;
import java.util.LinkedList;

public abstract class Group implements Node
{
	protected LinkedList<Node> m_children;
	
	public Group()
	{	
		m_children = new LinkedList<Node>();
	}
	
	public void addNode(Node node)
	{
		m_children.add(node);
	}
	
	public void removeFirst()
	{
		m_children.remove(0);
	}
}