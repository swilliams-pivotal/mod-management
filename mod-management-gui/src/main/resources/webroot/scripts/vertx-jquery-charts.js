(function( $ ) {
  $.fn.lineChart = function(dataset) {
    var canvas = this[0]
    var x = canvas.width;
    var y = canvas.height / 2;
    var ctx = canvas.getContext('2d');
    ctx.font = dataset.font;
    ctx.strokeStyle=dataset.color;
    ctx.fillStyle = dataset.color;
    ctx.fillText(dataset.title, 0, 10);
    var textOffset = 10 + ctx.measureText(dataset.title, 0, 10).width
    var count = dataset.series[0].data.length;
    var step = canvas.width / count;

    for (var j=0; j<dataset.series.length; j++) {
      var series = dataset.series[j];

      ctx.strokeStyle=series.color;
      ctx.fillStyle = series.color;

      ctx.beginPath();
      ctx.moveTo(0,y);
      diff = x / series.data.length;
      var a = 0;
      for (var i=0; i<series.data.length; i++) {
        a+=diff
        ctx.lineTo(a, y + step + (series.data[i]*-3));
      }
      ctx.lineWidth=series.width;
      ctx.stroke();
      ctx.closePath();

      // or use? ctx.fillRect(0,0,150,75);
      ctx.beginPath();
      ctx.arc(5 + textOffset + (20 * j),6,3,0,2*Math.PI);
      ctx.fill();
      ctx.closePath();

      ctx.font = 'bold ' + dataset.font;
      ctx.fillText(series.title, 10 + textOffset + (20 * j), 10)
      textOffset += ctx.measureText(series.title, 20, 10).width
    }
  };
  $.fn.barChart = function(data) {
    // Do your awesome plugin stuff here
  };
  $.fn.radialChart = function(dial) {
    var canvas = this[0]
    var ctx = canvas.getContext('2d');
    var x = canvas.height / 2;
    var y = canvas.width / 2;
    var r = canvas.width / 2;

    for (var i=0; i<dial.data.length; i++) {
      var data = dial.data[i]
      var width = data[1]

      var max = (1.5 + (2*(data[3]/100))) * Math.PI;
      ctx.beginPath();
      ctx.moveTo(x,y);
      ctx.lineTo(x,0);
      ctx.arc(x,y,r,1.5*Math.PI,max);
      ctx.lineTo(x,y);
      ctx.globalAlpha = 0.3;
      ctx.fillStyle=data[0];
      ctx.fill();
      ctx.closePath();

      var rad = (1.5 + (2*(data[2]/100))) * Math.PI;
      ctx.beginPath();
      ctx.moveTo(x,y);
      ctx.lineTo(x,0);
      ctx.arc(x,y,r,1.5*Math.PI,rad);
      ctx.lineTo(x,y);
      ctx.globalAlpha = 1;
      ctx.fillStyle=data[0];
      ctx.fill();
      ctx.closePath();

      ctx.beginPath();
      ctx.moveTo(x,y);
      ctx.lineTo(x,0);
      ctx.arc(x,y,r-width,1.5,1.5+(2*Math.PI));
      ctx.lineTo(x,y);
      ctx.fillStyle='#fff';
      ctx.fill();
      ctx.closePath();

      r = r - (dial.spacing + width)
    }
  };
})( jQuery );