var gl;
var factor;
var fractalExpression;
var fractalType;
var isMouseDown;
var center;
var mouseMoveAmount;
var cent;
var scale;

window.onload = function init() {
    var canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }
    
    // Three vertices
    var vertices = [
        vec2(-1,1),
        vec2(-1,-1),
        vec2(1,1),
        vec2(1,-1)
    ];
    
    // Configure WebGL
    gl.viewport(0,0,canvas.width, canvas.height);
    gl.clearColor(1.0, 1.0, 1.0, 1.0);
    
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
    
    factor = 1.0;

    center = [0,0];
    mouseMoveAmount = [0, 0];

    canvas.addEventListener("mousedown", function(event){
    	isMouseDown = true;
    	mouseMoveAmount = [event.clientX, event.clientY];
    })

    canvas.addEventListener("mouseup", function(event){
    	isMouseDown = false;
    })

    canvas.addEventListener("mousemove", function(event){
    	if(isMouseDown){
    		var deltaX = mouseMoveAmount[0] - event.clientX;
    		var deltaY = -(mouseMoveAmount[1] - event.clientY);

    		center = [center[0]+scale*2*deltaX/512.0, center[1]+scale*2*deltaY/512.0];

    		

    		mouseMoveAmount = [event.clientX, event.clientY];
    	}
    })
	
    scale = 1.0;

    document.addEventListener("keydown", function(event){
    	switch(event.keyCode){
    	case 65: scale = scale*1.03; break;
    	case 81: scale = scale/1.03; break;
    	default: break;
    	}
    })

	var m = document.getElementById("mymenu");
	m.addEventListener("click", function() {
		switch (m.selectedIndex) {
			case 0:
				fractalExpression = vec2(0,0);
				fractalType = true;
				break;
			case 1:
				fractalExpression = vec2(-0.4, 0.6);
				fractalType = false;
				break;
			case 2:
				fractalExpression = vec2(0.285, 0.0);
				fractalType = false;
				break;
			case 3:
				fractalExpression = vec2(0.285, 0.01);
				fractalType = false;
				break;
			case 4:
				fractalExpression = vec2(-0.8, 0.156);
				fractalType = false;
				break;
			case 5:
				fractalExpression = vec2(0, -0.8);
				fractalType = false;
				break;
		}
	});
    
    fact = gl.getUniformLocation(program, "factor");
	fractalExpr = gl.getUniformLocation(program, "c");
	fractType = gl.getUniformLocation(program, "isMandelbrot");
	cent = gl.getUniformLocation(program, "center");
	sc = gl.getUniformLocation(program, "scale");
	
    factor = 0.5;
	fractalExpression = vec2(0,0);
	fractalType = true;
    
    render();
}

function render() {
	factor = document.getElementById("slide").value;
    gl.clear(gl.COLOR_BUFFER_BIT);
    gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
    gl.uniform1f(fact, factor);
	gl.uniform2fv(fractalExpr, fractalExpression);
	gl.uniform1i(fractType, fractalType);
	gl.uniform2fv(cent, center);
	gl.uniform1f(sc, scale);
	window.requestAnimFrame(render);
}

  
  