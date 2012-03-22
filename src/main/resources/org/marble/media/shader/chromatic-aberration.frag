
const float F = 0.05;
const float etaR = 1.14;
const float etaG = 1.12;
const float etaB = 1.10;
const float fresnelPower = 2.0;

uniform samplerCube environment;

varying vec3 interfacePos;
varying vec3 normal;

void main(void) {
    vec3 i = normalize(interfacePos);
    vec3 n = normalize(normal);

    float ratio = F + (1.0 - F) * pow(1.0 - dot(-i, n), fresnelPower);

    vec3 refractR = refract(i, n, etaR);
    refractR = vec3(gl_TextureMatrix[0] * vec4(refractR, 1.0));

    vec3 refractG = refract(i, n, etaG);
    refractG = vec3(gl_TextureMatrix[0] * vec4(refractG, 1.0));

    vec3 refractB = refract(i, n, etaB);
    refractB = vec3(gl_TextureMatrix[0] * vec4(refractB, 1.0));

    vec3 reflectDir = reflect(i, n);
    reflectDir = vec3(gl_TextureMatrix[0] * vec4(reflectDir, 1.0));

    vec3 refractColor;
    refractColor.r = vec3(textureCube(environment, refractR)).r;
    refractColor.g = vec3(textureCube(environment, refractG)).g;
    refractColor.b = vec3(textureCube(environment, refractB)).b;

    vec3 reflectColor;
    reflectColor   = vec3(textureCube(environment, reflectDir));

    vec3 combinedColor = mix(refractColor, reflectColor, ratio);

    gl_FragColor = vec4(combinedColor, 1.0);
}
