<script>	
	function createVariantProduct() {
		$('#EditZuczugProduct').submit();		
	}

</script>

<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>"  class="form-horizontal" name="EditZuczugProduct" id="EditZuczugProduct">
	<input type="hidden" name="updateMode" value="${updateMode?if_exists}"/>
	<input type="hidden" name="bomId" value="${parameters.bomId?if_exists}"/>
	<input type="hidden" name="variantProductId" value="${parameters.variantProductId?if_exists}"/>
	<input type="hidden" name="colorId" value="${parameters.colorId?if_exists}"/>
	<input type="hidden" name="sizeId" value="${parameters.sizeId?if_exists}"/>	
	<input type="hidden" name="instruction" value="${parameters.instruction?if_exists}"/>
	<input type="hidden" name="quantity" value="${parameters.quantity?if_exists}"/>
	<input type="hidden" name="fromDate" value="${parameters.fromDate?if_exists}"/>
	<input type="hidden" name="productTypeId" value="${productMap.productTypeId?if_exists}"/>
	<input type="hidden" name="virtualProductId" value="${virtualProductId?if_exists}"/>
	<input type="hidden" name="productId" value="${productMap.productId?if_exists}"/>
	<input type="hidden" name="productCategoryId" value="${productCategoryId?if_exists}"/>
	<input type="hidden" name="zuczugId" value="${zuczugId?if_exists}"/>
	<input type="hidden" name="idValue" value="${zuczugId?if_exists}"/>
	<input type="hidden" name="productTypeId" value="${productTypeId?if_exists}"/>
<#if productTypeId == 'RAW_MATERIAL'>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_productId_title">素然物料编号</label>
        <div class="col-sm-4">
			<#if productMap.isVariant=='N'>
				<input type="text" name="productZuczugId" value="" class=" form-control">
                <p class="note"><strong>Note:</strong>非必填,如不输入则系统会自动分配物料编号</p>
			<#else>
                <p style="padding-top: 7px;">${zuczugId?if_exists}</p>
			</#if>
        </div>
    </div>
<#else>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_productId_title">素然物料编号</label>
        <div class="col-sm-4">
			<#if productMap.isVariant=='N'>
                <input type="text" name="productZuczugId" value="" class=" form-control">
                <p class="note"><strong>Note:</strong>非必填,如不输入则系统会自动分配物料编号</p>
			<#else>
                <p style="padding-top: 7px;">${zuczugId?if_exists}</p>
			</#if>
	</div>
</#if>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_internalName_title">内部名称</label>
        <div class="col-sm-4">
		<#if productMap.isVariant=='N'>
            <input type="text" name="internalName" value="" class=" form-control">
            <p class="note"><strong>Note:</strong>非必填,如不填写系统则会默认生成N&A</p>
		<#else>
            <p style="padding-top: 7px;">${productMap.internalName?if_exists}</p>
		</#if>
		</div>
	</div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_comments_title">商品描述</label>
        <div class="col-sm-4">
		<#if productMap.isVariant=='N'>
            <input type="text" name="comments" value="" class=" form-control">
            <p class="note"><strong>Note:</strong>非必填,填写商品描述信息</p>
		<#else>
            <p style="padding-top: 7px;">${productMap.comments?if_exists}</p>
		</#if>
		</div>
    </div>
<#if isShowReason?exists>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_comments_title">特征描述</label>
        <div class="col-sm-4">
			<p style="padding-top: 7px;">${reason}</p>
        </div>
    </div>
</#if>
<#list groupMaps as groupMap>
	<div class="form-group">
		<label class="col-md-2 control-label" id="EditZuczugProduct_${groupMap.productFeatureGroup.productFeatureGroupId}_title">固定特征-${groupMap.productFeatureGroup.description}</label>
		<div class="col-sm-4">
			<#if updateMode=="CREATE">
                <select name="${groupMap.productFeatureGroup.productFeatureGroupId}" size="1" class="form-control">
					<#list groupMap.productFeatures as productFeature>
                        <option value="${productFeature.productFeatureId}">
						${productFeature.description?if_exists}
                        </option>
					</#list>
                </select>
			<#else>
				<#if groupMap?exists>
					<#assign flag = "error">
				</#if>
				<#list groupMap.productFeatures as productFeature>
					<#if productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)?exists>
						<#if (updateMode=="UPDATE"&&(productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)) == productFeature.productFeatureId)>
                            <input type="hidden" name="${groupMap.productFeatureGroup.productFeatureGroupId}" value="${productFeature.productFeatureId}" class="form-control"/>
                            <p style="padding-top: 7px;">${productFeature.description?if_exists}</p>
							<#assign flag = "success">
						</#if>
					<#else>
						<#assign flag = "error">
					</#if>
				</#list>
				<#if flag!="success">
                    <p class="note"><strong>Note:</strong>此商品在创建时该固定特征分类还未创建</p>
				</#if>
			</#if>
		</div>
	</div>
</#list>
<#list groupMaps2 as groupMap2>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_${groupMap2.productFeatureCategory.productFeatureCategoryId}_title">可变特征-${groupMap2.productFeatureCategory.description}</label>
        <div class="col-sm-4">
			<#if productMap.isVariant=='N'>
                <select name="productFeatureCategory_${groupMap2.productFeatureCategory.productFeatureCategoryId}" id="productFeatureCategory_${groupMap2.productFeatureCategory.productFeatureCategoryId}" class="form-control">
					<#list groupMap2.productFeatures as productFeature>
                        <option value="${productFeature.productFeatureId}">
						${productFeature.description?if_exists}
                        </option>
					</#list>
                </select>
			<#else>
				<#list groupMap2.productFeatures as productFeature>
					<#if (productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)?exists&&updateMode=="UPDATE"&&(productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)) == productFeature.productFeatureId)>
                        <p style="padding-top: 7px;">${productFeature.description?if_exists}</p>
					</#if>
				</#list>
			</#if>
		</div>
    </div>
</#list>
<#if updateMode=="CREATE">
	<div class="form-group">
		<label class="col-md-2 control-label" id="">&nbsp;</label>
		<div class="col-sm-4"><!--  -->
            <input type="submit" class="btn btn-sm btn-primary" name="submintButton" value="新增" onclick="javascript:return confirm('确认要创建该商品吗?');">
    		<#--<a href="javascript:if(confirm('确认要创建该商品吗?'))document.EditZuczugProduct.submit();" class="buttontext" onclick="checkSubmit()">新增</a>-->
		</div>
	</div>
<#else>
</#if>
<#if productMap.productId?exists>
	<#if productMap.isVariant=='N'>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">&nbsp;</label>
    	<div class="col-sm-4">
            <input type="button" class="btn btn-sm btn-primary" name="submintButton" value="生成变形商品" onclick="createVariantProduct()">
		</div>
	</div>
	</#if>
</#if>
</form>
