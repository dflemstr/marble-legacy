uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat4 g_WorldMatrix;
uniform mat3 g_NormalMatrix;
uniform vec3 g_CameraPosition;

attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 worldPosition;
varying vec3 worldNormal;
varying vec3 worldIncident;
varying vec3 viewPosition;
varying vec3 viewNormal;
varying vec3 viewIncident;

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
    vec4 position = vec4(inPosition, 1.0);

    worldPosition = (g_WorldMatrix     * position).xyz;
    viewPosition  = (g_WorldViewMatrix * position).xyz;

    worldNormal = linearize(g_WorldMatrix) * inNormal;
    viewNormal  = g_NormalMatrix * inNormal;

    worldIncident = worldPosition - g_CameraPosition;
    viewIncident  = -viewPosition;

    gl_Position = g_WorldViewProjectionMatrix * position;
}
