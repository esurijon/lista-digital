function Uploader(form){
	var iframe;

	var init = function() {
		iframe = $('<iframe/>').attr('name', 'upload_iframe')
								.css('display', 'none');
		$('body').append(iframe);
	};

	var upload = function(callback){
		form.target = iframe.attr('name');
		form.submit();
		uploadCallBack = callback;
	};
	
	this.upload = upload;
		
	init();
}

function PlannerInfo(container) {
	var load = function() {
		$.ajax({
			url : '/ld/list/planner/info' + location.search,
			data : '',
			dataType: 'xml',
			success : function(xml) {
				var tools = new Tools($('#listing'),$('planner',xml).attr('id'));
				render(xml);
			}
		});
	};

	var render = function(xml) {
		plannerEmail = $('user', xml).text();
		var html = '<p><strong>Organizador de Eventos: '+ $('planner>name', xml).text() + '</strong><br>' + 
			'Dirección: ' + $('address', xml).text() + '<br/>' + 
			'Teléfono: ' + $('phone', xml).text() + '<br/>' + 
			'E-mail: ' + $('user', xml).text() + '<br/>' + 
			'Persona de contacto: ' + $('contactPerson', xml).text() + '<br/></p>';
		container.append(html);
		$('img', container).remove();
	};

	load();
}

function Halls(container, xml) {
	var processor;
	var uploader;
	var selected;
	var form;

	var addButtons = function(container, buttons) {
		container.empty();
		for (prop in buttons) {
			container.append($('<button/>').text(prop).addClass('form-btn').click(buttons[prop]));
		}
	};
	
	var render = function() {
		var resultDocument = processor.transformToFragment(xml.context, document);
		container.empty();
		container.append(resultDocument);
		$('tbody>tr[id!=hall-form][id!=new]', container).click(showEdit);
		$('tbody>tr[id=new]', container).click(showAdd);

		$('input[name=view]', container).click(function(){
			$(this).attr('checked', 'checked');
			var custom = $(this).val() == 'custom';
			if(custom){
				$('input[name=plane-img]', container).removeAttr('disabled');
				var planeUrl = $('plane', selected).text();
				if(planeUrl != '') {
					$('#hall-plane', container).attr('src', planeUrl);
				}
			}
			else{
				$('input[name=plane]', container).val('/planner-assets/hall.png');
				$('#hall-plane', container).attr('src', '/planner-assets/hall.png');
				$('input[name=plane-img]', container).attr('disabled', 'disabled');
			}
		});

		$('input[name=plane-img]', container).change(upload);
		form = $('#hall-form', container);
		uploader = new Uploader($('form', container)[0]);
	};

	var showEdit = function() {
		var row = $(this);

		if(selected) {
			$('tr#' + selected.attr('id'), container).show();
		} else {
			$('tr#new', container).show();
		}
		
		selected = $('hall[id=' + row.attr('id') + ']', xml);
		addButtons($('#footer', container), {
			CANCELAR: cancel,
			ELIMINAR: remove,
			ACEPTAR: update
		});
		$('input[name=view]', container).removeAttr('disabled');
		$('input[name=name]', form).val($('name', selected).text());
		$('#hall-plane', container).attr('src', $('plane', selected).text());
		var plane = $('plane', selected).text();
		if(plane == '/planner-assets/hall.png' || plane == ''){
			$($('input[name=view]', container)[0]).click();
		} else {
			$($('input[name=view]', container)[1]).click();
		}

		row.before(form);
		row.hide();
		
		if($.browser.msie) {
			form.show();
		} else {
			form.fadeIn(600);
		}
	};

	var showAdd = function() {
		var row = $(this);

		if(selected) {
			$('tr#' + selected.attr('id'), container).show();
		} 
		selected = null;

		addButtons($('#footer', container), {
			CANCELAR: cancel,
			AGREGAR: add
		});
		$('input[name=view]', container).removeAttr('disabled');
		$('input[name=name]', form).val('');
		$('#hall-plane', container).attr('src', '/planner-assets/hall.png');
		$($('input[name=view]', container)[0]).click();
		
		row.before(form);
		row.hide();
		if($.browser.msie) {
			form.show();
		} else {
			form.fadeIn(600);
		}
	};
	
	var cancel = function() {
		selected = null;
		render();
	};

	var remove = function() {
		switch(selected.attr('state')) {
		case 'E': 
		case 'U': 
		case 'N': 
			selected.attr('state','D');
			break;
		}
		$(that).trigger('changed', ['remove',{'@id': selected.attr('id')}]);
		render();
	};

	var update = function() {
		var hall = $.form2json(form);
		hall['@id']=selected.attr('id');
		
		switch(selected.attr('state')) {
		case 'E': 
		case 'U': 
			selected.attr('state', 'U');
			break;
		case 'N': 
			selected.attr('state', 'N');
			break;
		}
		$('name', selected).text(hall.name);		
		$('plane', selected).text(hall.plane);		

		$(that).trigger('changed', ['update',hall]);
		render();
	};

	var add = function() {
		var hall = $.form2json(form);
		hall['@id'] = new Date().getTime();
		hall['@state'] = 'N';
		xml.append($.json2dom(hall,'hall', xml));

		$(that).trigger('changed', ['add',hall]);
		render();
	};

	var init = function(){
		processor = new XSLTProcessor();
		$.ajax({
			url : "/planner/xsl/halls.xsl",
			success : function(data) {
				processor.importStylesheet(data);
				$(that).trigger('ready', xml);
			}
		});
	};

	var upload = function() {
		var uploadBtn = $('input[name=plane-img]', container);
		$('#hall-plane', container).attr('src','/img/throbber.gif');
		uploader.upload(function(data){
			var planeUrl = $('message', data).text();
			$('input[name=plane]', container).val(planeUrl);
			$('#hall-plane').attr('src', planeUrl);
			uploadBtn.attr('disabled','');
		});
		uploadBtn.attr('disabled','disabled');
	};

	var setXml = function(data) {
		xml = $('halls', data);
	};

	this.render = render;
	this.setXml = setXml;
	var that = this;
	init();
}

