<#if shipment?exists>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.PageTitleEditShipmentItems}&nbsp;&nbsp;&nbsp;
            	送货编号:[${shipment.shipmentId}]&nbsp;&nbsp;&nbsp;
            <#assign ShipmentItemTotalNumGroupShipmentId = delegator.findByAndCache("ShipmentItemTotalNumGroupShipmentId",Static["org.ofbiz.base.util.UtilMisc"].toMap("shipmentId",shipment.shipmentId)) />
            <#if ShipmentItemTotalNumGroupShipmentId[0]?exists>
    			总配货数量:${(ShipmentItemTotalNumGroupShipmentId[0].get("quantity")?default("0"))}
    		</#if>
    		</li>
        </ul>
        <br class="clear"/>
		<#if shipment.statusId == "SHIPMENT_INPUT">
			<#assign postalAddress1 = delegator.findByAndCache("PostalAddress", Static["org.ofbiz.base.util.UtilMisc"].toMap("contactMechId",shipment.destinationContactMechId))?if_exists>
	    	<a href="javascript:if(confirm('确定后无法编辑送货内容!\r总配货数量:${(ShipmentItemTotalNumGroupShipmentId[0].get("quantity")?default("0"))}\r收货地址:${postalAddress1[0].get("address1")}\r\r确定继续?')){document.UpdateShipmentStatusToPlane.submit()}" target="_blank" class="buttontext">确定送货</a>
	        <form name="UpdateShipmentStatusToPlane" method="post" action="<@ofbizUrl>updateShipmentStatusToPlane</@ofbizUrl>">
	            <input type="hidden" name="shipmentId" value="${shipmentId}"/>
			</form>
		</#if>
</div>

<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                	编辑收货地址
            </li>
        </ul>
        <br class="clear">
    </div>
	<form name="UpdateShipmentAddress" method="post" action="<@ofbizUrl>UpdateShipmentAddress</@ofbizUrl>">
		<input type="hidden" name="shipmentId" value="${shipmentId}"/>
		<table cellspacing="0" class="basic-table">
			<tr>
				<td>
					<select name="destinationContactMechId" class="required">
						<#assign postalAddress = delegator.findByAndCache("PostalAddress", Static["org.ofbiz.base.util.UtilMisc"].toMap("contactMechId",shipment.destinationContactMechId))?if_exists>
							<#if postalAddress[0]?exists>
		    					<option value="${postalAddress[0].get("contactMechId")}">${postalAddress[0].get("address1")}</option>
		    				<#else>
		    					<option value="">请选择</option>
		    				</#if>
						<#assign shipmentAddressList = delegator.findByAnd("FindFacilityContactMech", Static["org.ofbiz.base.util.UtilMisc"].toMap("facilityId",shipment.destinationFacilityId,"contactMechPurposeTypeId","SHIPPING_LOCATION"))?if_exists>
						<#list shipmentAddressList as shipmentAddress>
							<option value="${shipmentAddress.contactMechId}">
								${shipmentAddress.address1}
							</option>
						</#list>
					</select>
				</td>
			</tr>
			<tr>
				<td>
					<input type="submit" value="提交"/>
				</td>
			</tr>
		</table>
	</form>
</div>


<#if shipment.statusId == "SHIPMENT_INPUT">
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">
                	查询商品
            </li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>queryProductRealView</@ofbizUrl>" id="queryProductRealView" name="queryProductRealView">
    	<input type="hidden" name="shipmentId" value="${shipmentId}"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr>
		        <td class="label">查询款号：</td>
	            <td>
	            	<@htmlTemplate.lookupField formName="queryProductRealView" width="800" name="productId" fieldFormName="LookupRealProductNoVariantForSku" value="" className="required" maxlength="50"/>
	            </td>

	            <td>
					<input type="submit" value="查询"/>
				</td>
	        </tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>
