<div class="screenlet">
	<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
				订单项信息          
			</li>	
			<#if orderHeader.statusId=='ORDER_CREATED' || orderHeader.statusId=='ORDER_APPROVED'>
            	<li><a href="<@ofbizUrl>FindFabricPurchaseItem?orderId=${(parameters.orderId)!}&amp;editFlag=Y&purchaseType=${purchaseType}</@ofbizUrl>">编辑订单项</a></li>
            </#if>
            <#if orderHeader.statusId=='ORDER_CREATED'>
            <li>
            	<a href="javascript:if(confirm('批准后订单无法再次修改')){document.OrderApproveOrder.submit()}">批准订单</a>
	            <form name="OrderApproveOrder" method="post" action="<@ofbizUrl>changeOrderStatus</@ofbizUrl>">
		            <input type="hidden" name="statusId" value="ORDER_APPROVED">
		            <input type="hidden" name="newStatusId" value="ORDER_APPROVED">
		            <input type="hidden" name="setItemStatus" value="Y">
		            <input type="hidden" name="orderId" value="${(parameters.orderId)!}">
		            <input type="hidden" name="partyId" value="${(agentSupplier.partyId)!}">
		            <input type="hidden" name="purchaseType" value="${purchaseType}">
				</form>
			</li>
			</#if>
        </ul>
        <br class="clear">
    </div>
    <table cellspacing="0" class="basic-table" style="width: 120%;">
			    <tbody>
			    <tr class="header-row-2">
				    <td>素然物料编号</td>
				    <td>供应商物料编号</td>
				    <td>序列号</td>
				    <td>物料名称</td>
			    	<td>组别</td>
				    <td>供应商</td>
				    <td>规格</td>
				    <td>采购数量</td>
				    <td>单位</td>
				    <td>单价</td>
				    <td>已接收数量</td>
				    <td>预计到货日期</td>
				    <#--
				    <td>接收数量</td>
				    <td>库位</td>
				    <td>批号</td>
				    -->
				    <td>备注</td>
				    <td>操作</td>
				    <td width="150">上传码单</td>
			    </tr>
    <#if oisgList?has_content>
    	<#assign totalQuantity=0 />
	        	<#assign totalAmount=0 />
		<#list oisgList as oisg>	
			<#assign itemList = delegator.findByAnd("OrderItemShipGroupAssoc", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"shipGroupSeqId",oisg.shipGroupSeqId))?if_exists>
					
			    <#if itemList?has_content>
			    	<#list itemList as shipitem>
			    		<#assign orderItem = delegator.findByPrimaryKey("OrderItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"orderItemSeqId",shipitem.orderItemSeqId))?if_exists>
			    		<#assign product = orderItem.getRelatedOne("Product")>
	    		<tr <#if shipitem_index%2==999>class="alternate-row"</#if>>
				    	
				    	<td>${Static["com.zuczug.product.ZuczugProductUtils"].getProductGoodIdentificationCode(delegator,product.productId,"ZUCZUG_CODE")}</td>
				    	<td>${Static["com.zuczug.product.ZuczugProductUtils"].getProductSupplierCode(delegator,product.productId,"")}</td>
				    	<td>${(orderItem.orderItemSeqId)!}</td>
					    <td>${(product.productName)!}</td>
					    <td>
					    	<#assign groupList = delegator.findByAnd("ProductDesignFeatureWithType", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",product.productId,"productFeatureTypeId","GROUPNAME"))?if_exists>
					    	<#if groupList?has_content>
					    		<#assign groupFeature = delegator.findByPrimaryKey("ProductFeature", Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureId",groupList[0].productFeatureId))?if_exists>
					    		${(groupFeature.description)!}
					    	</#if>
					    </td>
					    <td>
					    	<#assign supplierGv = delegator.findByPrimaryKey("PartyGroup", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId",supplierId))?if_exists>
					    	${(supplierGv.groupName)!}
					    </td>
					   
					    <td>${(product.description)!}</td>
					    <td>
					    	
					    		${(shipitem.quantity)!}
					    		<!-- 数量 -->
						    	<input type="hidden" name="iqm_${(shipitem.orderItemSeqId)!}:${shipitem.shipGroupSeqId}" size="6" value="${(shipitem.quantity)!}">
					    	
					    </td>
					    <td>
					    	<#if product.quantityUomId?has_content>
						    	<#assign uomIdDesc='Uom.description.' + product.quantityUomId?if_exists />
						    	${(uiLabelMap[uomIdDesc])!}
					    	</#if>
					    </td>
					    <td>
					    	
					    		<@ofbizCurrency amount=orderItem.unitPrice isoCode=orderHeader.currencyUom/>
					    		<!-- 价格 -->
						    	<input type="hidden" size="8" name="ipm_${orderItem.orderItemSeqId}" value="${orderItem.unitPrice}"/>
					    	
					    </td>
					    <td>
					    	<#assign receiptQuantity=Static["com.zuczug.order.ZuczugOrderUtils"].getOrderItemShipReceiptedQuantity(delegator,parameters.orderId,orderItem.orderItemSeqId,shipitem.shipGroupSeqId)?if_exists />
				   			${(receiptQuantity)!}
							
					    </td>
					    <td>
					    	<!--如果更新Cancelled掉的点单项会引起报错 -->
					    	<#if orderItem.statusId != "ITEM_CANCELLED">
					    		<#-- 收货时间 
						    	<@htmlTemplate.renderDateTimeField name="isdm_${orderItem.orderItemSeqId}" value="${orderItem.estimatedShipDate?if_exists}" event="" action="" className="" alert="" title="Format: yyyy-MM-dd HH:mm:ss.SSS" size="25" maxlength="30" id="isdm_${orderItem.orderItemSeqId}" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
						    	-->
						    	<!-- 修改价格 -->
						    	<input type="hidden" size="8" name="opm_${orderItem.orderItemSeqId}" value="N"/>
						    	<!--预计到货时间-->
						    	<input type="hidden" name="isdm_${orderItem.orderItemSeqId}" value="${orderItem.estimatedShipDate?if_exists}" size="25" >
						    	
					    	</#if>
					    	${orderItem.estimatedShipDate?if_exists}							    	
					    </td>
					    <td>
							${(orderItem.comments)!}
						</td>
						<td>
							 <#assign itemStatusOkay = (orderItem.statusId != "ITEM_CANCELLED" && orderItem.statusId != "ITEM_COMPLETED")>
							 <#assign receiptQuantity=Static["com.zuczug.order.ZuczugOrderUtils"].getOrderItemShipReceiptedQuantity(delegator,orderItem.orderId,orderItem.orderItemSeqId,'')?if_exists />
							 
							 <#if ((security.hasEntityPermission("ORDERMGR", "_UPDATE", session)) && itemStatusOkay && orderHeader.statusId != "ORDER_SENT") || (security.hasEntityPermission("ORDERMGR", "_ADMIN", session) && itemStatusOkay)>
								<#if receiptQuantity gt 0>
									已收货${receiptQuantity}件
								<#else>
									<#if orderHeader.statusId == "ORDER_CREATED">
									<a href="javascript:document.updateItemInfo.action='<@ofbizUrl>cancelOrderItem</@ofbizUrl>';
									document.updateItemInfo.orderItemSeqId.value='${orderItem.orderItemSeqId}';
									document.updateItemInfo.shipGroupSeqId.value='${oisg.shipGroupSeqId}';
									document.updateItemInfo.submit()" class="buttontext">${uiLabelMap.CommonCancel}</a>
									</#if>
								</#if>								
							 <#else>
                                &nbsp;
                             </#if>
						</td>
						
					   	</tr>
					   	 <#assign totalAmount=totalAmount+orderItem.unitPrice*shipitem.quantity />
		            	<#assign totalQuantity=totalQuantity+shipitem.quantity />
			    	</#list>
			    </#if>			    
			
		</#list>
		</tbody>
			</table>
	</#if>
	<div>
		
		${uiLabelMap.Sumtotal}：
		总数量:${(totalQuantity)!}
		总价格:<@ofbizCurrency amount=totalAmount isoCode=cart.getCurrency()/>
	
	</div>
