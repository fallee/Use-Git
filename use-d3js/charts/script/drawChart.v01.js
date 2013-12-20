
//function drawBarChartFromDiv(div){
//  var data=d3.tsv.parse(d3.select(div).html());
//    console.log("data: "+JSON.stringify(data));
//  data=data.map(function(d){
//      return {name:d.ATTRIBUTE_NAME,value:+d.POST_COUNT};
//  });
//  drawBarChart(data,"#chart");
//}
//function drawTopic(){
//  d3.tsv("http://192.168.1.58/42v/chart/analyse_topic_media.php",function(data){
//    data=data.map(function(d){
//      var d2={name:d.MEDIA_NAME,value:+d.POST_COUNT};
//      return d2;
//      });
//    console.log("data: "+JSON.stringify(data));
//    drawBarChart(data,"#chart");
//  });
//}
function drawBarChart(data,continer,config){
  var _config={
    margin:{//为坐标轴留白
      top:20,right:20,bottom:30,left:40
    }
    ,width:490
    ,height:300
    ,barColor:"#1f77b4"
    ,barHoldColor:"#ff7f0e"
    ,tickCount:5
    ,continer:"#chart"
    ,debug:true
  };
  
	if(config){
    for (var key in config){
      _config[key]=config[key];
    }
  }
	if(!_config.onPointClick){
		_config.onPointClick=function(data){_d("on Point Click",data,"postion:"+d3.event.pageX+","+d3.event.pageY,d3.mouse(this));};
	}
  var _d=(function(){
    var _d=function(msg,object){};
    if(_config.debug){
      if(typeof(_config.debug)=="function"){
        _d=_config.debug;
      }else{
        _d=function(msg,object){
          if(arguments.length>1){
            var _msg=msg;
            for(var i=1;i<arguments.length;i++){
              _msg+=" "+JSON.stringify(arguments[i]);
            }
            console.log(_msg);
          }else{
            console.log(JSON.stringify(msg));
          }
        }
      }
    }
    return _d;
  })();

  var margin = _config.margin,
        width = _config.width - margin.left - margin.right,
        height = _config.height - margin.top - margin.bottom,
        barColor=_config.barColor,barHoldColor=_config.barHoldColor,
        tickCount=_config.tickCount,
        chartDiv=continer?continer:_config.continer
        onPointClick=_config.onPointClick;
        ;

  console.log("data: "+JSON.stringify(data));
  drawChart(data);
  function drawChart(data){
	console.log("BEGIN drawBarChart");
	// clear
	d3.select(chartDiv).select("svg").remove();
	var svg=d3.select(chartDiv)
		  .append("svg")
		  .attr("width", width + margin.left + margin.right)
		  .attr("height", height + margin.top + margin.bottom)
		  .append("g")
			.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	var x=d3.scale.ordinal()
			.rangeRoundBands([0,width],.1,.1);
	var y=d3.scale.linear()
			.range([height,0]);

	x.domain(data.map(function(d){return d.name;}));
	// 计算y轴数值范围
	var yrange=(function(data){
	  var min=d3.min(data,function(d){return d.value;});
	  var max=d3.max(data,function(d){return d.value;});
	  var padding=(max-min)/10;
	  var m1=min>=0?
			(2*min-max>0?
			  (min-padding>0?min-padding:min-padding)
			  :0)
			:min-padding;
	  var m2=max+(max-min)/10;
	  return [m1,m2];
	})(data);
	console.log("YRANGE "+yrange);
	y.domain(yrange)
	  .nice(true);// 优化边界值
	console.log(y.ticks(tickCount));
//      刻度线
	svg.append("g")
		.selectAll(".yline").data(y.ticks(tickCount))
		.enter().append("g")
		  .attr("class","bar")
		  .attr("transform",function(d){return "translate(0,"+y(d)+")";})
		  .append("line")
			.attr("x2",width)
			.attr("y2",0)
			.style("stroke","#777")
			.style("stroke-width",1)
			.style("stroke-dasharray",function(d,i){ return i==0?"0":"2,2";});

	
//      轴线
	var xAxis = d3.svg.axis() // x轴刻度线
		.scale(x)
		.orient("bottom");

	var yAxis = d3.svg.axis() // y轴刻度线
		.scale(y)
		.orient("left")
		.ticks(tickCount); // 
	svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + (height) + ")")
		.call(xAxis);

	svg.append("g")
		.attr("class", "y axis")
		.call(yAxis)


//      数据条
	svg.append("g")
		.selectAll(".bar").data(data)
		.enter().append("rect")
		  .attr("class","bar")
		  .attr("x",function(d){return x(d.name);})
		  .attr("width",x.rangeBand())
		  .attr("y",function(d){return y(d.value);})
		  .attr("height",function(d){return height-y(d.value);})
		  //.style("fill",barColor)
		  //.on("mouseover",function(d){d.mouseover=true;d.fill=d3.select(this).style("fill");d3.select(this).style("fill",barHoldColor)})
		  //.on("mouseout",function(d){d.mouseover=false;d3.select(this).style("fill",d.fill);})
		  .on("click",onPointClick);
	console.log("END drawBarChart");
  }
  function getTickValues(tickValues,tickCount){
    if(tickValues.length>tickCount){
      var count=tickValues.length
        ,step=Math.ceil(tickValues.length/(tickCount-1))
        ,step2=step
        ,lastIndex=0;
      var vs=tickValues.slice(0,1).concat( tickValues.filter(function(d,i){if(i>=step2){step2+=step;lastIndex=i;return true;}}));
      if(lastIndex!=count-1&&count-lastIndex<step*2/3){
        vs.pop();
      }
      if(vs.length<tickCount){
        vs.push(tickValues[tickValues.length-1]);
      }
    }
    //console.log(tickValues.length+" "+vs+" "+tickValues[tickValues.length-1]);
    return vs;
  }
};


