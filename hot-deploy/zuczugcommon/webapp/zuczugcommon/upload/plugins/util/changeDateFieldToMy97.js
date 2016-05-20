/*!
 * changeDateFieldToMy97
 * @author Sven
 * @version 1.0
 *
 * description : 扩展选择日期控件，设置格式为年月、年月日、时分秒等格式
 * 使用方法：
 * 1、在Screen中引入这个js，或者在CommonScreen	，
 *		<set field="layoutSettings.javaScripts[]" value="/zuczugcommon/upload/plugins/util/changeDateFieldToMy97.js" global="true"/>
 * 2、在OFBiz的Form中，使用 text 控件，并添加 widget-style 属性，为 "changeToMy97"
 * 3、如果有特定的格式，还需要增加其他的class，年月：yyyyMM 、 年月日：yyyyMMdd、带时分秒的:yyyyMMddHHmmss，暂时支持3中格式,不填就是带时分秒的
 * 例子：
 * <field name="listedMonthId" title="上市月份" map-name="lm" widget-style="changeToMy97 yyyyMM" required-field="true">
 *		<text></text>
 * </field>
 * 4、如果是ftl，直接参考这个文档，不需要使用这个js，但是如果你用，我也不能拦着你！
 */



$(function(){
	
	var getDateFmt=function(input){
		var dateFmt = "yyyy-MM-dd HH:mm:ss";
		if($(input).hasClass("yyyyMM")){
			dateFmt = "yyyy-MM";
		}else if($(input).hasClass("yyyyMMdd")){
			dateFmt = "yyyy-MM-dd";
		}else if($(input).hasClass("yyyyMMddHHmmss")){
			dateFmt = "yyyy-MM-dd HH:mm:ss";
		}
		return dateFmt;
	}

	var getRealDate=function(longDate,dateFmt){
		var realDate = "";
		longDate = longDate.substring(0,19);
		if(dateFmt=="yyyy-MM-dd HH:mm:ss"){
			realDate = longDate;
		}else if(dateFmt=="yyyy-MM-dd"){
			realDate = longDate.substring(0,10);
		}else if(dateFmt=="yyyy-MM"){
			realDate = longDate.substring(0,7);
		}
		return realDate;
	}

	var hm = document.createElement("script");
  	hm.src = "/zuczugcommon/upload/plugins/My97DatePicker/WdatePicker.js";
  	var s = document.getElementsByTagName("script")[1]; 
  	s.parentNode.insertBefore(hm, s);

	$(".changeToMy97").each(function(){
		var dateFmt = getDateFmt($(this));
		//绑定时间
		$(this).focus(function(){
			WdatePicker({skin:'whyGreen',dateFmt:dateFmt});
		}).addClass("Wdate").attr("readOnly","readOnly");

		//修改内容
		var val = $(this).val();
		if(val.length>=1){
			//OFBiz 时间是 2015-07-30 00:00:00.000 这种格式， 23个长度
			//1、去除结尾的3个0
			if(val.length==23){
				$(this).val(getRealDate(val,dateFmt));
			}
		}
	});
});






