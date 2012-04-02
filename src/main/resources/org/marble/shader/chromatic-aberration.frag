
// Spectrum of reciprocals of indices of refraction
uniform float etaR; // Red
uniform float etaG; // Green
uniform float etaB; // Blue
uniform float fresnelPower;
uniform samplerCube environment;

varying vec3 normal;
varying vec3 normalW;
varying vec3 incident;
varying vec3 incidentW;
varying vec3 light[gl_MaxLights];

void main(void) {
    float F = ((1.0 - etaG) * (1.0 - etaG)) / ((1.0 + etaG) * (1.0 + etaG));
    vec3 iW = normalize(incidentW);
    vec3 nW = normalize(normalW);

    float ratio = F + (1.0 - F) * pow(1.0 - dot(-iW, nW), fresnelPower);

    vec3 refractR = vec3(gl_TextureMatrix[0] * vec4(refract(iW, nW, etaR), 1.0));
    vec3 refractG = vec3(gl_TextureMatrix[0] * vec4(refract(iW, nW, etaG), 1.0));
    vec3 refractB = vec3(gl_TextureMatrix[0] * vec4(refract(iW, nW, etaB), 1.0));

    vec3 reflectDir = vec3(gl_TextureMatrix[0] * vec4(reflect(iW, nW), 1.0));

    vec4 refractColor;
    refractColor.ra = textureCube(environment, refractR).ra;
    refractColor.g  = textureCube(environment, refractG).g;
    refractColor.b  = textureCube(environment, refractB).b;

    vec4 reflectColor;
    reflectColor    = textureCube(environment, reflectDir);

    vec4 color = mix(refractColor, reflectColor, ratio);

    vec3 i = normalize(incident);
    vec3 n = normalize(normal);
    int lightIndex;
    for (lightIndex = 0; lightIndex < gl_MaxLights; lightIndex++) {
        vec3 l = normalize(light[lightIndex]);
        vec3 h = normalize(l + i);
        float diffuse = dot(l, n);
        if (diffuse > 0.0) {
            float specular = pow(max(0.0, dot(n, h)), 86);
            color += gl_LightSource[lightIndex].specular  * specular;
        }
    }

    gl_FragColor = color;
}
