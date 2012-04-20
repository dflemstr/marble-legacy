const float epsilon = 0.005;

uniform vec2 g_Resolution;
uniform sampler2D m_Texture;
uniform sampler2D m_SSAOTexture;
uniform sampler2D m_DepthTexture;
uniform float m_ZNear;
uniform float m_ZFar;
uniform bool m_ShowOnlyAO;
uniform bool m_DisableBlur;

varying vec2 texCoord;

float readDepth(vec2 uv) {
    float depthv = texture2D(m_DepthTexture, uv).r;
    return (2.0 * m_ZNear) / (m_ZFar + m_ZNear - depthv * (m_ZFar - m_ZNear));
}

void main(void) {
    vec4 amplitude;
#ifdef DISABLE_BLUR
    amplitude = texture2D(m_SSAOTexture, texCoord);
#else
    vec4 sum = vec4(0.0);

    float x = texCoord.x;
    float y = texCoord.y;

    float xScale = 2.0 / g_Resolution.x;
    float yScale = 2.0 / g_Resolution.y;

    float zsum = 1.0;
    float Zp = readDepth(texCoord);

    vec2 sample = vec2(x - 2.0 * xScale, y - 2.0 * yScale);
    float zTmp = readDepth(sample);
    float coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x - 0.0 * xScale, y - 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x + 2.0 * xScale, y - 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x - 1.0 * xScale, y - 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x + 1.0 * xScale, y - 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);
  
    sample = vec2(x - 2.0 * xScale, y - 0.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x + 2.0 * xScale, y - 0.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x - 1.0 * xScale, y + 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);
   
    sample = vec2(x + 1.0 * xScale, y + 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x - 2.0 * xScale, y + 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);
  
    sample = vec2(x - 0.0 * xScale, y + 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    sample = vec2(x + 2.0 * xScale, y + 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(m_SSAOTexture, sample);

    amplitude = sum / zsum;
#endif

#ifdef SHOW_ONLY_AO
        gl_FragColor = amplitude;
#else
        gl_FragColor = texture2D(m_Texture, texCoord) * amplitude;
#endif
}
