#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform sampler2D myMap;

// Variables passed in from the vertex shader
in float ndotl;
in vec2 frag_texcoord;
in vec3 cdiff;
in vec3 cspec;
in vec3 camp;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{		
	// The built-in GLSL function "texture" performs the texture lookup
	frag_shaded = ndotl * texture(myTexture, frag_texcoord) * vec4(cdiff+camp,0.0f);
	frag_shaded +=ndotl * texture(myMap, frag_texcoord) *vec4(cspec,0.0f);
}

