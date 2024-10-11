#version 130

in vec2 vTexCoord;  // Текстурные координаты из вершинного шейдера

uniform vec3 glowColor;  // Цвет свечения
uniform float glowRadius; // Радиус свечения
uniform float time;  // Время для анимации свечения

out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5, 0.5);  // Центр круга
    float distanceFromCenter = distance(vTexCoord, center);  // Расстояние от центра

    // Интенсивность свечения на основе расстояния
    float glowFactor = 1.0 - smoothstep(0.0, glowRadius, distanceFromCenter);

    // Анимация свечения
    float animatedGlow = 0.5 + 0.5 * sin(time * 3.14159);

    // Окончательная интенсивность свечения
    glowFactor *= animatedGlow;

    // Цвет свечения
    fragColor = vec4(glowColor * glowFactor, 1.0);
}
