<#assign isSaveHeader=cart.getOrderAttribute('FABRIC_PURCHASE_TYPE')?if_exists/>
<#-- 订单头信息 -->
<#if cartItems?has_content>
<a href="javascript:$('#headerForm').submit();" onclick="$('#HEADER_MODE').val('SAVE');return confirm('${uiLabelMap.ConfirmSave}')" class="buttontext">${uiLabelMap.CommitPurchase}</a>
</#if>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.AddFacbricPurchase}
            </li>
            <li><a href="<@ofbizUrl>emptycart</@ofbizUrl>" onclick="return confirm('${uiLabelMap.ClearOrderConfrim}')">${uiLabelMap.ClearOrder}</a></li>
        </ul>
        <br class="clear">
    </div>
    
    <#--
    订单备注
    订单项的单价
    最后一行添加总计
    显示币种
    -->
    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>addFabricPurchaseData</@ofbizUrl>" id="headerForm" name="headerForm">
   		<input type="hidden" name="purchaseType" value="${purchaseType}"/>
   		<input type="hidden" id="HEADER_MODE" name="MODE" value="HEADER_INFO"/>
    	<#include "component://accessoriespurchase/webapp/accessoriespurchase/html/purchase/fabricAccessoryPurchaseHeader.ftl"> 
	</form>
    </div>
</div>

<#-- 添加订单项部分 -->
<#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.AddProductItem}
            </li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>addFabricPurchaseData</@ofbizUrl>" id="addItemForm" name="addItemForm">
    	<input type="hidden" name="MODE" value="ADD_ITEM"/>
    	<input type="hidden" name="purchaseType" value="${purchaseType}"/>
    	
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">${uiLabelMap.SelectProduct}：</td>
	            <td>
	            	<#if "ACCESSORY_PURCHASE" == purchaseType >
	            			<@htmlTemplate.lookupField formName="addItemForm" name="idValue" fieldFormName="lookupVirtualProductByParty?supplierId=${(supplierId)!}" className="required" size="20" maxlength="40"/>	            		
	            	<#else>
	            			<@htmlTemplate.lookupField formName="addItemForm" name="productId" fieldFormName="lookupRealProductNotVirtual" className="required" size="20" maxlength="40"/>	            		
	            	</#if>
	            </td>
	            <td class="label">${uiLabelMap.Quantity}：</td>
	            <td><input type="text" name="quantity" class="required integerNumber"/></td>
	            <td class="label">${uiLabelMap.UnitPrice}：</td>
	            <td><input type="text" name="unitPrice" class="required integerNumber"/></td>
	        </tr>
	        <tr>
		        <td class="label">${uiLabelMap.EstimatedShipDate}：</td>
		        <td>
		            <@htmlTemplate.renderDateTimeField name="estimatedShipDate" event="" action="" value="${(session.getAttribute('estimatedShipDateStr'))!}" className="" alert="" title="" size="25" maxlength="30" id="estimatedShipDate" dateType="date" shortDateInput=true timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
		        	<script>
		        	$(function(){
		        		$("#estimatedShipDate_i18n").addClass("required");
		        	});
		        	</script>
		        </td>
		        <td class="label">&nbsp;</td>
		        <td>&nbsp;</td>
		    </tr>
	        <tr>
		        <td class="label">
					<input type="submit" value="${uiLabelMap.CommonAdd}"/>
				</td>
	        </tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>


<#-- 添加服务项部分 -->
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.ButStockNumber}
            </li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>addFabricPurchaseData</@ofbizUrl>" id="addServiceForm" name="addServiceForm">
    	<input type="hidden" name="MODE" value="ADD_SERVICE"/>
    	<input type="hidden" name="purchaseType" value="${purchaseType}"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">${uiLabelMap.SelectAService}：</td>
	            <td>
	            	<select name="productId">
	            		<option>-${uiLabelMap.PleaseSelectA}-</option>
	            		<#if serviceList?has_content>
	            		<#list serviceList as product>
	            			<option value="${(product.productId)!}">${(product.productName)!}</option>
	            		</#list>
	            		</#if>
	            	</select>
	            </td>
	            <td class="label">${uiLabelMap.TheAmountOf}：</td>
	            <td>
	            	<input type="text" name="unitPrice" class="required integerNumber"/>
	            	<input type="hidden" name="quantity" value="1"/>
	            </td>
	            <td class="label">
					<input type="submit" value="${uiLabelMap.CommonAdd}"/>
				</td>
	        </tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>


