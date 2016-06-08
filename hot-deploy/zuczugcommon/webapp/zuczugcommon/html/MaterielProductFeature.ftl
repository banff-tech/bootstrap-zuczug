<script type="text/javascript" src="/zuczugcommon/upload/plugins/validate/additional-methods.js"></script>
<script>
$(function(){
	jQuery("#EditZuczugProduct").validate();
	$("#EditZuczugProduct").find(".integernumber").each(function(){
	     $(this).rules(
	     	"integerNumber", {
			       required: true,
			       integerNumber: true,
		       	   messages: {
		         		required: "只能输入Number"
		           },
	     });   
    }); 
    $("#EditZuczugProduct").find(".idValueType").each(function(){
	     $(this).rules(
	     	"idValueType", {
			       idValueType: true,
		       	   messages: {
		         		required: "不可输入除“-”外的符号和中文字符",
		         		idValueType:"不可输入除“-”外的符号和中文字符"
		           },
	     });   
    }); 
});
</script>
<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" class="form-horizontal" id="EditZuczugProduct" name="EditZuczugProduct">
	<input type="hidden" name="productTypeId" value="${product.productTypeId}" id="EditAccessoryProduct_productTypeId">
	<input type="hidden" name="updateMode" value="${updateMode}"/>
	<input type="hidden" name="productCategoryId" value="${productCategoryId?if_exists}"/>
	<input type="hidden" name="productTypeId" value="${productMap.productTypeId?if_exists}"/>
	<input type="hidden" name="productId" value="${productMap.productId?if_exists}"/>
	<input type="hidden" name="internalName" value="${productMap.internalName?if_exists}"/>
	<input type="hidden" name="updateMode" value="${updateMode}">
	<input type="hidden" name="isVirtual" value="${product.isVirtual}">
	<input type="hidden" name="isVariant" value="${product.isVariant}">
	<input type="hidden" name="idValue" value="${idValue?if_exists}">
	<input type="hidden" name="productFeatureApplTypeId" value="STANDARD_FEATURE" id="EditAccessoryProduct_productFeatureApplTypeId">
    <#assign goodIdentification = delegator.findOne('GoodIdentification',Static["org.ofbiz.base.util.UtilMisc"].toMap("goodIdentificationTypeId", "ZUCZUG_CODE", "productId", productMap.productId),true)>
	<div class="form-group">
        <label class="col-md-2 control-label" id="">物料名称</label>
        <div class="col-sm-4">
            <input type="text" name="updateInternalName" value="${product.internalName?if_exists}" id="EditAccessoryProduct_internalName" required="true" class=" form-control" size="25">
		</div>
	</div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">物料描述</label>
        <div class="col-sm-4">
            <textarea name="comments" class=" form-control" cols="60" rows="3" id="EditAccessoryProduct_comments">${product.comments?if_exists}</textarea>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">备注</label>
        <div class="col-sm-4">
            <textarea name="longDescription" class=" form-control" cols="60" rows="3" id="EditAccessoryProduct_longDescription">${product.longDescription?if_exists}</textarea>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">是否虚拟</label>
        <div class="col-sm-4">
			<p style="padding-top: 7px;">${product.isVirtual?if_exists}</p>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">是否变形</label>
        <div class="col-sm-4">
            <p style="padding-top: 7px;">${product.isVariant?if_exists}</p>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">物料计量单位</label>
        <div class="col-sm-4">
            <select name="quantityUomId" class=" form-control" id="EditAccessoryProduct_quantityUomId" required="true">
                <option value="">&nbsp;</option>
                <option <#if product.quantityUomId?if_exists == 'LEN_m'> selected="selected"</#if> value="LEN_m">米</option>
                <option <#if product.quantityUomId?if_exists == 'WT_kg'> selected="selected"</#if> value="WT_kg">公斤</option>
                <option <#if product.quantityUomId?if_exists == 'AREA_m2'> selected="selected"</#if> value="AREA_m2">平方米</option>
                <option <#if product.quantityUomId?if_exists == 'JIAN'> selected="selected"</#if> value="JIAN">件</option>
                <option <#if product.quantityUomId?if_exists == 'GE'> selected="selected"</#if> value="GE">个</option>
                <option <#if product.quantityUomId?if_exists == 'GEN'> selected="selected"</#if> value="GEN">根</option>
                <option <#if product.quantityUomId?if_exists == 'ZHANG'> selected="selected"</#if> value="ZHANG">张</option>
			</select>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditAccessoryProduct_productWidth_title"><#if module=='ZUCZUG'>幅宽</#if><#if module=='HAZE'>宽度</#if></label>
        <div class="col-sm-4">
            <input type="text" class="form-control" id="productWidth" name="productWidth" value="${product.productWidth?if_exists}" size="10" maxlength="20" id="EditAccessoryProduct_productWidth">
            <p class="note"><strong>Note:</strong>默认单位为厘米</p>
        </div>
    </div>
	<#if product.isVirtual=="N">
        <div class="form-group">
			<#assign productAttr = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("attrName", "WEIGHT_G", "productId", productMap.productId),true)?if_exists>
            <label class="col-md-2 control-label" id="EditAccessoryProduct_productWidth_title"><#if module=='ZUCZUG'>克重</#if><#if module=='HAZE'>重量</#if></label>
            <div class="col-sm-4">
                <input type="text" name="productWeight" class="form-control" value="${productAttr.attrValue?if_exists}" size="10" maxlength="20" id="EditAccessoryProduct_productWidth">
                <p class="note"><strong>Note:</strong>默认单位为克/平方米</p>
            </div>
        </div>
	</#if>
	<#assign productCategory = delegator.findOne("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", productMap.productCategoryId),true)>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditAccessoryProduct_primaryProductCategoryId_title">物料分类</label>
        <div class="col-sm-4">
            <p style="padding-top: 7px;">${productCategory.categoryName?if_exists}</p>
        </div>
    </div>
