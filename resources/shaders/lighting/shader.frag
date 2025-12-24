#version 330 core

in vec2 lPosition;
in vec2 fPosition;
in float lRadius;
in float lStrength;

out vec4 color;

void main() {
    vec2 distance = vec2(lPosition.x - fPosition.x, lPosition.y - fPosition.y);

    float alpha = clamp((pow(lRadius, 2) - (pow(distance.x, 2) + pow(distance.y, 2))) / pow(lRadius, 2), 0, 1) * lStrength;

    color = vec4(0.0, 0.0, 0.0, alpha);
}
