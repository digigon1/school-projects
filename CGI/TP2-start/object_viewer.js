var gl;
var canvas;
var program, program1, program2;
var aspect;

var mModelView;
var mModelViewLoc;
var mNormals;
var mNormalsLoc;
var mProjection;
var mProjectionLoc;

window.onload = function init() {
    // Get the canvas
    canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }

    // Setup the contexts and the program
    gl = WebGLUtils.setupWebGL(canvas);
    program1 = initShaders(gl, "vertex-shader", "fragment-shader");
    
    document.getElementById("vertex").onchange = load_file;
    document.getElementById("fragment").onchange = load_file;

    gl.enable(gl.DEPTH_TEST);

    mModelViewLoc = gl.getUniformLocation(program1, "mModelView");
    mProjectionLoc = gl.getUniformLocation(program1, "mProjection");
    mNormalsLoc = gl.getUniformLocation(program1, "mNormals");

    gl.clearColor(0.3, 0.3, 0.3, 1.0);
    canvas.width = canvas.clientWidth;
    canvas.height = canvas.clientHeight;
    aspect = canvas.height / canvas.width;
    //gl.viewport(0,0,canvas.width, canvas.height);

    sphereInit(gl);
    cubeInit(gl);
    pyramidInit(gl);
    torusInit(gl);

    document.getElementById("gammaVal").value = Math.atan(Math.sqrt(Math.tan(radians(42))/Math.tan(radians(7)))) - Math.PI/2;
    document.getElementById("thetaVal").value = Math.asin(Math.sqrt(Math.tan(radians(42))*Math.tan(radians(7))));

    document.getElementById("alphaVal").value = radians(45);

    window.onresize = function() {
        canvas.width = canvas.clientWidth;
        canvas.height = canvas.clientHeight;

        aspect = canvas.height / canvas.width;
    }

    mProjection = mat4();

    gl.useProgram(program1);
    reset_program(program1);

    render();
}

function load_file() {
    var selectedFile = this.files[0];
    var reader = new FileReader();
    var id=this.id == "vertex" ? "vertex-shader-2" : "fragment-shader-2";
    reader.onload = (function(f){
        var fname = f.name;
        return function(e) {
            console.log(fname);
            console.log(e.target.result);
            console.log(id);
            document.getElementById(id).textContent = e.target.result;
            program2 = initShaders(gl, "vertex-shader-2", "fragment-shader-2");
            reset_program(program2);
            program = program2;
        }
    })(selectedFile);
    reader.readAsText(selectedFile);
}

function reset_program(prg) {
    mModelViewLoc = gl.getUniformLocation(prg, "mModelView");
    mNormalsLoc = gl.getUniformLocation(prg, "mNormals");
    mProjectionLoc = gl.getUniformLocation(prg, "mProjection");
    program = prg;
}

function drawObject(gl, program) 
{
	var drawType = document.getElementById("drawType").value;
	switch(document.getElementById("shape").value){
    	case "0":
    		drawType=="0"?
    			sphereDrawWireFrame(gl, program):
    			sphereDrawFilled(gl, program);
    		break;
    	case "1":
    		drawType=="0"?
    			cubeDrawWireFrame(gl, program):
    			cubeDrawFilled(gl, program);
    		break;
    	case "2":
    		drawType=="0"?
    			pyramidDrawWireFrame(gl, program):
    			pyramidDrawFilled(gl, program);
    		break;
    	case "3":
    		drawType=="0"?
    			torusDrawWireFrame(gl, program):
    			torusDrawFilled(gl, program);
    		break;
	}
}

function getModelView(){
    var choice = document.getElementById("projectionType").value;
    
    if(choice == "0"){
        var col = document.getElementsByClassName("axon");
        for (var i = col.length - 1; i >= 0; i--) {
        	col[i].classList.remove("hide");
        }
    } else {
        var col = document.getElementsByClassName("axon");
        for (var i = col.length - 1; i >= 0; i--) {
        	col[i].classList.add("hide");
        }
    }

    if(choice == "1"){
        var col = document.getElementsByClassName("obl");
        for (var i = col.length - 1; i >= 0; i--) {
        	col[i].classList.remove("hide");
        }
    } else {
        var col = document.getElementsByClassName("obl");
        for (var i = col.length - 1; i >= 0; i--) {
        	col[i].classList.add("hide");
        }
    }

    if(choice == "2"){
        var col = document.getElementsByClassName("perp");
        for (var i = col.length - 1; i >= 0; i--) {
        	col[i].classList.remove("hide");
        }
    } else {
        var col = document.getElementsByClassName("perp");
        for (var i = col.length - 1; i >= 0; i--) {
        	col[i].classList.add("hide");
        }
    }

    switch(choice){
        case "0": 
            var gamma = document.getElementById("gammaVal").value;
            var theta = document.getElementById("thetaVal").value;
            return mult(rotateX(gamma*180/Math.PI),rotateY(theta*180/Math.PI));
        case "1":
        	var l = document.getElementById("lVal").value;
        	var alpha = document.getElementById("alphaVal").value;

            return mat4(
            	[
            		1, 0, -l*Math.cos(alpha), 0,
            		0, 1, -l*Math.sin(alpha), 0,
            		0, 0, 1, 0,
            		0, 0, 0, 1
            	]
            ); //TODO
        case "2":
            var d = document.getElementById("dVal").value;
            return mat4(
                [
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, -1/d, 1
                ]
            ); //TODO
    }
    
}

function getNormals(){
    var choice = document.getElementById("projectionType").value;

    switch(choice){
        case "0": 
            return transpose(inverse(mModelView));
        default:
            return mat4();
    }
    
}

function render() 
{
	//drawObject(gl, program);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

    
    //TODO ortho(something)
    if(aspect>1){
        gl.uniformMatrix4fv(mProjectionLoc, false, flatten(ortho(-1,1,-aspect,aspect,-10,10)));
    }else{
        gl.uniformMatrix4fv(mProjectionLoc, false, flatten(ortho(-1/aspect,+1/aspect,-1,1,-10,10)));
    }

    var width = canvas.width/2;
    var height = canvas.height/2;

    mModelView = rotateX(90);
    // Top view
    gl.viewport(0,0,width,height);
    gl.uniformMatrix4fv(mModelViewLoc, false, flatten(mModelView));
    gl.uniformMatrix4fv(mNormalsLoc, false, flatten(transpose(inverse(mModelView))));
    drawObject(gl, program);

    
    mModelView = getModelView();
    // Other view
    gl.viewport(canvas.width/2,0,width,height);
    gl.uniformMatrix4fv(mModelViewLoc, false, flatten(mModelView));
    gl.uniformMatrix4fv(mNormalsLoc, false, flatten(getNormals()));
    drawObject(gl, program);
    

    mModelView = mat4();
    // Front view
    gl.viewport(0,canvas.height/2,width,height);
    gl.uniformMatrix4fv(mModelViewLoc, false, flatten(mModelView));
    gl.uniformMatrix4fv(mNormalsLoc, false, flatten(transpose(inverse(mModelView))));
    drawObject(gl, program);

    mModelView = rotateY(90);
    // Side view
    gl.viewport(canvas.width/2,canvas.height/2,width,height);
    gl.uniformMatrix4fv(mModelViewLoc, false, flatten(mModelView));
    gl.uniformMatrix4fv(mNormalsLoc, false, flatten(transpose(inverse(mModelView))));
    drawObject(gl, program);
	
	

    window.requestAnimationFrame(render);
}
