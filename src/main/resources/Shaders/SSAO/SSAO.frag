uniform sampler2D m_DepthTexture;
uniform sampler2D m_NormalTexture;
uniform float m_ZNear;
uniform float m_ZFar;
uniform vec3 m_FrustumCorner;
uniform float m_SampleRadius;
uniform float m_Intensity;
uniform float m_Scale;
uniform float m_Bias;
uniform float m_Cutoff;
uniform vec2[4] samples = { vec2(1.0, 0.0), vec2(-1.0, 0.0), vec2(0.0, 1.0), vec2(0.0, -1.0) };

varying vec2 texCoord;

float depthv;

vec3 getPosition(vec2 uv) {
    depthv = texture2D(m_DepthTexture, uv).r;
    float depth = (2.0 * m_ZNear) / (m_ZFar + m_ZNear - depthv * (m_ZFar - m_ZNear));
    float x = mix(-m_FrustumCorner.x, m_FrustumCorner.x, uv.x);
    float y = mix(-m_FrustumCorner.y, m_FrustumCorner.y, uv.y);
    return depth * vec3(x, y, m_FrustumCorner.z);
}

vec3 getNormal(vec2 uv) {
    return normalize(texture2D(m_NormalTexture, uv).xyz * 2.0 - vec3(1.0));
}

vec2 rand(vec2 texCoord) {
    float noiseX = clamp(fract(sin(dot(texCoord, vec2(12.9898, 78.233)      )) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
    float noiseY = clamp(fract(sin(dot(texCoord, vec2(12.9898, 78.233) * 2.0)) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
    return vec2(noiseX, noiseY);
}

vec2 getRandom(vec2 uv) {
    return normalize(rand(uv));
}

float doAmbientOcclusion(vec2 uv, vec3 pos, vec3 norm) {
    vec3 diff = getPosition(uv) - pos;
    vec3 v = normalize(diff);
    float d = length(diff) * m_Scale;
    return max(0.0, dot(norm, v) - m_Bias) * (1.0 / (1.0 + d)) * m_Intensity;
}

vec4 getColor(float result) {
    return vec4(result, result, result, 1.0);
}

vec2 reflection(vec2 v1, vec2 v2){
    return v1 - 2.0 * dot(v2, v1) * v2;
}

void main(void) {
    vec3 position = getPosition(texCoord);

    if (depthv > m_Cutoff) {
        gl_FragColor = getColor(1.0);
        return;
    }

    vec3 n = getNormal(texCoord);
    vec2 rand = getRandom(texCoord);

    float ao = 0.0;
    float rad = m_SampleRadius / position.z;

    const int iterations = 4;
    int i;
    for(int i = 0; i < iterations; i++) {
        vec2 coord1 = reflection(samples[i], rand) * vec2(rad, rad);
        vec2 coord2 = vec2(coord1.x * 0.707 - coord1.y * 0.707,
                           coord1.x * 0.707 + coord1.y * 0.707);

        ao += doAmbientOcclusion(texCoord + coord1 * 0.25, position, n);
        ao += doAmbientOcclusion(texCoord + coord2 * 0.50, position, n);
        ao += doAmbientOcclusion(texCoord + coord1 * 0.75, position, n);
        ao += doAmbientOcclusion(texCoord + coord2 * 1.00, position, n);
    }
    ao /= float(iterations) * 4.0;

    gl_FragColor = getColor(1.0 - ao);
}
