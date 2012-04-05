const float roughness = 0.005;
const float sharpness = 0.8;
const float scattering = 0.25;

uniform samplerCube environment;
uniform int lightCount;

varying vec3 normal;
varying vec3 normalW;
varying vec3 incident;
varying vec3 incidentW;
varying vec3 light[gl_MaxLights];

void main(void) {
    vec3 iW = normalize(incidentW);
    vec3 nW = normalize(normalW);
    vec4 color = textureCube(environment, vec3(gl_TextureMatrix[0] * vec4(reflect(iW, nW), 1.0)));

    // Blinn shading
    vec3 i = normalize(incident);
    vec3 n = normalize(normal);
    float w = 0.18 * (1.0 - sharpness);
    int lightIndex;
    for (lightIndex = 0; lightIndex < lightCount; lightIndex++) {
        vec3 l = normalize(light[lightIndex]);
        vec3 h = normalize(l + i);

        float diffuse = dot(l, n);
        if (diffuse > 0.0) {
            color += gl_LightSource[lightIndex].diffuse * diffuse * scattering;
        }

        float specular = smoothstep(0.72 - w, 0.72 + w, pow(max(0.0, dot(n, h)), 1 / roughness));
        color += gl_LightSource[lightIndex].specular * specular;
    }

    gl_FragColor = color;
}
