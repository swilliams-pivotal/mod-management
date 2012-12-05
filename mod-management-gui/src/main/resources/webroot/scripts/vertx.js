var pools = {
  id: 'main1',
  title: 'Thread pools:',
  font: '12px sans-serif',
  color: '#999',
  series: [
    {
      title: 'Event',
      width: 2,
      color: '#6f6',
      data: [4,2,-1,-3,-1,1,2,4,5,2,3,4,1,0,1,-1,1,-3,1,3,2,1,2,1,3,4,3,4,2,0,-1,2,3,1,3,4,2,-1,-3,-1,1,2,4,5,2,3,4,1,0,1,-1,1,-3,13,2,1,2,1,3,4,3,4,2,0,-1,2,3,1,3,4,2,-1,-3,-1,1,2,4,5,2,3,4,1,0,1,-1,1,-3,13,2,1,2,1,3,4,3,4,2,0,-1,2,3,1,3]
    },
    {
      title: 'Worker',
      width: 2,
      color: '#9ef',
      data: [3,2,1,2,1,3,4,3,4,2,0,-1,2,3,1,3,4,2,-1,-3,-1,1,2,4,5,2,3,4,1,0,1,-1,1,-3,13,2,1,2,1,3,4,3,4,2,0,-1,2,3,1,3,4,2,-1,-3,-1,1,2,4,5,2,3,4,1,0,1,-1,1,-3,13,2,1,2,1,3,4,3,4,2,0,-1,2,3,1,3,4,2,-1,-3,-1,1,2,4,5,2,3,4,1,0,1,-1,1,-3,1]
    }
  ]
}

var memory1 = {
  id: 'histo1',
  title: 'Memory:',
  font: '12px sans-serif',
  color: '#999',
  series: [
    {
      title: 'Heap',
      color: '#6f6',
      data: [0,3,5,7,11,13,0,3,5,7,11,13,17,19,23,29,31]
    },
    {
      title: 'Non-heap',
      color: '#9ef',
      data: [0,3,5,7,11,13,17,19,23,29,31,37,41,43,51,53,57]
    }
  ]
}

var memory2 = {
  id: 'histo2',
  title: 'Memory:',
  font: '12px sans-serif',
  color: '#999',
  series: [
    {
      title: 'Data1',
      color: '#6f6',
      data: [0,3,5,7,11,13,0,3,5,7,11,13,17,19,23,29,31]
    },
    {
      title: 'Data2',
      color: '#9ef',
      data: [0,3,5,7,11,13,17,19,23,29,31,37,41,43,51,53,57]
    },
    {
      title: 'Data3',
      color: '#9ef',
      data: [0,3,5,7,11,13,17,19,23,29,31,37,41,43,51,53,57]
    }
  ]
}

var dial1 = {
  spacing: 5,
  data: [['#fc6',22,67,91],['#fc6',10,37,51]]
//  data: [['#fc6',22,67,91],['#fc6',10,37,51],['#ddd',3,100,100]]
}

var dial2 = {
  spacing: 5,
  data: [['#9ef',22,37,51],['#9ef',10,57,73]]
//  data: [['#9ef',22,37,51],['#9ef',10,57,73],['#ddd',3,100,100]]
}

var nodestatus = {
  id: 'status1',
  title: 'Status:',
  font: '12px sans-serif',
  color: '#ccc',
  series: [
    {
      title: 'Data1',
      color: '#9ef',
      data: [0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1]
    },
    {
      title: 'Data2',
      color: '#9ef',
      data: [0,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1]
    },
    {
      title: 'Data3',
      color: '#9ef',
      data: [1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1]
    },
    {
      title: 'Data4',
      color: '#9ef',
      data: [1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1]
    },
    {
      title: 'Data5',
      color: '#9ef',
      data: [1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,0,0,1,1,1,1,1,1,1]
    }
  ]
}

;(function ($, window, undefined) {
  'use strict';


  // chart(pools)


  histogram(memory1)

  sparklines(memory2)

  // dial('dial2', 41, 5)

  boxgrid(nodestatus)

})(jQuery, this);

$(document).ready(function() {
  $('#main1').lineChart(pools);
  $('#dial1').radialChart(dial1)
  $('#dial2').radialChart(dial2)
});



// $('#dial1').dial(63, 5)

function boxgrid(dataset) {
  var canvas = document.getElementById(dataset.id)
  var ctx = canvas.getContext('2d');
  var w = canvas.width / dataset.series[0].data.length;
  var h = (canvas.height-10) / dataset.series.length;
  for (var j=0; j<dataset.series.length; j++) {
    var series = dataset.series[j];
    for (var i=0; i<series.data.length; i++) {
      var color = (series.data[i]==1) ? series.color : dataset.color;
      ctx.fillStyle=color;
      ctx.fillRect((i*w),5,w-2,h-2);
    }
    ctx.translate(0,h,0,0);
  }
}


function equalizer(dataset) {
  var canvas = document.getElementById(dataset.id)
  var ctx = canvas.getContext('2d');
}

function sparklines(dataset) {
  var canvas = document.getElementById(dataset.id)
  var ctx = canvas.getContext('2d');
  var boxh = (canvas.height-10) / dataset.series.length
  var inc = 10 / dataset.series.length;
  for (var j=0; j<dataset.series.length; j++) {
    var series = dataset.series[j];
    var x = 0;
    var y = boxh;
    ctx.beginPath();
    ctx.fillStyle='#ccc';
    ctx.fillRect(0,0,canvas.width,boxh);
    ctx.closePath();
    ctx.translate(0,boxh+inc,0,0)
  }
}

function histogram(dataset) {
  var canvas = document.getElementById(dataset.id)
  var ctx = canvas.getContext('2d');
  ctx.font = dataset.font;
  ctx.strokeStyle=dataset.color;
  ctx.fillStyle = dataset.color;
  ctx.fillText(dataset.title, 0, 10);
  var textOffset = 10 + ctx.measureText(dataset.title, 0, 10).width

  var h = canvas.height;
  var block = canvas.width / dataset.series[0].data.length;
  var unit = block / dataset.series.length;
  var w = unit - 1;
  for (var j=0; j<dataset.series.length; j++) {
    var series = dataset.series[j];
    for (var i=0; i<series.data.length; i++) {
      ctx.fillStyle=series.color;
      var d = (series.data[i]/100)*(canvas.height*0.9);
      ctx.fillRect((i*block)+(j*w),h-d,w,d);
    }
    // or use? ctx.fillRect(0,0,150,75);
    ctx.beginPath();
    ctx.arc(5 + textOffset + (20 * j),6,3,0,2*Math.PI);
    ctx.fill();
    ctx.closePath();
    ctx.font = 'bold ' + dataset.font;
    ctx.fillText(series.title, 10 + textOffset + (20 * j), 10)
    textOffset += ctx.measureText(series.title, 20, 10).width
  }
}

function chart(dataset) {
  var canvas = document.getElementById(dataset.id)
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
}
