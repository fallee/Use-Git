(function(window){
  console.log("load demo2");
  Demo2=function(paper){
    paper.install(window);
    init();
    this.render=function(){
      console.log("render");
      console.log(paper);
      console.log(Path);
      
    };
  };
  var init=function(){
    var rect=new Path.Rectangle({point:[10,10],
        size:[50,50],
        fillcolor:"#bbb",
        strokeColor: 'black'});
   
    var layer01=new Layer({strokeColor:"#bbb",strokeWidth:2});
    layer01.addChild(new Path.Line({from:[30,30],to:[50,50],strokeColor:"#ccc",strokeWidth:2}));
    layer01.addChild(new Path.Line({from:[35,35],to:[30,50],strokeColor:"#0bb",strokeWidth:2}));

    var mouseMoveHander=function(event){
      var p=new Path.Circle({
        center:event.point,
        radius:2,
        fillColor:"red",
        strokeColor:"#ccc",
        strokeWidth:1
      });
      p.removeOnMove();
        layer01.visible = true;
      if(rect.contains(event.point)){
        //console.log(rect.hitTest(event.point));
        p.fillColor="green";
        layer01.visible = false;
      }
      
//      console.log(view);
      //markPoint.position=event.point;
      //showMarkLine(event,"red");
    }
    //tool.onMouseMove=mouseMoveHander;
//    tool.onMouseMove=mouseMoveHander;
    tool.attach("mousemove",mouseMoveHander);
  }
  Demo2.prototype.test=function(){
    console.log("test");
    console.log(Path);
    console.log(paper);
    console.log(view._context);
  }
  var test2=function(){
    console.log("test2");
  }
  _.extend(Demo2.prototype,{test2:test2});
  window.Demo2=Demo2;
})(window);