</div>   
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                货运组列表
            </li>
            <#if orderHeader.statusId=='ORDER_APPROVED'>
            <li><a href="<@ofbizUrl>EditOrderShipmentGroups?purchaseType=${purchaseType}&amp;orderId=${(parameters.orderId)!}</@ofbizUrl>">编辑货运组</a></li>
            <li><a onclick="return window.confirm('创建一个新的货运组？\n运送目的地可以在[编辑订单项]功能中修改')" href="<@ofbizUrl>createOrderShipmentGroups?purchaseType=${purchaseType}&amp;orderId=${(parameters.orderId)!}</@ofbizUrl>">新增货运组</a></li>
            </#if>
            
        </ul>
        <br class="clear">
    </div>
    
    
<#if oisgList?has_content>
	<#assign totalQuantity=0 />
	        	<#assign totalAmount=0 />
	<#list oisgList as oisg>
		<#assign itemList = delegator.findByAnd("OrderItemShipGroupAssoc", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"shipGroupSeqId",oisg.shipGroupSeqId))?if_exists>
		<#if oisg.facilityId?has_content>
			<#assign facility = oisg.getRelatedOne("Facility")>
		<#else>
			<#assign facility = orderHeader.getRelatedOne("OriginFacility")>
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
			    <td>素然物料编号</td>
			    <td>供应商物料编号</td>
			    <td>序列号</td>
			    <td>物料名称</td>
		    	
			    <td>供应商</td>
			    
			    <td>规格</td>
			    <td>采购数量</td>
			    <td>单位</td>
			    <td>物料数量/单位</td>
			    <td>单价</td>
			    <td>已接收数量</td>
			    <td>预计到货日期</td>
			    <#--
			    <td>接收数量</td>
			    <td>库位</td>
			    <td>批号</td>
			    -->
			    <td>备注</td>
			    <td>关键字</td>
			    <td>状态</td>
		    </tr>
		    <#if itemList?has_content>
		    	
		    	<#list itemList as shipitem>
		    		<#assign orderItem = delegator.findByPrimaryKey("OrderItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"orderItemSeqId",shipitem.orderItemSeqId))?if_exists>
		    		<#assign product = orderItem.getRelatedOne("Product")>
    		<tr <#if shipitem_index%2==999>class="alternate-row"</#if>>
			    	<td>${Static["com.zuczug.product.ZuczugProductUtils"].getProductGoodIdentificationCode(delegator,product.productId,"ZUCZUG_CODE")?if_exists}</td>
			    	<td>${Static["com.zuczug.product.ZuczugProductUtils"].getProductSupplierCode(delegator,product.productId,"")?if_exists}</td>
			    	<td>${(orderItem.orderItemSeqId)!}</td>
				    <td>${(product.productName)!}</td>
				    
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
				    	<#assign secQuantity = delegator.findByPrimaryKey("OrderItemAttribute", Static["org.ofbiz.base.util.UtilMisc"].toMap("attrName","secQuantity","orderId",orderItem.orderId,"orderItemSeqId",orderItem.orderItemSeqId))?if_exists>
				    	<#assign secUom = delegator.findByPrimaryKey("OrderItemAttribute", Static["org.ofbiz.base.util.UtilMisc"].toMap("attrName","secUom","orderId",orderItem.orderId,"orderItemSeqId",orderItem.orderItemSeqId))?if_exists>
				    	${(secQuantity.attrValue)!}（${(secUom.attrValue)!}）
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
				    
				    <td>
						${(orderItem.comments)!}
					</td>
					<td><!--关键字预留位置--></td>
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
				    <#assign totalAmount=totalAmount+orderItem.unitPrice*shipitem.quantity />
		            <#assign totalQuantity=totalQuantity+shipitem.quantity />
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