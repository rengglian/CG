package simple;

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
public class simple
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager;
	static float currentstep, basicstep;
	static int numOfObjects=4;
	static Shape shape[]=new Shape[numOfObjects];

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	public final static class SimpleRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		
		public VertexData createTorus(int numberOfSegmentsT,int numberOfSegmentsC)
		{
			//TORUS
			float radT=2f;
			float radC=0.5f;
			float v[]=new float[3*((numberOfSegmentsC+1)*(numberOfSegmentsT+1))];
			float c[]=new float[3*((numberOfSegmentsC+1)*(numberOfSegmentsT+1))];
			int indices[]=new int[6*(numberOfSegmentsC)*(numberOfSegmentsT)+1000];
			int ite=0;


			
			for(int i=0;i<numberOfSegmentsT+1;i++)
			{
				for(int j=0;j<numberOfSegmentsC+1;j++)
				{
					v[3*i*(numberOfSegmentsC+1)+2*j+j]=(float) ((radT+radC*Math.cos(Math.toRadians(360/numberOfSegmentsC*j)))*Math.cos(Math.toRadians(360/numberOfSegmentsT*i)));
					v[3*i*(numberOfSegmentsC+1)+1+2*j+j]=(float) (radC*Math.sin(Math.toRadians(360/numberOfSegmentsC*j)));
					v[3*i*(numberOfSegmentsC+1)+2+2*j+j]=(float) ((radT+radC*Math.cos(Math.toRadians(360/numberOfSegmentsC*j)))*Math.sin(Math.toRadians(360/numberOfSegmentsT*i)));

					if(i%2==0)
					{
						ite=0;
					}
					else
					{
						ite=1;
					}
				
					switch(ite)
					{
					case 0:
						c[3*i*(numberOfSegmentsC+1)+2*j+j]=1;
						c[3*i*(numberOfSegmentsC+1)+1+2*j+j]=0;
						c[3*i*(numberOfSegmentsC+1)+2+2*j+j]=0;
						ite++;
						break;
					case 1:
						c[3*i*(numberOfSegmentsC+1)+2*j+j]=0;
						c[3*i*(numberOfSegmentsC+1)+1+2*j+j]=1;
						c[3*i*(numberOfSegmentsC+1)+2+2*j+j]=0;
						ite++;
						break;
					}

				}
			}
			
			for(int i=0;i<numberOfSegmentsT;i++)
			{
				for(int j=0;j<numberOfSegmentsC;j++)
				{
					indices[6*i*(numberOfSegmentsC+1)+5*j+j]=j+i*(numberOfSegmentsC+1);
					indices[6*i*(numberOfSegmentsC+1)+5*j+j+1]=j+1+i*(numberOfSegmentsC+1);
					indices[6*i*(numberOfSegmentsC+1)+5*j+j+2]=j+i*(numberOfSegmentsC+1)+(numberOfSegmentsC+1);
					
					indices[6*i*(numberOfSegmentsC+1)+5*j+j+3]=j+i*(numberOfSegmentsC+1)+1;
					indices[6*i*(numberOfSegmentsC+1)+5*j+j+4]=j+i*(numberOfSegmentsC+1)+(numberOfSegmentsC+1);
					indices[6*i*(numberOfSegmentsC+1)+5*j+j+5]=j+i*(numberOfSegmentsC+1)+(numberOfSegmentsC+2);
				}
			}
			VertexData vertexData = renderContext.makeVertexData(((numberOfSegmentsC+1)*(numberOfSegmentsT+1)));//Torus
			vertexData.addElement(c, VertexData.Semantic.COLOR,3);
			vertexData.addElement(v, VertexData.Semantic.POSITION,3);
			vertexData.addIndices(indices);
			
			return vertexData;
		}
		
		
		public VertexData createCube()
		{
			float v[] = {-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,		// front face
			         -1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,	// left face
				  	 1,-1,-1,-1,-1,-1, -1,1,-1, 1,1,-1,		// back face
					 1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,		// right face
					 1,1,1, 1,1,-1, -1,1,-1, -1,1,1,		// top face
					-1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face
			
			float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
			         -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
				  	 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
					 1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
					 0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
					 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face
			
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
			
			VertexData vertexData = renderContext.makeVertexData(24);
			vertexData.addElement(c, VertexData.Semantic.COLOR,3);
			vertexData.addElement(v, VertexData.Semantic.POSITION,3);
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
		
		public VertexData createCylinder(int numberOfSegments)
		{
			float x;
			float y;
			float radius=2f;
			float height=3f;
			float v[]=new float[6*(numberOfSegments+2)];
			
			
			v[v.length-6]=0f;
			v[v.length-5]=0f;
			v[v.length-4]=height;
			v[v.length-3]=0f;
			v[v.length-2]=0f;
			v[v.length-1]=-height;
			
			
			for(int i=0;i<numberOfSegments+1;i++)
			{	
				x=(float) (radius*Math.cos(Math.toRadians(360/numberOfSegments*i)));
				y=(float) (radius*Math.sin(Math.toRadians(360/numberOfSegments*i)));
				v[i+5*i]=x;
				v[i+1+5*i]=y;
				v[i+2+5*i]=height;
				
				v[i+3+5*i]=x;
				v[i+4+5*i]=y;
				v[i+5+5*i]=-height;				
			}
	
			float c[]=new float[3*(2*(numberOfSegments+1)+2)];
			int ite=0;
			for(int i=0;i<2*(numberOfSegments+1)+2;i++)
			{
				switch(ite)
				{
				case 0:
					c[i+2*i]=1;
					c[i+1+2*i]=0;
					c[i+2+2*i]=0;
					ite++;
					break;
				case 1:
					c[i+2*i]=1;
					c[i+1+2*i]=0;
					c[i+2+2*i]=0;
					ite++;
					break;
				case 2:
					c[i+2*i]=0;
					c[i+1+2*i]=1;
					c[i+2+2*i]=0;
					ite++;
					break;
				case 3:
					c[i+2*i]=0;
					c[i+1+2*i]=1;
					c[i+2+2*i]=0;
					ite++;
					break;
				case 4:
					c[i+2*i]=0;
					c[i+1+2*i]=0;
					c[i+2+2*i]=1;
					ite++;
					break;
				case 5:
					c[i+2*i]=0;
					c[i+1+2*i]=0;
					c[i+2+2*i]=1;
					ite=0;
					break;
				}
			}
			
			int indices[]=new int[12*numberOfSegments];
			
			for(int i=0;i<numberOfSegments;i++)
			{
				indices[i+11*i]=2*(i);
				indices[i+1+11*i]=2*(i)+1;
				indices[i+2+11*i]=2*(i)+2;
				
				indices[i+3+11*i]=2*(i)+1;
				indices[i+4+11*i]=2*(i)+2;
				indices[i+5+11*i]=2*(i)+3;
				
				indices[i+6+11*i]=2*(i);
				indices[i+7+11*i]=2*(i)+2;
				indices[i+8+11*i]=v.length/3-2;
				
				indices[i+9+11*i]=2*(i)+1;
				indices[i+10+11*i]=2*(i)+3;
				indices[i+11+11*i]=v.length/3-1;		
			}
			
			// Construct a data structure that stores the vertices, their
			// attributes, and the triangle mesh connectivity
			VertexData vertexData = renderContext.makeVertexData(2*(numberOfSegments+1)+2);
			vertexData.addElement(c, VertexData.Semantic.COLOR,3);
			vertexData.addElement(v, VertexData.Semantic.POSITION,3);
			//vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
			//vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
			
			vertexData.addIndices(indices);
			
			return vertexData;
		}

		
		public void init(RenderContext r)
		{
			renderContext = r;
			
			VertexData vertexData[]=new VertexData[numOfObjects];
			
			vertexData[0]=createTorus(30,10);
			vertexData[1]=vertexData[0];
			vertexData[2]=createCube();
			vertexData[3]=createCylinder(30);
							
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			//shape = new Shape(vertexData);
			//sceneManager.addShape(shape);
			
			
			for(int i=0;i<shape.length;i++)
			{
				shape[i]=new Shape(vertexData[i]);
				sceneManager.addShape(shape[i]);
			}

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
				material.texture.load("../textures/plant.jpg");
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

			currentstep+=basicstep;
			
			Matrix4f trans=new Matrix4f();
			Matrix4f rotY=new Matrix4f();
			Matrix4f rotZ=new Matrix4f();
			Matrix4f scale=new Matrix4f();
			trans.setIdentity();
			Matrix4f t=new Matrix4f();
			Matrix4f trans2=new Matrix4f();
			Matrix4f rotY2=new Matrix4f();
			trans.setIdentity();
			
			t = shape[0].getTransformation();
			t.setIdentity();
			rotZ.rotZ((float) (Math.PI/2));
			scale.set(.3f);
			rotY.rotY(currentstep);
			rotY2.rotY(currentstep);
			trans.set(new Vector3f(0.5f,0f,0f));
			trans2.set(new Vector3f(2f,0f,0f));
			t.mul(rotY2);
			t.mul(trans2);
			t.mul(trans);
			t.mul(rotZ);
			t.mul(rotY);
			t.mul(scale);
			
			t = shape[1].getTransformation();
			t.setIdentity();
			rotZ.rotZ((float) (Math.PI/2));
			scale.set(.3f);
			rotY.rotY(currentstep);
			rotY2.rotY(currentstep);
			trans.set(new Vector3f(-0.5f,0f,0f));
			trans2.set(new Vector3f(2f,0f,0f));
			t.mul(rotY2);
			t.mul(trans2);
			t.mul(trans);
			t.mul(rotZ);
			t.mul(rotY);
			t.mul(scale);
			
			t = shape[2].getTransformation();
			t.setIdentity();
			scale.set(.3f);
			rotY.rotY(currentstep);
			rotY2.rotY(currentstep);
			trans2.set(new Vector3f(2f,0.7f,0f));
			t.mul(rotY2);
			t.mul(trans2);
			t.mul(scale);
		
			t = shape[3].getTransformation();
			t.setIdentity();
			scale.set(.2f);
			rotY.rotY(currentstep);
			rotY2.rotY(currentstep);
			trans2.set(new Vector3f(2f,0f,0f));
			t.mul(rotY2);
			t.mul(trans2);
			t.mul(scale);

    		//shape.setTransformation(t);
    		// Trigger redrawing of the render window
    		renderPanel.getCanvas().repaint(); 
		}
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
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		//renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}

