(function(window){
  console.log("load Demo3Chart");
  var config={
    scaleLineWidth:2,
    scaleLineColor:"red",
  };
  var Chart=function(paper){
    paper.install(window);
  };
  function render(){
    drawScale();
  };
  // 绘制刻度
  function drawScale(){
    var view=view;
    var Path=Path;

    var widestXLabel=1,
      xAxisPosY=1,
      xAxisLength=10,
      width=view.bounds.width;

    //X axis line
    var scale=new Path({
      strokeWidth:config.scaleLineWidth,
      strokeColor:config.scaleLineColor,
      });
    scale.moveTo(width-widestXLabel/2+5,xAxisPosY);
    scale.lineTo(width-(widestXLabel/2)-xAxisLength-5,xAxisPosY);
    /*
    
    if (rotateLabels > 0){
      ctx.save();
      ctx.textAlign = "right";
    }
    else{
      ctx.textAlign = "center";
    }
    ctx.fillStyle = config.scaleFontColor;
    for (var i=0; i<data.labels.length; i++){
      ctx.save();
      if (rotateLabels > 0){
        ctx.translate(yAxisPosX + i*valueHop,xAxisPosY + config.scaleFontSize);
        ctx.rotate(-(rotateLabels * (Math.PI/180)));
        ctx.fillText(data.labels[i], 0,0);
        ctx.restore();
      }
      
      else{
        ctx.fillText(data.labels[i], yAxisPosX + i*valueHop + valueHop/2,xAxisPosY + config.scaleFontSize+3);					
      }

      ctx.beginPath();
      ctx.moveTo(yAxisPosX + (i+1) * valueHop, xAxisPosY+3);
      
      //Check i isnt 0, so we dont go over the Y axis twice.
        ctx.lineWidth = config.scaleGridLineWidth;
        ctx.strokeStyle = config.scaleGridLineColor;					
        ctx.lineTo(yAxisPosX + (i+1) * valueHop, 5);
      ctx.stroke();
    }
    
    //Y axis
    ctx.lineWidth = config.scaleLineWidth;
    ctx.strokeStyle = config.scaleLineColor;
    ctx.beginPath();
    ctx.moveTo(yAxisPosX,xAxisPosY+5);
    ctx.lineTo(yAxisPosX,5);
    ctx.stroke();
    
    ctx.textAlign = "right";
    ctx.textBaseline = "middle";
    for (var j=0; j<calculatedScale.steps; j++){
      ctx.beginPath();
      ctx.moveTo(yAxisPosX-3,xAxisPosY - ((j+1) * scaleHop));
      if (config.scaleShowGridLines){
        ctx.lineWidth = config.scaleGridLineWidth;
        ctx.strokeStyle = config.scaleGridLineColor;
        ctx.lineTo(yAxisPosX + xAxisLength + 5,xAxisPosY - ((j+1) * scaleHop));					
      }
      else{
        ctx.lineTo(yAxisPosX-0.5,xAxisPosY - ((j+1) * scaleHop));
      }
      
      ctx.stroke();
      if (config.scaleShowLabels){
        ctx.fillText(calculatedScale.labels[j],yAxisPosX-8,xAxisPosY - ((j+1) * scaleHop));
      }
    }
    
      */
    
  }
  var test=function(){
    console.log("test");
    console.log(Path);
    console.log(paper);
    console.log(view._context);
  }
  _.extend(Chart.prototype,{test:test,render:render});
  window.Demo3Chart=Chart;
})(window);