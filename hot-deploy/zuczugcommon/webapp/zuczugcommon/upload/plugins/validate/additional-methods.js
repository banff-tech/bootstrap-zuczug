/**
 * jQuery Validation Plugin 1.9.0
 *
 * http://bassistance.de/jquery-plugins/jquery-plugin-validation/
 * http://docs.jquery.com/Plugins/Validation
 *
 * Copyright (c) 2006 - 2011 Jörn Zaefferer
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

jQuery.validator.addMethod("integer", function(value, element) {
	if(value=="") return false;
	return this.optional(element) || /^-?\d+$/.test(value) || value=="";
}, "请输入一个整数");

jQuery.validator.addMethod("integerNumber", function(value, element) {
	if(value=="") return false;
	return this.optional(element) || /^[0-9]+\.{0,3}[0-9]{0,3}$/.test(value);
}, "只能输入数字");

jQuery.validator.addMethod("integerNull", function(value, element) {
	if(value.length>0){
		return /^\+?[1-9][0-9]*$/.test(value);
	}else{
		return true;
	}
}, "请输入一个不为0的正整数");

jQuery.validator.addMethod("capital", function(value, element) {
	if(value.length>0){
		return /([A-Z-]+[0-9]*)/.test(value);
	}else{
		return true;
	}
}, "只能输入大写英语字符和数字");

jQuery.validator.addMethod("idValueValidate", function(value, element) {
	if(value.length>0){
		return /^[0-9a-zA-Z-]*$/.test(value);
	}else{
		return true;
	}
}, "不可输入除“-”外的符号和中文字符");

jQuery.validator.addMethod("idValueType", function(value, element) {
	if(value=="") return false;
	return this.optional(element) || /^[0-9a-zA-Z-]*$/.test(value);
}, "不可输入除“-”外的符号和中文字符");

jQuery.validator.addMethod("eanValidata", function(value, element) {
	if(value=="") return false;
	return this.optional(element) || /^\d{12}$/.test(value);
}, "只能输入12数字");

jQuery.validator.addMethod("idType", function(value, element) {
	if(value=="") return false;
	return this.optional(element) || /^[a-zA-Z0-9_-]+$/.test(value);
}, "只能输入数字、字母、中划线、下划线");

jQuery.validator.addMethod("lessToOldAvailable", function(value, element) {
	var availableTotal = $('#availableToPromiseTotal').val();
    availableTotal = availableTotal.replace(/,/g, "");
    value=value.replace(/,/g, "");
	var availableToPromiseTotal = Number(availableTotal);
	return this.optional(element) || availableToPromiseTotal >= Number(value);
}, "转仓的数量不能大于承诺有效的数量");

function downloadExcel(isAll) {
	$("input[name='isAll']").attr("value",isAll);
	$("#FindProductVirtualContent").attr("action","downloadExcel");
	$("#FindProductVirtualContent").submit();
}

function findRealProduct() {
	$("#FindProductVirtualContent").attr("action","FindRealProductContentList");
	$("#FindProductVirtualContent").submit();
}

function loadContentcenterProductInfo() {
	$("#EditShopProductContent").attr("action","loadProductInfo");
	$("#EditShopProductContent").submit();
}

function saveContentcenterProductInfo() {
	$("#EditShopProductContent").attr("action","EditShopProductContent");
	$("#EditShopProductContent").submit();
}

function eanDistribution() {
	if($("input[name='isUserEanCode']:checked").val() == "N") {
		alert("不为" + $("input[name='productId']").val() + "分配EAN码");
		$("input[name='idValue']").attr("value",'不分配EAN码');
	}else{
		jQuery.ajax({				
                url: 'GetEANCode',
                type: 'POST',
                data: {"productId" : $("input[name='productId']").val()},
                success: function(data) {
                	alert("自动分配的EAN码为："+data.idValue);
                	$("input[name='idValue']").attr("value",data.idValue);
                    
                },
                error: {
                
                }
        });
	}
}

function cancelSelectEanCode() {
	if(confirm("是否取消关联的EAN码")){
       	jQuery.ajax({				
            url: 'cancelEanCode',
            type: 'POST',
            data: {"productId" : $("input[name='productId']").val(), "idValue" :  $("input[name='idValue']").val()},
            success: function(data) {
            	$("input[name='idValue']").attr("value","已取消已分配的EAN码，请刷新页面");
            	window.location.href="/manufact/control/PorductStickers?productId="+$("input[name='productId']").val();                
            },
            error: {
            
            }
    	});
    }	
}

