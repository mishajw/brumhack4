$(".row input").on('click', function(event) {
	$.ajax({
		url: '/rate',
		type: 'POST',
		data: {
			id: shpId,
			value: $(this).val(),
		},
	})
	.done(function() {
		console.log("success");
	})
	.fail(function() {
		console.log("error");
	});
});