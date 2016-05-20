<#if oisgList?has_content>
	<#list oisgList as oisg>
		<#assign itemList = delegator.findByAnd("OrderItemShipGroupAssoc", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"shipGroupSeqId",oisg.shipGroupSeqId))?if_exists>
		<#assign facility="" />
		<#if oisg.facilityId?has_content>
			<#assign facility = oisg.getRelatedOne("Facility")>
		<#else>
			<#assign facility = orderHeader.getRelatedOne("OriginFacility")>
		</#if>
		<b>货运组 ${(oisg.shipGroupSeqId)!} / 接受仓库：${(facility.facilityName)!}
		/ 预计到货日期：
		<#if oisg.estimatedShipDate?has_content>${(oisg.estimatedShipDate)?string('yyyy-MM-dd')}</#if>
		</b>
		<div id="searchOptions_col" class="screenlet-body" style="border-bottom: #9d9d9b thin solid;">
		<div id="search-options">
		<form method="post" action="<@ofbizUrl>receivePurchaseOrder</@ofbizUrl>" id="FabricPurchaseItemList" class="basic-form" name="FabricPurchaseItemList_${(oisg_index)!}">
		<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
			    <td>内部编号</td>
				<td>商品名称</td>
			    <td>供应商商品编号</td>
			    <td>序列号</td>
			    <td>供应商</td>
			    <td>单位</td>
			    <td>规格</td>
			    <td>采购数量</td>
			    <td>已接收数量</td>
			    <td>预计到货日期</td>
			    <td>接收数量</td>
			    <td>库位</td>
			    <td>批号</td>
			    <td>备注</td>
		    </tr>
		    <#if itemList?has_content>
		    	<#list itemList as shipitem>
		    		<#assign orderItem = delegator.findByPrimaryKey("OrderItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"orderItemSeqId",shipitem.orderItemSeqId))?if_exists>
		    		<#assign product = orderItem.getRelatedOne("Product")?if_exists>
	    			<#if orderItem.statusId!="ITEM_CANCELLED">
	    		<tr <#if shipitem_index%2==999>class="alternate-row"</#if>>
			    	<td>${(orderItem.productId)!}</td>
			    	<td>${(product.productName)!}</td>
			    	<td>${Static["com.zuczug.product.ZuczugProductUtils"].getProductSupplierCode(delegator,product.productId,"")?if_exists}</td>
			    	<td>${(orderItem.orderItemSeqId)!}</td>
				    <td>
				    	<#assign supplierGv = delegator.findByPrimaryKey("PartyGroup", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId",supplierId))?if_exists>
				    	${(supplierGv.groupName)!}
				    </td>
				    <td>
				    	<#assign uom = delegator.findByPrimaryKey("Uom", Static["org.ofbiz.base.util.UtilMisc"].toMap("uomId",product.quantityUomId))?if_exists>
				    	${(uom.description)!}
				    </td>
				    <td>${(product.description)!}</td>
				    <td>${(shipitem.quantity)!}</td>
				    <td>
				    	<#assign receiptQuantity=Static["com.zuczug.order.ZuczugOrderUtils"].getOrderItemShipReceiptedQuantity(delegator,parameters.orderId,orderItem.orderItemSeqId,shipitem.shipGroupSeqId)?if_exists />
				   		${(receiptQuantity)!}
						
						<#assign needQuantity=(shipitem.quantity-receiptQuantity) /><#-- 本货运组是否还有需要接受的 -->
						<#if product.productTypeId="SERVICE"><#-- 如果当前是服务商品，直接设置成0，不需要接收 -->
			    			<#assign needQuantity=0/>
			    		</#if>
						
					    <input type="hidden" name="orderId_o_${(shipitem_index)!}" value="${(parameters.orderId)!}" >
					    <input type="hidden" name="productId_o_${(shipitem_index)!}" value="${(orderItem.productId)!}" >
					    <input type="hidden" name="orderItemSeqId_o_${(shipitem_index)!}" value="${(orderItem.orderItemSeqId)!}" >
					    <input type="hidden" name="facilityId_o_${(shipitem_index)!}" value="${(facility.facilityId)!}" >
					    <input type="hidden" name="shipGroupSeqId_o_${(shipitem_index)!}" value="${(shipitem.shipGroupSeqId)!}" >
				    </td>
				    <td>
				    	<#if orderItem.estimatedShipDate?has_content>
				    		${(orderItem.estimatedShipDate)?string('yyyy-MM-dd')}
				    	</#if>
				    </td>
				    <td>
				    	<#if needQuantity gt 0>
				    		<input type="text" name="receiveQuantity_o_${(shipitem_index)!}" size="5">
				    	</#if>
				    </td>
				    <td>
				   	 	<#if needQuantity gt 0>
							<@htmlTemplate.lookupField formName="FabricPurchaseItemList_${(oisg_index)!}" name="locationSeqId_o_${(shipitem_index)!}" id="locationSeqId_o_${(orderItem_index)!}" fieldFormName="LookupFacilityLocation?facilityId=${(facility.facilityId)!}"/>
				    	</#if>
				    </td>
				    <td>
				    	<#if needQuantity gt 0>
							<input type="text" name="lotId_o_${(shipitem_index)!}" size="12">
						</#if>
				    </td>
				    <td>
						${(orderItem.comments)!}
					</td>
				    </tr>
				</#if>
		    	</#list>
		    </#if>
		    
		</tbody>
		</table>
		<input name="submitBtn" value="收货" type="submit">
		</form>
		</div>
		</div>
		<br/>
	</#list>
</#if>

