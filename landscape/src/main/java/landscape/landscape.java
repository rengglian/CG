package landscape;

import javax.swing.*;

import java.awt.Point;
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
public class landscape
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static float currentstep, basicstep;
	static int nrOfObjects = 1;
	static Shape shape[] = new Shape[nrOfObjects];
	static Point mousePressed;
	static Point mouseDragged;
	static int frameHeigth = 500;
	static int frameWidth = 500;
	static boolean DEBUG = false;
	
	private static void changeView()
	{
		//determine smallest side to handle aspect ration other than 1:1
		double frameScale = Math.min((double)frameWidth, (double)frameHeigth)/2.0;
		
		//read out x and y position of mouse and calculate z position
		double x1 = mousePressed.getX()/(frameScale);
		double y1 = mousePressed.getY()/(frameScale);
		x1 -= 1;
		y1 = 1 - y1;
		double z1 = Math.sqrt(1-x1*x1-y1*y1);
		z1 = z1 > 0.0 ? z1 : 0.0;
		
		//create a normalized vector out of the 3 points
		Vector3f tPressed = new Vector3f((float)x1,(float)y1,(float)z1);
		tPressed.normalize();
		
		//read out x and y position of mouse and calculate z position
		double x2 = mouseDragged.getX()/(frameWidth/2);
		double y2 = mouseDragged.getY()/(frameHeigth/2);
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
		float theta = tPressed.angle(tReleased);	
		AxisAngle4f axisAngle = new AxisAngle4f(rotAxis, theta);
		
		//if 
		if(z1 == 0.0 || z2 == 0.0)
		{
			float dir = 3.0f;
			if(x2-x1>0) dir = -3.0f;
			rotAxis = new Vector3f(0.0f, 0.0f, 1.0f);
			theta = dir*tPressed.angle(tReleased);	
			axisAngle = new AxisAngle4f(rotAxis, theta);
		}

		//Camera tCam = sceneManager.getCamera();
		Matrix4f mCam = shape[0].getTransformation();
		Matrix4f rot = new Matrix4f();
		rot.set(axisAngle);
		//Matrix4f mCam = tCam.getCameraMatrix();
		mCam.mul(rot,mCam);
		mousePressed = mouseDragged;	    
	}

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 * @return 
	 */ 
	public static VertexData createLandscape(int slices, float roughness, float decay)
	{
		int dim3 = 3;
		//first create the height map
		HeightMap heightMap = new HeightMap(slices, roughness, decay);
		// get the dimensions of the height map
		int objWidth = heightMap.getDimension();
		int objHeight = objWidth;

		// arrays to store the 3d information of the object
		float v3d[][] = new float[objWidth*objHeight][dim3];
		float v[] = new float[3*((objWidth)*(objHeight))];
		float c[] = new float[3*((objWidth)*(objHeight))];
		float uv[] = new float[2*((objWidth)*(objHeight))];
		int[] indices = new int[3*((objWidth-1)*(objHeight-1))*2];
		int k = 0;
		
		// create the object ( bending a square mesh around the object )
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{
				// math coordinates
				v3d[row * objWidth + column][0] = -1.0f + 2.0f/(objWidth)*(float)column;
				v3d[row * objWidth + column][1] = -1.0f + 2.0f/(objHeight)*(float)row;
				v3d[row * objWidth + column][2] = heightMap.getHeight(row, column);
				if(DEBUG)System.out.printf("x:%.2f, y:%.2f, z:%.2f\n", v3d[row * objWidth + column][0], v3d[row * objWidth + column][1], v3d[row * objWidth + column][2]);
				
				// convert the xyz matrix into an vector
				for(int dimension = 0; dimension < dim3; dimension++)
				{
					v[dimension+column*dim3+row*objWidth*dim3]=v3d[row * objWidth + column][dimension];
				}
				
				// assign colors depending on the height
				if(heightMap.getHeight(row, column)>(heightMap.getMax())*0.75)
				{	//white
					c[column*dim3+row*objWidth*dim3 + 0] = 1-heightMap.getHeight(row, column)*0.3f;
					c[column*dim3+row*objWidth*dim3 + 1] = 1-heightMap.getHeight(row, column)*0.3f;
					c[column*dim3+row*objWidth*dim3 + 2] = 1-heightMap.getHeight(row, column)*0.3f;
				}else if(heightMap.getHeight(row, column)<(heightMap.getMin()+heightMap.getMean())/2.0f)
				{	//blue
					c[column*dim3+row*objWidth*dim3 + 0] = 0;
					c[column*dim3+row*objWidth*dim3 + 1] = 0;
					c[column*dim3+row*objWidth*dim3 + 2] = 1-heightMap.getHeight(row, column)*0.3f;
				}else
				{	//green
					c[column*dim3+row*objWidth*dim3 + 0] = 0;
					c[column*dim3+row*objWidth*dim3 + 1] = 1-heightMap.getHeight(row, column)*0.3f;
					c[column*dim3+row*objWidth*dim3 + 2] = 0;
				}

				// create the indices vector
				if(row < objHeight -1 && column < objWidth -1)
				{
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + row * objWidth;
					indices[k++] = (column + 1)%objWidth + (row + 1)*objWidth;
					
					indices[k++] = column + row * objWidth;
					indices[k++] = (column + 1)%objWidth + (row + 1)*objWidth;
					indices[k++] = column + row * objWidth + objWidth;
				}
			}
		}
		
		Vector3f[] tFaces = new Vector3f[(objWidth-1)*(objHeight-1)*2];
		
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
				nVertices[column*dim3+row*objWidth*dim3 + 0] = tNorm.x/10.0f;
				nVertices[column*dim3+row*objWidth*dim3 + 1] = tNorm.y/10.0f;
				nVertices[column*dim3+row*objWidth*dim3 + 2] = tNorm.z/10.0f;
				//if(DEBUG)System.out.printf("V x:%5.3f\t\ty:%5.3f\t\tz:%5.3f\n", tNorm.x, tNorm.y, tNorm.z);
			}
		}
		
		
		float deltaX = 1.0f/objWidth;
		float deltaY = 1.0f/objHeight;
		float xCoords = 0.0f;
		float yCoords = 0.0f;
		
		k = 0;
		
		for(int row = 0; row < objHeight; row++)
		{
			for(int column = 0; column < objWidth; column++)
			{
				uv[k++] = xCoords%1;
				uv[k++] = yCoords;
				xCoords += deltaX;
			}
			yCoords += deltaY;
		}
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(v.length/dim3);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, dim3);
		vertexData.addElement(nVertices, VertexData.Semantic.NORMAL, dim3);
		vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		
		vertexData.addIndices(indices);

		if(DEBUG)System.out.printf("max value %.3f\n", heightMap.getMax());
		if(DEBUG)System.out.printf("min value %.3f\n", heightMap.getMin());
		if(DEBUG)System.out.printf("mean value %.3f\n", heightMap.getMean());
		
		return vertexData;
	}
	
	private static void initPosition()
	{
		// each shape has its own initialization (scale, position, rotation)
		float[] rotInit = new float[nrOfObjects];
		float[][] sizeFactor = new float[nrOfObjects][3];
		
		sizeFactor[0][0] = 20.0f;		//scale in x
		sizeFactor[0][1] = 20.0f;		//scale in y
		sizeFactor[0][2] = 10.0f;		//scale in z
		
		rotInit[0] = -(float)Math.PI/2.0f;
		//apply new position, scale and rotation each function call to all shapes 
		Matrix4f newSize = new Matrix4f();
		Matrix4f rotXYZ = new Matrix4f();
		Matrix4f tObj = new Matrix4f();
		for(int i = 0; i < shape.length; i++)
		{
    		if( shape[i] != null)
    		{
    			// get the transformation reference of the shape
    			tObj = shape[i].getTransformation();
    			rotXYZ.rotY(rotInit[i]);
    			tObj.mul(rotXYZ);
    			rotXYZ.rotX(rotInit[i]);
    			tObj.mul(rotXYZ);
    			newSize.setIdentity();
				newSize.m00 = sizeFactor[i][0];
    			newSize.m11 = sizeFactor[i][1];
    			newSize.m22 = sizeFactor[i][2];
    			tObj.mul(newSize);
    		}
		}
		if(DEBUG)System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\n",tObj.m00, tObj.m01, tObj.m02, tObj.m03);
		if(DEBUG)System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\n",tObj.m10, tObj.m11, tObj.m12, tObj.m13);
		if(DEBUG)System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\n",tObj.m20, tObj.m21, tObj.m22, tObj.m23);
		if(DEBUG)System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\n",tObj.m30, tObj.m31, tObj.m32, tObj.m33);
	}
	
	public final static class LandscapeRenderPanel extends GLRenderPanel
	{
		//first argument of command line as INT
		 int slices;
		 // overwrite ZylinderRenderPanel to pass first argument of command line
		 public LandscapeRenderPanel(int slices) {
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
			float roughness = 2.0f;
			float decay = 2.0f;
			int n =7;
			if(DEBUG)n = 2;
			VertexData[] vertexData = new VertexData[nrOfObjects];
			vertexData[0] = createLandscape(n, roughness, decay);
								
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
			
			initPosition();

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
				material.texture.load("../textures/torino.jpg");
				//material.texture.load("../textures/checkerboard.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}

			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.00f;
		    currentstep = basicstep;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		    //renderContext.useShader(material.shader);
		    //shape[0].setMaterial(material);
		    
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
	
	public static class TorusMouseMotionListener implements MouseMotionListener
	{

		public void mouseDragged(MouseEvent e) {
			mouseDragged = e.getPoint(); 
    		changeView();    		
		}

		public void mouseMoved(MouseEvent e) {
			mousePressed = e.getPoint();
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
	public static class TorusKeyListener implements KeyListener
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

			case 'k': {
				// Stop animation
				if(material.testColor.x == 0.0f)material.testColor.x = 1.0f;
				else material.testColor.x = 0.0f;

				break;
			}
			
			case 'o': {
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

			case 'g':{

				Frustum tFru = sceneManager.getFrustum();
				tFru.setFarPlane(200);				
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
			case 'y':{
				
				Camera tCam = sceneManager.getCamera();
				tCam.print();
				
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
		renderPanel = new LandscapeRenderPanel(firstArg);
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(frameWidth, frameHeigth);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new TorusMouseListener());
	    renderPanel.getCanvas().addMouseMotionListener(new TorusMouseMotionListener());
	    renderPanel.getCanvas().addMouseWheelListener(new TorusMouseWheelListener());
	    renderPanel.getCanvas().addKeyListener(new TorusKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
