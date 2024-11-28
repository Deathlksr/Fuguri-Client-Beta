#ifdef GL_ES
precision highp float;
#endif

uniform float time;
uniform vec2 resolution;

const float COUNT = 10.0;

void main( void ) {
    vec2 uPos = ( gl_FragCoord.xy / resolution.y );//normalize wrt y axis
    uPos -= vec2((resolution.x/resolution.y)/2.0, 0.1);//shift origin to center

    float vertColor = 0.0;
    for(float i=0.0; i<COUNT; i++){
        float t = time/3.0 + (i+0.1);

        uPos.y += sin(-t+uPos.x*9.0)*0.1;
        uPos.x += cos(-t+uPos.y*6.0+cos(t/1.0))*0.15;
        float value = (sin(uPos.y*10.0) + uPos.x*5.1);

        float stripColor = 1.0/sqrt(abs(value))*3.0;

        vertColor += stripColor/50.0;
    }

    float temp = vertColor;
    vec3 color = vec3(temp*0.2, temp*0.0, temp*1.0);
    color *= color.r+color.g+color.b;
    gl_FragColor = vec4(color, 1.0);
}