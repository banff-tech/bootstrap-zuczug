<script>
	function getProductFeatureCategory() {
		var productCategoryId =  $("#productCategoryId").val();
		$("#iframeDiv").load("<@ofbizUrl>AccessoryProductFeature?productCategoryId=</@ofbizUrl>" + $("#productCategoryId").val() + "&isVariant=" + isVariant);
		var isVariant = "N";
	}
	
	function getAccessoryProductInfo() {
		jQuery.ajax({				
                url: 'GetProduct',
                type: 'POST',
                data: {"productId" : $("#2_lookupId_accessoryProductId").val()},
                success: function(data) {
                    var productId = data.product.productId;
                    var productCategoryId =  data.product.primaryProductCategoryId;
                    var isVariant = data.product.isVariant;
                    $("select option[value='" + data.product.primaryProductCategoryId + "']").attr("selected", "selected"); 
					$("#iframeDiv").load("<@ofbizUrl>AccessoryProductFeature?productCategoryId=</@ofbizUrl>" + productCategoryId );
                },
                error: {
                
                }
            });
	}
</script>
<form method="post" action="<@ofbizUrl>FindRepeatAccessory</@ofbizUrl>" id="EditProductBom" name="EditProductBom" class="form-horizontal">
	<input type="hidden" id="productTypeId" name="productTypeId" value="${productTypeId}"/>

    <div class="form-group">
        <label class="col-md-2 control-label" id="">
			<#assign prodCatalogCategorys = delegator.findByAnd('ProdCatalogCategoryDetlView',Static["org.ofbiz.base.util.UtilMisc"].toMap("prodCatalogId", "MATERIAL_CATALOG", "productCategoryTypeId", "CATALOG_CATEGORY"))>
			${uiLabelMap.ChooseAccessoriesCategory}
		</label>
        <div class="col-sm-4">
            <#--<input type="text" name="updateInternalName" value="" id="EditAccessoryProduct_internalName" required="true" class=" form-control" size="25">-->
            <select name="productCategoryId" id="productCategoryId" onchange="getProductFeatureCategory()" class="form-control">
                <option value=""></option>
				<#list prodCatalogCategorys as prodCatalogCategory>
					<option value="${prodCatalogCategory.productCategoryId}">${prodCatalogCategory.categoryName}</option>
				</#list>
            </select>
        </div>
    </div>
	<div id="iframeDiv" name="iframeDiv"></div>
</form>