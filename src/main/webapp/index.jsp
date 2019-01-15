<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta property="hostAddress" url="${hostAddress}"/>
<meta property="loginURL" url="${loginURL}"/>
<meta property="tokenGiver" url="${tokenGiver}"/>
<meta property="tokenValidator" url="${tokenValidator}"/>
<meta property="indexURL" url="${indexURL}"/>

<script src="go-debug.js"></script>
<script src="jquery.min.js"></script>
<script src="comm.js"></script>
<style>
    html, body {
        position: relative;
        overflow: hidden;
        width: 100%;
        height: 100%;
    }
    #myDiagramDiv {
        display: inline-block;
        background-color: white;
        width: 100%;
        height: 100%;
    }
</style>
</head>

<body>
<div id="myDiagramDiv"></div>
</body>

<script type="text/javascript">

var MAKE = go.GraphObject.make; 
var lightGradient = MAKE(go.Brush, "Linear", {1: "#E6E6FA", 0: "#FFFAF0"});
var isSaving = false;
var myDiagram;
var comm;

/*
    When a user edits the relationship between two classes (by relationship, it is meant 1-1, 1-N, ...)
    this function checks if the text entered by the user is a valid text (it can be a digit or N or n)
    If not, it prevents the user from entering such data.
*/
function isRelation(textblock, oldString, newString) {
	var numberInNewString = ~~Number(newString);
	var testPattern = /([0-9nN])/i;
	return (String(numberInNewString) === newString && numberInNewString >= 0) || testPattern.test(newString);
}

/*
    This is used to determine if user is dragging a class box to another class box
*/
function isDraggingToAnotherClassBox(node1, node2) {

    //First of all, the dragged element must be a Node
	if (!(node1 instanceof go.Node))
	    return false;

	//One cannot drag an element to itself
	if (node1 === node2)
	    return false;

	return true;
}

function loadDiagram() {
	comm.makeRequest({
		"url" : "api/diagram/load",
		"type" : RequestMethod.GET,
		"success" : function(response) {
			if (response.length == 0) {
				var nodeDataArray = [];
				var linkDataArray = [];
				myDiagram.model = new go.GraphLinksModel(nodeDataArray, linkDataArray);
			}
			else
				myDiagram.model = go.Model.fromJson(response);
		},
		"error" : function (response) {
			var nodeDataArray = [];
			var linkDataArray = [];
			myDiagram.model = new go.GraphLinksModel(nodeDataArray, linkDataArray);

            if (response.status && response.statusText)
            	alert("Error: status code " + response.status + " " + response.statusText + " " + response.responseText);
            else
            	alert(response);
		}
	});
}

function saveDiagram() {
	if (isSaving)
	    return;

	isSaving = true;
	comm.makeRequest({
		"url" : "api/diagram/save",
		"type" : RequestMethod.POST,
		"data" : myDiagram.model.toJson(),
		"success" : function (response) {
			alert("Diagram is saved.");
			myDiagram.isModified = false;
			isSaving = false;
		},
		"error": function (response) {
			isSaving = false;

			if (response.status && response.statusText)
                alert("Error: status code " + response.status + " " + response.statusText + " " + response.responseText);
            else
                alert(response);
		}
	});
}

// Function to add a new property to a class
function nodeDoubleClick(e, obj) {
	var clicked = obj.part;
	if (clicked !== null) {
		var data = clicked.data;
		myDiagram.startTransaction("Add a new attribute");
		myDiagram.model.insertArrayItem(data.items, data.items.length, {name: "new", type: "int", wval: (data.hide) ? 0 : 14});
		myDiagram.commitTransaction("Add a new attribute");
	}
}


function findIndex(id, items) {
	for (var i = 0; i < items.length; i++)
		if (items[i].name.localeCompare(id) == 0)
			return i;
	return -1;
}

function deleteAttribute(e, obj) {
	var clicked = obj.part;
	if (clicked !== null) {
		var data = clicked.data;
		myDiagram.startTransaction("Remove an attribute");
		var index = findIndex(obj.id, data.items);
		myDiagram.model.removeArrayItem(data.items, index);
		myDiagram.commitTransaction("Remove an attribute");
	}
}

