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
	private static Vector3f m_centerOfProjection;
	private static Vector3f m_lookAtPoint;
	private static Vector3f m_upVector;
	/**
	 * Construct a camera with a default camera matrix. The camera
	 * matrix corresponds to the world-to-camera transform. This default
	 * matrix places the camera at (0,0,10) in world space, facing towards
	 * the origin (0,0,0) of world space, i.e., towards the negative z-axis.
	 */
	public Camera()
	{
		m_cameraMatrix = new Matrix4f();
		m_centerOfProjection = new Vector3f(0.0f, 0.0f, 10.0f);
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
		return m_cameraMatrix;
	}
	
	public Vector3f getCenterOfProjection()
	{
		return m_centerOfProjection;
	}
	
	public Vector3f getLookAtPoint()
	{
		return m_lookAtPoint;
	}
	
	public Vector3f getUpVector()
	{
		return m_upVector;
	}
	
	public void setCenterOfProjection(Vector3f centerOfProjection)
	{
		m_centerOfProjection = centerOfProjection;
		setCamera();
	}

	public void setLookAtPoint(Vector3f lookAtPoint)
	{
		m_lookAtPoint = lookAtPoint;
		setCamera();
	}	
	
	public void setUpVector(Vector3f upVector)
	{
		m_upVector = upVector;
		setCamera();
	}
	
	public void print()
	{
		System.out.printf("\nCoP:\t%3.3f\t%3.3f\t%3.3f\n", m_centerOfProjection.x, m_centerOfProjection.y, m_centerOfProjection.z);
		System.out.printf("LaP:\t%3.3f\t%3.3f\t%3.3f\n", m_lookAtPoint.x, m_lookAtPoint.y, m_lookAtPoint.z);
		System.out.printf("UV:\t%3.3f\t%3.3f\t%3.3f\n\n", m_upVector.x, m_upVector.y, m_upVector.z);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_cameraMatrix.m00, m_cameraMatrix.m01, m_cameraMatrix.m02, m_cameraMatrix.m03);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_cameraMatrix.m10, m_cameraMatrix.m11, m_cameraMatrix.m12, m_cameraMatrix.m13);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_cameraMatrix.m20, m_cameraMatrix.m21, m_cameraMatrix.m22, m_cameraMatrix.m23);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_cameraMatrix.m30, m_cameraMatrix.m31, m_cameraMatrix.m32, m_cameraMatrix.m33);
		System.out.flush();
	}

	private void setCamera()
	{
		Vector3f tw = new Vector3f();
		tw.sub(m_centerOfProjection,m_lookAtPoint);
		tw.normalize();

		Vector3f tu = new Vector3f();
		tu.cross(m_upVector, tw);
		tu.normalize();
		
		Vector3f tv = new Vector3f();
		tv.cross(tw, tu);
		
		Matrix3f rot3f = new Matrix3f();
		rot3f.setColumn(0, tu);
		rot3f.setColumn(1, tv);
		rot3f.setColumn(2, tw);
		
		m_cameraMatrix.set(rot3f, m_centerOfProjection, 1.0f);
		m_cameraMatrix.invert();
	}
}
