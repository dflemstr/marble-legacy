
uniform vec3 cameraPos;
uniform mat4 modelMatrix;

varying vec3 incident;
varying vec3 normal;

mat3 linearize(mat4 matrix) {
    mat3 result;
    result[0][0] = matrix[0][0];
    result[0][1] = matrix[0][1];
    result[0][2] = matrix[0][2];
    result[1][0] = matrix[0][0];
    result[1][1] = matrix[0][1];
    result[1][2] = matrix[0][2];
    result[2][0] = matrix[0][0];
    result[2][1] = matrix[0][1];
    result[2][2] = matrix[0][2];
    return result;
}

void main(void) {
    vec4 position = modelMatrix * gl_Vertex;
    incident = position.xyz / position.w - cameraPos;
    normal   = linearize(modelMatrix) * gl_Normal;

    gl_Position = ftransform();
}
