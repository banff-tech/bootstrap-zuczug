<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.OrderItemList}
            </li>
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
		<#if oisg.estimatedShipDate?has_content>${(oisg.estimatedShipDate)?string('yyyy-MM-dd hh:mm:ss')}</#if>
		</b>
		<div id="searchOptions_col" class="screenlet-body" style="border-bottom: #9d9d9b thin solid;">
		<div id="search-options">
		<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
			    <td>${uiLabelMap.ProductId}</td>			
			    <td>${uiLabelMap.SeqId}</td>
			    <td>${uiLabelMap.ProductName}</td>
			    <td>${uiLabelMap.Quantity}</td>			   
			    <td>${uiLabelMap.ReceiverQuantity}</td>
			    <td>${uiLabelMap.PlanDeliveryDate}</td>
			    <td>${uiLabelMap.Status}</td>
			    <td>${uiLabelMap.Operation}</td>
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
				    	<#if orderItem.estimatedShipDate?has_content>
				    		${(orderItem.estimatedShipDate)?string('yyyy-MM-dd')}
				    	</#if>
				    </td>
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
					<td>
						<#if (orderItem.statusId == "ITEM_APPROVED") && needQuantity gt 0><!-- 只有已批准的才可以 -->
						<form id="updateForm_${(parameters.orderId)!}_${(oisg.shipGroupSeqId)!}_${(orderItem.orderItemSeqId)!}" method="post" action="<@ofbizUrl>updateOrderShipGroupAssoc</@ofbizUrl>" onsubmit="return updateOrderShipGroupAssoc('${(parameters.orderId)!}','${(oisg.shipGroupSeqId)!}','${(orderItem.orderItemSeqId)!}')">
						<input type="hidden" name="orderId" value="${(parameters.orderId)!}"/>
						<input type="hidden" name="shipGroupSeqId" value="${(oisg.shipGroupSeqId)!}"/>
						<input type="hidden" name="orderItemSeqId" value="${(orderItem.orderItemSeqId)!}"/>
						${uiLabelMap.Move}<input type="input" style="height:20px;width:30px" name="moveQuantity" maxQuantity="${(needQuantity)!0}" id="moveQuantity_${(parameters.orderId)!}_${(oisg.shipGroupSeqId)!}_${(orderItem.orderItemSeqId)!}">${uiLabelMap.MoveTo}
						<select name="moveShipGroup" id="moveShipGroup_${(parameters.orderId)!}_${(oisg.shipGroupSeqId)!}_${(orderItem.orderItemSeqId)!}">
							<#if oisgList?has_content>
								<#list oisgList as sg>
									<#if sg.shipGroupSeqId!=oisg.shipGroupSeqId>
									<option value="${(sg.shipGroupSeqId)!}">${uiLabelMap.ShipGroup}${(sg.shipGroupSeqId)!}</option>
									</#if>
								</#list>
							</#if>
						</select>
						<input type="submit" value="${uiLabelMap.Sure}"/>
						</#if>
						</form>
					</td>
				    </tr>
		    	</#list>
		    </#if>
		    
		</tbody>
		</table>
		</div>
		</div>
		<br/>
	</#list>
</#if>
</div>

<script>
function updateOrderShipGroupAssoc(orderId,shipGroupSeqId,orderItemSeqId){
	var moveQuantity = $("#moveQuantity_" + orderId+"_"+shipGroupSeqId+"_"+orderItemSeqId).val();
	var maxQuantity = $("#moveQuantity_" + orderId+"_"+shipGroupSeqId+"_"+orderItemSeqId).attr("maxQuantity");
	var moveShipGroup = $("#moveShipGroup_" + orderId+"_"+shipGroupSeqId+"_"+orderItemSeqId).val();
	if(moveQuantity.length<1){
		showErrorAlert("${uiLabelMap.Tip}","${uiLabelMap.MoveCantEmpty}");
		return false;
	}
	
	if(isNaN(moveQuantity)){
		showErrorAlert("${uiLabelMap.Tip}","${uiLabelMap.NeedNumber}");
		return false;
	}
	if(Number(moveQuantity) > maxQuantity){
		showErrorAlert("${uiLabelMap.Tip}","${uiLabelMap.NotReceivedQuantity} " + maxQuantity + ",${uiLabelMap.MoveCantGreaterThanQuantity}");
		return false;
	}
	//$("#updateForm_" + orderId+"_"+shipGroupSeqId+"_"+orderItemSeqId).submit();
}
</script>