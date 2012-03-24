
uniform samplerCube environment;

varying vec3 incident;
varying vec3 normal;

void main(void) {
    vec3 i = normalize(incident);
    vec3 n = normalize(normal);
    gl_FragColor = textureCube(environment, vec3(gl_TextureMatrix[0] * vec4(reflect(i, n), 1.0)));
}
