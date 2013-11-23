package jrtr;


import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;

import javax.vecmath.*;
/**
 * A simple scene manager that stores objects and lights in linked lists.
 */
public class SimpleSceneManager implements SceneManagerInterface {

	private LinkedList<Shape> shapes;
	private LinkedList<Light> lights;
	
	private Frustum frustum;
	
	
	public static Camera camera;
	public SimpleSceneManager()
	{
		shapes = new LinkedList<Shape>();
		lights = new LinkedList<Light>();
		camera = new Camera();
		frustum = new Frustum();
	}
	
	public Camera getCamera()
	{
		return camera;
	}
	public Vector3f getCenterOfProjection()
	{
		return camera.getCenterOfProjection();
	}
	public Vector3f getLookAtPoint()
	{
		return camera.getLookAtPoint();
	}
	public Vector3f getUpVector()
	{
		return camera.getUpVector();
	}
	public void setCenterOfProjection(Vector3f centerOfProjection)
	{
		camera.setCenterOfProjection(centerOfProjection);
	}
	public void setLookAtPoint(Vector3f lookAtPoint)
	{
		camera.setLookAtPoint(lookAtPoint);
	}
	public void setUpVector(Vector3f upVector)
	{
		camera.setUpVector(upVector);
	}

	public Frustum getFrustum()
	{
		return frustum;
	}
	
	public float getAspectRatio()
	{
		return frustum.getAspectRatio();
	}
	
	public float getNearPlane()
	{
		return frustum.getNearPlane();
	}
	
	public float getFarPlane()
	{
		return frustum.getFarPlane();
	}
	
	public float getFOV()
	{
		return frustum.getFOV();
	}
	
	public void setAspectRatio(float aspectRatio)
	{
		frustum.setAspectRatio(aspectRatio);
	}
	
	public void setNearPlane(float nearPlane)
	{
		frustum.setNearPlane(nearPlane);
	}
	
	public void setFarPlane(float farPlane)
	{
		frustum.setFarPlane(farPlane);
	}
	
	public void setFOV(float fov)
	{
		frustum.setFOV(fov);
	}
	
	public void addShape(Shape shape)
	{
		shapes.add(shape);
	}
	
	public void addLight(Light light)
	{
		lights.add(light);
	}
	
	public Iterator<Light> lightIterator()
	{
		return lights.iterator();
	}
	
	public SceneManagerIterator iterator()
	{
		return new SimpleSceneManagerItr(this);
	}
	
	private class SimpleSceneManagerItr implements SceneManagerIterator {
		
		public SimpleSceneManagerItr(SimpleSceneManager sceneManager)
		{
			itr = sceneManager.shapes.listIterator(0);
		}
		
		public boolean hasNext()
		{
			return itr.hasNext();
		}
		
		public RenderItem next()
		{
			Shape shape = itr.next();
			// Here the transformation in the RenderItem is simply the 
			// transformation matrix of the shape. More sophisticated 
			// scene managers will set the transformation for the 
			// RenderItem differently.
			return new RenderItem(shape, shape.getTransformation());
		}
		
		ListIterator<Shape> itr;
	}
	
}
