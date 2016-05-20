package com.zuczug.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.manufacturing.bom.BOMNode;
import org.ofbiz.manufacturing.bom.BOMTree;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;



public class FacilityUtilServices {
	
	public static final String module = FacilityUtilServices.class.getName();
    public static final String resource = "ZuczugCommonUiLabels";
	
    /**
     * 根据inventoryTransfer，生成shipment对象
     */
    public static Map<String, Object> createTransferShipment(DispatchContext dctx, Map<String, ? extends Object> context){
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String inventoryTransferId = (String) context.get("inventoryTransferId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        try{
        	GenericValue inventoryTransfer = delegator.findByPrimaryKey("InventoryTransfer",
        			UtilMisc.toMap("inventoryTransferId", inventoryTransferId));
        	String originFacilityId = inventoryTransfer.getString("facilityId");
        	String destinationFacilityId = inventoryTransfer.getString("facilityIdTo");
        	GenericValue inventoryItem = inventoryTransfer.getRelatedOne("InventoryItem");
        	String inventoryItemId = inventoryItem.getString("inventoryItemId");
        	String productId = inventoryItem.getString("productId");
        	BigDecimal quantity = inventoryItem.getBigDecimal("quantityOnHandTotal");
        	// create Shipment
        	Map<String, Object> serviceResult = dispatcher.runSync("createShipment",
        			UtilMisc.toMap("originFacilityId", originFacilityId, "destinationFacilityId", destinationFacilityId, "shipmentTypeId", "TRANSFER", "statusId", "SHIPMENT_INPUT", "userLogin", userLogin));
        	String shipmentId = (String) serviceResult.get("shipmentId");
        	// create shipmentItem
        	serviceResult = dispatcher.runSync("createShipmentItem",
        			UtilMisc.toMap("productId", productId, "quantity", quantity, "shipmentId", shipmentId, "userLogin", userLogin));
        	String shipmentItemSeqId = (String) serviceResult.get("shipmentItemSeqId");
        	
        	// create ItemIssuance
        	serviceResult = dispatcher.runSync("createItemIssuance",
        			UtilMisc.toMap("quantity", quantity, "inventoryItemId", inventoryItemId, "shipmentId", shipmentId, "shipmentItemSeqId", shipmentItemSeqId, "issuedByUserLoginId", userLogin.getString("userLoginId"), "userLogin", userLogin));
        	String itemIssuanceId = (String) serviceResult.get("itemIssuanceId");
        	result.put("itemIssuanceId", itemIssuanceId);
        	result.put("shipmentId", shipmentId);
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
			e.printStackTrace();
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
		}
        return result;
    }

    /**
     * 根据inventoryTransfer，生成shipment对象
     */
    public static Map<String, Object> completeTransferShipment(DispatchContext dctx, Map<String, ? extends Object> context){
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String shipmentId = (String) context.get("shipmentId"); // 这个参数是必须的
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        try{
        	// 找到shipment对应的itemIssuance
        	List<GenericValue> itemIssuances = delegator.findByAnd("ItemIssuance", UtilMisc.toMap("shipmentId", shipmentId));
        	if (UtilValidate.isEmpty(itemIssuances)) {
                return ServiceUtil.returnError("No ItemIssuance found for shipment " + shipmentId);
        	}
        	GenericValue itemIssuance = EntityUtil.getFirst(itemIssuances);
        	
        	// 然后再找到inventoryItem
        	GenericValue inventoryItem = itemIssuance.getRelatedOne("InventoryItem");
        	String inventoryItemId = inventoryItem.getString("inventoryItemId");
        	
        	// 再找到inventoryTransfer
        	List<GenericValue> inventoryTransfers = delegator.findByAnd("InventoryTransfer", UtilMisc.toMap("inventoryItemId", inventoryItemId));
        	if (UtilValidate.isEmpty(inventoryTransfers)) {
                return ServiceUtil.returnError("No InventoryTransfer found for shipment " + shipmentId);
        	}
        	GenericValue inventoryTransfer = EntityUtil.getFirst(inventoryTransfers);
        	String inventoryTransferId = inventoryTransfer.getString("inventoryTransferId");
        	
        	
        	// complete inventoryTransfer
            Timestamp nowTime = UtilDateTime.nowTimestamp();
        	Map<String, Object> serviceResult = dispatcher.runSync("completeInventoryTransfer",
        			UtilMisc.toMap("inventoryTransferId", inventoryTransferId, "receiveDate", nowTime, "userLogin", userLogin));
        	// complete shipment
        	serviceResult = dispatcher.runSync("updateShipment",
        			UtilMisc.toMap("shipmentId", shipmentId, "statusId", "SHIPMENT_PACKED", "userLogin", userLogin));
        
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
			e.printStackTrace();
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
		}
        return result;
    }
    
}