function Events(container, xml) {
	var processor;
	var selected;

	var remove = function() {
		switch(selected.attr('state')) {
		case 'E': 
		case 'U': 
		case 'N': 
			selected.attr('state','D');
			break;
		}
		$(that).trigger('changed');
		form.dialog('close');		
		render();
	};

	var update = function() {
		if(validateEvent()) {
			$('type',selected).text($('select[name=type]',form).val());
			$('hall',selected).text($('select[name=hall]',form).val());
			$('date',selected).text($('input[name=date]',form).val());

			var bride = $('host[type=bride]', selected);
			$('name',bride).text($('input[name=bride.name]',form).val());
			$('lastname',bride).text($('input[name=bride.lastname]',form).val());
			$('user',bride).text($('input[name=bride.user]',form).val());
			$('phone',bride).text($('input[name=bride.phone]',form).val());
			$('cellphone',bride).text($('input[name=bride.cellphone]',form).val());

			var groom = $('host[type=groom]', selected);
			$('name',groom).text($('input[name=groom.name]',form).val());
			$('lastname',groom).text($('input[name=groom.lastname]',form).val());
			$('user',groom).text($('input[name=groom.user]',form).val());
			$('phone',groom).text($('input[name=groom.phone]',form).val());
			$('cellphone',groom).text($('input[name=groom.cellphone]',form).val());

			$('permissions>permission[type=writeEnabled]',selected).attr('granted', !$('input[name=block]').is(':checked'));

			switch(selected.attr('state')) {
			case 'E': 
			case 'U': 
				selected.attr('state','U');
				break;
			case 'N': 
				selected.attr('state','N');
				break;
			}

			$(that).trigger('changed');
			form.dialog('close');		
			render();
		}
	};

	var validateBrideEmail = function(field) {
		if(!field.val().match('\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*')) {
			return 'La dirección de e-mail de la novia no parece ser válida.';
		}
		var groomEmail = $('input[name=groom.user]', form).val();
		if(field.val() == groomEmail) {
			return 'La dirección de e-mail de la novia no puede ser la misma que la del novio.';
		}
		if(field.val() == plannerEmail) {
			return 'La dirección de e-mail de la novia no puede ser la misma que la del organizador.';
		}
	};
	
	var validateGroomEmail = function(field) {
		if(!field.val().match('\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*')) {
			return 'La dirección de e-mail del novio no parece ser válida.';
		}
		var brideEmail = $('input[name=bride.user]', form).val();
		if(field.val() == brideEmail) {
			return 'La dirección de e-mail del novio no puede ser la misma que la de la novia.';
		}
		if(field.val() == plannerEmail) {
			return 'La dirección de e-mail del novio no puede ser la misma que la del organizador.';
		}
	};

	var validateEvent = function() {
		$(':text', form).each(function() {
			$(this).val($(this).val().trim());
		});

		var rules = {
			date: {match:'^\\d\\d-\\d\\d-\\d\\d\\d\\d$', msg: 'Por favor ingrese una fecha para el evento.'},
			'bride.name': {match:'.+', msg: 'Por favor ingrese el nombre de la novia.'},
			'bride.lastname': {match:'.+', msg: 'Por favor ingrese el apellido de la novia.'},
			'bride.user': {match: validateBrideEmail, msg: 'La dirección de e-mail de la novia no parece ser válida.'},
			'groom.name': {match:'.+', msg: 'Por favor ingrese el nombre del novio.'},
			'groom.lastname': {match:'.+', msg: 'Por favor ingrese el apellido del novio.'},
			'groom.user': {match: validateGroomEmail, msg: 'La dirección de e-mail del novio no parece ser válida.'}
		};
		var invalidMsg = $.validate(form, rules);
		if(!invalidMsg) {
			return true;
		} else {
			$('#message',form).text(invalidMsg);
			return false;
		}
	};

	var add = function() {
		if(validateEvent()) {
			var event = $.form2json(form);
			event['@id'] = new Date().getTime();
			event['@state'] = 'N';
			event.bride['@type'] = 'bride';
			event.groom['@type'] = 'groom';
			event.hosts = [event.bride,event.groom];
			delete event.bride;
			delete event.groom;
			delete event.block;

			xml.append($.json2dom(event,'event', xml, {hosts: 'host'}));

			$(that).trigger('changed');
			form.dialog('close');		
			render();
		}
	};
	
	var editEventButtons = {
		Aceptar : update,
		Eliminar : remove,
		Cancelar : function() {
			$(this).dialog('close');
		}
	};
	
	var addEventButtons = {
		Agregar : add,
		Cancelar : function() {
			$(this).dialog('close');
		}
	};

	var form = $('#eventForm').dialog({
		show : 'slide',
		hide : 'slide',
		autoOpen : false,
		resizable : false,
		modal : false,
		width: 600
	});
	
	$("#calendar", form).datepicker({
		dateFormat: 'dd-mm-yy',
		monthNames: ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
		dayNamesMin: ['Do', 'Lu', 'Ma', 'Mie', 'Jue', 'Vie', 'Sab']
	});


	var render = function() {
		var resultDocument = processor.transformToFragment(xml.context, document);
		container.empty();
		container.append(resultDocument);
		$('tbody>tr', container).click(editEvent);
		$('tfoot>tr', container).click(addEvent);
		$('a.ver', container).click(function(){
			var uri = $(this).attr('href');
			if(uri != 'unsaved') {
				window.open(uri);
			} else {
				alert('Suba los cambios para poder ver este evento.');
			}
			return false;
		});
		$('a.mail', container).click(function(){window.location = $(this).attr('href'); return false;});
	};

	var editEvent = function(){
		$('#message', form).text('');
		var id = $(this).attr('id');
		selected = $('event#' + id ,xml);

		if(selected.attr('state')=='N') {
			$('input[name=block]',form).attr('disabled','disabled');			
		}else {
			$('input[name=block]',form).removeAttr('disabled');			
		}	

		$('select[name=type]',form).val($('type',selected).text());
		$('select[name=hall]',form).val($('hall',selected).text());
		$('input[name=date]',form).val($('date',selected).text());

		var bride = $('host[type=bride]', selected);
		$('input[name=bride.name]',form).val($('name',bride).text());
		$('input[name=bride.lastname]',form).val($('lastname',bride).text());
		$('input[name=bride.user]',form).val($('user',bride).text());
		$('input[name=bride.phone]',form).val($('phone',bride).text());
		$('input[name=bride.cellphone]',form).val($('cellphone',bride).text());

		var groom = $('host[type=groom]', selected);
		$('input[name=groom.name]',form).val($('name',groom).text());
		$('input[name=groom.lastname]',form).val($('lastname',groom).text());
		$('input[name=groom.user]',form).val($('user',groom).text());
		$('input[name=groom.phone]',form).val($('phone',groom).text());
		$('input[name=groom.cellphone]',form).val($('cellphone',groom).text());
		
		if(writeEnabled(selected)) {
			$('input[name=block]',form).removeAttr('checked');
		} else {
			$('input[name=block]',form).attr('checked', 'checked');
		}

		form.dialog('option', 'title', 'Modificar evento');
		form.dialog('option', 'buttons', editEventButtons).dialog('open');
	};
	
	var writeEnabled = function(planner) {
		var enabled = false;
		if(planner.attr('state')=='N') {
			enabled = true;		
		} else {
			var granted = $('permission[type=writeEnabled]',planner).attr('granted')=='true';
			var expires = $('permission[type=writeEnabled]',planner).attr('expires');
			if(granted && (expires == 'never' || Date.parse(expires) > new Date())) {
				enabled = true;
			}
		}
		return enabled;
	};

	var addEvent = function(){
		$('input[name=block]',form).removeAttr('checked');
		$('input[name=block]',form).attr('disabled', 'disabled');
		$('#message', form).text('');
		$(':input:not(:button)', form).val('');
		form.dialog('option', 'title', 'Agregar nuevo evento');
		form.dialog('option', 'buttons', addEventButtons).dialog('open');
	};

	var init = function(){
		processor = new XSLTProcessor();
		$.ajax({
			url : "/planner/xsl/plain.xsl",
			success : function(data) {
				processor.importStylesheet(data);
				processor.setParameter(null, 'query-string', location.search.substring(1));  
				$(that).trigger('ready');
			}
		});
	};

	var setXml = function(data) {
		xml = $('events', data);
	};

	var setHalls = function(ev, halls) {
		var h = $('select[name=hall]', form);
		h.empty();
		h.append($('<option>')
			.attr('value', '')
			.text('Escoja un salón')
		);
		$('hall[state!=D]', halls).each(function() {
			h.append($('<option>')
						.attr('value', $(this).attr('id'))
						.text($('name', this).text())
			);
		});
	};

	var updateHall = function(ev, action, hall) {
		switch(action) {
		case 'add': 
			var sel = $('select[name=hall]', form);
			sel.append($('<option>')
				.attr('value', hall['@id'])
				.text(hall.name)
			);
			break;
		case 'update': 
			var opt = $('select[name=hall]>option[value=' + hall['@id'] + ']', form);
			opt.text(hall.name);
			break;
		case 'remove': 
			var opt = $('select[name=hall]>option[value=' + hall['@id'] + ']', form);
			opt.remove();
			break;
		}
	};

	this.updateHall = updateHall;
	this.setHalls = setHalls;
	this.update = update;
	this.render = render;
	this.setXml = setXml;
	var that = this;
	init();
}

