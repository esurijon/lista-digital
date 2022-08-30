/*
 
 */
function Macro() {
	var ctx;
	var macros;
	var events;
	var playCount=0;

	var store = {
		click: function(event) {
			return {
				type: event.type,
				path: $(event.target).getPath(),
				pageX: event.pageX,
				pageY: event.pageY
			}
		},
		dblclick: function(event) {
			return {
				type: event.type,
				path: $(event.target).getPath(),
				pageX: event.pageX,
				pageY: event.pageY
			}
		},
		change: function(event) {
			return {
				type: event.type,
				path: $(event.target).getPath(),
				value: $(event.target).val()
			}
		}
	};

	var fire = {
		click: function(event, next) {
			var el = $(event.path);
			var pos = {
				top: el.position() + el.height()/2,
				left: el.position() + el.width()/2
			}
			cursor.animate(pos, function(){
				el.trigger(event.type); next();			
			})
		},
		change: function(event, next) {;$(event.path).val(event.value); next();}
	};
	
	var recOpts = {
	};

	var playOpts = {
	};

	var start = function(options, context) {
		ctx =  context ? context : $(document);
		var opts = $.extend({},recOpts, options);
		events = [];
		ctx.bind('dblclick click change', record);
	};
	
	var stop = function() {
		ctx.unbind('dblclick click change', record);
		return events;
	};
	
	var record = function(event) {
		events.push(store[event.type](event));
	};
	
	var play = function() {
		var event = macros[playCount++];
		if(event) {
			if(fire[event.type]) {
				fire[event.type](event, play);
			}
		} else {
			playCount = 0;
		}
	};
	
	var locateCss = {
		top: {top: 0, right: 0, bottom: '85%', left: 0},
		right: {top: 0, right: 0, bottom: 0, left: '85%'},
		bottom: {top: '85%', right: 0, bottom: 0, left: 0},
		left: {top: 0, right: '85%', bottom: 0, left: 0},
	};

	var locate = function(pos) {
		board.css(locateCss[pos]);
	};
	
	var board = $('<div>')
		.append($('<div>')
			.append($('<button>').text('REC').click(start))
			.append($('<button>').text('STOP').click(function(){ macros = stop();}))
			.append($('<button>').text('PLAY').click(play))
		)
		.append($('<div>')
			.append($('<button>').text('<').click(function(){ locate('left');}))
			.append($('<button>').text('^').click(function(){ locate('top');}))
			.append($('<button>').text('>').click(function(){ locate('right');}))
			.append($('<button>').text('_').click(function(){ locate('bottom');}))
		)
		.append($('<div>').text('lista de macros'))
		.css({
			border: '1px solid red',
			position: 'fixed'
		})
	;
	var cursor = $('<div>').attr('id','macro-cursor').show();
	$('body').append(board).append(cursor);

	
	this.start = start;
	this.stop = stop;
	this.play = play;
	this.locate = locate
	this.cursor=cursor;
} 