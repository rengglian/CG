package common;

import jrtr.RenderContext;
import jrtr.VertexData;

public class CreateSphere {
	
	private VertexData vertexData;
	
	public CreateSphere(int slices, int circ, float torRadius, RenderContext renderContext)
	{
		
		// variables needed to build the object
		int objWidth = circ;
		int objHeight = slices;
		int dim3 = 3;
		double theta = 2*Math.PI/objHeight;
		double phi = 2*Math.PI/objWidth;
		
		// arrays to store the 3d information of the object
		float v3d[][] = new float[objWidth*objHeight][dim3];
		float v[] = new float[3*(objWidth*objHeight)];
		float c[] = new float[3*(objWidth*objHeight)];
		float n[] = new float[3*(objWidth*objHeight)];
		int[] indices = new int[3*(objWidth*objHeight)*2];
		int k = 0;
		
		// create the object ( bending a square mesh around the object )
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{
				// math coordinates
				v3d[row * objWidth + column][0] = torRadius*(float)Math.cos(column*phi)*(float)Math.cos(row*theta);
				v3d[row * objWidth + column][1] = torRadius*(float)Math.cos(column*phi)*(float)Math.sin(row*theta);
				v3d[row * objWidth + column][2] = torRadius*(float)Math.sin(column*phi);
				
				// convert the xyz matrix into an vector
				for(int dimension = 0; dimension < dim3; dimension++)
				{
					v[dimension+column*dim3+row*objWidth*dim3]=v3d[row * objWidth + column][dimension];
				}
				float normFactor = (float)Math.sqrt((Math.pow(v[column*dim3+row*objWidth*dim3 + 0],2)+Math.pow(v[column*dim3+row*objWidth*dim3 + 1],2)+Math.pow(v[column*dim3+row*objWidth*dim3 + 2],2)));
				n[column*dim3+row*objWidth*dim3 + 0]=v[column*dim3+row*objWidth*dim3 + 0]/normFactor;
				n[column*dim3+row*objWidth*dim3 + 1]=v[column*dim3+row*objWidth*dim3 + 1]/normFactor;
				n[column*dim3+row*objWidth*dim3 + 2]=v[column*dim3+row*objWidth*dim3 + 2]/normFactor;
				
				// make some "random" colors
				c[column*dim3+row*objWidth*dim3 + 0] = column%2;
				c[column*dim3+row*objWidth*dim3 + 1] = (column + 1)%2;
				c[column*dim3+row*objWidth*dim3 + 2] = row%2;
				
				// create the indices vector (make sure the last line is connected to the first)
				if(row < objHeight - 1)
				{
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + row * objWidth;
					indices[k++] = (column + 1)%objWidth + (row + 1)*objWidth;
					
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + (row + 1)*objWidth;
					indices[k++] = column + row * objWidth + objWidth;
					
				}
				else
				{
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + row * objWidth;
					indices[k++] = (column + 1)%objWidth;
					
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth;
					indices[k++] = column;	
				}
			}
		}			
		
		float uv[] = new float[2*(objWidth*objHeight)];
		
		float deltaX = 1.0f/objWidth;
		float deltaY = 1.0f/objHeight;
		float xCoords = 0.0f;
		float yCoords = 0.0f;
		
		k = 0;
		
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{
				uv[k++] = 1-yCoords;
				uv[k++] = (xCoords)%1;
				
				xCoords += deltaX;
			}
			yCoords += deltaY;
		}
		
		
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		vertexData = renderContext.makeVertexData(v.length/dim3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, dim3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		vertexData.addIndices(indices);
	}	
	
	public VertexData getVertexData()
	{
		return vertexData;
	}

}