// Function to move a property in a class box up
function upAttribute(e, obj) {
	var clicked = obj.part;
	if (clicked !== null) {
		var data = clicked.data;
		var index = findIndex(obj.id, data.items);

		if (index == 0) 
			return;

		myDiagram.startTransaction("Up attribute");

		var element = data.items[index];

		myDiagram.model.removeArrayItem(data.items, index);
		myDiagram.model.insertArrayItem(data.items, index - 1, element);

		myDiagram.commitTransaction("Up attribute");
	}
}

// Function to move a property in a class box down
function downAttribute(e, obj) {
	var clicked = obj.part;
	if (clicked !== null) {
		var data = clicked.data;
		var index = findIndex(obj.id, data.items);

		if (index == data.items.length - 1) 
			return;

		myDiagram.startTransaction("Down attribute");

		var element = data.items[index];

		myDiagram.model.removeArrayItem(data.items, index);
		myDiagram.model.insertArrayItem(data.items, index + 1, element);

		myDiagram.commitTransaction("Down attribute");
	}
}

function hideButtons(e, obj) {
	var clicked = obj.part;
	if (clicked !== null) {
		var data = clicked.data;
		myDiagram.startTransaction("Hide buttons");

		data.hide = !data.hide;
		var current = (data.hide) ? 0 : 14;
		for (var i = 0; i < data.items.length; i++) {
			var old = data.items[i].wval;
			data.items[i].wval = current;
			myDiagram.model.raiseDataChanged(data.items[i], "wval", old, current); 
		}

		myDiagram.commitTransaction("Hide buttons");
	}
}

myDiagram = MAKE(go.Diagram, "myDiagramDiv",
{
	initialContentAlignment: go.Spot.Center,
	allowDelete: true,
	allowCopy: true,
	layout: MAKE(go.ForceDirectedLayout),
	"undoManager.isEnabled": true
});

// Event listener for double click on canvas: In this case we create a new box for a new class.
myDiagram.addDiagramListener("BackgroundDoubleClicked", function(e) {
	var point = e.diagram.lastInput.documentPoint
	myDiagram.startTransaction("Add a new node");
	myDiagram.model.addNodeData({name: "new entity", items: [], hide: false, location: new go.Point(point.x, point.y)});
	myDiagram.commitTransaction("Add a new node");
});

// Bind Shift + 1 to save event
myDiagram.commandHandler.doKeyDown = function() {
	var e = myDiagram.lastInput;
	var key = e.key;					//the letter that the user presses
	var shift = e.shift

	//If user presses Shift+1 and the diagram is modified, take necessary actions...
	if (shift && (key === '1') && myDiagram.isModified)
		saveDiagram();

	//...Otherwise, CommandHandler should process the event
	go.CommandHandler.prototype.doKeyDown.call(this);
};

//Margin(top, right, bottom, left)

// The template for a property
var itemTemplate = MAKE(go.Panel, "Horizontal",
	{margin: new go.Margin(0, 0, 5, 0)},

	MAKE(go.Picture,
		{source: "delete.png", height:14, margin: new go.Margin(0, 5, 0, 0), click: deleteAttribute},
		new go.Binding("id", "name"),
		new go.Binding("width", "wval").makeTwoWay()
	),

	MAKE(go.Picture,
		{source: "up.png", height:14, margin: new go.Margin(0, 5, 0, 0), click: upAttribute},
		new go.Binding("id", "name"),
		new go.Binding("width", "wval").makeTwoWay()
	),

	MAKE(go.Picture,
		{source: "down.png", height:14, margin: new go.Margin(0, 5, 0, 0), click: downAttribute},
		new go.Binding("id", "name"),
		new go.Binding("width", "wval").makeTwoWay()
	),

	MAKE(go.TextBlock,
		{stroke: "#333333",	font: "bold 14px sans-serif", width: 150, editable: true},
		new go.Binding("text", "name").makeTwoWay()
	),

	MAKE(go.TextBlock,
		{stroke: "#333333", font: "bold 14px sans-serif", editable: true},
		new go.Binding("text", "type").makeTwoWay()
	)
);

