
uniform mat4 g_ViewMatrix;
uniform vec4 g_LightPosition;
uniform vec4 g_LightColor;
uniform vec4 g_AmbientLightColor;

uniform samplerCube m_EnvironmentMap;

uniform vec4 m_Ambient;
uniform vec4 m_Diffuse;
uniform vec4 m_Specular;
uniform float m_Shininess;

varying vec3 worldPosition;
varying vec3 worldNormal;
varying vec3 worldIncident;
varying vec3 viewPosition;
varying vec3 viewNormal;
varying vec3 viewIncident;

#import "Common/ShaderLib/Optics.glsllib"
#import "ShaderLib/Lighting.glsllib"

void main(void) {
    vec3 n = normalize(worldNormal);
    vec3 i = normalize(worldIncident);

    vec4 color = Optics_GetEnvColor(m_EnvironmentMap, reflect(i, n));

    gl_FragColor = Lighting_Compute(g_LightPosition, g_LightColor, g_AmbientLightColor,
                                    worldPosition, n, normalize(viewIncident),
                                    m_Ambient, m_Diffuse * color, m_Specular, m_Shininess);
}
