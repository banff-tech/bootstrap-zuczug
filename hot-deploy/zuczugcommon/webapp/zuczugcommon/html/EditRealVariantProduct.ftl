<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="EditRealProduct" id="EditRealProduct" onsubmit="return validateAndSubmit()">
	<input type="hidden" name="updateMode" value="${updateMode?if_exists}"/>
	<input type="hidden" name="productCategoryId" value="${productCategoryId?if_exists}"/>
	<input type="hidden" name="isVirtual" value="${isVirtual?if_exists}"/>
	<input type="hidden" name="isVariant" value="${isVariant?if_exists}"/>
	<input type="hidden" name="productTypeId" value="${productTypeId?if_exists}"/>
	<input type="hidden" name="internalName" value="${internalName?if_exists}"/>
	<table cellspacing="0" class="basic-table">
	<font color="#FF0000">${errorMsg?if_exists}</font> 
	<tbody>
		<tr>
			<td class="label">
				<span id="AddVritualProduct_productId_title">商品款号</span>
			</td>			
			<td>
				<input type="text" name="productId" class="required" size="30" maxlength="20" id="AddVritualProduct_productId" >
				*
				<span class="tooltip">只能包含英文，下划线(_)、横线(-)、以及数字</span>
			</td>
			
		</tr>
		
		<tr>
			<td class="label">
				<span id="AddVritualProduct_quantityUomId_title">商品名称</span>	
			</td>
			<td>
				<input type="text" name="productName" class="required" size="30" maxlength="100" id="AddVritualProduct_productName">
				*		
			</td>			
		</tr>
		<#assign UomAndGroupViews = delegator.findByAnd('UomAndGroupView',Static["org.ofbiz.base.util.UtilMisc"].toMap("uomGroupId","PRODUCT_QUANTITY"))>
		<tr>
			<td class="label">
				<span id="AddVritualProduct_quantityUomId_title">数量单位</span>
			</td>
			<td>
				<select name="quantityUomId" id="AddVritualProduct_quantityUomId" size="1" class="required">
					<option value="">&nbsp;</option>
					<#list UomAndGroupViews as UomAndGroupView>
						<option value="${UomAndGroupView.uomId}" >${UomAndGroupView.description}</option> 
					</#list>
 				</select>
			</td>			
		</tr>
		<tr>
			<td class="label">
				<span id="AddVritualProduct_quantityUomId_title">组别</span>
			</td>
			<td>
				<@htmlTemplate.lookupField formName="EditRealProduct" name="groupId" fieldFormName="lookupProdGroupName"/>
			</td>			
		</tr>
		<tr>
		    <td class="label">
				<span id="AddVritualProduct_comments_title">备注</span>    </td>
		    <td>
				<textarea name="comments" cols="30" rows="3" id="AddVritualProduct_comments"></textarea>		
		    </td>
	    </tr>
	    <tr>
    		<td class="label">
				<span id="AddVritualProduct_longDescription_title">描述</span>    </td>
    		<td>
				<textarea name="longDescription" cols="30" rows="3" id="AddVritualProduct_longDescription"></textarea>

    		</td>
    	</tr>
		<#list groupMaps as groupMap>
			<tr>
				<td class="label">
					<span id="EditZuczugProduct_${groupMap.productFeatureGroup.productFeatureGroupId}_title">固定特征-${groupMap.productFeatureGroup.description}</span>
				</td>
				<td>
					<span class="ui-widget">
						<#if updateMode=="CREATE">
							<select name="${groupMap.productFeatureGroup.productFeatureGroupId}" size="1">
								<option value="NONE"></option>
								<#list groupMap.productFeatures as productFeature>
									<option value="${productFeature.productFeatureId}"<#if (updateMode=="UPDATE"&&(productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)) == productFeature.productFeatureId)> selected="selected"</#if>>
										${productFeature.description?if_exists}
									</option>
								</#list>
							</select>
						<#else>
							<#list groupMap.productFeatures as productFeature>						
								<#if productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)?exists>
									<#if (updateMode=="UPDATE"&&(productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)) == productFeature.productFeatureId)>
										<input type="hidden" name="${groupMap.productFeatureGroup.productFeatureGroupId}" value="${productFeature.productFeatureId}"/>
										${productFeature.description?if_exists}
										<#assign flag = "success">
									</#if>
								<#else>
									<#assign flag = "error">							
								</#if>
							</#list>
							<#if flag!="success">							
								<span class="tooltip">此商品在创建时该固定特征分类还未创建</span>
							</#if>
						</#if>					
					</span>
				</td>
			</tr>
		</#list>
		<#list groupMaps2 as groupMap2>
			<tr>
				<td class="label">
					<span id="EditZuczugProduct_${groupMap2.productFeatureCategory.productFeatureCategoryId}_title">可变特征-${groupMap2.productFeatureCategory.description}</span>
				</td>
				<td>
					<span class="ui-widget">
						<#if productMap.isVariant=='N'>
							<select name="productFeatureCategory_${groupMap2.productFeatureCategory.productFeatureCategoryId}" size="1">
								<option value="NONE">不选择</option>
								<#list groupMap2.productFeatures as productFeature>
									<option value="${productFeature.productFeatureId}"<#if (productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)?exists&&updateMode=="UPDATE"&&(productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)) == productFeature.productFeatureId)> selected="selected"</#if>>
										${productFeature.description?if_exists}
									</option>
								</#list>
							</select>
						<#else>
							<#list groupMap2.productFeatures as productFeature>
								<#if (productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)?exists&&updateMode=="UPDATE"&&(productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)) == productFeature.productFeatureId)>
									${productFeature.description?if_exists}
								</#if>
							</#list>
						</#if>					
					</span>
				</td>			
			</tr>
		</#list>		
		<tr>
			<#if updateMode=="CREATE">
				<td><input type="submit" value="新增"/></td>	
				
			<#else>
				<#if productMap.isVariant=='N'>
					<td><input type="submit" value="更新"/></td>		
				</#if>	
			</#if>		
		</tr>
	</tbody>
	</table>
</form>
<script>
	function validateAndSubmit(){
		var productId = $("input[name='productId']").val();
		var productName = $("input[name='productName']").val();
		var quantityUomId = $("select[name='quantityUomId'] option:selected'").val();		
		var groupId = $("input[name='groupId']").val();		
		if(productId == "" || quantityUomId == "" || productName == "") {
			alert("商品编号, 数量单位，商品名称为必填项!");
			return false;
		} else {
			return true;
		}
	}
</script>