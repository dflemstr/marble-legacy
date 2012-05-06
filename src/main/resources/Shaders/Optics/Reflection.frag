
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
#import "ShaderLib/SingleLighting.glsllib"

void main(void) {
    vec3 n = normalize(worldNormal);
    vec3 i = normalize(worldIncident);

    vec4 color = Optics_GetEnvColor(m_EnvironmentMap, reflect(i, n));

    vec3 ambientColor = m_Ambient.rgb;
    vec3 diffuseColor = m_Diffuse.rgb * color.rgb;
    vec3 specularColor = m_Specular.rgb;
    float alpha = m_Diffuse.a;
    vec3 ambientLightSum = ambientColor * g_AmbientLightColor.rgb;
    vec3 diffuseLightSum = vec3(0.0);
    vec3 specularLightSum = vec3(0.0);
    vec3 vn = normalize(viewNormal);
    vec3 vi = normalize(viewIncident);

    vec4 lightPosition = g_LightPosition;
    vec4 lightColor    = g_LightColor;
    vec3 lightVector;
    float attenuation;

    SingleLighting_CalculateLightVector(worldPosition, lightPosition, lightColor, lightVector, attenuation);

    vec3 vl = normalize((g_ViewMatrix * vec4(lightVector, 0.0)).xyz);

    SingleLighting_AddLightPhong(vn, vl, vi, lightColor.rgb, attenuation, m_Shininess, diffuseLightSum, specularLightSum);

    gl_FragColor.rgb = diffuseColor * (ambientLightSum + diffuseLightSum) + specularColor * specularLightSum;
    gl_FragColor.a = alpha;
}
