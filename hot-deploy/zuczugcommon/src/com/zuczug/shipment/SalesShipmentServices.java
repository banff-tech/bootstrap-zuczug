package com.zuczug.shipment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class SalesShipmentServices {
	
	/**
	 * 更新送货状态为已计划并检查库存
	 * by liujia
	 */
	public static String updateShipmentStatusToPlane(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String shipmentId = request.getParameter("shipmentId");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		
		String destinationContactMechId = "";
		try {
			GenericValue shipment = EntityUtil.getFirst(delegator.findByAnd("Shipment", UtilMisc.toMap("shipmentId",shipmentId)));
			destinationContactMechId=shipment.getString("destinationContactMechId");
			if(destinationContactMechId==null||destinationContactMechId.equals("")){
				List<GenericValue> shipmentAddressList = delegator.findByAnd("FindFacilityContactMech", UtilMisc.toMap("facilityId",shipment.get("destinationFacilityId"),"contactMechPurposeTypeId","SHIPPING_LOCATION"));
				if (shipmentAddressList.isEmpty()){
					request.setAttribute("_ERROR_MESSAGE_","目的地场所收货地址不存在");
					return "error";
				}else{
					if(shipmentAddressList.size()==1){
						destinationContactMechId = shipmentAddressList.get(0).getString("contactMechId");
					}else{
						request.setAttribute("_ERROR_MESSAGE_","目的地场所收货地址有多个,请选择一个收货地址");
						return "error";
					}
				}
			}
			
			Map<String, Object> resultMap = dispatcher.runSync("PlaneShipmentForIssuance", UtilMisc.<String, Object>toMap("shipmentId", shipmentId, "userLogin", userLogin));
			if(resultMap.get("responseMessage").equals("error")){
				request.setAttribute("_ERROR_MESSAGE_",resultMap.get("errorMessage"));
				return "error";
			}
			
			Map<String, Object> updateShipmentMap = dispatcher.runSync("updateShipment", UtilMisc.<String, Object>toMap("shipmentId", shipmentId,"destinationContactMechId",destinationContactMechId, "statusId", "SHIPMENT_SCHEDULED", "userLogin", userLogin));
			if (ServiceUtil.isError(updateShipmentMap)) {
				request.setAttribute("_ERROR_MESSAGE_","更新失败,请联系系统管理员");
				return "error";
        	}
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","更新异常,请联系系统管理员");
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		request.setAttribute("_EVENT_MESSAGE_","成功转为计划运送状态");
		return "success";
	}
	
	
	/**
	 * shipment占用库存数量准备发货
	 * @param shipmentId
	 * @return Map
	 */
	public static Map<String, Object> planeShipmentForIssuance(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		String shipmentId = (String) context.get("shipmentId");
		//检查库存
		String errorMessage = checkInventoryQuantity(delegator,shipmentId);
		if(!errorMessage.equals("")){
			return ServiceUtil.returnError(errorMessage);
		}
		//createIssuance入口
		createShipmentForIssuance(delegator,dispatcher,shipmentId,userLogin);
		
		return resultMap;
	}
	
	
	//检查库存是否符合送货条件
	private static String checkInventoryQuantity(Delegator delegator,String shipmentId) {
		String errorMessage= "";
		try {
			GenericValue shipment = EntityUtil.getFirst(delegator.findByAnd("Shipment", UtilMisc.toMap("shipmentId",shipmentId)));
			String shipmentFacilityId = shipment.getString("originFacilityId");
			//取group的值 重复的productid数量已经sum
			List<GenericValue> shipmentItemGroupProductList = delegator.findByAnd("ShipmentItemGroupbyProductId", UtilMisc.toMap("shipmentId",shipmentId));
			if(!shipmentItemGroupProductList.isEmpty()){
				for(GenericValue shipmentItemGroupProduct:shipmentItemGroupProductList){
					String productId = shipmentItemGroupProduct.getString("productId");
					BigDecimal shipmentItemProudctTotalQuantity = shipmentItemGroupProduct.getBigDecimal("quantity");
					List<GenericValue> inventoryItemGroupbyProductIdList = delegator.findByAnd("InventoryItemGroupbyProductId", UtilMisc.toMap("productId",productId,"facilityId",shipmentFacilityId));
					if(!inventoryItemGroupbyProductIdList.isEmpty()){
						BigDecimal inventoryItemProudctTotalQuantity = inventoryItemGroupbyProductIdList.get(0).getBigDecimal("availableToPromiseTotal");
						if (inventoryItemProudctTotalQuantity.compareTo(shipmentItemProudctTotalQuantity)==-1){
							errorMessage += "  " + productId + " 库存量不足,现有库存为" + inventoryItemProudctTotalQuantity;
						}
					}else{
						errorMessage += "  " + productId + " 没有库存 ";
					}
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			return errorMessage;
		}
		return errorMessage;
	}
	
	//根据shipment创建issuance
	private static String createShipmentForIssuance(Delegator delegator,LocalDispatcher dispatcher,String shipmentId,GenericValue userLogin) {
		String errorMessage= "";
        
		try {
			List<GenericValue> shipmentItemList = delegator.findByAnd("ShipmentItem", UtilMisc.toMap("shipmentId",shipmentId));
			if(!shipmentItemList.isEmpty()){
				for(GenericValue shipmentItem:shipmentItemList){
					//创建issuace
					errorMessage += createIssuance(delegator,dispatcher,shipmentItem,userLogin,errorMessage,null);
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			errorMessage += e.getMessage();
			return errorMessage;
		}
		return errorMessage;
	}
	
	//创建issuance主体
	private static String createIssuance(Delegator delegator,LocalDispatcher dispatcher,GenericValue shipmentItem,GenericValue userLogin,String errorMessage,BigDecimal tempShipmentItemQuantity) {
		try{
			String productId = shipmentItem.getString("productId");
			BigDecimal shipmentItemQuantity = shipmentItem.getBigDecimal("quantity");
			String shipmentItemSeqId = shipmentItem.getString("shipmentItemSeqId");
			String shipmentId = shipmentItem.getString("shipmentId");
			
			
			GenericValue shipment = EntityUtil.getFirst(delegator.findByAnd("Shipment", UtilMisc.toMap("shipmentId",shipmentId)));
			String shipmentFacilityId = shipment.getString("originFacilityId");
			//取出inventoryItem表中数量可用的库存
			List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
				exprs.add(EntityCondition.makeCondition("availableToPromiseTotal", EntityOperator.GREATER_THAN, BigDecimal.ZERO));
				exprs.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, shipmentFacilityId));
				exprs.add(EntityCondition.makeCondition("datetimeReceived", EntityOperator.NOT_EQUAL, null));
				exprs.add(EntityCondition.makeCondition("inventoryItemTypeId", EntityOperator.EQUALS, "NON_SERIAL_INV_ITEM"));				
			GenericValue inventoryItem = EntityUtil.getFirst(delegator.findList("InventoryItem", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, UtilMisc.toList("datetimeReceived ASC"), null, false));
			BigDecimal inventoryItemQuantity = inventoryItem.getBigDecimal("availableToPromiseTotal");
			String inventoryItemId = inventoryItem.getString("inventoryItemId");
			//递归传递过来的剩余送货值
			if(tempShipmentItemQuantity!=null && !tempShipmentItemQuantity.equals("")){
				shipmentItemQuantity = tempShipmentItemQuantity;
			}
			//送货量大于库存量
			if (shipmentItemQuantity.compareTo(inventoryItemQuantity)==1){
				//tempShipmentItemQuantity 剩余送货值
				tempShipmentItemQuantity = shipmentItemQuantity.subtract(inventoryItemQuantity);
				//创建issuance
//				Map<String, Object> itemIssuanceCreate = dispatcher.runSync("createItemIssuance", UtilMisc.<String, Object>toMap("quantity", inventoryItemQuantity,
//						"inventoryItemId",inventoryItemId,"shipmentId",shipmentId,"shipmentItemSeqId",shipmentItemSeqId,"issuedByUserLoginId",userLogin.getString("userLoginId"), "userLogin", userLogin));
//				String itemIssuanceId = (String) itemIssuanceCreate.get("itemIssuanceId");
				
				//创建inventoryItemDetail
				Map<String, Object> inventoryItemDetailCreate = dispatcher.runSync("createInventoryItemDetail", UtilMisc.<String, Object>toMap("inventoryItemId", inventoryItemId,
						"shipmentId",shipmentId,"shipmentItemSeqId",shipmentItemSeqId,
						"description","直营店送货占用库存","availableToPromiseDiff",inventoryItemQuantity.negate(), "userLogin", userLogin));
				
			}else{
				//创建issuance
				tempShipmentItemQuantity=BigDecimal.ZERO;
//				Map<String, Object> itemIssuanceCreate = dispatcher.runSync("createItemIssuance", UtilMisc.<String, Object>toMap("quantity", shipmentItemQuantity,
//						"inventoryItemId",inventoryItemId,"shipmentId",shipmentId,"shipmentItemSeqId",shipmentItemSeqId,"issuedByUserLoginId",userLogin.getString("userLoginId"), "userLogin", userLogin));
//				String itemIssuanceId = (String) itemIssuanceCreate.get("itemIssuanceId");
				
				//创建inventoryItemDetail
				Map<String, Object> inventoryItemDetailCreate = dispatcher.runSync("createInventoryItemDetail", UtilMisc.<String, Object>toMap("inventoryItemId", inventoryItemId,
						"shipmentId",shipmentId,"shipmentItemSeqId",shipmentItemSeqId,
						"description","直营店送货占用库存","availableToPromiseDiff",shipmentItemQuantity.negate(), "userLogin", userLogin));
			}
		}catch (GenericEntityException e) {
			e.printStackTrace();
			errorMessage += e.getMessage();
			return errorMessage;
		}catch (GenericServiceException e){
			e.printStackTrace();
			errorMessage += e.getMessage();
			return errorMessage;
		}
		//如果数量为0说明该shipmentItem已经全部分配完 跳出
		if(tempShipmentItemQuantity.compareTo(BigDecimal.ZERO)!=0){
			createIssuance(delegator,dispatcher,shipmentItem,userLogin,errorMessage,tempShipmentItemQuantity);
		}
		return errorMessage;
	}
	
	
	public static String queryProductRealView(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String productId = request.getParameter("productId");
		String shipmentId = request.getParameter("shipmentId");
		String quoteId = request.getParameter("quoteId");
		if(productId==null||productId.equals("")){
			request.setAttribute("_ERROR_MESSAGE_","查询款号不能为空,请选择商品款号");
			return "error";
		}
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String facilityId = "";
		try {
			if(shipmentId!=null&&!shipmentId.equals("")){
				GenericValue shipment = EntityUtil.getFirst(delegator.findByAnd("Shipment", UtilMisc.toMap("shipmentId",shipmentId)));
				facilityId = shipment.getString("originFacilityId");
			}else if(quoteId!=null&&!quoteId.equals("")){
				GenericValue quote = EntityUtil.getFirst(delegator.findByAnd("Quote", UtilMisc.toMap("quoteId",quoteId)));
				String partyId = quote.getString("partyId");
				GenericValue facility = EntityUtil.getFirst(delegator.findByAnd("Facility", UtilMisc.toMap("facilityId",partyId)));
				if(facility!=null&&!facility.isEmpty()){
					facilityId = facility.getString("facilityId");
				}
			}
			if(facilityId.equals("")){
				request.setAttribute("_ERROR_MESSAGE_","对应的场所不存在!");
				return "error";
			}

			List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%"+productId+"%"));
				exprs.add(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "N"));
				exprs.add(EntityCondition.makeCondition("isVariant", EntityOperator.EQUALS, "Y"));

			List<GenericValue> productRealViewList = delegator.findList("ProductRealViewInventoryManufact", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
			request.setAttribute("productRealViewList", productRealViewList);
			request.setAttribute("productGroup", productId);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
		return "success";
	}
	
	@SuppressWarnings("unchecked")
	public static String createShipmentItem(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String shipmentId = request.getParameter("shipmentId");
		String productGroup = request.getParameter("productGroup");
		String quoteId = request.getParameter("quoteId");
		String facilityId = "";
		try {
			if(shipmentId!=null&&!shipmentId.equals("")){
				GenericValue shipment = EntityUtil.getFirst(delegator.findByAnd("Shipment", UtilMisc.toMap("shipmentId",shipmentId)));
				facilityId = shipment.getString("originFacilityId");
			}else if(quoteId!=null&&!quoteId.equals("")){
				GenericValue quote = EntityUtil.getFirst(delegator.findByAnd("Quote", UtilMisc.toMap("quoteId",quoteId)));
				String partyId = quote.getString("partyId");
				GenericValue facility = EntityUtil.getFirst(delegator.findByAnd("Facility", UtilMisc.toMap("facilityId",partyId)));
				facilityId = facility.getString("facilityId");
			}
			if(facilityId.equals("")){
				request.setAttribute("_ERROR_MESSAGE_","场所不存在!");
				return "error";
			}
			List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%"+productGroup+"%"));
				exprs.add(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "N"));
				exprs.add(EntityCondition.makeCondition("isVariant", EntityOperator.EQUALS, "Y"));
	
			List<GenericValue> productRealViewList = delegator.findList("ProductRealViewInventoryManufact", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
			if(productRealViewList!=null&&productRealViewList.size()>0){
				for(int i=0;i<productRealViewList.size();i++){
					String sku = (String) productRealViewList.get(i).get("productId");
					String quantity = request.getParameter("quantity_"+sku);
					if(quantity==null||quantity.equals("0")||quantity.trim().equals("")){
						
					}
					else{
						Map<String, Object> createShipmentItemForm = dispatcher.runSync("createShipmentItem", UtilMisc.<String, Object>toMap("shipmentId", shipmentId,"productId",sku,
								"quantity",quantity,"userLogin", userLogin));
					}					
				}
			}
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return "success";
	}

	public static String UpdateShipmentAddress(HttpServletRequest request, HttpServletResponse response){
		String shipmentAddressId = request.getParameter("shipmentAddressId");
		
		return "success";
	}
	
}
