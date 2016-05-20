package com.zuczug.haze;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class SalesOrderUtils {
	/**
	 * 获取某个订单的具体的备注信息
	 * by liujia
	 */
	public static String getOrderNoteByName(Delegator delegator,String orderId,String noteName){
		String noteInfo = "";
		
		try {
			List<GenericValue> notes = delegator.findByAnd("OrderHeaderAndNote", UtilMisc.toMap("orderId",orderId,"noteName",noteName));
			if(UtilValidate.isNotEmpty(notes)){
				noteInfo = EntityUtil.getFirst(notes).getString("noteInfo");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return noteInfo;
	}
	
	/**
	 * 获取订单某个订单项的已收货的数量
	 * by liujia
	 */
	public static BigDecimal getOrderItemShipReceiptedQuantity(Delegator delegator,String orderId,String orderItemSeqId,String shipGroupSeqId){
		BigDecimal quantity = BigDecimal.ZERO;
		Map<String,Object> cond = FastMap.newInstance();
		cond.put("orderId", orderId);
		cond.put("orderItemSeqId", orderItemSeqId);
		if(UtilValidate.isNotEmpty(shipGroupSeqId)){
			cond.put("shipGroupSeqId", shipGroupSeqId);
		}
		try {
			List<GenericValue> receipteds = delegator.findByAnd("ShipmentAndReceiptView", cond);
			for (GenericValue receipt : receipteds) {
				quantity = quantity.add(receipt.getBigDecimal("quantityAccepted"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return quantity;
	}
	
	/**
	 * 确保服务产品和供应商有关系
	 * by liujia
	 */
	public static String checkAndCreateServiceSupplierDate(Delegator delegator,LocalDispatcher dispatcher,GenericValue userLogin,String currencyUomId,String productId,String agentSupplierId){
		try {
			//如果是添加服务，先检查供应商，如果没有，就加一个，然后把mode设置成ADD_ITEM,让它继续下去
			List<GenericValue> spList = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId",productId,"partyId",agentSupplierId));
			spList = EntityUtil.filterByDate(spList, UtilDateTime.nowTimestamp(), "availableFromDate", "availableThruDate",false);
			GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
			if(UtilValidate.isEmpty(spList)){
				Map<String,Object> newSupplierProduct = FastMap.newInstance();
				newSupplierProduct.put("availableFromDate", UtilDateTime.nowTimestamp());
				newSupplierProduct.put("currencyUomId", UtilValidate.isNotEmpty(currencyUomId)?currencyUomId:"CNY");
				newSupplierProduct.put("lastPrice", BigDecimal.ZERO);
				newSupplierProduct.put("minimumOrderQuantity", BigDecimal.ZERO);
				newSupplierProduct.put("partyId", agentSupplierId);
				newSupplierProduct.put("productId", productId);
				newSupplierProduct.put("supplierProductId", productId);
				newSupplierProduct.put("supplierProductName", product.getString("productName"));
				newSupplierProduct.put("userLogin", userLogin);
				Map<String,Object> cspResult = dispatcher.runSync("createSupplierProduct", newSupplierProduct);
				if(ServiceUtil.isError(cspResult)){
					return "error";
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		return "success";
	}
}
