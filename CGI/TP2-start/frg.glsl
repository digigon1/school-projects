precision mediump float;

varying vec4 test;

uniform mat4 mProjection;
uniform mat4 mModelView;
uniform mat4 mNormals;
        
void main() {
    gl_FragColor = vec4(1.0, 1.0, 1.0, 2.0) - test; //vec4(1.0, 1.0, 1.0, 1.0);
}