function Tools(container, plannerId) {
	var loginHtml = '<a href=\'javascript:if(typeof(ListaDigital)=="undefined"){plannerId="' + plannerId + '";var LD_SCRIPT=document.createElement("script");LD_SCRIPT.src="http://www.listadigital.com.ar/js/remoteLogin.js";LD_SCRIPT.type="text/javascript";document.getElementsByTagName("head")[0].appendChild(LD_SCRIPT);}else{ListaDigital(plannerId);}void(null);\'>Ver mi Lista</a>';

	var init = function() {
		$('#tools').click(render);
	};

	var render = function() {
		container.empty();
		container
			.append($('<p>').text('Haga que sus clientes puedan acceder a su lista desde su página Web'))
			.append($('<p>').text('Copie y  pegue este código HTML en su sitio Web:'))
			.append($('<textarea>').text(loginHtml).attr('cols', 60).attr('rows', 10).attr('readonly','readonly'))
			.append($('<p>').text('Y tendrá un link como el de aquí abajo. Haga click sobre el mismo para ver como funcionaría en su sitio.'))
			.append($(loginHtml));
	};

	init();
}

function Payments(container) {
	
	var processor;
	var xml;
	
	var init = function(){
		processor = new XSLTProcessor();
		var requests = [ {
			url : '/planner/xsl/payments.xsl',
			dataType : 'xml'
		}, {
			url : '/ld/payments/load' + location.search,
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
				$(that).trigger('ready');
			} else {
				alert('Ha ocurrido un error');
			}
		});
	};

	var render = function() {
		var resultDocument = processor.transformToFragment(xml, document);
		container.empty();
		container.append(resultDocument);
	};

	var that = this;
	this.render = render;
	init();
}

