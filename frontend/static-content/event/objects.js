function EventModel(controller) {
	var loadUri;
	var saveUri;
	var saveXsl;

	var processor;
	var serializer;

	xml = null;
	var guests=null;
	var tables=null;
	var eventInfo;
	var selected;
	
	var makeId = function (lastName, name) {
		return lastName.replace(/ /g, '_') + '_' + name.replace(/ /g, '_');
	};

	var serialize = function(dom){
		return serializer.serializeToString(dom);		
	};
	
	var getGuestList = function() {
		return guests;
	};

	var getTableList = function() {
		return tables;
	};

	var getEventInfo = function() {
		return eventInfo;
	};

	var getGuest = function(id) {
		var guest = guests.find('guest[id="' + id + '"]');
		return guest.size() == 1 ? guest : null;
	};

	var getSelectedGuest = function() {
		return getGuest(selected);
	};

	var setSelectedGuest = function(id) {
		selected = id;
	};

	var insertGuest = function(id, guest) {
		guests.append(
				$('<guest/>', xml).attr('id', id).attr('state', 'N')
				.append($('<lastname/>', xml).text(guest.lastname))
				.append($('<name/>', xml).text(guest.name))
				.append($('<quantity/>', xml).text(guest.quantity))
				.append($('<table/>', xml).text(guest.table))
				.append($('<cubierto/>', xml).text(guest.cubierto))
				.append($('<menu/>', xml).text(guest.menu))
				.append($('<babychair/>', xml).text(guest.babychair))
				.append($('<confirmed/>', xml).text(guest.confirmed))
				.append($('<comments/>', xml).text(guest.comments))
			);
	};

	var removeGuest = function(id) {
		if(id) {
			guests.find('guest[id="' + id +'"]').attr('state', 'D');			
		}	else {
			guests.find('guest[id="' + selected +'"]').attr('state', 'D');			
		}
	};

	var updateGuest = function(id, guest) {
	  var node = guests.find('guest[id="'+ id +'"]');
	  if(node.attr('state')=='E') {
	  	node.attr('state', 'U');
	  }

	  node.attr('id', guest.id);
	  node.find('lastname').text(guest.lastname);
	  node.find('name').text(guest.name);
	  node.find('quantity').text(guest.quantity);
	  node.find('table').text(guest.table);
	  node.find('cubierto').text(guest.cubierto);
	  node.find('menu').text(guest.menu);
	  node.find('babychair').text(guest.babychair);
	  node.find('confirmed').text(guest.confirmed);
	  node.find('comments').text(guest.comments);
	};

	var insertTable = function(table) {
		tables.append(
				$('<table/>', xml).attr('id', table.id).attr('state', 'N')
					.append($('<description/>', xml).text(table.description))
					.append($('<shape/>', xml).text(table.shape))
					.append($('<left/>', xml))
					.append($('<top/>', xml))
			);
	};

	var updateTable = function(id, position) {
	  var table = tables.find('table[id=' + id +']');
	  if(table.attr('state')=='E') {
	  	table.attr('state', 'U');
	  }
	  
		table.find('left').text(position.left);
		table.find('top').text(position.top);
	};
	
	var loadEvent = function() {
		var result = false;
		$.ajax({
			url: loadUri, 
			contentType: 'text/xml',
			success: function(data){
						xml = data;
						guests = $(xml).find('guests');
						guests.find('guest').each(function(){
							$(this).attr('server-id', $(this).attr('id'));
						});	
						tables = $(xml).find('tables');
						tables.find('table').each(function(){
							$(this).attr('server-id', $(this).attr('id'));
						});
						result = true;
					},
			error: function(ajax, textStatus, errorThrown) { 
					if(ajax.status==500) {
						result = false;
					}
				}
	});
		return result;
	};

	var saveEvent = function() {
		var result = false;
		var resultDocument = processor.transformToDocument(xml);
		$.ajax({
				type: 'POST',
				data: serialize(resultDocument), 
				contentType : "text/xml",
				url: saveUri, 
				success: function(data){
					xml = data;
					guests = $(xml).find('guests');
					guests.find('guest').each(function(){
						$(this).attr('server-id', $(this).attr('id'));
					});
					tables = $(xml).find('tables');
					tables.find('table').each(function(){
						$(this).attr('server-id', $(this).attr('id'));
					});
					result = "SUCCESS";
				},
				error: function(ajax, textStatus, errorThrown) { 
					if(ajax.status==500) {
						var msg = $('error>message',ajax.responseXML).text();
						if(msg.match('Permissions denied:')) {
							result = "NO_PERMISSION";
						} else {
							result = "ERROR";
						}
					} else if(ajax.status==403) {
						result = "NOT_LOGGED";
					} else {
						result = "ERROR";
					}
				}
		});
		saved = result;
		return result;
	};
	
	var init = function() {
		processor = new XSLTProcessor();
		$.ajax({url: '/ld-static/event/xsl/changes.xsl', success: function(data){ saveXsl = data;}});
		processor.importStylesheet(saveXsl);
		serializer = new XMLSerializer();

		loadUri = "/ld/load/event" + location.search;
		saveUri = "/ld/save/event" + location.search;		

		if(!loadEvent()) {
			throw 'Permissions denied: READ_ENABLED';
		}
	};
	
	// Constructor code
	this.getGuestList = getGuestList;
	this.getTableList = getTableList;
	this.getEventInfo = getEventInfo;
	this.getGuest = getGuest;
	this.getSelectedGuest = getSelectedGuest;
	this.setSelectedGuest = setSelectedGuest;
	this.updateGuest = updateGuest;
	this.getSelectedTable = null;
	this.setSelectedTable = null;
	this.updateTable = updateTable;
	this.insertGuest = insertGuest;
	this.insertTable = insertTable;
	this.removeGuest = removeGuest;
	this.saveEvent = saveEvent;
	this.loadEvent = loadEvent;
	init();
}

