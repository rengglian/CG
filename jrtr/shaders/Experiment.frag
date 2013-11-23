#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform sampler2D myMap;

// Variables passed in from the vertex shader
in float ndotl;
in vec2 frag_texcoord;
in vec3 cd;
in float time;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

float wave(vec2 p, float angle) {
	vec2 direction = vec2(cos(angle), sin(angle));
	return cos(dot(p, direction));
}

float wrap(float x) {
	return abs(mod(x, 2.0f)-1.0f);
}

void main()
{	

	vec2 p = (frag_texcoord - 0.5f) * 50.0f;	

	float psych = 0.0f;
	for (float i = 1.0f; i <= 11.0f; i++) {
		psych += wave(p, time / i);
	}
	
	psych = wrap(psych);
	
	// The built-in GLSL function "texture" performs the texture lookup
	frag_shaded = ndotl * vec4(vec3(0.78f, 1-cos(time), 1-sin(time)),1.0f) * vec4(vec3(psych), 1.0f);
	//frag_shaded = vec4(0.4, 0.4, 0.8, 1.0);
	
	
}