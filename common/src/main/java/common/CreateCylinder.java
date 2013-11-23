package common;

import jrtr.RenderContext;
import jrtr.VertexData;

public class CreateCylinder {
	
	private VertexData vertexData;
	
	public CreateCylinder(int slices, float zylLen, float zylRadius, float zylOffset, RenderContext renderContext)
	{
		
		// variables needed to build the object
		float zylOffset1 = zylOffset;
		int objWidth = slices*1;
		int objHeight = slices*1;
		int dim3 = 3;
		double radiance = 2*Math.PI/objWidth;
		
		// additional points for the two top and bottom 
		int addPoints = 2;
		
		// arrays to store the 3d information of the object
		float v3d[][] = new float[objWidth*objHeight+addPoints][dim3];
		float v[] = new float[3*(objWidth*objHeight+addPoints)];
		float c[] = new float[3*(objWidth*objHeight+addPoints)];
		int[] indices = new int[3*(objWidth*objHeight)*2];
		float[] n = new float[3*(objWidth*objHeight+addPoints)];
		int k = 0;
		
		// create the object ( bending a square mesh around the object )
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{	
				// math coordinates
				v3d[row * objWidth + column][0] = zylRadius*(float)Math.cos(column*radiance);
				v3d[row * objWidth + column][1] = zylRadius*(float)Math.sin(column*radiance);
				v3d[row * objWidth + column][2] = zylOffset1;
				
				// convert the xyz matrix into an vector
				for(int dimension = 0; dimension < dim3; dimension++)
				{
					v[dimension+column*dim3+row*objWidth*dim3]=v3d[row * objWidth + column][dimension];
				}
				
				float normFactor = (float)Math.sqrt((Math.pow(v[column*dim3+row*objWidth*dim3 + 0],2)+Math.pow(v[column*dim3+row*objWidth*dim3 + 1],2)));
				n[column*dim3+row*objWidth*dim3 + 0]=v[column*dim3+row*objWidth*dim3 + 0]/normFactor;
				n[column*dim3+row*objWidth*dim3 + 1]=v[column*dim3+row*objWidth*dim3 + 1]/normFactor;
				n[column*dim3+row*objWidth*dim3 + 2]=0;
				
				
				// make some "random" colors
				c[column*dim3+row*objWidth*dim3 + 0] = column%2;
				c[column*dim3+row*objWidth*dim3 + 1] = 1;
				c[column*dim3+row*objWidth*dim3 + 2] = row%3;
				
				// create the indices vector (make sure the last line is connected to the first)
				if(row < objHeight -1)
				{
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + row * objWidth;
					indices[k++] = (column + 1)%objWidth + (row + 1)*objWidth;
					
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + (row + 1)*objWidth;
					indices[k++] = column + row * objWidth + objWidth;
				}
				
			}
			// after each column in the same row increase the row height
			zylOffset1 += zylLen/objHeight;
		}
		
		// add the top and bottom of the cylinder
		v3d[objWidth*objHeight][0] = 0.0f;
		v3d[objWidth*objHeight][1] = 0.0f;
		v3d[objWidth*objHeight][2] = zylOffset;
		v3d[objWidth*objHeight + addPoints - 1][0] = 0;
		v3d[objWidth*objHeight + addPoints - 1][1] = 0;
		v3d[objWidth*objHeight + addPoints - 1][2] = zylOffset+zylLen;			

		// convert the xyz matrix into an vector
		for(int addPts = 0; addPts < addPoints; addPts++)
		{
			for(int dimension = 0; dimension < dim3; dimension++)
			{
				v[objWidth * objHeight * dim3 + addPts * dim3 + dimension]=v3d[objWidth * objHeight + addPts][dimension];
			}

		}
		n[objWidth * objHeight * dim3 + 0 * dim3 + 0]= 0.0f;
		n[objWidth * objHeight * dim3 + 0 * dim3 + 1]= 0.0f;
		n[objWidth * objHeight * dim3 + 0 * dim3 + 2]=-0.1f;
		
		n[objWidth * objHeight * dim3 + 1 * dim3 + 0]= 0.0f;
		n[objWidth * objHeight * dim3 + 1 * dim3 + 1]= 0.0f;
		n[objWidth * objHeight * dim3 + 1 * dim3 + 2]= 0.1f;
		
		// create the indices vector (make sure the last line is connected to the first)
		for(int column = 0; column < objWidth; column++)
		{
			indices[k++] = column;
			indices[k++] = (column + 1)%objWidth;
			indices[k++] = objWidth * objHeight;
			
			indices[k++] = objWidth * (objHeight - 1) + column;
			indices[k++] = (column + 1)%objWidth+(objWidth * (objHeight - 1));
			indices[k++] = objWidth * objHeight + 1;
		}	
		float uv[] = new float[2*(objWidth*objHeight+addPoints)];
		
		float deltaX = 1.0f/objWidth;
		float deltaY = 1.0f/objHeight;
		float xCoords = 0.0f;
		float yCoords = 0.0f;
		
		k = 0;
		
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{
				uv[k++] = yCoords;
				uv[k++] = xCoords%1;
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
