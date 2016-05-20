<table cellspacing="0" class="basic-table">
    <tbody>
    <tr>
	    <td class="label">${uiLabelMap.Customer}：</td>
        <td valign='middle'>
          <div class='tabletext'>
        	<#assign buyerId=cart.getBillToCustomerPartyId()?if_exists/><!-- 买方 -->
        	<#if isSaveHeader?has_content>
        		<#if buyerId?has_content>
        			<#assign buyerName=Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator,buyerId, true)?if_exists/>
        			${(buyerName)!}
        			<input type="hidden" name="buyerId" value="${(buyerId)!}"/>
        		</#if>
        	<#else>	
        		<@htmlTemplate.lookupField formName="headerForm" name="buyerId" fieldFormName="LookupPartyName" value="" className="required"/>
         	</#if>
    	   </div>
        </td>
    <#--<tr>
       
 		<td class="label">${uiLabelMap.OrderName}：</td>
		<td><input type="text" name="orderName" value="${cart.getOrderName()?if_exists}"/></td>
        
        <td class="label">自定义订单号：</td>
        <td><input type="text" name="externalId" value="${cart.getExternalId()?if_exists}"/></td>
       
    </tr>  
    -->  
    <#-- 暂时停用 改用默认写死b2b店铺方式
	<tr>
        <td class="label">产品店铺：</td>
        <td valign='middle'>
    	   <div class="tabletext">
    	   	<#if isSaveHeader?has_content>
    	   		<#assign productStore = delegator.findByPrimaryKey("ProductStore", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreId",cart.getProductStoreId()))?if_exists>
    	   		${(productStore.storeName)!}
    	   		<input type="hidden" name="productStoreId" value="${(cart.getProductStoreId())!}"/>
    	   	<#else>
    	   	<select name="productStoreId" class="required">
    	   		<option value="">请选择</option>
    	   		<#list productStoreList as productStore>
    	   			<option value="${productStore.productStoreId}" <#if productStore.productStoreId==cart.getProductStoreId()!''>selected</#if>>
    	   				[${productStore.productStoreId}]-[${productStore.storeName}]
    	   			</option>
    	   		</#list>
    	   	</select>
    	   	</#if>
    	   </div>
      	</td>
	</tr>
	--> 
	<input type="hidden" name="productStoreId" value="B2B"/>
    <tr>
        <td class="label">${uiLabelMap.CreatedBy}：</td>
        <td>${(userLogin.userLoginId)!} </td>
    </tr>
    <tr>
      	<td class="label">${uiLabelMap.OrderType}：</td>
        <td valign='middle'>
        	<div class='tabletext'>
              <select name="orderTypeId" id="orderTypeId">
                <option value="1">${uiLabelMap.SureCancelOrder}</option>
                <option value="2">${uiLabelMap.ConfigOrder}</option>
				<option value="3">${uiLabelMap.ReplenishmentOrder}</option>
				<option value="4">${uiLabelMap.BuyOneOrder}</option>
              </select>
              <#assign orderType=cart.getOrderAttribute("ORDER_TYPE")?if_exists/><!-- 订单类型-->
            <script>
 				document.getElementById("orderTypeId").value=${(orderType)!}
			</script>
            </div>
        </td>
    </tr>
    
    <tr>
        <td class="label">${uiLabelMap.Description}：</td>
        <td colspan="3"><#-- #if cart.getInternalOrderNotes()?has_content>$!{cart.getInternalOrderNotes().get(0)?if_exists}</ #if -->
        	<textarea cols="60" rows="3" name="orderNote">${(session.getAttribute('orderNote'))!}</textarea>
        </td>
    </tr>
    
    <tr>
        <td class="label">
        	<#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
			<input type="submit" onclick="$('#HEADER_MODE').val('HEADER_INFO');" value="保存"/>
			<#else>
			<input type="submit" onclick="$('#HEADER_MODE').val('HEADER_INFO');" value="下一步"/>
			</#if>
		</td>
    </tr>
    </tbody>
</table>