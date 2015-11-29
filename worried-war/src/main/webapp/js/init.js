/** responsible for getting a channel token (id) and the initial
 * set of incidents to plot */
function init() {
    $(".tweet-stream-sidebar").resizable({ handles: "e" });

	gapi.client.load('sibw', 'v2', function() {
		// SET UP THE MAP
		var mapOptions = {
			center : new google.maps.LatLng(41.2430972, -96.0089942),
			zoom : 13,
			panControl: false,
			zoomControlOptions:{ position: google.maps.ControlPosition.TOP_RIGHT }
		};
		map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
		// INITIALIZE THE CHANNEL
		gapi.client.sibw.init().execute(function(resp) {
			console.log(resp);
			// PLOT THE INITIAL SET OF INCIDENTS (if any)
			if(resp.incidents){
				resp.incidents.forEach(function(incident){
					addMarker(incident);
				});
			}

			// CALLBACKS FOR CHANNEL
			var channel = new goog.appengine.Channel(resp.channel);
			console.debug(channel);

			channel.open({
				onclose: function(){
					console.debug("onclose");
				},
				onopen: function(){
					console.debug("onopen");
				},
				onmessage : function(msg){
					console.log(msg);
					var incident = JSON.parse(msg.data);
					addMarker(incident, true);
				},
				onerror : function (err){
					console.log(err);
				}
			});

		});


		addUserMarker(map);

	}, ROOT);

	
}


