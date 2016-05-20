<#assign isSaveHeader=cart.getOrderAttribute('FABRIC_PURCHASE_TYPE')?if_exists/>
<#-- 订单头信息 -->
<#if cartItems?has_content>
<a href="javascript:$('#headerForm').submit();" onclick="$('#HEADER_MODE').val('SAVE');return confirm('提交这个采购单')" class="buttontext">提交采购单</a>
</#if>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                添加采购单
            </li>
            <li><a href="<@ofbizUrl>purchase.emptycart</@ofbizUrl>" onclick="return confirm('${uiLabelMap.ClearOrderConfrim}')">清除订单</a></li>
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
   	<form method="post" action="<@ofbizUrl>addPurchaseOrderData</@ofbizUrl>" id="headerForm" name="headerForm">
   		<input type="hidden" id="HEADER_MODE" name="MODE" value="HEADER_INFO"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">采购商：</td>
		        <td valign='middle'>
		        	<div class='tabletext'>
		        	<#assign supplierId=cart.getBillFromVendorPartyId()?if_exists/><!-- 卖方 -->
		        	<#if isSaveHeader?has_content>
		        		<#if supplierId?has_content>
		        			<#assign supplierName=Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator,supplierId, true)?if_exists/>
		        			${(supplierName)!}
		        			<input type="hidden" name="agentSupplierId" value="${(supplierId)!}"/>
		        		</#if>
		        	<#else>
		        		
		        		<@htmlTemplate.lookupField formName="headerForm" name="agentSupplierId" fieldFormName="LookupSupplier" value="" className="required"/>
		         	</#if>
		         	<span class="tooltip">也是结算方</span>
		    	   </div>
		      	</td>
		      	
		      	<td class="label">商品供应商：</td>
		        <td valign='middle'>
		        	<div class='tabletext'>
		        	<#assign roleMap=cart.getAdditionalPartyRoleMap()?if_exists/>
		        	<#if isSaveHeader?has_content>
		        		<if roleMap?has_content && roleMap.SUPPLIER?has_content && roleMap.SUPPLIER[0]?has_content>
		        			<assign supplierName=Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator,roleMap.SUPPLIER[0], true)?if_exists/>
		        			${(supplierName)!}
		        		</if>
		        		<input type="hidden" name="partyId" value="${(supplierId)!}"/>
		        	<#else>
		             	<#--
		             	<select name="partyId" style="width:150px">
		             		<option value="">${uiLabelMap.PleaseSelect}</option>
		              	  	<#list suppliers as supplier>
		              	    	<option value="${supplier.partyId}" <#if roleMap?has_content && roleMap.SUPPLIER?has_content && roleMap.SUPPLIER[0]==supplier.partyId>selected</#if>>[${supplier.partyId}] - ${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(supplier, true)}</option>
		             	   	</#list>
		             	</select>
		             	-->
		             	<@htmlTemplate.lookupField formName="headerForm" name="partyId" fieldFormName="LookupSupplier" value=""/>
		         	</#if>
		         	<span class="tooltip">商品供应商，不选默认和采购商一致</span>
		    	   </div>
		      	</td>
		    </tr>
		    <tr>
		        <#--
		        	<td class="label">${uiLabelMap.OrderName}：</td>
		        	<td><input type="text" name="orderName" value="${cart.getOrderName()?if_exists}"/></td>
		        -->
		        <td class="label">自定义订单号：</td>
		        <td><input type="text" name="externalId" value="${cart.getExternalId()?if_exists}"/></td>
		        <td class="label">接收仓库：</td>
		        <td valign='middle'>
		    	   <div class="tabletext">
		    	   	<#if isSaveHeader?has_content>
		    	   		<#assign facility = delegator.findByPrimaryKey("Facility", Static["org.ofbiz.base.util.UtilMisc"].toMap("facilityId",cart.getFacilityId()))?if_exists>
		    	   		${(facility.facilityName)!}
		    	   		<input type="hidden" name="facilityId" value="${(cart.getFacilityId())!}"/>
		    	   	<#else>
		    	   	<select name="facilityId" class="required">
		    	   		<option value="">-请选择-</option>
		    	   		<#list facilityList as facility>
		    	   			<option value="${facility.facilityId}" <#if facility.facilityId==cart.getFacilityId()!''>selected</#if>>
		    	   				[${facility.facilityId}]-[${facility.facilityName}]
		    	   			</option>
		    	   		</#list>
		    	   	</select>
		    	   	</#if>
		    	   </div>
		      	</td>
		    </tr>
		    <tr>
		        <td class="label">制单人：</td>
		        <td>${(userLogin.userLoginId)!}</td>
		        <td>&nbsp;</td>
		        <td>&nbsp;</td>
		    </tr>
		    <tr>
		        <td class="label">采购单类型：</td>
		        <td>
		        	<select name="purchaseType">
		        		<option value="FAB_ACC_PURCHASE" <#if isSaveHeader?has_content && isSaveHeader=='FAB_ACC_PURCHASE'>selected</#if>>物料采购单</option>
		        		<option value="CLOTH_DIS_PURCHASE" <#if isSaveHeader?has_content && isSaveHeader=='CLOTH_DIS_PURCHASE'>selected</#if>>经销商品采购</option>
		        	</select>
		        </td>
		        <td>&nbsp;</td>
		        <td>&nbsp;</td>
		    </tr>
		    <tr>
		        <td class="label">描述：</td>
		        <td colspan="3"><#-- #if cart.getInternalOrderNotes()?has_content>$!{cart.getInternalOrderNotes().get(0)?if_exists}</ #if -->
		        	<textarea cols="60" rows="3" name="orderNote">${(session.getAttribute('orderNote'))!}</textarea>
		        </td>
		    </tr>
		    <tr>
		        <td class="label">
		        	<#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
					<input type="submit" onclick="$('#HEADER_MODE').val('HEADER_INFO');" value="再次保存"/>
					<#else>
					<input type="submit" onclick="$('#HEADER_MODE').val('HEADER_INFO');" value="下一步"/>
					</#if>
				</td>
		    </tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>

