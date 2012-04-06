const float epsilon = 0.005;

uniform sampler2D ssao;
uniform sampler2D normals;
uniform float frustumNear;
uniform float frustumFar;
uniform vec2 scale;

varying vec2 coord;

float readDepth(vec2 uv) {
    float depthv = texture2D(normals, uv).a;
    return (2.0 * frustumNear) / (frustumFar + frustumNear - depthv * (frustumFar - frustumNear));
}

void main(void) {
    vec4 sum = vec4(0.0);

    float x = coord.x;
    float y = coord.y;

    float xScale = scale.x;
    float yScale = scale.y;

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

    gl_FragColor = sum / zsum;
}
