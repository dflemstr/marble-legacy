#ifndef NUM_LIGHTS
#define NUM_LIGHTS 4
#endif

uniform mat4 g_ViewMatrix;
uniform vec4 g_LightPosition;
uniform vec4 g_LightColor;
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
#import "ShaderLib/Lighting.glsllib"

void main(void) {
    vec3 n = normalize(worldNormal);
    vec3 i = normalize(worldIncident);

    float distance =
        length(cross(modelPosition - m_TrunkCenter1, modelPosition - m_TrunkCenter2)) /
        length(m_TrunkCenter2 - m_TrunkCenter1);
    float intensity = (1.0 + sin(distance * m_DistanceWeight +
                                 snoise(m_NoiseScale * modelPosition + m_NoiseSeed) * m_NoiseWeight)) / 2.0;
    vec4 color = texture2D(m_WoodGradient, vec2(m_Variation, intensity));

    gl_FragColor = Lighting_Compute(g_LightPosition, g_LightColor, g_AmbientLightColor,
                                    worldPosition, n, normalize(viewIncident),
                                    m_Ambient, m_Diffuse * color, m_Specular, m_Shininess);
}
