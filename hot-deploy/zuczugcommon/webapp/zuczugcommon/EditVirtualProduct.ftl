<form method="post" action="<@ofbizUrl>${(formAction)!}</@ofbizUrl>" name="EditZuczugProduct">
	<input type="hidden" name="updateMode" value="${updateMode}"/>
	<input type="hidden" name="productCategoryId" value="${productMap.productCategoryId}"/>
	<input type="hidden" name="productTypeId" value="${productMap.productTypeId}"/>
	<input type="hidden" name="productId" value="${(productId)!}"/>
	<table cellspacing="0" class="basic-table">
	<tbody>
		<#if (!hideProductInfo?has_content) || (hideProductInfo?has_content && hideProductInfo=="N")><!-- 如果这个参数不传，就显示，如果传Y，就隐藏 -->
		<tr>
			<td class="label">
				<span id="EditZuczugProduct_productId_title">内部编号</span>
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
				<span id="EditZuczugProduct_internalName_title">内部名称</span>
			</td>
			<td>${productMap.internalName?if_exists}</td>
		</tr>
		</#if>
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
			<td><span><a href="javascript:document.EditZuczugProduct.submit();" class="buttontext"><#if updateMode=="CREATE">新增<#else>更新</#if></a></span></td>
		</tr>
	</tbody>
	</table>
</form>
