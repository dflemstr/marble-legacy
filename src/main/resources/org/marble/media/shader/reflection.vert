
uniform vec3 cameraPos;
uniform mat4 modelMatrix;

varying vec3 incident;
varying vec3 normal;

void main(void) {
    vec3 position = vec3(modelMatrix * gl_Vertex);
    incident = position - cameraPos;
    normal   = mat3(modelMatrix) * gl_Normal;

    gl_Position = ftransform();
}
