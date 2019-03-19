document.addEventListener("DOMContentLoaded",loadFile);
// onstart
var server = "http://127.0.0.1:8080"
var filename = "/Users/TrofimovDM/JS_projects/backend/rawFromTridb1.txt";
var loadedData;
//



function loadFile(evt) {    
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open("GET",server+filename,true);
    xmlhttp.send();
    xmlhttp.onreadystatechange = function(){
      //console.log(xmlhttp);
      if(xmlhttp.status == 200 && xmlhttp.readyState == 4){              
        loadedData = xmlhttp.responseText.replace("\n"," ").split(" "); 
        //console.log(loadedData);      
      }
      if(xmlhttp.status !== 200 && xmlhttp.readyState == 4){
        alert("Cannnot load of find\n"+filename+"\n");
      }
    };
    }



function loadFile1(evt) {   
    THREE.Cache.enabled = true;
var loader = new THREE.FileLoader();
loader.load(
	// resource URL
	'rawFromTridb.txt',

	// onLoad callback
	function ( data ) {
		// output the text to the console
		document.getElementById("contents").textContent = txt;
	},

	// onProgress callback
	function ( xhr ) {
		document.getElementById("contents").textContent =  (xhr.loaded / xhr.total * 100) + '% loaded';
	},

	// onError callback
	function ( err ) {
		alert( 'An error happened' );
	}
);
}