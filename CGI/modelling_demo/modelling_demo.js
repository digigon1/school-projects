var gl;

var canvas;

// GLSL programs
var program;

// Render Mode
var WIREFRAME=1;
var FILLED=2;
var renderMode = WIREFRAME;

var projection;
var modelView;
var view;

var x;
var y;
var baseAngle;
var firstArmAngle;
var secondArmAngle;
var handAngle;
var fingerDistance;

matrixStack = [];

function pushMatrix()
{
    matrixStack.push(mat4(modelView[0], modelView[1], modelView[2], modelView[3]));
}

function popMatrix() 
{
    modelView = matrixStack.pop();
}

function multTranslation(t) {
    modelView = mult(modelView, translate(t));
}

function multRotX(angle) {
    modelView = mult(modelView, rotateX(angle));
}

function multRotY(angle) {
    modelView = mult(modelView, rotateY(angle));
}

function multRotZ(angle) {
    modelView = mult(modelView, rotateZ(angle));
}

function multMatrix(m) {
    modelView = mult(modelView, m);
}
function multScale(s) {
    modelView = mult(modelView, scalem(s));
}

function initialize() {
    gl.clearColor(0.0, 0.0, 0.0, 1.0);
    gl.viewport(0,0,canvas.width, canvas.height);
    gl.enable(gl.DEPTH_TEST);
    
    program = initShaders(gl, "vertex-shader-2", "fragment-shader-2");
    
    cubeInit(gl);
    sphereInit(gl);
    cylinderInit(gl);
    
    setupProjection();
    setupView();
}

function setupProjection() {
    projection = perspective(100, 1, 0.1, 100);
    //projection = ortho(-5,5,-2.5,7.5,-10,10);
}

function setupView() {
    var x = parseFloat(document.getElementById("xVal").value);
    var y = parseFloat(document.getElementById("yVal").value);
    var z = parseFloat(document.getElementById("zVal").value);
    view = lookAt([x,z,y], [0,0,0], [0,1,0]);
    modelView = mat4(view[0], view[1], view[2], view[3]);

   	//modelView = mult(rotateX(gamma*180/Math.PI),rotateY(theta*180/Math.PI));
}

function setMaterialColor(color) {
    var uColor = gl.getUniformLocation(program, "color");
    gl.uniform3fv(uColor, color);
}

function sendMatrices()
{
    // Send the current model view matrix
    var mView = gl.getUniformLocation(program, "mView");
    gl.uniformMatrix4fv(mView, false, flatten(view));
    
    // Send the normals transformation matrix
    var mViewVectors = gl.getUniformLocation(program, "mViewVectors");
    gl.uniformMatrix4fv(mViewVectors, false, flatten(normalMatrix(view, false)));  

    // Send the current model view matrix
    var mModelView = gl.getUniformLocation(program, "mModelView");
    gl.uniformMatrix4fv(mModelView, false, flatten(modelView));
    
    // Send the normals transformation matrix
    var mNormals = gl.getUniformLocation(program, "mNormals");
    gl.uniformMatrix4fv(mNormals, false, flatten(normalMatrix(modelView, false)));  
}

function draw_sphere(color)
{
    setMaterialColor(color);
    sendMatrices();
    sphereDrawFilled(gl, program);
}

function draw_cube(color)
{
    setMaterialColor(color);
    sendMatrices();
    cubeDrawFilled(gl, program);
}

function draw_cylinder(color)
{
    setMaterialColor(color);
    sendMatrices();
    cylinderDrawFilled(gl, program);
}

function draw_scene()
{
	//TODO PDF
	
    pushMatrix();
    	multTranslation([0,-0.5,0]);
    	multScale([5,1,5]);
    	draw_cube([0.5,0.5,0.5])
    popMatrix();
    multTranslation([x,0,-y]);
    multTranslation([0,0.15,0]);
    pushMatrix();
    	multScale([2.2,0.3,2.2]);
    	draw_cube([1,0,0]);
    popMatrix();
    multTranslation([0,0.30,0]);
    multRotY(baseAngle);
    pushMatrix();
    	multScale([0.7,0.3,0.7]);
    	draw_cylinder([0.0, 1.0, 0.0]);
    popMatrix();
    multTranslation([0,0.4,0]);
    pushMatrix();
    	multScale([0.25,0.7,0.25]);
    	draw_cube([1,0,0]);
    popMatrix();
    multTranslation([0,0.3,0]);
    pushMatrix();
    	multRotX(90);
    	multScale([0.4,0.4,0.4]);
    	draw_cylinder([0,0,1]);
    popMatrix();
    multRotZ(firstArmAngle);
    multTranslation([0,0.6,0]);
    pushMatrix();
    	multScale([0.25,1,0.25]);
    	draw_cube([1,0,0]);
    popMatrix();
    multTranslation([0,0.4,0]);
    pushMatrix();
    	multRotX(90);
    	multScale([0.4,0.4,0.4]);
    	draw_cylinder([1,1,0]);
    popMatrix();
    multRotZ(secondArmAngle);
    multTranslation([0,0.8,0]);
    pushMatrix();
    	multScale([0.25,1.4,0.25]);
    	draw_cube([1,0,0]);
    popMatrix();
    multTranslation([0,0.6,0]);
    multRotY(handAngle);
    pushMatrix();
    	multScale([1,0.3,1]);
    	draw_cylinder([0.9,0.9,0.9]);
    popMatrix();
    multTranslation([0,0.5,0]);
    multScale([0.15,0.7,0.15]);
    pushMatrix();
    	multTranslation([fingerDistance,0,0]);
    	draw_cube([1,1,0]);
    popMatrix();
    pushMatrix();
    	multTranslation([-fingerDistance,0,0]);
    	draw_cube([1,1,0]);
    popMatrix();
}

function render() {
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

    gl.useProgram(program);
    
    setupView();
    
    // Send the current projection matrix
    var mProjection = gl.getUniformLocation(program, "mProjection");
    gl.uniformMatrix4fv(mProjection, false, flatten(projection));
        
    draw_scene();
    
    requestAnimFrame(render);
}


window.onload = function init()
{
    canvas = document.getElementById("gl-canvas");
    gl = WebGLUtils.setupWebGL(canvas);
    if(!gl) { alert("WebGL isn't available"); }

    x = 0;
    y = 0;
    baseAngle = 0;
    firstArmAngle = 0;
    secondArmAngle = 0;
    handAngle = 0;
    fingerDistance = 2;

    window.addEventListener("keydown", effect, false);
    
    initialize();
            
    render();
}

function effect(event){
	switch(event.keyCode){
		case 38: y +=(y<1.4?0.05:0); break;
		case 40: y -=(y>-1.4?0.05:0); break;
		case 37: x -=(x>-1.4?0.05:0); break;
		case 39: x +=(x<1.4?0.05:0); break;
		case 81: baseAngle +=1; break;
		case 87: baseAngle -=1; break;
		case 90: firstArmAngle +=(firstArmAngle < 45?1:0); break;
		case 88: firstArmAngle -=(firstArmAngle > -45?1:0); break;
		case 65: secondArmAngle +=(secondArmAngle < 45?1:0); break;
		case 83: secondArmAngle -=(secondArmAngle > -45?1:0); break;
		case 75: handAngle +=1; break;
		case 76: handAngle -=1; break;
		case 79: fingerDistance +=(fingerDistance<2.5?0.05:0); break;
		case 80: fingerDistance -=(fingerDistance>0.6?0.05:0); break;
		default: break;
	}
}
