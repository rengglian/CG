#version 150
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
#define MAX_LIGHT 8

uniform mat4 projection;
uniform mat4 modelview;
uniform vec4 lightDirection[MAX_LIGHT];
uniform vec4 lightPosition[MAX_LIGHT];
uniform vec3 lightDiffuse[MAX_LIGHT];
uniform vec3 lightAmbient[MAX_LIGHT];
uniform vec3 e;
uniform vec3 materialDiffuse;
uniform vec3 materialSpecular;
uniform vec3 materialAmbient;

uniform float materialShininess;

uniform int nLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec2 texcoord;


// Output variables for fragment shader
out float ndotl;
out vec2 frag_texcoord;
out vec3 cdiff;
out vec3 cspec;
out vec3 camp;

void main()
{		
	// Compute dot product of normal and light direction
	// and pass color to fragment shader
	// Note: here we assume "lightDirection" is specified in camera coordinates,
	// so we transform the normal to camera coordinates, and we don't transform
	// the light direction, i.e., it stays in camera coordinates
	ndotl = 0.0f;
	cdiff = vec3(0.0f);
	cspec = vec3(0.0f);
	camp = vec3(0.0f);
	
	float ndl = 0.0f;
	vec4 R = vec4(0.0f);
	vec4 v = vec4(modelview * position);
	vec4 E = normalize(-v);
	vec4 L = vec4(0.0f);
	vec3 cl = vec3(0.0f);

	for(int Loop = 0; Loop < nLights; Loop++)
	{
		L = normalize(lightPosition[Loop] - position);
		ndl = max( dot( modelview*vec4(normal,0.0f), L ) ,0.0f);
		R = reflect( L ,modelview*vec4(normal,0.0f));
		cl = lightDiffuse[Loop]/pow(length(lightPosition[Loop]-position),2);
				
		cspec.x += cl.x*materialSpecular.x*pow(max(dot(R,E),0.0f),materialShininess);
		cspec.y += cl.y*materialSpecular.y*pow(max(dot(R,E),0.0f),materialShininess);
		cspec.z += cl.z*materialSpecular.z*pow(max(dot(R,E),0.0f),materialShininess);
		
		camp.x += lightAmbient[Loop].x*materialAmbient.x;
		camp.y += lightAmbient[Loop].y*materialAmbient.y;
		camp.z += lightAmbient[Loop].z*materialAmbient.z;
		ndotl += ndl;
	}
	
	
	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner) 
	frag_texcoord = texcoord;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
