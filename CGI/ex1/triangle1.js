var gl;
var counter = 0;

var TRIANGLE_NUMBER = 5000;
var TRIANGLES_TO_DRAW = 200;

window.onload = function init() {
    var canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }
    
    // Three vertices
    var vertices = [];
    var last1 = 0.0, last2 = 0.0;

    for (var i = TRIANGLE_NUMBER * 3; i >= 0; i--) {
        last1 += (2*Math.random() - 1.0)/80;
        last2 += (2*Math.random() - 1.0)/80;
        vertices.push(vec2(last1, last2));
    }

    /*var vertices = [
        vec2(-0.5,-0.5),
        vec2(0.5,-0.5),
        vec2(0.5,0.5),
        vec2(-0.5,0.5),

        vec2(-0.5, -0.5),
        vec2(-0.5, 0.5),
        vec2(0.5, 0.5),

        vec2(-0.5, -0.5),
        vec2(0.5, -0.5),
        vec2(0.5, 0.5)
    ];*/
    
    // Configure WebGL
    gl.viewport(0,0,canvas.width, canvas.height);
    gl.clearColor(1.0, 0.0, 1.0, 1.0); //Background color
    
    // Load shaders and initialize attribute buffers
    var program = initShaders(gl, "vertex-shader", "fragment-shader");
    gl.useProgram(program);

    // Load the data into the GPU
    var bufferId = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, bufferId);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(vertices), gl.STATIC_DRAW);

    // Associate our shader variables with our data buffer
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 2, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);
    requestAnimationFrame(render);
    render();
}

function render() {
    gl.clear(gl.COLOR_BUFFER_BIT);
    gl.drawArrays(gl.TRIANGLES, counter, TRIANGLES_TO_DRAW * 3); //Type of vertex drawn
    counter = (counter+3)%(3*(TRIANGLE_NUMBER - TRIANGLES_TO_DRAW));
    requestAnimationFrame(render);
}