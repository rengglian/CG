package jrtr;

import javax.vecmath.*;

/**
 * Stores the specification of a virtual camera. You will extend
 * this class to construct a 4x4 camera matrix, i.e., the world-to-
 * camera transform from intuitive parameters. 
 * 
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a camera.
 */
public class Camera {

	private Matrix4f m_cameraMatrix;
	private Vector3f m_centerOfProjection;
	private Vector3f m_lookAtPoint;
	private Vector3f m_upVector;
	/**
	 * Construct a camera with a default camera matrix. The camera
	 * matrix corresponds to the world-to-camera transform. This default
	 * matrix places the camera at (0,0,10) in world space, facing towards
	 * the origin (0,0,0) of world space, i.e., towards the negative z-axis.
	 */
	public Camera()
	{
		m_cameraMatrix = new Matrix4f();
		m_centerOfProjection = new Vector3f(0.0f, 0.0f, -10.0f);
		m_lookAtPoint = new Vector3f(0.0f, 0.0f, 0.0f);
		m_upVector = new Vector3f(0.0f, 1.0f, 0.0f);
		setCamera();
	}	
	
	/**
	 * Return the camera matrix, i.e., the world-to-camera transform. For example, 
	 * this is used by the renderer.
	 * 
	 * @return the 4x4 world-to-camera transform matrix
	 */
	public Matrix4f getCameraMatrix()
	{
		return this.m_cameraMatrix;
	}
	
	public Vector3f getCenterOfProjection()
	{
		Vector3f tVect = this.m_centerOfProjection;
		tVect.x = tVect.x;
		tVect.y = tVect.y;
		tVect.z = tVect.z;
		
		return tVect;
	}
	
	public Vector3f getLookAtPoint()
	{
		return this.m_lookAtPoint;
	}
	
	public Vector3f getUpVector()
	{
		return this.m_upVector;
	}
	
	public void setCenterOfProjection(Vector3f centerOfProjection)
	{
		centerOfProjection.x = centerOfProjection.x;
		centerOfProjection.y = centerOfProjection.y;
		centerOfProjection.z = centerOfProjection.z;
		this.m_centerOfProjection = centerOfProjection;
		setCamera();
	}

	public void setLookAtPoint(Vector3f lookAtPoint)
	{
		this.m_lookAtPoint = lookAtPoint;
		setCamera();
	}	
	
	public void setUpVector(Vector3f upVector)
	{
		this.m_upVector = upVector;
		setCamera();
	}

	private void setCamera()
	{
		Vector3f tw = new Vector3f();
		tw.sub(this.m_centerOfProjection,this.m_lookAtPoint);
		tw.normalize();

		Vector3f tu = new Vector3f();
		tu.cross(this.m_upVector, tw);
		tu.normalize();
		
		Vector3f tv = new Vector3f();
		tv.cross(tw, tu);
		
		Matrix3f rot3f = new Matrix3f();
		rot3f.setColumn(0, tu);
		rot3f.setColumn(1, tv);
		rot3f.setColumn(2, tw);
		
		this.m_cameraMatrix.set(rot3f, this.m_centerOfProjection, 1.0f);
		this.m_cameraMatrix.invert();
	}
}
