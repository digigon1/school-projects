var torus_points = [];
var torus_normals = [];
var torus_faces = [];
var torus_edges = [];

var torus_points_buffer;
var torus_normals_buffer;
var torus_faces_buffer;
var torus_edges_buffer;

var SPHERE_LATS=20;
var SPHERE_LONS=30;

function torusInit(gl) {
    torusBuild(SPHERE_LATS, SPHERE_LONS);
    torusUploadData(gl);
}

function normal(a, b, c){
	return cross(subtract(torus_points[a],torus_points[b]), subtract(torus_points[a],torus_points[c]));
}

// Generate points using polar coordinates
function torusBuild(nlat, nlon) 
{
    // phi will be latitude
    // theta will be longitude
 
    var d_phi = 2*Math.PI / (nlat);
    var d_theta = 2*Math.PI / nlon;
    var r = 0.2;
    var R = 0.7;
    
    // Generate north polar cap
    /*
    var north = vec3(0,r,0);
    torus_points.push(north);
    torus_normals.push(vec3(0,1,0));
    */
    
    // Generate middle
    for(var i=0, phi=Math.PI/2-d_phi; i<nlat; i++, phi-=d_phi) {
        for(var j=0, theta=0; j<nlon; j++, theta+=d_theta) {
            var pt = vec3(R*Math.cos(theta) + r*Math.cos(phi)*Math.cos(theta),r*Math.sin(phi),R*Math.sin(theta) + r*Math.cos(phi)*Math.sin(theta));
            torus_points.push(pt);
            //var n = vec3(pt);
            //torus_normals.push(normalize(n));
        }
    }
    
    // Generate norh south cap
    /*
    var south = vec3(0,-r,0);
    torus_points.push(south);
    torus_normals.push(vec3(0,-1,0));
    */
    
    // Generate the faces
    
    // north pole faces
    /*
    for(var i=0; i<nlon-1; i++) {
        torus_faces.push(0);
        torus_faces.push(i+1);
        torus_faces.push(i+2);
    }
    torus_faces.push(0);
    torus_faces.push(nlon);
    torus_faces.push(1);
    */
    
    /**/
    // general middle faces
    var offset=0;
    
    for(var i=0; i<nlat-1; i++) {
        for(var j=0; j<nlon-1; j++) {
            var p = offset+i*nlon+j;
            torus_faces.push(p);
            torus_faces.push(p+nlon);
            torus_faces.push(p+nlon+1);
            
            torus_faces.push(p);
            torus_faces.push(p+nlon+1);
            torus_faces.push(p+1);
        }
        var p = offset+i*nlon+nlon-1;
        torus_faces.push(p);
        torus_faces.push(p+nlon);
        torus_faces.push(p+1);

        torus_faces.push(p);
        torus_faces.push(p+1);
        torus_faces.push(p-nlon+1);
    }
    /**/

    /**/
    // south pole faces
    var offset = (nlat-1) * nlon;
    for(var j=0; j<nlon-1; j++) {

        torus_faces.push(j);
        torus_faces.push(offset+j+1);
        torus_faces.push(offset+j);

        torus_faces.push(j);
        torus_faces.push(j+1);
        torus_faces.push(offset+j+1);

    }

    torus_faces.push(offset+nlon-1);
    torus_faces.push(nlon-1);
    torus_faces.push(0);
    
    torus_faces.push(offset+nlon-1);
    torus_faces.push(offset);
    torus_faces.push(0);
    /**/
 
    // Build the edges
    /*
    for(var i=0; i<nlon; i++) {
        torus_edges.push(0);   // North pole 
        torus_edges.push(i+1);
    }
    */

    for(var i=0; i<nlat; i++, p++) {
        for(var j=0; j<nlon;j++, p++) {

            var p = i*nlon + j;
            torus_edges.push(p);   // horizontal line (same latitude)
            
            var hor;
            if(j!=nlon-1) {
            	hor = p+1;
                torus_edges.push(p+1);
            } else {
            	hor = p+1-nlon;
                torus_edges.push(p+1-nlon);
            }
            
            var ver;
            torus_edges.push(p);   // vertical line (same longitude)
            if(i!=nlat-1) {
            	ver = p+nlon;
                torus_edges.push(p+nlon);
            } else {
            	ver = p-i*nlon;
                torus_edges.push(p-i*nlon);
            }

            torus_normals.push(normal(p, hor, ver));
        }
    }
    
}

function torusUploadData(gl)
{
    torus_points_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, torus_points_buffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(torus_points), gl.STATIC_DRAW);
    
    torus_normals_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, torus_normals_buffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(torus_normals), gl.STATIC_DRAW);
    
    torus_faces_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, torus_faces_buffer);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(torus_faces), gl.STATIC_DRAW);
    
    torus_edges_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, torus_edges_buffer);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(torus_edges), gl.STATIC_DRAW);
}

function torusDrawWireFrame(gl, program)
{    
    gl.useProgram(program);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, torus_points_buffer);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, torus_normals_buffer);
    var vNormal = gl.getAttribLocation(program, "vNormal");
    gl.vertexAttribPointer(vNormal, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vNormal);
    
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, torus_edges_buffer);
    gl.drawElements(gl.LINES, torus_edges.length, gl.UNSIGNED_SHORT, 0);
}

function torusDrawFilled(gl, program)
{
    gl.useProgram(program);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, torus_points_buffer);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, torus_normals_buffer);
    var vNormal = gl.getAttribLocation(program, "vNormal");
    gl.vertexAttribPointer(vNormal, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vNormal);
    
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, torus_faces_buffer);
    gl.drawElements(gl.TRIANGLES, torus_faces.length, gl.UNSIGNED_SHORT, 0);
}

