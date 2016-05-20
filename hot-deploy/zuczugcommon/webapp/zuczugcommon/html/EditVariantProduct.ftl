<script>
	function checkZuczugId() {
		jQuery.ajax({				
                url: 'checkZuczugId',
                type: 'POST',
                data: {"idValue" : $("input[name='productZuczugId']").val()},
                success: function(data) {
                    
                },
                error: {
                
                }
            });
	}
</script>

<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="EditZuczugProduct" id="EditZuczugProduct">
	<input type="hidden" name="updateMode" value="${updateMode?if_exists}"/>
	<input type="hidden" name="isCheckFeatureCount" value="${isCheckFeatureCount?if_exists}"/>
	<input type="hidden" name="toVariant" value="${toVariant?if_exists}"/>
	<input type="hidden" name="toVirtual" value="${toVirtual?if_exists}"/>
	<input type="hidden" name="virtualProductId" value="${virtualProductId?if_exists}"/>
	<input type="hidden" name="productId" value="${productMap.productId?if_exists}"/>
	<input type="hidden" name="productCategoryId" value="${productCategoryId?if_exists}"/>
	<input type="hidden" name="zuczugId" value="${zuczugId?if_exists}"/>
	<input type="hidden" name="isNoVirtualNoVarient" value="${isNoVirtualNoVarient?if_exists}"/>
	<table cellspacing="0" class="basic-table">
	<font color="#FF0000">${errorMsg?if_exists}</font> 
	<tbody>
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_productId_title">素然物料编号</span>
			</td>			
			<td>
				<#if productMap.isVariant=='N'>
					<input type="text" name="productZuczugId" value="${zuczugId?if_exists}">
					<span class="tooltip">非必填:如不输入则系统会自动分配物料编号</span>
				<#else>
					${zuczugId?if_exists}
				</#if>	
				
			</td>
			
		</tr>
		
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_internalName_title">内部名称</span>
			</td>
			<td>
				<#if productMap.isVariant=='N'>
					<input type="text" name="internalName" value="${productMap.internalName?if_exists}">
					<span class="tooltip">非必填:如不填写系统则会默认生成N&A</span>
				<#else>
					${productMap.internalName?if_exists}
				</#if>				
			</td>			
		</tr>
		
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_comments_title">商品描述</span>
			</td>
			<td>
				<#if productMap.isVariant=='N'>
					<input type="text" name="comments" value="${productMap.comments?if_exists}">
				<span class="tooltip">非必填:填写商品描述信息</span>
				<#else>
					${productMap.comments?if_exists}
				</#if>	
				<#if module=='ZUCZUG'>
					<input type="hidden" name="productTypeId" id="productTypeId" value="RAW_MATERIAL"/>
				</#if>	
			</td>			
		</tr>
		<#if module=='HAZE'>
			<#assign productTypes = delegator.findByAnd('ProductType',Static["org.ofbiz.base.util.UtilMisc"].toMap())>
			<tr>
				<td class="label">
					<span>商品类型</span>
				</td>
				<td>
					<select name="productTypeId" size="1">
						<option value="NONE"></option>
						<#list productTypes as productType>
							<option value="${productType.productTypeId}">
								${productType.description?if_exists}
							</option>
						</#list>
					</select>					
				</td>			
			</tr>
		</#if>
		
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
				<td><span><a href="javascript:if(confirm('确认要创建该商品吗?'))document.EditZuczugProduct.submit();" class="buttontext" onclick="checkSubmit()">新增</a></span></td>
			<#else>
				<td></td>
			</#if>
			<#if productMap.productId?exists>
				<#if productMap.isVariant=='N'>
					<td><span><a href="javascript:document.${formAction}.submit();" class="buttontext"></a></span></td>	
				</#if>							
			</#if>			
		</tr>
	</tbody>
	</table>
</form>