function EventView(controller, model) {

	//Variables
	var trans = {};	// Pointer to style sheets
	var processor;	// XSLT Processor
	var options;		// Config options dom
	var page = {};	// Pointer to html page

	//State variables
	var currentRow;
	var currView;
	
	var showInlineGuestForm = function (mode) {
		var id = null;
		switch (mode) {
		case 'add':
			page.guestForm.find('button[name=remove]').hide();
			page.guestForm.find('button[name=update]').hide();
			page.guestForm.find('button[name=copy]').hide();
			page.guestForm.find('button[name=insert]').show().addClass('disabled').unbind('click',execute);
			id = 'new';
			break;
		case 'edit':
			page.guestForm.find('button[name=remove]').show();
			page.guestForm.find('button[name=update]').show().addClass('disabled').unbind('click',execute);
			page.guestForm.find('button[name=copy]').show();
			page.guestForm.find('button[name=insert]').hide();
			id = model.getSelectedGuest().attr('id');
			break;
		}
		var row = page.listing.find('tr[id=' + id + ']');
		row.before(page.guestForm);
		if($.browser.msie) {
			page.guestForm.show();
		} else {
			page.guestForm.fadeIn(600);
		}
		row.hide();
	};

	var hideGuestForm = function () {
		var row;
		if(model.getSelectedGuest()) {
			var id = model.getSelectedGuest().attr('id');
			var row = page.listing.find('tr[id=' + id + ']');
		} else {
			row = page.listing.find('tr[id=new]');			
		}
		row.show();
		page.guestForm.hide();
		page.guestForm.find('button[name=insert]').show();
	};

	var showTableForm = function () {
		var tid=0;
		var tables = model.getTableList();
		$('table', tables).each(function(){
			if(tid < $(this).attr('id')) {
				tid = parseInt($(this).attr('id'));
			}
		});
		tid++;
		$('#tid',page.tableForm).text("Mesa #" + tid);
		$('input[name=id]',page.tableForm).val(tid);
		$('input[name=description]',page.tableForm).val('');
		page.tableForm.dialog('open');
	};

	var hideTableForm = function () {
		page.tableForm.dialog('close');			
	};

	var clearGuestForm = function () {
		$(':input:not(:button)', page.guestForm).val('');
	};

	var fillGuestForm = function () {
		var node = model.getSelectedGuest();
		$(node).children().each(function(){
			$(':input:not(:button)[name=' + $(this)[0].nodeName + ']',page.guestForm).val($(this).text());
		});
		if($('babychair',node).text() == 'true') {
			$(':checkbox[name=babychair]' ,page.guestForm).attr('checked', 'checked');
		} else {
			$(':checkbox:checkbox[name=babychair]' ,page.guestForm).attr('checked', '');
		}
		if($('confirmed',node).text() == 'true') {
			$(':checkbox[name=confirmed]' ,page.guestForm).attr('checked', 'checked');
		} else {
			$(':checkbox[name=confirmed]' ,page.guestForm).attr('checked', '');
		}
	};

	var parseGuestForm = function() {
		$(':text', page.guestForm).each(function() {
			$(this).val($(this).val().trim());
		});

		var rules = {
			lastname: {match:'.+', msg:'Debe ingresar un apellido para el invitado'},
			name: {match:'.+', msg:'Debe ingresar un nombre para el invitado'},
			quantity: {match:'\\d', msg:'Indique por favor cuantos invitados esta ingresando.'}
		};
		var invalidMsg = $.validate(page.guestForm, rules);
		if(!invalidMsg) {
			var result = {};
			$(':input:not(:button)',page.guestForm).each(function(){
				result[$(this).attr('name')] = $(this).val();
			});
			result['babychair'] = $(':checkbox[name=babychair]',page.guestForm).is(':checked').toString();
			result['confirmed'] = $(':checkbox[name=confirmed]',page.guestForm).is(':checked').toString();
			return result;
		} else {
			alert(invalidMsg);
			return null;
		}
	};

	var parseTableForm = function() {
		return {
			id: page.tableForm.find('input[name=id]').val(),
			description: page.tableForm.find('input[name=description]').val(),
			shape: page.tableForm.find('select[name=shape]').val()
		};
	};

	var changeView = function() {
		show($(this).attr('id'));	
	};
	
	var highlightRow = function(current, selected) {
		if (current) {
			current.removeClass('selected');
		}
		selected.addClass('selected');
	};
	
	var selectItem = function() {
		var id = $(this).attr('id');
		controller.selectRow(id);			
	};

	var execute = function(source) {
		var action = $(this).attr('name');
		switch (action) {
		case 'insert':
			var guest = parseGuestForm();
			if(guest) {
				controller.addGuest(guest);
			}
			break;
		case 'remove':
			controller.deleteGuest();
			break;
		case 'update':
			var guest = parseGuestForm();
			if(guest) {
				controller.updateGuest(guest);
			}
			break;
		case 'copy':
			controller.copyGuest();
			break;
		case 'save':
			controller.saveEvent();
			break;
		case 'table':
			controller.addTable(parseTableForm());
			break;
		case 'cancelGuest':
			hideGuestForm();
			break;
		case 'cancelTable':
			hideTableForm();
			break;
		}
	};

	var show = function(view) {
		currView = view;
		processor = new XSLTProcessor();
		processor.importStylesheet(trans[view]);
		if(view=='plane') {
			processor.setParameter(null, 'plane-url', eventInfo.getHallPlane());  
		}
		refresh();
	};
	
	var initGuestForm = function() {
		page.guestForm = $('#guestForm');
		page.guestForm.menus = page.guestForm.find('select[name=menu]');
		page.guestForm.cubiertos = page.guestForm.find('select[name=cubierto]');
		page.guestForm.table = page.guestForm.find('select[name=table]');

		options.find('menus > menu').each(
			function() {
				page.guestForm.menus.append($('<option/>').attr('value',$(this).attr('id')).text($(this).text()));
			}
		);
		
		options.find('cubiertos > cubierto').each(
			function() {
				page.guestForm.cubiertos.append($('<option/>').attr('value',$(this).attr('id')).text($(this).text()));
			}
		);

		page.guestForm.table.change(function(){		
			if($(this).val()=='new') {
				showTableForm();
			}
		});
		page.guestForm.find('button').click(execute);
		page.guestForm.find('input, select').change(
			function() {
				page.guestForm.find('button.disabled').click(execute).removeClass('disabled');				
			}
		);
		renderTableShape();
	}; 

	var refresh = function() {
		var resultDocument = processor.transformToFragment(model.getGuestList().context, document);
		page.listing.empty();
		page.listing.append(resultDocument);
		initGuestForm();
		hideGuestForm();
		$('a#add-table').click(showTableForm);
		switch (currView) {
		case 'plain':
			page.listing.find('>table>tbody>tr[id!=guestForm]').click(selectItem);
			page.listing.find('>table>tfoot>tr[id!=guestForm]').click(selectItem);
			break;
		case 'grouped':
			var tables = page.listing.find('div[id^=table-]');
			tables.droppable( {
				accept : function(drag) {
					return drag.is('tr') && $(this).find('tr[id=' + drag.attr('id') + ']').size()==0;
				},
				drop : function(event, ui) {
					var tableId = $(this).attr('id').replace('table-','');
					controller.updateTable(tableId, ui.draggable.attr('id'));
					var siblings = $(ui.draggable).siblings(':not(.grp-hdr,.guest-drag)');
					
					var t = ui.draggable.parent();
					while(!t.attr('id').match('^table-')){t = t.parent();}
					var cubs = parseInt($('span.cubiertos', t).text()) - parseInt(ui.draggable.attr('qt'));
					var bc = parseInt($('span.babychair', t).text()) - (ui.draggable.attr('bc')=='true'?1:0);
					$('span.cubiertos', t).text(cubs);
					$('span.babychair', t).text(bc);

					if(siblings.size() == 0) {
						$(ui.draggable).before('<tr id="empty"><td class="grp-col1"></td><td class="grp-col2"></td><td class="grp-col3"></td></tr>');
					}
					$('table.detail>tbody', this).append(ui.draggable).find('tr#empty').remove();

					cubs = parseInt($('span.cubiertos', this).text()) + parseInt(ui.draggable.attr('qt'));
					bc = parseInt($('span.babychair', this).text()) + (ui.draggable.attr('bc')=='true'?1:0);
					$('span.cubiertos', this).text(cubs);
					$('span.babychair', this).text(bc);
				}
			});
			tables.find('table.detail tr.item').draggable( {
				helper : function() {
					return $('<table/>').append($(this).clone()).addClass('guest-drag');
				},
				cursor : 'move',
				cursorAt: {left: 20, top: 10},
				distance : 8,
				opacity : 0.8,
				stop : function(event, ui) {
				}
			});
			
			tables.find('button.pin').click(function(){
				var widget = $(this).closest('div');
				if(widget.hasClass('ui-draggable')) {
					widget
						.draggable('destroy')
						.css({top:0, left:0, 'z-index': 'auto', 'border-style': 'solid'})
						.find('table.summary').css('cursor','');	
				} else {
					$('div.table-col>div.ui-draggable')
						.draggable('destroy')
						.css({top:0, left:0, 'z-index': 'auto', 'border-style': 'solid'})
						.find('table.summary').css('cursor','');	
					widget
					.draggable({cursor: 'move', opacity: 0.7, cursorAt: {left: 150, top: 25}})
					.css({'z-index':1, top: 10, left: 10, 'border-style': 'dotted'})
					.find('table.summary').css('cursor','move');	
				}
			});
			break;
		case 'summary':
			break;
		case 'plane':
			page.listing.find('div[id=map] > div').draggable(
					{
						containment: 'parent', 
						cursor: 'move', 
						distance: 8, 
						opacity: 0.35, 
						start: function(event, ui) {
							var widget = $(this);
							if(widget.css('position') != 'absolute') {
								widget.css('position', 'absolute');
							}
						},
						stop: function(event, ui) {
							controller.updateTablePos(ui.helper.attr('id'), ui.position);
						}
					}
			);
			page.listing.find('div[id=map] > div[positioned=true]').css('position','absolute');
			page.listing.find('div[id=map] > div[positioned=false]').css('position','static');
			break;
		}		
	};

	var setUnsaved = function() {
		$('#tabs>#save').addClass('enabled').bind('click', controller.saveEvent);
	};

	var setSaved = function() {
		$('#tabs>#save').removeClass('enabled').unbind('click', controller.saveEvent);
	};

	var loadAssets = function(data) {
		$.ajax({url: "/ld-static/event/options.xml",success : function(data) {options = $(data);}});
		$.ajax({url: "/ld-static/event/xsl/plain.xsl",success: function(data){trans.plain = data;}});
		$.ajax({url: "/ld-static/event/xsl/grouped.xsl",success: function(data){trans.grouped = data;}});
		$.ajax({url: "/ld-static/event/xsl/summary.xsl",success: function(data){trans.summary = data;}});
		$.ajax({url: "/ld-static/event/xsl/plane.xsl",success: function(data){trans.plane = data;}});		
	};
	
	var init = function() {
		processor = new XSLTProcessor();

		loadAssets();
		
		page.listing = $('#listing');
		page.loginForm = $('#loginForm');
		page.tableForm = $('#tableForm');
		
		page.loginForm.find('button').click(controller.login);
		
		page.tableForm.dialog({
			title: 'Agregar mesa',
			show : 'slide',
			hide : 'slide',
			autoOpen : false,
			resizable : false,
			modal : true,
			buttons : {
				'Crear Mesa' : function() {
					controller.addTable(parseTableForm());
				},
				Cancelar : function() {
					$(this).dialog('close');
				}
			}
		});
		
		$('#saveChanges').dialog({
			title: 'Subir cambios',
			show : 'slide',
			hide : 'slide',
			autoOpen : false,
			modal : true,
			buttons : {
				'Subir cambios' : function() {
					controller.saveEvent();		
				},
				'No subir cambios' : function() {
					$(this).dialog('close');
				}
			}
		});

		$('#tabs div.txt-tab').click(changeView);
		$('#tabs div[id=print]').click(function() {window.print();});
	};
		
	var renderTableShape = function(selected) {
		page.guestForm.table
			.empty()
			.append($('<option/>').attr('value', '0').text('Elija un Mesa'))
			.append($('<option/>').attr('value', 'new').text('Nueva mesa'));

		model.getTableList().children().sort(sortTables).each(
				function() {
					var id = $(this).attr('id');
					var desc = $(this).find('description').text();
					if (desc) {
						desc = id + ' - ' + desc;
					} else {
						desc = id;
					}
					page.guestForm.table.append($('<option/>').attr('value', id).text(desc));
				}
			);
		if(selected) {
			page.guestForm.table.val(selected);
		}
	};
	
	var sortTables = function(a, b) {
		return $(a).attr('id') - $(b).attr('id');
	};
	
	var showThrobber = function(show) {
		if(show) {
			$('#throbber').show();
		} else {
			$('#throbber').fadeOut(1000);
		}
	};

	var showLoginForm = function(show) {
		page.loginForm.toggle(show);
	};
	
	var saveChangesPopup = function() {
		$('#saveChanges').dialog('open');
	};

	// Constructor code
	this.showLoginForm = showLoginForm;
	this.showThrobber = showThrobber;
	this.showTableForm = showTableForm;
	this.hideTableForm = hideTableForm;
	
	this.showInlineGuestForm = showInlineGuestForm;
	this.hideGuestForm = hideGuestForm;
	this.fillGuestForm = fillGuestForm;
	this.clearGuestForm = clearGuestForm;
	this.setSaved = setSaved;
	this.setUnsaved = setUnsaved;
	this.saveChangesPopup = saveChangesPopup;
	this.show = show;
	this.refresh = refresh;
	this.renderTableShape = renderTableShape;
	this.currView = function() { return currView;};
	init();
}

