package torusGloss;

import javax.swing.*;

import java.awt.event.*;

import javax.vecmath.*;

import jrtr.*;

import java.util.Timer;
import java.util.TimerTask;

import common.*;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class torusGloss
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
			
			CreateTorus torus = new CreateTorus(32, 32, 3.0f, 2.0f, r);
			VertexData vertexData = torus.getVertexData();
								
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
		    	diffuseShader.load("../jrtr/shaders/gloss.vert", "../jrtr/shaders/gloss.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
			material.shininess = 32.0f;
			material.texture = renderContext.makeTexture();
			
			try {
				material.texture.load("../textures/colorGloss.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			material.map = renderContext.makeTexture();
			try {
				material.map.load("../textures/mapGloss.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load map.\n");
				System.out.print(e.getMessage());
			}
			
			Light firstLight = new Light();
			firstLight.diffuse = new Vector3f(0.0f, 0.0f,32.0f);
			firstLight.position = new Vector3f(-1.0f, -1.0f, -1.0f);
			firstLight.direction = new Vector3f(1.0f, 1.0f, 1.0f);
			firstLight.ambient = new Vector3f(0.1f, 0.1f, 0.1f);
			firstLight.specular = new Vector3f(1.0f, 1.0f, 1.0f);
			
			Light secondLight = new Light();
			secondLight.diffuse = new Vector3f(0.0f, 54.0f, 0.0f);
			secondLight.position = new Vector3f(-6.0f, 6.0f, 6.0f);
			secondLight.direction = new Vector3f(1.0f, 1.0f, 1.0f);
			secondLight.ambient = new Vector3f(0.1f, 0.1f, 0.1f);
			secondLight.specular = new Vector3f(1.0f, 1.0f, 1.0f);
			
			Light thirdLight = new Light();
			thirdLight.diffuse = new Vector3f(32.0f, 0.0f, 0.0f);
			thirdLight.position = new Vector3f(1.0f, -1.0f, 1.0f);
			thirdLight.direction = new Vector3f(1.0f, 1.0f, 1.0f);
			thirdLight.ambient = new Vector3f(0.1f, 0.1f, 0.1f);
			thirdLight.specular = new Vector3f(1.0f, 1.0f, 1.0f);
			
			sceneManager.addLight(firstLight);
			sceneManager.addLight(secondLight);
			sceneManager.addLight(thirdLight);

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
	    renderPanel.getCanvas().addMouseWheelListener(new TorusMouseWheelListener());
	    renderPanel.getCanvas().addKeyListener(new TorusKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
