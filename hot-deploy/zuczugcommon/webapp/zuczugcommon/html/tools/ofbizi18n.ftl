<html>
	<head>
		<title>自动转换OFBiz国际化</title>
		<script type="text/javascript" src="http://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
		<script type="text/javascript">
			$(function(){
				$("#gen").click(function(){
					gen();
				});

				$("#translate").click(function(){
					var zh = $("#zh").val();
					// 提供jsonp服务的url地址（不管是什么类型的地址，最终生成的返回值都是一段javascript代码）
				    var url = "http://fanyi.youdao.com/openapi.do?keyfrom=tourmsg&key=1134363095&type=data&callback=show&doctype=jsonp&version=1.1&q=" + zh;
				    // 创建script标签，设置其属性
				    var script = document.createElement('script');
				    script.setAttribute('src', url);
				    // 把script标签加入head，此时调用开始
				    document.getElementsByTagName('head')[0].appendChild(script);
					
				});
			});

			function gen(){
				var zh = $("#zh").val();
				var en = $("#en").val();
				var key = $("#key").val();
				var content = '<property key="' + key + '">\r'+
			        '\t<value xml:lang="en">'+ en +'</value>\r'+
			        '\t<value xml:lang="zh">'+ zh +'</value>\r'+
			    '</property>';
			    $("#reslut").val(content);
			}

			function show(data){
				var translation = data['translation'];
				var apiResult = translation[0];
				var arr = apiResult.split(" ");
				var newstr = "";
				$(arr).each(function(k,v){
					newstr+=v.firstUpperCase();
				});
				$("#key").val(newstr);
				$("#en").val(translation);
				gen();
			}

			String.prototype.firstUpperCase = function(){
			    return this.replace(/\b(\w)(\w*)/g, function($0, $1, $2) {
			        return $1.toUpperCase() + $2.toLowerCase();
			    });
			}
		</script>
	</head>
	<body>
		<div>
		KEY:<input type="text" id="key" size="40"/>
		</div>
		<div style="height: 100px;">
			<div style="position: relative;float: left;">
				<p>中文：</p>
				<input type="text" size="20" id="zh"/>
			</div>
			<div style="position: relative;float: left;margin-left: 30px">
				<p>英文：</p>
				<input type="text" size="20" id="en" onkeyup="$('#key').val($('#en').val());gen()"/>
			</div>
		</div>
		<div><button id="gen" style="width: 120px;height: 30px">生产OFBiz国际化</button>
		 OR
		<button id="translate" style="width: 120px;height: 30px">翻译并取第一个</button></div>
	
		<div>
			<p>结果：</p>
			<textarea rows="5" cols="70" id="reslut">
<property key="">
	<value xml:lang="en"></value>
	<value xml:lang="zh"></value>
</property>
			</textarea>
		</div>
		<hr/>

	</body>
</html>