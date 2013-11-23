#version 150
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
#define MAX_LIGHT 8
#define M_PI 3.1415926535897932384626433832795


uniform mat4 projection;
uniform mat4 modelview;
uniform vec4 lightDirection[MAX_LIGHT];
uniform vec3 lightDiffuse[MAX_LIGHT];
uniform vec3 lightAmbient[MAX_LIGHT];
uniform vec3 e;
uniform vec3 materialDiffuse;
uniform vec3 materialSpecular;
uniform vec3 materialAmbient;

uniform float runTime;

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
out vec3 cd;
out float time;

void main()
{		
	// Compute dot product of normal and light direction
	// and pass color to fragment shader
	// Note: here we assume "lightDirection" is specified in camera coordinates,
	// so we transform the normal to camera coordinates, and we don't transform
	// the light direction, i.e., it stays in camera coordinates
	ndotl = 0.0f;
	cd = vec3(0);
	float ndl = 0.0f;
	vec4 halfVector = vec4(0);
	float hdl = 0.0f;
	vec4 posNew = vec4(0);
	time = 0;
	time = runTime/1000;
	
	float wobble = cos(2*M_PI/1000*mod(runTime,1000))/7.0f;
	
	
	for(int Loop = 0; Loop < nLights; Loop++)
	{
		ndl = max(dot(modelview * vec4(normal,0), lightDirection[Loop]),0);
		halfVector = normalize(lightDirection[Loop] - vec4(normalize(position.xyz),0));
		hdl = max(dot(halfVector, vec4(normal,0)), 0.0);
		
		
		cd.x += lightDiffuse[Loop].x*materialDiffuse.x*ndl;
		cd.y += lightDiffuse[Loop].y*materialDiffuse.y*ndl;
		cd.z += lightDiffuse[Loop].z*materialDiffuse.z*ndl;
		
		cd.x += lightDiffuse[Loop].x*pow(materialSpecular.x*hdl,materialShininess);
		cd.y += lightDiffuse[Loop].y*pow(materialSpecular.y*hdl,materialShininess);
		cd.z += lightDiffuse[Loop].z*pow(materialSpecular.z*hdl,materialShininess);
		
	    cd.x += lightAmbient[Loop].x*materialAmbient.x;
		cd.y += lightAmbient[Loop].y*materialAmbient.y;
		cd.z += lightAmbient[Loop].z*materialAmbient.z;
		
		ndotl += ndl;
	}
	
	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner) 
	frag_texcoord = texcoord;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	posNew = position;
	float a = 1;
	if(posNew.x < 0)posNew.x -= wobble;
	if(posNew.x > 0)posNew.x += wobble;
	if(posNew.y < 0)posNew.y -= wobble;
	if(posNew.y > 0)posNew.y += wobble;
	if(posNew.z < 0)posNew.z -= wobble;
	if(posNew.z > 0)posNew.z += wobble;
	gl_Position = projection * modelview * posNew;
}
