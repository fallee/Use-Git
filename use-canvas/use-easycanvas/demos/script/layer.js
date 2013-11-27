(function(window){
  var LayerCanvas={};
  var Viewport={};

  /**
   * 绑定 DOM 事件
   * DOM Event Helper
   * @private
   * @description dom event register from Dean Edwards
   * @link http://dean.edwards.name/weblog/2005/10/add-event/
   * @property {fn} add
   * @property {fn} del 
   */
  var DOMEvent = {
    _uid: 1,

    add: function(elm, type, fn) {
      if (!fn._uid) fn._uid = this._uid++;

      if (!elm.events) elm.events = {};

      var handlers = elm.events[type];

      if (!handlers) {
        handlers = elm.events[type] = {};
        
        if (elm["on" + type]) {
          handlers[0] = elm["on" + type];
        }
      }
      
      handlers[fn._uid] = fn;
      
      elm["on"+type] = this.handle;
    },

    handle: function(event) {
      var returnValue = true;

      event = event || DOMEvent.fixIE(window.event);
      
      var handlers = this.events[event.type];
      
      for (var i in handlers) {
        this._handler = handlers[i];
        if (this._handler(event) === false) {
          returnValue = false;
        }
      }
      
      return returnValue;
    },

    fixIE: function(event) {
      event.stopPropagation = function() {
        this.cancelBubble = true;
      };
      
      event.preventDefault = function() {
        this.returnValue = false;
      };
      
      return event;
    },

    del: function(elm, type, fn) { 
      if (elm.events && elm.events[type]) {
        delete elm.events[type][fn._uid];
      }
    }
  };


  /**
   * @private
   * animation Easing functions
   * from jquery animation & jquery.easing plugin
   * @link https://github.com/danro/jquery-easing/blob/master/jquery.easing.js
   * @params (x, t, b, c, d)  refer to (percent, duration*percent, 0, 1, duration)
   */
  var Easing = {
    linear: function(x) {
      return x;
    },
    swing: function(x) {
      return 0.5 - Math.cos( x*Math.PI ) / 2;
    },
    easeInQuad: function (x, t, b, c, d) {
      return c*(t/=d)*t + b;
    },
    easeOutQuad: function (x, t, b, c, d) {
      return -c *(t/=d)*(t-2) + b;
    },
    easeInOutQuad: function (x, t, b, c, d) {
      if ((t/=d/2) < 1) return c/2*t*t + b;
      return -c/2 * ((--t)*(t-2) - 1) + b;
    },
    easeInCubic: function (x, t, b, c, d) {
      return c*(t/=d)*t*t + b;
    },
    easeOutCubic: function (x, t, b, c, d) {
      return c*((t=t/d-1)*t*t + 1) + b;
    },
    easeInOutCubic: function (x, t, b, c, d) {
      if ((t/=d/2) < 1) return c/2*t*t*t + b;
      return c/2*((t-=2)*t*t + 2) + b;
    },
    easeInQuart: function (x, t, b, c, d) {
      return c*(t/=d)*t*t*t + b;
    },
    easeOutQuart: function (x, t, b, c, d) {
      return -c * ((t=t/d-1)*t*t*t - 1) + b;
    },
    easeInOutQuart: function (x, t, b, c, d) {
      if ((t/=d/2) < 1) return c/2*t*t*t*t + b;
      return -c/2 * ((t-=2)*t*t*t - 2) + b;
    },
    easeInQuint: function (x, t, b, c, d) {
      return c*(t/=d)*t*t*t*t + b;
    },
    easeOutQuint: function (x, t, b, c, d) {
      return c*((t=t/d-1)*t*t*t*t + 1) + b;
    },
    easeInOutQuint: function (x, t, b, c, d) {
      if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
      return c/2*((t-=2)*t*t*t*t + 2) + b;
    },
    easeInSine: function (x, t, b, c, d) {
      return -c * Math.cos(t/d * (Math.PI/2)) + c + b;
    },
    easeOutSine: function (x, t, b, c, d) {
      return c * Math.sin(t/d * (Math.PI/2)) + b;
    },
    easeInOutSine: function (x, t, b, c, d) {
      return -c/2 * (Math.cos(Math.PI*t/d) - 1) + b;
    },
    easeInExpo: function (x, t, b, c, d) {
      return (t==0) ? b : c * Math.pow(2, 10 * (t/d - 1)) + b;
    },
    easeOutExpo: function (x, t, b, c, d) {
      return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
    },
    easeInOutExpo: function (x, t, b, c, d) {
      if (t==0) return b;
      if (t==d) return b+c;
      if ((t/=d/2) < 1) return c/2 * Math.pow(2, 10 * (t - 1)) + b;
      return c/2 * (-Math.pow(2, -10 * --t) + 2) + b;
    },
    easeInCirc: function (x, t, b, c, d) {
      return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
    },
    easeOutCirc: function (x, t, b, c, d) {
      return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
    },
    easeInOutCirc: function (x, t, b, c, d) {
      if ((t/=d/2) < 1) return -c/2 * (Math.sqrt(1 - t*t) - 1) + b;
      return c/2 * (Math.sqrt(1 - (t-=2)*t) + 1) + b;
    },
    easeInElastic: function (x, t, b, c, d) {
      var s=1.70158;var p=0;var a=c;
      if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
      if (a < Math.abs(c)) { a=c; var s=p/4; }
      else var s = p/(2*Math.PI) * Math.asin (c/a);
      return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
    },
    easeOutElastic: function (x, t, b, c, d) {
      var s=1.70158;var p=0;var a=c;
      if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
      if (a < Math.abs(c)) { a=c; var s=p/4; }
      else var s = p/(2*Math.PI) * Math.asin (c/a);
      return a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b;
    },
    easeInOutElastic: function (x, t, b, c, d) {
      var s=1.70158;var p=0;var a=c;
      if (t==0) return b;  if ((t/=d/2)==2) return b+c;  if (!p) p=d*(.3*1.5);
      if (a < Math.abs(c)) { a=c; var s=p/4; }
      else var s = p/(2*Math.PI) * Math.asin (c/a);
      if (t < 1) return -.5*(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
      return a*Math.pow(2,-10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )*.5 + c + b;
    },
    easeInBack: function (x, t, b, c, d, s) {
      if (s == undefined) s = 1.70158;
      return c*(t/=d)*t*((s+1)*t - s) + b;
    },
    easeOutBack: function (x, t, b, c, d, s) {
      if (s == undefined) s = 1.70158;
      return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
    },
    easeInOutBack: function (x, t, b, c, d, s) {
      if (s == undefined) s = 1.70158; 
      if ((t/=d/2) < 1) return c/2*(t*t*(((s*=(1.525))+1)*t - s)) + b;
      return c/2*((t-=2)*t*(((s*=(1.525))+1)*t + s) + 2) + b;
    },
    easeInBounce: function (x, t, b, c, d) {
      return c - this.easeOutBounce (x, d-t, 0, c, d) + b;
    },
    easeOutBounce: function (x, t, b, c, d) {
      if ((t/=d) < (1/2.75)) {
        return c*(7.5625*t*t) + b;
      } else if (t < (2/2.75)) {
        return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
      } else if (t < (2.5/2.75)) {
        return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
      } else {
        return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
      }
    },
    easeInOutBounce: function (x, t, b, c, d) {
      if (t < d/2) return this.easeInBounce (x, t*2, 0, c, d) * .5 + b;
      return this.easeOutBounce (x, t*2-d, 0, c, d) * .5 + c*.5 + b;
    }
  }

  /**
   * @class Viewport
   * @private
   * @export {object} Layer.viewport
   *
   * Layer.viewport
   * @descrpition a viewport is a drawing area, with multi layers(canvas)
   * @property {dom} elm: the viewport element, parentNode of canvases
   * @property {number} width
   * @property {number} height
   * @property {number} x: current x coordinate from left 0 to right width, when mouse on viewport
   * @property {number} y: current y coordinate from top 0 to bottom height, when mouse on viewport
   * @property {fn} resize: resize the viewport with attribute {width, height}
   */

  var Viewport = Backbone.Model.extend({
    width: 200,
    height: 200,
    domId: 'viewport',
    initialize: function(domId) {
      this.elm = document.getElementById(domId || this.domId);
      if (!this.elm) {return;}
      this.width = this.elm.offsetWidth || this.width;
      this.height = this.elm.offsetHeight || this.height;
      this.initPos();
      this.posEvents();
    },

    resize: function(o, reRenderCTXs) {
      if (!_.isObject(o)) {return;}
      if (o.width) {
        this.width = o.width;
        this.elm.style.width = o.width + 'px';
        [].forEach.call(this.elm.children, function(v, i) {
          v.width = o.width;
        });
      } 
      if (o.height) {
        this.height = o.height;
        this.elm.style.height = o.height + 'px';
        [].forEach.call(this.elm.children, function(v, i) {
          v.height = o.height;
        });
      }
      if (!reRenderCTXs) {return;}
      if (_.isArray(reRenderCTXs)) {
        reRenderCTXs.forEach(function(v) {
          v.reRender();
        });
      } else if (_.isObject(reRenderCTXs)) {
        reRenderCTXs.reRender();
      }
    },

    initPos: function() {
      function pageX(elm) {
        return elm.offsetParent ? 
        pageX(elm.offsetParent) + elm.offsetLeft : elm.offsetLeft;
      }

      function pageY(elm) {
        return elm.offsetParent ? 
        pageY(elm.offsetParent) + elm.offsetTop : elm.offsetTop; 
      }
      this.ox = pageX(this.elm);
      this.oy = pageY(this.elm);
      
      function _fixScroll() {
        This.clientX = This.ox - document.documentElement.scrollLeft;
        This.clientY = This.oy - document.documentElement.scrollTop;
      }

      //this._fixScroll = _fixScroll;
      _fixScroll();
    },

    posEvents: function() {

      var This = this;

      DOMEvent.add(window, 'scroll', _fixScroll);
      DOMEvent.add(window, 'load', _fixScroll);
      DOMEvent.add(window, 'resize', _fixScroll);
      DOMEvent.add(this.elm, 'mousemove', _setPos);
      DOMEvent.add(this.elm, 'mouseout', _clearPos);

      function _setPos(e) {
        if (This.x) {
          This.x0 = This.x;
          This.y0 = This.y;
        }

        _fixTouch(e);

        This.x = e.clientX - This.clientX;
        This.y = e.clientY - This.clientY;
      }

      function _clearPos(e) {
        delete This.x;
        delete This.y;
      }

      function _fixTouch(e) {
        // make clientX and Y compatible to touch event
        // notice: reset e.clientX,Y in desktop opera will throw exception, so should put it in if statement.
        if (e.touches) {
          e.clientX = e.touches[0].clientX;
          e.clientY = e.touches[0].clientY;
        }
      }
      function createLayer(o){
        if(_.isString(o)){
          o={id:o};
        }else if(!_.isObject(o)){
          // warring arguments is failed ...
          o={};
        }
        o.viewport=this;
        return new Layer(o);
      }
    }
  });


  /**
   * @class Layer
   * @public as EC.Layer
   * @classProperty {object} viewport: instance of Viewport
   * @classProperty {array} moves: list of instance of Move
   * @classProperty {object} animate: animation controler with start, stop, restart methods
   * 
   * @instance 
   * @description each layer as the specific drawing layer based on specific canvas
   * @property {string} customId: used to set the layer canvas id
   * @property {dom} canvas
   * @property {object} ctx
   * @property {fn} update: the specific update function for updating status of canvas animation
   */
  var Layer = Backbone.Model.extend({
	viewport:null,
	moves:[],
	animate:null,
    initialize: function(o) {
      // init viewport
      if (!this.viewport) {
        this.viewport = new Viewport();
      }
      if (!this.moves) {
        this.moves = [];
      }
      if (!this.animate) {
        _animation(this);
      }

      this.customId = customId || null;

      this._create();

      this.viewport._fixScroll();
    },

    _create: function() {
      this.canvas = document.createElement('canvas');
      this.viewport.elm.appendChild(this.canvas);
      this.canvas.width = this.viewport.width;
      this.canvas.height = this.viewport.height;
      this.canvas.style.position = 'absolute';
      this.canvas.id = this.customId ? 'ec_' + this.customId : 'ec_' + this.cid;
      this.ctx = this.canvas.getContext('2d');
      _enhanceCTX(this);
    },

    _aniClear: function() {
      this.ctx.clearRect(0, 0, this.viewport.width, this.viewport.height);
    },

    _aniRender: function() {
      var graphs = this.ctx.graphs,
          len = graphs.length,
          i = 0;
      if (len == 0) {return;}

      for (; i < len; i++) {
        graphs[i].render();
      }
    },

    _aniUpdate: function() {},

    update: function(fn) {
      this.viewport.aniLayers.push(this);
      this._aniUpdate = fn;
    }

  });

  LayerCanvas.Layer=Layer;
  LayerCanvas.Viewport=Viewport;
  window.lc=window.LayerCanvas=LayerCanvas;
})(window);
/*
viewport=new lc.Viewport("divid");
layer=viewport.createLayer("layerid");
graph=new lc.Graph("g");
//graph.render(layer);
layer.add(graph);
*/