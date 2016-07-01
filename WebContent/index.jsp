<!DOCTYPE html>
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->
<%@ page contentType="text/html;charset=UTF-8" %> 
<html>
	<head>
		<title>多天旅遊行程規劃系統</title>
		<script src="https://code.jquery.com/jquery-2.2.3.min.js"></script>
		 <script src="https://malsup.github.io/jquery.form.js"></script> 
		<script src="https://maps.googleapis.com/maps/api/js?v=3.exp"></script>
		<script src="http://spin.js.org/spin.min.js"></script>
		  <script src="./js/async.js"></script> 
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
		<link rel="stylesheet" href="./css/mycss.css"></link>
		<script>
		$(document).ready(function() {
			var opts = {
					  lines: 13 // The number of lines to draw
					, length: 28 // The length of each line
					, width: 14 // The line thickness
					, radius: 42 // The radius of the inner circle
					, scale: 1 // Scales overall size of the spinner
					, corners: 1 // Corner roundness (0..1)
					, color: '#000' // #rgb or #rrggbb or array of colors
					, opacity: 0.25 // Opacity of the lines
					, rotate: 0 // The rotation offset
					, direction: 1 // 1: clockwise, -1: counterclockwise
					, speed: 1 // Rounds per second
					, trail: 60 // Afterglow percentage
					, fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
					, zIndex: 2e9 // The z-index (defaults to 2000000000)
					, className: 'spinner' // The CSS class to assign to the spinner
					, top: '50%' // Top position relative to parent
					, left: '50%' // Left position relative to parent
					, shadow: false // Whether to render a shadow
					, hwaccel: false // Whether to use hardware acceleration
					, position: 'absolute' // Element positioning
			};
			
			var map;
			var target = document.getElementById('trigger_hadoop')
			var spinner = new Spinner(opts);
			var scheduleDiv = $('#schedule');
			
			
			$("#trigerJobBtn").click(function() {
				localStorage.clear();
				spinner.spin(target);
			 	$("#trigger_hadoop").unbind('submit').submit(function(e) {
				    var url = "./aco"; 
				    $.ajax({
			           type: "POST",
			           url: url,
			           data: $("#trigger_hadoop").serialize(), 
			           success:function(data) {
			        	   console.log("success");
			        
			        	   var parsedData = JSON.parse(data);
			        	   //console.log(parsedData.schedule);
			        	   var scheduleArr = makeScheduleStringArr(parsedData.schedule);
			        	   initialize(parsedData.map_points, scheduleArr);
			        	   //setResultText(parsedData);
			        	   spinner.stop();
			        	   $("#trigger_hadoop :input").prop("checked", false);
			           }
					});
				    e.preventDefault(); 
				}); 
			});
			
			function makeScheduleStringArr(scheduleString) {
				var resultArr = [];
				scheduleString.forEach(function(perDay, index) {
					var processedArr = perDay.slice(6).split(" >> ");
					resultArr.push(processedArr);
				});
				//console.log(resultArr);
				return resultArr;
			}
			
			
			//set schedule, path_length and weight
			function setResultText(schedule) {
				
				
				$('#schedule').children().remove();
				schedule.forEach(function(perDay, index) {
					$('#schedule').append("<p><span id='"+index+"' class='label label-warning showmap'>Day "+(index+1)+" map</span> " + perDay + "</p>");
				});
				
				$('.showmap').click(function() {
					var myID = $(this).attr("id");
					//alert(myID);
					$('#resultNumber').html("執行結果 Day "+ (parseInt(myID)+1));
					drawGoogleMap(JSON.parse(localStorage['day'+myID])[0], JSON.parse(localStorage['day'+myID])[1], JSON.parse(localStorage['day'+myID])[2], JSON.parse(localStorage['day'+myID])[3], JSON.parse(localStorage['day'+myID])[4]);
				});
				console.log("setResultText end");
			}
			
			
			function sortByGoogle(googleRoute, rawRoute, firstName, lastName) {
				//console.log(googleRoute, rawRoute, firstName, lastName);
				var newRouteTmp = [];
				googleRoute.forEach(function(iterator, index) {
					newRouteTmp.push(rawRoute[iterator]);
				});
				
				newRouteTmp.unshift(firstName);
				newRouteTmp.push(lastName);
				//console.log(newRouteTmp);
				
				return newRouteTmp;
			}
			
			//點選第一天，第二天或第三天，地圖會重新畫該天的行程路線，另外要把一二三天的map_points存進localstorage裡面，然後
			//需要某天的路線時就帶該天的map_point進去即可。
			//initial
		    function initialize(map_points, schedule) {
				console.log('initialize----------');
			
				map_points.forEach(function(iter, index) {
					iter.forEach(function(iter_in, index_in) {
						iter_in.push(schedule[index][index_in]);
					});
				});
			
		    	 async.eachSeries(map_points, function(item, cb){
		    		 var asyncIndex = map_points.indexOf(item)
		    		 console.log("--------- Use async.js ------- here is round " + asyncIndex);		    		
		    		
		    		var arrPoint = [];
		    		var pointNameArr = [];
		    	    
		    		item.forEach(function(iterator, index) {
		    			arrPoint.push(new google.maps.LatLng(item[index][0], item[index][1]));
		    			pointNameArr.push(item[index][2]);
		    		});
		    	    
		            //set route request 
		            var waypts = []; //途中景點
		            var wayNameMap = []; //途中景點名稱
		            
		            for (var j = 1; j < arrPoint.length-1; j++) {
		                    waypts.push({location: arrPoint[j],stopover: true});
		                    wayNameMap.push(pointNameArr[j]);
		            }
		            		            
		            var start = arrPoint[0];
		            var end = arrPoint[arrPoint.length-1];
		         
		            var request = {
		                    origin: start,
		                    destination: end,
		                    waypoints: waypts,
		                    optimizeWaypoints: true,
		                    travelMode: google.maps.DirectionsTravelMode.DRIVING
		            };
		            // route callback
		            var directionsService = new google.maps.DirectionsService();
		            directionsService.route(request, function(response, status) {		        
	                    if (status == google.maps.DirectionsStatus.OK) {
	                    	console.log('status == google.maps.DirectionsStatus.OK');
	                    	var path = response.routes[0].overview_path;
	                    	var rightWayStringArr = sortByGoogle(response.routes[0].waypoint_order, wayNameMap, pointNameArr[0], pointNameArr[pointNameArr.length-1]);
			            	console.log(rightWayStringArr);	    
	                        					    
					    	var forStorage = [path, rightWayStringArr, start, end, waypts];
					    	localStorage.setItem("day" + asyncIndex, JSON.stringify(forStorage));
					    	
					    	cb(null);
	                    }else{
	                    	console.log("no callback");
	                    }	                    
		            }); //--- end of directionsService.route
		    	},function(err){
	    	    // All tasks are done now
	    	    console.log("all async iterator are done");
	    	    if(!err) {
	    	    	var showStringArr = [];
	    	    	for(var i = 0; i < localStorage.length; i++) {
	    	    		var parsedArr = JSON.parse(localStorage['day'+i]);
	    	    		var pointString = parsedArr[1];
	    	    		var showString = pointString.join(" >> ");
	    	    		//console.log(showString);
	    	    		showStringArr.push(showString);
	    	    	}
	    	    	setResultText(showStringArr);
					drawGoogleMap(JSON.parse(localStorage['day0'])[0], JSON.parse(localStorage['day0'])[1], JSON.parse(localStorage['day0'])[2], JSON.parse(localStorage['day0'])[3], JSON.parse(localStorage['day0'])[4]);
					$('#resultNumber').html("執行結果 Day 1");
	    	    	console.log("all done");
	    	    } else {
	    	    	console.log(err);}
	    	 	 }); //-- end of async foreach
	    	  
			}//--- end of initialize
			
			function drawGoogleMap(path, rightWayStringArr, start, end, waypts) {
				console.log(path, rightWayStringArr, start, end, waypts);
					var rendererOptions = {
						    suppressMarkers: true
						};
					var startPoint = new google.maps.LatLng(24.032340, 121.602128);
					 map = new google.maps.Map(document.getElementById('map-canvas'), {
					    zoom: 9,
					    mapTypeId: google.maps.MapTypeId.ROADMAP,
					    center: startPoint
					});
					//var color = ['#000000', '#ff0000', '#0000ff'];



					var poly = new google.maps.Polyline({
					    strokeColor: '#000000',
					    strokeOpacity: 1.0,
					    strokeWeight: 3
					});
					
					//var bounds = new google.maps.LatLngBounds();

					$(path).each(function(index, item) {
					    //console.log(item);
					    var formatItem = new google.maps.LatLng(item)
					    poly.getPath().push(formatItem);
					});

					poly.setMap(map);
					//---------------marker color setting------------------
					var pinColor = "f0f700";
					var pinColorEnd = "0910f2";
					var pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColor,
					    new google.maps.Size(21, 34),
					    new google.maps.Point(0, 0),
					    new google.maps.Point(10, 34));

					var pinImageEnd = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColorEnd,
					    new google.maps.Size(21, 34),
					    new google.maps.Point(0, 0),
					    new google.maps.Point(10, 34));
					//---------------marker color setting end------------------

					var marker = new google.maps.Marker({
					    position: start,
					    title: "Start",
					    icon: pinImage,
					    map: map
					});
					var markerEnd = new google.maps.Marker({
					    position: end,
					    title: "End",
					    icon: pinImageEnd,
					    map: map
					});

					// Add a new marker at the new plotted point on the polyline.
					$(waypts).each(function(index, item) {

					    var marker = new google.maps.Marker({
					        position: item.location,
					        map: map
					    });
					});
					
					
				
			}//---------drawGoogleMap End---------------
			
			//google.maps.event.addDomListener(window, 'load', initialize);
		});
    	</script>
		
	</head>
	
	<body>
	<!-- 
	    <div class="row">
	      <div class="col-md-12" style="padding-left:35px">
	      	<h1>
	      	<span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> 多天旅遊行程規劃系統</h1>
	      	<div class="lead" >Enjoy your trip!</div>
	      </div>
	      <div class="col-md-6"></div>
	
	    </div>
	    -->
	    	<div calss="row">
			 <div class="col-md-12" style="padding-left:35px">
		      	<h1>
		      	<span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> 多天旅遊行程規劃系統</h1>
		      	<div class="lead" >Enjoy your trip!</div>
		      </div>
			<div class="col-md-6">
				<form id="trigger_hadoop" name="trigger_hadoop" method="POST" action="./aco">

				<div class="panel panel-primary"> 
				<div class="panel-heading"> 
				<h3 class="panel-title">Step 1. 選擇旅遊天數</h3> 
				</div> 
				<div class="panel-body">
				<div class="form-group">
				<label for="">Select how many days you want to travel?</label>
				<select name="Kday" class="form-control col-md-6">
					<option value="1">玩一天</option>
					<option value="2">玩二天</option>
					<option value="3">玩三天</option>
				</select>
				</div>
				</div> 
				</div>