<#list groupMaps as groupMap>
    <div class="form-group">
        <label class="col-md-2 control-label" id="EditZuczugProduct_${groupMap.productFeatureGroup.productFeatureGroupId}_title">${groupMap.productFeatureGroup.description}</label>
        <div class="col-sm-4">
            <select name="${groupMap.productFeatureGroup.productFeatureGroupId}" class=" form-control">
                <option value="NONE"></option>
				<#list groupMap.productFeatures as productFeature>
                    <option value="${productFeature.productFeatureId}"<#if (updateMode=="UPDATE"&&productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)?if_exists == productFeature.productFeatureId)> selected="selected"</#if>>
					${productFeature.description?if_exists}
                    </option>
				</#list>
            </select>
        </div>
    </div>
</#list>
<#if (product.isVirtual=="N"&&product.isVariant=="N")>
	<#if groupMaps2?exists>
		<#list groupMaps2 as groupMap2>
            <div class="form-group">
                <label class="col-md-2 control-label" id="EditZuczugProduct_${groupMap2.productFeatureCategory.productFeatureCategoryId}_title">${groupMap2.productFeatureCategory.description}</label>
                <div class="col-sm-4">
                    <select name="productFeatureCategory_${groupMap2.productFeatureCategory.productFeatureCategoryId}" size="1" class="form-control">
						<#if isVirtual=="Y"><option value="NONE">不选择</option></#if>
						<#list groupMap2.productFeatures as productFeature>
                            <option value="${productFeature.productFeatureId}"<#if (productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)?exists&&updateMode=="UPDATE"&&(productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)) == productFeature.productFeatureId)> selected="selected"</#if>>
							${productFeature.description?if_exists}
                            </option>
						</#list>
                    </select>
                </div>
            </div>
		</#list>
	</#if>
</#if>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">&nbsp;</label>
        <div class="col-sm-4">
            <input type="submit" class="btn btn-sm btn-primary" name="submintButton" value="提交">
        </div>
    </div>

    <div class="form-group">
        <label class="col-md-2 control-label" id="">上次修改者:</label>
        <div class="col-sm-4">
            <p style="padding-top: 7px;">[${product.lastModifiedByUserLogin?if_exists}] ${uiLabelMap.CommonOn?if_exists} ${product.lastModifiedDate?if_exists}</p>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">创建者:</label>
        <div class="col-sm-4">
            <p style="padding-top: 7px;">[${product.createdByUserLogin?if_exists}] ${uiLabelMap.CommonOn?if_exists} ${product.createdDate?if_exists}</p>
        </div>
    </div>


</form>
