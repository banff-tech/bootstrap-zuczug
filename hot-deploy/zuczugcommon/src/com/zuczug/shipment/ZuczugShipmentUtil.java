package com.zuczug.shipment;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.MiniLangUtil;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.service.LocalDispatcher;

import com.zuczug.product.ProductBomUtils;

/**
 * @author Sven Wong
 * @version 创建时间：2015年11月16日 上午11:40:44
 * @description：
 */

public class ZuczugShipmentUtil {
	public static final String module = ProductBomUtils.class.getName();
	
	public static BigDecimal getShipmentItemPackageQuantity(Delegator delegator,String shipmentId,String shipmentItemSeqId){
		BigDecimal result = BigDecimal.ZERO;
		try {
			List<GenericValue> allConcent = delegator.findByAnd("ShipmentPackageContent", UtilMisc.toMap("shipmentId",shipmentId,"shipmentItemSeqId",shipmentItemSeqId));
			for (GenericValue content : allConcent) {
				result = result.add(UtilValidate.isEmpty(content.getBigDecimal("quantity")) ? BigDecimal.ZERO :content.getBigDecimal("quantity"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 获取订单内有效的shipmentId，作为成衣仓库订单发货的
	 * @return
	 */
	public static String getOrderPrimaryShipmentId(Delegator delegator,String orderId,HttpServletRequest request,HttpServletResponse response){
		String shipmentId="";
		try {
			request.setAttribute("orderId", orderId);
			request.removeAttribute("shipmentId");
			String result = SimpleMethod.runSimpleEvent("component://fabricfacility/script/FabricFacilityServices.xml", "createOrderShipmentAndPackage", request, response);
			shipmentId=(String) request.getAttribute("shipmentId");
			Debug.log("shipmentId"+shipmentId);
		} catch (MiniLangException e) {
			e.printStackTrace();
		}
		return shipmentId;
	}
	
	
	public static void createOrUpdateShipmentAttribute(Delegator delegator,String attrName,String attrValue,String shipmentId){
		try {
			delegator.removeByAnd("ShipmentAttribute", UtilMisc.toMap("shipmentId",shipmentId,"attrName",attrName));
			GenericValue shipmentAttribute = delegator.makeValue("ShipmentAttribute");
			shipmentAttribute.set("shipmentId", shipmentId);
			shipmentAttribute.set("attrName", attrName);
			shipmentAttribute.set("attrValue", attrValue);
			shipmentAttribute.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	public static void createOrUpdateInventoryItemAttribute(Delegator delegator,String attrName,String attrValue,String inventoryItemId){
		try {
			delegator.removeByAnd("InventoryItemAttribute", UtilMisc.toMap("inventoryItemId",inventoryItemId,"attrName",attrName));
			GenericValue shipmentAttribute = delegator.makeValue("InventoryItemAttribute");
			shipmentAttribute.set("inventoryItemId", inventoryItemId);
			shipmentAttribute.set("attrName", attrName);
			shipmentAttribute.set("attrValue", attrValue);
			shipmentAttribute.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 检查货运是否预定到了库存
	 * @return
	 */
	public static boolean checkShipmentIsIssueanInventory(Delegator delegator,String shipmentId){
		boolean isAssign=true;
		try {
			List<GenericValue> list = delegator.findByAnd("ShipmentItem", UtilMisc.toMap("shipmentId",shipmentId));
			for (GenericValue item : list) {
				List<GenericValue> iidList = delegator.findByAnd("InventoryItemDetail", UtilMisc.toMap("shipmentId",shipmentId,"shipmentItemSeqId",item.getString("shipmentItemSeqId")));
				if(UtilValidate.isEmpty(iidList)){
					isAssign=false;
					break;
				}
				BigDecimal totalmatch = BigDecimal.ZERO;
				for (GenericValue iid : iidList) {
					totalmatch = totalmatch.add(iid.getBigDecimal("availableToPromiseDiff").negate());
				}
				if(totalmatch.compareTo(item.getBigDecimal("quantity"))<0){
					isAssign=false;
					break;
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return isAssign;
	}
}
