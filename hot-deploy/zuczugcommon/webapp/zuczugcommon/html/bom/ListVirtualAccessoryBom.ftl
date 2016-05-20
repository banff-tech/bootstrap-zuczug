<script>
	function inputValue(idValue) {
		var instruction = $("#ListVirtualAccessoryBom_Text_Instruction_o_" + idValue).attr("value");
		var quantity = $("#ListVirtualAccessoryBom_Text_Quantity_o_" + idValue).attr("value");
		var reason = $("#ListVirtualAccessoryBom_Text_Reason_o_" + idValue).attr("value");
		if (instruction!=null) {
			$("#ListVirtualAccessoryBom_instruction_o_" + idValue).val(instruction);
		}
		if (quantity!=null) {
			$("#ListVirtualAccessoryBom_quantity_o_" + idValue).val(quantity);
		}
		
		if (reason!=null) {
			$("#ListVirtualAccessoryBom_reason_o_" + idValue).val(reason);
		}
		
		
	}
</script>
<!-- 查询当前登陆者的权限 -->
<#assign partyRoles = delegator.findByAnd('PartyRole', Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", userLogin.partyId))/>
<#list partyRoles as partyRole>
	<!-- 设计 -->
	<#if partyRole.roleTypeId=="DESIGNER">
		<#assign roleType = "DESIGNER"/>
		<#break/>
	</#if>
	<!-- 制版 -->
	<#if partyRole.roleTypeId=="PATTERN">
		<#assign roleType = "PATTERN"/>	
		<#break/>
	</#if>
	<!-- 生产 -->
	<#if partyRole.roleTypeId=="WORKER">
		<#assign roleType = "WORKER"/>
		<#break/>	
	</#if>
	<!-- 采购 -->
	<#if partyRole.roleTypeId=="BUYER">
		<#assign roleType = "BUYER"/>	
		<#break/>
	</#if>
</#list>
<#if roleType = "PATTERN">
	<#assign bomVirtualProductNoClassifyList = delegator.findByAnd('BomDetailsView',Static["org.ofbiz.base.util.UtilMisc"].toMap("variantProductId", variantProductId, "isVirtual", "Y"))/>
<#else>
	<#assign bomVirtualProductNoClassifyList = Static["org.ofbiz.entity.util.EntityUtil"].filterByDate(delegator.findByAnd('BomDetailsView',Static["org.ofbiz.base.util.UtilMisc"].toMap("variantProductId", variantProductId, "isVirtual", "Y")))/>	
</#if>
<#if bomVirtualProductNoClassifyList?exists>
	<#assign result = dispatcher.runSync("classifyProductByCategory", Static["org.ofbiz.base.util.UtilMisc"].toMap("fromProductList", bomVirtualProductNoClassifyList)) />
