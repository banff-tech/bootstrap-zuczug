package com.zuczug.haze;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class OrderShipServices {
	/**
	 * 更新送货状态
	 * by liujia
	 */
	public static Map<String, Object> updateShipmentStatus(DispatchContext ctx, Map<String, ? extends Object> context) {
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String shipmentId = (String) context.get("shipmentId");
		String statusId = (String) context.get("statusId");
		
		String orderId = (String) context.get("orderId");
		String orderItemSeqId = (String) context.get("orderItemSeqId");
		BigDecimal quantity = (BigDecimal) context.get("quantity");
		String shipGroupSeqId = "";
		String shipmentItemSeqId = (String) context.get("shipmentItemSeqId");
		String submitType = (String) context.get("submitType");
		try {
			if(submitType.equals("add")){
				//更新送货状态 为已计划
				dispatcher.runSync("updateShipment", UtilMisc.toMap("shipmentId", shipmentId, "statusId", statusId,
						"orderId", orderId, "orderItemSeqId", orderItemSeqId,"quantity", quantity, "shipGroupSeqId", "",
						"shipmentItemSeqId", shipmentItemSeqId,"userLogin",userLogin));
				}
			if(submitType.equals("delete")){
				List<GenericValue> shipmentItemList = delegator.findByAnd("ShipmentItem", UtilMisc.toMap("shipmentId",shipmentId));
				if(shipmentItemList.isEmpty()){
					//更新送货状态 为输入
					dispatcher.runSync("updateShipment", UtilMisc.toMap("shipmentId", shipmentId, "statusId", statusId,
							"orderId", orderId, "orderItemSeqId", orderItemSeqId,"quantity", quantity, "shipGroupSeqId", "",
							"shipmentItemSeqId", shipmentItemSeqId,"userLogin",userLogin));
				}
			}
		}
		catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
				
		return resultMap;
	}
}
