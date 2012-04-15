#ifndef NUM_LIGHTS
#define NUM_LIGHTS 1
#endif

uniform mat4 g_ViewMatrix;
uniform vec4 g_LightPosition[NUM_LIGHTS];
uniform vec4 g_LightColor[NUM_LIGHTS];
uniform vec4 g_AmbientLightColor;

uniform vec4 m_Ambient;
uniform vec4 m_Diffuse;
uniform vec4 m_Specular;
uniform float m_Shininess;

uniform float m_DistanceWeight;
uniform float m_NoiseScale;
uniform float m_NoiseWeight;
uniform float m_Roughness;
uniform vec3 m_TrunkCenter1;
uniform vec3 m_TrunkCenter2;
uniform vec3 m_NoiseSeed;
uniform float m_Variation;
uniform sampler2D m_WoodGradient;

varying vec3 modelPosition;
varying vec3 worldPosition;
varying vec3 worldNormal;
varying vec3 worldIncident;
varying vec3 viewPosition;
varying vec3 viewNormal;
varying vec3 viewIncident;

#import "ShaderLib/Noise3D.glsllib"
#import "ShaderLib/SingleLighting.glsllib"

void main(void) {
    float distance =
        length(cross(modelPosition - m_TrunkCenter1, modelPosition - m_TrunkCenter2)) /
        length(m_TrunkCenter2 - m_TrunkCenter1);
    float intensity = (1.0 + sin(distance * m_DistanceWeight +
                                 snoise(m_NoiseScale * modelPosition + m_NoiseSeed) * m_NoiseWeight)) / 2.0;
    vec4 color = texture2D(m_WoodGradient, vec2(m_Variation, intensity));

    vec3 ambientColor = m_Ambient.rgb;
    vec3 diffuseColor = m_Diffuse.rgb * color.rgb;
    vec3 specularColor = m_Specular.rgb;
    float alpha = m_Diffuse.a;
    vec3 ambientLightSum = ambientColor * g_AmbientLightColor.rgb;
    vec3 diffuseLightSum = vec3(0.0);
    vec3 specularLightSum = vec3(0.0);
    vec3 vn = normalize(viewNormal);
    vec3 vi = normalize(viewIncident);

    int lightIndex;
    for (lightIndex = 0; lightIndex < NUM_LIGHTS; lightIndex++) {
        vec4 lightPosition = g_LightPosition[lightIndex];
        vec4 lightColor    = g_LightColor[lightIndex];
        vec3 lightVector;
        float attenuation;

        SingleLighting_CalculateLightVector(worldPosition, lightPosition, lightColor, lightVector, attenuation);

        vec3 vl = normalize((g_ViewMatrix * vec4(lightVector, 0.0)).xyz);

        SingleLighting_AddLightOrenNayar(vn, vl, vi, lightColor.rgb, attenuation, m_Shininess, diffuseLightSum, specularLightSum);
    }

    gl_FragColor.rgb = diffuseColor * (ambientLightSum + diffuseLightSum) + specularColor * specularLightSum;
    gl_FragColor.a = alpha;
}