function EventController() {
	var saved = true;

	var login;
	var model;
	var view;
	
	var makeId = function (lastName, name) {
		return lastName.replace(/ /g, '_') + '_' + name.replace(/ /g, '_');
	};

	var saveMsg = 'ATENCIÓN!!! Si abandona la página ahora sus últimas modificaciones no serán registradas.'; 
	var init = function() {

		$.ajaxSetup( {
			async : false,
			type : 'GET',
			dataType : 'xml',
			error : function(ajax, textStatus, errorThrown) {
				alert('error');
				console.log(textStatus, errorThrown);
			}
		});
		login = new Login();
		login.onLogged(function(role) {
			if(model.saveEvent()) {
				view.refresh();
				view.setSaved();
				saved = true;
			} else {
				alert('Error al subir cambios.');
			}
			login.hide();
		});

		try{
			model = new EventModel(this);
		} catch(e) {
			if(e.match('Permissions denied:.*READ_ENABLED')) {
				$("<div>").text("Se han revocado los permisos para abrir este evento. Por favor contacte a su organizador para regularizar su situación.").dialog({title:'Permisos revocados'});
			} else {
				$("<div>").text("Ha ocurrido un error. Por favor inténtelo nuevamente en unos minutos.").dialog({title:'Mensaje de error'});
			}
			return;
		} 
		model = new EventModel(this);			
		view = new EventView(this, model);
		view.show('plain');
		window.onbeforeunload = function() {
			if(!saved) {
				return saveMsg;
			}
		};
	};
	
	var selectRow = function(id) {
		view.hideGuestForm();
		if(id=='new') {
			view.clearGuestForm();
			view.showInlineGuestForm('add');			
			model.setSelectedGuest(null);	
		} else {
			model.setSelectedGuest(id);	
			view.fillGuestForm();
			view.showInlineGuestForm('edit');			
		}
	};
	
	var saveEvent = function() {
		view.showThrobber(true);
		result = model.saveEvent();
		switch (result) {
		case 'SUCCESS':
			view.refresh();
			view.setSaved();
			saved = true;
			break;
		case 'NO_PERMISSION':
			alert('Ud. no tiene permisos para subir cambios. Por favor contacte a su Organizador.');
			break;
		case 'NOT_LOGGED':
			login.show();
			break;
		case 'ERROR':
			alert('Ha ocurrido un error al intentar subir sus cambios.');
			break;
		}
		view.showThrobber(false);
	}; 

	var addGuest = function(guest) {
		var id = makeId(guest.lastname, guest.name);
		if(!model.getGuest(id)) {
			model.insertGuest(id, guest);
			view.refresh();
			view.setUnsaved();
			saved = false;
		} else {
			alert('El nombre del invitado ya existe en el listado.');
		}
	};
	
	var updateGuest = function(guest) {
		var id = model.getSelectedGuest().attr('id');
		guest.id = makeId(guest.lastname, guest.name);
		model.updateGuest(id, guest);
		view.refresh();
		view.setUnsaved();
		saved = false;
	};

	var copyGuest = function() {
		view.hideGuestForm();
		view.showInlineGuestForm('add');			
		model.setSelectedGuest(null);	
	};

	var deleteGuest = function() {
		model.removeGuest();
		view.refresh();
		view.setUnsaved();
		saved = false;
	};

	var addTable = function(table) {
		model.insertTable(table);
		view.hideTableForm();
		view.renderTableShape(table.id);
		if(view.currView()=='grouped' || view.currView()=='plane'){
			view.refresh();
		}
		view.setUnsaved();
		saved = false;
	}; 

	var updateTable = function(tableId, guestId) {
		model.updateGuest(guestId, {id: guestId, table:tableId});			
		view.setUnsaved();
		saved = false;
	};
		
	var updateTablePos = function(id, position) {
		model.updateTable(id, position);
		view.setUnsaved();
		saved = false;
	};

	var deleteTable = function(id) {
		console.log('borrame');
		saved = false;
	};
	
	// Constructor code
	this.init = init;
	this.selectRow = selectRow;

	this.saveEvent = saveEvent;

	this.addGuest = addGuest;
	this.copyGuest = copyGuest;
	this.updateGuest = updateGuest;
	this.deleteGuest = deleteGuest;

	this.addTable = addTable;
	this.updateTable = updateTable;
	this.deleteTable = deleteTable;
	this.updateTablePos = updateTablePos;
}

