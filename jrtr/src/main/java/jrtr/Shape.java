package jrtr;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.vecmath.*;

import jrtr.VertexData.VertexElement;

/**
 * Represents a 3D shape. The shape currently just consists
 * of its vertex data. It should later be extended to include
 * material properties, shaders, etc.
 */
public class Shape {

	private Material material;
	private VertexData vertexData;
	private Matrix4f t;
	private Vector3f centerCoords;
	private float radius;
	
	/**
	 * Make a shape from {@link VertexData}. A shape contains the geometry 
	 * (the {@link VertexData}), and material properties for shading.
	 *  
	 *  
	 * @param vertexData the vertices of the shape.
	 */
	public Shape(VertexData vertexData)
	{
		this.vertexData = vertexData;
		t = new Matrix4f();
		t.setIdentity();
		
		material = null;
	}

	public VertexData getVertexData()
	{
		return vertexData;
	}
	
	public void setTransformation(Matrix4f t)
	{
		this.t = t;
	}
	
	public Matrix4f getTransformation()
	{
		return t;
	}

	public Vector3f getCenterCoords()
	{
		return centerCoords;
	}
	
	public float getRadius()
	{
		return radius;
	}
	
	/**
	 * Set a reference to a material for this shape.
	 * 
	 * @param material
	 * 		the material to be referenced from this shape
	 */
	public void setMaterial(Material material)
	{
		this.material = material;
	}

	/**
	 * To be implemented in the "Textures and Shading" project.
	 */
	public Material getMaterial()
	{
		return material;
	}
	
	public void createBoundingSphere()
	{
		float maxDist = 0f;

		LinkedList<VertexElement> vertexElements = vertexData.getElements();
		int indices[] = vertexData.getIndices();

		if (indices == null) return;

		Vector3f centroid = getCentroid();
		
		for(int j=0; j < indices.length; j++)
		{
			int i = indices[j];
			
			ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);
			while(itr.hasNext())
			{
				VertexData.VertexElement e = itr.next();
				if(e.getSemantic() == VertexData.Semantic.POSITION)
				{
					if(e.getNumberOfComponents()==3)
					{
						float dist = (float)Math.sqrt(Math.pow(e.getData()[i*3] - centroid.x, 2) + 
													  Math.pow(e.getData()[i*3+1] - centroid.y, 2) + 
													  Math.pow(e.getData()[i*3+2] - centroid.z, 2));
						if (maxDist < dist)
							maxDist = dist;
					}
				} 
			}
		}	
		
		centerCoords = centroid;
		radius = maxDist;
	}
	
	public Vector3f getCentroid()
	{
		LinkedList<VertexElement> vertexElements = this.vertexData.getElements();
		int indices[] = vertexData.getIndices();

		int count = 0;
		Vector3f centroid = new Vector3f(0, 0, 0);
		
		for(int j=0; j < indices.length; j++)
		{
			int i = indices[j];
			
			ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);
			while(itr.hasNext())
			{
				VertexData.VertexElement e = itr.next();
				if(e.getSemantic() == VertexData.Semantic.POSITION)
				{
					if(e.getNumberOfComponents()==3)
					{
						centroid.add(new Vector3f(e.getData()[i*3], e.getData()[i*3+1], e.getData()[i*3+2]));
						count++;
					}
				} 
			}
		}	
		
		centroid.scale(1f / (float)count);
		return centroid;
	}
	
}
