<#assign isSaveHeader=cart.getOrderAttribute('ORDER_TYPE')?if_exists/>
<#assign isSaveShip=cart.getOrderAttribute('ORDER_SHIP')?if_exists/>

<#-- 订单头信息 -->
<#if cartItems?has_content>
<a href="javascript:$('#headerForm').submit();" onclick="$('#HEADER_MODE').val('SAVE');return confirm('确定提交')" class="buttontext">提交订单</a>
</#if>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
              	${uiLabelMap.ShipType}
            </li>
            <li><a href="<@ofbizUrl>emptycart</@ofbizUrl>" onclick="return confirm('${uiLabelMap.ClearOrderConfrim}')">清除订单</a></li>
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
   	<form method="post" action="<@ofbizUrl>addSalesOrderData</@ofbizUrl>" id="headerForm" name="headerForm">
   		<input type="hidden" id="HEADER_MODE" name="MODE" value="HEADER_INFO"/>
    	<#include "component://haze/webapp/haze/html/sales/salesOrderHeader.ftl"> 
	</form>
    </div>
</div>

<#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
              	${uiLabelMap.ShipType}
            </li>
        </ul>
        <br class="clear">
    </div>

    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>addSalesOrderData</@ofbizUrl>" id="shipForm" name="shipForm">
   		<input type="hidden" id="HEADER_MODE" name="MODE" value="HEADER_SHIP"/>
    	<#include "component://haze/webapp/haze/html/sales/salesOrderShip.ftl"> 
	</form>
    </div>
</div>
</#if><!-- 验证是否有订单头信息 -->

<#-- 添加订单项部分 -->
<#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
<#if isSaveShip?has_content><!-- 验证是否有订单送货方式信息 -->
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                	${uiLabelMap.AddOrderItem}
            </li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>addSalesOrderData</@ofbizUrl>" id="addItemForm" name="addItemForm">
    	<input type="hidden" name="MODE" value="ADD_ITEM"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">${uiLabelMap.ChooseProduct}：</td>
	            <td>
	            	<@htmlTemplate.lookupField formName="addItemForm" width="800" name="productId" fieldFormName="LookupRealProductNoVirtualForSkuNoAjex" value="" className="required" maxlength="50"/>
	            </td>
	            <td class="label">${uiLabelMap.Quantity}：</td>
	            <td><input type="text" name="quantity" class="required integerNumber"/></td>
	        </tr>
	        <tr>
		        <td class="label">${uiLabelMap.PlanDeliveryDate}：</td>
		        <td>
		            <@htmlTemplate.renderDateTimeField name="itemDesiredDeliveryDate" event="" action="" value="" className="" alert="" title="Format: yyyy-MM-dd HH:mm:ss.SSS" size="25" maxlength="30" id="itemDesiredDeliveryDate1" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
		        	<script>
		        	$(function(){
		        		$("#itemDesiredDeliveryDate_i18n").addClass("required");
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


<#-- 已经添加的订单项目 -->
<div class="screenlet">
	<div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.AddOrderItemYet}</li>
            <li><a href="javascript:$('#recalculatedForm').submit()">${uiLabelMap.Recalculated}</a></li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>RecalculatedSalesOrder</@ofbizUrl>" id="recalculatedForm">
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
		        <td>${uiLabelMap.ProductId}</td>
		        <td width="200">${uiLabelMap.ProductName}</td>
		        <!--<td width="200">${uiLabelMap.FabricComponentId}</td>-->
	            <td>${uiLabelMap.Quantity}</td>
	            <td>${uiLabelMap.Currency}</td>
	            <!--<td>${uiLabelMap.ProductLastPrice}</td>-->
	            <td>${uiLabelMap.PlanDeliveryDate}</td>
	            <td>${uiLabelMap.TotalCount}</td>
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
			        <!--<td>${(product.description)!}</td>-->
		            <td>
		            	<input style="width:80px" type="text" id="quantity_${item_index}" name="quantity" value="${(item.getQuantity())!}" class="integernumber">
		            	<input type="hidden" name="itemIndex" value="${(cart.getItemIndex(item))!}"/>
		            	<input type="hidden" id="unitPrice_${item_index}" name="unitPrice" value="${(item.getBasePrice())!}"/>
		            </td>
		            <td>${(cart.getCurrency())!}</td>
		            <!--<td><input style="width:80px" type="text" id="unitPrice_${item_index}" name="unitPrice" value="${(item.getBasePrice())!}" class="integernumber" /></td>-->
		            <td>		            	
		            	<#if item.getDesiredDeliveryDate()?has_content>
		            	
						<@htmlTemplate.renderDateTimeField name="estimatedDeliveryDate" value="${item.getDesiredDeliveryDate()!''}" className="" alert="" title="Format: yyyy-MM-dd HH:mm:ss.SSS" size="25" maxlength="30" id="item3" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
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
	        		<td></td>
	        		<td align="right">${uiLabelMap.ProductTotalQuantity}：</td>
	        		<td>${(totalQuantity)!}</td>
	        		<td></td>
	        		
	        		<td align="right">${uiLabelMap.Summation}：</td>        		

	        		<td><@ofbizCurrency amount=totalAmount isoCode=cart.getCurrency()/>
	        		<input type="hidden" name="totalAmount" value="${(totalAmount)!}"/></td>
	        		
	        		
	        	</tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>
</#if><!-- 验证是否有订单头信息 -->
</#if><!-- 验证是否有订单送货方式信息 -->
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
   
	$("input[name='productId']").bind('input', function(){
		getProductInfo();
    });
    
    $("input[name='productId']").bind('propertychange', function(){
		getProductInfo();
    });
    
    $("input[name='productId']").bind('blur', function(){
		getProductInfo();
    });
   
});
function getProductInfo(){
	if($("input[name='productId']").val()!=""){
		$.post("<@ofbizUrl>findProductMasterUomId</@ofbizUrl>",{'productId':$("input[name='productId']").val()},function(result){
			if("uom" in result){
				$("#secUomId").text(result['uom'].abbreviation);
				$("#secUom").val(result['uom'].abbreviation);
				
			}else{
				$("#secUomId").text("无");
			}
		});
	}
}

</script>