<#-- 已经添加的订单项目 -->
<div class="screenlet">
	<div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.CartItemList}</li>
            <li><a href="javascript:$('#recalculatedForm').submit()">${uiLabelMap.Recalculated}</a></li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>RecalculatedFabricPurchaseOrder</@ofbizUrl>" id="recalculatedForm">
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
		        <td>${uiLabelMap.FabricProductId}</td>
		        <td width="200">${uiLabelMap.FabricInternalName}</td>
		        <td width="200">${uiLabelMap.FabricComponentId}</td>
	            <td>${uiLabelMap.Quantity}</td>
	            <td>${uiLabelMap.CurrencyUomId}</td>
	            <td>${uiLabelMap.ProductLastPrice}</td>
	            <td>${uiLabelMap.ExpectedArrivalDate}</td>
	            <td>${uiLabelMap.Sumtotal}</td>
	        </tr>
	        <#if cartItems?has_content>
	        <#assign totalQuantity=0 />
	        <#assign totalAmount=0 />
	        <#assign alt_row = false>
	        <#list cartItems as item>
	        	<#assign product = delegator.findByPrimaryKey("Product", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",item.getProductId()))?if_exists>
		        <tr <#if alt_row> class="alternate-row"</#if>>
		        	<td>${(item.getProduct().productId)!}</td>
			        <td>${(product.internalName)!}</td>
			        <td>${(product.description)!}</td>
		            <td>
		            	<input style="width:80px" type="text" id="quantity_${item_index}" name="quantity" value="${(item.getQuantity())!}" class="integernumber">
		            	<input type="hidden" name="itemIndex" value="${(cart.getItemIndex(item))!}"/>
		            </td>
		            <td>${(cart.getCurrency())!}</td>
		            <td><input style="width:80px" type="text" id="unitPrice_${item_index}" name="unitPrice" value="${(item.getBasePrice())!}" class="integernumber"/></td>
		            <td>
		            	<#if item.getEstimatedShipDate()?has_content>
		            		<@htmlTemplate.renderDateTimeField name="estimatedShipDate" event="" action="" value="${item.getEstimatedShipDate()?string('yyyy-MM-dd')}" className="" alert="" title="" size="25" maxlength="30" id="estimatedShipDate_${item_index}" dateType="date" shortDateInput=true timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
		            	<#else>
		            		-
		            	</#if>
		            </td>
		            <td><@ofbizCurrency amount=item.getQuantity()*item.getBasePrice() isoCode=cart.getCurrency()/></td>
		            <#assign totalAmount=totalAmount+item.getQuantity()*item.getBasePrice() />
		            <#assign totalQuantity=totalQuantity+item.getQuantity() />
		        </tr>
		       	<#assign alt_row = !alt_row>
	        </#list>
	        </#if>
	        	<tr>
	        		<td>-</td>
	        		<td>-</td>
	        		<td>${uiLabelMap.Sumtotal}：</td>
	        		<td>${(totalQuantity)!}</td>
	        		<td>-</td>
	        		<td>-</td>
	        		<td>-</td>
	        		<td><@ofbizCurrency amount=totalAmount isoCode=cart.getCurrency()/></td>
	        	</tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>
</#if><!-- 验证是否有订单头信息 -->

<script>
$(function(){
	jQuery("#headerForm").validate({
	     submitHandler:
	         function(form) {
	         form.submit();
	     }
	});
	
	jQuery("#addItemForm").validate({
	     submitHandler:
	         function(form) {
	         form.submit();
	     }
	});
	
	jQuery("#addServiceForm").validate({
	     submitHandler:
	         function(form) {
	         form.submit();
	     }
	});
	
	jQuery("#recalculatedForm").validate();
	$("#recalculatedForm").find(".integernumber").each(function(){
	     $(this).rules("add", {
	       required: true,
	       integerNumber: true,
	       messages: {
	         required: "只能输入Number"
	       }
	     });   
   });
});
</script>