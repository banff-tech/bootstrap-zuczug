<div class="screenlet">
    <div id="screenlet_4_col" class="screenlet-body">
    <form method="post" action="<@ofbizUrl>queryQuoteProductRealView</@ofbizUrl>" id="queryQuoteProductRealView" name="queryQuoteProductRealView" class="form-horizontal">
    	<input type="hidden" name="quoteId" value="${quoteId}"/>

        <div class="form-group">
            <label class="col-md-2 control-label" id="">${uiLabelMap.FindProductGroup}：</label>
            <div class="col-sm-4">
				<@htmlTemplate.lookupField formName="queryQuoteProductRealView" width="800" name="productId" fieldFormName="LookupRealProductNoVariantForSku" value="" className="required" maxlength="50"/>
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-2 control-label" id="">&nbsp;</label>
            <div class="col-sm-4">
                <input type="submit" value="${uiLabelMap.Find}" class="btn btn-sm btn-primary"/>
            </div>
        </div>
	</form>
    </div>
</div>

<#if productRealViews?has_content>
<div class="screenlet">
	<div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.ProductGroupDetail}</li>
        </ul>
        <br class="clear">
    </div>
    <div id="screenlet_4_col" class="screenlet-body">
   	<form method="post" action="<@ofbizUrl>createQuoteItem</@ofbizUrl>" id="createQuoteItem">
   		<input type="hidden" name="quoteId" value="${quoteId}"/>
   		<input type="hidden" name="productGroup" value="${productGroup}"/>
    	<table cellspacing="0" class="basic-table">
		    <tbody>xxxxxxxxxxxxx
		    <tr class="header-row-2">
		    <#list productRealViews as productRealView>
		    	<td>${(productRealView.get("productId"))}</td>
		    </#list>
	        </tr>
	        <tr class="alternate-row">
			<#list productRealViews as productRealView>
				<#assign quoteList = delegator.findByAndCache("Quote",Static["org.ofbiz.base.util.UtilMisc"].toMap("quoteId",quoteId)) />
				<#assign facilityList = delegator.findByAndCache("Facility",Static["org.ofbiz.base.util.UtilMisc"].toMap("facilityId",quoteList[0].partyId)) />
				<#assign inventoryItemGroupbyProductId = delegator.findByAndCache("InventoryItemGroupbyProductId",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId",facilityList[0].facilityId)) />
		    	<#assign facilityManufactProductNum = delegator.findByAndCache("FacilityManufactProductNum",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId",facilityList[0].facilityId)) />
		    	<#assign alreadySalesProductNum = delegator.findByAndCache("AlreadySalesProductNum",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"))) />
		    	<#assign b2cGroupbyProductId = delegator.findByAndCache("InventoryItemGroupbyProductId",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId","B2C_WAREHOUSE")) />
		    	<#assign clothesGroupbyProductId = delegator.findByAndCache("InventoryItemGroupbyProductId",Static["org.ofbiz.base.util.UtilMisc"].toMap("productId",productRealView.get("productId"),"facilityId","ZUCZUG_CLOTHESFACILITY")) />
		    	<td>
		    		${uiLabelMap.Quantity}:<input type="text" id="quantity_${(productRealView.get("productId"))}" name="quantity_${(productRealView.get("productId"))}" value="0" >
		    		<br>
		    		${uiLabelMap.Price}:<input type="text" id="price_${(productRealView.get("productId"))}" name="price_${(productRealView.get("productId"))}" value="" >
					<br>
		    		<#if inventoryItemGroupbyProductId[0]?exists>
		    			${uiLabelMap.StoreAvailableNum}/${uiLabelMap.StoreOnHandNum}:${(inventoryItemGroupbyProductId[0].get("availableToPromiseTotal")?default("0"))}/${(inventoryItemGroupbyProductId[0].get("quantityOnHandTotal")?default("0"))}
		    		<#else>
		    			${uiLabelMap.StoreAvailableNum}/${uiLabelMap.StoreOnHandNum}:0/0
		    		</#if>
		    		<br>
		    		<#if alreadySalesProductNum[0]?exists>
		    			${uiLabelMap.StoreSalesYet}:${(alreadySalesProductNum[0].get("quantity")?default("0"))}
		    		<#else>
		    			${uiLabelMap.StoreSalesYet}:0
		    		</#if>
		    		<br>
		    		<#if b2cGroupbyProductId[0]?exists>
		    			${uiLabelMap.B2cStoreAvailableNum}/${uiLabelMap.StoreOnHandNum}:${(b2cGroupbyProductId[0].get("availableToPromiseTotal")?default("0"))}/${(b2cGroupbyProductId[0].get("quantityOnHandTotal")?default("0"))}
		    		<#else>
		    			${uiLabelMap.B2cStoreAvailableNum}/${uiLabelMap.StoreOnHandNum}:0/0
		    		</#if>
		    		<br>
		    		<#if clothesGroupbyProductId[0]?exists>
		    			${uiLabelMap.ClothesStoreAvailableNum}/${uiLabelMap.StoreOnHandNum}:${(clothesGroupbyProductId[0].get("availableToPromiseTotal")?default("0"))}/${(clothesGroupbyProductId[0].get("quantityOnHandTotal")?default("0"))}
		    		<#else>
		    			${uiLabelMap.ClothesStoreAvailableNum}/${uiLabelMap.StoreOnHandNum}:0/0
		    		</#if>
		    		<br>
		    		<#if facilityManufactProductNum[0]?exists>
		    			${uiLabelMap.FacilityManufactYet}:${(facilityManufactProductNum[0].get("quantityProduced")?default("0"))}
		    		<#else>
		    			${uiLabelMap.FacilityManufactYet}:0
		    		</#if>
		    		
		    	</td>

		    </#list>
		    </tr>
		    <tr>
		    	<td align="left">
					<input type="submit" value="${uiLabelMap.Submit}"/>
				</td>
		    </tr>
		    </tbody>
		</table>
	</form>
    </div>
</div>
</#if>