function EventInfo(container) {
	var planeUrl;
	
	var load = function() {
		$.ajax( {
			async: true,
			url: '/ld/list/event/info' + location.search,
			dataType: 'xml',
			success: render,
			error: function(){}
		});
	};

	var render = function(xml) {
		planeUrl = $('hall>plane',xml).text();
		var anfitriones = '';
		$('host',xml).each(function(){
			anfitriones += ( '<a href="mailto:' + $('user', this).text() + '">' + $('name', this).text() + ' ' + $('lastname', this).text() + '</a>, ');
		});
		var html = '<p><strong>Datos del Evento</strong><br>' +
			'Tipo de evento: ' + $('type',xml).text() + '<br/>' +
			'Organiza: ' + $('planner',xml).text() + '<br/>' +
			'Salón: ' + $('hall>name',xml).text() + '<br/>' +
			'Fecha del Evento: ' + $('date',xml).text() + '<br/>' +
			'Anfitriones: ' + anfitriones.substring(0, anfitriones.length-2) + '</p>';
		container.append(html);
		$('img', container).remove();
	};

	load();
	
	this.getHallPlane = function() {
		return planeUrl;
	};
}

function StandAloneEventInfo(container) {
	var planeUrl;
	
	var load = function() {
		$.ajax( {
			async: true,
			url: '/ld/list/event/info' + location.search,
			dataType: 'xml',
			success: render,
			error: function(){}
		});
	};

	var render = function(xml) {
		planeUrl = $('hall>plane',xml).text();
		var anfitriones = '';
		$('host',xml).each(function(){
			anfitriones += ( '<a href="mailto:' + $('user', this).text() + '">' + $('name', this).text() + ' ' + $('lastname', this).text() + '</a>, ');
		});
		var d = $('date',xml).text();
		var eventDate = new Date(d.substring(6),d.substring(3,5)-1,d.substring(0,2));
		var expires = new Date($('permission[type=writeEnabled]',xml).attr('expires'));
		var html = '<span style="float:left; width: 60%"><p><strong>Datos del Evento</strong><br>' +
			'Tipo de evento: ' + $('type',xml).text() + '<br/>' +
			'Organiza: ' + $('planner',xml).text() + '<br/>' +
			'Fecha del Evento: ' + (isNaN(eventDate)?'never':eventDate.format('dd MMM yyyy')) + '<br/>' +
			'Anfitriones: ' + anfitriones.substring(0, anfitriones.length-2) + '</p></span>' +
			'<span><p><strong>Sus permisos caducan el día: ' + (isNaN(expires)?'never':expires.format('dd MMM yyyy')) + '</strong><br/>' +
			'<form target="MercadoPago" action="https://www.mercadopago.com/mla/buybutton" method="post">' +
			'<input type="hidden" name="acc_id" value="15357217"/>' +
			'<input type="hidden" name="enc" value="508e4b850d1d23fd98f8423429290604"/>' +
			'<input type="hidden" name="item_id" value="STANDALONEEVENT"/>' +
			'<input type="hidden" name="name" value="SINGLE EVENT PACK"/>' +
			'<input type="hidden" name="price" value="120.0"/>' +
			'<input type="hidden" name="currency" value="ARG"/>' +
			'<input type="hidden" name="ship_cost_mode" value=""/>' +
			'<input type="hidden" name="op_retira" value=""/>' +
			'<input type="hidden" name="url_process" value="http://www.listadigital.com.ar/ld-static/event/standalone.html?PROCESS"/>' +
			'<input type="hidden" name="url_succesfull" value="http://www.listadigital.com.ar/ld-static/event/standalone.html?SUCCESFULL"/>' +
			'<input type="hidden" name="url_cancel" value="http://www.listadigital.com.ar/ld-static/event/standalone.html?CANCEL"/>' +
			'<input type="hidden" name="extra_part" value="STANDALONEEVENT-' + $('event',xml).attr('id') + '"/>' +
			'<input type="submit" name="checkout" value="Extender permisos por 3 meses más" class="nxt-payment-button" />' +
			'</form></span>';
		container.append(html);
		$('img', container).remove();
	};

	load();
	
	this.getHallPlane = function() {
		return planeUrl;
	};
}

