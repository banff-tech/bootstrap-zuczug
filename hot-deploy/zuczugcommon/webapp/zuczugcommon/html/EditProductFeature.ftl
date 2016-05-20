
<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" id="EditZuczugProduct" name="EditZuczugProduct">
		<#assign product = delegator.findOne('Product',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId),true)?if_exists>
		<#assign paList = delegator.findByAnd('ProductAssoc',Static["org.ofbiz.base.util.UtilMisc"].toMap("productAssocTypeId", "UNIQUE_ITEM", "productIdTo", productId))?if_exists>
		<#if paList[0]?exists>
			<#assign productDes = delegator.findOne('Product',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", paList[0].productId),true)?if_exists>			
			<#assign uom= delegator.findOne('Uom',Static["org.ofbiz.base.util.UtilMisc"].toMap("uomId", productDes.quantityUomId),true)?if_exists>
			<#if uom?exists>
				<#assign uomDescription = uom.description/>				
			</#if>
		<#else>
			<#assign productDes = delegator.findOne('Product',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId),true)?if_exists>			
			<#assign uom= delegator.findOne('Uom',Static["org.ofbiz.base.util.UtilMisc"].toMap("uomId", productDes.quantityUomId),true)?if_exists>
			<#if productDes.quantityUomId?exists>
				<#assign uomDescription = uom.description/>				
			</#if>
		</#if>
		
		
		<#assign sellingPointAttribute = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId, "attrName", "SELLING_POINT"),true)?if_exists>
		<#assign materialFeatureAttribute = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId, "attrName", "MATERIAL_FEATURE"),true)?if_exists>
		<#assign specialPackagingAttribute = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId, "attrName", "specialPackaging"),true)?if_exists>
		<#assign sewingThreadAttribute = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId, "attrName", "sewingThread"),true)?if_exists>
		<#assign specialProcessAttribute = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId, "attrName", "specialProcess"),true)?if_exists>
		<#assign distributionModeAttribute = delegator.findOne('ProductAttribute',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId, "attrName", "DISTRIBUTION_MODE"),true)?if_exists>
		
		<#assign styleList = delegator.findByAnd('ProductDesignFeatureWithType',Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureTypeId", "STYLE", "productId", productId))?if_exists>
		<#assign orderMethodList = delegator.findByAnd('ProductDesignFeatureWithType',Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureTypeId", "ORDER_METHOD", "productId", productId))?if_exists>
		<#assign listImageTypeList = delegator.findByAnd('ProductDesignFeatureWithType',Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureTypeId", "LIST_IMAGE_TYPE", "productId", productId))?if_exists>
		
		<#assign group = delegator.findOne('ProductCategory',Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", groupInfo.groupId),true)?if_exists>		
		<#assign season = delegator.findOne('ProductCategory',Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", groupInfo.seasonId),true)?if_exists>
		<#assign series = delegator.findOne('ProductCategory',Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", groupInfo.seriesId),true)?if_exists>
		<#assign subSeries = delegator.findOne('ProductCategory',Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", groupInfo.subSeriesId),true)?if_exists>
		
		<#assign groupName = group.categoryName?if_exists />
		<#assign seasonName = season.categoryName?if_exists />
		<#assign seriesName = series.categoryName?if_exists />
		<#assign subSeriesName = subSeries.categoryName?if_exists/>
		
		
		
		<#if styleList[0]?exists>
			<#assign styleId= styleList[0].productFeatureId />
			<#assign styleFeature = delegator.findOne('ProductFeature',Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureId", styleId),true)?if_exists>
			<#assign styleDescription = styleFeature.productFeatureId + "-" + styleFeature.description/>
		</#if>
		<#if orderMethodList[0]?exists>
			<#assign orderMethodId= orderMethodList[0].productFeatureId />	
		</#if>
		<#if listImageTypeList[0]?exists>	
			<#assign listImageTypeId= listImageTypeList[0].productFeatureId />	
		</#if>	
				
		<#if distributionModeAttribute.attrValue?exists>
			<#assign distributionMode= distributionModeAttribute.attrValue />
		</#if>
		<#if specialProcessAttribute.attrValue?exists>
			<#assign specialProcess= specialProcessAttribute.attrValue />
		</#if>
		<#if sewingThreadAttribute.attrValue?exists>
			<#assign sewingThread= sewingThreadAttribute.attrValue />
		</#if>
		<#if specialPackagingAttribute.attrValue?exists>
			<#assign specialPackaging= specialPackagingAttribute.attrValue />
		</#if>
		<#if materialFeatureAttribute.attrValue?exists>
			<#assign materialFeature= materialFeatureAttribute.attrValue />
		</#if>
		<#if sellingPointAttribute.attrValue?exists>
			<#assign sellingPoint= sellingPointAttribute.attrValue />
		</#if>
		
		
	 <input type="hidden" name="productId" value="${productId?if_exists}">
	 <input type="hidden" name="productCategoryId" value="${productCategoryId?if_exists}">	
	 <table cellspacing="0" class="basic-table">
		</tbody>
			<tr>
			    <td class="label">
					<span id="EditProductDesign_productId_title">款号编码</span>    </td>
			    <td colspan="4">
					${productId?if_exists}				   
				</td>
		    </tr>	
		    <tr>
	    		<td class="label">
					<span id="EditProductDesign_quantityUomId_title">数量单位</span>   
				</td>
			    <td>		
					 ${uomDescription?if_exists}
				</td>
			    <#assign productCategory = delegator.findOne("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", parameters.productCategoryId),true)>				
				<td class="label">
					<span id="EditAccessoryProduct_primaryProductCategoryId_title">物料分类</span>    
				</td>
			    <td>
					${productCategory.categoryName?if_exists}    
				</td>
		    </tr>	
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_groupId_title">组别</span>    </td>
			    <td>
			    	<@htmlTemplate.lookupField formName="EditZuczugProduct" name="groupId" fieldFormName="lookupProdGroupName" value="${groupName?if_exists}"/>
					
				</td>
			    <td class="label">
					<span id="EditProductDesign_seasonId_title">季节</span>    </td>
			    <td>
					${seasonName?if_exists}
			   </td>
		    </tr>  
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_seriesId_title">系列</span>    </td>
			    <td>
					${seriesName?if_exists}
			    </td>
			    <td class="label">
					<span id="EditProductDesign_subSeriesId_title">子系列</span>    </td>
			    <td>
					${subSeriesName?if_exists}
			    </td>
	        </tr> 
	        
	        <tr>
			    <td class="label">
					<span id="EditProductDesign_styleId_title">款型</span>    </td>
			    <td>
			    	${styleDescription?if_exists}
				</td>
				<#assign orderMethodFeatureList = delegator.findByAnd('ProductFeature',Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureTypeId", "ORDER_METHOD"))?if_exists>
				
			    <td class="label">
					<span id="EditProductDesign_orderMethodId_title">配订方式</span>    </td>
			    <td>
					<span class="ui-widget">
						<select name="orderMethodId" id="EditProductDesign_orderMethodId" size="1">
							<option value=""></option> 
							<#list orderMethodFeatureList as orderMethodFeature>
								<option value="${orderMethodFeature.productFeatureId}" <#if orderMethodId?exists && orderMethodId==orderMethodFeature.productFeatureId>selected="selected"</#if>>${orderMethodFeature.description}</option> 
							</#list>
						</select>
					</span>
			    </td>
		    </tr> 
		    <#assign listImageTypeFeatureList = delegator.findByAnd('ProductFeature',Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureTypeId", "LIST_IMAGE_TYPE"))?if_exists>
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_listImageTypeId_title">列表显示方式</span>    </td>
			    <td>
					<span class="ui-widget">
						<select name="listImageTypeId" id="EditProductDesign_listImageTypeId" size="1"> 
							<option value=""></option> 
							<#list listImageTypeFeatureList as listImageTypeFeature>
								<option value="${listImageTypeFeature.productFeatureId}" <#if listImageTypeId?exists && listImageTypeId==listImageTypeFeature.productFeatureId>selected="selected"</#if>>${listImageTypeFeature.description}</option> 
							</#list>
						</select>
					</span>
			    </td>
			    <td class="label">
					<span class="integerNull" id="EditProductDesign_iPadMemCode_title">iPad助记码</span>    </td>
			    <td>
					<input type="text" name="iPadMemCode" class="integerNull" size="25" maxlength="5" id="EditProductDesign_iPadMemCode" value="${Static["com.zuczug.product.ZuczugProductUtils"].getProductGoodIdentificationCode(delegator,productId,'IPAD_MEM_CODE')}">
			    </td>
		    </tr>
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_productName_title">款号商品名称</span>    
				</td>
			    <td>
					<input type="text" name="productName" size="25" id="EditProductDesign_productName" value="${product.productName?if_exists}">
			    </td>
			    <td class="label">
					<span id="EditProductDesign_description_title">规格</span>    
				</td>
			    <td>
					<input type="text" name="description" size="25" id="EditProductDesign_description" value="${product.description?if_exists}">
			    </td>
		    </tr>
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_sewingThread_title">缝纫线要求</span>    </td>
			    <td>
			    	<textarea name="sewingThread" cols="30" rows="3" id="EditProductDesign_sellingPoint">${sewingThread?if_exists}</textarea>					
				</td>
			    <td class="label">
					<span id="EditProductDesign_specialProcess_title">特殊工艺</span>    </td>
			    <td>
			    	<textarea name="specialProcess" cols="30" rows="3" id="EditProductDesign_sellingPoint">${specialProcess?if_exists}</textarea>								    						
				</td>
		    </tr>
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_specialPackaging_title">特殊包装</span>    </td>
			    <td>
			    	<textarea name="specialPackaging" cols="30" rows="3" id="EditProductDesign_sellingPoint">${specialPackaging?if_exists}</textarea>	
				</td>
			    <td class="label">
					<span id="EditProductDesign_distributionMode_title">经销方式</span>    </td>
			    <td>
			    	<textarea name="distributionMode" cols="30" rows="3" id="EditProductDesign_sellingPoint">${distributionMode?if_exists}</textarea>	
					
				</td>
	    	</tr>
	    	<tr>
			    <td class="label">
					<span id="EditProductDesign_sellingPoint_title">卖点介绍</span>    </td>
			    <td>
					<textarea name="sellingPoint" cols="30" rows="3" id="EditProductDesign_sellingPoint">${sellingPoint?if_exists}</textarea>		
			    </td>
			    <td class="label">
					<span id="EditProductDesign_materialFeature_title">材质特点</span>    </td>
			    <td>
					<textarea name="materialFeature" cols="30" rows="3" id="EditProductDesign_materialFeature">${materialFeature?if_exists}</textarea>		
			    </td>
		    </tr>
		    <tr>
			    <td class="label">
					<span id="EditProductDesign_comments_title">备注</span>    </td>
			    <td>
					<textarea name="comments" cols="30" rows="3" id="EditProductDesign_comments">${product.comments?if_exists}</textarea>		
			    </td>
			    <td class="label">
					<span id="EditProductDesign_longDescription_title">描述</span>    </td>
			    <td>
					<textarea name="longDescription" cols="30" rows="3" id="EditProductDesign_longDescription">${product.longDescription?if_exists}</textarea>		
			    </td>
		    </tr>
		    
	    	<tr>
			    <td class="label">
					<span id="EditProductDesign_designProductId_title">特征维护</span>   
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
								<option value="${productFeature.productFeatureId}"<#if (productMap.get(groupMap.productFeatureGroup.productFeatureGroupId)?if_exists == productFeature.productFeatureId)> selected="selected"</#if>>
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
		<#if (product.isVariant=="Y")>
			<#if groupMaps2?exists>
				<#list groupMaps2 as groupMap2>
					<tr>
						<td class="label">
							<span id="EditZuczugProduct_${groupMap2.productFeatureCategory.productFeatureCategoryId}_title">${groupMap2.productFeatureCategory.description}</span>
						</td>
						<td>
							<span class="ui-widget">				
									<#list groupMap2.productFeatures as productFeature>
										<#if (productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)?exists&&updateMode=="UPDATE"&&(productMap.get("productFeatureCategory_" + groupMap2.productFeatureCategory.productFeatureCategoryId)) == productFeature.productFeatureId)>
											${productFeature.description?if_exists}
										</#if>
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
		</tbody>
	</table>
</form>
