// Depth of field algorithm by Martins Upitis
const float PI = 3.14159265;

uniform vec2 g_Resolution;

uniform sampler2D m_Texture;
uniform sampler2D m_DepthTexture;

uniform float m_ZNear;
uniform float m_ZFar;
uniform float m_FocalDepth;
uniform float m_FocalLength;
uniform float m_FStop;
uniform bool m_ShowFocus;
uniform int m_Samples;
uniform int m_Rings;
uniform bool m_ManualDOF;
uniform float m_NearDOFStart;
uniform float m_NearDOFDistance;
uniform float m_FarDOFStart;
uniform float m_FarDOFDistance;
uniform float m_CoC;
uniform bool m_Vignetting;
uniform float m_VignettingOuterBorder;
uniform float m_VignettingInnerBorder;
uniform float m_VignettingFade;
uniform bool m_AutoFocus;
uniform vec2 m_Focus;
uniform float m_MaxBlur;
uniform float m_Threshold;
uniform float m_Gain;
uniform float m_Bias;
uniform float m_Fringe;
uniform bool m_Noise;
uniform float m_NoiseDitherAmount;
uniform bool m_DepthBlur;
uniform float m_DepthBlurSize;
uniform bool m_PentagonBokeh;
uniform float m_PentagonFeather;

varying vec2 texCoord;

float width  = g_Resolution.x;
float height = g_Resolution.y;

vec2 texel = vec2(1.0 / width, 1.0 / height);

float penta(vec2 coords) {
    float scale = float(m_Rings) - 1.3;
    vec4  HS0 = vec4( 1.0,          0.0,          0.0,  1.0);
    vec4  HS1 = vec4( 0.309016994,  0.951056516,  0.0,  1.0);
    vec4  HS2 = vec4(-0.809016994,  0.587785252,  0.0,  1.0);
    vec4  HS3 = vec4(-0.809016994, -0.587785252,  0.0,  1.0);
    vec4  HS4 = vec4( 0.309016994, -0.951056516,  0.0,  1.0);
    vec4  HS5 = vec4( 0.0,          0.0,          1.0,  1.0);

    vec4  one = vec4( 1.0);

    vec4 P = vec4(coords, vec2(scale, scale));

    vec4 dist = vec4(0.0);
    float inorout = -4.0;

    dist.x = dot(P, HS0);
    dist.y = dot(P, HS1);
    dist.z = dot(P, HS2);
    dist.w = dot(P, HS3);

    dist = smoothstep(-m_PentagonFeather, m_PentagonFeather, dist);

    inorout += dot(dist, one);

    dist.x = dot(P, HS4);
    dist.y = HS5.w - abs(P.z);

    dist = smoothstep(m_PentagonFeather, m_PentagonFeather, dist);
    inorout += dist.x;

    return clamp(inorout, 0.0, 1.0);
}

float bdepth(vec2 coords) {
    float d = 0.0;
    float kernel[9];
    vec2 offset[9];

    vec2 wh = vec2(texel.x, texel.y) * m_DepthBlurSize;

    offset[0] = vec2(-wh.x, -wh.y);
    offset[1] = vec2( 0.0,  -wh.y);
    offset[2] = vec2( wh.x  -wh.y);

    offset[3] = vec2(-wh.x,  0.0);
    offset[4] = vec2( 0.0,   0.0);
    offset[5] = vec2( wh.x,  0.0);

    offset[6] = vec2(-wh.x,  wh.y);
    offset[7] = vec2( 0.0,   wh.y);
    offset[8] = vec2( wh.x,  wh.y);

    kernel[0] = 1.0/16.0;   kernel[1] = 2.0/16.0;   kernel[2] = 1.0/16.0;
    kernel[3] = 2.0/16.0;   kernel[4] = 4.0/16.0;   kernel[5] = 2.0/16.0;
    kernel[6] = 1.0/16.0;   kernel[7] = 2.0/16.0;   kernel[8] = 1.0/16.0;

    int i;
    for (i = 0; i < 9; i++) {
        float tmp = texture2D(m_DepthTexture, coords + offset[i]).r;
        d += tmp * kernel[i];
    }

    return d;
}

vec3 color(vec2 coords, float blur) {
    vec3 col;

    col.r = texture2D(m_Texture, coords + vec2( 0.0,    1.0) * texel * m_Fringe * blur).r;
    col.g = texture2D(m_Texture, coords + vec2(-0.866, -0.5) * texel * m_Fringe * blur).g;
    col.b = texture2D(m_Texture, coords + vec2( 0.866, -0.5) * texel * m_Fringe * blur).b;

    vec3 lumcoeff = vec3(0.299, 0.587, 0.114);
    float lum = dot(col.rgb, lumcoeff);
    float thresh = max((lum - m_Threshold) * m_Gain, 0.0);
    return col + mix(vec3(0.0), col, thresh * blur);
}

