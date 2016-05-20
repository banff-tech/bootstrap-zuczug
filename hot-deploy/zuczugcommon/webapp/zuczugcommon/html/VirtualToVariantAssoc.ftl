<script>	
	function createVariantProduct() {
		$('#EditZuczugProduct').submit();		
	}

</script>

<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="EditZuczugProduct" id="EditZuczugProduct">
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
	<table cellspacing="0" class="basic-table">
	<font color="#FF0000">${errorMsg?if_exists}</font> 
	<tbody>
		<#if productTypeId == 'RAW_MATERIAL'>
			<tr>
				<td class="label">
					<span id="EditZuczugProduct_productId_title">素然物料编号</span>
				</td>			
				<td>
					<#if productMap.isVariant=='N'>
						<input type="text" name="productZuczugId" value="">
						<span class="tooltip">非必填:如不输入则系统会自动分配物料编号</span>
					<#else>
						${zuczugId?if_exists}
					</#if>	
					
				</td>			
			</tr>
		<#else>
			<tr>
				<td class="label">
					<span id="EditZuczugProduct_productId_title">素然物料编号</span>
				</td>			
				<td>
					<#if productMap.isVariant=='N'>
						<input type="text" name="productZuczugId" value="">
						<span class="tooltip">非必填:如不输入则系统会自动分配物料编号</span>
					<#else>
						${zuczugId?if_exists}
					</#if>	
					
				</td>			
			</tr>
		</#if>		
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_internalName_title">内部名称</span>
			</td>
			<td>
				<#if productMap.isVariant=='N'>
					<input type="text" name="internalName" value="">
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
					<input type="text" name="comments" value="">
				<span class="tooltip">非必填:填写商品描述信息</span>
				<#else>
					${productMap.comments?if_exists}
				</#if>					
			</td>			
		</tr>
		<#if isShowReason?exists>
			<tr>
				<td class="label">
					<span id="EditZuczugProduct_comments_title">特征描述</span>
				</td>
				<td>
					${reason}
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
							<#list groupMap2.productFeatures as productFeature>
								<option value="${productFeature.productFeatureId}">
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
					<td><span><a href="#" class="buttontext" onclick="createVariantProduct()">生成变形商品</a></span></td>	
				</#if>							
			</#if>			
		</tr>
	</tbody>
	</table>
</form>