function Saver(xml) {
	var changed = false;
	var serializer = new XMLSerializer();
	var processor = null;
	var saveMsg = 'ATENCIÓN!!! Si abandona la página ahora sus últimas modificaciones no serán registradas.'; 
	
	var init = function() {
		$.ajax({
			url: '/planner/xsl/changes.xsl',
			success: function(data){ 
				processor = new XSLTProcessor();
				processor.importStylesheet(data);
			}
		});

		window.onbeforeunload = function() {
			if(changed) {
				return saveMsg;
			}
		};
	};
	
	var save = function() {
		if(!confirmDeletes()) {
			uncheckedsave();
		} 
	};
	
	var deletesCont = $('<ul>');
	var deletesPopup = $('<div>')
	.append($('<p class="form-message">Si decide subir los cambios, se borraran los siguientes eventos junto con las listas de invitados asociadas.</p><br/><p class="form-message">Seleccione los eventos que no desee que sean eliminados.</p>'))
	.append(deletesCont)
	.dialog( {
		title: 'Atencion',
		autoOpen : false,
		modal : true,
		width: 500,
		buttons : {
			'Subir cambios' : function() {
				$(':checked', deletesPopup).each(function() {
					$('event[id=' + $(this).val() + ']', xml).attr('state','E');
				});
				$(this).dialog('close');
				uncheckedsave();
			},
			'Cancelar' : function() {
				$(this).dialog('close');
			}
		}
	});

	var confirmDeletes = function() {
		var deletes = $('events>event[state=D]', xml);
		if(deletes.size()>0) {
			deletesCont.empty();
			$('events>event[state=D]', xml).each(function(){
				var event = $(this);
				var evtName = '';
				$('host', event).each(function(){evtName+= $('name',this).text() + ' ' + $('lastname',this).text() + ', ';});
				var lbl = $('<label>').text(evtName.substring(0, evtName.length-2) + ' (' + $('date',event).text() + ')');
				var chk = $('<input type="checkbox" style="margin-right: 6px;">').attr('value',event.attr('id'));
				deletesCont.append($('<li style="list-style: none;">').append(chk).append(lbl));
			});
			deletesPopup.dialog('open');
			return true;
		} else {
			return false;
		}
	};
	
	
	var uncheckedsave = function() {
		
		$('#throbber').show();

		saveUri = "/ld/save/planner" + location.search;
		var resultDocument = processor.transformToDocument(xml);
		$.ajax({
			type: 'POST',
			data: serializer.serializeToString(resultDocument), 
			url: saveUri, 
			success: function(data){
				xml = data;
				$('#throbber').fadeOut(1000);
				$('#save').removeClass('enabled').unbind('click', save);
				changed = false;
				$(that).trigger('saved', xml);
			},
			error: function(xhr, textStatus, errorThrown) { 
				if(xhr.status == 403) {
					var login = new Login();
					login.onLogged(function(role) {
						login.hide();
						save();
					});
					login.show();
				} else if(xhr.status == 500) {
					var msg = $('error>message',xhr.responseXML).text();
					if(msg.match('Permissions denied:')) {
						$("<div>").text("Ud. no tiene permisos suficientes para realizar esta operación. Por favor contactenos para regularizar su situación.").dialog({title:'Subir cambios'});
					} else {
						$("<div>").text("No se han podido subir los cambios. Por favor inténtelo nuevamente en unos minutos.").dialog({title:'Subir cambios'});
					}
				} else {
					$("<div>").text("No se han podido subir los cambios. Por favor inténtalo nuevamente en unos minutos.").dialog({title:'Subir cambios'});
				}
				$('#throbber').hide();
			}
		});
	};

	var setUnsaved = function() {
		if(processor) {
			$('#save').click(save).addClass('enabled');			
			changed = true;
		}
	};

	this.setUnsaved = setUnsaved;
	that = this;
	init();
}
