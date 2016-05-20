<table cellspacing="0" class="basic-table">
    <tbody>
	<tr>
        <td class="label">${uiLabelMap.ShipType}：</td>
        <td valign='middle'>
    	   <div class="tabletext">
    	   	<#if isSaveShip?has_content>
    	   		<#assign orderShipName = delegator.findByPrimaryKey("ProductStoreShipmentMethView", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreShipMethId",isSaveShip))?if_exists>
    	   		${orderShipName.partyId}-${orderShipName.shipmentMethodTypeId}
    	   		<!--<input type="hidden" name="productStoreId" value="${(cart.getProductStoreId())!}"/>-->
    	   	<#else>
    	   	<select name="shipId" class="required">
    	   		<option value="">${uiLabelMap.PleaseSelectA}</option>
    	   		<#assign orderShipList = delegator.findByAnd("ProductStoreShipmentMethView", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreId",cart.getProductStoreId()))?if_exists>
    	   		<#list orderShipList as orderShip>
    	   			<option value="${orderShip.productStoreShipMethId}">
    	   				${orderShip.partyId}-${orderShip.shipmentMethodTypeId}
    	   			</option>
    	   		</#list>
    	   	</select>
    	   	</#if>
    	   </div>
      	</td>
	</tr>
    <#if isSaveHeader?has_content><!-- 验证是否有订单头信息 -->
    <tr>
        <td class="label">
        		<#if isSaveShip?has_content><!-- 验证是否有发货方式头信息 -->
					<!--<input type="submit" onclick="$('#HEADER_MODE').val('HEADER_SHIP');" value="保存"/>-->
				<#else>
					<input type="submit" onclick="$('#HEADER_MODE').val('HEADER_SHIP');" value="下一步"/>
				</#if>
		</td>
    </tr>
    </#if>
    </tbody>
</table>