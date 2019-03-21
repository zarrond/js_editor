String.prototype.format = function () {
    
                    var str = this;
    
                    for ( var i = 0; i < arguments.length; i ++ ) {
    
                        str = str.replace( '{' + i + '}', arguments[ i ] );
    
                    }
                    return str;
    
                };
                // requirements
                // var WEBGL = require('../js/WebGL');
                // //var window = require('window')
                // var THREE = require('../js/three')
                // var jsdom = require('jsdom');
                // var document = jsdom.JSDOM;

                //

                // Browser check
                THREE.Cache.enabled = true;

                if ( WEBGL.isWebGLAvailable === false ) { 
                      
                    document.body.appendChild( WEBGL.getWebGLErrorMessage() ); 
                     
                } 
                if (window.File && window.FileReader && window.FileList && window.Blob) {
                        // All the File APIs are supported.
                    } else {
                        alert('The File APIs are not fully supported in this browser.');
                    }
                
                    
                var OPTIONS = {
                    GEOMETRY_SIZE: 15,
                    GRID_HELPER: true,
                    GUI: true,
                    FIX_SRC_AXES: true,
                };

                var server = "http://127.0.0.1:8080"
                var filename = "/Users/TrofimovDM/JS_projects/backend/rawFromTridb.txt";
                var loadedData;

                var wireGeometry = new THREE.Geometry();
                var loadedMesh;

                var container, stats;
                var camera, scene, renderer;
                var splineHelperObjects = [];
                var splinePointsLength = 4;
                var positions = [];
                var point = new THREE.Vector3();
                var mouse = new THREE.Vector2();
    
                var geometry = new THREE.BoxBufferGeometry(OPTIONS.GEOMETRY_SIZE, OPTIONS.GEOMETRY_SIZE, OPTIONS.GEOMETRY_SIZE);
                var transformControl;
    
                var ARC_SEGMENTS = 200;
    
                var splines = {};
    
                var params = {
                    uniform: true,
                    tension: 0,
                    centripetal: false,
                    chordal: false,
                    addPoint: addPoint,
                    removePoint: removePoint,
                    exportSpline: exportSpline,
                    wireframe: true,
                    helper: true,
                    _continiousRendering: false,
                };
                var OnStart = true;
                var infoOpened  = false;
                var POSTOpened = false;
                var raycastingEnabled = false;
                var controls;

    
                init();
                animate();
    
                function init() {
                    var btn4 = document.getElementById("btn4");
                    btn4.addEventListener("click", loadFile);
                    //document.addEventListener("DOMContentLoaded",loadFile);
                    function loadFile(evt) {    
                        var xmlhttp = new XMLHttpRequest();
                        
                        xmlhttp.open("GET",server+filename,true);
                        xmlhttp.send();
                        xmlhttp.onreadystatechange = function(){
                          //console.log(xmlhttp);
                          if(xmlhttp.status == 200 && xmlhttp.readyState == 4){              
                            loadedData = xmlhttp.responseText.replace(/\n/g," ").split(" "); 
                            //console.log(loadedData);   
                            btn4.removeEventListener("click", loadFile);                              
                            btn4.addEventListener("click", createGeometry);
                            btn4.classList.toggle("ButtonNotLoaded");
                            document.getElementById("POSTinput0").textContent = filename.split("/").slice(-1)[0];
                          }
                          if(xmlhttp.status !== 200 && xmlhttp.readyState == 4){
                            alert("Cannnot load of find\n"+filename+"\n");
                          }
                        };
                        }


                    function cG(){
                        console.log("got data2");
                        //console.log(wireGeometry);
                        var _Points_count = parseInt(loadedData[0]);
                        var _Triags_count = parseInt(loadedData[1]);
                        
                        var _s = 2; //shift
                        var _Points = loadedData.slice(2,_Points_count*3+2).map(Number);
                        //console.log(loadedData.slice(2,_Points_count*3+2));
                        
                        var wireGeometry1 = new THREE.BufferGeometry();
                        // create a simple square shape. We duplicate the top left and bottom right
                        // vertices because each vertex needs to appear once per triangle.
                        var vertices = new Float32Array( _Points );
                        console.log(vertices);
                        // itemSize = 3 because there are 3 values (components) per vertex
                        wireGeometry1.addAttribute( 'position', new THREE.BufferAttribute( vertices, 3 ) );
                        //geometry.scale(10,10,10);
                        
                        var material = new THREE.MeshBasicMaterial( { color: 0xff0000 } );
                        var mesh1 = new THREE.Mesh( wireGeometry1, material );
                        scene.add(mesh1);
                    }




                    function createGeometry()
                    {
                        // statusManager("started loading model");
                        // sleep(1000);
                        // render();
                        console.log("got data");
                        console.log(wireGeometry);
                        var _Points_count = parseInt(loadedData[0]);
                        var _Triags_count = parseInt(loadedData[1]);
                        //var Points = [];
                        var _s = 2; //shift
                        for( var i = 0; i < _Points_count; i++)
                        {
                            if(OPTIONS.FIX_SRC_AXES)   //TODO Fix orientation of object
                                var _Point = new THREE.Vector3(parseFloat(loadedData[3*i+_s]), parseFloat(loadedData[3*i+_s+2]), parseFloat(loadedData[3*i+_s+1]))
                            else
                                var _Point = new THREE.Vector3(parseFloat(loadedData[3*i+_s]), parseFloat(loadedData[3*i+_s+1]), parseFloat(loadedData[3*i+_s+2]))
                            //Points.push(_Point); 178.449116
                            wireGeometry.vertices.push(_Point);
                        }
                        document.getElementById("info3").textContent = loadedData[3*_Points_count+2] +" ve " + loadedData.length + "\n" + (_Points_count*3+3*_Triags_count+2);
                        _s = _Points_count*3 + 2;
                        for( var i = 0; i < _Triags_count; i++)
                        {
                            var f = new THREE.Face3(parseInt(loadedData[3*i+_s]), parseInt(loadedData[3*i+_s+1]), parseInt(loadedData[3*i+_s+2]) )
                            wireGeometry.faces.push(f);
                        }
                        //var loader = new THREE.FileLoader();
                        wireGeometry.computeBoundingSphere();
                        wireGeometry.computeFaceNormals();
                        //var material = new THREE.MeshBasicMaterial( { color: 0xff0000 } );
                        //material.wireframe = true; 
                        //material.transparent = true;
                        //wireGeometry.scale(0.01,0.01,0.01);

                        var meshMaterial = new THREE.MeshLambertMaterial( {
                            color: 0xff0000,
                            opacity: 0.5,
                            transparent: false,
                            side: THREE.DoubleSide,
                        } );
        

                        
                        loadedMesh = new THREE.Mesh( wireGeometry, meshMaterial ); 
                        //loadedMesh.scale(0.1,0.1,0.1);
                        
                        //render;
                        var center = wireGeometry.boundingSphere.center;//.multiply(-1);
                        //var d = wireGeometry.boundingSphere.center.distanceTo(new THREE.Vector3(0,0,0));
                        //wireGeometry.translate(center.normalize.x/d, center.normalize.y/d, center.normalize.z/d );
                        //console.log(wireGeometry);
                        

                        // var m = new THREE.Matrix4();
                        // m.makeTranslation(center.normalize().x*d, center.normalize().y*d, center.normalize().z*d );
                        // console.log(m);
                        // console.log(   center.normalize());
                        // console.log(d)

                        loadedMesh.castShadow = true;
                        loadedMesh.receiveShadow = true;
                        loadedMesh.name = "loadedMesh";
                        scene.add( loadedMesh);

                        var helper = new THREE.FaceNormalsHelper( loadedMesh, 4, 0x00ff00, 0.1 );
                        helper.name = "helper";
                       
                        scene.add( helper );
                        //wireGeometry.position = new THREE.Vector3(0,0,0);
                        //camera.lookAt(wireGeometry.boundingSphere.center);
                        //camera.position.set( center.z,center.y, center.x );
                        controls.target = center;
                        render();
                        statusManager("model loaded succesfully");
                        console.log("got data");

                    }

                    var menu = document.getElementById("menu");
                    
                    var btn1 = document.getElementById('btn1');
             
                    btn1.addEventListener("click",function(){
                        infoOpened = !infoOpened;
                        console.log(infoOpened);
                        if(POSTOpened&&infoOpened){
                            POSTmenu.classList.toggle("menuMoveRight");
                            POSTOpened = false;
                        }    
                        menu.classList.toggle("menuMoveRight");
                    }); 

                    

                    var btn2 = document.getElementById('btn2');
                    
                    btn2.addEventListener("click",function(){
                        
                        document.getElementById("info1").textContent = document.getElementById("info1").textContent+"1";
                        console.log(scene);
                        console.log(scene.getObjectByName("loadedMesh"));
                        console.log(scene.getObjectByName("helper",true));
                    });
                    


                    var btn3 = document.getElementById('btn3');
                    
                    btn3.addEventListener("click",function(){
                        btn3.classList.toggle("lockedButton");
                        raycastingEnabled = !raycastingEnabled;
                    });
                   
                    


                    var slider = document.getElementById("range");
                    if(slider)
                        slider.addEventListener("change", function(){
                            geometry.scale(2,2,2);
                            console.log(slider.value);
                            updateSplineOutline();  
                        })


                    var btn5 = document.getElementById('btn5');
                    var POSTmenu = document.getElementById("POSTmenu");
                    btn5.addEventListener("click",function(){
                        POSTOpened = !POSTOpened;
                        console.log(POSTOpened);
                        if(POSTOpened&&infoOpened){
                            menu.classList.toggle("menuMoveRight");
                            infoOpened = false;
                        }
                            
                        POSTmenu.classList.toggle("menuMoveRight");
                    });
                    POSTbtn = document.getElementById("POSTbtn");
                    POSTbtn.addEventListener("click",POST);


                    container = document.getElementById( 'container' );
                    
                    scene = new THREE.Scene();
                    scene.background = new THREE.Color( 0xf0f0f0 );
    
                    camera = new THREE.PerspectiveCamera( 70, window.innerWidth / window.innerHeight, 1, 10000 );
                    camera.position.set( 0, 250, 1000 );
                    scene.add( camera );
    
                    scene.add( new THREE.AmbientLight( 0xf0f0f0 ) );
                    var light = new THREE.SpotLight( 0xffffff, 1.5 );
                    light.position.set( 0, 1500, 200 );
                    light.castShadow = false;
                    light.shadow = new THREE.LightShadow( new THREE.PerspectiveCamera( 70, 1, 200, 2000 ) );
                    light.shadow.bias = - 0.000222;
                    light.shadow.mapSize.width = 1024;
                    light.shadow.mapSize.height = 1024;
                    scene.add( light );
    
                    var planeGeometry = new THREE.PlaneBufferGeometry( 2000, 2000 );
                    planeGeometry.rotateX( - Math.PI / 2 );
                    var planeMaterial = new THREE.ShadowMaterial( { opacity: 0.2 } );
    
                    var plane = new THREE.Mesh( planeGeometry, planeMaterial );
                    plane.position.y = - 200;
                    plane.receiveShadow = false;
                    scene.add( plane );
                    

                    if(OPTIONS.GRID_HELPER){
                        var helper = new THREE.GridHelper( 2000, 100 );
                        helper.position.y = - 199;
                        helper.material.opacity = 0.25;
                        helper.material.transparent = true;
                        scene.add( helper );
                    }
                    
    
                    var axes = new THREE.AxesHelper( 1000 );
                    axes.position.set( 0,0,0 );
                    scene.add( axes );
    
                    renderer = new THREE.WebGLRenderer( { antialias: true } );
                    renderer.setPixelRatio( window.devicePixelRatio );
                    renderer.setSize( window.innerWidth, window.innerHeight );
                    renderer.shadowMap.enabled = true;
                    container.appendChild( renderer.domElement );
    
                    
                    if (OPTIONS.GUI){
                        var gui = new dat.GUI();   
                        gui.domElement.addEventListener("change",render); // IMPORTANT!
                        gui.add( params, 'uniform' );
                        gui.add( params, 'tension', 0, 1 ).step( 0.01 ).onChange( function ( value ) {
                            render();
                            splines.uniform.tension = value;
                            updateSplineOutline();
        
                        } );
                        
                        gui.add( params, 'addPoint' );
                        gui.add( params, 'removePoint' );
                        gui.add( params, 'exportSpline' );
                        gui.add( params, 'wireframe' );
                        gui.add( params, 'helper' );
                        gui.add( params, '_continiousRendering' );
                        gui.open();
                    }


        
        
                    // Controls 
                    controls = new THREE.OrbitControls( camera, renderer.domElement );
                    controls.damping = 0.2;
                    controls.enableKeys = false;
                    controls.addEventListener( 'mousemove', function(event){
                        if(infoOpened){
                            document.getElementById("info1").textContent = event.object.id+" "+event.object.position.x+" "+event.object.position.y+" "+event.object.position.z;
                        }
                    } ); 
                    controls.addEventListener( 'change', render );
    
                    controls.addEventListener( 'start', function () {
    
                        cancelHideTransform();
    
                    } );
    
                    controls.addEventListener( 'end', function () {
    
                        delayHideTransform();
    
                    } );
    
                    transformControl = new THREE.TransformControls( camera, renderer.domElement );
                    transformControl.addEventListener( 'change', render );
                    
                    transformControl.addEventListener( 'dragging-changed', function ( event ) {
    
                        controls.enabled = ! event.value;
                        
        } );
                    scene.add( transformControl );
    
                    // Hiding transform situation is a little in a mess :()
                    transformControl.addEventListener( 'change', function () {
                        
                        cancelHideTransform();
                        
    
                    } );
    
                    transformControl.addEventListener( 'mouseDown', function (event) {
                        
                        cancelHideTransform();
    
                    } );
    
                    transformControl.addEventListener( 'mouseUp', function () {
    
                        delayHideTransform();
    
                    } );
    
                    transformControl.addEventListener( 'objectChange', function () {
    
                        updateSplineOutline();                       
    
                    } );

                    var dragcontrols = new THREE.DragControls( splineHelperObjects, camera, renderer.domElement ); //
                    dragcontrols.enabled = false;
                    dragcontrols.addEventListener( 'hoveron', function ( event ) {
                       //console.log(document.getElementById(event.object.id));
                       console.log(event.object);
                       //console.log(scene.children);
                       
                        
                        tippy("#"+event.object.uuid,
                        {
                          content: "content",
                          arrow: "sharp",
                          placement: "top"
                  
                        });

                        if(infoOpened){
                            document.getElementById("info1").textContent = event.object.id+" "+event.object.position.x+" "+event.object.position.y+" "+event.object.position.z;
                        }

                        transformControl.attach( event.object );
                        cancelHideTransform();
    
                    } );
    
                    dragcontrols.addEventListener( 'hoveroff', function () {
    
                        delayHideTransform();
    
                    } );

                    

                    var hiding;
    
                    function delayHideTransform() {
    
                        cancelHideTransform();
                        hideTransform();
    
                    }
    
                    function hideTransform() {
    
                        hiding = setTimeout( function () {
    
                            transformControl.detach( transformControl.object );
    
                        }, 250 );
    
                    }
    
                    function cancelHideTransform() {
    
                        if ( hiding ) clearTimeout( hiding );
    
                    }
    
                    /*******
                     * Curves
                     *********/
                    
                    for ( var i = 0; i < splinePointsLength; i ++ ) {
    
                        addSplineObject( positions[ i ] );
    
                    }
    
                    positions = [];
    
                    for ( var i = 0; i < splinePointsLength; i ++ ) {
    
                        positions.push( splineHelperObjects[ i ].position );
    
                    }
    
                    var geometry = new THREE.BufferGeometry();
                    geometry.addAttribute( 'position', new THREE.BufferAttribute( new Float32Array( ARC_SEGMENTS * 3 ), 3 ) );
    
                    var curve = new THREE.CatmullRomCurve3( positions );
                    curve.curveType = 'catmullrom';
                    curve.mesh = new THREE.Line( geometry.clone(), new THREE.LineBasicMaterial( {
                        color: 0xff0000,
                        opacity: 0.35
                    } ) );
                    curve.mesh.castShadow = true;
                    splines.uniform = curve;
    
                    curve = new THREE.CatmullRomCurve3( positions );
                    curve.curveType = 'centripetal';
                    curve.mesh = new THREE.Line( geometry.clone(), new THREE.LineBasicMaterial( {
                        color: 0x00ff00,
                        opacity: 0.35
                    } ) );
                    curve.mesh.castShadow = true;
                    splines.centripetal = curve;
    
                    curve = new THREE.CatmullRomCurve3( positions );
                    curve.curveType = 'chordal';
                    curve.mesh = new THREE.Line( geometry.clone(), new THREE.LineBasicMaterial( {
                        color: 0x0000ff,
                        opacity: 0.35
                    } ) );
                    curve.mesh.castShadow = true;
                    splines.chordal = curve;
                    // splines.addEventListener("hoveron", function(event){
                    //     document.getElementById("info1").textContent = "cown";            
                    // })
                    for ( var k in splines ) {
    
                        var spline = splines[ k ];
                        scene.add( spline.mesh );
    
                    }
    
                    load( [ new THREE.Vector3( 289.76843686945404, 452.51481137238443, 56.10018915737797 ),
                        new THREE.Vector3( - 53.56300074753207, 171.49711742836848, - 14.495472686253045 ),
                        new THREE.Vector3( - 91.40118730204415, 176.4306956436485, - 6.958271935582161 ),
                        new THREE.Vector3( - 383.785318791128, 491.1365363371675, 47.869296953772746 ) ] );


                    document.addEventListener( 'mousemove', onDocumentMouseMove, false );
                    // document.addEventListener('mousewheel', function() {
                    //     console.log("zoom  "+camera.zoom);
                    //     document.getElementById("info").textContent = camera.zoom;
                    // });
    
                }

                
                function POST()
                {
                    var cmd = "C:\\Users\\TrofimovDM\\JS_projects\\backend\\java\\testJS.jar";

                    
                      var data = {};
                      data['cmd'] = cmd;
                      data['dacc'] = document.getElementById("POSTinput1").value;
                      data['vox'] = document.getElementById("POSTinput2").value;
                      data['file'] = document.getElementById("POSTinput0").textContent;
                      console.log(document.getElementById("POSTinput0"));
                    //   for (var i = 0, ii = form.length; i < ii; ++i) {
                    //     var input = form[i];
                    //     if (input.name) {
                    //       data[input.name] = input.value;
                    //     }
                    //   }
                    
                      // construct an HTTP request
                      var xhr = new XMLHttpRequest();
                      xhr.open('POST', server, true);
                      xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
                    
                      // send the collected data as JSON
                      xhr.send(JSON.stringify(data));
                      xhr.onreadystatechange = function() { // Call a function when the state changes.
                        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
                            // Request finished. Do processing here.
                            console.log(this)
                        }
                    }
                    
                      xhr.onloadend = function () {
                        // done
                      };
                   


                }


                function sleep( sleepDuration ){
                    var now = new Date().getTime();
                    while(new Date().getTime() < now + sleepDuration){ /* do nothing */ } 
                }


                function statusManager(value){

                    var TIMEOUT = 5000;

                    document.getElementById("statusLine").textContent = value;
                    var t = setTimeout(function(){ 
                        document.getElementById("statusLine").textContent = ""; 
                    }, TIMEOUT);
                    document.getElementById("statusLine").addEventListener("mouseover", function(){
                        console.log("mouseover");
                        clearTimeout(t);
                    });
                    document.getElementById("statusLine").addEventListener("mouseleave", function(){
                        console.log("mouseleave");
                        t = setTimeout(function(){ 
                            document.getElementById("statusLine").textContent = ""; 
                        }, TIMEOUT);
                    });
                }


    
                function addSplineObject( position ) {
    
                    var material = new THREE.MeshLambertMaterial( { color: Math.random() * 0xffffff } );
                    var object = new THREE.Mesh( geometry, material );
    
                    if ( position ) {
    
                        object.position.copy( position );
    
                    } else {
    
                        object.position.x = Math.random() * 1000 - 500;
                        object.position.y = Math.random() * 600;
                        object.position.z = Math.random() * 800 - 400;
    
                    }
    
                    object.castShadow = true;
                    object.receiveShadow = true;
                    scene.add( object );
                    splineHelperObjects.push( object );
                    return object;
    
                }
    
                function addPoint() {
    
                    splinePointsLength ++;
    
                    positions.push( addSplineObject().position );
    
                    updateSplineOutline();
    
                }
    
                function removePoint() {
    
                    if ( splinePointsLength <= 4 ) {
    
                        return;
    
                    }
                    splinePointsLength --;
                    positions.pop();
                    scene.remove( splineHelperObjects.pop() );
    
                    updateSplineOutline();
    
                }
    
                function updateSplineOutline() {
    
                    for ( var k in splines ) {
    
                        var spline = splines[ k ];
    
                        var splineMesh = spline.mesh;
                        var position = splineMesh.geometry.attributes.position;
    
                        for ( var i = 0; i < ARC_SEGMENTS; i ++ ) {
    
                            var t = i / ( ARC_SEGMENTS - 1 );
                            spline.getPoint( t, point );
                            position.setXYZ( i, point.x, point.y, point.z );
    
                        }
    
                        position.needsUpdate = true;
    
                    }
    
                }
    
                function exportSpline() {
    
                    var strplace = [];
    
                    for ( var i = 0; i < splinePointsLength; i ++ ) {
    
                        var p = splineHelperObjects[ i ].position;
                        strplace.push( 'new THREE.Vector3({0}, {1}, {2})'.format( p.x, p.y, p.z ) );
    
                    }
    
                    console.log( strplace.join( ',\n' ) );
                    var code = '[' + ( strplace.join( ',\n\t' ) ) + ']';
                    prompt( 'copy and paste code', code );
    
                }
    
                function load( new_positions ) {
    
                    while ( new_positions.length > positions.length ) {
    
                        addPoint();
    
                    }
    
                    while ( new_positions.length < positions.length ) {
    
                        removePoint();
    
                    }
    
                    for ( var i = 0; i < positions.length; i ++ ) {
    
                        positions[ i ].copy( new_positions[ i ] );
    
                    }
    
                    updateSplineOutline();
    
                }
    
                function onDocumentMouseMove( event ) {
                    
                    event.preventDefault();
    
                    // mouse.x = ( event.clientX / window.innerWidth ) * 2 - 1;
                    // mouse.y = - ( event.clientY / window.innerHeight ) * 2 + 1;

                    mouse.x = event.clientX;
                    mouse.y = event.clientY;


                    document.getElementById("info2").textContent = "x: "+mouse.x+" y:"+mouse.y;
                    document.getElementById("info4").textContent = camera.zoom;
                }
                    
                    


                function animate() {
    
                    requestAnimationFrame( animate );

                    if(params._continiousRendering)
                        render();
                    if(OnStart)
                    {
                        render();
                        OnStart = false;
                    }
                    
    
                }
    
                function render() {
                    
                    
                    if(raycastingEnabled){
                        //for(child in scene.children){
                            var raycaster = new THREE.Raycaster(); 
                            raycaster.linePrecision = 3;
                            raycaster.setFromCamera( mouse, camera );
                            var intersects = raycaster.intersectObjects(scene.children, true ); 
                            if ( intersects.length > 0 ) {
                                
                                console.log( intersects[ 0 ] );
            
                            } else {
                                
                            }
                        //}
                    }     
                    splines.uniform.mesh.visible = params.uniform;
                    splines.centripetal.mesh.visible = params.centripetal;
                    splines.chordal.mesh.visible = params.chordal;
                    if(loadedMesh){
                        loadedMesh.material.wireframe = params.wireframe;
                        scene.getObjectByName("helper",true).visible = params.helper;
                    }
                    renderer.render( scene, camera );
                    
    
                }


                