var server = "http://127.0.0.1:8080"
var btn = document.getElementById("btn1");
var btn2 = document.getElementById("btn2");
btn.addEventListener("click", readSingleFile);
btn2.addEventListener("click", post);

    function post(evt){

        console.log("OPST");


        var xhr = new XMLHttpRequest();
        xhr.open("POST", server, true);

        xhr.onreadystatechange = function() { // Call a function when the state changes.
            if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
                // Request finished. Do processing here.
                console.log(this)
            }
        }

        xhr.send("foo=bar&lorem=ipsum");
    }

  function readSingleFile(evt) {
    btn.classList.toggle("pressed");
    //Retrieve the first (and only!) File from the FileList object
    var filename = document.getElementById("filefield").value;
    console.log(filename);
    var txt = '';
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function(){
      console.log(xmlhttp);
      if(xmlhttp.status == 200 && xmlhttp.readyState == 4){
        if(filename.indexOf(".png") !== -1)
        {
          txt = xmlhttp.responseText;
          document.getElementById("myimg").src = txt;
        }
        else 
        {
          txt = xmlhttp.responseText;
          
          document.getElementById("contents").textContent = txt;
        }
      }
      else{
        document.getElementById("contents").textContent = xmlhttp.status+"\nFile not found";
      }
      btn.classList.toggle("pressed");
    };
    xmlhttp.open("GET",server+filename,true);
    xmlhttp.send();
    

    }

