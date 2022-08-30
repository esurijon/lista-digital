function Error(container) {
	container.dialog( {
		title: 'Error al iniciar sesión',
		show : 'slide',
		hide : 'slide',
		resizable : false,
		autoOpen : false,
		modal : true,
		buttons : {
			Ok : function() {
				$(this).dialog('close');
			}
		}
	});

	this.show = function(message) {
		container.text(message);
		container.dialog('open');
	};
}

function Login(container) {
	$('a[action=login]',container).click(function(){submit();});
	$('span[action=resetPassword]',container).click(resetPassword.show);
	
	$('select[name=PLANNER_ID]',container).mousedown(function() {
		$(this).removeClass('gray');
	});

	$('select[name=PLANNER_ID]',container).change(function() {
		$(this).toggleClass('gray',$(this).val() == 'none');
	});

	$('input[name=USER]',container).focus(function() {
		if($(this).val() == 'e-mail') {
			$(this).val('');
			$(this).removeClass('gray');			
		} 
	});

	$('input[name=USER]',container).blur(function() {
		if($(this).val() == '') {
			$(this).val('e-mail');
			$(this).addClass('gray');
		} else {
			$(this).removeClass('gray');			
		}
	});

	$('input[name=PASSWORD]',container).blur(function() {
	    if($('input[name=PASSWORD]',container).val() == '') {
	    	$('input[name=PASSWORD_LBL]',container).show();
	        $('input[name=PASSWORD]',container).addClass('hidden');
	    }
	});

	$('input[name=PASSWORD_LBL]',container).focus(function() {
		$('input[name=PASSWORD_LBL]',container).hide();
		$('input[name=PASSWORD]',container).removeClass('hidden').focus();
	});
	
	$.ajax({
		async: true,
		dataType: 'xml',
		url: '/ld/list/planners',
		success: function(data){
			var planners = $('select[name=PLANNER_ID]', container);
			$('planner', data).each(function() {
				var planner = $(this);
				var opt = $('<option/>').val(planner.attr('id')).text(planner.text());
				$('select[name=PLANNER_ID]', container).append(opt);
			});
		}
	});
	
	var rolePageMap = {
			HOST: '/event/main.html',
			PLANNER: '/planner/main.html'
	};

	var getField = function(field) {
		return $('*[name=' + field + ']', container).val();
	};

	var setField = function(field, value) {
		$('*[name=' + field + ']', container).val(value);
	};

	var submit = function() {
		var form = $('form', container);
		var rules = {
			PLANNER_ID: {match:'^none$|^separator$', msg:'Por favor selecciona un Organizador para poder iniciar sesión.', negate: true},
			USER: {match:'\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*', msg: 'La dirección de e-mail ingresada no parece ser válida.'},
			PASSWORD: {match:'.+', msg: 'Por favor ingresa tu contraseña para poder iniciar sesión.'}
		};
		var invalidMsg = $.validate(form, rules);
		if(!invalidMsg) {
			$.ajax({
				async : false,
				type : 'POST',
				data : form.serialize(),
				url : '/ld/login',
				success : function(data) {
					if($('status',data).text() == 'LOGGED') {
						var role = $('role',data).text();
						var isStandAloneEvent = role == 'HOST' && $('select[name=PLANNER_ID]', form).val() == '0'; 
						if(isStandAloneEvent) {
							var url = '/event/standalone.html';
						} else {
							var url = rolePageMap[role];
						}
						form.attr('action', url);
						form.submit();
					}
				},
				error : function(xhr, status, err) {
					if(xhr.status == 403) {
						error.show("Los datos ingresados no son correctos. Por favor revíselos e inténtelo nuevamente.");
					} else {
						error.show("Ha ocurrido un error al intentar iniciar tu sesión, por favor inténtalo nuevamente en unos minutos.");
					}
				}
			});
		} else {
			error.show(invalidMsg);
		}
		return false;
	};
	
	this.getField = getField;
	this.setField = setField;
	this.submit = submit;
}

