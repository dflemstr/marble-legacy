#version 120

uniform sampler2D depth;
uniform sampler2D normal;
uniform vec2 resolution;
uniform float znear;
uniform float zfar;
uniform vec3 frustumCorner;
uniform float sampleRadius = 1.0;
uniform float intensity = 8.0;
uniform float scale = 1.0;
uniform float bias = 0.1;
uniform float cutoff = 0.99;
uniform vec2[4] samples = { vec2(1.0, 0.0), vec2(-1.0, 0.0), vec2(0.0, 1.0), vec2(0.0, -1.0) };

varying vec2 coord;

float depthv;

vec3 getPosition(vec2 uv) {
    depthv = texture2D(depth, uv).r;
    float depth = (2.0 * znear) / (zfar + znear - depthv * (zfar - znear));
    float x = mix(-frustumCorner.x, frustumCorner.x, uv.x);
    float y = mix(-frustumCorner.y, frustumCorner.y, uv.y);
    return depth * vec3(x, y, frustumCorner.z);
}

vec3 getNormal(vec2 uv) {
    return normalize(texture2D(normal, uv).xyz * 2.0 - vec3(1.0));
}

vec2 rand(vec2 coord) {
    float noiseX = clamp(fract(sin(dot(coord, vec2(12.9898, 78.233)      )) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
    float noiseY = clamp(fract(sin(dot(coord, vec2(12.9898, 78.233) * 2.0)) * 43758.5453), 0.0, 1.0) * 2.0 - 1.0;
    return vec2(noiseX, noiseY);
}

vec2 getRandom(vec2 uv) {
    return normalize(rand(uv));
}

float doAmbientOcclusion(vec2 uv, vec3 pos, vec3 norm) {
    vec3 diff = getPosition(uv) - pos;
    vec3 v = normalize(diff);
    float d = length(diff) * scale;
    return max(0.0, dot(norm, v) - bias) * (1.0 / (1.0 + d)) * intensity;
}

vec4 getColor(float result) {
    return vec4(result, result, result, 1.0);
}

vec2 reflection(vec2 v1, vec2 v2){
    return v1 - 2.0 * dot(v2, v1) * v2;
}

void main(void) {
    vec3 position = getPosition(coord);

    if (depthv > cutoff) {
        gl_FragColor = getColor(1.0);
        return;
    }

    vec3 n = getNormal(coord);
    vec2 rand = getRandom(coord);

    float ao = 0.0;
    float rad = sampleRadius / position.z;

    const int iterations = 4;
    int i;
    for(int i = 0; i < iterations; i++) {
        vec2 coord1 = reflection(samples[i], rand) * vec2(rad, rad);
        vec2 coord2 = vec2(coord1.x * 0.707 - coord1.y * 0.707,
                           coord1.x * 0.707 + coord1.y * 0.707);

        ao += doAmbientOcclusion(coord + coord1 * 0.25, position, n);
        ao += doAmbientOcclusion(coord + coord2 * 0.50, position, n);
        ao += doAmbientOcclusion(coord + coord1 * 0.75, position, n);
        ao += doAmbientOcclusion(coord + coord2 * 1.00, position, n);
    }
    ao /= float(iterations) * 4.0;

    gl_FragColor = getColor(1.0 - ao);
}
