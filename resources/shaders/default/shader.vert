#version 330 core

layout (location=0) in vec2 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in float aMod;

uniform mat4 uProjection;

out vec4 fColor;
out vec2 fTexCoords;
out float fMod;
out vec2 fragPos;

void main() {
    fColor = aColor;
    fTexCoords = aTexCoords;
    fMod = aMod;
    gl_Position = uProjection * vec4(aPos, 0, 1);
    fragPos = aPos.xy;
}