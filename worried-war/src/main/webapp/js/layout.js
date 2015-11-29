var panelMarginReset = $('.slide-panel').css('margin-left');
//TODO if the window is resized across media boundaries, this'll be wrong

$('#slider-button').on('click', function() {
	var panel = $('.slide-panel');
	if (panel.hasClass("visible")) {
		panel.removeClass('visible').animate({
			'margin-left' : panelMarginReset
		});
	} else {
		panel.addClass('visible').animate({
			'margin-left' : '0px'
		});
		$(".twitter-timeline").height(panel.height());
	}
	
	/* don't have the icon rotate when the page first renders, otherwise
	 * toggle between active and inactive states */
	var icon = $('#slider-button span');
	if(!icon.hasClass("active") && !icon.hasClass("inactive")){
		icon.toggleClass("active");
	} else {
		icon.toggleClass("active");
		icon.toggleClass("inactive");
	}
	
	return false;
});
