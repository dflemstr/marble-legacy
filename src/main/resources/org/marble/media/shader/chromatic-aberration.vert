
uniform vec3 cameraPos;
uniform mat4 modelMatrix;

varying vec3 incident;
varying vec3 normal;

void main(void) {
    vec4 position = modelMatrix * gl_Vertex;
    incident = position.xyz / position.w - cameraPos;
    normal   = mat3(modelMatrix) * gl_Normal;

    gl_Position = ftransform();
}
