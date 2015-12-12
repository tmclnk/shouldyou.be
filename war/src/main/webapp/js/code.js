/* jshint laxbreak:true */
//ROOT = window.location.href.split('/').slice(0, 3).join('/') + '/_ah/api';
//ROOT='https://shouldibeworrying.appspot.com/_ah/api';
ROOT='//localhost:8080/_ah/api';
var theList = [];
var map;
var markers = [];

function addMarker(incident){
	if(!incident.createdAt){
		console.log("No create date!  Incident will default to 'now'");
	}

	var createMoment = moment(incident.createdAt);
	var timestampMoment = moment(incident.timestamp);

	/* if we have an entry in localStorage for this incident id
	 * we'll call it "read" */
	var isRead = !!localStorage[incident.id];

	var incidentMarkers = [];
	incident.locations.forEach(function(latLng) {
		console.log(incident);
		//the thing that is to be drawn
		var marker = new google.maps.Marker({
			position : new google.maps.LatLng(latLng.lat, latLng.lng),
			animation : google.maps.Animation.DROP,
			map : map,
			title : incident.text,
			zIndex:0,
			icon : {
				path: SHEILD, //not my typo, it's map-icon's typo
				fillColor: isRead === true ? '#999999' : '#0E77E9',
				fillOpacity: opacityOf(createMoment),
				strokeColor: '#ffffff',
				strokeWeight: 1,
				// rotate the icon randomly between -180 and 180 (because it's whimsical,
				// and because often multiple icons land in the same location and overap)
				rotation: Math.floor(Math.random() * 180 * (Math.random() < 0.5 ? -1 : 1)),
				scale: 1/4
			}
		});

		map.panTo(marker.getPosition());  

		// make a marker bounce on the screen if it's newish (whatever that means)
		// and isn't marked as read
		if(!isRead && createMoment.isAfter(moment().subtract(15, 'm'))){
			marker.setAnimation(google.maps.Animation.BOUNCE);
		}

		google.maps.event.addListener(marker, 'click', function() {
			localStorage[incident.id] = 1;
		});

		incidentMarkers.push(marker);
		markers.push(marker);
	});

	//when a marker is clicked, go through and make it
	//and the other related markers stop blinking
	incidentMarkers.forEach(function(marker){
		//CREATE A WINDOW TO SHOW DETAILS
		var timeId = new Date().getTime();
		var infoWindow = new google.maps.InfoWindow({
			content : "<b id='"+timeId+"'>" + createMoment.fromNow() 
			+ "</b><br/><a href='http://twitter.com/"
			+ incident.screenName + "' target='new'>@"
			+ incident.screenName + "</a><div style='width:200px'>"
			+ incident.text + "</div><div><a href='http://twitter.com/"
			+ incident.screenName + "/status/" + incident.tweetId
			+ "' target='new'>original tweet</div>"
		});

		//SET A TIMER TO UPDATE THE OPACITY OF THE ICON
		var intervalId = setInterval(function(){
			var timeElapsedElt = document.getElementById(timeId);
			// it's possible for things to get out of sync on service restart
			// so gracefully let it happen
			if(timeElapsedElt){
				timeElapsedElt.innerHTML = createMoment.fromNow();

				marker.getIcon().fillOpacity = opacityOf(createMoment);
				marker.setIcon(marker.getIcon());
			}
		}, 60000);


		//When the marker is clicked, open an infoWindow
		var isOpen = false; //is the infoWindow open?  track on open and close
		google.maps.event.addListener(marker, 'click', function() {
			//don't open multiple infoWindows
			if(isOpen === true){
				return;
			}

			marker.setZIndex(marker.getZIndex() - 100);
			infoWindow.open(map,marker);
			isOpen = true;

			// TODO this doesn't really make sense to on incidents with
			// multiple markers, e.g. domestic disturbances in groups of 3
			incidentMarkers.forEach(function(m){
				m.getIcon().fillColor = '#999999';
				m.setIcon(m.getIcon());
				m.setAnimation(null);
			});
		});

		google.maps.event.addListener(infoWindow,'closeclick', function(){
			isOpen = false;
		});
	});

}

function addUserMarker(theMap){

	navigator.geolocation.getCurrentPosition(function(pos){
		var latLng = { lat: pos.coords.latitude, lng: pos.coords.longitude };
		//theMap.panTo(latLng);
		var marker = new google.maps.Marker({
			position:latLng,
			animation : google.maps.Animation.DROP,
			map : map,
			title : "you, roughly",
			zIndex:0
		});
	});



}

/** returns a number in the range [0.25,1] indicating the opacity we want
 * for an icon based on how long ago the incident occurred */
function opacityOf(moment){
	var incidentMillis = moment.toDate().getTime();
	var nowMillis = new Date().getTime();

	// an hour ago
	var startTime = (nowMillis - 3600000) ;

	//normalize these so 24 hours ago is 0
	var pct = (incidentMillis - startTime) / (nowMillis - startTime);
	var opacity = Math.max(0.25, pct);
	return opacity;
}


function clearMarkers() {
	markers.forEach(function(m) {
		m.setMap(null);
	});
	markers = [];
}

clearMarkers();

