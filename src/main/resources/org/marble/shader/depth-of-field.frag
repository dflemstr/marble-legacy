#version 120

// Depth of field algorithm by Martins Upitis

const float PI = 3.14159265;

uniform sampler2D screen;
uniform sampler2D depth;
uniform vec2 resolution;
uniform float znear;
uniform float zfar;
uniform float focalDepth;
uniform float focalLength;
uniform float fstop;
uniform bool showFocus;
uniform int samples;
uniform int rings;
uniform bool manualDOF;
uniform float nearDOFStart;
uniform float nearDOFDistance;
uniform float farDOFStart;
uniform float farDOFDistance;
uniform float coc;
uniform bool vignetting;
uniform float vignettingOuterBorder;
uniform float vignettingInnerBorder;
uniform float vignettingFade;
uniform bool autoFocus;
uniform vec2 focus;
uniform float maxBlur;
uniform float threshold;
uniform float gain;
uniform float bias;
uniform float fringe;
uniform bool noise;
uniform float noiseDitherAmount;
uniform bool depthBlur;
uniform float depthBlurSize;
uniform bool pentagonBokeh;
uniform float pentagonFeather;

varying vec2 coord;

float width  = resolution.x;
float height = resolution.y;

vec2 texel = vec2(1.0 / width, 1.0 / height);

float penta(vec2 coords) {
    float scale = float(rings) - 1.3;
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

    dist = smoothstep(-pentagonFeather, pentagonFeather, dist);

    inorout += dot(dist, one);

    dist.x = dot(P, HS4);
    dist.y = HS5.w - abs(P.z);

    dist = smoothstep(pentagonFeather, pentagonFeather, dist);
    inorout += dist.x;

    return clamp(inorout, 0.0, 1.0);
}

float bdepth(vec2 coords) {
    float d = 0.0;
    float kernel[9];
    vec2 offset[9];

    vec2 wh = vec2(texel.x, texel.y) * depthBlurSize;

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
        float tmp = texture2D(depth, coords + offset[i]).r;
        d += tmp * kernel[i];
    }

    return d;
}

vec3 color(vec2 coords, float blur) {
    vec3 col;

    col.r = texture2D(screen, coords + vec2( 0.0,    1.0) * texel * fringe * blur).r;
    col.g = texture2D(screen, coords + vec2(-0.866, -0.5) * texel * fringe * blur).g;
    col.b = texture2D(screen, coords + vec2( 0.866, -0.5) * texel * fringe * blur).b;

    vec3 lumcoeff = vec3(0.299, 0.587, 0.114);
    float lum = dot(col.rgb, lumcoeff);
    float thresh = max((lum - threshold) * gain, 0.0);
    return col + mix(vec3(0.0), col, thresh * blur);
}

vec2 rand(vec2 coord) {
    float noiseX = ((fract(1.0 - coord.s * (width / 2.0)) * 0.25) + (fract(coord.t * (height / 2.0)) * 0.75)) * 2.0 - 1.0;
    float noiseY = ((fract(1.0 - coord.s * (width / 2.0)) * 0.75) + (fract(coord.t * (height / 2.0)) * 0.25)) * 2.0 - 1.0;

    if (noise) {
        noiseX = clamp(fract(sin(dot(coord, vec2(12.9898, 78.233)      )) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
        noiseY = clamp(fract(sin(dot(coord, vec2(12.9898, 78.233) * 2.0)) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
    }
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
    return -zfar * znear / (depth * (zfar - znear) - zfar);
}

float vignette() {
    float dist = distance(gl_TexCoord[3].xy, vec2(0.5,0.5));
    dist = smoothstep(vignettingOuterBorder + (fstop / vignettingFade), vignettingInnerBorder + (fstop / vignettingFade), dist);
    return clamp(dist, 0.0, 1.0);
}

void main()
{
    float depthPoint = linearize(texture2D(depth, coord).r);

    if (depthBlur) {
        depthPoint = linearize(bdepth(coord));
    }

    float fDepth = focalDepth;

    if (autoFocus) {
        fDepth = linearize(texture2D(depth, focus).r);
    }

    float blur = 0.0;

    if (manualDOF) {
        float a = depthPoint - fDepth;
        float b = (a - farDOFStart) / farDOFDistance;
        float c = (-a - nearDOFStart) / nearDOFDistance;
        blur = (a > 0.0) ? b : c;
    } else {
        float f = focalLength;
        float d = fDepth * 1000.0;
        float o = depthPoint * 1000.0;

        float a = (o * f) / (o - f);
        float b = (d * f) / (d - f);
        float c = (d - f) / (d * fstop * coc);

        blur = abs(a - b) * c;
    }

    blur = clamp(blur, 0.0, 1.0);

    vec2 noise = rand(coord) * noiseDitherAmount * blur;

    float w = (1.0 / width)  * blur * maxBlur + noise.x;
    float h = (1.0 / height) * blur * maxBlur + noise.y;

    vec3 col = vec3(0.0);

    if(blur < 0.05) {
        col = texture2D(screen, coord).rgb;
    } else {
        col = texture2D(screen, coord).rgb;
        float s = 1.0;
        int ringsamples;

        int i;
        for (i = 1; i <= rings; i++) {
            ringsamples = i * samples;

            int j;
            for (j = 0; j < ringsamples; j++) {
                float step = PI * 2.0 / float(ringsamples);
                float pw = cos(float(j) * step) * float(i);
                float ph = sin(float(j) * step) * float(i);
                float p = 1.0;
                if (pentagonBokeh) {
                    p = penta(vec2(pw, ph));
                }
                col += color(coord + vec2(pw * w, ph * h), blur) * mix(1.0, float(i) / float(rings), bias) * p;
                s += 1.0 * mix(1.0, float(i) / float(rings), bias) * p;
            }
        }
        col /= s;
    }

    if (showFocus) {
        col = debugFocus(col, blur, depthPoint);
    }

    if (vignetting) {
        col *= vignette();
    }

    gl_FragColor = vec4(col, 1.0);
}
