<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.OrderItemList}
            </li>
            <#if orderHeader.statusId!="ORDER_COMPLETED">
            	<li><a href="<@ofbizUrl>EditOrderShipmentGroups?orderId=${(parameters.orderId)!}</@ofbizUrl>">${uiLabelMap.EditShipGroup}</a></li>
            	<li><a onclick="return window.confirm('${uiLabelMap.CreateShipGroup}?\n${uiLabelMap.ShipPurposeEdit}')" href="<@ofbizUrl>createOrderShipmentGroups?orderId=${(parameters.orderId)!}</@ofbizUrl>">${uiLabelMap.AddShipGroup}</a></li>
            </#if>
            <#if orderHeader.statusId=="ORDER_CREATED">
            	<li><a href="<@ofbizUrl>FindSalesOrderItem?orderId=${(parameters.orderId)!}&editFlag=Y</@ofbizUrl>">${uiLabelMap.EditOrderItem}</a></li>
            </#if>
            <#if orderHeader.statusId=="ORDER_CREATED">
            <li>
            	<a href="javascript:if(confirm('批准后订单无法修改')){document.OrderApproveOrder.submit()}">${uiLabelMap.ApproveOrder}</a>
	            <form name="OrderApproveOrder" method="post" action="<@ofbizUrl>changeOrderStatus</@ofbizUrl>">
		            <input type="hidden" name="statusId" value="ORDER_APPROVED">
		            <input type="hidden" name="newStatusId" value="ORDER_APPROVED">
		            <input type="hidden" name="setItemStatus" value="Y">
		            <input type="hidden" name="orderId" value="${(parameters.orderId)!}">
				</form>
			</li>
			</#if>
        </ul>
        <br class="clear">
    </div>
<#if oisgList?has_content>
	<#list oisgList as oisg>
		<#assign itemList = delegator.findByAnd("OrderItemShipGroupAssoc", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"shipGroupSeqId",oisg.shipGroupSeqId))?if_exists>
		<#if oisg.facilityId?has_content>
			<#assign facility = oisg.getRelatedOne("Facility")>
		<#else>
			<#assign facility = orderHeader.getRelatedOne("OriginFacility")>
		</#if>
		<b>${uiLabelMap.ShipGroup}: ${(oisg.shipGroupSeqId)!} / ${uiLabelMap.OutFacility}：${(facility.facilityName)!} 
		 / 预计发货日期：
		<#if oisg.estimatedShipDate?has_content>${(oisg.estimatedShipDate)?string('yyyy-MM-dd')} </#if>
		</b>
		<div id="searchOptions_col" class="screenlet-body" style="border-bottom: #9d9d9b thin solid;">
		<div id="search-options">
		<!-- Begin  Form Widget - Form Element  component://fabricfacility/widget/FabricFacilityForms.xml#FabricPurchaseItemList -->
		<form method="post" action="<@ofbizUrl>receivePurchaseFabric</@ofbizUrl>" id="FabricPurchaseItemList" class="basic-form" name="FabricPurchaseItemList_${(oisg_index)!}">
		<!-- Begin  Form Widget  component://fabricfacility/widget/FabricFacilityForms.xml#FabricPurchaseItemList --><table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
			    <td>${uiLabelMap.ProductId}</td>			   
			    <td>${uiLabelMap.SeqId}</td>
			    <td>${uiLabelMap.ProductName}</td>		    	
			    <td>${uiLabelMap.Quantity}</td>			   
			    <td>${uiLabelMap.UnitPrice}</td>
			    <td>${uiLabelMap.ReceiverQuantity}</td>
			    <td>${uiLabelMap.PlanDeliveryDate}</td>
			    <td>${uiLabelMap.Status}</td>
		    </tr>
		    <#if itemList?has_content>
		    	<#list itemList as shipitem>
		    		<#assign orderItem = delegator.findByPrimaryKey("OrderItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"orderItemSeqId",shipitem.orderItemSeqId))?if_exists>
		    		<#assign product = orderItem.getRelatedOne("Product")>
    		<tr <#if shipitem_index%2==999>class="alternate-row"</#if>>
			    	<td>${(orderItem.productId)!}</td>
			    	<td>${(orderItem.orderItemSeqId)!}</td>
				    <td>${(product.productName)!}</td>
				    <td>${(shipitem.quantity)!}</td>
				    <td>
				    	<@ofbizCurrency amount=orderItem.unitPrice isoCode=orderHeader.currencyUom/>
				    </td>
				    <td>
				    	<#assign receiptQuantity=Static["com.zuczug.order.ZuczugOrderUtils"].getOrderItemShipReceiptedQuantity(delegator,parameters.orderId,orderItem.orderItemSeqId,shipitem.shipGroupSeqId)?if_exists />
				   		${(receiptQuantity)!}
						
						<#assign needQuantity=(shipitem.quantity-receiptQuantity) /><#-- 本货运组是否还有需要接受的 -->
						
					    <input type="hidden" name="orderId_o_${(shipitem_index)!}" value="${(parameters.orderId)!}" >
					    <input type="hidden" name="productId_o_${(shipitem_index)!}" value="${(orderItem.productId)!}" >
					    <input type="hidden" name="orderItemSeqId_o_${(shipitem_index)!}" value="${(orderItem.orderItemSeqId)!}" >
					    <input type="hidden" name="facilityId_o_${(shipitem_index)!}" value="${(oisg.facilityId)!}" >
					    <input type="hidden" name="shipGroupSeqId_o_${(shipitem_index)!}" value="${(shipitem.shipGroupSeqId)!}" >
				    </td>
				    <td>
				    	<#if orderItem.estimatedDeliveryDate?has_content>
				    		${(orderItem.estimatedDeliveryDate)?string('yyyy-MM-dd')}
				    	</#if>
				    </td>
				    <#--
				    <td>
				    	<#if needQuantity gt 0>
				    		<input type="text" name="receiveQuantity_o_${(shipitem_index)!}" size="5">
				    	</#if>
				    </td>
				    <td>
				   	 	<#if needQuantity gt 0>
							<@htmlTemplate.lookupField width="100" formName="FabricPurchaseItemList_${(oisg_index)!}" name="locationSeqId_o_${(shipitem_index)!}" id="locationSeqId_o_${(orderItem_index)!}" fieldFormName="LookupFacilityLocation?facilityId=${(facility.facilityId)!}"/>
				    	</#if>
				    </td>
				    <td>
				    	<#if needQuantity gt 0>
							<input type="text" name="lotId_o_${(shipitem_index)!}" size="12">
						</#if>
				    </td>
				    -->
					<td>
						<#if (orderItem.statusId == "ITEM_APPROVED")>
							${uiLabelMap.ItemApproved}
						<#elseif (orderItem.statusId == "ITEM_CREATED")>
							${uiLabelMap.ItemCreated}
						<#elseif (orderItem.statusId == "ITEM_COMPLETED")>
							${uiLabelMap.ItemCompleted}
						<#else>
							${uiLabelMap.ItemCancel}
						</#if>
					</td>
				    </tr>
		    	</#list>
		    </#if>
		    
		</tbody>
		</table>
		</form>
		</div>
		</div>
		<br/>
	</#list>
</#if>
</div>
<script>
	
</script>