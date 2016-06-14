<script type="text/javascript">
	function queryProduct() {
		var parameters = $('#EditProductBom').serialize();
		jQuery.ajax({
	        url: "ListRepeatAccessory",
	        type: 'POST',
	        data: parameters,
	        error: function(msg) {
	            alert("An error occured loading content! : " + msg);
	        },
	        success: function(msg) {
	            jQuery('#search-results').html(msg);
	        }
	    });
	}
</script>

	<input type="hidden" name="updateMode" value="${updateMode}"/>
	<input type="hidden" name="productCategoryId" value="${productMap.productCategoryId}"/>
	<#list groupMaps as groupMap>
		<div class="form-group">
			<label class="col-md-2 control-label" id="EditZuczugProduct_${groupMap.productFeatureGroup.productFeatureGroupId}_title">${groupMap.productFeatureGroup.description}</label>
			<div class="col-sm-4">
				<div class="input-group">
					<#if isVariant == "Y">
						<#list groupMap.productFeatures as productFeature>
							<#if (updateMode=="UPDATE"&&(productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)) == productFeature.productFeatureId)>
							${productFeature.description?if_exists}
							</#if>
						</#list>
					<#else>
						<select name="${groupMap.productFeatureGroup.productFeatureGroupId}" size="1" class="form-control" style="height: 32px;">
							<option value="NONE">不检索</option>
							<#list groupMap.productFeatures as productFeature>
								<option value="${productFeature.productFeatureId}"<#if (updateMode=="UPDATE"&&(productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)) == productFeature.productFeatureId)> selected="selected"</#if>>
								${productFeature.description?if_exists}
								</option>
							</#list>
						</select>
					</#if>
					<div class="input-group-btn">
						<button class="btn btn-default btn-primary" type="button" onclick="createVariantProduct()" style="margin-left: -3px;">
							<i class="fa fa-external-link"></i> 生成变形商品
						</button>
					</div>
				</div>
			</div>
		</div>
	</#list>

<#--<input type="button" class="btn btn-sm btn-primary" name="submintButton" value="生成变形商品" onclick="createVariantProduct()">-->
<div class="form-group">
    <label class="col-md-2 control-label" id="">&nbsp;</label>
    <div class="col-sm-4">
        <input type="button" class="btn btn-sm btn-primary" name="submintButton" value="查询" onclick="queryProduct()">
    </div>
</div>