function ResetPassword(container) {
	
	container.dialog( {
		title: 'Olvidé mi contraseña',
		show : 'slide',
		hide : 'slide',
		resizable : false,
		autoOpen : false,
		modal : true,
		buttons : {
			Ok : function() {submit();},
			Cancel : function() {
				$(this).dialog('close');
				slider.play(true);
			}
		}
	});

	$.ajax({
		async: true,
		url: '/ld/list/planners',
		success: function(data){
			var planners = $('select[name=PLANNER_ID]', container);
			$('planner', data).each(function() {
				var planner = $(this);
				var opt = $('<option/>').val(planner.attr('id')).text(planner.text());
				$('select[name=PLANNER_ID]', container).append(opt);
			});
		}
	});

	var getField = function(field) {
		return $('*[name=' + field + ']', container).val();
	};

	var setField = function(field, value) {
		$('*[name=' + field + ']', container).val(value);
	};

	var show = function() {
		slider.stop();
		$('#message', container).text("");
		setField('USER', login.getField('USER'));
		setField('PLANNER_ID', login.getField('PLANNER_ID'));
		container.dialog('open');
	};

	var submit = function() {
		var form = $('form', container);
		var rules = {
			PLANNER_ID: {match:'^none$|^separator$', msg:'Por favor selecciona un Organizador.', negate: true},
			USER: {match:'\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*', msg: 'La dirección de e-mail ingresada no parece ser válida.'}
		};
		var invalidMsg = $.validate(form, rules);
		if(!invalidMsg) {
			$.ajax( {
				async : false,
				type : 'POST',
				data : form.serialize(),
				url : '/ld/resetPassword',
				success : function(data) {
					if($('status',data).text() == 'PASSWORD_RESET') {
						container.dialog('close');
						$("<div class='form-message'><p>Verifica tu casilla de correo, en minutos recibirás un email para cambiar tu contraseña.</p></div>").dialog({
							title: 'Solicitud de cambio enviada',
							buttons : {
								Aceptar: function() {
									$(this).dialog('close');
									slider.play(true);
								}
							}
						});
					} else {
						$('#message', container).text("El e-mail ingresado no está registrado. Verifique los datos e inténtelo nuevamente.");
					}
				},
				error : function() {
					$('#message', container).text("Error al enviar mail de cambio de contraseña. Por favor intente nuevamente en unos minutos.");
				}
			});
		} else {
			$('#message', container).text(invalidMsg);
		}
		return false;
	};
	
	this.getField = getField;
	this.setField = setField;
	this.submit = submit;
	this.show = show;
}

