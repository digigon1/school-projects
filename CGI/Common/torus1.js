var torus_points = [];
var torus_normals = [];
var torus_faces = [];
var torus_edges = [];

var torus_points_buffer;
var torus_normals_buffer;
var torus_faces_buffer;
var torus_edges_buffer;

var torus_LATS=20;
var torus_LONS=30;

function torusInit(gl) {
	torusBuild(torus_LATS, torus_LONS);
	torusUploadData(gl);
}

// Generate points using polar coordinates
function torusBuild(nlat, nlon) 
{
	// phi will be latitude
	// theta will be longitude
 
	var d_phi = Math.PI / nlat;
	var d_theta = 2*Math.PI / nlon;
	var r = 0.2;
	var R = 0.7;
	
	// Generate north polar cap
	for(var i=0, theta = 0; i<nlon; i++, theta+=d_theta){
		var north = vec3(R*Math.cos(theta),r,R*Math.sin(theta));
		torus_points.push(north);
		torus_normals.push(vec3(0,1,0));
	}
	
	/**/
	// Generate middle
	for(var j=0, theta=0; j<nlon; j++, theta+=d_theta) {
		for(var i=0, phi=Math.PI/2-d_phi; i<nlat; i++, phi-=d_phi) {
			var pt = vec3((R*Math.cos(theta) + r*Math.cos(phi)*Math.cos(theta)),r*Math.sin(phi),R*Math.sin(theta) + r*Math.cos(phi)*Math.sin(theta));
			torus_points.push(pt);
			var n = vec3(pt);
			torus_normals.push(normalize(n));
		}
	}
	/**/

	/**/
	for(var j=0, theta=0; j<nlon; j++, theta+=d_theta) {
		for(var i=0, phi=Math.PI/2-d_phi; i<nlat; i++, phi-=d_phi) {
			var pt = vec3(R*Math.cos(theta) - r*Math.cos(phi)*Math.cos(theta),r*Math.sin(phi),R*Math.sin(theta) - r*Math.cos(phi)*Math.sin(theta));
			torus_points.push(pt);
			var n = vec3(pt);
			torus_normals.push(normalize(n));
		}
	}
	/**/
	
	// Generate south cap
	for(var i=0, theta = 0; i<nlon; i++, theta+=d_theta){
		var north = vec3(R*Math.cos(theta),-r,R*Math.sin(theta));
		torus_points.push(north);
		torus_normals.push(vec3(0,-1,0));
	}

	// Generate the faces
	
	/**/
	// north pole faces
	
	/**/
	for(var j=0; j<nlon-1; j++){
		torus_faces.push(j);
		torus_faces.push(j+1);
		torus_faces.push(j*nlat + nlon);

		torus_faces.push(j+1);
		torus_faces.push(j*nlat + nlon);
		torus_faces.push((j+1)*nlat + nlon);
	}
	/**/
	
	torus_faces.push(nlon-1);
	torus_faces.push(0);
	torus_faces.push((nlon-1)*nlat + nlon);

	torus_faces.push(0);
	torus_faces.push((nlon-1)*nlat + nlon);
	torus_faces.push(nlon);
	

	for(var j=0; j<nlon-1; j++){
		torus_faces.push(j);
		torus_faces.push(j+1);
		torus_faces.push(j*nlat + nlon + nlon*nlat);

		torus_faces.push(j+1);
		torus_faces.push(j*nlat + nlon + nlon*nlat);
		torus_faces.push((j+1)*nlat + nlon + nlon*nlat);
	}
	/**/
	
	torus_faces.push(nlon-1);
	torus_faces.push(0);
	torus_faces.push((nlon-1)*nlat + nlon + nlon*nlat);

	torus_faces.push(0);
	torus_faces.push((nlon-1)*nlat + nlon + nlon*nlat);
	torus_faces.push(nlon + nlon*nlat);

	/**/

	/**/
	// general middle faces
	/**/
	for(var j=0; j<nlon; j++) {
		for(var i=0; i<nlat-1; i++) {
			var p = nlon + j*nlat + i;

			
			if(j != nlon-1){
				
				torus_faces.push(p);
				torus_faces.push(p+1);
				torus_faces.push(p+nlat);

				torus_faces.push(p+nlat);
				torus_faces.push(p+nlat+1);
				torus_faces.push(p+1);
				
			} else {
				
				torus_faces.push(p);
				torus_faces.push(p+1);
				torus_faces.push(p-nlat*(nlon-1));

				torus_faces.push(p+1);
				torus_faces.push(p-nlat*(nlon-1));
				torus_faces.push(p-nlat*(nlon-1)+1);
				
			}
		}		
	}

	for(var j=0; j<nlon; j++) {
		for(var i=0; i<nlat-1; i++) {
			var p = nlon*nlat + nlon + j*nlat + i;

			
			if(j != nlon-1){
				
				torus_faces.push(p);
				torus_faces.push(p+1);
				torus_faces.push(p+nlat);

				torus_faces.push(p+nlat);
				torus_faces.push(p+nlat+1);
				torus_faces.push(p+1);
				
			} else {
				
				torus_faces.push(p);
				torus_faces.push(p+1);
				torus_faces.push(p-nlat*(nlon-1));

				torus_faces.push(p+1);
				torus_faces.push(p-nlat*(nlon-1));
				torus_faces.push(p-nlat*(nlon-1)+1);
				
			}
		}		
	}
	/**/

	
	// south pole faces
	/**/
	//TODO



	// Build the edges
	for(var i=0; i<nlon-1; i++) {
		torus_edges.push(i);   // North pole 
		torus_edges.push(i+1);
	}
	torus_edges.push(nlon-1);
	torus_edges.push(0);


	/**/
	for(var i=0; i<nlon; i++){ //TODO
		torus_edges.push(i);
		torus_edges.push((i)*nlat + nlon);
	}

	for(var i=0; i<nlon; i++){ //TODO
		torus_edges.push(i);
		torus_edges.push(nlat*nlon + (i)*nlat + nlon);
	}
	/**/
	
	/**/
	for(var j=0; j<nlon; j++) {
		for(var i=0; i<nlat; i++) {
			var p = nlon + j*nlat + i;
			   // vert line (same latitude)
			if(i < nlat-1){
				torus_edges.push(p);
				torus_edges.push(p+1);
			}
			
			
			torus_edges.push(p);
			if(j!=nlon-1) {
				torus_edges.push(p+nlat); // horz line (same longitude)
			} else {
				torus_edges.push(p-nlat*(nlon-1));
			}
			
		}
	}
	/**/

	/**/
	for(var j=0; j<nlon; j++) {
		for(var i=0; i<nlat; i++) {
			var p = nlon*nlat + nlon + j*nlat + i;
			// horizontal line (same latitude)
			if(i < nlat-1) {
				torus_edges.push(p);
				torus_edges.push(p+1);
			}
			
			
			torus_edges.push(p);	 // vertical line (same longitude)
			if(j!=nlon-1) {
				torus_edges.push(p+nlat);
			} else {
				torus_edges.push(p-nlat*(nlon-1));
			}
			
		}
	}
	/**/
	
	/**/
	var offset = 2*nlon*nlat+ nlon;
	for(var i=0; i<nlon-1; i++) {
		torus_edges.push(offset+i);   // North pole 
		torus_edges.push(offset+i+1);
	}
	
	torus_edges.push(offset+nlon-1);
	torus_edges.push(offset);
	/**/
	
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

