
varying vec2 coord;

void main(void) {
   vec2 position = sign(gl_Vertex.xy);
   gl_Position = vec4(position.xy, 0, 1);
   coord = vec2(0.5 * (1.0 + position.x), 0.5 * (1.0 + position.y));
}
