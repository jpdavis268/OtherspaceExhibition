#version 330 core

in vec2 fragPos;

in vec4 fColor;
in vec2 fTexCoords;
in float fMod;

uniform sampler2D uTexture;

out vec4 color;

void main() {
    if (fMod > 0) {
        // If fMod is greater than 0, draw a texture.
        int id = int(fMod);
        color = fColor * texture(uTexture, fTexCoords);
    }
    else {
        // If texture is null, draw primitives.
        // Rounded Rectangle
        if (fMod < 0) {
            vec2 distance = abs(vec2(fTexCoords.x - fragPos.x, fTexCoords.y - fragPos.y));

            if (pow(distance.x, 8) + pow(distance.y, 8) >= pow(-fMod, 8)) {
                discard;
            }
        }
        color = fColor;
    }
}