<!-- ========================================================================== --> 
			<div class="panel panel-primary"> 
				<div class="panel-heading"> 
				<h3 class="panel-title">Step 2. 選擇希望經過的景點</h3> 
				</div> 
				<div class="panel-body">
					<table class="table table-condensed">
					  <tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="1"/> 清水斷崖</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="3"/> 太魯閣遊客中心</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="5"/> 長春祠</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="6"/> 布落灣遊憩區</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="7"/> 燕子口</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="9"/> 九曲洞</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="10"/> 綠水合流</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="11"/> 白楊步道</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="12"/> 天祥風景區</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="13"/> 梅園竹村步道</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="14"/> 西寶國小</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="15"/> 新白楊服務站</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="17"/> 關原雲海</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="18"/>大禹嶺</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="20"/> 新城天主堂</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="21"/> 七星潭風景區</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="22"/> 七星潭柴魚博物館</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="23"/> 向日廣場</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="24"/> 七星潭自行車道</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="25"/> 花蓮觀光酒廠</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="26"/> 慈濟靜思精舍</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="27"/> 松園別館</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="28"/> 美崙山公園</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="29"/> 北濱公園</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="30"/> 石藝大街</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="31"/> 花蓮創意文化園區</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="32"/> 鐵道文化商圈</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="33"/> 花蓮縣石雕博物館</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="34"/> 舊鐵道文化商圈</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="35"/> 慈濟文化園區</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="36"/> 紅葉溫泉</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="37"/> 連城連花園</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="38"/> 吉安慶休院</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="39"/> 知卡宣森林公園</label></td>
					  <td class="warning"><label><input type="checkbox" name="POI"  value="40"/> 初英親水生態公園</label></td>
					  <td class="danger"><label><input type="checkbox" name="POI"  value="41"/> 楓林步道</label></td>
					</tr>
					<tr>
					  <td class="active"><label><input type="checkbox" name="POI"  value="42"/> 佐倉步道</label></td>
					  <td class="success"><label><input type="checkbox" name="POI"  value="43"/> 鳳林客家文物館</label></td>
					  <td class="warning"></td>
					  <td class="danger"></td>
					</tr>
					
					</table>
					
					<div class="form-group">
						<div class="checkbox col-md-12">
			  				<label>	
								<input id="trigerJobBtn" type="submit" name="submit" value="開始規劃行程！" class="btn btn-primary"/>
							</label>
						</div>
					</div>

				</div> 
			</div>
<!-- ========================================================================== --> 	
						
				</form>
			</div>
			
			  
			<div class="col-md-6" style="padding-left: 0px;">
			
			
			
			 <div class="panel panel-success">
			      <div class="panel-heading">
			      	<h3 class="panel-title" id="resultNumber">執行結果</h3> 
			      </div>
			      <div class="panel-body">
			      		<div class="row" style="padding: 10px">
						<div id="map-canvas" style="width:500px;height:380px;"></div>
						</div>
						
						
						<div class="row" style="padding: 10px">
							<div id="schedule"></div>
							
							<!-- <div class="form-group">
							    <label for="path_length">Path Length: </label>
							    <span id="path_length"></span>
							</div>
							<div class="form-group">
							    <label for="weight">Weighth: </label>
							    <span id="weight"></span>
							</div> -->
						</div>
			      </div>
		    </div>
			
			
			
				
			</div>
			
		</div>
	    
	    
		<!-- <div calss="row" style="height: 20px"></div> -->
		
		

	    
	    
	</body>
</html>