var el = document.getElementById("btn-download-xhr");
var myInterval = null;
if(remove && el) {
	if(myInterval) clearInterval(myInterval);
    el.remove();
} else if(!remove && !el) {
    var button = document.createElement("input");
    button.onclick = function() {
        document.cookie = 'download-xhr=true; expires=Sun, 1 Jan 2037 00:00:00 UTC; path=/'
    };
    button.type = "button";
    button.id = "btn-download-xhr";
    button.value = "Download Video - " + document.title;
    myInterval = window.setInterval(function() {
		if(button) button.value = "Download Video - " + document.title;
	}, 100);
    button.style = "font-size:16px; background-color:#4287f5; border:none; color:white; padding:15px 32px; top:10px;right:10px; position:absolute; z-index: 9999; cursor:pointer; box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2), 0 6px 20px 0 rgba(0,0,0,0.19);";
    document.body.appendChild(button);
}
