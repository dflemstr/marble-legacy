const float epsilon = 0.005;

uniform sampler2D screen;
uniform sampler2D ssao;
uniform sampler2D depth;
uniform vec2 resolution;
uniform float znear;
uniform float zfar;
uniform bool showOnlyAO;
uniform bool disableBlur;

varying vec2 coord;

float readDepth(vec2 uv) {
    float depthv = texture2D(depth, uv).r;
    return (2.0 * znear) / (zfar + znear - depthv * (zfar - znear));
}

void main(void) {
    vec4 sum = vec4(0.0);

    float x = coord.x;
    float y = coord.y;

    float xScale = 2.0 / resolution.x;
    float yScale = 2.0 / resolution.y;

    float zsum = 1.0;
    float Zp = readDepth(coord);

    vec2 sample = vec2(x - 2.0 * xScale, y - 2.0 * yScale);
    float zTmp = readDepth(sample);
    float coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x - 0.0 * xScale, y - 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x + 2.0 * xScale, y - 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x - 1.0 * xScale, y - 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x + 1.0 * xScale, y - 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);
  
    sample = vec2(x - 2.0 * xScale, y - 0.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x + 2.0 * xScale, y - 0.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x - 1.0 * xScale, y + 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);
   
    sample = vec2(x + 1.0 * xScale, y + 1.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x - 2.0 * xScale, y + 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);
  
    sample = vec2(x - 0.0 * xScale, y + 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    sample = vec2(x + 2.0 * xScale, y + 2.0 * yScale);
    zTmp = readDepth(sample);
    coefZ = 1.0 / (epsilon + abs(Zp - zTmp));
    zsum += coefZ;
    sum += coefZ * texture2D(ssao, sample);

    vec4 amplitude;
    if (disableBlur) {
        amplitude = texture2D(ssao, sample);
    } else {
        amplitude = sum / zsum;
    }
    if (showOnlyAO) {
        gl_FragColor = amplitude;
    } else {
        gl_FragColor = texture2D(screen, coord) * amplitude;
    }
}
