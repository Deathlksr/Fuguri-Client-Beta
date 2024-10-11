#version 120

uniform sampler2D texture;
uniform vec2 texelSize; // Размер текстурного пикселя (1/текстура_ширина, 1/текстура_высота)

uniform vec3 color;      // Цвет свечения
uniform int radius;      // Радиус свечения
uniform float fade;      // Уровень затухания
uniform float targetAlpha; // Альфа-канал для конечного цвета

void main() {
    // Получаем цвет центра
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    // Если центр не прозрачный, возвращаем его цвет
    if (centerCol.a != 0.0) {
        gl_FragColor = vec4(centerCol.rgb * color, targetAlpha);
        return;
    }

    // Инициализация переменной для альфа-канала
    float alpha = 0.0;

    // Итерация по всем пикселям в радиусе
    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            // Получаем цвет текущего пикселя
            vec4 currentColor = texture2D(texture, gl_TexCoord[0].xy + vec2(texelSize.x * x, texelSize.y * y));
            float distanceSquared = float(x * x + y * y);

            // Если текущий пиксель не прозрачный, добавляем к альфа
            if (currentColor.a > 0.0) {
                // Рассчитываем альфа-канал в зависимости от расстояния и затухания
                if (fade > 0.0) {
                    alpha += max(0.0, (radius - sqrt(distanceSquared)) / float(radius));
                } else {
                    alpha += 1.0;
                }
            }
        }
    }

    // Нормализация альфа-канала по уровню затухания
    alpha = min(alpha / float(fade), 1.0);

    // Применяем цвет свечения и финальный альфа-канал
    gl_FragColor = vec4(color, alpha);
}
