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
<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" id="EditZuczugProduct" name="EditZuczugProduct">
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
	<table cellspacing="0" class="basic-table">
	<tbody>
		<!--<tr>
		    <td class="label">
				<span class="idValueValidate" id="EditAccessoryProduct_idValue_title">素然物料编号</span>    </td>
		    <td>
				${goodIdentification.idValue?if_exists}
		    </td>-->
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_internalName_title">物料名称</span>    </td>
		    <td>
				<input type="text" name="updateInternalName" value="${product.internalName?if_exists}" size="30" id="EditAccessoryProduct_internalName">
		    </td>
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_comments_title">物料描述</span>    </td>
		    <td>
				<textarea name="comments" cols="60" rows="2" id="EditAccessoryProduct_comments" class="valid">${product.comments?if_exists}</textarea>		
		    </td>
	    </tr>
	   
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_longDescription_title">备注</span>    </td>
		    <td>
				<textarea name="longDescription" cols="60" rows="2" id="EditAccessoryProduct_longDescription">${product.longDescription?if_exists}</textarea>		
		    </td>
	    </tr>
	     <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_comments_title">是否虚拟</span>    </td>
		    <td>
				${product.isVirtual?if_exists}	
		    </td>
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_comments_title">是否变形</span>    </td>
		    <td>
				${product.isVariant?if_exists}	
		    </td>
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_quantityUomId_title">物料计量单位</span>    </td>
		    <td>
				<span class="ui-widget">
					<select name="quantityUomId" class="required valid" id="EditAccessoryProduct_quantityUomId" size="1">  
						 <option value="">&nbsp;</option>
						 <option <#if product.quantityUomId?if_exists == 'LEN_m'> selected="selected"</#if> value="LEN_m">米</option> 
						 <option <#if product.quantityUomId?if_exists == 'WT_kg'> selected="selected"</#if> value="WT_kg">公斤</option> 
						 <option <#if product.quantityUomId?if_exists == 'AREA_m2'> selected="selected"</#if> value="AREA_m2">平方米</option> 
						 <option <#if product.quantityUomId?if_exists == 'JIAN'> selected="selected"</#if> value="JIAN">件</option> 
						 <option <#if product.quantityUomId?if_exists == 'GE'> selected="selected"</#if> value="GE">个</option> 
						 <option <#if product.quantityUomId?if_exists == 'GEN'> selected="selected"</#if> value="GEN">根</option> 
						 <option <#if product.quantityUomId?if_exists == 'ZHANG'> selected="selected"</#if> value="ZHANG">张</option>
					</select>
				</span>
		    </td>
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_productWidth_title"><#if module=='ZUCZUG'>幅宽</#if><#if module=='HAZE'>宽度</#if></span>    
			</td>
		    <td>
				<input type="text" class="integerNumber" id="productWidth" name="productWidth" value="${product.productWidth?if_exists}" size="10" maxlength="20" id="EditAccessoryProduct_productWidth" class="valid">
				<span class="tooltip">默认单位为厘米</span>    
			</td>
	    </tr>
	    <#if product.isVirtual=="N">
		    <#assign productAttr = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("attrName", "WEIGHT_G", "productId", productMap.productId),true)?if_exists>
		    <tr>
			    <td class="label">
					<span id="EditAccessoryProduct_weight_title"><#if module=='ZUCZUG'>克重</#if><#if module=='HAZE'>重量</#if></span>    
				</td>
			    <td>
					<input type="text" name="productWeight" class="integerNumber" value="${productAttr.attrValue?if_exists}" size="10" maxlength="20" id="EditAccessoryProduct_productWidth" class="valid">
					<span class="tooltip">默认单位为克/平方米</span>    
				</td>
		    </tr>
	    </#if>
	    <#assign productCategory = delegator.findOne("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", productMap.productCategoryId),true)>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_primaryProductCategoryId_title">物料分类</span>    
			</td>
		    <td>
				${productCategory.categoryName?if_exists}    
			</td>
	    </tr>
		<#list groupMaps as groupMap>
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_${groupMap.productFeatureGroup.productFeatureGroupId}_title">${groupMap.productFeatureGroup.description}</span>
			</td>
			<td>
				<span class="ui-widget">
						<select name="${groupMap.productFeatureGroup.productFeatureGroupId}" size="1">
							<option value="NONE"></option>
							<#list groupMap.productFeatures as productFeature>
								<option value="${productFeature.productFeatureId}"<#if (updateMode=="UPDATE"&&productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)?if_exists == productFeature.productFeatureId)> selected="selected"</#if>>
									${productFeature.description?if_exists}
								</option>
							</#list>
						</select>									
				</span>
			</td>
		</tr>
	</#list>
	<#if (product.isVirtual=="N"&&product.isVariant=="N")>
		<#if groupMaps2?exists>
			<#list groupMaps2 as groupMap2>
				<tr>
					<td class="label">
						<span id="EditZuczugProduct_${groupMap2.productFeatureCategory.productFeatureCategoryId}_title">${groupMap2.productFeatureCategory.description}</span>
					</td>
					<td>
						<span class="ui-widget">				
							<select name="productFeatureCategory_${groupMap2.productFeatureCategory.productFeatureCategoryId}" size="1">
								<#if isVirtual=="Y"><option value="NONE">不选择</option></#if>
								<#list groupMap2.productFeatures as productFeature>
									<option value="${productFeature.productFeatureId}"<#if (productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)?exists&&updateMode=="UPDATE"&&(productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)) == productFeature.productFeatureId)> selected="selected"</#if>>
										${productFeature.description?if_exists}
									</option>
								</#list>
							</select>								
						</span>
					</td>			
				</tr>
			</#list>
		</#if>
	</#if>
		<tr>
		    <td class="label">
				&nbsp;    
			</td>
		    <td>
				<a href="javascript:document.EditZuczugProduct.submit();" class="buttontext">更新</a>
		    </td>
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_lastUpdatedByText_title">上次修改者:</span>    </td>
		    <td>	
				[${product.lastModifiedByUserLogin?if_exists}] ${uiLabelMap.CommonOn?if_exists} ${product.lastModifiedDate?if_exists}    
			</td>
	    </tr>
	    <tr>
		    <td class="label">
				<span id="EditAccessoryProduct_createdByText_title">创建者:</span>    </td>
		    <td colspan="4">	
				[${product.createdByUserLogin?if_exists}] ${uiLabelMap.CommonOn?if_exists} ${product.createdDate?if_exists}
			</td>
	    </tr>	
	</tbody>
	</table>

</form>
