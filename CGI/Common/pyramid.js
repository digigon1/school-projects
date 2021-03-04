pyramid_vertices = [
    //vec3(-0.5, -0.5, +0.5),     // 0
    //vec3(+0.5, -0.5, +0.5),     // 1
    //vec3(+0.5, +0.5, +0.5),     // 2
    vec3(+0.0, +0.5, +0.0),     // 0
    vec3(-0.5, -0.5, -0.5),     // 1
    vec3(+0.5, -0.5, -0.5),     // 2
    vec3(+0.5, -0.5, +0.5),     // 3
    vec3(-0.5, -0.5, +0.5)      // 4
];

var pyramid_points = [];
var pyramid_normals = [];
var pyramid_faces = [];
var pyramid_edges = [];

var pyramid_points_buffer;
var pyramid_normals_buffer;
var pyramid_faces_buffer;
var pyramid_edges_buffer;

function pyramidInit(gl) {
    pyramidBuild();
    drawNormals(pyramid_points, pyramid_normals, pyramid_edges);
    pyramidUploadData(gl);
}

function pyramidBuild()
{
    pyramidAddBase(1,2,3,4,vec3(0,-1,0));

    pyramidAddFace(1,2,0,cross(subtract(pyramid_vertices[1],pyramid_vertices[0]), subtract(pyramid_vertices[1],pyramid_vertices[2])));
    pyramidAddFace(2,3,0,cross(subtract(pyramid_vertices[2],pyramid_vertices[0]), subtract(pyramid_vertices[2],pyramid_vertices[3])));
    pyramidAddFace(3,4,0,cross(subtract(pyramid_vertices[3],pyramid_vertices[0]), subtract(pyramid_vertices[3],pyramid_vertices[4])));
    pyramidAddFace(4,1,0,cross(subtract(pyramid_vertices[4],pyramid_vertices[0]), subtract(pyramid_vertices[4],pyramid_vertices[1])));
}

function drawNormals(points, normals, edges)
{
    var len = points.length;
    for (var i=0; i<len; i++)
    {
        var v1 = points[i];
        var v2 = normals[i];
       
        var v = vec3(v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2]);
       
        var offset = points.length;
        points.push(v);
        normals.push(v);
       
        edges.push(i);
        edges.push(offset);
    }
}

function pyramidUploadData(gl)
{
    pyramid_points_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, pyramid_points_buffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(pyramid_points), gl.STATIC_DRAW);
    
    pyramid_normals_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, pyramid_normals_buffer);
    gl.bufferData(gl.ARRAY_BUFFER, flatten(pyramid_normals), gl.STATIC_DRAW);
    
    pyramid_faces_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, pyramid_faces_buffer);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint8Array(pyramid_faces), gl.STATIC_DRAW);
    
    pyramid_edges_buffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, pyramid_edges_buffer);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint8Array(pyramid_edges), gl.STATIC_DRAW);
}

function pyramidDrawWireFrame(gl, program)
{    
    gl.useProgram(program);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, pyramid_points_buffer);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, pyramid_normals_buffer);
    var vNormal = gl.getAttribLocation(program, "vNormal");
    gl.vertexAttribPointer(vNormal, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vNormal);
    
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, pyramid_edges_buffer);
    gl.drawElements(gl.LINES, pyramid_edges.length, gl.UNSIGNED_BYTE, 0);
}

function pyramidDrawFilled(gl, program)
{
    gl.useProgram(program);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, pyramid_points_buffer);
    var vPosition = gl.getAttribLocation(program, "vPosition");
    gl.vertexAttribPointer(vPosition, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vPosition);
    
    gl.bindBuffer(gl.ARRAY_BUFFER, pyramid_normals_buffer);
    var vNormal = gl.getAttribLocation(program, "vNormal");
    gl.vertexAttribPointer(vNormal, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(vNormal);
    
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, pyramid_faces_buffer);
    gl.drawElements(gl.TRIANGLES, pyramid_faces.length, gl.UNSIGNED_BYTE, 0);
}

function pyramidAddBase(a, b, c, d, n)
{
    var offset = pyramid_points.length;
    
    pyramid_points.push(pyramid_vertices[a]);
    pyramid_points.push(pyramid_vertices[b]);
    pyramid_points.push(pyramid_vertices[c]);
    pyramid_points.push(pyramid_vertices[d]);
    for(var i=0; i<4; i++)
        pyramid_normals.push(n);
    
    // Add 2 triangular faces (a,b,c) and (a,c,d)
    pyramid_faces.push(offset);
    pyramid_faces.push(offset+1);
    pyramid_faces.push(offset+2);
    
    pyramid_faces.push(offset);
    pyramid_faces.push(offset+2);
    pyramid_faces.push(offset+3);
    
    // Add first edge (a,b)
    pyramid_edges.push(offset);
    pyramid_edges.push(offset+1);
    
    // Add second edge (b,c)
    pyramid_edges.push(offset+1);
    pyramid_edges.push(offset+2);
}

function pyramidAddFace(a, b, c, n){
    var offset = pyramid_points.length;

    pyramid_points.push(pyramid_vertices[a]);
    pyramid_points.push(pyramid_vertices[b]);
    pyramid_points.push(pyramid_vertices[c]);

    for(var i=0; i<3; i++)
        pyramid_normals.push(n);

    pyramid_faces.push(offset);
    pyramid_faces.push(offset+1);
    pyramid_faces.push(offset+2);

    // Add first edge (a,b)
    pyramid_edges.push(offset);
    pyramid_edges.push(offset+1);
    
    // Add second edge (b,c)
    pyramid_edges.push(offset+1);
    pyramid_edges.push(offset+2);
}