
uniform int lightCount;

varying vec3 position;
varying vec3 normal;
varying vec3 incident;
varying vec3 light[gl_MaxLights];

void main(void) {
    normal = gl_NormalMatrix * gl_Normal;
    position = gl_Vertex.xyz;

    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
    incident = -vertex.xyz;

    int lightIndex;
    for (lightIndex = 0; lightIndex < lightCount; lightIndex++) {
        light[lightIndex] = gl_LightSource[lightIndex].position.xyz - vertex.xyz;
    }

    gl_Position = ftransform();
}
