
varying vec3 position;
varying vec3 normal;
varying vec3 incident;
varying vec3 light[gl_MaxLights];

void main(void) {
    normal = gl_NormalMatrix * gl_Normal;
    position = gl_Vertex.xyz;

    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
    incident = -vertex.xyz;

    int i;
    for (i = 0; i < gl_MaxLights; i++) {
        light[i] = gl_LightSource[i].position.xyz - vertex.xyz;
    }

    gl_Position = ftransform();
}
