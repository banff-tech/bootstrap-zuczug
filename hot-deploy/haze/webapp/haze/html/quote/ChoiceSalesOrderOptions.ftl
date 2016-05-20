<#if shipContactMechId?has_content>
<#else>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                	${uiLabelMap.OrderOption}
            </li>
        </ul>
        <br class="clear">
    </div>
	<form name="ChoiceSalesOrderOptions" method="post" action="<@ofbizUrl>ChoiceSalesOrderOptions</@ofbizUrl>">
		<input type="hidden" name="quoteId" value="${quoteId}"/>
		<input type="hidden" name="isnext" value="1"/>
		<table cellspacing="0" class="basic-table">
			<#assign quote = delegator.findByAndCache("Quote", Static["org.ofbiz.base.util.UtilMisc"].toMap("quoteId",quoteId))?if_exists>
			<tr>
				<td class="label">${uiLabelMap.ShippingAddress}:</td>
				<td>
					<select name="shipContactMechId" class="required">
								<#assign partyContactMechPurpose = delegator.findByAndCache("FindPartyContactMech", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId",quote[0].get("partyId"),"contactMechPurposeTypeId","SHIPPING_LOCATION"))?if_exists>
									<!--<option value="${partyContactMechPurpose[0].get("contactMechId")}">${partyContactMechPurpose[0].get("address1")}</option>-->

								<#assign partyContactMechPurposeList = delegator.findByAnd("FindPartyContactMech", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId",quote[0].get("partyId"),"contactMechPurposeTypeId","SHIPPING_LOCATION"))?if_exists>
								<#list partyContactMechPurposeList as partyContactMechPurposes>
									<option <#if partyContactMechPurposes.contactMechId == partyContactMechPurpose[0].get("contactMechId")>selected="selected"</#if> value="${partyContactMechPurposes.contactMechId}">
										${partyContactMechPurposes.address1}
									</option>
								</#list>
					</select>
				</td>
			</tr>
			<tr>
		        <td class="label">${uiLabelMap.ShipType}：</td>
		        <td valign='middle'>
		    	   <div class="tabletext">
		    	   	<select name="shipId" class="required">
		    	   		<#assign orderShipList = delegator.findByAnd("ProductStoreShipmentMethView", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreId",quote[0].get("productStoreId")))?if_exists>
		    	   		<!--<option value="${orderShipList[0].get("productStoreShipMethId")}">${orderShipList[0].get("partyId")}-${orderShipList[0].get("shipmentMethodTypeId")}</option>-->
		    	   		
		    	   		<#list orderShipList as orderShip>
		    	   			<option <#if orderShip.productStoreShipMethId == orderShipList[0].get("productStoreShipMethId")>selected="selected"</#if> value="${orderShip.productStoreShipMethId}">
		    	   				${orderShip.partyId}-${orderShip.shipmentMethodTypeId}
		    	   			</option>
		    	   		</#list>
		    	   	</select>
		    	   </div>
		      	</td>
			</tr>
			<tr>
		        <td class="label">${uiLabelMap.PayType}：</td>
		        <td valign='middle'>
		    	   <div class="tabletext">
		    	   	<select name="paymentType" class="required">
		    	   		<#assign productStorePaymentList = delegator.findByAnd("FindProductStorePayment", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreId",quote[0].get("productStoreId")))?if_exists>
		    	   		<!--<option value="${productStorePaymentList[0].get("paymentMethodTypeId")}">${productStorePaymentList[0].get("description")}</option>-->
		    	   		
		    	   		<#list productStorePaymentList as productStorePayment>
		    	   			<option <#if productStorePayment.paymentMethodTypeId == productStorePaymentList[0].get("paymentMethodTypeId")>selected="selected"</#if> value="${productStorePayment.paymentMethodTypeId}">
		    	   				${productStorePayment.get("description",locale)}
		    	   			</option>
		    	   		</#list>
		    	   	</select>
		    	   </div>
		      	</td>
			</tr>
			
			<tr>
				<td>
					<input type="submit" value="${uiLabelMap.Next}"/>
				</td>
				<td></td>
			</tr>
		</table>
	</form>
</div>
</#if>
			
					