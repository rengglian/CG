package car;

import jrtr.*;

import javax.swing.*;

import java.awt.event.*;

import javax.vecmath.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class car
{	
	static int nrOfObjects = 7;
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape[] shape = new Shape[nrOfObjects];
	static float currentstep, basicstep;
	static int oscillation = 0;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 

	public static VertexData createCylinder(int slices)
	{
		float zylLen = 1.0f;
		float zylOffset = -zylLen/2;
		float zylRadius = 1.0f;
		int objWidth = slices*1;
		int objHeight = slices*1;
		
		int addPoints = 2;
		int dim3 = 3;
		double radiance = 2*Math.PI/objWidth;
		
		float v3d[][] = new float[objWidth*objHeight+addPoints][dim3];
		float v[] = new float[3*(objWidth*objHeight+addPoints)];
		float c[] = new float[3*(objWidth*objHeight+addPoints)];
		int[] indices = new int[3*(objWidth*objHeight+addPoints)*2];
		int k = 0;
		
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{
				v3d[row * objWidth + column][0] = zylRadius*(float)Math.cos(column*radiance);
				v3d[row * objWidth + column][1] = zylRadius*(float)Math.sin(column*radiance);
				v3d[row * objWidth + column][2] = zylOffset;
				
				for(int dimension = 0; dimension < dim3; dimension++)
				{
					v[dimension+column*dim3+row*objWidth*dim3]=v3d[row * objWidth + column][dimension];
				}
				
				c[column*dim3+row*objWidth*dim3 + 0] = column%2;
				c[column*dim3+row*objWidth*dim3 + 1] = 1;
				c[column*dim3+row*objWidth*dim3 + 2] = row%3;
				
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
			zylOffset += zylLen/objHeight;
		}
		
		v3d[objWidth*objHeight][0] = 0;
		v3d[objWidth*objHeight][1] = 0;
		v3d[objWidth*objHeight][2] = -zylLen/2;
		v3d[objWidth*objHeight + addPoints - 1][0] = 0;
		v3d[objWidth*objHeight + addPoints - 1][1] = 0;
		v3d[objWidth*objHeight + addPoints - 1][2] = zylLen/2-zylLen/objHeight;			

		for(int addPts = 0; addPts < addPoints; addPts++)
		{
			for(int dimension = 0; dimension < dim3; dimension++)
			{
				v[objWidth * objHeight * dim3 + addPts * dim3 + dimension]=v3d[objWidth * objHeight + addPts][dimension];
			}
		}
		
		
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
		//vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		//vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		
		vertexData.addIndices(indices);

		
		return vertexData;
	}
	
	public static VertexData createTorus(int slices)
	{
		float torRadius = 1.0f;
		float torTube = 0.5f;
		int torWidth = slices*1;
		int torHeight = slices*1;
		
		int dim3 = 3;
		double theta = 360.0/torHeight*Math.PI/180.0;
		double phi = 360.0/torWidth*Math.PI/180.0;

		float v3d[][] = new float[torWidth*torHeight][dim3];
		float v[] = new float[3*(torWidth*torHeight)];
		float c[] = new float[3*(torWidth*torHeight)];
		int[] indices = new int[3*(torWidth*torHeight)*2];
		int k = 0;
		
		for(int row = 0; row < torHeight; row++)
		{
			for(int column = 0; column < torWidth; column++)
			{
				v3d[row * torWidth + column][0] = (torRadius + torTube*(float)Math.cos(column*phi))*(float)Math.cos(row*theta);
				v3d[row * torWidth + column][1] = (torRadius + torTube*(float)Math.cos(column*phi))*(float)Math.sin(row*theta);
				v3d[row * torWidth + column][2] = torTube*(float)Math.sin(column*phi);
				
				for(int dimension = 0; dimension < dim3; dimension++)
				{
					v[dimension+column*dim3+row*torWidth*dim3]=v3d[row * torWidth + column][dimension];
				}
				
				c[column*dim3+row*torWidth*dim3 + 0] = 0.1f*row%1;
				c[column*dim3+row*torWidth*dim3 + 1] = 0.1f*column%1;
				c[column*dim3+row*torWidth*dim3 + 2] = 0.1f;
				
				if(row < torHeight - 1)
				{
					indices[k++] = column + row * torWidth;
					indices[k++] = (column + 1)%torWidth + row * torWidth;
					indices[k++] = (column + 1)%torWidth + (row + 1)*torWidth;
					
					indices[k++] = column + row * torWidth;
					indices[k++] = (column + 1)%torWidth + (row + 1)*torWidth;
					indices[k++] = column + row * torWidth + torWidth;
					
				}
				else
				{
					indices[k++] = column + row * torWidth;
					indices[k++] = (column + 1)%torWidth + row * torWidth;
					indices[k++] = (column + 1)%torWidth;
					
					indices[k++] = column + row * torWidth;
					indices[k++] = (column + 1)%torWidth;
					indices[k++] = column;	
				}
			}
		}					
		
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(v.length/dim3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, dim3);
		//vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		//vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		
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
	
	public static void arrangeObj()
	{
		Matrix4f tShape = new Matrix4f();
		Matrix4f newPos = new Matrix4f();
		Matrix4f rotObj = new Matrix4f();
		Matrix4f scaleObj = new Matrix4f();
		
		rotObj.rotY((float)Math.PI/2);
		tShape = shape[0].getTransformation();
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(1.5f,0.0f,2.0f));
		tShape.mul(newPos, rotObj);
		shape[0].setTransformation(tShape);
		
		tShape = shape[1].getTransformation();
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(1.5f,0.0f,-2.0f));
		tShape.mul(newPos, rotObj);
		shape[1].setTransformation(tShape);
		
		tShape = shape[2].getTransformation();
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(-1.5f,0.0f,2.0f));
		tShape.mul(newPos, rotObj);
		shape[2].setTransformation(tShape);
		
		tShape = shape[3].getTransformation();
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(-1.5f,0.0f,-2.0f));
		tShape.mul(newPos,rotObj);
		shape[3].setTransformation(tShape);	
				
		tShape = shape[4].getTransformation();
		scaleObj.setIdentity();
		scaleObj.m00 = 4.0f;
		scaleObj.m11 = 0.5f;
		tShape.mul(scaleObj);
		rotObj.rotY((float)Math.PI/2);
		tShape.mul(rotObj);
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(1.5f,0.0f,0.0f));
		tShape.mul(newPos);
		shape[4].setTransformation(tShape);
		
		tShape = shape[5].getTransformation();
		scaleObj.setIdentity();
		scaleObj.m00 = 4.0f;
		scaleObj.m11 = 0.5f;
		tShape.mul(scaleObj);
		rotObj.rotY((float)Math.PI/2);
		tShape.mul(rotObj);
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(-1.5f,0.0f,0.0f));
		tShape.mul(newPos);
		shape[5].setTransformation(tShape);

		tShape = shape[6].getTransformation();
		scaleObj.setIdentity();
		scaleObj.m22 = 2.0f;
		tShape.mul(scaleObj);
		rotObj.rotY((float)Math.PI/2);
		tShape.mul(rotObj);
		newPos.setIdentity();
		newPos.setTranslation(new Vector3f(0.0f,0.0f,0.0f));
		tShape.mul(newPos);
		shape[6].setTransformation(tShape);
		
	}
	
	public final static class TorusRenderPanel extends GLRenderPanel
	{
		//first argument of command line as INT
		 int slices;
		 // overwrite ZylinderRenderPanel to pass first argument of command line
		 public TorusRenderPanel(int slices) {
			 this.slices = slices;
		 }		
		 
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
			
			vertexData[0] = createTorus(32); //wheel front right
			vertexData[1] = vertexData[0];	//wheel front left
			vertexData[2] = vertexData[0];	//wheel back right
			vertexData[3] = vertexData[0];	//wheel back left
			vertexData[4] = createCylinder(32);	// body of the car
			vertexData[5] = vertexData[4];	// body of the car
			vertexData[6] = createCube();	//cube melted the body of the car

										
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
		
			arrangeObj();
			
			
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
		    basicstep = 0.01f;
		    currentstep = basicstep;
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
			// Update transformation by rotating with angle "currentstep"

			for(int i = 0; i < shape.length; i++)
			{
	    		if( shape[i] != null)
	    		{
					Matrix4f tObj = shape[i].getTransformation();
		    		Matrix4f basicMove = new Matrix4f();

		    		
		    		if( i <= 5)
		    		{
		    			basicMove.rotZ(-currentstep);
		    			tObj.mul(basicMove);
		    		}
		    		if (i == 6)
		    		{
		    			basicMove.setIdentity();
		    			basicMove.setTranslation(new Vector3f(0.0f,0.01f*(float)Math.cos(Math.PI*oscillation++/100), 0.0f));
		    			tObj.mul(basicMove);
		    		}
		    		
		    		shape[i].setTransformation(tObj);		    			
	    		}
			}
    		
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
					currentstep = 0;
					break;
				}
				case 'p': {
					// Resume animation
					currentstep = basicstep;
					break;
				}
				case '+': {
					// Accelerate roation
					currentstep += basicstep;
					break;
				}
				case '-': {
					// Slow down rotation
					currentstep -= basicstep;
					break;
				}
				case 'n': {
					// Remove material from shape, and set "normal" shader
					shape[0].setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case 'd': {
					// Remove material from shape, and set "default" shader
					shape[0].setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(shape[0].getMaterial() == null) {
						shape[0].setMaterial(material);
					} else
					{
						shape[0].setMaterial(null);
						renderContext.useDefaultShader();
					}
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
		// check args. If there is at least one, try to pars it as INT
		int firstArg = 6; // if there is no argument, use 6 faces. 
		if (args.length > 0) {
		    // try to parse the first argument to int
			try {
		        firstArg = Integer.parseInt(args[0]);
		    } catch (NumberFormatException e) {
		    	// output error message
		        System.err.println("First Argument" + " has to be an INT (e.g. 6");
		        System.exit(1);
		    }
		}
		
	
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new TorusRenderPanel(firstArg);
		
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

