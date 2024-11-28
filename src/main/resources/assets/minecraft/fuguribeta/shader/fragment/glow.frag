#version 120

uniform sampler2D texture;
uniform vec2 texelSize;

uniform vec3 color;
uniform int radius;
uniform float fade;
uniform float targetAlpha;

void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    if (centerCol.a != 0.0) {
        gl_FragColor = vec4(centerCol.rgb * color, targetAlpha);
        return;
    }

    float alpha = 0.0;

    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            vec4 currentColor = texture2D(texture, gl_TexCoord[0].xy + vec2(texelSize.x * x, texelSize.y * y));
            float distanceSquared = float(x * x + y * y);

            if (currentColor.a > 0.0) {
                if (fade > 0.0) {
                    alpha += max(0.0, (radius - sqrt(distanceSquared)) / float(radius));
                } else {
                    alpha += 1.0;
                }
            }
        }
    }

    alpha = min(alpha / float(fade), 1.0);

    gl_FragColor = vec4(color, alpha);
}
