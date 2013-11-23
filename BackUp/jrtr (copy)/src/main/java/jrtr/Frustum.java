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
	private float m_aspectRatio;
	private float m_nearPlane;
	private float m_farPlane;
	private float m_FOV;
	
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
		return this.m_aspectRatio;
	}
	
	public float getNearPlane()
	{
		return this.m_nearPlane;
	}
	
	public float getFarPlane()
	{
		return this.m_farPlane;
	}
	
	public float getFOV()
	{
		return this.m_FOV;
	}
	
	public void setAspectRatio(float aspectRatio)
	{
		this.m_aspectRatio = aspectRatio;
		setProjectionMatrix();
	}
	
	public void setNearPlane(float nearPlane)
	{
		this.m_nearPlane = nearPlane;
		setProjectionMatrix();
	}
	
	public void setFarPlane(float farPlane)
	{
		this.m_farPlane = farPlane;
		setProjectionMatrix();
	}
	
	public void setFOV(float fov)
	{
		this.m_FOV = fov;
		setProjectionMatrix();
	}
	
	private void setProjectionMatrix()
	{
		this.m_projectionMatrix = new Matrix4f();
		this.m_projectionMatrix.m00 = (float)(1/(this.m_aspectRatio*Math.tan(this.m_FOV/Math.PI*90)));
		this.m_projectionMatrix.m11 = (float)(1/(Math.tan(this.m_FOV/Math.PI*90)));
		this.m_projectionMatrix.m22 = (m_nearPlane+m_farPlane)/(m_nearPlane-m_farPlane);
		this.m_projectionMatrix.m23 = (2*m_nearPlane*m_farPlane)/(m_nearPlane-m_farPlane);
		this.m_projectionMatrix.m32 = -1;
	}
	
	
}
