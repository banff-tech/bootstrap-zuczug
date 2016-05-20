package com.zuczug.haze;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import com.zuczug.product.ZuczugProductUtils;

public class RequestServices {

	public static final String module = RequestServices.class.getName();
	
	public static Map<String, Object> findRequestItems(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		
		String custRequestId = (String) context.get("custRequestId");
		List<Map> theRequestItems = FastList.newInstance();
		int totalQuantity = 0;
		try {
			List<GenericValue> custRequestItems = delegator.findByAnd("CustRequestItem", UtilMisc.toMap("custRequestId", custRequestId));
			for (GenericValue custRequestItem : custRequestItems) {
				int quantity = custRequestItem.getBigDecimal("quantity").intValue();
				totalQuantity += quantity;
			}
			for (GenericValue custRequestItem : custRequestItems) {
				Map<String, Object> theRequestItem = FastMap.newInstance();
				theRequestItem.put("custRequestId", custRequestItem.getString("custRequestId")); 
				theRequestItem.put("custRequestItemSeqId", custRequestItem.getString("custRequestItemSeqId")); 
				theRequestItem.put("statusId", custRequestItem.getString("statusId")); 
				theRequestItem.put("sequenceNum", custRequestItem.getBigDecimal("sequenceNum"));
				String productId = custRequestItem.getString("productId");
				theRequestItem.put("productId", productId);
				BigDecimal quantity = custRequestItem.getBigDecimal("quantity");
				theRequestItem.put("quantity", quantity);
				BigDecimal productPercent = calcProductPercent(delegator, dispatcher, productId, quantity);
				theRequestItem.put("productPercent", productPercent);
				BigDecimal quantityPercent = calcQuantityPercent(quantity, totalQuantity);
				theRequestItem.put("quantityPercent", quantityPercent);
				theRequestItems.add(theRequestItem);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("theRequestItems", theRequestItems);
		
		return resultMap;
	}

	private static BigDecimal calcQuantityPercent(BigDecimal quantity, int totalQuantity) {
		double percent = (double) quantity.intValue()/totalQuantity;
		return new BigDecimal(percent);
	}

	private static BigDecimal calcProductPercent(Delegator delegator,
			LocalDispatcher dispatcher, String productId, BigDecimal quantity) throws GenericEntityException {
		// TODO Auto-generated method stub
		List<GenericValue> custRequestItems = delegator.findByAnd("CustRequestItem", UtilMisc.toMap("productId", productId));
		int totalQuantity = 0;
		for (GenericValue custRequestItem : custRequestItems) {
			int singleQuantity = custRequestItem.getBigDecimal("quantity").intValue();
			totalQuantity += singleQuantity;
		}
		double percent = (double) quantity.intValue()/totalQuantity;
		Debug.logInfo("================================ percent = " + percent, module);
		return new BigDecimal(percent);
	}
	
	// 想办法将requrestItem，用素然横排报表的方式进行展示
	public static Map<String, Object> findProductRequestHeng(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		
		String custRequestId = (String) context.get("custRequestId");
		List<Map> theRequestItems = FastList.newInstance();
		int totalQuantity = 0;
		List<Map> productHeng = null;
		try {
			List<GenericValue> custRequestItems = delegator.findByAnd("CustRequestItem", UtilMisc.toMap("custRequestId", custRequestId));
			productHeng = productItemsToProductHeng(delegator, custRequestItems);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}

		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("theRequestItems", productHeng);
		
		return resultMap;
	}

	private static List<Map> productItemsToProductHeng(Delegator delegator,
			List<GenericValue> productItems) throws GenericEntityException {
		Map<String,Map<String, Object>> productColorMap = FastMap.newInstance();
		for (GenericValue productItem : productItems) {
			String productId = productItem.getString("productId");
			String virtualProductId = ZuczugProductUtils.getVirtualProductIdByVariant(delegator, productId);
			String colorCode = ZuczugProductUtils.getProductColorCode(delegator, productId);
			String sizeCode = ZuczugProductUtils.getProductSizeCode(delegator, productId);
			String productColorId;
			if (UtilValidate.isNotEmpty(virtualProductId)) {
				productColorId = virtualProductId + "-" + colorCode;
			} else {
				productColorId = productId;
			}
			BigDecimal quantity = productItem.getBigDecimal("quantity");
			BigDecimal listPrice = ZuczugProductUtils.getListPrice(delegator, productId);
			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
			
			Map<String, Object> productColor = productColorMap.get(productColorId);
			if (UtilValidate.isEmpty(productColor)) {
				productColor = FastMap.newInstance();
				productColor.put("productColorId", productColorId);
				productColor.put("quantity", BigDecimal.ZERO);
				productColor.put("SIZE0", BigDecimal.ZERO);
				productColor.put("SIZE2", BigDecimal.ZERO);
				productColor.put("SIZE4", BigDecimal.ZERO);
				productColor.put("SIZE6", BigDecimal.ZERO);
				productColor.put("SIZE8", BigDecimal.ZERO);
				if (UtilValidate.isNotEmpty(productItem.getBigDecimal("unitPrice"))) {
					productColor.put("unitPrice", productItem.getBigDecimal("unitPrice"));
				}
				if (UtilValidate.isNotEmpty(listPrice)) {
					productColor.put("listPrice", listPrice);
				}
				productColor.put("listPriceSum", BigDecimal.ZERO);
				productColor.put("priceSum", BigDecimal.ZERO);
				// 组别、系列、子系列等
				Map basicInfo = ZuczugProductUtils.getVariantGroupCategorys(delegator, productId);
				productColor.put("seriesId", basicInfo.get("seriesId"));
				productColor.put("groupId", basicInfo.get("groupId"));
				productColor.put("subSeriesId", basicInfo.get("subSeriesId"));
				if (UtilValidate.isNotEmpty(virtualProductId)) {
					productColor.put("virtualProductId", virtualProductId);
				}
				productColor.put("internalName", product.getString("internalName"));
			}
			BigDecimal productColorQuantity = (BigDecimal) productColor.get("quantity");
			productColorQuantity = productColorQuantity.add(quantity);
			productColor.put("quantity", productColorQuantity);
			String internalSizeCode = ZuczugProductUtils.INTERNAL_SIZE_MAPPING.get(sizeCode);
			BigDecimal productColorSizeQuantity = (BigDecimal) productColor.get(internalSizeCode);
			productColorSizeQuantity = productColorSizeQuantity.add(quantity);
			productColor.put(internalSizeCode, productColorSizeQuantity);

			if (UtilValidate.isNotEmpty(productItem.getBigDecimal("unitPrice"))) {
				BigDecimal priceSum = (BigDecimal) productColor.get("priceSum");
				priceSum = priceSum.add(quantity.multiply(productItem.getBigDecimal("unitPrice")));
				productColor.put("priceSum", priceSum);
			}
			if (UtilValidate.isNotEmpty(listPrice)) {
				BigDecimal listPriceSum = (BigDecimal) productColor.get("listPriceSum");
				listPriceSum = listPriceSum.add(quantity.multiply(listPrice));
				productColor.put("listPriceSum", listPriceSum);
			}

			productColorMap.put(productColorId, productColor);
		}

		return new ArrayList<Map>(productColorMap.values());
	}

}