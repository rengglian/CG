package jrtr;
import java.util.LinkedList;

import javax.vecmath.*;

public class TransformGroup extends Group
{
	private Matrix4f m_trans;

	public TransformGroup()
	{
		super();
		Matrix4f mat = new Matrix4f();
		mat.setIdentity();
		m_trans = mat;
	}
	
	public TransformGroup(Matrix4f trans)
	{
		super();
		m_trans = trans;
	}
	
	public LinkedList<Node> getChildren() 
	{
		return m_children;
	}

	public Shape getShape() 
	{
		return null;
	}

	public Matrix4f getTransformation() 
	{
		return m_trans;
	}
	
	public void rotX(float angle)
	{
		Matrix4f t = new Matrix4f();
		t.rotX(angle);
		t.mul(m_trans);
		m_trans = t;
	}

	public void rotY(float angle)
	{
		Matrix4f t = new Matrix4f();
		t.rotY(angle);
		t.mul(m_trans);
		m_trans = t;
	}
	
	public void rotZ(float angle)
	{
		Matrix4f t = new Matrix4f();
		t.rotZ(angle);
		t.mul(m_trans);
		m_trans = t;
	}
	
	public void setTransformation(Matrix4f trans)
	{
		m_trans = trans;
	}

}