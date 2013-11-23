package jrtr;
import java.util.LinkedList;

import javax.vecmath.Matrix4f;

public interface Node 
{
	public Shape getShape();
	public Matrix4f getTransformation();
	public LinkedList<Node> getChildren();
}