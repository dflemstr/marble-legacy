
uniform vec3 cameraPos;

varying vec3 incident;
varying vec3 normal;

void main(void) {
    vec4 position = gl_ModelViewMatrix * gl_Vertex;
    incident = position.xyz / position.w - cameraPos;
    normal   = gl_NormalMatrix * gl_Normal;

    gl_Position = ftransform();
}
