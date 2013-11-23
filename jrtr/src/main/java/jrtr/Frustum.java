package jrtr;

import javax.vecmath.Matrix4f;

/**
 * Stores the specification of a viewing frustum, or a viewing
 * volume. The viewing frustum is represented by a 4x4 projection
 * matrix. You will extend this class to construct the projection 
 * matrix from intuitive parameters.
 * <p>
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a frustum.
 */
public class Frustum {

	private Matrix4f m_projectionMatrix;
	private static float m_aspectRatio;
	private static float m_nearPlane;
	private static float m_farPlane;
	private static float m_FOV;
	
	/**
	 * Construct a default viewing frustum. The frustum is given by a 
	 * default 4x4 projection matrix.
	 */
	public Frustum()
	{
		m_projectionMatrix = new Matrix4f();
		m_aspectRatio = 1.0f;
		m_nearPlane = 1.0f;
		m_farPlane = 100.0f;
		m_FOV = 60.0f;
		setProjectionMatrix();
	}
	
	/**
	 * Return the 4x4 projection matrix, which is used for example by 
	 * the renderer.
	 * 
	 * @return the 4x4 projection matrix
	 */
	public Matrix4f getProjectionMatrix()
	{
		return m_projectionMatrix;
	}
	
	public float getAspectRatio()
	{
		return m_aspectRatio;
	}
	
	public float getNearPlane()
	{
		return m_nearPlane;
	}
	
	public float getFarPlane()
	{
		return m_farPlane;
	}
	
	public float getFOV()
	{
		return m_FOV;
	}
	
	public void setAspectRatio(float aspectRatio)
	{
		m_aspectRatio = aspectRatio;
		setProjectionMatrix();
	}
	
	public void setNearPlane(float nearPlane)
	{
		m_nearPlane = nearPlane;
		setProjectionMatrix();
	}
	
	public void setFarPlane(float farPlane)
	{
		m_farPlane = farPlane;
		setProjectionMatrix();
	}
	
	public void setFOV(float fov)
	{
		m_FOV = fov;
		setProjectionMatrix();
	}
	
	public void print()
	{
		System.out.printf("\nAR:\t%3.3f\n", m_aspectRatio);
		System.out.printf("NP:\t%3.3f\n", m_nearPlane);
		System.out.printf("FP:\t%3.3f\n", m_farPlane);
		System.out.printf("FoV:\t%3.3f\n", m_FOV);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_projectionMatrix.m00, m_projectionMatrix.m01, m_projectionMatrix.m02, m_projectionMatrix.m03);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_projectionMatrix.m10, m_projectionMatrix.m11, m_projectionMatrix.m12, m_projectionMatrix.m13);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_projectionMatrix.m20, m_projectionMatrix.m21, m_projectionMatrix.m22, m_projectionMatrix.m23);
		System.out.printf("\t%3.3f\t%3.3f\t%3.3f\t%3.3f\n", m_projectionMatrix.m30, m_projectionMatrix.m31, m_projectionMatrix.m32, m_projectionMatrix.m33);
	}
	
	private void setProjectionMatrix()
	{
		this.m_projectionMatrix = new Matrix4f();
		this.m_projectionMatrix.m00 = (float)(1/(m_aspectRatio*Math.tan(Math.toRadians(m_FOV)/2)));
		this.m_projectionMatrix.m11 = (float)(1/(Math.tan(Math.toRadians(m_FOV)/2)));
		this.m_projectionMatrix.m22 = (m_nearPlane+m_farPlane)/(m_nearPlane-m_farPlane);
		this.m_projectionMatrix.m23 = (2*m_nearPlane*m_farPlane)/(m_nearPlane-m_farPlane);
		this.m_projectionMatrix.m32 = -1;
		
	}
	
	
}
