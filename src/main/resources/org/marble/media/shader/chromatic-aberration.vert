
varying vec3 interfacePos;
varying vec3 normal;

void main(void) {
    vec4 position = gl_ModelViewMatrix * gl_Vertex;

    interfacePos = position.xyz / position.w;
    normal = gl_NormalMatrix * gl_Normal;

    gl_Position = ftransform();
}
