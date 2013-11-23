package torusSW;

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
public class torusSW
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
	
	
	
	public final static class TorusRenderPanel extends SWRenderPanel
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
			
			slices = 32;
			
			// variables needed to build the object
			float torRadius = 2.0f;
			float torTube = 1.0f;
			int objWidth = slices*1;
			int objHeight = slices*1;
			int dim3 = 3;
			double theta = 2*Math.PI/objHeight;
			double phi = 2*Math.PI/objWidth;
			
			// arrays to store the 3d information of the object
			float v3d[][] = new float[objWidth*objHeight][dim3];
			float v[] = new float[3*(objWidth*objHeight)];
			float c[] = new float[3*(objWidth*objHeight)];
			int[] indices = new int[3*(objWidth*objHeight)*2];
			int k = 0;
			
			// create the object ( bending a square mesh around the object )
			for(int row = 0; row < objHeight; row++)
			{
				for(int column = 0; column < objWidth; column++)
				{
					// math coordinates
					v3d[row * objWidth + column][0] = (torRadius + torTube*(float)Math.cos(column*phi))*(float)Math.cos(row*theta);
					v3d[row * objWidth + column][1] = (torRadius + torTube*(float)Math.cos(column*phi))*(float)Math.sin(row*theta);
					v3d[row * objWidth + column][2] = torTube*(float)Math.sin(column*phi);
					
					// convert the xyz matrix into an vector
					for(int dimension = 0; dimension < dim3; dimension++)
					{
						v[dimension+column*dim3+row*objWidth*dim3]=v3d[row * objWidth + column][dimension];
					}
					
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

			Vector3f[] tFaces = new Vector3f[(objWidth)*(objHeight)*2];
			
			for(int i = 0; i<indices.length; i += 3)
			{
				tFaces[i/3] = new Vector3f();
				Vector3f p0 = new Vector3f(v3d[indices[i+0]][0],v3d[indices[i+0]][1],v3d[indices[i+0]][2]);
				Vector3f p1 = new Vector3f(v3d[indices[i+1]][0],v3d[indices[i+1]][1],v3d[indices[i+1]][2]);
				Vector3f p2 = new Vector3f(v3d[indices[i+2]][0],v3d[indices[i+2]][1],v3d[indices[i+2]][2]);
				p1.sub(p0);
				p2.sub(p0);
				tFaces[i/3].cross(p1, p2);
				tFaces[i/3].normalize();
			}
			
			//calculate normals
			float[] nVertices = new float[3*((objWidth)*(objHeight))];
			
			for(int row = 0; row < objHeight; row++)
			{
				for(int column = 0; column < objWidth; column++)
				{
					Vector3f tNorm = new Vector3f();
					if(row == 0 && column == 0) // ld corner
					{
						tNorm.add(tFaces[0], tFaces[1]);
					}
					else if(row == objWidth-1 && column == objWidth-1) // ru corner
					{
						tNorm.add(tFaces[column*row*2-2], tFaces[column*row*2-1]);
					}
					else if(row == 0 && column == objWidth-1) // rd corner
					{
						tNorm.add(tFaces[column*(2)-2]);
					}
					else if(row == objWidth-1 && column == 0) // lu corner
					{
						tNorm.add(tFaces[(row-1)*(objWidth-1)*2+1]);

					}
					else if(row == 0 && column != 0 && column != objWidth-1) //  bottom side
					{
						tNorm.add(tFaces[2*(column-1)]);
						tNorm.add(tFaces[2*column]);
						tNorm.add(tFaces[2*column+1]);
					}
					else if(row == objWidth-1 && column != 0 && column != objWidth-1) // up side
					{
						tNorm.add(tFaces[2*((row-1)*(objWidth-1))+column*2-2]);
						tNorm.add(tFaces[2*((row-1)*(objWidth-1))+column*2-1]);
						tNorm.add(tFaces[2*((row-1)*(objWidth-1))+column*2+1]);
					}
					else if(column == 0 && row != 0 && row != objWidth-1) // left side
					{
						tNorm.add(tFaces[2*(row*(objWidth-1))]);
						tNorm.add(tFaces[2*(row*(objWidth-1))+1]);
						tNorm.add(tFaces[2*(objWidth-1)-1]);
					}
					else if(column == objWidth-1 && row != 0 && row != objWidth-1) // right side
					{
						tNorm.add(tFaces[2*(row*(objWidth-1))-1]);
						tNorm.add(tFaces[2*(row*(objWidth-1))-2]);
						tNorm.add(tFaces[2*((row+1)*(objWidth-1))-2]);
					}
					else // inside
					{
						tNorm.add(tFaces[row*(objWidth-1)*2+column*2]);
						tNorm.add(tFaces[row*(objWidth-1)*2+column*2+1]);
						tNorm.add(tFaces[row*(objWidth-1)*2+column*2-2]);
						tNorm.add(tFaces[(row-1)*(objWidth-1)*2+column*2+1]);
						tNorm.add(tFaces[(row-1)*(objWidth-1)*2+column*2-1]);
						tNorm.add(tFaces[(row-1)*(objWidth-1)*2+column*2-2]);
					}
					tNorm.normalize();
					float nFact = 1f;
					nVertices[column*dim3+row*objWidth*dim3 + 0] = tNorm.x*nFact;
					nVertices[column*dim3+row*objWidth*dim3 + 1] = tNorm.y*nFact;
					nVertices[column*dim3+row*objWidth*dim3 + 2] = tNorm.z*nFact;
					//if(DEBUG)System.out.printf("V x:%5.3f\t\ty:%5.3f\t\tz:%5.3f\n", tNorm.x, tNorm.y, tNorm.z);
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
			vertexData.addElement(nVertices, VertexData.Semantic.NORMAL, 3);
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
		    	normalShader.load("../jrtr/shaders/normal_last_year.vert", "../jrtr/shaders/normal_last_year.frag");
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
				material.texture.load("../textures/torino1.jpg");
				//material.texture.load("../textures/checkerboard.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.01f;
		    currentstep = 0.0f;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		    renderContext.useShader(normalShader);
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
    		Matrix4f t = shape.getTransformation();
    		Matrix4f rotX = new Matrix4f();
    		rotX.rotX(currentstep);
    		Matrix4f rotY = new Matrix4f();
    		rotY.rotY(currentstep);
    		t.mul(rotX);
    		t.mul(rotY);
    		shape.setTransformation(t);
    		
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
	
	public static class TorusMouseWheelListener implements MouseWheelListener
	{

		public void mouseWheelMoved(MouseWheelEvent e) {
			Camera tCam = sceneManager.getCamera();
			Vector3f tCenterOfP = tCam.getCenterOfProjection();
			Vector3f tLookAt = tCam.getLookAtPoint();
			tCenterOfP.z += 3.0f/e.getUnitsToScroll();
			tLookAt.z += 3.0f/e.getUnitsToScroll();;
								
			tCam.setCenterOfProjection(tCenterOfP);
			tCam.setLookAtPoint(tLookAt);
			
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
	    renderPanel.getCanvas().addMouseWheelListener(new TorusMouseWheelListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
