package robot;

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
public class robot
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static GraphSceneManager sceneManager;
	static float currentstep, basicstep;
	static int nrOfObjects = 7; // number of object to build the car
	static Shape[] shape = new Shape[nrOfObjects];	// each object has a shape
	static TransformGroup worldTG;
	static TransformGroup sphereTG;
	static TransformGroup robotTG;
	static TransformGroup thoraxTG;
	static TransformGroup neckTG;
	static TransformGroup headTG;
	static TransformGroup leftHumerusTG;
	static TransformGroup rightHumerusTG;
	static TransformGroup leftRadiusTG;
	static TransformGroup rightRadiusTG;
	static TransformGroup leftFemurTG;
	static TransformGroup rightFemurTG;

	
	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
			
	public final static class RobotRenderPanel extends GLRenderPanel
	{
		 
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			
			sceneManager = new GraphSceneManager();
			Matrix4f trans= new Matrix4f();
			Matrix4f rot = new Matrix4f();

			float xPosRobot = -4.0f;
			float yPosRobot = 3.0f;
			float zPosRobot = 0.0f;
			
			float xPosThorax = 0.0f;
			float yPosThorax = 0.0f;
			float zPosThorax = 0.0f;
			
			float widthThorax = 2.0f;
			float heightThorax = 3.0f;
			float depthThorax = 0.6f;

			float radiusNeck = depthThorax/2;
			float lengthNeck = 0.2f;
			float offsetNeck = 0.0f;
			
			float xPosNeck = 0.0f;
			float yPosNeck = heightThorax/2;
			float zPosNeck = 0.0f;	
			
			float radiusHead = 0.7f;
			int xSlicesHead = 32;
			int ySlicesHead = 32;
			
			float xPosHead = 0.0f;
			float yPosHead = 0.0f;
			float zPosHead = lengthNeck+radiusHead;				
			
			float radiusHumeris = depthThorax/2;
			float lengthHumeris = 1.5f;
			float offsetHumeris = 0.0f;
			
			float xPosLeftHumerus = -widthThorax/2 - radiusHumeris;
			float yPosLeftHumerus = heightThorax/2;
			float zPosLeftHumerus = 0.0f;
			
			float xPosRightHumerus = widthThorax/2 + radiusHumeris;
			float yPosRightHumerus = heightThorax/2;
			float zPosRightHumerus = 0.0f;
			
			float radiusRadii = radiusHumeris;
			float lengthRadii = 1.5f;
			float offsetRadii = 0.0f;
			
			float xPosLeftRadius = 0.0f;
			float yPosLeftRadius = 0.0f;
			float zPosLeftRadius = -lengthHumeris+lengthHumeris/3;
			
			float xPosRightRadius = 0.0f;
			float yPosRightRadius = 0.0f;
			float zPosRightRadius = -lengthHumeris+lengthHumeris/3;
			
			
			// create the world
			worldTG = new TransformGroup();
			
			// create a sphere
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(0.0f, 0.0f , 0.0f));
			Shape sphereS = new Shape(new CreateSphere(32, 32, 1.0f, r).getVertexData());
			ShapeNode sphereN = new ShapeNode(sphereS);
			sphereTG = new TransformGroup(trans);
			sphereTG.addNode(sphereN);
			
			// add sphere to the world
			worldTG.addNode(sphereTG);
			
			// create robot
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosRobot, yPosRobot , zPosRobot));
			robotTG = new TransformGroup(trans);
			
			// start with thorax
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosThorax, yPosThorax , zPosThorax));
			Shape thoraxS = new Shape(new CreateBlock(widthThorax, heightThorax, depthThorax, r).getVertexData());
			ShapeNode thoraxN = new ShapeNode(thoraxS);
			thoraxTG = new TransformGroup(trans);
			thoraxTG.addNode(thoraxN);			
			// add thorax to robot
			robotTG.addNode(thoraxTG);

			// start with neck
			Shape neckS = new Shape(new CreateCylinder(32, lengthNeck, radiusNeck, offsetNeck, r).getVertexData());
			ShapeNode neckN = new ShapeNode(neckS);
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosNeck, yPosNeck , zPosNeck));
			rot.rotX(-(float)Math.PI/2.0f);
			trans.mul(rot);
			neckTG = new TransformGroup(trans);
			neckTG.addNode(neckN);			
			// add neck to thorax
			thoraxTG.addNode(neckTG);
			
			// start with head
			Shape headS = new Shape(new CreateSphere(xSlicesHead, ySlicesHead, radiusHead, r).getVertexData());
			ShapeNode headN = new ShapeNode(headS);
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosHead, yPosHead , zPosHead));
			rot.rotX(-(float)Math.PI/2.0f);
			trans.mul(rot);
			headTG = new TransformGroup(trans);
			headTG.addNode(headN);			
			// add head to neck
			neckTG.addNode(headTG);			
			
			// start with left Humerus
			Shape leftHumerusS = new Shape(new CreateCylinder(32, lengthHumeris, radiusHumeris, offsetHumeris, r).getVertexData());
			ShapeNode leftHumerusN = new ShapeNode(leftHumerusS);
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosLeftHumerus, yPosLeftHumerus , zPosLeftHumerus));
			rot.rotX(-(float)Math.PI/2.0f);
			trans.mul(rot);
			leftHumerusTG = new TransformGroup(trans);
			leftHumerusTG.addNode(leftHumerusN);			
			// add leftHumerus to thorax
			thoraxTG.addNode(leftHumerusTG);
			
			// start with right Humerus
			Shape rightHumerusS = new Shape(new CreateCylinder(32, lengthHumeris, radiusHumeris, offsetHumeris, r).getVertexData());
			ShapeNode rightHumerusN = new ShapeNode(rightHumerusS);
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosRightHumerus, yPosRightHumerus , zPosRightHumerus));
			rot.rotX(-(float)Math.PI/2.0f);
			trans.mul(rot);
			rightHumerusTG = new TransformGroup(trans);
			rightHumerusTG.addNode(rightHumerusN);			
			// add right humerus to thorax
			thoraxTG.addNode(rightHumerusTG);
			
			// start with left radius
			Shape leftRadiusS = new Shape(new CreateCylinder(32, lengthRadii, radiusRadii, offsetRadii, r).getVertexData());
			ShapeNode leftRadiusN = new ShapeNode(leftRadiusS);
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosLeftRadius, yPosLeftRadius , zPosLeftRadius));
			leftRadiusTG = new TransformGroup(trans);
			leftRadiusTG.addNode(leftRadiusN);			
			// add leftRadius to leftHumerus
			leftHumerusTG.addNode(leftRadiusTG);	
			
			// start with right radius
			Shape rightRadiusS = new Shape(new CreateCylinder(32, lengthRadii, radiusRadii, offsetRadii, r).getVertexData());
			ShapeNode rightRadiusN = new ShapeNode(rightRadiusS);
			trans = new Matrix4f();
			trans.setIdentity();
			trans.setTranslation(new Vector3f(xPosRightRadius, yPosRightRadius , zPosRightRadius));
			rightRadiusTG = new TransformGroup(trans);
			rightRadiusTG.addNode(rightRadiusN);			
			// add leftRadius to leftHumerus
			rightHumerusTG.addNode(rightRadiusTG);
			
			// add robot to the world
			worldTG.addNode(robotTG);
			
			// add world to the scene
			sceneManager.setRootNode(worldTG);
			
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
				material.texture.load("../textures/metal.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			neckS.setMaterial(material);
			

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = (float)Math.PI/300.0f;
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
			// Update transformation by rotating with angle "currentstep"
			currentstep += basicstep;

			robotTG.rotY(basicstep);
			leftHumerusTG.rotX(basicstep);
    		
    		// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class RobotMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
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
	public static class RobotKeyListener implements KeyListener
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
		renderPanel = new RobotRenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new RobotMouseListener());
	    renderPanel.getCanvas().addMouseWheelListener(new SimpleMouseWheelListener());
	    renderPanel.getCanvas().addKeyListener(new RobotKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
		
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
