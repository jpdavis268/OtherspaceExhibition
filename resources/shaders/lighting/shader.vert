#version 330 core

layout (location=0) in vec2 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 lightPos;
layout (location=3) in float scaleFactor;

uniform mat4 uProjection;

out float lRadius;
out float lStrength;
out vec2 lPosition;
out vec2 fPosition;

void main() {
    lRadius = aColor.b * scaleFactor;
    lStrength = aColor.w;
    gl_Position = uProjection * vec4(aPos, 0, 1);
    lPosition = lightPos;
    fPosition = aPos;
}
