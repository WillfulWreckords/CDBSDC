$(document).ready(function() {

	//Get unique constraint names
	var uniqueCols=[];
	var testsel = $('table th');
	for (var ti=0;ti < testsel.length; ti++){
		var tel = testsel.eq(ti);
		var txt = tel.text();
		
		if ($.inArray(txt,uniqueCols) == -1 {
			uniqueCols.push(txt);
		}
	}
	
	var x = [];
	var y = [];
	var rows = $('table tbody tr');
	for (var ti=0;ti < rows.length; ti++){
		var row = rows.eq(ti);
		var d = $(".SALES",row).eq(0).text();
		var v = $(".PAYABLE",row).eq(0).text();
		var m = v.match(/[0-9]*\.[0-9]+/g);
		v = Number(m[0]);
		if ($.inArray(d,x) == -1 {
			x.push(txt);
			y.push(v);
		}else{
			var i = $.inArray(d,x);
			y[i] += v;
		}
	}

	var options = {
		series: {
			lines: { show: true },
			points: { show: true }
		},
		legend: {
			show: true
		}
	};
	
	var plotdata = [];	

	var pd = new Object;
	pd.label = "Sales vs. Payable";
	pd.data = [];
	for(var i=0;i<x.length;i++){
		var d = [x, y];
		pd.data.push(d);
	}
	plotdata.push(pd);
	
	var idstr = "plots";
	var divvar = $("<div id='"+idstr+"' style='width:1200px;height:300px;' class='plotArea'></div>");
	$('body').prepend("<h1>"+idstr+"</h1>");
	$('body').prepend(divvar);		
	$.plot(divvar, plotdata, options);
	
	/*
	for (var ci=0; ci < uniqueCols.length; ci++){
		var cname = uniqueCols[ci];
		var pname = 'CollectionTime vs. '+cname;

		var options = {
			xaxis: { 
				mode: "time",
				timeformat: "%y/%m/%d %H:%M:%S" 
			}, 
  			series: {
  				lines: { show: true },
  				points: { show: true }
			},
  			legend: {
    				show: true
 			}
  		};
		
		var seriesname = [];
		var plotdata = [];
		var rows = $('#collectionWindowTable tbody  tr');

        var rt1 = $(rows.eq(0)).text();
        var rtl = rt1.length;
        if (rt1.length == 0 || rtl==2){
            rows.splice(0,1);
        }
        
		//sort the rows by center time
		rows.sort(function(a,b){
            var at = $(a).html();
            var bt = $(b).html();
			var as = $(".CenterTime",a).eq(0).text();
			var bs = $(".CenterTime",b).eq(0).text();
			var aymd = as.match(/[0-9]{2,4}/g);
			var bymd = bs.match(/[0-9]{2,4}/g);
			var at = Number(new Date(aymd[0],aymd[1],aymd[2],aymd[3],aymd[4],aymd[5]).getTime());
			var bt = Number(new Date(bymd[0],bymd[1],bymd[2],bymd[3],bymd[4],bymd[5]).getTime());
			return at-bt;
		});
		
		for (var ri = 1;ri< rows.length; ri++){
			var row = rows.eq(ri);
			
			//Get the time
			var ys = $(".CenterTime",row).eq(0).text();
			var ymd = ys.match(/[0-9]{2,4}/g);
			var time = Number(new Date(ymd[0],ymd[1],ymd[2],ymd[3],ymd[4],ymd[5]).getTime());
			
			//Value
			var sel = '.' + $.trim(cname); 
			var val = Number($(sel,row).eq(0).text());
			var d = [time, val];
						
			//Get the branch ID
			var bids = $('.BranchLabel',row);
			var bid = 0;
			if (bids.length > 0){
				bid = bids.eq(0).text();
			}
						
			//Asset Locale...
			var loc = $('.AssetLocale',row).eq(0).text();
						
			var sname = loc + "-" + bid;
						
			if ($.inArray(sname,seriesname)==-1){
				var pd = new Object;
				pd.label = sname;
				pd.data = [];
				seriesname.push(sname);
				plotdata.push(pd);
			}
			plotdata[$.inArray(sname,seriesname)].data.push(d);
		}
		var idstr = ""+pname;
		var divvar = $("<div id='"+idstr+"' style='width:1200px;height:300px;' class='plotArea'></div>");
		$('#collectionGraphics').append("<h1>"+idstr+"</h1>");
		$('#collectionGraphics').append(divvar);		
		$.plot(divvar, plotdata, options);
	}
	*/
});