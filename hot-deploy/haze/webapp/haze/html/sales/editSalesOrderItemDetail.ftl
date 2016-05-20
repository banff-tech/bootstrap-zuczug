<script language="javascript" type="text/javascript" src="/zuczugcommon/upload/plugins/imageupload/jQuery.upload.js"></script>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.AddFacbricPurchase}
            </li>
        	<li><a href="<@ofbizUrl>FindSalesOrderItem?orderId=${(parameters.orderId)!}</@ofbizUrl>">结束编辑</a></li>
        </ul>
        <br class="clear">
    </div>
    <form method="post" action="<@ofbizUrl>updateOrderItems</@ofbizUrl>" name="updateItemInfo" class="basic-form">
    <input type="hidden" name="orderId" value="${(orderHeader.orderId)!}">
    <input type="hidden" name="supplierPartyId" value="${(supplierId)!}">
    <input type="hidden" name="orderTypeId" value="${(orderHeader.orderTypeId)!}">
    <input type="hidden" name="orderItemSeqId">
    <input type="hidden" name="shipGroupSeqId">
	<#if oisgList?has_content>
		<#list oisgList as oisg>
			<#assign itemList = delegator.findByAnd("OrderItemShipGroupAssoc", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"shipGroupSeqId",oisg.shipGroupSeqId))?if_exists>
			<#if oisg.facilityId?has_content>
				<#assign facility = oisg.getRelatedOne("Facility")>
			<#else>
				<#assign facility = orderHeader.getRelatedOne("OriginFacility")>
			</#if>
			
			<b>${uiLabelMap.ShipGroup} ${(oisg_index+1)!} / 
			${uiLabelMap.OutFacility}：
			<select name="oisg_${(oisg.shipGroupSeqId)!}">
				<#list facilityList as f>
					<option value="${(f.facilityId)!}" <#if facility.facilityId==f.facilityId>selected</#if>>${(f.facilityName)!}</option>
				</#list>
			</select>
			
			 / ${uiLabelMap.PlanDeliveryDate}：
			 <#if oisg.estimatedShipDate?has_content><#assign thisDate=(oisg.estimatedShipDate)?string('yyyy-MM-dd hh:mm:ss') /></#if>
			<@htmlTemplate.renderDateTimeField name="oisgEstDate_${(oisg.shipGroupSeqId)!}" value="${(thisDate)!}" event="" action="" className="" alert="" title="Format: yyyy-MM-dd HH:mm:ss.SSS" size="25" maxlength="30" id="oisgEstDate_${(oisg.shipGroupSeqId)!}" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
			 </b>
			<div id="searchOptions_col" class="screenlet-body" style="border-bottom: #9d9d9b thin solid;overflow-y: auto;">
			<div id="search-options">
			<table cellspacing="0" class="basic-table" style="width: 120%;">
			    <tbody>
			    <tr class="header-row-2">
				    <td>${uiLabelMap.ProductId}</td>
				    <td>${uiLabelMap.ProductName}</td>
				    <td>${uiLabelMap.Quantity}</td>
				    <td>${uiLabelMap.UnitPrice}</td>
				    <td>${uiLabelMap.ReceiverQuantity}</td>
				    <td>${uiLabelMap.PlanDeliveryDate}</td>
				    <td>${uiLabelMap.Operation}</td>
				   </tr>
			    <#if itemList?has_content>
			    	<#list itemList as shipitem>
			    		<#assign orderItem = delegator.findByPrimaryKey("OrderItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId",parameters.orderId,"orderItemSeqId",shipitem.orderItemSeqId))?if_exists>
			    		<#assign product = orderItem.getRelatedOne("Product")>
	    		<tr <#if shipitem_index%2==0>class="alternate-row"</#if>>
				    	<td>${(orderItem.productId)!}</td>
					    <td>${(product.productName)!}</td>
					    <td>${(shipitem.quantity)!}</td>
					    <td>
					    	<@ofbizCurrency amount=orderItem.unitPrice isoCode=orderHeader.currencyUom/>
					    </td>
					    <td>
					    	<#assign receiptQuantity=Static["com.zuczug.order.ZuczugOrderUtils"].getOrderItemShipReceiptedQuantity(delegator,parameters.orderId,orderItem.orderItemSeqId,shipitem.shipGroupSeqId)?if_exists />
					   		${(receiptQuantity)!}
					    </td>
					    <td>
					    	<!--如果更新Cancelled掉的点单项会引起报错 -->
					    	<#if orderItem.statusId != "ITEM_CANCELLED">
						    	<!-- 价格 -->
						    	<input type="hidden" size="8" name="ipm_${orderItem.orderItemSeqId}" value="${orderItem.unitPrice}"/>
						    	<!-- 修改价格 -->
						    	<input type="hidden" size="8" name="opm_${orderItem.orderItemSeqId}" value="N"/>
						    	<!-- 数量 -->
						    	<input type="hidden" name="iqm_${(shipitem.orderItemSeqId)!}:${shipitem.shipGroupSeqId}" size="6" value="${(shipitem.quantity)!}">
					    	</#if>
							<#if orderItem.estimatedDeliveryDate?has_content>
				    			${(orderItem.estimatedDeliveryDate)?string('yyyy-MM-dd')}
				    		</#if>
					    </td>
						<td>
							 <#assign itemStatusOkay = (orderItem.statusId != "ITEM_CANCELLED" && orderItem.statusId != "ITEM_COMPLETED")>
							 <#assign receiptQuantity=Static["com.zuczug.order.ZuczugOrderUtils"].getOrderItemShipReceiptedQuantity(delegator,orderItem.orderId,orderItem.orderItemSeqId,'')?if_exists />
							 
							 <#if ((security.hasEntityPermission("ORDERMGR", "_UPDATE", session)) && itemStatusOkay && orderHeader.statusId != "ORDER_SENT") || (security.hasEntityPermission("ORDERMGR", "_ADMIN", session) && itemStatusOkay)>
								<#if receiptQuantity gt 0>
									${uiLabelMap.ReceiveYet}${receiptQuantity}${uiLabelMap.Piece}
								<#else>
									<#if orderHeader.statusId == "ORDER_CREATED" || orderHeader.statusId == "ORDER_PROCESSING">
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
			    	</#list>
			    </#if>			    
			</tbody>
			</table>
			</div>
			</div>
			<br/>
		</#list>
	</#if>
	
	<input name="submitBtn" value="${uiLabelMap.Save}" type="submit">
	</form>
