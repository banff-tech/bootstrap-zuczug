package com.zuczug.haze;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class SalesReturnServices {
	/**
	 * 创建退货单
	 * by liujia
	 */
	public static Map<String, Object> createSalesReturnHeader(DispatchContext dctx, Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> resultMap = ServiceUtil.returnError("");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

		String returnHeaderTypeId = "CUSTOMER_RETURN";
		String statusId = "RETURN_REQUESTED";
		String createdBy = userLogin.getString("userLoginId");
		String fromPartyId = (String) context.get("fromPartyId");
		String toPartyId = (String) context.get("toPartyId");
		String paymentMethodId = (String) context.get("paymentMethodId");
		String finAccountId = (String) context.get("finAccountId");

		//String entryDate = nowTimestamp;
		String originContactMechId = (String) context.get("originContactMechId");
		String destinationFacilityId = "";
		String currencyUomId = (String) context.get("currencyUomId");
		String needsInventoryReceive = (String) context.get("needsInventoryReceive");
		String billingAccountId = "";
		String returnId = "";
        try {
        	GenericValue billingAccount = EntityUtil.getFirst(delegator.findByAnd("BillingAccountAndRole", UtilMisc.toMap("partyId",fromPartyId)));
        	if(billingAccount!=null&&!billingAccount.isEmpty()){
        		billingAccountId = billingAccount.getString("billingAccountId");
        	}else{
        		return ServiceUtil.returnError("该客户没有账单账户");
        	}
        	
        	GenericValue productStore = EntityUtil.getFirst(delegator.findByAnd("ProductStore", UtilMisc.toMap("productStoreId",toPartyId)));
        	if(productStore!=null&&!productStore.isEmpty()){
        		destinationFacilityId = productStore.getString("inventoryFacilityId");
            	if(destinationFacilityId.isEmpty()){
            		return ServiceUtil.returnError("目的地没有关联退货场所");
            	}
        	}else{
        		return ServiceUtil.returnError("目的地退货场所不存在");
        	}
        	
        	Map<String, Object> serviceResult = dispatcher.runSync("createReturnHeader", UtilMisc.toMap("userLogin", userLogin,
					"entryDate",nowTimestamp,"returnHeaderTypeId", returnHeaderTypeId, "statusId",statusId,"createdBy",createdBy,
					"fromPartyId",fromPartyId,"toPartyId",toPartyId,"paymentMethodId",paymentMethodId,"finAccountId",finAccountId,
					"billingAccountId",billingAccountId,"originContactMechId",originContactMechId,"destinationFacilityId",destinationFacilityId,
					"needsInventoryReceive",needsInventoryReceive,"currencyUomId",currencyUomId));
            if (ServiceUtil.isError(serviceResult)) {
            	return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceResult));
            }
        	returnId = (String) serviceResult.get("returnId");
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("returnId", returnId);
        return result;
	}
}