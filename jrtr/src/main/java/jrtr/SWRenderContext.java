package jrtr;

import jrtr.RenderContext;
import jrtr.VertexData.VertexElement;

import java.awt.Color;
import java.awt.image.*;
import java.util.LinkedList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;


/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 
 * you will implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel} 
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer;
	private boolean debug = false;
	private boolean drawBox = false;
	private boolean drawTriag = true;
	private boolean drawTexture = false;
	private boolean task2 = false;
	private boolean biLin = false;
	
	private static void printMatrix(String s, Matrix4f m)
	{
		System.out.println(s);
		System.out.printf("%f\t%f\t%f\t%f\n", m.m00, m.m01, m.m02, m.m03);
		System.out.printf("%f\t%f\t%f\t%f\n", m.m10, m.m11, m.m12, m.m13);
		System.out.printf("%f\t%f\t%f\t%f\n", m.m20, m.m21, m.m22, m.m23);
		System.out.printf("%f\t%f\t%f\t%f\n", m.m30, m.m31, m.m32, m.m33);
	}
	
	private static void printMatrix(String s, Matrix3f m)
	{
		System.out.println(s);
		System.out.printf("%f\t%f\t%f\n", m.m00, m.m01, m.m02);
		System.out.printf("%f\t%f\t%f\n", m.m10, m.m11, m.m12);
		System.out.printf("%f\t%f\t%f\n", m.m20, m.m21, m.m22);
	}
	
	public void setDrawBox(boolean t)
	{
		drawBox = t;
	}
	public boolean getDrawBox() {return drawBox;}
	
	public void setTask2(boolean t)
	{
		task2 = t;
	}
	public boolean getTask2() {return task2;}
	
	public void setBiLin(boolean t)
	{
		biLin = t;
	}
	public boolean getBiLin() {return biLin;}
	
	public void setDrawTexture(boolean t)
	{
		drawTexture = t;
	}
	public boolean getDrawTexture() {return drawTexture;}
	
	public void setDrawTriag(boolean t)
	{
		drawTriag = t;
	}
	public boolean getDrawTriag() {return drawTriag;}
		
	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;
	}
	
	/**
	 * This is called by the SWRenderPanel to render the scene to the 
	 * software frame buffer.
	 */
	public void display()
	{
		if(sceneManager == null) return;
		
		beginFrame();
	
		SceneManagerIterator iterator = sceneManager.iterator();	
		
		BufferedImage tImage = getColorBuffer();
		
		tImage.getHeight();
		tImage.getWidth();
		
		tImage.getGraphics().clearRect(0, 0, tImage.getWidth(), tImage.getHeight());
		
		while(iterator.hasNext())
		{
			if(!task2)draw(iterator.next());
			else draw2(iterator.next());
		}		
		
		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer()
	{
		return colorBuffer;
	}
	
	/**
	 * Set a new viewport size. The render context will also need to store
	 * a viewport matrix, which you need to reset here. 
	 */
	public void setViewportSize(int width, int height)
	{
		colorBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	}
		
	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame()
	{
	}
	
	private void endFrame()
	{		
	}
	
	/**
	 * The main rendering method. You will need to implement this to draw
	 * 3D objects.
	 */
	
	private void draw(RenderItem renderItem)
	{
		Shape tShape = renderItem.getShape();
		VertexData tVertexData = tShape.getVertexData();
		LinkedList<VertexElement> elements = tVertexData.getElements();
		float[] aPositions	= new float[tVertexData.getNumberOfVertices()*3];
		float[] aColors	= new float[tVertexData.getNumberOfVertices()*3];
		float[] aNorms	= new float[tVertexData.getNumberOfVertices()*3];
		int[] 	aIntices = tVertexData.getIndices();
		
		for (VertexElement el:elements)
		{
			switch(el.getSemantic())
			{
			
			case POSITION:
				{
					aPositions = el.getData();
					break;
				}
				case COLOR:
				{
					aColors = el.getData();
					break;
				}
				case NORMAL:
				{
					aNorms = el.getData();
					break;
				}
				case TEXCOORD:
				{
					break;
				}
				default:
				{
					break;
				}
			}

		}
		BufferedImage tImage = getColorBuffer();
		
		tImage.getHeight();
		tImage.getWidth();
				
		Matrix4f tProjSpace = sceneManager.getFrustum().getProjectionMatrix();
		Matrix4f tCamSpace = sceneManager.getCamera().getCameraMatrix();
		//tCamSpace.invert();
		
		Matrix4f tImageSpace = new Matrix4f();
		tImageSpace.m00 = (tImage.getWidth()/2);
		tImageSpace.m03 = (tImage.getWidth()/2);
		tImageSpace.m11 = (tImage.getHeight()/2);
		tImageSpace.m13 = (tImage.getHeight()/2);
		tImageSpace.m22 = (1.0f/2.0f);
		tImageSpace.m23 = (1.0f/2.0f);
		tImageSpace.m33 = (1.0f);
		
		Matrix4f tTransform = new Matrix4f();
		
		Matrix4f tObjSpace = renderItem.getT();
		tTransform.mul(tImageSpace, tProjSpace);
		tTransform.mul(tTransform, tCamSpace);
		tTransform.mul(tTransform, tObjSpace);
		
		if(debug)printMatrix("tTransform", tTransform);
		
		int r = 255;
		int g = 255;
		int b = 255;
		int pixelColor = (r << 16) | (g << 8) | b;
		
		for(int k = 0; k < aIntices.length; k+=3)
		{
			Vector3f[] v = new Vector3f[3];
			Vector3f[] c = new Vector3f[3];
			Vector3f[] n = new Vector3f[3];
		
			
			v[0] = new Vector3f(aPositions[aIntices[k+0]*3 + 0], aPositions[aIntices[k+0]*3 + 1], aPositions[aIntices[k+0]*3 + 2]);
			v[1] = new Vector3f(aPositions[aIntices[k+1]*3 + 0], aPositions[aIntices[k+1]*3 + 1], aPositions[aIntices[k+1]*3 + 2]);
			v[2] = new Vector3f(aPositions[aIntices[k+2]*3 + 0], aPositions[aIntices[k+2]*3 + 1], aPositions[aIntices[k+2]*3 + 2]);
			
			c[0] = new Vector3f(aColors[aIntices[k+0]*3 + 0], aColors[aIntices[k+0]*3 + 1], aColors[aIntices[k+0]*3 + 2]);
			c[1] = new Vector3f(aColors[aIntices[k+1]*3 + 0], aColors[aIntices[k+1]*3 + 1], aColors[aIntices[k+1]*3 + 2]);
			c[2] = new Vector3f(aColors[aIntices[k+2]*3 + 0], aColors[aIntices[k+2]*3 + 1], aColors[aIntices[k+2]*3 + 2]);
			
			n[0] = new Vector3f(aNorms[aIntices[k+0]*3 + 0], aNorms[aIntices[k+0]*3 + 1], aNorms[aIntices[k+0]*3 + 2]);
			n[1] = new Vector3f(aNorms[aIntices[k+1]*3 + 0], aNorms[aIntices[k+1]*3 + 1], aNorms[aIntices[k+1]*3 + 2]);
			n[2] = new Vector3f(aNorms[aIntices[k+2]*3 + 0], aNorms[aIntices[k+2]*3 + 1], aNorms[aIntices[k+2]*3 + 2]);
			
			
			//if(debug)System.out.printf("v0: %d\t%d\t%d\n", aIntices[k+0]*3 + 0, aIntices[k+0]*3 + 1, aIntices[k+0]*3 + 2);
			//if(debug)System.out.printf("v1: %d\t%d\t%d\n", aIntices[k+1]*3 + 0, aIntices[k+1]*3 + 1, aIntices[k+1]*3 + 2);
			//if(debug)System.out.printf("v2: %d\t%d\t%d\n", aIntices[k+2]*3 + 0, aIntices[k+2]*3 + 1, aIntices[k+2]*3 + 2);
			//if(debug)System.out.println();
			
			Matrix4f tPointM = new Matrix4f();
			for(int l = 0; l < 3; l++)
			{
				tPointM.setIdentity();
				tPointM.setTranslation(v[l]);
				
				//if(debug)printMatrix("tPointM", tPointM);
				
				tPointM.mul(tTransform, tPointM);
				
				//if(debug)printMatrix("tPointM", tPointM);
				
				tImage.setRGB(0, 0, pixelColor);
				tImage.setRGB(0, tImage.getHeight()-1, pixelColor);
				tImage.setRGB(tImage.getWidth()-1, 0, pixelColor);
				tImage.setRGB(tImage.getWidth()-1, tImage.getHeight()-1, pixelColor);
				
				if( tPointM.m33 > 0 )
				{
					v[l].x = (tPointM.m03/tPointM.m33);
					v[l].y = (tPointM.m13/tPointM.m33);
					v[l].z = (tPointM.m23/tPointM.m33);
					//if(debug)System.out.printf("x: %d\ty: %d\n", v[l].x, v[l].y);
					if( ((int)v[l].x >= 0) && 
						((int)v[l].y > 0) && 
						((int)v[l].x < tImage.getWidth()) && 
						((int)v[l].y < tImage.getHeight()) )
					{
					    try{
					    		tImage.setRGB((int)v[l].x, tImage.getHeight()-(int)v[l].y, pixelColor);
					    }catch (ArrayIndexOutOfBoundsException e){
					    	System.out.println(e.toString());
					    }  
						
					}
				}

			}
		}
		
	}

	
	private Vector4f boundingBox(Vector3f[] v)
	{
	
		BufferedImage tImage = getColorBuffer();
		
		tImage.getHeight();
		tImage.getWidth();
		
		Vector4f tBbox = new Vector4f(); //tBbox.w = minx, tBbox.x = maxx, tBbox.y = miny, tBbox.z = maxy
		
		if(v[0].z < 0 && v[1].z < 0 && v[2].z < 0)
		{
			if(tBbox.w < 0) tBbox.w= 0.0f;
			if(tBbox.x < 0) tBbox.x= 0.0f;
			if(tBbox.y < 0) tBbox.y= 1.0f;
			if(tBbox.z < 0) tBbox.z= 1.0f;
		}
		else
		{

				if(v[0].z > 0 && v[1].z > 0 && v[2].z > 0)
				{
					tBbox.w = v[0].x/v[0].z; //x min
					tBbox.x = v[0].x/v[0].z; //x max
					tBbox.y = v[0].y/v[0].z; //y min
					tBbox.z = v[0].y/v[0].z; //y max
					
					for(int k = 1; k < 3; k++)
					{
						if(v[k].x/v[k].z < tBbox.w) tBbox.w=v[k].x/v[k].z;
						if(v[k].x/v[k].z > tBbox.x) tBbox.x=v[k].x/v[k].z;
						if(v[k].y/v[k].z < tBbox.y) tBbox.y=v[k].y/v[k].z;
						if(v[k].y/v[k].z > tBbox.z) tBbox.z=v[k].y/v[k].z;
					}
				}
				
				if(tBbox.w < 0) tBbox.w= 0.0f;
				if(tBbox.x < 0) tBbox.x= 0.0f;
				if(tBbox.y < 0) tBbox.y= 1.0f;
				if(tBbox.z < 0) tBbox.z= 1.0f;
			
				if(tBbox.w > tImage.getWidth()) tBbox.w= tImage.getWidth()-1;
				if(tBbox.x > tImage.getWidth()) tBbox.x= tImage.getWidth()-1;
				if(tBbox.y > tImage.getHeight()) tBbox.y= tImage.getHeight();
				if(tBbox.z > tImage.getHeight()) tBbox.z= tImage.getHeight();
				
			//if(debug)System.out.printf("v0 x: %.3fy:\t%.3f\n",v[0].x, v[0].y);
			//if(debug)System.out.printf("v1 x: %.3fy:\t%.3f\n",v[1].x, v[1].y);
			//if(debug)System.out.printf("v2 x: %.3fy:\t%.3f\n",v[2].x, v[2].y);
			//if(debug)System.out.printf("min x: %.3f\tmin y: %.3f\n",tBbox.w, tBbox.y);
			//if(debug)System.out.printf("max x: %.3f\tmax y: %.3f\n",tBbox.x, tBbox.z);
			//if(debug)System.out.println();
		}
		return tBbox;
		
	}
	
	private Matrix3f f(Vector3f[] v)
	{
		Matrix3f fTrans = new Matrix3f();
		fTrans.m00 = v[0].x;
		fTrans.m01 = v[0].y;
		fTrans.m02 = v[0].z;
		fTrans.m10 = v[1].x;
		fTrans.m11 = v[1].y;
		fTrans.m12 = v[1].z;
		fTrans.m20 = v[2].x;
		fTrans.m21 = v[2].y;
		fTrans.m22 = v[2].z;
		
	    try{
	    	fTrans.invert();
	    }catch (SingularMatrixException e){
	    	//System.out.println(e.toString());
	    	fTrans = new Matrix3f();
	    }  
		return fTrans;
	}

	
	private void draw2(RenderItem renderItem)
	{
		Shape tShape = renderItem.getShape();
		VertexData tVertexData = tShape.getVertexData();
		LinkedList<VertexElement> elements = tVertexData.getElements();
		float[] aPositions	= new float[tVertexData.getNumberOfVertices()*3];
		float[] aColors	= new float[tVertexData.getNumberOfVertices()*3];
		float[] aNorms	= new float[tVertexData.getNumberOfVertices()*3];
		float[] aTextCoords	= new float[tVertexData.getNumberOfVertices()*2];
		int[] 	aIntices = tVertexData.getIndices();
		for (VertexElement el:elements)
		{
			switch(el.getSemantic())
			{
			
			case POSITION:
				{
					aPositions = el.getData();
					break;
				}
				case COLOR:
				{
					aColors = el.getData();
					break;
				}
				case NORMAL:
				{
					aNorms = el.getData();
					break;
				}
				case TEXCOORD:
				{
					aTextCoords = el.getData();
					break;
				}
				default:
				{
					break;
				}
			}

		}
		BufferedImage tImage = getColorBuffer();
		
		tImage.getHeight();
		tImage.getWidth();
		
		float[][] zBuf	= new float[tImage.getWidth()][tImage.getHeight()];
		
		Matrix4f tProjSpace = sceneManager.getFrustum().getProjectionMatrix();
		Matrix4f tCamSpace = sceneManager.getCamera().getCameraMatrix();
		
		Matrix4f tImageSpace = new Matrix4f();
		
		tImageSpace.m00 = (tImage.getWidth()/2);
		tImageSpace.m03 = (tImage.getWidth()/2);
		tImageSpace.m11 = (tImage.getHeight()/2);
		tImageSpace.m13 = (tImage.getHeight()/2);
		tImageSpace.m22 = (1.0f/2.0f);
		tImageSpace.m23 = (1.0f/2.0f);
		tImageSpace.m33 = (1.0f);
		
		Matrix4f tTransform = new Matrix4f();
		
		Matrix4f tObjSpace = renderItem.getT();
		tTransform.mul(tImageSpace, tProjSpace);
		tTransform.mul(tTransform, tCamSpace);
		tTransform.mul(tTransform, tObjSpace);
		
		if(debug)printMatrix("tTransform", tTransform);
		
		int r = 255;
		int g = 255;
		int b = 255;
		int pixelColor = (r << 16) | (g << 8) | b;

		for(int k = 0; k < aIntices.length; k+=3)
		{
			Vector3f[] v = new Vector3f[3];
			Vector3f[] c = new Vector3f[3];
			Vector3f[] n = new Vector3f[3];
			Vector3f[] uv = new Vector3f[3];
		
			v[0] = new Vector3f(aPositions[aIntices[k+0]*3 + 0], aPositions[aIntices[k+0]*3 + 1], aPositions[aIntices[k+0]*3 + 2]);
			v[1] = new Vector3f(aPositions[aIntices[k+1]*3 + 0], aPositions[aIntices[k+1]*3 + 1], aPositions[aIntices[k+1]*3 + 2]);
			v[2] = new Vector3f(aPositions[aIntices[k+2]*3 + 0], aPositions[aIntices[k+2]*3 + 1], aPositions[aIntices[k+2]*3 + 2]);
			
			c[0] = new Vector3f(aColors[aIntices[k+0]*3 + 0], aColors[aIntices[k+0]*3 + 1], aColors[aIntices[k+0]*3 + 2]);
			c[1] = new Vector3f(aColors[aIntices[k+1]*3 + 0], aColors[aIntices[k+1]*3 + 1], aColors[aIntices[k+1]*3 + 2]);
			c[2] = new Vector3f(aColors[aIntices[k+2]*3 + 0], aColors[aIntices[k+2]*3 + 1], aColors[aIntices[k+2]*3 + 2]);
			
			n[0] = new Vector3f(aNorms[aIntices[k+0]*3 + 0], aNorms[aIntices[k+0]*3 + 1], aNorms[aIntices[k+0]*3 + 2]);
			n[1] = new Vector3f(aNorms[aIntices[k+1]*3 + 0], aNorms[aIntices[k+1]*3 + 1], aNorms[aIntices[k+1]*3 + 2]);
			n[2] = new Vector3f(aNorms[aIntices[k+2]*3 + 0], aNorms[aIntices[k+2]*3 + 1], aNorms[aIntices[k+2]*3 + 2]);

			uv[0] = new Vector3f(aTextCoords[aIntices[k+0]*2 + 0], aTextCoords[aIntices[k+0]*2 + 1], 1.0f);
			uv[1] = new Vector3f(aTextCoords[aIntices[k+1]*2 + 0], aTextCoords[aIntices[k+1]*2 + 1], 1.0f);
			uv[2] = new Vector3f(aTextCoords[aIntices[k+2]*2 + 0], aTextCoords[aIntices[k+2]*2 + 1], 1.0f);
			
			//if(debug)System.out.printf("v0: %d\t%d\t%d\n", aIntices[k+0]*3 + 0, aIntices[k+0]*3 + 1, aIntices[k+0]*3 + 2);
			//if(debug)System.out.printf("v1: %d\t%d\t%d\n", aIntices[k+1]*3 + 0, aIntices[k+1]*3 + 1, aIntices[k+1]*3 + 2);
			//if(debug)System.out.printf("v2: %d\t%d\t%d\n", aIntices[k+2]*3 + 0, aIntices[k+2]*3 + 1, aIntices[k+2]*3 + 2);
			//if(debug)System.out.println();
			
			Matrix4f tPointM = new Matrix4f();
			for(int l = 0; l < 3; l++)
			{
				tPointM.setIdentity();
				tPointM.setTranslation(v[l]);
				
				//if(debug)printMatrix("tPointM", tPointM);
				
				tPointM.mul(tTransform, tPointM);
				
				//if(debug)printMatrix("tPointM", tPointM);

				v[l].x = (tPointM.m03);
				v[l].y = (tPointM.m13);
				v[l].z = (tPointM.m33);

			}


			Vector4f bBox =  boundingBox(v);
			
			if(drawBox)tImage.getGraphics().draw3DRect((int)bBox.w, tImage.getHeight()-(int)bBox.z, (int)(bBox.x-bBox.w), (int)(bBox.z-bBox.y), true);
			
			Matrix3f fTrans = f(v);
			for(int xCoords = (int)bBox.w; xCoords < (int)bBox.x; xCoords++)
			{
				for(int yCoords = (int)bBox.y; yCoords < (int)bBox.z; yCoords++)
				{
					float alpha_w = fTrans.m00*xCoords + fTrans.m10*yCoords + fTrans.m20;
					float beta_w = fTrans.m01*xCoords + fTrans.m11*yCoords + fTrans.m21;
					float gamma_w = fTrans.m02*xCoords + fTrans.m12*yCoords + fTrans.m22;
					
					if( (alpha_w > 0) && (beta_w > 0) && (gamma_w > 0))						
					{

						float a_ = fTrans.m00+fTrans.m01+fTrans.m02;
						float b_ = fTrans.m10+fTrans.m11+fTrans.m12;
						float c_ = fTrans.m20+fTrans.m21+fTrans.m22;
						
						float w_ =	a_*xCoords + b_*yCoords + c_;
						
						if(debug)System.out.println(alpha_w);
						if(debug)System.out.println(beta_w);
						if(debug)System.out.println(gamma_w);
						if(debug)System.out.println(w_);
						
						if( w_ > zBuf[xCoords][yCoords] && drawTriag)
						{
							if(drawTexture)
							{
								Material tMat = tShape.getMaterial();
								SWTexture tTex = null;
								if (tMat != null) {
									tTex = (SWTexture) tMat.texture;
									BufferedImage iTex =  tTex.texture;
									float fxTex = alpha_w/w_ * uv[0].x + beta_w/w_ * uv[1].x + gamma_w/w_ * uv[2].x;
									float fyTex = 1-(alpha_w/w_ * uv[0].y + beta_w/w_ * uv[1].y + gamma_w/w_ * uv[2].y);
									
									float iHeight = (float)iTex.getHeight();
									float iWidth = (float)iTex.getWidth();
									
									float xTex = (iWidth-1)*fxTex;
									float yTex = (iHeight-1)*fyTex;
									Color col = new Color(pixelColor);
									if(biLin)
									{
										int iColor1 = iTex.getRGB((int)(xTex), (int)(yTex));
										Color col1 = new Color(iColor1, true);
										int iColor2 = iTex.getRGB((int)Math.ceil(xTex), (int)(yTex));
										Color col2 = new Color(iColor2, true);
										int iColor3 = iTex.getRGB((int)(xTex), (int)Math.ceil(yTex));
										Color col3 = new Color(iColor3, true);
										int iColor4 = iTex.getRGB((int)Math.ceil(xTex), (int)Math.ceil(yTex));
										Color col4 = new Color(iColor4, true);
										
										float lowPixel = ((float)col1.getRed())*((int)Math.ceil(xTex)-xTex) + ((float)col2.getRed())*(xTex-(int)(xTex));
										float upPixel = ((float)col3.getRed())*((int)Math.ceil(xTex)-xTex) + ((float)col4.getRed())*(xTex-(int)(xTex));
										
										r = (int)((lowPixel*((int)Math.ceil(yTex)-yTex) + upPixel*(yTex-(int)(yTex))));
										
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",((int)Math.ceil(xTex)-xTex), (xTex-(int)(xTex)));
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",((int)Math.ceil(yTex)-yTex), (yTex-(int)(yTex)));
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",(float)((int)Math.ceil(xTex)-xTex), (xTex-(int)(xTex)));
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",(float)((int)Math.ceil(yTex)-yTex), (yTex-(int)(yTex)));
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",(float)col1.getRed(), (float)col2.getRed());
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",(float)col3.getRed(), (float)col4.getRed());
										if(debug)System.out.printf("x: %.3f\ty: %.3f\n",(float)lowPixel, (float)upPixel);
										if(debug)System.out.printf("x: %.3f\n",(float)r);
										
										lowPixel = ((float)col1.getGreen())*((int)Math.ceil(xTex)-xTex) + ((float)col2.getGreen())*(xTex-(int)(xTex));
										upPixel = ((float)col3.getGreen())*((int)Math.ceil(xTex)-xTex) + ((float)col4.getGreen())*(xTex-(int)(xTex));
										
										g = (int)((lowPixel*((int)Math.ceil(yTex)-yTex) + upPixel*(yTex-(int)(yTex))));
										
										lowPixel = ((float)col1.getBlue())*((int)Math.ceil(xTex)-xTex) + ((float)col2.getBlue())*(xTex-(int)(xTex));
										upPixel = ((float)col3.getBlue())*((int)Math.ceil(xTex)-xTex) + ((float)col4.getBlue())*(xTex-(int)(xTex));
					
										b = (int)((lowPixel*((int)Math.ceil(yTex)-yTex) + upPixel*(yTex-(int)(yTex))));
					

										
									}else
									{
										int iColor = iTex.getRGB(Math.round(xTex), Math.round(yTex));
										col = new Color(iColor, true);
										r = col.getRed();
										g = col.getGreen();
										b = col.getBlue();
									}
									

									
									if(debug)printMatrix("fTrans", fTrans);
								}
							}else
							{
								Vector3f colVect = new Vector3f(c[0].x, c[1].x, c[2].x);
								fTrans.transform(colVect);						
								r = (int) ((colVect.x*xCoords+colVect.y*yCoords+colVect.z)*255.0f/w_);
								
								colVect = new Vector3f(c[0].y, c[1].y, c[2].y);

								fTrans.transform(colVect);
								g = (int) ((colVect.x*xCoords+colVect.y*yCoords+colVect.z)*255.0f/w_);
								
								colVect = new Vector3f(c[0].z, c[1].z, c[2].z);
								fTrans.transform(colVect);
								b = (int) ((colVect.x*xCoords+colVect.y*yCoords+colVect.z)*255.0f/w_);
								

							}

							pixelColor = (r << 16) | (g << 8) | b;
							zBuf[xCoords][yCoords] = w_;							
							try{
								tImage.setRGB(xCoords, tImage.getHeight()-yCoords, pixelColor);
							}catch (ArrayIndexOutOfBoundsException e){
								System.out.println(e.toString());
							}  							
						}						
					}
				}
			}
		}
	}
	
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader()	
	{
		return new SWShader();
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useShader(Shader s)
	{
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useDefaultShader()
	{
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture()
	{
		return new SWTexture();
	}
	
	public VertexData makeVertexData(int n)
	{
		return new SWVertexData(n);		
	}
}