<#-- 添加订单项部分 -->
<#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                添加订单项
            </li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>addPurchaseOrderData</@ofbizUrl>" id="addItemForm" name="addItemForm">
    	<input type="hidden" name="MODE" value="ADD_ITEM"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">选择商品：</td>
	            <td>
	            	<@htmlTemplate.lookupField formName="addItemForm" name="productId" fieldFormName="LookupProduct" value="" className="required" size="20" maxlength="40"/>
	            </td>
	            <td class="label">数量：</td>
	            <td><input type="text" name="quantity" class="required integerNumber"/></td>
	            <td class="label">单价：</td>
	            <td><input type="text" name="unitPrice" class="required integerNumber"/></td>
	        </tr>
	        <tr>
		        <td class="label">预计到货时间：</td>
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
                添加服务
            </li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>addPurchaseOrderData</@ofbizUrl>" id="addServiceForm" name="addServiceForm">
    	<input type="hidden" name="MODE" value="ADD_SERVICE"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">选择服务：</td>
	            <td>
	            	<select name="productId">
	            		<option>-请选择-</option>
	            		<#if serviceList?has_content>
	            		<#list serviceList as product>
	            			<option value="${(product.productId)!}">${(product.productName)!}</option>
	            		</#list>
	            		</#if>
	            	</select>
	            </td>
	            <td class="label">金额：</td>
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
            <li class="h3">已经添加的项目</li>
            <li><a href="javascript:$('#recalculatedForm').submit()">重新计算</a></li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>RecalculatedFabricPurchaseOrder</@ofbizUrl>" id="recalculatedForm">
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
		        <td>商品名称</td>
		        <td width="200">内部名称</td>
		        <td width="200">备注</td>
	            <td>数量</td>
	            <td>货币类型</td>
	            <td>单价</td>
	            <td>预计到货日期</td>
	            <td>小计</td>
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
		            <td><input style="width:80px" type="text" id="unitPrice_${item_index}" name="unitPrice" value="${(item.getBasePrice())!}" class="integernumber" /></td>
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
	        		<td>合计：</td>
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