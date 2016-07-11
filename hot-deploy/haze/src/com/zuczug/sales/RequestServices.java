package com.zuczug.sales;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
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
//				BigDecimal productPercent = calcProductPercent(delegator, dispatcher, productId, quantity);
//				theRequestItem.put("productPercent", productPercent);
//				BigDecimal quantityPercent = calcQuantityPercent(quantity, totalQuantity);
//				theRequestItem.put("quantityPercent", quantityPercent);
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
	
	/**
	 * 根据波段导入Quote
	 * by liujia
	 */
	public static String importQuoteFromWave(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String custRequestId = (String) request.getParameter("custRequestId");
		String flag = "success";

        try {
        	//取波段列表
        	List<GenericValue> waveList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryTypeId","WAVE"));
        	for(int i=0;i<waveList.size();i++){
        		String waveId = waveList.get(i).getString("productCategoryId");
        		
    			String waveName = waveList.get(i).getString("description")+" "+UtilDateTime.nowTimestamp().toString();
                //取所要导入的requestItemList
        		List<GenericValue> requestItemList = delegator.findByAnd("FindRequestItemWave", UtilMisc.toMap("custRequestId",custRequestId,"waveId",waveId));
        		requestItemList.addAll(delegator.findByAnd("FindRequestItemAssocWave", UtilMisc.toMap("custRequestId",custRequestId,"waveId",waveId)));
        		if(requestItemList!=null&&requestItemList.size()>0){
                    //生成报价
                	GenericValue custRequest = delegator.findOne("CustRequest", UtilMisc.toMap("custRequestId", custRequestId), false);
        			Map<String, Object> createQuoteForm = dispatcher.runSync("createQuote", UtilMisc.<String, Object>toMap(
        					"partyId",custRequest.get("fromPartyId"),"productStoreId",custRequest.get("productStoreId"),
        					"salesChannelEnumId",custRequest.get("salesChannelEnumId"),"quoteName",waveName,
        					"description",custRequest.get("description"),"currencyUomId",custRequest.get("maximumAmountUomId"),
        					"statusId","QUO_CREATED","quoteTypeId","PRODUCT_QUOTE","userLogin", userLogin));
        			
        			for(int j=0;j<requestItemList.size();j++){
        				//生成报价项
            			BigDecimal unitPrice = BigDecimal.ZERO;
                		//获取默认价格
                    	Map<String, Object> priceContext = FastMap.newInstance();
            			priceContext.put("partyId", custRequest.get("fromPartyId"));
            			priceContext.put("quantity", new BigDecimal(1));
            			priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",requestItemList.get(j).get("productId"))));
                        priceContext.put("currencyUomId", "CNY");
                        priceContext.put("productStoreId", custRequest.get("productStoreId"));
                        
            			Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
            			
            			if (ServiceUtil.isError(priceResult)) {
            				request.setAttribute("_ERROR_MESSAGE_","There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
        					return "error";
                        }
                        Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
                        if (!validPriceFound.booleanValue()) {
        	            	request.setAttribute("_ERROR_MESSAGE_","Could not find a valid price for the product with ID [" + requestItemList.get(j).get("productId") + "] and partyId with ID [" + custRequest.get("fromPartyId") + "]");
        	            	return "error";
                        }
                        unitPrice = (BigDecimal) priceResult.get("basePrice");

            			dispatcher.runSync("createQuoteItem", UtilMisc.<String, Object>toMap("quoteId", createQuoteForm.get("quoteId"),"productId",requestItemList.get(j).get("productId"),
            					"custRequestId",custRequestId,"custRequestItemSeqId",requestItemList.get(j).get("custRequestItemSeqId"),"quantity",requestItemList.get(j).get("quantity"),"quoteUnitPrice",unitPrice,"userLogin", userLogin));
            		
            			dispatcher.runSync("updateCustRequestItem", UtilMisc.<String, Object>toMap("custRequestId", custRequestId,"custRequestItemSeqId",requestItemList.get(j).get("custRequestItemSeqId"),
            					"statusId","CRQ_ACCEPTED","userLogin", userLogin));
        			}
        		}
        	}
        	
        	request.setAttribute("_EVENT_MESSAGE_","波段生成报价已完成");
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
			return "error";
		}
        
		return flag;
	}
	
	/**
	 * 根据上市日期导入Quote
	 * by liujia
	 */
	public static String importQuoteFromReleaseDate(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String custRequestId = (String) request.getParameter("custRequestId");
		String releaseDate = (String) request.getParameter("releaseDate");
		String flag = "success";

        try {
        	//取波段列表
			String waveName = "上市时间"+" "+releaseDate;
            //取所要导入的requestItemList
    		List<GenericValue> requestItemList = delegator.findByAnd("FindRequestItemReleaseDate", UtilMisc.toMap("custRequestId",custRequestId,"releaseDate",Timestamp.valueOf(releaseDate)));
    		if(requestItemList!=null&&requestItemList.size()>0){
                //生成报价
            	GenericValue custRequest = delegator.findOne("CustRequest", UtilMisc.toMap("custRequestId", custRequestId), false);
    			Map<String, Object> createQuoteForm = dispatcher.runSync("createQuote", UtilMisc.<String, Object>toMap(
    					"partyId",custRequest.get("fromPartyId"),"productStoreId",custRequest.get("productStoreId"),
    					"salesChannelEnumId",custRequest.get("salesChannelEnumId"),"quoteName",waveName,
    					"description",custRequest.get("description"),"currencyUomId",custRequest.get("maximumAmountUomId"),
    					"statusId","QUO_CREATED","quoteTypeId","PRODUCT_QUOTE","userLogin", userLogin));
    			
    			for(int j=0;j<requestItemList.size();j++){
    				//生成报价项
        			BigDecimal unitPrice = BigDecimal.ZERO;
            		//获取默认价格
                	Map<String, Object> priceContext = FastMap.newInstance();
        			priceContext.put("partyId", custRequest.get("fromPartyId"));
        			priceContext.put("quantity", new BigDecimal(1));
        			priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",requestItemList.get(j).get("productId"))));
                    priceContext.put("currencyUomId", "CNY");
                    priceContext.put("productStoreId", custRequest.get("productStoreId"));
                    
        			Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
        			
        			if (ServiceUtil.isError(priceResult)) {
    					request.setAttribute("_ERROR_MESSAGE_","There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
    					return "error";
                    }
                    Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
                    if (!validPriceFound.booleanValue()) {
    	            	request.setAttribute("_ERROR_MESSAGE_","Could not find a valid price for the product with ID [" + requestItemList.get(j).get("productId") + "] and partyId with ID [" + custRequest.get("fromPartyId") + "]");
    	            	return "error";
                    }
                    unitPrice = (BigDecimal) priceResult.get("basePrice");

        			dispatcher.runSync("createQuoteItem", UtilMisc.<String, Object>toMap("quoteId", createQuoteForm.get("quoteId"),"productId",requestItemList.get(j).get("productId"),
        					"custRequestId",custRequestId,"custRequestItemSeqId",requestItemList.get(j).get("custRequestItemSeqId"),"quantity",requestItemList.get(j).get("quantity"),"quoteUnitPrice",unitPrice,"userLogin", userLogin));
        		
        			dispatcher.runSync("updateCustRequestItem", UtilMisc.<String, Object>toMap("custRequestId", custRequestId,"custRequestItemSeqId",requestItemList.get(j).get("custRequestItemSeqId"),
        					"statusId","CRQ_ACCEPTED","userLogin", userLogin));
    			}
    		}
        	
        	
        	request.setAttribute("_EVENT_MESSAGE_","上市时间生成报价已完成");
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
			return "error";
		}
		return flag;
	}
	
	/**
	 * 选择requestItem导入至报价
	 * by liujia
	 */
	public static String importQuoteFromRequestItem(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String custRequestId = (String) request.getParameter("custRequestId");
		String flag = "success";
		int rowCount = UtilHttp.getMultiFormRowCount(request);
        if (rowCount < 1 ) {
			request.setAttribute("_ERROR_MESSAGE_","no row to choose");
			return "error";
        }
        try{
	        //取选中的quoteItemId 生成List
	        List<Map<String, Object>> requestItemList = new ArrayList();
	        GenericValue custRequest = delegator.findOne("CustRequest", UtilMisc.toMap("custRequestId", custRequestId), false);
	        for (int i = 0; i < rowCount; i++) {
	        	String curSuffix = UtilHttp.MULTI_ROW_DELIMITER + i;
	            boolean rowSelected = false;
	            if (UtilValidate.isNotEmpty(request.getAttribute(UtilHttp.ROW_SUBMIT_PREFIX + i))) {
	                rowSelected = request.getAttribute(UtilHttp.ROW_SUBMIT_PREFIX + i) == null ? false :
	                "Y".equalsIgnoreCase((String)request.getAttribute(UtilHttp.ROW_SUBMIT_PREFIX + i));
	            } else {
	                rowSelected = request.getParameter(UtilHttp.ROW_SUBMIT_PREFIX + i) == null ? false :
	                "Y".equalsIgnoreCase(request.getParameter(UtilHttp.ROW_SUBMIT_PREFIX + i));
	            }
	            if (!rowSelected) {
	                continue;
	            }
	            
	            BigDecimal unitPrice = BigDecimal.ZERO;
	    		//获取默认价格
	        	Map<String, Object> priceContext = FastMap.newInstance();
				priceContext.put("partyId", custRequest.get("fromPartyId"));
				priceContext.put("quantity", new BigDecimal(1));
				priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",request.getParameter("productId"+curSuffix))));
	            priceContext.put("currencyUomId", "CNY");
	            priceContext.put("productStoreId", custRequest.get("productStoreId"));
	            
				Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
				
				if (ServiceUtil.isError(priceResult)) {
					request.setAttribute("_ERROR_MESSAGE_","There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
					return "error";
	            }
	            Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
	            if (!validPriceFound.booleanValue()) {
	            	request.setAttribute("_ERROR_MESSAGE_","Could not find a valid price for the product with ID [" + request.getParameter("productId"+curSuffix) + "] and partyId with ID [" + request.getParameter("fromPartyId"+curSuffix) + "]");
	            	return "error";
	            }
	            unitPrice = (BigDecimal) priceResult.get("basePrice");
	
	            Map<String, Object> requestItem = new HashMap();
	            requestItem.put("custRequestItemSeqId", request.getParameter("custRequestItemSeqId"+curSuffix));
	            requestItem.put("productId", request.getParameter("productId"+curSuffix));
	            requestItem.put("quantity", request.getParameter("quantity"+curSuffix));
	            requestItem.put("unitPrice", unitPrice);
	            requestItemList.add(requestItem);
	        }
	        String waveName = "需求导入"+" "+UtilDateTime.nowTimestamp().toString();
			if(requestItemList!=null&&requestItemList.size()>0){
				//生成报价
				Map<String, Object> createQuoteForm = dispatcher.runSync("createQuote", UtilMisc.<String, Object>toMap(
						"partyId",custRequest.get("fromPartyId"),"productStoreId",custRequest.get("productStoreId"),
						"salesChannelEnumId",custRequest.get("salesChannelEnumId"),"quoteName",waveName,
						"description",custRequest.get("description"),"currencyUomId",custRequest.get("maximumAmountUomId"),
						"statusId","QUO_CREATED","quoteTypeId","PRODUCT_QUOTE","userLogin", userLogin));

		        for (int j=0;j<requestItemList.size();j++){
	    			dispatcher.runSync("createQuoteItem", UtilMisc.<String, Object>toMap("quoteId", createQuoteForm.get("quoteId"),"productId",requestItemList.get(j).get("productId"),
	    					"custRequestId",custRequestId,"custRequestItemSeqId",requestItemList.get(j).get("custRequestItemSeqId"),"quantity",new BigDecimal((String)requestItemList.get(j).get("quantity")),"quoteUnitPrice",requestItemList.get(j).get("unitPrice"),"userLogin", userLogin));
	    		
	    			dispatcher.runSync("updateCustRequestItem", UtilMisc.<String, Object>toMap("custRequestId", custRequestId,"custRequestItemSeqId",requestItemList.get(j).get("custRequestItemSeqId"),
	    					"statusId","CRQ_ACCEPTED","userLogin", userLogin));
		        }
			}
			request.setAttribute("_EVENT_MESSAGE_","需求项导入报价已完成");
        }catch (GenericServiceException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        	request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
        	return "error";
        } catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e1.getMessage());
			return "error";
		}
        
		return flag;
	}
	
	/**
	 * 根据波段预测分数导入Quote
	 * by liujia
	 */
	public static String importQuoteFromForecastWave(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String waveId = (String) request.getParameter("productCategoryId");
		String errorMessage = "";
		if(waveId==null||waveId.equals("")){
			request.setAttribute("_ERROR_MESSAGE_","请选择波段");
			return "error";
		}
		String flag = "success";
		try{
//		List<GenericValue> productStoreSaleList = delegator.findByAnd("NoCloseProductStore",UtilMisc.toMap("productStoreGroupId", "BUSINESSMODEL001"));
//		errorMessage = importQuoteProductStore(productStoreSaleList,waveId,delegator,dispatcher,userLogin);
//        if(!errorMessage.equals("")){
//        	request.setAttribute("_ERROR_MESSAGE_",errorMessage);
//        }
        List<GenericValue> productStoreDirectList = delegator.findByAnd("NoCloseProductStore",UtilMisc.toMap("productStoreGroupId", "BUSINESSMODEL002"));
        importShipmentProductStore(productStoreDirectList,waveId,delegator,dispatcher,userLogin);
        request.setAttribute("_EVENT_MESSAGE_","数据已计算完成");
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_EVENT_MESSAGE_",e.getMessage());
			return "error";
		}
        
		return flag;
	}

	private static String importShipmentProductStore(List<GenericValue> productStoreList, String waveId,Delegator delegator, LocalDispatcher dispatcher,GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		String value="";
		for(GenericValue productStore : productStoreList){
			String productStoreId = productStore.getString("productStoreId");
			GenericValue productCategory = delegator.findOne("ProductCategory", true, UtilMisc.toMap("productCategoryId",waveId));
			List<GenericValue> productSkuList = delegator.findByAnd("productCategoryMemberAssoc",UtilMisc.toMap("productCategoryId", waveId));
			boolean isCreateShipment = isCreate(productSkuList,dispatcher,productStoreId,userLogin);
			if(!isCreateShipment){
				continue;
			}
			//创建shipment本体
			Map<String, Object> createShipmentForm = dispatcher.runSync("createShipmentAndAttribute", UtilMisc.<String, Object>toMap(
					"shipmentTypeId","TRANSFER","statusId","SHIPMENT_INPUT",
					"originFacilityId","ZUCZUG_CLOTHESFACILITY","destinationFacilityId",productStoreId,
					"estimatedShipDate",UtilDateTime.nowTimestamp(),"currencyUomId","CNY",
					"destinationContactMechId","","handlingInstructions","预测自动生成","shipmentAttribute","","userLogin", userLogin));
			
			for(GenericValue productSku : productSkuList){
				String productId = "";
				if(productSku.get("isVirtual").equals("Y")){
					productId = (String)productSku.get("productIdTo");
				}else{
					productId = (String)productSku.get("productId");
				}
                //取预测数量
				String forecastQuantity = getForecastQuantity(dispatcher,productId,productStoreId,userLogin);
				if(!forecastQuantity.equals("0")){
					Map<String, Object> createShipmentItemForm = dispatcher.runSync("createShipmentItem", UtilMisc.<String, Object>toMap("shipmentId", createShipmentForm.get("shipmentId"),"productId",productId,
							"quantity",forecastQuantity,"userLogin", userLogin));
				}
			}
		}
		return value;
	}
	
	private static String importQuoteProductStore(List<GenericValue> productStoreList,String waveId, Delegator delegator,LocalDispatcher dispatcher, GenericValue userLogin) throws GenericServiceException, GenericEntityException {
		String value="";
		for(GenericValue productStore : productStoreList){
    		String productStoreId = productStore.getString("productStoreId");
			GenericValue productCategory = delegator.findOne("ProductCategory", true, UtilMisc.toMap("productCategoryId",waveId));
			String waveName = productCategory.getString("description")+" "+UtilDateTime.nowTimestamp().toString();
            //取所要导入的productList
			Debug.log("====================================="+productStoreId);
			List<GenericValue> productSkuList = delegator.findByAnd("productCategoryMemberAssoc",UtilMisc.toMap("productCategoryId", waveId));
			boolean isCreateQuote = isCreate(productSkuList,dispatcher,productStoreId,userLogin);
			if(!isCreateQuote){
				continue;
			}
			Debug.log("=====================================aaa"+productStoreId);
			Map<String, Object> createQuoteForm = dispatcher.runSync("createQuote", UtilMisc.<String, Object>toMap(
					"partyId",productStoreId,"productStoreId",productStoreId,
					"salesChannelEnumId","PURCHASING_CHANNEL","quoteName",waveName,
					"description","预测自动生成","currencyUomId","CNY",
					"statusId","QUO_CREATED","quoteTypeId","PRODUCT_QUOTE","userLogin", userLogin));
			
			for(GenericValue productSku : productSkuList){
				String productId = "";
				if(productSku.get("isVirtual").equals("Y")){
					productId = (String)productSku.get("productIdTo");
				}else{
					productId = (String)productSku.get("productId");
				}
				
    			BigDecimal unitPrice = BigDecimal.ZERO;
        		//获取默认价格
            	Map<String, Object> priceContext = FastMap.newInstance();
    			priceContext.put("partyId", productStoreId);
    			priceContext.put("quantity", new BigDecimal(1));
    			priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",productId)));
                priceContext.put("currencyUomId", "CNY");
                priceContext.put("productStoreId", "B2B");
                
    			Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
    			
    			if (ServiceUtil.isError(priceResult)) {
    				value = "There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult);
					return value;
                }
                Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
                if (!validPriceFound.booleanValue()) {
                	value = "Could not find a valid price for the product with ID [" + productId + "] and partyId with ID [" + productStoreId + "]";
	            	return value;
                }
                unitPrice = (BigDecimal) priceResult.get("basePrice");
                //取预测数量
                String forecastQuantity = getForecastQuantity(dispatcher,productId,productStoreId,userLogin);
				if(!forecastQuantity.equals("0")){
					dispatcher.runSync("createQuoteItem", UtilMisc.<String, Object>toMap("quoteId", createQuoteForm.get("quoteId"),"productId",productId,
        					"quantity",new BigDecimal(forecastQuantity),"quoteUnitPrice",unitPrice,"userLogin", userLogin));
				}
			}
		}
		return value;
	}

	private static boolean isCreate(List<GenericValue> productSkuList,LocalDispatcher dispatcher,String productStoreId,GenericValue userLogin) throws GenericServiceException {
		boolean flag = false;
		for(GenericValue productSku : productSkuList){
			String productId = "";
			if(productSku.get("isVirtual").equals("Y")){
				productId = (String)productSku.get("productIdTo");
			}else{
				productId = (String)productSku.get("productId");
			}
			
			Map serviceResult = dispatcher.runSync("analysis.forecastQuantityByInventory", UtilMisc.toMap("productId", productId,"isManufactCount","false", "userLogin", userLogin));
			List<Map<String,Object>> storeProductQuantityList = (List) serviceResult.get("storeProductQuantityList");
			if(storeProductQuantityList!=null&&!storeProductQuantityList.isEmpty()){
				for(Map<String,Object> storeProductQuantity : storeProductQuantityList){
					if(storeProductQuantity.get("productStoreId").equals(productStoreId)){
						if(!String.valueOf(storeProductQuantity.get("quantity")).equals("0")){
							return true;
						}
					}
				}
			}
		}
		return flag;
	}
	
	private static String getForecastQuantity(LocalDispatcher dispatcher,String productId,String productStoreId,GenericValue userLogin) throws GenericServiceException {
		String forecastQuantity = "0";
		Map serviceResult = dispatcher.runSync("analysis.forecastQuantityByInventory", UtilMisc.toMap("productId", productId,"isManufactCount","false", "userLogin", userLogin));
		List<Map<String,Object>> storeProductQuantityList = (List) serviceResult.get("storeProductQuantityList");
		if(storeProductQuantityList!=null&&!storeProductQuantityList.isEmpty()){
			for(Map<String,Object> storeProductQuantity : storeProductQuantityList){
				if(storeProductQuantity.get("productStoreId").equals(productStoreId)){
					forecastQuantity = String.valueOf(storeProductQuantity.get("quantity"));
					break;
				}
			}
		}
		return forecastQuantity;
	}
}