function ChangePassword(container) {
	var planner;
	var user;
	
	container.dialog( {
		title: 'Modificar contraseña',
		show : 'slide',
		hide : 'slide',
		resizable : false,
		autoOpen : false,
		modal : true,
		buttons : {
			Ok : function() {
				submit();
			},
			Cancel : function() {
				$(this).dialog('close');
				slider.play(true);
			}
		}
	});

	var getField = function(field) {
		return $('*[name=' + field + ']', container).val();
	};

	var setField = function(field, value) {
		$('*[name=' + field + ']', container).val(value);
	};

	var show = function(encodedParams) {
		if(encodedParams) {
			setField('ENCODED', encodedParams);
			var params = encodedParams.decode64().split('&');
			var parameterMap = {};
			for(var p in params) {
				var pair = params[p].split('=');
				parameterMap[pair[0]] = pair[1];
			}
			planner = parameterMap.PLANNER;
			user = parameterMap.USER;
			$('#planner', container).text(planner);
			$('#user', container).text(user);
		}
		slider.stop();
		$('#message', container).text("Por favor, ingresa tu nueva contraseña:");
		$(':password', container).val('');
		container.dialog('open');
	};

	var submit = function() {
		var invalidMsg;
		if(getField('NEW_PASSWORD').length < 6 ) {
			invalidMsg = "La contraseña debe tener al menos 6 caracteres.";
		}
		if(getField('NEW_PASSWORD')!=getField('PASSWORD_CONFIRM')) {
			invalidMsg = "La contraseña y su repetida no son iguales.";
		}
		if(!invalidMsg) {
			var form = $('form', container);
			$.ajax( {
				async : false,
				type : 'POST',
				data : form.serialize(),
				url : '/ld/changePassword',
				success : function(data) {
					if($('status', data).text() == 'PASSWORD_CHANGED') {
						container.dialog('close');
						$("<div><p class='form-message'>Su contraseña se ha actualizado exitosamente, intente iniciar sesión ahora.</p><br/>Recuerda que para iniciar sesión deberás indicar: <ul style='margin-left: 15px; list-style: square'><li>Organizador: <b>" + planner + "</b></li><li>E-mail: <b>" + user + "</b></li></ul></div>").dialog({
							title: 'Contraseña actualizada',
							buttons : {
								Aceptar: function() {
									$(this).dialog('close');
									slider.play(true);
								}
							}
						});
					} else {
						$('#message', container).text("El e-mail ingresado no está registrado para el organizador elegido. Verifique los datos e intente nuevamente.");
					}
				},
				error : function() {
					$('#message', container).text("Ah ocurrido un error al intentar cambiar su contraseña. Por favor inténtalo nuevamente en unos minutos.");
				}
			});
		} else {
			$('#message', container).text(invalidMsg);
		}
		return false;
	};

	this.getField = getField;
	this.setField = setField;
	this.submit = submit;
	this.show = show;
}

function ActivateAccount(container) {
	var planner;
	var user;
	
	container.dialog( {
		title: 'Activar cuenta',
		show : 'slide',
		hide : 'slide',
		resizable : false,
		autoOpen : false,
		modal : true,
		buttons : {
			Ok : function() {
				submit();
			},
			Cancel : function() {
				$(this).dialog('close');
				slider.play(true);
			}
		}
	});

	var getField = function(field) {
		return $('*[name=' + field + ']', container).val();
	};

	var setField = function(field, value) {
		$('*[name=' + field + ']', container).val(value);
	};

	var show = function(encodedParams) {
		if(encodedParams) {
			setField('ENCODED', encodedParams);
			var params = encodedParams.decode64().split('&');
			var parameterMap = {};
			for(var p in params) {
				var pair = params[p].split('=');
				parameterMap[pair[0]] = pair[1];
			}
			planner = parameterMap.PLANNER;
			user = parameterMap.USER;
			$('#planner', container).text(planner);
			$('#user', container).text(user);
		}
		slider.stop();
		$('#message', container).text("Activa tu cuenta creando una contraseña para tu usuario:");
		$(':password', container).val('');
		container.dialog('open');
	};

	var submit = function() {
		var invalidMsg;
		if(getField('NEW_PASSWORD').length < 6 ) {
			invalidMsg = "La contraseña debe tener al menos 6 caracteres.";
		}
		if(getField('NEW_PASSWORD')!=getField('PASSWORD_CONFIRM')) {
			invalidMsg = "La contraseña y su repetida no son iguales.";
		}
		if(!invalidMsg) {
			var form = $('form', container);
			$.ajax( {
				async : false,
				type : 'POST',
				data : form.serialize(),
				url : '/ld/changePassword',
				success : function(data) {
					if($('status', data).text() == 'PASSWORD_CHANGED') {
						container.dialog('close');
						$("<div><p class='form-message'>Su cuenta se ha activado exitosamente, intente iniciar sesión ahora.</p><br/>Recuerda que para iniciar sesión deberás indicar: <ul style='margin-left: 15px; list-style: square'><li>Organizador: <b>" + planner + "</b></li><li>E-mail: <b>" + user + "</b></li></ul></div>").dialog({
							title: 'Cuenta activada',
							buttons : {
								Aceptar: function() {
									$(this).dialog('close');
									slider.play(true);
								}
							}
						});
					} else {
						$('#message', container).text("El e-mail ingresado no está registrado para el organizador elegido. Verifique los datos e intente nuevamente.");
					}
				},
				error : function() {
					$('#message', container).text("Ah ocurrido un error al intentar activar tu cuenta. Por favor inténtalo nuevamente en unos minutos.");
				}
			});
		} else {
			$('#message', container).text(invalidMsg);
		}
		return false;
	};

	this.getField = getField;
	this.setField = setField;
	this.submit = submit;
	this.show = show;
}

