function Planners(container) {
	var processor;
	var xsl;
	var xml;

	var init = function() {
		processor = new XSLTProcessor();
		var requests = [ {
			url : '/ld-static/admin/xsl/plain.xsl',
			dataType : 'xml'
		}, {
			url : '/ld/load/admin',
			dataType : 'xml'
		} ];
		$.majax(requests, function(results) {
			var error = null;
			for ( var i in results) {
				if (results[i].status >= 400) {
					error = results[i].data;
					break;
				}
			}
			if (!error) {
				processor.importStylesheet(results[0].data);
				xml = results[1].data;
				render();
			} else {
				alert('Ha ocurrido un error');
			}
		});
	};

	var render = function() {
		var resultDocument = processor.transformToFragment(xml, document);
		container.empty();
		container.append(resultDocument);
		$('a.ver', container).click(function(){
			var uri = $(this).attr('href');
			if(uri != 'unsaved') {
				window.open(uri);
			} else {
				alert('Suba los cambios para poder ver este evento.');
			}
			return false;
		});
	};
	
	init();
}