</#if>
<#if productRealViews?has_content>
<div class="screenlet">
	<div class="screenlet-title-bar">
        <ul>
            <li class="h3">款号明细</li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>createShipmentItem</@ofbizUrl>" id="createShipmentItem">
   		<input type="hidden" name="shipmentId" value="${shipmentId}"/>
   		<input type="hidden" name="productGroup" value="${productGroup}"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>
		    <tr class="header-row-2">
		    <#list productRealViews as productRealView>
		    	<td>
		    		${(productRealView.get("productId"))}
		    	</td>
		    </#list>
	        </tr>
	        <tr class="alternate-row">
			<#list productRealViews as productRealView>
				<#assign inventoryItemGroupbyProductId = delegator.findByAndCache("InventoryItemGroupbyProductId",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId",shipment.destinationFacilityId)) />
				<#assign b2cGroupbyProductId = delegator.findByAndCache("InventoryItemGroupbyProductId",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId","B2C_WAREHOUSE")) />
				<#assign clothesGroupbyProductId = delegator.findByAndCache("InventoryItemGroupbyProductId",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId","ZUCZUG_CLOTHESFACILITY")) />
		    	<#assign facilityManufactProductNum = delegator.findByAndCache("FacilityManufactProductNum",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId",shipment.originFacilityId)) />
		    	<#assign ShipmentShippedTotalNum = delegator.findByAndCache("ShipmentShippedTotalNum",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"originFacilityId",shipment.originFacilityId,"destinationFacilityId",shipment.destinationFacilityId)) />
		    	<#assign alreadySalesProductNum = delegator.findByAndCache("AlreadySalesProductNum",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"))) />
		    	<td>
		    		数量:<input type="text" id="quantity_${(productRealView.get("productId"))}" name="quantity_${(productRealView.get("productId"))}" value="" >
		    		<br>
		    		<#if inventoryItemGroupbyProductId[0]?exists>
		    			店铺库存可用/物理数量:${(inventoryItemGroupbyProductId[0].get("availableToPromiseTotal")?default("0"))}/${(inventoryItemGroupbyProductId[0].get("quantityOnHandTotal")?default("0"))}
		    		<#else>
		    			店铺库存可用/物理数量:0/0
		    		</#if>
		    		<br>
		    		<#if alreadySalesProductNum[0]?exists>
		    			店铺已销售数量:${(alreadySalesProductNum[0].get("quantity")?default("0"))}
		    		<#else>
		    			店铺已销售数量:0
		    		</#if>
		    		<br>
		    		<#if ShipmentShippedTotalNum[0]?exists>
		    			在途数量:${(ShipmentShippedTotalNum[0].get("quantity")?default("0"))}
		    		<#else>
		    			在途数量:0
		    		</#if>
		    		<br>
		    		<#if b2cGroupbyProductId[0]?exists>
		    			电商仓可用/物理数量:${(b2cGroupbyProductId[0].get("availableToPromiseTotal")?default("0"))}/${(b2cGroupbyProductId[0].get("quantityOnHandTotal")?default("0"))}
		    		<#else>
		    			电商仓可用/物理数量:0/0
		    		</#if>
		    		<br>
		    		<#if clothesGroupbyProductId[0]?exists>
		    			成衣仓可用/物理数量:${(clothesGroupbyProductId[0].get("availableToPromiseTotal")?default("0"))}/${(clothesGroupbyProductId[0].get("quantityOnHandTotal")?default("0"))}
		    		<#else>
		    			成衣仓可用/物理数量:0/0
		    		</#if>
		    		<br>
		    		<#if facilityManufactProductNum[0]?exists>
		    			已生产数量:${(facilityManufactProductNum[0].get("quantityProduced")?default("0"))}
		    		<#else>
		    			已生产数量:0
		    		</#if>
		    		
		    	</td>
		    </#list>
		    </tr>
		    <tr>
		    	<td align="left">
					<input type="submit" value="提交"/>
				</td>
		    </tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>
</#if>

    <div class="screenlet-body">
        <table cellspacing="0" class="basic-table">
            <tr class="header-row">
                <td>${uiLabelMap.ProductItem}</td>
                <td>产品标识</td>
                <td>&nbsp;</td>
                <td>配货数量</td>
                <td></td>
                <td>库存可用数量</td>
                <td>需求数量</td>
                <td>&nbsp;</td>
            </tr>
        <#assign alt_row = false>
        <#list shipmentItemDatas as shipmentItemData>
            <#assign shipmentItem = shipmentItemData.shipmentItem>
            <#assign itemIssuances = shipmentItemData.itemIssuances>
            <#assign orderShipments = shipmentItemData.orderShipments>
            <#assign shipmentPackageContents = shipmentItemData.shipmentPackageContents>
            <#assign product = shipmentItemData.product?if_exists>
            <#assign totalQuantityPackaged = shipmentItemData.totalQuantityPackaged>
            <#assign totalQuantityToPackage = shipmentItemData.totalQuantityToPackage>
            <#assign totalInventoryQuantity = shipmentItemData.totalInventoryQuantity>
            <#assign totalRequirementQuantity = shipmentItemData.totalRequirementQuantity>
            <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                <td>${shipmentItem.shipmentItemSeqId}</td>
                <td colspan="2">${shipmentItem.productId} ${(product.internalName)}</td>
                <td>${shipmentItem.quantity?default("&nbsp;")}</td>
                <td>${shipmentItem.shipmentContentDescription?default("&nbsp;")}</td>
                <td>${totalInventoryQuantity?default("0")}</td>
                <td>${totalRequirementQuantity?default("0")}</td>
            	<#if shipment.statusId == "SHIPMENT_INPUT">
             		<td><a href="javascript:document.deleteShipmentItem${shipmentItemData_index}.submit();" class="buttontext">${uiLabelMap.CommonDelete}</a></td>
           		<#else >
             		<td></td>
           		</#if>
            </tr>
            <form name="deleteShipmentItem${shipmentItemData_index}" method="post" action="<@ofbizUrl>deleteShipmentItem</@ofbizUrl>">
                <input type="hidden" name="shipmentId" value="${shipmentId}"/>
                <input type="hidden" name="shipmentItemSeqId" value="${shipmentItem.shipmentItemSeqId}"/>
            </form>
            <#list orderShipments as orderShipment>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                    <td>&nbsp;</td>
                    <td><span class="label">${uiLabelMap.ProductOrderItem}</span> <a href="/ordermgr/control/orderview?orderId=${orderShipment.orderId?if_exists}" class="buttontext">${orderShipment.orderId?if_exists}</a> ${orderShipment.orderItemSeqId?if_exists}</td>
                    <td>&nbsp;</td>
                    <td>${orderShipment.quantity?if_exists}</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;<#-- don't allow a delete, need to implement a cancel issuance <a href="<@ofbizUrl>deleteShipmentItemIssuance?shipmentId=${shipmentId}&amp;itemIssuanceId=${itemIssuance.itemIssuanceId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonDelete}</a> --></td>
                </tr>
            </#list>

            
            <#-- toggle the row color -->
            <#assign alt_row = !alt_row>
        </#list>


        </table>
    </div>
</div>



<#else>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.ProductShipmentNotFoundId} : [${shipmentId?if_exists}]</li>
        </ul>
        <br class="clear"/>
    </div>
</div>
</#if>