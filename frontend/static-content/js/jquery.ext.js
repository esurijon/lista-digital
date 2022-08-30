(function($) {
	var add = function(obj, prop, val) {
		var result;
		if (!obj[prop]) {
			obj[prop] = val;
		} else {
			if ($.isArray(obj[prop])) {
				obj[prop].push(val);
			} else if (!(obj[prop] instanceof Object)) {
				var tmp = obj[prop];
				obj[prop] = [ tmp, val ];
			}
		}
		return obj[prop];
	};

	var form2json = function(form) {
		var obj = {};
		$(':input:not(:button)', form).each(function() {
			var o = obj;
			var props = $(this).attr('name').split('.');
			for ( var i = 0; i < props.length - 1; i++) {
				o = add(o, props[i], {});
			}
			var prop = props[props.length - 1];
			var val = $(this).val();
			add(o, prop, val);
		});
		return obj;
	};

	$.form2json = form2json;

})(jQuery);

(function($) {
	var ctx;
	var mappings = {
			arrayItemTagName : 'item',
			arrayItemIndexAtt : null,
			attributePropPattern : '^@(.+)'
	};

	var json2dom = function(obj, nodeName) {
		var node = $('<' + nodeName + '>', ctx);
		for ( var p in obj) {
			var prop = p;
			var value = obj[p];
			var isArrItem = !isNaN(parseInt(p));
			if (isArrItem) {
				if (mappings[nodeName]) {
					prop = mappings[nodeName];
				} else {
					prop = mappings.arrayItemTagName;
				}
			}
			if (typeof (value) == 'object') {
				if (isArrItem) {
					value['@index'] = p;
				}
				$(node).append(json2dom(value, prop));
			} else {
				var matches = prop.match(mappings.attributePropPattern);
				if (matches) {
					$(node).attr(matches[1], value.toString());
				} else {
					var tag = $('<' + prop + '>', ctx).text(value.toString());
					if (isArrItem && mappings.arrayItemIndexAtt) {
						tag.attr(mappings.arrayItemIndexAtt, p);
					}
					$(node).append(tag);
				}
			}
		}
		return node[0];
	};

	/*
	 * 	Maps a json objects into DOM objects
	 * 	obj: the json object
	 *  nodeName: name for the root node of the DOM object
	 *  context: Document object owner of the DOM nodes, if not specified document will be used
	 *  map: object with mapping properties
	 *  	arrayItemTagName : name for tags corresponding to an array item, default:'item',
	 *  	arrayItemIndexAtt : null,
	 *  	attributePropPattern : RegExp used to identify which json properties should be mapped as attribures, default: '^@(.+)'
	 */
	$.json2dom = function(obj, nodeName, context, map) {
		ctx = context ? context : document;
		$.extend(mappings, map);
		return json2dom(obj, nodeName);
	};

})(jQuery);

(function($) {
	/*
	 * Validates form fields against a set of rules.
	 * 
	 * form: a form or any html container in which input elements are looked for
	 * rules: an object in which each property name has the name of the input field to validate and the value is a rule object
	 * 
	 * A rule is an object with 3 properties: 
	 * 	match: a regular expression which must match the input value or function pointer
	 *  msg: Message returned if the input field doesn't match against the RE
	 *  negate: an optional property to negate de result of the match    
	 */
	$.validate = function(form, rules) {
		var msg;
		for (field in rules) {
			var rule = rules[field];
			if(typeof rule.match == 'function') {
				msg = rule.match($(':input[name=' + field + ']', form));
				if(msg){
					break;
				}
			} else if (!$(':input[name=' + field + ']', form).val().match(rule.match) ^ rule.negate) {
				msg = rule.msg;
				break;
			}
		}
		return msg;
	};

})(jQuery);

(function($) {

	/*
	 * Executes asynchronoulsy multiple ajax requests and after the last one is completed 
	 * executes a callback function passing an array on results from each request.
	 * It overrides the 'complete' propertiy in ajax request. 
	 * 
	 * request: array of objects, each one is used as parameter in $.ajax() call
	 * callback: callback function invoked after last ajax request is completed
	 *  
	 */
	$.majax = function(requests, callback) {
		var counter = requests.length;
		var results = [];
		var _majax = function (xhr, status) {
			if(typeof status != 'string') {
				for(var i = 0; i < requests.length; i++) {
					var req = requests[i];
					$.extend(req, {secuence: i, complete: _majax});
					$.ajax(req);			
				}	
			} else {
				counter--;
				results[this.secuence] = {status: xhr.status, data: xhr.responseXML?xhr.responseXML:xhr.responseText};
				if(counter <=0){
					callback(results);
				}
			}	
		};
		_majax(requests);
	}; 
	
})(jQuery);

jQuery.fn.getPath = function () {
    if (this.length != 1) throw 'Requires one element.';

    var path, node = this;
    while (node.length) {
        var realNode = node[0], name = realNode.localName;
        if (!name) break;
        name = name.toLowerCase();

        var parent = node.parent();

        var siblings = parent.children(name);
        if (siblings.length > 1) { 
            name += ':eq(' + siblings.index(realNode) + ')';
        }

        path = name + (path ? '>' + path : '');
        node = parent;
    }

    return path;
};
