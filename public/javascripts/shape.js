// JSON CONSTs
var shpSides = 3,
	shpNum = 50,
	shpRad = 0.15,
	shpRot = 0,
	shpOffset = 0,
	shpSize = 0.06,
	shpCol = "rgba(255, 255, 255, 0.15)",
	mult = view.size._height,
	angleRev = 0,
	rotRev = 1
;
if(getUrlVars()["rad"] != undefined){
	shpRad = parseFloat(getUrlVars()["rad"]);
}
if(getUrlVars()["num"] != undefined){
	shpNum = parseFloat(getUrlVars()["num"]);
}
if(getUrlVars()["rev"] != undefined){
	angleRev = parseFloat(getUrlVars()["rev"]);
}
if(getUrlVars()["offset"] != undefined){
	shpOffset = parseFloat(getUrlVars()["offset"]);
}
if(getUrlVars()["rot"] != undefined){
	rotRev = parseFloat(getUrlVars()["rot"]);
}
if(getUrlVars()["size"] != undefined){
	shpSize = parseFloat(getUrlVars()["size"]);
}

var shapes = [];
var shapeGroup;
makeShapes();

function makeShape(sides, size, posX, posY, rot){

	var shape;
	var pos = new Point(posX, posY);

	if(sides <= 2){
		shape = new Path.Circle({
			center: pos,
			radius: size
		});
	} else {
		shape = new Path.RegularPolygon(pos, sides, size);
	}
	shape.rotate(rot);

	return shape;

}

function makeShapes() {
	for (var i = 0; i < shpNum; i++) {
		shapes[i] = makeShape(shpSides, shpSize * mult, view.center._x, ((0.5 - shpRad) * mult) - (shpOffset * i), shpRot);
	}

	shapeGroup = new Group(shapes);
	transformShapes();
}

function showShapes() {
	shapes.forEach( function(shp, i) {
		shp.fillColor = shpCol;
		shp.strokeColor = "rgba(255,255,255,0.35)";
	});
}

function transformShapes() {
	var angle = (360 / shpNum) * rotRev;
	shapes.forEach( function(shp, i) {
		shp.rotate(angle * i, view.center);
		shp.rotate(angle * i * angleRev);
	});
	showShapes();
}

// resize the canvas to fill browser window dynamically
window.addEventListener('resize', resizeCanvas, false);

function resizeCanvas() {
	shapeGroup.position = view.center;
	if(view.size._height <= view.size._width){
		shapeGroup.scale(view.size._height / shapeGroup.bounds.height);
	} else {
		shapeGroup.scale(view.size._width / shapeGroup.bounds.width);
	}
	console.log(shapeGroup.position._x * 2, canvas.width);
}

resizeCanvas();


// Read a page's GET URL variables and return them as an associative array.
function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}