</#if>
<#assign bomVirtualProductList = result.get("listIt")/>
<div id="screenlet_2_col" class="screenlet-body">
	<table cellspacing="0" class="basic-table hover-bar">
    	<tbody>
    		<tr class="header-row-2" style="vertical-align:middle; text-align:center;">
    			<td width="100">
					<span id="ListVirtualAccessoryBom_idValue_title">物料编号</span>
				</td>
				<td>
					<span id="ListVirtualAccessoryBom_internalName_title">物料名称</span>
    			</td>
			    <td width="300">
					<span id="ListVirtualAccessoryBom_description_title">物料规格</span>
			    </td>
			    <td width="50">
					<span id="ListVirtualAccessoryBom_primaryProductCategoryId_title" >物料分类</span>
			    </td>
			    <td>
					<span id="ListVirtualAccessoryBom_comments_title">物料描述</span>
			    </td>
			    <td>
					<span id="ListVirtualAccessoryBom_lastUpdatedStamp_title">物料更新时间</span>
			    </td>
			    <#if roleType = "PATTERN">
			    	<td>
						<span id="ListVirtualAccessoryBom_thruDate_title">结束日期</span>
					</td>
			    </#if>
			    <td>
					<span id="ListVirtualAccessoryBom_instruction_title">说明</span>
			    </td>
			    <td width="30">
					<span id="ListVirtualAccessoryBom_quantity_title">用量</span>
			    </td>
			    <#if roleType?exists>
					<#if roleType = "BUYER">
						<td width="50">
							<span id="ListVirtualAccessoryBom_更新_title">变形</span>
					    </td>
					</#if>
				</#if>
				<#if roleType?exists>
					<#if roleType != "WORKER">
					    <td width="50">
							<span id="ListVirtualAccessoryBom_更新_title">更新</span>
					    </td>
			    	</#if>
			    </#if>
			    <#if roleType?exists>
					<#if roleType != "WORKER" >
					    <td width="50">
							<span id="ListVirtualAccessoryBom_remove_title">删除</span>
					    </td>
				    </#if>
			    </#if>
    		</tr>
    		<#list bomVirtualProductList as bomVirtualProduct>    			
    			<#assign idValue = delegator.findByPrimaryKey('GoodIdentification',Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", bomVirtualProduct.productIdTo, "goodIdentificationTypeId", "ZUCZUG_CODE"))/>
				<#if bomVirtualProduct.isVariant=="N">
					<#assign productCategoryId = bomVirtualProduct.primaryProductCategoryId/>
				<#else>
					<#assign productAssoc = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(delegator.findByAnd("ProductAssoc", Static["org.ofbiz.base.util.UtilMisc"].toMap("productIdTo", bomVirtualProduct.productIdTo, "productAssocTypeId", "PRODUCT_VARIANT")))/>
					<#assign virtualProduct = delegator.findByPrimaryKey("Product", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productAssoc.productId))/>
					<#assign productCategoryId = virtualProduct.primaryProductCategoryId/>					
				</#if>	
				<#assign productCategory = delegator.findByPrimaryKey('ProductCategory',Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", productCategoryId))/>	   			
				   			
	   			<tr>
	    			<td>${idValue.idValue?if_exists}</td>
	    			<td>${bomVirtualProduct.internalName?if_exists}</td>
	    			<td class="tableTdDescription">
	    				<#if bomVirtualProduct.reason?exists>
	    					<#assign reason=bomVirtualProduct.reason/>
	    				<#else>
	    					<#assign reason="无"/>
	    				</#if>
	    				<#if roleType?exists>
							<#if roleType = "DESIGNER" || roleType = "PATTERN" && isBomConfirm=='false'>
								${bomVirtualProduct.description?if_exists}	
	    						<input type="text" name="text_Reason" id="ListVirtualAccessoryBom_Text_Reason_o_${idValue.idValue}" size="40" value="${reason}">		    	
							<#else>
								${bomVirtualProduct.description?if_exists}
								</br><b><h2>特征描述：	${reason}</h2></b>
							</#if>
						<#else>
							${bomVirtualProduct.description?if_exists}	
							</br><b><h2>特征描述：	${reason}</h2></b>
						</#if>	
	    			</td>
				    <td style="vertical-align:middle; text-align:center;"><#if productCategory?exists>${productCategory.categoryName?if_exists}<#else>无分类</#if></td>
				    <td>${bomVirtualProduct.comments?if_exists}</td>
				    <td>${bomVirtualProduct.lastUpdatedStamp?if_exists}</td>
				    <#if roleType = "PATTERN">
				    	<td style="color:red">${bomVirtualProduct.thruDate?if_exists}</td>
				    </#if>
				    <td>
				    	<#if roleType?exists>
							<#if roleType != "WORKER" && isBomConfirm=='false'>
					    		<input type="text" name="text_Instruction" value="${bomVirtualProduct.instruction?if_exists}" size="25" id="ListVirtualAccessoryBom_Text_Instruction_o_${idValue.idValue}">
					    	<#else>
					    		${bomVirtualProduct.instruction?if_exists}
					    	</#if>
					    <#else>
					    		${bomVirtualProduct.instruction?if_exists}
						</#if>
				    </td>
					<td style="vertical-align:middle; text-align:center;">
						<#if roleType?exists>
							<#if roleType = "PATTERN" && isBomConfirm=='false'>
								<input type="text" name="quantity" id="ListVirtualAccessoryBom_Text_Quantity_o_${idValue.idValue}" size="5" value="${bomVirtualProduct.quantity?if_exists}">
							<#else>
								${bomVirtualProduct.quantity?if_exists}
							</#if>
						<#else>
							${bomVirtualProduct.quantity?if_exists}
						</#if>	
					</td>
					<#if roleType?exists>
						<#if roleType = "BUYER" && isBomConfirm=='false'>
							<td width="50">
								<form method="post" action="<@ofbizUrl>CreateVariantProductToBom</@ofbizUrl>" onsubmit="inputValue('${idValue.idValue}')" name="ListVirtualAccessoryBom_CreateVariant_${idValue.idValue}">
								    <input type="hidden" name="bomId" value="${productId?if_exists}" id="ListVirtualAccessoryBom_bomId_o_${idValue.idValue}">								    
								    <input type="hidden" name="productId" value="${bomVirtualProduct.productIdTo?if_exists}" id="ListVirtualAccessoryBom_productId_o_${idValue.idValue}">								    
								    <input type="hidden" name="productCategoryId" value="${productCategory.productCategoryId?if_exists}" id="ListVirtualAccessoryBom_productCategoryId_o_${idValue.idValue}">
								    <input type="hidden" name="idValue" value="${idValue.idValue?if_exists}" id="ListVirtualAccessoryBom_idValue_o_${idValue.idValue}">
								    <input type="hidden" name="internalName" value="${bomVirtualProduct.internalName?if_exists}" id="ListVirtualAccessoryBom_internalName_o_${idValue.idValue}">
								    <input type="hidden" name="description" value="${bomVirtualProduct.description?if_exists}" id="ListVirtualAccessoryBom_description_o_${idValue.idValue}">
								    <input type="hidden" name="primaryProductCategoryId" value="${bomVirtualProduct.primaryProductCategoryId?if_exists}" id="ListVirtualAccessoryBom_primaryProductCategoryId_o_${idValue.idValue}">
								    <input type="hidden" name="comments" value="${bomVirtualProduct.comments?if_exists}" id="ListVirtualAccessoryBom_comments_o_${idValue.idValue}">
								    <input type="hidden" name="lastUpdatedStamp" value="${bomVirtualProduct.lastUpdatedStamp?if_exists}" id="ListVirtualAccessoryBom_lastUpdatedStamp_o_${idValue.idValue}">
								    <input type="hidden" name="productTypeId" value="${bomVirtualProduct.productTypeId?if_exists}" id="ListVirtualAccessoryBom_productTypeId_o_${idValue.idValue}">
								    <input type="hidden" name="variantProductId" value="${variantProductId?if_exists}" id="ListVirtualAccessoryBom_variantProductId_o_${idValue.idValue}">						    
								    <input type="hidden" name="colorId" value="${colorId?if_exists}" id="ListVirtualAccessoryBom_colorId_o_${idValue.idValue}">
								    <input type="hidden" name="sizeId" value="${sizeId?if_exists}" id="ListVirtualAccessoryBom_sizeId_o_${idValue.idValue}">
								    <input type="hidden" name="productIdTo" value="${bomVirtualProduct.productIdTo?if_exists}" id="ListVirtualAccessoryBom_productIdTo_o_${idValue.idValue}">						   
								    <input type="hidden" name="isUpdateQuantity" value="${isUpdateQuantity?if_exists}" id="ListVirtualAccessoryBom_isUpdateQuantity_o_${idValue.idValue}">
								    <input type="hidden" name="fromDate" value="${bomVirtualProduct.fromDate?if_exists}" id="ListVirtualAccessoryBom_fromDate_o_${idValue.idValue}">						   
								    <input type="hidden" name="thruDate" value="${bomVirtualProduct.thruDate?if_exists}" id="ListVirtualAccessoryBom_thruDate_o_0">
						    		<input type="hidden" name="quantity" value="${bomVirtualProduct.quantity?if_exists}" id="ListVirtualAccessoryBom_quantity_o_${idValue.idValue}">
								    <input type="hidden" name="instruction" value="${bomVirtualProduct.instruction?if_exists}" id="ListVirtualAccessoryBom_instruction_o_${idValue.idValue}">						    
									<input type="hidden" name="reason" value="${bomVirtualProduct.reason?if_exists}" id="ListVirtualAccessoryBom_reason_o_${idValue.idValue}">						    							
									<input type="submit" name="变型" value="变型">						    							
								</form>
						    </td>
						</#if>
					</#if>
					
					<#if roleType?exists>
						<#if roleType != "WORKER" && isBomConfirm=='false'>
						    <td>
								<form method="post" action="<@ofbizUrl>updateInstruction</@ofbizUrl>" onsubmit="inputValue('${idValue.idValue}')" name="ListVirtualAccessoryBom_o_${idValue.idValue}">
								    <input type="hidden" name="productId" value="${productId?if_exists}" id="ListVirtualAccessoryBom_productId_o_${idValue.idValue}">
								    <input type="hidden" name="idValue" value="${idValue.idValue?if_exists}" id="ListVirtualAccessoryBom_idValue_o_${idValue.idValue}">
								    <input type="hidden" name="internalName" value="${bomVirtualProduct.internalName?if_exists}" id="ListVirtualAccessoryBom_internalName_o_${idValue.idValue}">
								    <input type="hidden" name="description" value="${bomVirtualProduct.description?if_exists}" id="ListVirtualAccessoryBom_description_o_${idValue.idValue}">
								    <input type="hidden" name="primaryProductCategoryId" value="${bomVirtualProduct.primaryProductCategoryId?if_exists}" id="ListVirtualAccessoryBom_primaryProductCategoryId_o_${idValue.idValue}">
								    <input type="hidden" name="comments" value="${bomVirtualProduct.comments?if_exists}" id="ListVirtualAccessoryBom_comments_o_${idValue.idValue}">
								    <input type="hidden" name="lastUpdatedStamp" value="${bomVirtualProduct.lastUpdatedStamp?if_exists}" id="ListVirtualAccessoryBom_lastUpdatedStamp_o_${idValue.idValue}">
								    <input type="hidden" name="productTypeId" value="${bomVirtualProduct.productTypeId?if_exists}" id="ListVirtualAccessoryBom_productTypeId_o_${idValue.idValue}">
								    <input type="hidden" name="variantProductId" value="${variantProductId?if_exists}" id="ListVirtualAccessoryBom_variantProductId_o_${idValue.idValue}">						    
								    <input type="hidden" name="colorId" value="${colorId?if_exists}" id="ListVirtualAccessoryBom_colorId_o_${idValue.idValue}">
								    <input type="hidden" name="sizeId" value="${sizeId?if_exists}" id="ListVirtualAccessoryBom_sizeId_o_${idValue.idValue}">
								    <input type="hidden" name="productIdTo" value="${bomVirtualProduct.productIdTo?if_exists}" id="ListVirtualAccessoryBom_productIdTo_o_${idValue.idValue}">						   
								    <input type="hidden" name="isUpdateQuantity" value="${isUpdateQuantity?if_exists}" id="ListVirtualAccessoryBom_isUpdateQuantity_o_${idValue.idValue}">
								    <input type="hidden" name="fromDate" value="${bomVirtualProduct.fromDate?if_exists}" id="ListVirtualAccessoryBom_fromDate_o_${idValue.idValue}">						   
								    <input type="hidden" name="thruDate" value="${bomVirtualProduct.thruDate?if_exists}" id="ListVirtualAccessoryBom_thruDate_o_0">
								    <input type="hidden" name="quantity" value="${bomVirtualProduct.quantity?if_exists}" id="ListVirtualAccessoryBom_quantity_o_${idValue.idValue}">
								    <input type="hidden" name="instruction" value="${bomVirtualProduct.instruction?if_exists}" id="ListVirtualAccessoryBom_instruction_o_${idValue.idValue}">						    
									<input type="hidden" name="reason" value="${bomVirtualProduct.reason?if_exists}" id="ListVirtualAccessoryBom_reason_o_${idValue.idValue}">						    							
									<input type="submit" name="更新" value="更新">						    							
								</form>
							</td>
						</#if>
					</#if>
					<#if roleType?exists>
						<#if roleType != "WORKER" && isBomConfirm=='false'>
						    <td>
								<form method="post" action="<@ofbizUrl>removeAccessoryBom</@ofbizUrl>" onsubmit="javascript:submitFormDisableSubmits(this)" name="ListVirtualAccessoryBom_o_${idValue.idValue}_o_remove">
									<input type="hidden" name="productId" value="${productId?if_exists}" id="ListVirtualAccessoryBom_productId_o_${idValue.idValue}">
								    <input type="hidden" name="idValue" value="${idValue.idValue?if_exists}" id="ListVirtualAccessoryBom_idValue_o_${idValue.idValue}">
								    <input type="hidden" name="internalName" value="${bomVirtualProduct.internalName?if_exists}" id="ListVirtualAccessoryBom_internalName_o_${idValue.idValue}">
								    <input type="hidden" name="description" value="${bomVirtualProduct.description?if_exists}" id="ListVirtualAccessoryBom_description_o_${idValue.idValue}">
								    <input type="hidden" name="primaryProductCategoryId" value="${bomVirtualProduct.primaryProductCategoryId?if_exists}" id="ListVirtualAccessoryBom_primaryProductCategoryId_o_${idValue.idValue}">
								    <input type="hidden" name="comments" value="${bomVirtualProduct.comments?if_exists}" id="ListVirtualAccessoryBom_comments_o_${idValue.idValue}">
								    <input type="hidden" name="lastUpdatedStamp" value="${bomVirtualProduct.lastUpdatedStamp?if_exists}" id="ListVirtualAccessoryBom_lastUpdatedStamp_o_${idValue.idValue}">
								    <input type="hidden" name="productTypeId" value="${bomVirtualProduct.productTypeId?if_exists}" id="ListVirtualAccessoryBom_productTypeId_o_${idValue.idValue}">
								    <input type="hidden" name="variantProductId" value="${variantProductId?if_exists}" id="ListVirtualAccessoryBom_variantProductId_o_${idValue.idValue}">						    
								    <input type="hidden" name="colorId" value="${colorId?if_exists}" id="ListVirtualAccessoryBom_colorId_o_${idValue.idValue}">
								    <input type="hidden" name="sizeId" value="${sizeId?if_exists}" id="ListVirtualAccessoryBom_sizeId_o_${idValue.idValue}">
								    <input type="hidden" name="productIdTo" value="${bomVirtualProduct.productIdTo?if_exists}" id="ListVirtualAccessoryBom_productIdTo_o_${idValue.idValue}">						   
								    <input type="hidden" name="isUpdateQuantity" value="${isUpdateQuantity?if_exists}" id="ListVirtualAccessoryBom_isUpdateQuantity_o_${idValue.idValue}">
								    <input type="hidden" name="fromDate" value="${bomVirtualProduct.fromDate?if_exists}" id="ListVirtualAccessoryBom_fromDate_o_${idValue.idValue}">						   
								    <input type="hidden" name="thruDate" value="${bomVirtualProduct.thruDate?if_exists}" id="ListVirtualAccessoryBom_thruDate_o_0">
								    <input type="hidden" name="quantity" value="${bomVirtualProduct.quantity?if_exists}" id="ListVirtualAccessoryBom_quantity_o_${idValue.idValue}">
								    <input type="hidden" name="instruction" value="${bomVirtualProduct.instruction?if_exists}" id="ListVirtualAccessoryBom_instruction_o_${idValue.idValue}">						    
									<input type="hidden" name="reason" value="${bomVirtualProduct.reason?if_exists}" id="ListVirtualAccessoryBom_reason_o_${idValue.idValue}">			
									<input type="submit" name="删除" value="删除">
								</form>
							</td>
						</#if>
					</#if>
	    		</tr>
    		</#list>   
		</tbody>
	</table>
</div>