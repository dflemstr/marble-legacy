
uniform samplerCube environment;

varying vec3 normal;
varying vec3 normalW;
varying vec3 incident;
varying vec3 incidentW;
varying vec3 light[gl_MaxLights];

void main(void) {
    vec3 iW = normalize(incidentW);
    vec3 nW = normalize(normalW);
    vec4 color = textureCube(environment, vec3(gl_TextureMatrix[0] * vec4(reflect(iW, nW), 1.0)));

    vec3 i = normalize(incident);
    vec3 n = normalize(normal);
    int lightIndex;
    for (lightIndex = 0; lightIndex < gl_MaxLights; lightIndex++) {
        vec3 l = normalize(light[lightIndex]);
        vec3 h = normalize(l + i);
        float diffuse = dot(l, n);
        if (diffuse > 0.0) {
            float specular = pow(max(0.0, dot(n, h)), 86);
            color += gl_LightSource[lightIndex].specular * specular;
        }
    }

    gl_FragColor = color;
}
