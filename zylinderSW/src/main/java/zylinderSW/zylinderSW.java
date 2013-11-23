package zylinderSW;

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
public class zylinderSW
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static float currentstep, basicstep;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	
	
	public final static class ZylinderRenderPanel extends SWRenderPanel
	{
		//first argument of command line as INT
		 int slices = 0;
		 // overwrite ZylinderRenderPanel to pass first argument of command line
		 public ZylinderRenderPanel(int slicesIn) {
			 slices = slicesIn;
		 }		
		 
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			slices = 4;
			// variables needed to build the object
			float zylLen = 10.0f;
			float zylOffset = -zylLen/2;
			float zylRadius = 2.0f;
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
			VertexData vertexData = renderContext.makeVertexData(v.length/dim3);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, dim3);
			//vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
			vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
			
			vertexData.addIndices(indices);
								
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			shape = new Shape(vertexData);
			sceneManager.addShape(shape);

			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
			
			// Load some more shaders
		    normalShader = renderContext.makeShader();
		    try {
		    	normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }
	
		    diffuseShader = renderContext.makeShader();
		    try {
		    	diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.texture = renderContext.makeTexture();
			try {
				material.texture.load("../textures/torino.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.01f;
		    currentstep = 0.0f;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		    shape.setMaterial(material);
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
    		// Trigger redrawing of the render window
			// Update transformation by rotating with angle "currentstep"
    		Matrix4f t = shape.getTransformation();
    		Matrix4f rotX = new Matrix4f();
    		rotX.rotX(currentstep);
    		Matrix4f rotY = new Matrix4f();
    		rotY.rotY(currentstep);
    		t.mul(rotX);
    		t.mul(rotY);
    		shape.setTransformation(t);
			
    		renderPanel.getCanvas().repaint(); 
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class ZylinderMouseListener implements MouseListener
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
	public static class ZylinderKeyListener implements KeyListener
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
					shape.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case 'd': {
					// Remove material from shape, and set "default" shader
					shape.setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(shape.getMaterial() == null) {
						shape.setMaterial(material);
					} else
					{
						shape.setMaterial(null);
						renderContext.useDefaultShader();
					}
					break;
				}
				case 'b':{
					
					if(renderContext instanceof SWRenderContext)
					{
						boolean t = ((SWRenderContext) renderContext).getDrawBox();
						((SWRenderContext) renderContext).setDrawBox(!t);
						System.out.printf("setDrawBox %b\n", !t);
					}
					break;
			}
			case 't':{
				
				if(renderContext instanceof SWRenderContext)
				{
					boolean t = ((SWRenderContext) renderContext).getTask2();
					((SWRenderContext) renderContext).setTask2(!t);
					System.out.printf("setTask2 %b\n", !t);
				}
				break;
			}
			case 'c':{
				
				if(renderContext instanceof SWRenderContext)
				{
					boolean t = ((SWRenderContext) renderContext).getBiLin();
					((SWRenderContext) renderContext).setBiLin(!t);
					System.out.printf("setBiLin %b\n", !t);
				}
				break;
			}
			case 'h':{
				
				if(renderContext instanceof SWRenderContext)
				{
					boolean t = ((SWRenderContext) renderContext).getDrawTexture();
					((SWRenderContext) renderContext).setDrawTexture(!t);
					System.out.printf("setDrawTexture %b\n", !t);
				}
				break;
			}
			case 'l':{
				
				if(renderContext instanceof SWRenderContext)
				{
					boolean t = ((SWRenderContext) renderContext).getDrawTriag();
					((SWRenderContext) renderContext).setDrawTriag(!t);
					System.out.printf("setDrawTriag %b\n", !t);
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
		renderPanel = new ZylinderRenderPanel(firstArg);
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new ZylinderMouseListener());
	    renderPanel.getCanvas().addKeyListener(new ZylinderKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