vec2 rand(vec2 texCoord) {
#ifdef NOISE
    float noiseX = clamp(fract(sin(dot(texCoord, vec2(12.9898, 78.233)      )) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
    float noiseY = clamp(fract(sin(dot(texCoord, vec2(12.9898, 78.233) * 2.0)) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
#else
    float noiseX = ((fract(1.0 - texCoord.s * (width / 2.0)) * 0.25) + (fract(texCoord.t * (height / 2.0)) * 0.75)) * 2.0 - 1.0;
    float noiseY = ((fract(1.0 - texCoord.s * (width / 2.0)) * 0.75) + (fract(texCoord.t * (height / 2.0)) * 0.25)) * 2.0 - 1.0;
#endif
    return vec2(noiseX, noiseY);
}

vec3 debugFocus(vec3 col, float blur, float depth) {
    float edge = 0.002 * depth;
    float m = clamp(smoothstep(0.0,        edge, blur), 0.0, 1.0);
    float e = clamp(smoothstep(1.0 - edge, 1.0,  blur), 0.0, 1.0);

    col = mix(col, vec3(1.0, 0.5, 0.0), (1.0 - m) * 0.6);
    col = mix(col, vec3(0.0, 0.5, 1.0), ((1.0 - e) - (1.0 - m)) * 0.2);

    return col;
}

float linearize(float depth) {
    return -m_ZFar * m_ZNear / (depth * (m_ZFar - m_ZNear) - m_ZFar);
}

float vignette() {
    float dist = distance(texCoord, m_Focus);
    dist = smoothstep(m_VignettingOuterBorder + (m_FStop / m_VignettingFade), m_VignettingInnerBorder + (m_FStop / m_VignettingFade), dist);
    return clamp(dist, 0.0, 1.0);
}

void main()
{
#if DEPTH_BLUR
    float depthPoint = linearize(bdepth(texCoord));
#else
    float depthPoint = linearize(texture2D(m_DepthTexture, texCoord).r);
#endif

#if AUTO_FOCUS
    float fDepth = linearize(texture2D(m_DepthTexture, m_Focus).r);
#else
    float fDepth = m_FocalDepth;
#endif

    float blur = 0.0;

#if MANUAL_DOF
    float a = depthPoint - fDepth;
    float b = (a - m_FarDOFStart) / m_FarDOFDistance;
    float c = (-a - m_NearDOFStart) / m_NearDOFDistance;
    blur = (a > 0.0) ? b : c;
#else
    float f = m_FocalLength;
    float d = fDepth * 1000.0;
    float o = depthPoint * 1000.0;

    float a = (o * f) / (o - f);
    float b = (d * f) / (d - f);
    float c = (d - f) / (d * m_FStop * m_CoC);

    blur = abs(a - b) * c;
#endif

    blur = clamp(blur, 0.0, 1.0);

    vec2 noise = rand(texCoord) * m_NoiseDitherAmount * blur;

    float w = (1.0 / width)  * blur * m_MaxBlur + noise.x;
    float h = (1.0 / height) * blur * m_MaxBlur + noise.y;

    vec3 col;
    if(blur < 0.05) {
        col = texture2D(m_Texture, texCoord).rgb;
    } else {
        col = texture2D(m_Texture, texCoord).rgb;
        float s = 1.0;
        int ringsamples;

        int i;
        for (i = 1; i <= m_Rings; i++) {
            ringsamples = i * m_Samples;

            int j;
            for (j = 0; j < ringsamples; j++) {
                float step = PI * 2.0 / float(ringsamples);
                float pw = cos(float(j) * step) * float(i);
                float ph = sin(float(j) * step) * float(i);
#if PENTAGON_BOKEH
                float p = penta(vec2(pw, ph));
#else
                float p = 1.0;
#endif
                col += color(texCoord + vec2(pw * w, ph * h), blur) * mix(1.0, float(i) / float(m_Rings), m_Bias) * p;
                s += 1.0 * mix(1.0, float(i) / float(m_Rings), m_Bias) * p;
            }
        }
        col /= s;
    }
#if SHOW_FOCUS
    col = debugFocus(col, blur, depthPoint);
#endif

#if VIGNETTING
    col *= vignette();
#endif

    gl_FragColor = vec4(col, 1.0);
}
