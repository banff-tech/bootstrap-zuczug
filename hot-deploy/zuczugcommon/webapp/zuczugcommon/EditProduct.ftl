<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="EditZuczugProduct">
	<input type="hidden" name="updateMode" value="${updateMode}"/>
	<input type="hidden" name="productCategoryId" value="${productMap.productCategoryId}"/>
	<input type="hidden" name="productTypeId" value="${productMap.productTypeId}"/>
	<table cellspacing="0" class="basic-table">
	<tbody>
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_productId_title">${uiLabelMap.ProductId}</span>
			</td>
			<td>
				<#if productMap.productId?exists>
					<input type="hidden" name="productId" value="${productMap.productId}"/>
					${productMap.productId}
				<#else>
					<input type="text" name="productId">
				</#if>
			</td>
		</tr>
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_internalName_title">${uiLabelMap.InternalName}</span>
			</td>
			<td><input type="text" name="internalName" value="${productMap.internalName?if_exists}"></td>
		</tr>
	<#list groupMaps as groupMap>
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_${groupMap.productFeatureGroup.productFeatureGroupId}_title">${groupMap.productFeatureGroup.description}</span>
			</td>
			<td>
				<span class="ui-widget">
					<select name="${groupMap.productFeatureGroup.productFeatureGroupId}" size="1">
					<#list groupMap.productFeatures as productFeature>
						<option value="${productFeature.productFeatureId}"<#if (updateMode=="UPDATE"&&(productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)) == productFeature.productFeatureId)> selected="selected"</#if>>
							${productFeature.description?if_exists}
						</option>
					</#list>
					</select>
				</span>
			</td>
		</tr>
	</#list>
		<tr>
			<td><span><a href="javascript:document.EditZuczugProduct.submit();" class="buttontext"><#if updateMode=="CREATE">${uiLabelMap.CommonCreate}<#else>${uiLabelMap.CommonUpdate}</#if></a></span></td>
		</tr>
	</tbody>
	</table>
</form>
