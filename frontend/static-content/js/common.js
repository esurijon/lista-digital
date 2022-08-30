Date.prototype.months = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

Date.prototype.days = ['Domingo', 'Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado'];

Date.prototype.leadingZeros = function(val, length) { 
	var len = length ? length : 2;
	var str = String(val); 
	while(str.length<len) {
		str = '0'+str;
	}
	return str; 
};

Date.prototype.yyyy = function() {
	return this.getFullYear(); 
};

Date.prototype.yy = function() { 
	return this.leadingZeros(this.getFullYear() % 1000); 
};

Date.prototype.MM = function() { 
	return this.leadingZeros(this.getMonth()+1); 
};

Date.prototype.MMM = function() { 
	return this.months[this.getMonth()].substring(0,3); 
};

Date.prototype.MMMM = function() { 
	return this.months[this.getMonth()]; 
};

Date.prototype.dd = function() { 
	return this.leadingZeros(this.getDate()); 
};

Date.prototype.ddd = function() { 
	return this.days[this.getDay()].substring(0,3); 
};

Date.prototype.dddd = function() { 
	return this.days[this.getDay()]; 
};

Date.prototype.hh = function() {
	var hours = this.getHours(); 
	if(hours>12) hours-=12; 
	return this.leadingZeros(hours); 
};

Date.prototype.a = function() {
	return this.getHours() > 12 ? 'PM' : 'AM'; 
};

Date.prototype.HH = function() {
	return this.leadingZeros(this.getHours());
};

Date.prototype.mm = function() {
	return this.leadingZeros(this.getMinutes()); 
};

Date.prototype.ss = function() {
	return this.leadingZeros(this.getSeconds()); 
};

Date.prototype.SSS = function() {
	return this.leadingZeros(this.getMilliseconds(), 3);
};

Date.prototype.format = function(pattern) {
	var fields = pattern.match(/yyyy|yy|MMMM|MMM|MM|dddd|ddd|dd|hh|HH|mm|ss|SSS|a/g)
	for(var i=0; i < fields.length; i++){
		pattern = pattern.replace(fields[i], this[fields[i]]());
	}
	return pattern;
};

String.prototype.trim = function() {
	return this.replace(/^\s+|\s+$/g,"");
};

String.prototype.ltrim = function() {
	return this.replace(/^\s+/,"");
};

String.prototype.rtrim = function() {
	return this.replace(/\s+$/,"");
};

String.prototype.keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

String.prototype.encode64 = function() {
	var output = '';
	var chr1, chr2, chr3;
	var enc1, enc2, enc3, enc4;
	var i = 0;
	while (i < this.length) {
		chr1 = this.charCodeAt(i++);
		chr2 = this.charCodeAt(i++);
		chr3 = this.charCodeAt(i++);
		enc1 = chr1 >> 2;
		enc2 = (chr1 & 3) << 4 | chr2 >> 4;
		enc3 = (chr2 & 15) << 2 | chr3 >> 6;
		enc4 = chr3 & 63;
		if (isNaN(chr2)) {
			enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
			enc4 = 64;
		}
		output += (this.keyStr.charAt(enc1) + this.keyStr.charAt(enc2) + this.keyStr.charAt(enc3) + this.keyStr.charAt(enc4));
	}
	return output;
};

String.prototype.decode64 = function() {
	var input = this;
	var output = '';
	var chr1, chr2, chr3;
	var enc1, enc2, enc3, enc4;
	var i = 0;
	input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
	while (i < input.length) {
		enc1 = this.keyStr.indexOf(input.charAt(i++));
		enc2 = this.keyStr.indexOf(input.charAt(i++));
		enc3 = this.keyStr.indexOf(input.charAt(i++));
		enc4 = this.keyStr.indexOf(input.charAt(i++));
		chr1 = enc1 << 2 | enc2 >> 4;
		chr2 = (enc2 & 15) << 4 | enc3 >> 2;
		chr3 = (enc3 & 3) << 6 | enc4;
		output += String.fromCharCode(chr1);
		if (enc3 != 64) {
			output += String.fromCharCode(chr2);
		}
		if (enc4 != 64) {
			output += String.fromCharCode(chr3);
		}
	}
	return output;
};

function clientSupport() {
	var ie6 = $.browser.msie && ($.browser.version < 7);
	var supported = $.support.boxModel && !ie6; 
	if(!supported) {
		var msg = $.param($.extend({msg: 'NOT SUPPORTED BROWSER'}, $.browser));
		$.ajax({
			url: '/ld/track',
			data: msg.encode64()
		});
		var popup = $('<div>').append('<p>Lo sentimos la versión de su navegador es muy vieja y no esta soportada por Lista Digital.</p><p>Por favor actualice su navegador a la última versión.</p>');
		$('body').append(popup);
		popup.dialog({
			modal: true, 
			closeOnEscape: false, 
			title: 'Navegador no soportado', 
			disabled: true, 
			draggable: false,
			close: function(event, ui) { 
				if(history.length == 0) {
					window.close(); 
				} else {
					history.back(); 
				}
			}
		});
	}
	return supported;
}

