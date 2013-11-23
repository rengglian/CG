package jrtr;


import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TimeZone;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * A simple scene manager that stores objects and lights in linked lists.
 */
public class GraphSceneManager implements SceneManagerInterface {

	private LinkedList<Shape> shapes;
	private LinkedList<Light> lights;
	private Node rootNode;
	private boolean culling = false;
	
	public static Frustum frustum;
	public static Camera camera;
	public static Calendar calendar;
	
	public GraphSceneManager()
	{
		shapes = new LinkedList<Shape>();
		lights = new LinkedList<Light>();
		camera = new Camera();
		frustum = new Frustum();
		
		calendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.clear();
		calendar.setTime(new Date());
	}
	
	public void setRootNode(Node node)
	{
		rootNode = node;
	}
	
	public void enableCulling(boolean enable)
	{
		culling = enable;
	}
	
	public Node getRootNode()
	{
		return rootNode;
	}

	public long getRunTime()
	{
		return System.currentTimeMillis() - calendar.getTimeInMillis();
	}
	
	public Camera getCamera()
	{
		return camera;
	}

	public Frustum getFrustum()
	{
		return frustum;
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
		return new GraphSceneManagerItr(this);
	}
	

	

	

	
	
	private class StackElement
	{
		private Node m_node;
		private Matrix4f m_trans;
		
		public StackElement(Node n, Matrix4f t)
		{
			m_node = n;
			m_trans = t;
		}
		
		public Node getNode()
		{
			return m_node;
		}
		
		public Matrix4f getTransformation()
		{
			return m_trans;
		}
	}
	
	private class GraphSceneManagerItr implements SceneManagerIterator 
	{
		private Stack<StackElement> stack;
		
		public GraphSceneManagerItr(GraphSceneManager sceneManager)
		{
			stack = new Stack<StackElement>();
			Node root = GraphSceneManager.this.getRootNode();
			stack.push(new StackElement(root, root.getTransformation()));
		}
		
		public boolean hasNext()
		{
			return !stack.isEmpty();
		}
		
		public RenderItem next()
		{
			// next
			StackElement next = stack.pop();
			Node node = next.getNode();
			
			if (!(node instanceof Leaf))
			{
				for (Node n : node.getChildren())
				{
					Matrix4f t = new Matrix4f();
					t.mul(next.getTransformation(), n.getTransformation());
					stack.add(new StackElement(n, t));
				}
			}
			
			RenderItem retVal = new RenderItem(node.getShape(), next.getTransformation());
			if (culling && node.getShape() != null)
			{
				if (!shouldPaint(node, next.getTransformation()))
					retVal = new RenderItem(null, null);
			}
			
			return retVal;
		}
		
		private boolean shouldPaint(Node node, Matrix4f fullTransformation)
		{
			if (node.getShape().getCenterCoords() == null)node.getShape().createBoundingSphere();
			
			// check if bounding sphere is completely outside view frustum
			Frustum frustum = GraphSceneManager.this.getFrustum();
			Camera camera = GraphSceneManager.this.getCamera();
			Matrix4f camMat = (Matrix4f)camera.getCameraMatrix().clone();
			Matrix4f rot;
			float FOV = frustum.getFOV();
			float alpha = FOV / 2;
			float aspect = frustum.getAspectRatio();
			float near = frustum.getNearPlane();
			float far = frustum.getFarPlane();
			float beta = (float)Math.atan(Math.tan((double)alpha * (1 / aspect)));
			Vector3f oldCenter = node.getShape().getCenterCoords();
			Vector4f center = new Vector4f(oldCenter.x, oldCenter.y, oldCenter.z, 1);
			fullTransformation.transform(center);
			camMat.transform(center);
			float radius = node.getShape().getRadius();				
			
			
			Vector4f normal = new Vector4f();
			float d = 0;
			float distance = 0;
			boolean paint = true;
			
			// near
			normal = new Vector4f(0, 0, 1, 0);
			camMat.transform(normal);
			d = near;
			distance = center.dot(normal) - d;
			if (distance > radius)
				paint = false;
			
			// far
			normal = new Vector4f(0, 0, -1, 0);
			camMat.transform(normal);
			d = far;
			distance = center.dot(normal) - d;
			if (distance > radius)
				paint = false;
			
			// left
			normal = new Vector4f(-1, 0, 0, 0);
			rot = new Matrix4f();
			rot.setIdentity();
			rot.rotY(alpha);
			rot.transform(normal);
			camMat.transform(normal);
			d = 0;
			distance = center.dot(normal) - d;
			if (distance > radius)
				paint = false;

			// right
			normal = new Vector4f(1, 0, 0, 0);
			rot = new Matrix4f();
			rot.setIdentity();
			rot.rotY(-alpha);
			rot.transform(normal);
			camMat.transform(normal);
			d = 0;
			distance = center.dot(normal) - d;
			if (distance > radius)
				paint = false;
			
			// top
			normal = new Vector4f(0, 1, 0, 0);
			rot = new Matrix4f();
			rot.setIdentity();
			rot.rotX(beta);
			rot.transform(normal);
			camMat.transform(normal);
			d = 0;
			distance = center.dot(normal) - d;
			if (distance > radius)
				paint = false;
			
			// bottom
			normal = new Vector4f(0, -1, 0, 0);
			rot = new Matrix4f();
			rot.setIdentity();
			rot.rotX(-beta);
			rot.transform(normal);
			camMat.transform(normal);
			d = 0;
			distance = center.dot(normal) - d;
			if (distance > radius)
				paint = false;
		
			return paint;
		}
	}
}
