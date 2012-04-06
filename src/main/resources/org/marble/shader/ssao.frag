uniform sampler2D normals;
uniform sampler2D randoms;
uniform vec2 resolution;
uniform float frustumNear;
uniform float frustumFar;
uniform vec3 frustumCorner;
uniform float sampleRadius;
uniform float intensity;
uniform float scale;
uniform float bias;
uniform float cutoff = 0.99;
uniform vec2[4] samples;

varying vec2 coord;

float depthv;

vec3 getPosition(vec2 uv) {
    depthv = texture2D(normals, uv).a;
    float depth = (2.0 * frustumNear) / (frustumFar + frustumNear - depthv * (frustumFar - frustumNear));
    float x = mix(-frustumCorner.x, frustumCorner.x, uv.x);
    float y = mix(-frustumCorner.y, frustumCorner.y, uv.y);
    return depth * vec3(x, y, frustumCorner.z);
}

vec3 getNormal(vec2 uv) {
    return normalize(texture2D(normals, uv).xyz * 2.0 - 1.0);
}

vec2 getRandom(vec2 uv) {
    return normalize(texture2D(randoms, uv * resolution / 128.0).xy * 2.0 - 1.0);
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

    vec3 normal = getNormal(coord);
    vec2 rand = getRandom(coord);

    float ao = 0.0;
    float rad = sampleRadius / position.z;

    int iterations = 4;
    int i;
    for(int i = 0; i < iterations; i++) {
        vec2 coord1 = reflection(samples[i], rand) * vec2(rad, rad);
        vec2 coord2 = vec2(coord1.x * 0.707 - coord1.y * 0.707,
                           coord1.x * 0.707 + coord1.y * 0.707);

        ao += doAmbientOcclusion(coord + coord1 * 0.25, position, normal);
        ao += doAmbientOcclusion(coord + coord2 * 0.50, position, normal);
        ao += doAmbientOcclusion(coord + coord1 * 0.75, position, normal);
        ao += doAmbientOcclusion(coord + coord2 * 1.00, position, normal);
    }
    ao /= float(iterations) * 4.0;

    gl_FragColor = getColor(1.0 - ao);
}
