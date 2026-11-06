#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// Blocks in front of the scene over which a puff fades in, so ground and hulls don't cut it off in a line.
const float FADE_DISTANCE = 2.0;

float viewDistance(float depth) {
    return ProjMat[3][2] / (depth * 2.0 - 1.0 + ProjMat[2][2]);
}

// Vanilla's particle.fsh without its discard below 10% opacity, plus the fade against a copy of the scene's depth (Sampler1).
void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    float scene = viewDistance(texture(Sampler1, gl_FragCoord.xy / ScreenSize).r);
    color.a *= smoothstep(0.0, FADE_DISTANCE, scene - viewDistance(gl_FragCoord.z));
    if (color.a < 0.004) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
