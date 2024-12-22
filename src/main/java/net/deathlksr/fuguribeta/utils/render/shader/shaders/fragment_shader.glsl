#version 330 core

in vec2 uvCoord;  // UV координаты из вершинного шейдера

out vec4 FragColor;  // Итоговый цвет пикселя

uniform vec3 innerColor;    // Внутренний цвет круга
uniform vec3 glowColor;     // Цвет свечения
uniform float glowRadius;   // Радиус свечения
uniform float glowIntensity; // Интенсивность свечения

void main() {
    // Центр круга - это точка (0.5, 0.5) в UV координатах
    vec2 uv = uvCoord - vec2(0.5, 0.5);
    float dist = length(uv);  // Расстояние от центра круга до текущей точки

    // Градиент свечения
    float glow = smoothstep(0.5, 0.5 + glowRadius, dist) * glowIntensity;

    // Смешивание внутреннего цвета и цвета свечения
    vec3 finalColor = mix(innerColor, glowColor, glow);

    // Прозрачность (постепенное исчезновение к границам)
    float alpha = 1.0 - smoothstep(0.5, 0.5 + glowRadius, dist);

    FragColor = vec4(finalColor, alpha);
}
