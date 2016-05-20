<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                订单项列表
            </li>
            
            <li><a href="<@ofbizUrl>EditOrderShipmentGroups?orderId=${(parameters.orderId)!}</@ofbizUrl>">编辑货运组</a></li>
            <li><a onclick="return window.confirm('创建一个新的货运组？\n运送目的地可以在[编辑订单项]功能中修改')" href="<@ofbizUrl>createOrderShipmentGroups?orderId=${(parameters.orderId)!}</@ofbizUrl>">新增货运组</a></li>
            <#if orderHeader.statusId=='ORDER_CREATED' || orderHeader.statusId=='ORDER_APPROVED'>
            	<li><a href="<@ofbizUrl>FindPurchaseOrderItem?orderId=${(parameters.orderId)!}&editFlag=Y</@ofbizUrl>">编辑订单项</a></li>
            </#if>
            <#if orderHeader.statusId=='ORDER_CREATED'>
            <li>
            	<a href="javascript:if(confirm('批准后订单无法再次修改')){document.OrderApproveOrder.submit()}">批准订单</a>
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
			<#assign facility = oisg.getRelatedOne("Facility")?if_exists>
		<#else>
			<#assign facility = orderHeader.getRelatedOne("OriginFacility")?if_exists>
		</#if>
		<b>货运组 ${(oisg.shipGroupSeqId)!} / 接受仓库：${(facility.facilityName)!} 
		 / 预计到货日期：
		<#if oisg.estimatedShipDate?has_content>${(oisg.estimatedShipDate)?string('yyyy-MM-dd')} </#if>
		</b>
		<div id="searchOptions_col" class="screenlet-body" style="border-bottom: #9d9d9b thin solid;">
		<div id="search-options">
		<!-- Begin  Form Widget - Form Element  component://fabricfacility/widget/FabricFacilityForms.xml#FabricPurchaseItemList -->
		<form method="post" action="<@ofbizUrl>receivePurchaseFabric</@ofbizUrl>" id="FabricPurchaseItemList" class="basic-form" name="FabricPurchaseItemList_${(oisg_index)!}">
		<!-- Begin  Form Widget  component://fabricfacility/widget/FabricFacilityForms.xml#FabricPurchaseItemList --><table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
			    <td>商品编号</td>
			    <td>商品名称</td>
			    <td>供应商物料编号</td>
			    <td>序列号</td>
			    <td>供应商</td>
			    <td width="250">规格</td>
			    <td>采购数量</td>
			    <td>单位</td>
			    <td>单价</td>
			    <td>已接收数量</td>
			    <td>预计到货日期</td>
			    <td>备注</td>
			    <td>状态</td>
		    </tr>
		    <#if itemList?has_content>
		    	<#list itemList as shipitem>
		    		<#assign orderItem = delegator.findByPrimaryKey("OrderItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"orderItemSeqId",shipitem.orderItemSeqId))?if_exists>
		    		<#assign product = orderItem.getRelatedOne("Product")>
    		<tr <#if shipitem_index%2==0>class="alternate-row"</#if>>
			    	<td>${(orderItem.productId)!}</td>
			    	<td>${(product.productName)!}</td>
			    	<td>${Static["com.zuczug.product.ZuczugProductUtils"].getProductSupplierCode(delegator,product.productId,"")?if_exists}</td>
			    	<td>${(orderItem.orderItemSeqId)!}</td>
				    
				    <td>
				    	<#assign supplierGv = delegator.findByPrimaryKey("PartyGroup", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId",supplierId))?if_exists>
				    	${(supplierGv.groupName)!}
				    </td>
				    <td>${(product.description)!}</td>
				    <td>${(shipitem.quantity)!}</td>
				    <td>
				    	<#if product.quantityUomId?has_content>
					    	<#assign uomIdDesc='Uom.description.' + product.quantityUomId?if_exists />
					    	${(uiLabelMap[uomIdDesc])!}
				    	</#if>
				    </td>
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
				    	<#if orderItem.estimatedShipDate?has_content>
				    		${(orderItem.estimatedShipDate)?string('yyyy-MM-dd')}
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
						${(orderItem.comments)!}
					</td>
					<td>
						<#if (orderItem.statusId == "ITEM_APPROVED")>
							已批准
						<#elseif (orderItem.statusId == "ITEM_CREATED")>
							已创建
						<#elseif (orderItem.statusId == "ITEM_COMPLETED")>
							已完成
						<#else>
							已取消
						</#if>
					</td>
				    </tr>
		    	</#list>
		    </#if>
		    
		</tbody>
		</table>
			<#-- <input name="submitBtn" value="收货" type="submit"> -->
		</form>
		</div>
		</div>
		<br/>
	</#list>
</#if>
</div>
<script>
	
</script>