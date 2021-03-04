var gl;
var loc;

window.onload = function init() {
    var canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }
    
    // Three vertices
    var vertices = [
        vec2(-0.5,-0.5),
        vec2(0.5,-0.5),
        vec2(0,0.5)
    ];

    var colors = [
    	vec4(1.0, 0.0, 0.0, 1.0),
    	vec4(0.0, 1.0, 0.0, 1.0),
    	vec4(0.0, 0.0, 1.0, 1.0)
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
    gl.bufferData(gl.ARRAY_BUFFER, flatten(vertices), gl.STATIC_DRAW);

    // Associate our shader variables with our vertex data buffer
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 2, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);

    // Load the color data into the GPU
    var colorBufferId = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, colorBufferId);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(colors), gl.STATIC_DRAW);

    // Associate our shader variables with our vertex data buffer
    var vColor = gl.getAttribLocation(program, "vColor");
    gl.vertexAttribPointer(vColor, 4, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vColor);

    loc = gl.getUniformLocation(program, "color");

    render();
}

function render() {
	gl.clear(gl.COLOR_BUFFER_BIT);

	gl.uniform4fv(loc, flatten(vec4(1.0, 0.5, 0.0, 1.0))); //orange inside
    gl.drawArrays(gl.TRIANGLES, 0, 3);

    gl.uniform4fv(loc, flatten(vec4(0.0, 0.0, 0.0, 1.0))); //black outline
    gl.drawArrays(gl.LINE_LOOP, 0, 3);
}