</div>

<#if orderHeader.statusId == "ORDER_CREATED">
<div id="screenlet_4_col" class="screenlet">
	<div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                ${uiLabelMap.AddProductItem}
            </li>
        </ul>
        <br class="clear">
    </div>
   
	    <form method="post" action="<@ofbizUrl>appendItemToSalesOrder</@ofbizUrl>" id="appendItemForm" name="appendItemForm" onsubmit="return validateAndSubmit()">
	    	<input type="hidden" name="MODE" value="ADD_ITEM"/>
	    	<input type="hidden" name="orderId" value="${(orderHeader.orderId)!}"/>
	    	<table cellspacing="0" class="basic-table">
			    <tbody>
		    <tr>
		        <td class="label">ChooseProduct：</td>
	            <td>
	            	<@htmlTemplate.lookupField formName="appendItemForm" width="800" name="productId" fieldFormName="LookupRealProductNoVirtualForSkuNoAjex" value="" className="required" maxlength="50"/>
	            </td>
	            <td class="label">${uiLabelMap.Quantity}：</td>
	            <td><input type="text" name="quantity" class="required integerNumber"/></td>
	        </tr>
	        <tr>
		        <td class="label">${uiLabelMap.PlanDeliveryDate}：</td>
		        <td>
		        	<@htmlTemplate.renderDateTimeField name="itemDesiredDeliveryDate" event="" action="" value="" className="" alert="" title="Format: yyyy-MM-dd HH:mm:ss.SSS" size="25" maxlength="30" id="itemDesiredDeliveryDate1" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
		        	<script>
		        	$(function(){
		        		$("#estimatedShipDate_i18n").addClass("required");
		        	});
		        	</script>
		        </td>
		        <td class="label">&nbsp;</td>
		        <td>&nbsp;</td>
		    </tr>
		    </tbody>
			</table>
			<input name="shipGroupSeqId" type="hidden" value="1"/>
			<input name="submitBtn" value="${uiLabelMap.AddNew}" type="submit">
		</form>
	
    </div>
</div>




</#if>
<script>
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

	$("input[name='productId']").bind('input', function(){
		getProductInfo();
    });
    
    $("input[name='productId']").bind('propertychange', function(){
		getProductInfo();
    });
    
    $("input[name='productId']").bind('blur', function(){
		getProductInfo();
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
	
	function uploadShipmentExcel(orderId,orderItemSeqId,shipGroupSeqId){
		var fileId="file_md_"+orderItemSeqId+"_"+shipGroupSeqId;
		if($("#"+fileId).val().length <= 0){
			showErrorAlert("${uiLabelMap.Tip}","${uiLabelMap.NeedChooseFile}");
			return false;
		}
		$("#"+fileId).upload({
		    action: "<@ofbizUrl>uploadShipGroupDetailExcel</@ofbizUrl>",
		    data:{
		    	orderId:orderId,
		    	orderItemSeqId:orderItemSeqId,
		    	shipGroupSeqId:shipGroupSeqId
		    },
		    oncomplete:function(result){
		    	if("dataResource" in result){
		    		alert(result['_EVENT_MESSAGE_']);
		    		window.location.reload();
		    	}else{
		    		showErrorAlert("${uiLabelMap.Error}",result['_ERROR_MESSAGE_']);
		    	}
		    }
		});
	}
	
	function validateAndSubmit(){
		var productId = $("input[name='productId']").val();
		var quantity = $("input[name='quantity']").val();
		if(productId == "" || quantity == "") {
			alert("${uiLabelMap.NeedProductAndQuantity}");
			return false;
		} else {
			return true;
		}
	}
</script>