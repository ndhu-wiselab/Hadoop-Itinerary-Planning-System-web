
<!DOCTYPE html">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<%@ page contentType="text/html; charset=UTF-8"%>
<html>
	<head>
	 <meta charset="UTF-8" />
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
		<title>Hello App Engine</title>
		<script src="https://code.jquery.com/jquery-2.2.3.min.js"></script>
		<script src="https://malsup.github.io/jquery.form.js"></script>
		<script src="https://maps.googleapis.com/maps/api/js?v=3.exp"></script>
		<script src="http://spin.js.org/spin.min.js"></script>
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
					}
			
			var target = document.getElementById('trigger_hadoop')
			var spinner = new Spinner(opts);
			
			function processJson(data) { 
			    console.log(data);
			    spinner.stop();
			    $("#trigger_hadoop :input").prop("checked", false);
			}
			
			
			
			$("#trigerJobBtn").click(function() {
				spinner.spin(target);
				
				$('#trigger_hadoop').ajaxForm({ 
			        dataType:  'json', 
			        success:   processJson 
			    }); 
				
				
			});
			
			
			var map;
			function initialize() {
			  map = new google.maps.Map(document.getElementById('map-canvas'), {
			    zoom: 8,
			    center: {lat: -34.397, lng: 150.644}
			  });
			}
			google.maps.event.addDomListener(window, 'load', initialize);
		});
		
		
		
		

		  
			
    	</script>
		
	</head>
	
	<body>
		<form id="trigger_hadoop" name="trigger_hadoop" method="POST" action="./aco ">
			<span class="back">Trigger Hadoop Job from Web Page </span><br> 
			Input how many days you want to travel?<input type="text"  /><br>
			<input type="checkbox" name="0"  /> 清水斷崖<br>
			<input type="checkbox" name="1"  /> 西寶國小<br>
			<input type="checkbox" name="2"  /> 太魯閣遊客中心<br>
			<input type="checkbox" name="4"  /> 白楊步道<br>
			<input type="checkbox" name="6"  /> 長春祠<br>
			<input type="checkbox" name="8"  /> 布落灣遊憩區<br>
			<input type="checkbox" name="11"  /> 燕子口<br>
			<input type="checkbox" name="26"  /> 天祥風景區<br>
			<input type="checkbox" name="38"  /> 九曲洞<br>
			<input type="checkbox" name="44"  /> 綠水合流<br>
			<input type="checkbox" name="3"  /> 梅園竹村步道<br>
			<input type="checkbox" name="5"  /> 新白楊服務站<br>
			<input type="checkbox" name="7"  /> 關原雲海<br>
			<input type="checkbox" name="9"  /> 大禹嶺<br>
			<input type="checkbox" name="10"  /> 新城天主堂<br>
			<input type="checkbox" name="12"  /> 七星潭風景區<br>
			<input type="checkbox" name="13"  /> 七星潭柴魚博物館<br>
			<input type="checkbox" name="14"  /> 向日廣場<br>
			<input type="checkbox" name="15"  /> 七星潭自行車道<br>
			<input type="checkbox" name="16"  /> 花蓮觀光酒廠<br>
			<input type="checkbox" name="17"  /> 慈濟靜思精舍<br>
			<input type="checkbox" name="18"  /> 松園別館<br>
			<input type="checkbox" name="19"  /> 美崙山公園<br>
			<input type="checkbox" name="20"  /> 北濱公園<br>
			<input type="checkbox" name="21"  /> 石藝大街<br>
			<input type="checkbox" name="22"  /> 花蓮創意文化園區<br>
			<input type="checkbox" name="23"  /> 鐵道文化商圈<br>
			<input type="checkbox" name="24"  /> 花蓮縣石雕博物館<br>
			<input type="checkbox" name="25"  /> 舊鐵道文化商圈<br>
			<input type="checkbox" name="27"  /> 慈濟文化園區<br>
			<input type="checkbox" name="28"  /> 紅葉溫泉<br>
			<input type="checkbox" name="29"  /> 連城連花園<br>
			<input type="checkbox" name="30"  /> 吉安慶休院<br>
			<input type="checkbox" name="31"  /> 知卡宣森林公園<br>
			<input type="checkbox" name="32"  /> 初英親水生態公園<br>
			<input type="checkbox" name="33"  /> 楓林步道<br>
			<input type="checkbox" name="34"  /> 佐倉步道<br>
			<input type="checkbox" name="35"  /> 鳳林客家文物館<br>
			
			<input id="trigerJobBtn" type="submit" name="submit" value="Trigger Job" />
		</form>

		<div id="map-canvas" style="width:500px;height:380px;"></div>
	</body>
</html>