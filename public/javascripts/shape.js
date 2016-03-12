var shpId = -1;

// JSON CONSTs
var shpSides = 3,
    	// number of shapes
	shpNum = 50,
	// radius from the centre
	shpRad = 0.15,
	// shapes initial rotation
	shpRot = 0,
	// how much the shape moves out
	shpOffset = 0,
	// size of the shape
	shpSize = 0.06,
	// shape colour
	shpColR = 1,
	shpColG = 1,
	shpColB = 1,
	shpColA = 0.15,
	// turns percentage values to pixels
	mult = view.size._height,
	// how many times the shape rotates in a single outer rotation
	angleRev = 0,
	// number of outer rotations
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

// creates a shape
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

// creates all the shapes in the image
function makeShapes() {
	for (var i = 0; i < shpNum; i++) {
		shapes[i] = makeShape(shpSides, shpSize * mult, view.center._x, ((0.5 - shpRad) * mult) - (shpOffset * i), shpRot);
	}

	shapeGroup = new Group(shapes);
	transformShapes();
    console.log(shapes);
}

// displays the shapes
function showShapes() {
	shapes.forEach( function(shp, i) {
		shp.fillColor = "rgba(" + shpColR + "," + shpColG + "," + shpColB + "," + shpColA + ")";
		shp.strokeColor = "rgba(" + shpColR + "," + shpColG + "," + shpColB + "," + (shpColA * 1.25) + ")";
	});
}

// transforms all of the shapes
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
	paper.view.draw();
}



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

$.ajax({
      dataType: "json",
      url: "/next",
})
.done(function(toMuchData) {
    console.log("WOOOO");
   // load variables from JSON
    shpId = toMuchData.id;
    data = toMuchData.fields;
    console.log(data);
    shpSides = Math.round(data.shpSides);
    shpNum = Math.round(data.shpNum);
    console.log(shpNum);
    shpRad = data.shpRad;
    shpRot = data.shpRot;
    shpOffset = data.shpOffset;
    shpSize = data.shpSize;
    shpColR = data.shpColR * 255;
    shpColG = data.shpColG * 255;
    shpColB = data.shpColB * 255;
    shpColA = data.shpColA;
    angleRev = data.angleRev;
    rotRev = data.rotRev;
    // do the shape things
    makeShapes();
    resizeCanvas(); 

})
.fail(function( data ) {
    console.log("HELP!");
});

function submitRating(value){
	console.log(value);
	$.ajax({
		url: '/rating',
		type: 'POST',
		data: {
			id: shpId,
			rating: value,
		},
	})
	.done(function() {
	    document.location.reload(false);	
	})
	.fail(function() {
		console.log("error");
	});
}

$(".row input").on('click', function() {
	submitRating($(this).val());
});
document.addEventListener('keydown', function(event) {
	var keycode = event.keyCode;
	if(keycode >= 49 && keycode <= 53) {
		submitRating(keycode - 48);
	}
});