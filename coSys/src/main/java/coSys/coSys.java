package coSys;

import javax.swing.*;

import java.awt.event.*;

import javax.vecmath.*;

import jrtr.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class coSys
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static float currentstep, basicstep;
	static int nrOfObjects = 4; // number of object to build the coSys
	static Shape[] shape = new Shape[nrOfObjects];	// each object has a shape
	
	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 

	// number of slices (how round the object is)
	public static VertexData createCylinder(int slices)
	{
		// variables needed to build the object
		float zylLen = 1.0f;
		float zylOffset = -zylLen/2;
		float zylRadius = 1.0f;
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
		int[] indices = new int[3*(objWidth*objHeight+addPoints)*2];
		int k = 0;
		
		// create the object ( bending a square mesh around the object )
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{	
				// math coordinates
				v3d[row * objWidth + column][0] = zylRadius*(float)Math.cos(column*radiance);
				v3d[row * objWidth + column][1] = zylRadius*(float)Math.sin(column*radiance);
				v3d[row * objWidth + column][2] = zylOffset;
				
				// convert the xyz matrix into an vector
				for(int dimension = 0; dimension < dim3; dimension++)
				{
					v[dimension+column*dim3+row*objWidth*dim3]=v3d[row * objWidth + column][dimension];
				}
				
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
			zylOffset += zylLen/objHeight;
		}
		
		// add the top and bottom of the cylinder
		v3d[objWidth*objHeight][0] = 0;
		v3d[objWidth*objHeight][1] = 0;
		v3d[objWidth*objHeight][2] = -zylLen/2;
		v3d[objWidth*objHeight + addPoints - 1][0] = 0;
		v3d[objWidth*objHeight + addPoints - 1][1] = 0;
		v3d[objWidth*objHeight + addPoints - 1][2] = zylLen/2-zylLen/objHeight;			

		// convert the xyz matrix into an vector
		for(int addPts = 0; addPts < addPoints; addPts++)
		{
			for(int dimension = 0; dimension < dim3; dimension++)
			{
				v[objWidth * objHeight * dim3 + addPts * dim3 + dimension]=v3d[objWidth * objHeight + addPts][dimension];
			}
		}
		
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
	
		
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(v.length/dim3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, dim3);
		// vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		// vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		
		vertexData.addIndices(indices);

		
		return vertexData;
	}
		
	public static VertexData createCube()
	{
		// The vertex positions of the cube
		float v[] = {-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,		// front face
			         -1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,	// left face
				  	 1,-1,-1,-1,-1,-1, -1,1,-1, 1,1,-1,		// back face
					 1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,		// right face
					 1,1,1, 1,1,-1, -1,1,-1, -1,1,1,		// top face
					-1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face
		
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
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(24);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		
		// The triangles (three vertex indices for each triangle)
		int indices[] = {0,2,3, 0,1,2,			// front face
						 4,6,7, 4,5,6,			// left face
						 8,10,11, 8,9,10,		// back face
						 12,14,15, 12,13,14,	// right face
						 16,18,19, 16,17,18,	// top face
						 20,22,23, 20,21,22};	// bottom face

		vertexData.addIndices(indices);
		
		
		return vertexData;
	}	
		
	public final static class coSysRenderPanel extends GLRenderPanel
	{
		 
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			// create all vertices 
			VertexData[] vertexData = new VertexData[nrOfObjects];
			
			vertexData[0] = createCylinder(32);	// wheel front right
			vertexData[1] = vertexData[0];		// wheel front left
			vertexData[2] = vertexData[0];		// wheel back right
			vertexData[3] = createCube();		// cube on top of the car

										
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			
			for(int i = 0; i < shape.length;i++)
			{
				if( vertexData[i] != null)
				{
					shape[i] = new Shape(vertexData[i]);
					sceneManager.addShape(shape[i]);
				}
			}
				
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
			
			// Load some more shaders
		    normalShader = renderContext.makeShader();
		    try {
		    	normalShader.load("..\\jrtr\\shaders\\normal.vert", "..\\jrtr\\shaders\\normal.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }
	
		    diffuseShader = renderContext.makeShader();
		    try {
		    	diffuseShader.load("..\\jrtr\\shaders\\diffuse.vert", "..\\jrtr\\shaders\\diffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.texture = renderContext.makeTexture();
			try {
				material.texture.load("..\\textures\\plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0;
		    currentstep = 0;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}
	}

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
			// variables needed to position the shapes 
			Matrix4f newPos = new Matrix4f();
			Matrix4f rotX = new Matrix4f();
			Matrix4f rotY = new Matrix4f();
			Matrix4f rotZ = new Matrix4f();
			Matrix4f newSize = new Matrix4f();
			Matrix4f tObj = new Matrix4f();
			
			// each shape has its own initialization (scale, position, rotation)
			float[] transX = new float[nrOfObjects];
			float[] transY = new float[nrOfObjects];
			float[] transZ = new float[nrOfObjects];
			float[][] sizeFactor = new float[nrOfObjects][3];
			float[][] rotInit = new float[nrOfObjects][3];
			
			// initial scale, position and rotation of each shape
			transX[0] = 0.0f;					// x zyl
			transX[1] = 1.5f;					// y zyl
			transX[2] = 0.0f;					// z zyl
			transX[3] = 0.0f;					// center cube

			transY[0] = 0.0f;
			transY[1] = 0.0f;
			transY[2] = 1.5f;
			transY[3] = 0.0f;

			
			transZ[0] = 1.5f;
			transZ[1] = 0.0f;
			transZ[2] = 0.0f;
			transZ[3] = 0.0f;

			
			sizeFactor[0][0] = 0.1f;
			sizeFactor[0][1] = 0.1f;
			sizeFactor[0][2] = 3.0f;
			
			sizeFactor[1][0] = 0.1f;
			sizeFactor[1][1] = 0.1f;
			sizeFactor[1][2] = 3.0f;
			
			sizeFactor[2][0] = 0.1f;
			sizeFactor[2][1] = 0.1f;
			sizeFactor[2][2] = 3.0f;
			
			sizeFactor[3][0] = 0.3f;
			sizeFactor[3][1] = 0.3f;
			sizeFactor[3][2] = 0.3f;
			
			
			rotInit[0][0] = 0.0f;
			rotInit[0][1] = 0.0f;
			rotInit[0][2] = 0.0f;
			
			rotInit[1][0] = 0.0f;
			rotInit[1][1] = (float)Math.PI/2.0f;
			rotInit[1][2] = 0.0f;
			
			rotInit[2][0] = (float)Math.PI/2.0f;
			rotInit[2][1] = 0.0f;
			rotInit[2][2] = 0.0f;
			
			rotInit[3][0] = 0.0f;
			rotInit[3][1] = 0.0f;
			rotInit[3][2] = 0.0f;
			
			//apply new position, scale and rotation each function call to all shapes 
			for(int i = 0; i < shape.length; i++)
			{
	    		if( shape[i] != null)
	    		{
	    			// get the transformation reference of the shape
	    			tObj = shape[i].getTransformation();
	    			// set it back to the origin
	    			tObj.setIdentity();

	    			// move the object to the right place
	    			newPos.setIdentity();
	    			newPos.set(new Vector3f(transX[i], transY[i] ,transZ[i]));
	    			tObj.mul(newPos);
	    			// rotate the object to its correct position
	    			rotX.rotX(rotInit[i][0]);
	    			tObj.mul(rotX);
	    			
	    			rotY.rotY(rotInit[i][1]);
	    			tObj.mul(rotY);
	    			
	    			rotZ.rotZ(rotInit[i][2]);
	    			tObj.mul(rotZ);
	    			
	    			// scale object first
	    			newSize.setIdentity();
    				newSize.m00 = sizeFactor[i][0];
	    			newSize.m11 = sizeFactor[i][1];
	    			newSize.m22 = sizeFactor[i][2];
	    			tObj.mul(newSize);
	    		}
			}
			// Update transformation by rotating with angle "currentstep"
			currentstep = 0;
    		// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class TorusMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
	}
	
	/**
	 * A key listener for the main window. Use this to process key events.
	 * Currently this provides the following controls:
	 * 's': stop animation
	 * 'p': play animation
	 * '+': accelerate rotation
	 * '-': slow down rotation
	 * 'd': default shader
	 * 'n': shader using surface normals
	 * 'm': use a material for shading
	 */
	public static class TorusKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{
				case 's': {
					// Stop animation
					basicstep = 0;

					break;
				}
				case 'p': {
					// Resume animation
					basicstep = (float)Math.PI/300.0f;
					break;
				}
				case '+': {
					// Accelerate roation
					basicstep = (float)Math.PI/300.0f;;
					break;
				}
				case '-': {
					// Slow down rotation
					basicstep = -(float)Math.PI/300.0f;;
					break;
				}
			}
			
			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		
		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
        {
        }
		
	}
	
	/**
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed 
	 * for initialization automatically. It then constructs a simple 3D scene, 
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
	
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new coSysRenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new TorusMouseListener());
	    renderPanel.getCanvas().addKeyListener(new TorusKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
		
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
