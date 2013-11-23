package jrtr;
import java.util.LinkedList;

import javax.vecmath.Matrix4f;

public class ShapeNode extends Leaf
{
	private Shape m_shape;
	
	public ShapeNode(Shape s)
	{
		m_shape = s;
	}
	
	public LinkedList<Node> getChildren() 
	{
		return null;
	}

	public Shape getShape() 
	{
		return m_shape;
	}
	
	public void setShape(Shape s)
	{
		m_shape = s;
	}

	public Matrix4f getTransformation() 
	{
		Matrix4f mat = new Matrix4f();
		mat.setIdentity();
		return mat;
	}

}