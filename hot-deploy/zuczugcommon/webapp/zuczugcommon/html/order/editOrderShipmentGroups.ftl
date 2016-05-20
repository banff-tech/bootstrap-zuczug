<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                订单项列表
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
		<b>货运组 ${(oisg.shipGroupSeqId)!} / 接受仓库：${(facility.facilityName)!}  
		/ 预计到货日期：
		<#if oisg.estimatedShipDate?has_content>${(oisg.estimatedShipDate)?string('yyyy-MM-dd hh:mm:ss')}</#if>
		</b>
		<div id="searchOptions_col" class="screenlet-body" style="border-bottom: #9d9d9b thin solid;">
		<div id="search-options">
		<table cellspacing="0" class="basic-table">
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
			    <td>已接收数量</td>
			    <td>预计到货日期</td>
			    <td>备注</td>
			    <td>状态</td>
			    <td>操作</td>
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
				    	<#assign groupList = delegator.findByAnd("ProductDesignFeatureWithType", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",product.productId,"productFeatureTypeId","GROUPNAME"))?if_exists>
				    	<#if groupList?has_content>
				    		<#assign groupFeature = delegator.findByPrimaryKey("ProductFeature", Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureId",groupList[0].productFeatureId))?if_exists>
				    		${(groupFeature.description)!}
				    	</#if>
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
					<td>
						<#if (orderItem.statusId == "ITEM_APPROVED") && needQuantity gt 0><!-- 只有已批准的才可以 -->
						<form id="updateForm_${(parameters.orderId)!}_${(oisg.shipGroupSeqId)!}_${(orderItem.orderItemSeqId)!}" method="post" action="<@ofbizUrl>updateOrderShipGroupAssoc</@ofbizUrl>" onsubmit="return updateOrderShipGroupAssoc('${(parameters.orderId)!}','${(oisg.shipGroupSeqId)!}','${(orderItem.orderItemSeqId)!}')">
						<input type="hidden" name="orderId" value="${(parameters.orderId)!}"/>
						<input type="hidden" name="shipGroupSeqId" value="${(oisg.shipGroupSeqId)!}"/>
						<input type="hidden" name="orderItemSeqId" value="${(orderItem.orderItemSeqId)!}"/>
						<input type="hidden" name="purchaseType" value="${purchaseType}">
						
						移动<input type="input" style="height:20px;width:30px" name="moveQuantity" maxQuantity="${(needQuantity)!0}" id="moveQuantity_${(parameters.orderId)!}_${(oisg.shipGroupSeqId)!}_${(orderItem.orderItemSeqId)!}">到
						<select name="moveShipGroup" id="moveShipGroup_${(parameters.orderId)!}_${(oisg.shipGroupSeqId)!}_${(orderItem.orderItemSeqId)!}">
							<#if oisgList?has_content>
								<#list oisgList as sg>
									<#if sg.shipGroupSeqId!=oisg.shipGroupSeqId>
									<option value="${(sg.shipGroupSeqId)!}">货运组${(sg.shipGroupSeqId)!}</option>
									</#if>
								</#list>
							</#if>
						</select>
						<input type="submit" value="确定"/>
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
		showErrorAlert("提示","移动数量不能为空");
		return false;
	}
	
	if(isNaN(moveQuantity)){
		showErrorAlert("提示","必须输入数字");
		return false;
	}
	if(Number(moveQuantity) > maxQuantity){
		showErrorAlert("提示","未收货数量 " + maxQuantity + "，移动的数量不能大于这个数量");
		return false;
	}
	//$("#updateForm_" + orderId+"_"+shipGroupSeqId+"_"+orderItemSeqId).submit();
}
</script>