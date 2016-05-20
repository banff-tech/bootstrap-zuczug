<script>
	function getProductFeatureCategory() {
		var productCategoryId =  $("#productCategoryId").val();
		$("#iframeDiv").load("<@ofbizUrl>EditVirtualAccessory?productCategoryId=</@ofbizUrl>" + $("#productCategoryId").val() + "&isVariant=" + isVariant);
		var isVariant = "N";
	}
	
	function getAccessoryProductInfo() {
		jQuery.ajax({				
                url: 'GetProduct',
                type: 'POST',
                data: {"idValue" : $("#0_lookupId_accessoryProductIdValue").val()},
                success: function(data) { 
                	if (data.product == null) {
                		alert(data._ERROR_MESSAGE_LIST_);
                	}	
                    var productId = data.product.productId;
                    
                    if (data.product.primaryProductCategoryId == null) {
                    	 var productCategoryId =  data.productCategoryId;
                    }else {
                    	 var productCategoryId =  data.product.primaryProductCategoryId;
                    }
                    
                	var isVariant = data.product.isVariant;
                    $("select option[value='" + data.product.primaryProductCategoryId + "']").attr("selected", "selected"); 
					$("#iframeDiv").load("<@ofbizUrl>EditVirtualAccessory?productCategoryId=</@ofbizUrl>" + productCategoryId + "&productId=" + productId + "&isVariant=" + isVariant );
                                      
                },
                error: {
                
                }
            });
	}
</script>
<form action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="EditProductBom">
	<input type="hidden" name="variantProductId" id="variantProductId" value="${variantProductId?if_exists}"/>
	<input type="hidden" name="productId" id="productId" value="${productId?if_exists}"/>
	<input type="hidden" name="colorId" id="colorId" value="${colorId?if_exists}"/>
	<input type="hidden" name="sizeId" id="sizeId" value="${sizeId?if_exists}"/>
	<table cellspacing="0" class="basic-table">
		<tr>
			<td class="label">
				<span id="selectAccessory">商品标识</span>
			</td>
			<td>
				<span>${variantProductId?if_exists}</span>
			</td>
		</tr>
		
		<#if isBomConfirm?exists && isBomConfirm=='false'>
		<tr>
			<td class="label">
				<span id="selectAccessory">选择物料</span>
			</td>
			<td>
				<@htmlTemplate.lookupField formName="EditProductBom" name="accessoryProductIdValue" id="accessoryProductIdValue" fieldFormName="lookupAccessoryProductInCatalog"/>	
				<span><a href="javascript:getAccessoryProductInfo();" class="buttontext">确认选择</a></span>
			</td>
		</tr>
		<#else>
			<#if isBomConfirm?exists>
			<tr>
				<td class="label">
					<span id="selectAccessory">提示</span>
				</td>
				<td>
					该Bom已确认
				</td>
			</tr>
			<#else>
			<tr>
				<td class="label">
					<span id="selectAccessory">提示</span>
				</td>
				<td>
					请选择颜色
				</td>
			</tr>
			</#if>
		</#if>
		<!--<tr>
			<td class="label">
				<span id="selectAccessory">物料说明</span>
			</td>
			<td>
				<input type="text" name="instruction" id="instruction"/>
			</td>
		</tr>-->
	</table>
	<div id="iframeDiv" name="iframeDiv"></div>
</form>