function RegisterPlanner(container) {
	container.dialog( {
		title: 'Registrar nueva cuenta',
		show : 'slide',
		hide : 'slide',
		autoOpen : false,
		resizable : false,
		modal : true,
		width: 350,
		buttons : {
			Enviar : function() {
				submit();
			},
			Cancelar : function() {
				$(this).dialog('close');
				slider.play(true);
			}
		}
	});
	
	$('#showRegisterForm').click(function(){show();});
	$('#linkRegisterForm').click(function(){show();});

	var	serializer = new XMLSerializer();

	var getField = function(field) {
		return $('*[name=' + field + ']', container).val();
	};

	var setField = function(field, value) {
		$('*[name=' + field + ']', container).val(value);
	};

	var show = function() {
		slider.stop();
		$('#message', container).text("");
		$('input', container).val('');
		container.dialog('open');
	};

	function parseFormData(form){
	    var date = new Date();
	    var plnr = $.form2json(form);
	    var dom = (new DOMParser()).parseFromString('<' + form.attr('name') + '/>', "text/xml");
	    var xml = $.json2dom(plnr, form.attr('name'), dom);
	    return serializer.serializeToString(xml);		
	};
	
	var submit = function() {
		$(':text', container).each(function() {
			$(this).val($(this).val().trim());
		});

		var rules = {
			name: {match:'.+', msg: 'Por favor ingrese un nombre para la empresa/organizador.'},
			address: {match:'.+', msg: 'Por favor ingrese una dirección.'},
			phone: {match:'.+', msg: 'Debe ingresar un numero de teléfono.'},
			user: {match:'\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*', msg: 'La dirección de e-mail ingresada no parece ser válida.'},
			contactPerson: {match:'.+', msg: 'Por favor indique el nombre de la persona de contacto.'}
		};
		
		var invalidMsg = $.validate(container, rules);	

		if(!invalidMsg) {
			var form = $('form', container);
			var xml = parseFormData(form);
			$.ajax( {
				async : false,
				type : 'POST',
				data : xml,
				url : '/ld/registerPlanner',
				success : function(data) {
					if($('status', data).text() == 'PLANNER_REGISTERED') {
						container.dialog('close');
						$("<div class='form-message'><p>Verifica tu casilla de correo, en minutos recibirás un email para activar tu cuenta de usuario.</p><p>Al activar tu cuenta se te pedirá que crees una contraseña, la cual te será requerida para ingresar al sitio.</p></div>").dialog({
							title: 'Registración exitosa',
							buttons : {
								Aceptar: function() {
									$(this).dialog('close');
									slider.play(true);
								}
							}
						});
					} else {
						$('#message', container).empty().append($("<div>Ya contamos en nuestros registros con un empresa con el mismo nombre.<br/>Por favor ingresa un nuevo nombre de empresa o <a href=\"mailto:info@listadigital.com.ar?subject=Pedido de arbitraje por nombre de empresa duplicado\">contáctanos</a> para reclamar el nombre solicitado.</div>"));
					}
				},
				error : function() {
					$('#message', container).empty().text("Lo sentimos a ocurrido un error al intentar registrarte, por favor inténtalo nuevamente en unos minutos.");
				}
			});
		} else {
			$('#message', container).empty().text(invalidMsg);
		}
		return false;
	};
	
	this.getField = getField;
	this.setField = setField;
	this.submit = submit;
	this.show = show;
}
