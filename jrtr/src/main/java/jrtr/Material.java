package jrtr;

import javax.vecmath.*;

/**
 * Stores the properties of a material.
 */
public class Material {

	// Material properties
	public Vector3f diffuse;
	public Vector3f specular;
	public Vector3f ambient;
	public Vector4f testColor;
	public float shininess;
	public Texture texture;
	public Texture map;
	public Shader shader;
	public boolean experimental;
	
	//Point lights have a radiance cl and a position p.
	//Objects have a diffuse reflection coefficient kd
	
	public Material()
	{
		diffuse = new Vector3f(1.f, 1.f, 1.f);
		specular = new Vector3f(1.f, 1.f, 1.f);
		ambient = new Vector3f(1.f, 1.f, 1.f);
		shininess = 1.f;
		texture = null;
		map = null;
		shader = null;
		experimental = false;
	}
}