function Tools(container) {

	var xml;
	var processor;
	var exportUrl = '/ld/download/event' + location.search;
	var versionsUrl = '/ld/list/event/versions' + location.search;
	var restoreUrl = location.search.length>0 ? '/ld/restore/event' + location.search + '&version=':'/ld/restore/event?version=';
	
	var exportCsv = function() {
		location.href = exportUrl;
	};
	
	var showVersions = function() {
		$.ajax({
			async: false,
			url : versionsUrl,
			success : function(data) {
				container.empty();
				var table = $('<table/>').addClass('versions')
					.append($('<tr class="head"><td colspan="2">Versiones anteriores</td></tr>'));
				container.append(table);
				var date = new Date();
				$('item', data).each(function(i) {
					var it = $(this);
					date.setTime(it.text());
					var dateStr = $.sprintf('Versión del día: %02d/%02d/%04d a las %02d:%02d:hs', date.getDate(), date.getMonth()+1, date.getFullYear(), date.getHours(), date.getMinutes());
					var tr = $('<tr>')
						.append($('<td>').text(dateStr))
						.append($('<td>').append($('<a>').attr('href', restoreUrl + it.text()).click(function(){return confirm('Se reemplazara la lista actual por la versión seleccionada.');}).text('Reemplazar')))
						.addClass(i%2?'even':'odd')
					;
					table.append(tr);
				});
				
			}
		});
	};

	var render = function() {
		var resultDocument = processor.transformToFragment(xml, document);
		container.empty();
		container.append(resultDocument);
		$('#csv', container).click(exportCsv);
		$('#replace', container).click(showVersions);
	};

	var init = function() {
		processor = new XSLTProcessor();
		var async = 2;
		$.ajax({
			url : "/ld-static/event/xsl/tools.xsl",
			success : function(data) {
				processor.importStylesheet(data);
				if(--async == 0) {
					$('#tabs div[id=tools]').click(render);
				}
			}
		});

		$.ajax({
			url : "/ld-static/event/tools.xml",
			success : function(data) {
				xml = data; 
				if(--async == 0) {
					$('#tabs div[id=tools]').click(render);
				}
			}
		});
	};
	
	init();
}
