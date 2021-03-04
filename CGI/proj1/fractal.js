var gl;
var factor;
var mandel;
var zoom;
var zoom_val;
var julia;
var center;
var mouse_x;
var mouse_y;
var center_x;
var center_y;
var clicked;

window.onload = function init() {

    zoom_val = 1.0;
    center_x = 0;
    center_y = 0;


    var canvas = document.getElementById("gl-canvas");

    addEventListener("keydown", keyDownEvent, false);

    canvas.addEventListener("mousedown", mouseDownEvent, false);
    canvas.addEventListener("mousemove", mouseMoveEvent, false);
    canvas.addEventListener("mouseup", mouseUpEvent, false);


    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }
    
    // Three vertices
    var vertices = [
        vec2(-1,-1),
        vec2(1,-1),
        vec2(-1,1),
        vec2(1,1)
    ];
    
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

    factor = gl.getUniformLocation(program, "fact");
    mandel = gl.getUniformLocation(program, "mandelbrot");
    zoom = gl.getUniformLocation(program, "zoom");
    julia = gl.getUniformLocation(program, "julia_c");
    center = gl.getUniformLocation(program, "center");

    render();
}

function keyDownEvent(key){
    var keycode = key.keyCode;
    switch(keycode){
        case 81: zoom_val /= 1.05; break;
        case 65: zoom_val *= 1.05; break;
        default: break;
    }
}

function mouseDownEvent(click){
	mouse_x = click.clientX;
	mouse_y = click.clientY;
    clicked = true;
}

function mouseMoveEvent(click){
    if(clicked){
	   mouse_x -= click.clientX;
	   mouse_y -= click.clientY;
	   center_x += (zoom_val)*mouse_x/256.0;
	   center_y -= (zoom_val)*mouse_y/256.0;
       mouse_x = click.clientX;
       mouse_y = click.clientY;
    }
}

function mouseUpEvent(click){
    clicked = false;
}

function render() {

    gl.clear(gl.COLOR_BUFFER_BIT);

    //factor value
    var fact_val = document.getElementById("factor").value;
    gl.uniform1f(factor, fact_val);

    //is mandelbrot
    gl.uniform1i(mandel, document.getElementById("type").value);

    //julia value
    gl.uniform2f(julia, document.getElementById("julia_r").value, document.getElementById("julia_i").value);

    //scale
    gl.uniform1f(zoom, zoom_val);

    //center
    gl.uniform2f(center, center_x, center_y);

    gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4); //Type of vertex drawn

    requestAnimationFrame(render);
}

function check(option){
    zoom_val = 1.0;
    center_x = 0.0;
    center_y = 0.0;
    if(option == "0"){
        document.getElementById("julia").style.display="block";
    } else {
        document.getElementById("julia").style.display="none";
    }
}