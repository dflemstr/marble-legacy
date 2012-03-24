
// Spectrum of reciprocals of indices of refraction
uniform float etaR; // Red
uniform float etaG; // Green
uniform float etaB; // Blue
uniform float fresnelPower;
uniform samplerCube environment;

varying vec3 incident;
varying vec3 normal;

void main(void) {
    float F = ((1.0 - etaG) * (1.0 - etaG)) / ((1.0 + etaG) * (1.0 + etaG));
    vec3 i = normalize(incident);
    vec3 n = normalize(normal);

    float ratio = F + (1.0 - F) * pow(1.0 - dot(-i, n), fresnelPower);

    vec3 refractR = vec3(gl_TextureMatrix[0] * vec4(refract(i, n, etaR), 1.0));
    vec3 refractG = vec3(gl_TextureMatrix[0] * vec4(refract(i, n, etaG), 1.0));
    vec3 refractB = vec3(gl_TextureMatrix[0] * vec4(refract(i, n, etaB), 1.0));

    vec3 reflectDir = vec3(gl_TextureMatrix[0] * vec4(reflect(i, n), 1.0));

    vec4 refractColor;
    refractColor.ra = textureCube(environment, refractR).ra;
    refractColor.g  = textureCube(environment, refractG).g;
    refractColor.b  = textureCube(environment, refractB).b;

    vec4 reflectColor;
    reflectColor    = textureCube(environment, reflectDir);

    vec3 combinedColor = mix(refractColor, reflectColor, ratio);

    gl_FragColor = vec4(combinedColor, 1.0);
}