// Define the Node template, representing an entity (a class)
myDiagram.nodeTemplate = MAKE(go.Node, "Auto",  
	{
		selectionAdorned: true,
		resizable: true,
		layoutConditions: go.Part.LayoutStandard & ~go.Part.LayoutNodeSized,
		fromSpot: go.Spot.AllSides,
		toSpot: go.Spot.AllSides,
		isShadowed: true,
		shadowColor: "#C5C1AA",
		doubleClick: nodeDoubleClick
	},

	{
		mouseDragEnter: function (e, node, prev) {
			var diagram = node.diagram;
			var selnode = diagram.selection.first();

			if (!isDraggingToAnotherClassBox(selnode, node))
				return;

			var shape = node.findObject("SHAPE");
			if (shape) {
				shape._prevFill = shape.fill;
				shape.fill = "darkred";
			}
		},

		mouseDragLeave: function (e, node, next) {
			var shape = node.findObject("SHAPE");
			if (shape && shape._prevFill)
				shape.fill = shape._prevFill;
		},

		mouseDrop: function (e, node) {
			var diagram = node.diagram;
			var selnode = diagram.selection.first();
			if (isDraggingToAnotherClassBox(selnode, node))
				myDiagram.model.addLinkData({from: node.data.key, to: selnode.data.key, fromText: "1", toText: "1"});

		}
	},

	new go.Binding("location", "location").makeTwoWay(),

	// define the node's outer shape, which will surround the Table
	MAKE(go.Shape, "Rectangle",
		{name: "SHAPE", fill: lightGradient, stroke: "#756875", strokeWidth: 3}
	),

	MAKE(go.Panel, "Table",
		{margin: new go.Margin(8, 8, 20, 8), stretch: go.GraphObject.Fill},

		MAKE(go.RowColumnDefinition, {row: 0, sizing: go.RowColumnDefinition.None}),
		
		MAKE(go.TextBlock,
			{row: 0, alignment: go.Spot.Center, margin: new go.Margin(0, 15, 10, 15), 
			 font: "bold 16px sans-serif", editable: true},
			new go.Binding("text", "name").makeTwoWay()
		),

		// the collapse/expand button
		MAKE("PanelExpanderButton", "LIST",
			{row: 0, alignment: go.Spot.TopRight}
		),
		
		MAKE("Shape", "Rectangle", 
			{row:0, width:10, height:10, alignment: go.Spot.TopLeft, click: hideButtons}
		),

		MAKE(go.Panel, "Vertical",
			{name: "LIST", row: 1, padding: 3, alignment: go.Spot.TopLeft, defaultAlignment: go.Spot.Left,
			 stretch: go.GraphObject.Horizontal, itemTemplate: itemTemplate},
			new go.Binding("itemArray", "items")
		)
	)
);

// The template for the line among two connected class
myDiagram.linkTemplate = MAKE(go.Link,
    {
        selectionAdorned: true,
        layerName: "Foreground",
        reshapable: true,
        routing: go.Link.AvoidsNodes,
        corner: 5,
        curve: go.Link.JumpOver
    },

	MAKE(go.Shape,
		{stroke: "#303B45", strokeWidth: 2.5}
	),

	MAKE(go.TextBlock, 
		{editable: true, textAlign: "center", font: "bold 14px sans-serif", stroke: "#1967B3",
		 segmentIndex: 0, segmentOffset: new go.Point(NaN, NaN), segmentOrientation: go.Link.OrientUpright,
		 textValidation: isRelation},
		new go.Binding("text", "fromText").makeTwoWay()
	),

	MAKE(go.TextBlock,
		{editable: true, textAlign: "center", font: "bold 14px sans-serif", stroke: "#1967B3",
		 segmentIndex: -1, segmentOffset: new go.Point(NaN, NaN), segmentOrientation: go.Link.OrientUpright,
		 textValidation: isRelation},
		new go.Binding("text", "toText").makeTwoWay()
	)
);

// If authentication fails, redirect user to login page
var authErrorCallback = function(error) {
	alert("Authentication error!");
	comm.redirectToLogin();
}

// If authenticated successfully, fetch the saved diagram
var authSuccessCallback = function() {
	loadDiagram();
}

// Create a Communicator object. This is responsible of handling all communication among UI and the server.
$(document).ready(function() {
	comm = new Communicator({
		"loginMethod"     : LoginMethod.Redirect,
		"authErrorCallback"   : authErrorCallback,
		"authSuccessCallback" : authSuccessCallback
	});
});

</script>

</html>