
uniform bool blendEnabled;

varying vec3 normal;

void main(void) {
    vec3 n = normalize(normal);
    gl_FragColor = vec4(n.xy * 0.5 + 0.5, -n.z * 0.5 + 0.5, gl_FragCoord.z);
}
