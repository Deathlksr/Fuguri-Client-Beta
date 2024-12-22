#version 330 core

layout(location = 0) in vec3 aPos;  // Позиции вершин
layout(location = 1) in vec2 aUV;   // UV координаты для текстуры

out vec2 uvCoord;  // Передаем UV координаты в фрагментный шейдер

uniform mat4 model;       // Матрица модели
uniform mat4 view;        // Матрица вида
uniform mat4 projection;  // Матрица проекции

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    uvCoord = aUV;  // Прямо передаем UV координаты в фрагментный шейдер
}
