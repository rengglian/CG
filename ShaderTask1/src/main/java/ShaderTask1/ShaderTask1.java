package ShaderTask1;

import javax.swing.*;

import java.awt.Point;
import java.awt.event.*;

import javax.vecmath.*;

import common.*;
import jrtr.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class ShaderTask1
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material0;
	static Material material1;
	static Material material2;
	static SimpleSceneManager sceneManager;
	static int nrOfObjects = 3;
	static Shape[] shape = new Shape[nrOfObjects];
	static float currentstep, basicstep;
	static Point mouseInit;
	static Point mouseDragged;
	static int frameHeigth = 500;
	static int frameWidth = 500;
	static boolean DEBUG = false; 

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	
	public final static class ShaderTask1RenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			VertexData[] vertexData = new VertexData[nrOfObjects];
			
			CreateCylinder cylinder = new CreateCylinder(32, 1.0f, 1.0f, r);
			vertexData[0] = cylinder.getVertexData();
			
			cylinder = new CreateCylinder(8, 1.0f, 1.0f, r);
			vertexData[1] = cylinder.getVertexData();

			cylinder = new CreateCylinder(4, 1.0f, 1.0f, r);
			vertexData[2] = cylinder.getVertexData();
			
			
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
			
			Frustum tFru = sceneManager.getFrustum();
			tFru.setAspectRatio(frameWidth/frameHeigth);

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
		    	diffuseShader.load("../jrtr/shaders/task1.vert", "../jrtr/shaders/task1.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			material0 = new Material();
			material1 = new Material();
			material2 = new Material();
			
			material0.shader = diffuseShader;
			material0.diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
			material0.ambient = new Vector3f(1.0f, 1.0f, 1.0f);
			material0.specular = new Vector3f(1.0f, 1.0f, 1.0f);
			material0.texture = renderContext.makeTexture();
			try {
				material0.texture.load("../textures/wood.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			material1.shader = diffuseShader;
			material1.diffuse = new Vector3f(1.0f, 0.0f, 0.0f);
			material1.ambient = new Vector3f(1.0f, 1.0f, 1.0f);
			material1.specular = new Vector3f(1.0f, 1.0f, 1.0f);
			material1.shininess = 32.0f;
			material1.texture = renderContext.makeTexture();
			material1.texture = renderContext.makeTexture();
			try {
				//material1.texture.load("../textures/torino.jpg");
				material1.texture.load("../textures/wood.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			material2.shader = diffuseShader;
			material2.diffuse = new Vector3f(0.0f, 1.0f, 0.0f);
			material2.specular = new Vector3f(1.0f, 1.0f, 1.0f);
			material2.ambient = new Vector3f(1.0f, 1.0f, 1.0f);
			material2.texture = renderContext.makeTexture();
			material2.texture = renderContext.makeTexture();
			try {
				//material2.texture.load("../textures/checkerboard.jpg");
				material2.texture.load("../textures/wood.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			Light firstLight = new Light();
			firstLight.diffuse = new Vector3f(10.0f, 10.0f, 10.0f);
			firstLight.position = new Vector3f(1.0f, 1.0f, 1.0f);
			firstLight.direction = new Vector3f(1.0f, 1.0f, 1.0f);
			firstLight.ambient = new Vector3f(1.0f, 1.0f, 1.0f);
			
			Light secondLight = new Light();
			secondLight.diffuse = new Vector3f(50.0f, 50.0f, 50.0f);
			secondLight.position = new Vector3f(1.0f, 1.0f, 1.0f);
			secondLight.direction = new Vector3f(-1.0f, 0.0f, 1.0f);
			secondLight.ambient = new Vector3f(1.0f, 1.0f, 1.0f);
			
			
			sceneManager.addLight(firstLight);
			//sceneManager.addLight(secondLight);
		
			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.0f;
		    currentstep = 0.0f;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		    shape[0].setMaterial(material0);
		    shape[1].setMaterial(material1);
		    shape[2].setMaterial(material2);
		    renderContext.useShader(material0.shader);
		    
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
			currentstep += basicstep;
			// variables needed to position the shapes 
			Matrix4f newPos = new Matrix4f();
			Matrix4f rotXYZ = new Matrix4f();
			Matrix4f newSize = new Matrix4f();
			Matrix4f tObj = new Matrix4f();
			
			// each shape has its own initialization (scale, position)
			float[][] sizeFactor = new float[nrOfObjects][3];
			Vector3f[] transXYZ = new Vector3f[nrOfObjects];
			
			transXYZ[0] = new Vector3f(	-3.0f,				0.0f,	0.0f);
			transXYZ[1] = new Vector3f(	0.0f,				0.0f,	0.0f);
			transXYZ[2] = new Vector3f(	3.0f,				0.0f,	0.0f);
			
			sizeFactor[0][0] = 0.5f;		//scale in x
			sizeFactor[0][1] = 0.5f;		//scale in y
			sizeFactor[0][2] = 5.0f;		//scale in z
			
			sizeFactor[1][0] = 0.5f;
			sizeFactor[1][1] = 0.5f;
			sizeFactor[1][2] = 5.0f;
			
			sizeFactor[2][0] = 0.5f;
			sizeFactor[2][1] = 0.5f;
			sizeFactor[2][2] = 5.0f;
			
		
			//apply new position, scale and rotation each function call to all shapes 
			for(int i = 0; i < shape.length; i++)
			{
	    		if( shape[i] != null)
	    		{
	    			// get the transformation reference of the shape
	    			tObj = shape[i].getTransformation();
	    			// set it back to the origin
	    			tObj.setIdentity();
	    			// rotate the object in a circle 
	    			rotXYZ.rotX(currentstep);
	    			tObj.mul(rotXYZ);
	    			// move the object to the right place
	    			newPos.setIdentity();
	    			newPos.set(transXYZ[i]);
	    			tObj.mul(newPos);
	    			// rotate the object to its correct position
	    			rotXYZ.rotX((float)Math.toRadians(90.0f));
	    			tObj.mul(rotXYZ);
	    			// rotate the object around itself 
	    			rotXYZ.rotZ(currentstep);
	    			tObj.mul(rotXYZ);
	    			// scale object first
	    			newSize.setIdentity();
    				newSize.m00 = sizeFactor[i][0];
	    			newSize.m11 = sizeFactor[i][1];
	    			newSize.m22 = sizeFactor[i][2];
	    			tObj.mul(newSize);
	    		}
			}
			
			// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
	}
	public static void changeView()
	{
		//determine smallest side to handle aspect ration other than 1:1
		double frameScale = Math.min((double)frameWidth, (double)frameHeigth)/2.0;
		
		//read out x and y position of mouse and calculate z position
		double x1 = mouseInit.getX()/(frameScale);
		double y1 = mouseInit.getY()/(frameScale);
		x1 -= 1;
		y1 = 1 - y1;
		double z1 = Math.sqrt(1-x1*x1-y1*y1);
		z1 = z1 > 0.0 ? z1 : 0.0;
		
		//create a normalized vector out of the 3 points
		Vector3f tPressed = new Vector3f((float)x1,(float)y1,(float)z1);
		tPressed.normalize();
		
		//read out x and y position of mouse and calculate z position
		double x2 = mouseDragged.getX()/(frameScale);
		double y2 = mouseDragged.getY()/(frameScale);
		x2 -= 1;
		y2 = 1 - y2;
		double z2 = Math.sqrt(1-x2*x2-y2*y2);
		z2 = z2 > 0.0 ? z2 : 0.0;
		
		//create a normalized vector out of the 3 points
		Vector3f tReleased = new Vector3f((float)x2,(float)y2,(float)z2);
		tReleased.normalize();
		
		//calculate rotation axis and rotation angle
		Vector3f rotAxis = new Vector3f();
		rotAxis.cross(tPressed, tReleased);
		float sensitivity = 3.0f;
		float theta = tPressed.angle(tReleased)*sensitivity;	
		AxisAngle4f axisAngle = new AxisAngle4f(rotAxis, theta);
		
		//if 
		if(z1 == 0.0 || z2 == 0.0)
		{	
			float dir = sensitivity*2.0f;
			if(x2-x1>0) dir = -sensitivity*2.0f;
			rotAxis = new Vector3f(0.0f, 0.0f, 1.0f);
			theta = dir*tPressed.angle(tReleased);	
			axisAngle = new AxisAngle4f(rotAxis, theta);
		}

		
		Camera tCam = sceneManager.getCamera();
		Matrix4f rot = new Matrix4f();
		rot.set(axisAngle);
		Matrix4f mCam = tCam.getCameraMatrix();
		mCam.mul(rot,mCam);
		/*Matrix4f tShape = shape[0].getTransformation();
		Matrix4f rot = new Matrix4f();
		rot.set(axisAngle);
		tShape.mul(rot,tShape);*/
		mouseInit = mouseDragged;
	    if(DEBUG)System.out.printf("angle: %f\t", theta/Math.PI*180.0);
	    if(DEBUG)System.out.printf("x: %f\ty: %f\tz: %f\n", rotAxis.x, rotAxis.y, rotAxis.z);
	    
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
	}
	
	public static class SimpleMouseMotionListener implements MouseMotionListener
	{

		public void mouseDragged(MouseEvent e) {
			mouseDragged = e.getPoint(); 
    		changeView();    		
		}

		public void mouseMoved(MouseEvent e) {
			mouseInit = e.getPoint();
		}

	}
	
	public static class SimpleMouseWheelListener implements MouseWheelListener
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
	public static class SimpleKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{

			case 'a':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				Vector3f tLookAt = tCam.getLookAtPoint();
				tCenterOfP.x -= 1.0f;
				tLookAt.x -= 1.0f;
				
				tCam.setCenterOfProjection(tCenterOfP);
				tCam.setLookAtPoint(tLookAt);

				break;
				
			}
		
			case 'o': {
				// Stop animation
				basicstep = 0;

				break;
			}
			case 'p': {
				// Resume animation
				basicstep = (float)Math.PI/360.0f;
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
			
			case 'w':{

				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				Vector3f tLookAt = tCam.getLookAtPoint();
				tCenterOfP.z -= 1.0f;
				tLookAt.z -= 1.0f;
									
				tCam.setCenterOfProjection(tCenterOfP);
				tCam.setLookAtPoint(tLookAt);
				break;
				
			}
			case 's':{

				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				Vector3f tLookAt = tCam.getLookAtPoint();
				tCenterOfP.z += 1.0f;
				tLookAt.z += 1.0f;

				tCam.setCenterOfProjection(tCenterOfP);
				tCam.setLookAtPoint(tLookAt);
				
				break;
				
			}


			case 'f':{

				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				Vector3f tLookAt = tCam.getLookAtPoint();
				tCenterOfP.y -= 1.0f;
				tLookAt.y -= 1.0f;

				tCam.setCenterOfProjection(tCenterOfP);
				tCam.setLookAtPoint(tLookAt);

				break;
				
			}

			case 'r':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				Vector3f tLookAt = tCam.getLookAtPoint();
				tCenterOfP.y += 1.0f;
				tLookAt.y += 1.0f;
				
				tCam.setCenterOfProjection(tCenterOfP);
				tCam.setLookAtPoint(tLookAt);

				break;
				
			}
			
			case 'd':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				Vector3f tLookAt = tCam.getLookAtPoint();
				tCenterOfP.x += 1.0f;
				tLookAt.x += 1.0f;
				
				tCam.setCenterOfProjection(tCenterOfP);
				tCam.setLookAtPoint(tLookAt);

				break;
				
			}
			case 'b':{
				
				Matrix4f tShape = shape[0].getTransformation();
				tShape.setIdentity();
				
			}
			case 'n':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f eyePos = new Vector3f(0.0f, 0.0f, 40.0f);
				Vector3f viewDirection = new Vector3f(0.0f, 0.0f, 0.0f);
				Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
				
				tCam.setCenterOfProjection(eyePos);
				tCam.setLookAtPoint(viewDirection);
				tCam.setUpVector(up);
				break;
				
			}
			case 'm':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f eyePos = new Vector3f(-10.0f, 40.0f, 40.0f);
				Vector3f viewDirection = new Vector3f(-5.0f, 0.0f, 0.0f);
				Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
				
				tCam.setCenterOfProjection(eyePos);
				tCam.setLookAtPoint(viewDirection);
				tCam.setUpVector(up);
				break;
				
			}
			case 'q':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				tCenterOfP.x -= 1.0f;
	
				tCam.setCenterOfProjection(tCenterOfP);
				
				break;
				
			}
			case 'e':{
				
				Camera tCam = sceneManager.getCamera();
				Vector3f tCenterOfP = tCam.getCenterOfProjection();
				tCenterOfP.x += 1.0f;
	
				tCam.setCenterOfProjection(tCenterOfP);
				
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
		renderPanel = new ShaderTask1RenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(frameWidth, frameHeigth);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window
		
		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
	    renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener());
	    renderPanel.getCanvas().addMouseWheelListener(new SimpleMouseWheelListener());
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