function Slider(contents, keypad, interval, effectinterval) {
	
	var forward = true;
	var slides = $('#'+contents + ' div');
	var current = 0;
	var running = false;
	
	function changeSlide() {
		var next;
		var dir;
		var rdir;
		if(forward) {
			next = (current+1) % slides.length;
			dir = 'left';
			rdir = 'right';
		} else {
			rdir = 'left';
			dir = 'right';
			next = (current-1) % slides.length;
		}
		$(slides[current]).toggle('slide', {direction: dir}, effectinterval);
		$(slides[next]).toggle('slide', {direction: rdir}, effectinterval);
		current = next;
	}
	
	var toggle = function() {
		if(running) {
			stop();
		} else {
			play(true);
		}
	};

	var stop = function() {
		running = false;
		$('#'+contents).stopTime();
		$('#play', keypad).removeClass('pause').addClass('play');
	};
	
	var play = function(fwd) {
		forward = fwd;
		running = true;
		$('#'+contents).everyTime(interval, changeSlide);
		$('#play', keypad).removeClass('play').addClass('pause');
	}; 
	
	var prev = function() {
		stop();
		forward = false;
		changeSlide();
	};

	var next = function() {
		stop();
		forward = true;
		changeSlide();
		$('#play', keypad).removeClass('pause').addClass('play');
	};
	
	var show = function(index) {
		$(slides[current]).toggle('slide', {direction: 'left'}, effectinterval);
		$(slides[index]).toggle('slide', {direction: 'right'}, effectinterval);
		current = index;
	};

	this.toggle = toggle;
	this.stop = stop;
	this.play = play;
	this.prev = prev;
	this.next = next;
	this.show = show;

	var keypad = $('#'+keypad);
	$('#play', keypad).addClass('pause').click(this.toggle);
	$('#prev', keypad).addClass('prev').click(this.prev);
	$('#next', keypad).addClass('next').click(this.next);
}

function Reporter(context) {
	var body;

	var build = function() {
		body = $('<div>').addClass('body').hide()
			.append($('<div>').append('<form><div>Descripción:<br/><textarea name="description" rows="8" cols="40" style="overflow-x: hidden"></textarea></div><input type="checkbox" name="always"/> Ocurre siempre</form>'))
			.append($('<div>').addClass('error'))
			.append($('<div>')
				.append($('<button>').text('ACEPTAR').addClass('form-btn').click(report))
				.append($('<button>').text('CANCELAR').addClass('form-btn').click(collapse))
			)
		;
		var header = $('<div>').addClass('header').text('Reportar falla ...').click(function(){body.toggle();});
		var reporterHtml = $('<div>').addClass('reporter')
			.addClass('reporter')
			.append(header)
			.append(body)
		;

		$('body').append(reporterHtml);
	};
	
	var open = function() {
		body.show();
	};
	
	var collapse = function(index) {
		body.hide();
	};

	var report = function(index) {
		$.ajax({
			url: '/ld/report',
			data: 'context=' + context + '&' + $('form', body).serialize(),
			success: function(data, textStatus, xhr) {
				$('.error', body).text('');
				$('textarea', body).val('');
				$('input:checkbox', body).attr('checked', false);
				body.hide();		
			},
			error: function(xhr, textStatus, errorThrown) {
				$('.error', body).text('No se pudo reportar la falla. Por favor inténtelo en otro momento.');
			}
		});
	};
	
	build();

	this.open = open;
	this.collapse = collapse;
	this.report = report;
}