function drawLineChart(data,continer,config){
  var _config={
    margin:{//为坐标轴留白
      top:20,right:20,bottom:30,left:40
    }
    ,width:490
    ,height:300
    ,barColor:"#1f77b4"
    ,barHoldColor:"#ff7f0e"
    ,tickCount:5
    ,continer:"#chart"
    ,debug:true
  };
  
	if(config){
    for (var key in config){
      _config[key]=config[key];
    }
  }
	if(!_config.onPointClick){
		_config.onPointClick=function(data){_d("on Point Click",data,"postion:"+d3.event.pageX+","+d3.event.pageY,d3.mouse(this));};
	}
  var _d=(function(){
    var _d=function(msg,object){};
    if(_config.debug){
      if(typeof(_config.debug)=="function"){
        _d=_config.debug;
      }else{
        _d=function(msg,object){
          if(arguments.length>1){
            var _msg=msg;
            for(var i=1;i<arguments.length;i++){
              _msg+=" "+JSON.stringify(arguments[i]);
            }
            console.log(_msg);
          }else{
            console.log(JSON.stringify(msg));
          }
        }
      }
    }
    return _d;
  })();

  var margin = _config.margin,
        width = _config.width - margin.left - margin.right,
        height = _config.height - margin.top - margin.bottom,
        barColor=_config.barColor,barHoldColor=_config.barHoldColor,
        tickCount=_config.tickCount,
        chartDiv=continer?continer:_config.continer
        onPointClick=_config.onPointClick;
        ;
        
  _d("data: ",data);
  return drawChart(data);
  function drawChart(data){
    console.log("BEGIN drawLineChart");
    // clear
    d3.select(chartDiv).select("svg").remove();
    var svg=d3.select(chartDiv)
          .append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
		  .attr("id","svgdiscuss")
          .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var x=d3.scale.ordinal()
            .rangePoints([0,width],0);
    var y=d3.scale.linear()
            .range([height,0]);

    x.domain(data.map(function(d){return d.name;}));
    // 计算y轴数值范围
    var yrange=(function(data){
      var min=d3.min(data,function(d){return d.value;});
      var max=d3.max(data,function(d){return d.value;});
      var padding=(max-min)/10;
      var m1=min>=0?
            (2*min-max>0?
              (min-padding>0?min-padding:min-padding)
              :0)
            :min-padding;
      var m2=max+(max-min)/10;
      return [m1,m2];
    })(data);
    _d("YRANGE ",yrange);
    y.domain(yrange)
      .nice(true);// 优化边界值
    console.log(y.ticks(tickCount));
//      刻度线
    svg.append("g")
          .attr("class","scale continer")
        .selectAll(".yline").data(y.ticks(tickCount))
        .enter().append("g")
          .attr("class","bar")
          .attr("transform",function(d){return "translate(0,"+y(d)+")";})
          .append("line")
            .attr("x2",width)
            .attr("y2",0)
            .style("stroke","#777")
            .style("stroke-width",1)
            .style("stroke-dasharray",function(d,i){ return i==0?"0":"2,2";});

    
//      轴线
    var xAxis = d3.svg.axis() // x轴刻度线
        .scale(x)
        .orient("bottom")
        .tickValues(getTickValues(data.map(function(d){return d.name;}),tickCount));

    var yAxis = d3.svg.axis() // y轴刻度线
        .scale(y)
        .orient("left")
        .ticks(tickCount);; // 
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + (height) + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)

    var line=d3.svg.line()
          .x(function(d){return x(d.name);})
          .y(function(d){return y(d.value);});

//      数据条
    svg.append("g")
          .attr("class","line continer")
        .append("path")
          .datum(data)
          .attr("class","line")
          .attr("d",line);
//        数据点
    svg.append("g")
          .attr("class","circle continer")
        .selectAll(".circle")
          .data(data)
          .enter().append("circle")
          .attr("class","circle")
          .attr("r",2)
          .attr("transform",function(d){return "translate("+x(d.name)+","+y(d.value)+")";})
          //.on("mouseover",function(d){d.mouseover=true;d.fill=d3.select(this).style("fill");d3.select(this).style("fill",barHoldColor)})
          //.on("mouseout",function(d){d.mouseover=false;d3.select(this).style("fill",d.fill);})
          .on("click",onPointClick);
    svg.selectAll(".x.axis .tick text").attr("style","");
	
		  
          //.on("click",function(d){
          //     alert("data:"+JSON.stringify(d)
          //         +";event:"+d3.event.pageX+","+d3.event.pageY+";"
          //         +";mouse:"+d3.mouse(this));
			//	   alert(d.name);
          //   });
    console.log("END drawLineChart");
  }
  function getTickValues(tickValues,tickCount){
    if(tickValues.length>tickCount){
      var count=tickValues.length
        ,step=Math.ceil(tickValues.length/(tickCount-1))
        ,step2=step
        ,lastIndex=0;
      var vs=tickValues.slice(0,1).concat( tickValues.filter(function(d,i){if(i>=step2){step2+=step;lastIndex=i;return true;}}));
      if(lastIndex!=count-1&&count-lastIndex<step*2/3){
        vs.pop();
      }
      if(vs.length<tickCount){
        vs.push(tickValues[tickValues.length-1]);
      }
    }
    //console.log(tickValues.length+" "+vs+" "+tickValues[tickValues.length-1]);
    return vs;
  }
  
};


