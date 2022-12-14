#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    color = vec4(color.rgb, color.a * vertexColor[3]); // Only want to change transparency
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}