function Login(plannerId) {

	var loginForm;

	var init = function() {
		loginForm = $(
			'<form>' +
				'<div class="msg">Su sesión ha caducado, por favor vuelva a ingresar sus datos.</div>' +
				'<label class="form-label">Organizador:</label>' + 
				'<select name="PLANNER_ID">' +
				'	<option value="none">Organizador</option>' +
				'</select>' +
				'<p><label class="form-label">E-mail:</label> <input name="USER" type="text" class="form-field"/></p>' +
				'<p><label class="form-label">Contraseña: </label><input name="PASSWORD" type="password" class="form-field"/></p>' +
			'</form>'
		)
		.hide()
		.dialog({
			autoOpen : false,
			title: 'Iniciar sesión',
			show : 'slide',
			hide : 'slide',
			resizable : false,
			modal : true,
			buttons : {
				Ok : function() {doLogin();},
				Cancel : function() {
					$(this).dialog('close');
				}
			}
		});

		$.ajax({
			async: true,
			dataType: 'xml',
			url: '/ld/list/planners',
			success: function(data){
				var planners = $('select[name=PLANNER_ID]', loginForm);
				$('planner', data).each(function() {
					var planner = $(this);
					var opt = $('<option/>').val(planner.attr('id')).text(planner.text());
					$('select[name=PLANNER_ID]', loginForm).append(opt);
				});
			}
		});
	};

	var show = function() {
		$('div.msg', loginForm).text('Su sesión ha caducado, por favor vuelva a ingresar sus datos.');
		loginForm.dialog('open');
	};
	
	var hide = function() {
		loginForm.dialog('close');
	};

	var doLogin = function() {
		var rules = {
			PLANNER_ID: {match:'^none$|^separator$', msg:'Por favor selecciona un Organizador para poder iniciar sesión.', negate: true},
			USER: {match:'\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*', msg: 'La dirección de e-mail ingresada no parece ser válida.'},
			PASSWORD: {match:'.+', msg: 'Por favor ingresa una contraseña para poder iniciar sesión.'}
		};
		var invalidMsg = $.validate(loginForm, rules);
		if(!invalidMsg) {
			$.ajax({
				async : false,
				type : 'POST',
				data : loginForm.serialize(),
				url : '/ld/login',
				success : function(data) {
					if(loggedCallback) {
						if($('status',data).text() == 'LOGGED') {
							var role = $('role',data).text();
							loggedCallback(role);
						}
					}
				},
				error : function(xhr, status, error) {
					if(xhr.status == 403) {
						$('div.msg', loginForm).text("Los datos ingresados no son correctos. Por favor revíselos e inténtelo nuevamente.");
					} else {
						$('div.msg', loginForm).text("Ha ocurrido un error al intentar iniciar tu sesión, por favor inténtalo nuevamente en unos minutos.");
					}
				}
			});
		} else {
			$('div.msg', loginForm).text(invalidMsg);
		}
		return false;
	};

	var loggedCallback = null;
	
	var onLogged = function(f) {
		loggedCallback = f;
	};
	
	this.show = show;
	this.hide = hide;
	this.onLogged = onLogged;
	init();
};

function AdminLogin(plannerId) {

	var loginForm;

	var init = function() {
		loginForm = $(
			'<form>' +
				'<div class="msg">Su sesión ha caducado, por favor vuelva a ingresar sus datos.</div>' +
				'<p><label class="form-label">Usuario:</label> <input name="USER" type="text" class="form-field"/></p>' +
				'<p><label class="form-label">Contraseña: </label><input name="PASSWORD" type="password" class="form-field"/></p>' +
			'</form>'
		)
		.hide()
		.dialog({
			autoOpen : false,
			title: 'Iniciar sesión de Admin',
			show : 'slide',
			hide : 'slide',
			resizable : false,
			modal : true,
			buttons : {
				Ok : function() {doLogin();},
				Cancel : function() {
					$(this).dialog('close');
				}
			}
		});
	};

	var show = function() {
		$('div.msg', loginForm).text('Su sesión ha caducado, por favor vuelva a ingresar sus datos.');
		loginForm.dialog('open');
	};
	
	var hide = function() {
		loginForm.dialog('close');
	};

	var doLogin = function() {
		var rules = {
			USER: {match:'.+', msg: 'Por favor ingresa un nombre de usuario para poder iniciar sesión.'},
			PASSWORD: {match:'.+', msg: 'Por favor ingresa una contraseña para poder iniciar sesión.'}
		};
		var invalidMsg = $.validate(loginForm, rules);
		if(!invalidMsg) {
			$.ajax({
				async : false,
				type : 'POST',
				data : loginForm.serialize(),
				url : '/ld/admin/login',
				success : function(data) {
					if(loggedCallback) {
						if($('status',data).text() == 'LOGGED') {
							var role = $('role',data).text();
							loggedCallback(role);
						}
					}
				},
				error : function(xhr, status, error) {
					if(xhr.status == 403) {
						$('div.msg', loginForm).text("Los datos ingresados no son correctos. Por favor revíselos e inténtelo nuevamente.");
					} else {
						$('div.msg', loginForm).text("Ha ocurrido un error al intentar iniciar tu sesión, por favor inténtalo nuevamente en unos minutos.");
					}
				}
			});
		} else {
			$('div.msg', loginForm).text(invalidMsg);
		}
		return false;
	};

	var loggedCallback = null;
	
	var onLogged = function(f) {
		loggedCallback = f;
	};
	
	this.show = show;
	this.hide = hide;
	this.onLogged = onLogged;
	init();
}

function UserInfo(container, success, error) {
	var load = function() {
		$.ajax( {
			url : '/ld/list/user/info',
			dataType: 'xml',
			success : render,
			error : error
		});
	};

	var render = function(xml) {
		$('div.welcome', container).text($('role', xml).text());
		$('div.user', container).text($('email', xml).text()).attr('title', $('email', xml).text());
		if($('role', xml).attr('type')!="GUEST") {
			success();
		} else {
			error();
		}
	};

	load();
}

function Tabs() {
	var tabs = {};
	var lastRendered;

	var register = function(id, tab) {
		tabs[id] = tab;
		$('#'+id).click(click);
	};
	
	var click = function() {
		var tab = $(this).attr('id');
		show(tab);
	};
	
	var show = function(tab) {
		if(tab){
			lastRendered = tabs[tab];
		}
		lastRendered.render();
	};

	this.register = register;
	this.show = show;
}