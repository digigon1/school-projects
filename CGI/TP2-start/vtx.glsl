precision mediump float;

attribute vec4 vPosition;
attribute vec3 vNormal;

uniform mat4 mProjection;
uniform mat4 mModelView;
uniform mat4 mNormals;

varying vec4 test;

void main(){
    gl_Position = mProjection * mModelView * vPosition;

    test = vec4((vNormal+1.0)/2.0, 1.0);
}