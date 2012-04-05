
uniform vec3 cameraPos;
uniform mat4 modelMatrix;
uniform int lightCount;

varying vec3 normal;
varying vec3 normalW;
varying vec3 incident;
varying vec3 incidentW;
varying vec3 light[gl_MaxLights];

mat3 linearize(mat4 matrix) {
    mat3 result;
    result[0][0] = matrix[0][0];
    result[0][1] = matrix[0][1];
    result[0][2] = matrix[0][2];
    result[1][0] = matrix[1][0];
    result[1][1] = matrix[1][1];
    result[1][2] = matrix[1][2];
    result[2][0] = matrix[2][0];
    result[2][1] = matrix[2][1];
    result[2][2] = matrix[2][2];
    return result;
}

void main(void) {
    vec3 positionW = vec3(modelMatrix * gl_Vertex);
    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;

    incident = -vertex.xyz;
    incidentW = positionW - cameraPos;
    normal = gl_NormalMatrix * gl_Normal;
    normalW   = linearize(modelMatrix) * gl_Normal;

    int lightIndex;
    for (lightIndex = 0; lightIndex < lightCount; lightIndex++) {
        light[lightIndex] = gl_LightSource[lightIndex].position.xyz - vertex.xyz;
    }

    gl_Position = ftransform();
}