function drawPieChart(data,continer,config){
  var _config={
    margin:{//为坐标轴留白
      top:20,right:20,bottom:30,left:40
    }
    ,width:490
    ,height:300
    ,barColor:"#1f77b4"
    ,barHoldColor:"#ff7f0e"
    ,tickCount:5
    ,continer:"#chart"
    ,debug:true
  };
  
	if(config){
    for (var key in config){
      _config[key]=config[key];
    }
  }
  var _d=(function(){
    var _d=function(msg,object){};
    if(_config.debug){
      if(typeof(_config.debug)=="function"){
        _d=_config.debug;
      }else{
        _d=function(msg,object){
          if(arguments.length>1){
            var _msg=msg;
            for(var i=1;i<arguments.length;i++){
              _msg+=" "+JSON.stringify(arguments[i]);
            }
            console.log(_msg);
          }else{
            console.log(JSON.stringify(msg));
          }
        }
      }
    }
    return _d;
  })();
	if(!_config.onPointClick){
		_config.onPointClick=function(data){_d("on Point Click",data,"postion:"+d3.event.pageX+","+d3.event.pageY,d3.mouse(this));};
	}
  var margin = _config.margin,
        width = _config.width - margin.left - margin.right,
        height = _config.height - margin.top - margin.bottom,
        barColor=_config.barColor,barHoldColor=_config.barHoldColor,
        tickCount=_config.tickCount,
        chartDiv=continer?continer:_config.continer,
        onPointClick=_config.onPointClick,
        radius = Math.min(width, height) / 2
        ;
    
        
  console.log("data: "+JSON.stringify(data));
  drawChart(data);
  function drawChart(data){
    _d("BEGIN drawPieChart");

    
    var color = d3.scale.ordinal()
        .range("#1f77b4 #aec7e8 #ff7f0e #ffbb78 #2ca02c #98df8a #d62728 #ff9896 #9467bd #c5b0d5 #8c564b #c49c94 #e377c2 #f7b6d2 #7f7f7f #c7c7c7 #bcbd22 #dbdb8d #17becf #9edae5".split(" "));

    var arc = d3.svg.arc()
        .outerRadius(radius - 10)
        .innerRadius(0);

    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.value; });

    // clear
    d3.select(chartDiv).select("svg").remove();
    var svg=d3.select(chartDiv)
          .append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

    color.domain(data.map(function(d){return d.name;}));
    console.log(data.map(function(d){return d.name+" "+color(d.name);}));

    var g = svg.selectAll(".arc")
        .data(pie(data))
      .enter().append("g")
        .attr("class", "arc");
    g.append("path")
          .attr("d", arc)
          .attr("class",function(d,i){return "color"+(i+1);})
          //.style("fill", function(d) { return color(d.data.name); })
          .on("click",onPointClick);

    g.append("text")
        .attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .text(function(d) { return d.data.name; });
    
    console.log("END drawPieChart");
  }
  
};