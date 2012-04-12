#version 120

// Depth of field algorithm by Martins Upitis

const float PI = 3.14159265;

uniform sampler2D screen;
uniform sampler2D depth;
uniform vec2 resolution;
uniform float znear;
uniform float zfar;
uniform float focalDepth = 0.0; // focal distance value in meters, but you may use autofocus option below
uniform float focalLength = 40.0; // focal length in mm
uniform float fstop = 1.4; // f-stop value
uniform bool showFocus = false; // show debug focus point and focal range (red = focal point, green = focal range)
uniform int samples = 3; // samples on the first ring
uniform int rings = 3; // ring count
uniform bool manualdof = false; // manual dof calculation
uniform float ndofstart = 1.0; // near dof blur start
uniform float ndofdist = 2.0; // near dof blur falloff distance
uniform float fdofstart = 1.0; // far dof blur start
uniform float fdofdist = 3.0; // far dof blur falloff distance
uniform float CoC = 0.03; // circle of confusion size in mm (35mm film = 0.03mm)
uniform bool vignetting = true; // use optical lens vignetting?
uniform float vignout = 1.3; // vignetting outer border
uniform float vignin = 0.0; // vignetting inner border
uniform float vignfade = 22.0; // f-stops till vignete fades
uniform bool autofocus = true; // use autofocus in shader? disable if you use external focalDepth value
uniform vec2 focus = vec2(0.5,0.5); // autofocus point on screen (0.0,0.0 - left lower corner, 1.0,1.0 - upper right)
uniform float maxblur = 1.0; // clamp value of max blur (0.0 = no blur,1.0 default)
uniform float threshold = 0.5; // highlight threshold
uniform float gain = 25.0; // highlight gain
uniform float bias = 0.2; // bokeh edge bias
uniform float fringe = 0.7; // bokeh chromatic aberration/fringing
uniform bool noise = true; // use noise instead of pattern for sample dithering
uniform float namount = 0.0001; // dither amount
uniform bool depthblur = true; // blur the depth buffer?
uniform float dbsize = 1.25; // depthblursize
uniform bool pentagon = false; //use pentagon as bokeh shape?
uniform float feather = 0.4; //pentagon shape feather

varying vec2 coord;

float width  = resolution.x;
float height = resolution.y;

vec2 texel = vec2(1.0 / width, 1.0 / height);

// pentagonal shape
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

    dist = smoothstep(-feather, feather, dist);

    inorout += dot(dist, one);

    dist.x = dot(P, HS4);
    dist.y = HS5.w - abs(P.z);

    dist = smoothstep(feather, feather, dist);
    inorout += dist.x;

    return clamp(inorout, 0.0, 1.0);
}

// blurring depth
float bdepth(vec2 coords) {
    float d = 0.0;
    float kernel[9];
    vec2 offset[9];

    vec2 wh = vec2(texel.x, texel.y) * dbsize;

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

// processing the sample
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

// generating noise/pattern texture for dithering
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
    float edge = 0.002 * depth; // distance based edge smoothing
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
    dist = smoothstep(vignout + (fstop / vignfade), vignin + (fstop / vignfade), dist);
    return clamp(dist, 0.0, 1.0);
}

void main()
{
    // scene depth calculation

    float depthPoint = linearize(texture2D(depth, coord).r);

    if (depthblur) {
        depthPoint = linearize(bdepth(coord));
    }

    // focal plane calculation

    float fDepth = focalDepth;

    if (autofocus) {
        fDepth = linearize(texture2D(depth, focus).r);
    }

    // dof blur factor calculation

    float blur = 0.0;

    if (manualdof) {
        float a = depthPoint - fDepth; //focal plane
        float b = (a - fdofstart) / fdofdist; //far DoF
        float c = (-a - ndofstart) / ndofdist; //near Dof
        blur = (a > 0.0) ? b : c;
    } else {
        float f = focalLength; //focal length in mm
        float d = fDepth * 1000.0; //focal plane in mm
        float o = depthPoint * 1000.0; //depth in mm

        float a = (o * f) / (o - f);
        float b = (d * f) / (d - f);
        float c = (d - f) / (d * fstop * CoC);

        blur = abs(a - b) * c;
    }

    blur = clamp(blur, 0.0, 1.0);

    // calculation of pattern for ditering

    vec2 noise = rand(coord) * namount * blur;

    // getting blur x and y step factor

    float w = (1.0 / width)  * blur * maxblur + noise.x;
    float h = (1.0 / height) * blur * maxblur + noise.y;

    // calculation of final color

    vec3 col = vec3(0.0);

    if(blur < 0.05) { // some optimization thingy
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
                if (pentagon) {
                    p = penta(vec2(pw, ph));
                }
                col += color(coord + vec2(pw * w, ph * h), blur) * mix(1.0, float(i) / float(rings), bias) * p;
                s += 1.0 * mix(1.0, float(i) / float(rings), bias) * p;
            }
        }
        col /= s; // divide by sample count
    }

    if (showFocus) {
        col = debugFocus(col, blur, depthPoint);
    }

    if (vignetting) {
        col *= vignette();
    }

    gl_FragColor.rgb = col;
    gl_FragColor.a = 1.0;
    // gl_FragColor = texture2D(depth, coord);
}
