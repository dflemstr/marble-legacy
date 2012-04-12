
varying vec3 normal;

void main(void) {
    vec3 n = normalize(normal);
    gl_FragColor = vec4(n.x * 0.5 + 0.5, n.y * 0.5 + 0.5, -n.z * 0.5 + 0.5, 1.0);
}
