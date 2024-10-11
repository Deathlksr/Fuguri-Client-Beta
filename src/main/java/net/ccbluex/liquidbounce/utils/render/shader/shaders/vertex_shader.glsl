#version 130

in vec3 position;  // Положение вершины
in vec2 texCoord;  // Текстурные координаты

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

out vec2 vTexCoord;

void main() {
    vTexCoord = texCoord;  // Передаем текстурные координаты во фрагментный шейдер
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
