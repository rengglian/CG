package common;

import jrtr.RenderContext;
import jrtr.VertexData;

public class CreateBlock {
	
	private VertexData vertexData;
	
	public CreateBlock(float width, float height, float depth, RenderContext renderContext)
	{	
		// The vertex positions of the cube
		float v[] = {-width/2,-height/2,depth/2,	width/2,-height/2,depth/2,		width/2,height/2,depth/2,		-width/2,height/2,depth/2,		// front face
			         -width/2,-height/2,-depth/2,	-width/2,-height/2,depth/2,		-width/2,height/2,depth/2,		-width/2,height/2,-depth/2,	// left face
			         width/2,-height/2,-depth/2,	-width/2,-height/2,-depth/2,	-width/2,height/2,-depth/2,		width/2,height/2,-depth/2,		// back face
			         width/2,-height/2,depth/2,		width/2,-height/2,-depth/2,		width/2,height/2,-depth/2,		width/2,height/2,depth/2,		// right face
			         width/2,height/2,depth/2,		width/2,height/2,-depth/2,		-width/2,height/2,-depth/2,		-width/2,height/2,depth/2,		// top face
					-width/2,-height/2,depth/2,		-width/2,-height/2,-depth/2,	width/2,-height/2,-depth/2,		width/2,-height/2,depth/2};	// bottom face
		
		// The vertex normals 
		float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
			         -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
				  	 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
					 1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
					 0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
					 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

		// The vertex colors
		float c[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
				     0,1,0, 0,1,0, 0,1,0, 0,1,0,
					 1,0,0, 1,0,0, 1,0,0, 1,0,0,
					 0,1,0, 0,1,0, 0,1,0, 0,1,0,
					 0,0,1, 0,0,1, 0,0,1, 0,0,1,
					 0,0,1, 0,0,1, 0,0,1, 0,0,1};

		// Texture coordinates 
		float uv[] = {0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1};

		int indices[] = {0,2,3, 0,1,2,			// front face
						 4,6,7, 4,5,6,			// left face
						 8,10,11, 8,9,10,		// back face
						 12,14,15, 12,13,14,	// right face
						 16,18,19, 16,17,18,	// top face
						 20,22,23, 20,21,22};	// bottom face
		
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		// The triangles (three vertex indices for each triangle)		
		vertexData = renderContext.makeVertexData(24);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		vertexData.addIndices(indices);
	
	}	
	
	public VertexData getVertexData()
	{
		return vertexData;
	}

}
