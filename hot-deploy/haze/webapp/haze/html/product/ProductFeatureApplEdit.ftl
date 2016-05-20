<form action="createVariantFromVirtual" method="post" id="newVariantForm">
	<input type="hidden" name="productId" value="${(productId)!}"/>
	<#if featureGroupList?has_content>
		<#if product?has_content && product.isVariant=="N">
		<table cellspacing="0" class="basic-table">
		    <tbody>
		    	<tr class="header-row">
		    		<td colspan="2">固定特征</td>
		    	</tr>
		    	<#list featureGroupList as featureGroupCategory>
					<#assign featureGroup = featureGroupCategory.getRelatedOne("ProductFeatureGroup")?if_exists>
		    		<#assign existFeatureInGroups = Static["com.zuczug.product.ZuczugProductUtils"].findFeatureByGroup(delegator,productId, featureGroup.productFeatureGroupId)?if_exists/>
					<#assign featureList = delegator.findByAnd("ProductFeatureGroupAndAppl", Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureGroupId",featureGroup.productFeatureGroupId))?if_exists>
					<tr>
					    <td class="label">
							<span>${(featureGroup.description)!}</span>    
						</td>
					    <td>
							<select name="${(featureGroup.productFeatureGroupId)!}" class="selectBox">
								<#if featureList?has_content>
									<#list featureList as feature>
										<option <#if existFeatureInGroups?has_content && existFeatureInGroups[0].productFeatureId==feature.productFeatureId>selected</#if> value="${(feature.productFeatureId)!}">${(feature.description)!}</option>
									</#list>
								</#if>
							</select>
					    </td>
				    </tr>
				</#list>
		    </tbody>
	    </table>
	    </#if>
    <br/><br/>
	</#if>
<#if featureCategoryList?has_content>
	<#--if 1=1-->
	<#if product?has_content && product.isVirtual=="Y">
	<table cellspacing="0" class="basic-table">
	    <tbody>
	    	<tr>
	    		<td>商品编号：</td>
	    		<td><input type="text" name="newproductId" class="required idType valid"/></td>
	    	</tr>
	    	<#list featureCategoryList as featureCategoryListAppl>
				<#assign featureCategory = featureCategoryListAppl.getRelatedOne("ProductFeatureCategory")?if_exists>
				<#assign existFeatureInCategory = Static["com.zuczug.product.ZuczugProductUtils"].findFeatureByCategory(delegator,productId, featureCategory.productFeatureCategoryId)?if_exists/>
				<#assign featureList = delegator.findByAnd("ProductFeature", Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureCategoryId",featureCategory.productFeatureCategoryId))?if_exists>
				<tr>
				    <td style="width:100px">
						<span>${(featureCategory.description)!}：</span>    
					</td>
				    <td>
						<select name="productFeatureCategory_${(featureCategory.productFeatureCategoryId)!}" class="required valid selectBox">
							<#if featureList?has_content>
								<#list featureList as feature>
									<option <#if existFeatureInCategory?has_content && existFeatureInCategory[0].productFeatureId==feature.productFeatureId>selected</#if> value="${(feature.productFeatureId)!}">${(feature.description)!}</option>
								</#list>
							</#if>
						</select>
				    </td>
			    </tr>
			</#list>
	    </tbody>
    </table>
    </#if>
    <input type="submit" class="smallSubmit" name="searchButton" value="创建变型"><span class="tooltip">会同步虚拟商品的全部信息</span>
</form>
<script>
	$(function(){
		jQuery("#newVariantForm").validate({
	     submitHandler:
	         function(form) {
	         form.submit();
	     }
	  });
	});
  
</script>
</#if>