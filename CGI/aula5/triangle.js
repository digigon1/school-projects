var gl;
var loc;
var ctm;
var ctm_loc;
var trans;
var scale;
var rot;

window.onload = function init() {
    var canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }
    
    // Three vertices
    var vertex_color = [
        vec3(-0.5, -0.5, 0.0),
        vec3(0.5,-0.5, 0.0),
        vec3(0, 0.5, 0.0),
        vec3(1.0, 0.0, 0.0),
    	vec3(0.0, 1.0, 0.0),
    	vec3(0.0, 0.0, 1.0)
    ];

    
    // Configure WebGL
    gl.viewport(0,0,canvas.width, canvas.height);
    gl.clearColor(1.0, 1.0, 1.0, 1.0);
    
    // Load shaders and initialize attribute buffers
    var program = initShaders(gl, "vertex-shader", "fragment-shader");
    gl.useProgram(program);

    // Load the vertex data into the GPU
    var vertBufferId = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vertBufferId);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(vertex_color), gl.STATIC_DRAW);

    // Associate our shader variables with our vertex data buffer
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);

    // Associate our shader variables with our vertex data buffer
    var vColor = gl.getAttribLocation(program, "vColor");
    gl.vertexAttribPointer(vColor, 3, gl.FLOAT, false, 0, 3*3*4);
    gl.enableVertexAttribArray(vColor);

    loc = gl.getUniformLocation(program, "color");

    trans = mat4();
    ctm = mat4();
    scale = mat4();
    rot = mat4();

    ctm_loc = gl.getUniformLocation(program, "ctm");

    render();
}

function render() {

    ctm = mult(mult(trans, scale), rot);
    gl.uniformMatrix4fv(ctm_loc, false, flatten(ctm));

	gl.clear(gl.COLOR_BUFFER_BIT);

	gl.uniform4fv(loc, flatten(vec4(1.0, 0.5, 0.0, 1.0))); //orange inside
    gl.drawArrays(gl.TRIANGLES, 0, 3);

    gl.uniform4fv(loc, flatten(vec4(0.0, 0.0, 0.0, 1.0))); //black outline
    gl.drawArrays(gl.LINE_LOOP, 0, 3);

    requestAnimationFrame(render);
}

function changeX(deltaX){
    trans[3][0] = deltaX*1.0;
}

function changeY(deltaY){
    trans[3][1] = deltaY*1.0;
}

function scaleX(deltaX){
    scale[0][0] = deltaX*1.0;
}

function scaleY(deltaY){
    scale[1][1] = deltaY*1.0;
}

function changeRot(angle){
    rot = rotateZ(angle);
}