package com.zuczug.haze;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.event.EventHandlerException;


public class QuoteServices {

	public static final String module = QuoteServices.class.getName();
	
	/**
	 * 查找报价项
	 * by liujia
	 */
	public static Map<String, Object> findQuoteItems(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		
		String quoteId = (String) context.get("quoteId");
		List<Map> theQuoteItems = FastList.newInstance();
		int totalQuantity = 0;
		try {
			List<GenericValue> quoteItems = delegator.findByAnd("QuoteItem", UtilMisc.toMap("quoteId", quoteId));
			GenericValue quote = delegator.findByPrimaryKey("Quote", UtilMisc.toMap("quoteId", quoteId));
			for (GenericValue quoteItem : quoteItems) {
				Map<String, Object> theQuoteItem = FastMap.newInstance();
				theQuoteItem.put("quoteId", quoteItem.getString("quoteId")); 
				theQuoteItem.put("quoteItemSeqId", quoteItem.getString("quoteItemSeqId")); 
				theQuoteItem.put("custRequestId", quoteItem.getString("custRequestId")); 
				theQuoteItem.put("custRequestItemSeqId", quoteItem.getString("custRequestItemSeqId")); 
				theQuoteItem.put("quoteUnitPrice", quoteItem.getBigDecimal("quoteUnitPrice"));
				String productId = quoteItem.getString("productId");
				theQuoteItem.put("productId", productId);
				BigDecimal quantity = quoteItem.getBigDecimal("quantity");
				theQuoteItem.put("quantity", quantity);
				BigDecimal productListPrice = getProductListPrice(ctx, productId, quote.getString("partyId"), quote.getString("productStoreId"), quote.getString("currencyUomId"));
				theQuoteItem.put("productListPrice", productListPrice);
//				BigDecimal productPercent = calcProductPercent(delegator, dispatcher, productId, quantity);
//				theQuoteItem.put("productPercent", productPercent);
//				BigDecimal quantityPercent = calcQuantityPercent(quantity, totalQuantity);
//				theQuoteItem.put("quantityPercent", quantityPercent);
//				BigDecimal custRequestProductNumber = getcustRequestProductNumber(delegator, productId);
//				theQuoteItem.put("custRequestProductNumber", custRequestProductNumber);
				BigDecimal totalRequirementQuantityApprovedNumber = getTotalRequirementQuantityApprovedNumber(delegator, productId);
				theQuoteItem.put("totalRequirementQuantityApprovedNumber", totalRequirementQuantityApprovedNumber);
				BigDecimal totalRequirementQuantity = getTotalRequirementQuantity(delegator, productId);
				theQuoteItem.put("totalRequirementQuantity", totalRequirementQuantity);
				theQuoteItems.add(theQuoteItem);

			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("theQuoteItems", theQuoteItems);
		
		return resultMap;
	}

	private static BigDecimal getTotalRequirementQuantity(Delegator delegator,
			String productId) throws GenericEntityException {
		int totalQuantity = 0;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
            try {
            	String groupHelperName = delegator.getGroupHelperName("org.ofbiz");  
				connection = ConnectionFactory.getConnection(groupHelperName);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String sql = null;
            stmt = connection.createStatement();
	
            sql = "select ROUND(sum(r.QUANTITY),0) from requirement r "+
            		"where r.PRODUCT_ID = '" + productId + "' and r.STATUS_ID <> 'REQ_REJECTED' and r.FACILITY_ID = 'ZUCZUG_CLOTHESFACILITY' group by r.PRODUCT_ID";

            rs = stmt.executeQuery(sql);
            
            if(rs.next()) {
            	totalQuantity = Integer.valueOf(rs.getString(1));
            }
            rs.close();
		}catch (SQLException e) {
			Debug.logWarning(e, "Error closing statement in sequence util", module);
        }finally {
                try {
                    if (stmt != null) stmt.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "Error closing statement in sequence util", module);
                }
                try {
                    if (connection != null) connection.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "Error closing connection in sequence util", module);
                }
        }
//        List<EntityExpr> exprs = FastList.newInstance();
//        exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
//        exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "REQ_REJECTED"));
//        exprs.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, "ZUCZUG_CLOTHESFACILITY"));
//		List<GenericValue> requirements = delegator.findList("Requirement", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
//		for (GenericValue requirement : requirements) {
//			int singleQuantity = requirement.getBigDecimal("quantity").intValue();
//			totalQuantity += singleQuantity;
//		}
		return new BigDecimal(totalQuantity);
	}

	private static BigDecimal calcQuantityPercent(BigDecimal quantity, int totalQuantity) {
		double percent = (double) quantity.intValue()/totalQuantity;
		return new BigDecimal(percent);
	}

	private static BigDecimal calcProductPercent(Delegator delegator,
			LocalDispatcher dispatcher, String productId, BigDecimal quantity) throws GenericEntityException {
        List<EntityExpr> exprs = FastList.newInstance();
		List<GenericValue> quoteItems = delegator.findByAnd("QuoteItem", UtilMisc.toMap("productId", productId));
		int totalQuantity = 0;
		for (GenericValue quoteItem : quoteItems) {
			GenericValue quote = delegator.findByPrimaryKey("Quote", UtilMisc.toMap("quoteId", quoteItem.getString("quoteId")));
			if (!quote.getString("statusId").equals("QUO_REJECTED")) {
				int singleQuantity = quoteItem.getBigDecimal("quantity").intValue();
				totalQuantity += singleQuantity;
			}
		}
		double percent = (double) quantity.intValue()/totalQuantity;
		return new BigDecimal(percent);
	}
	
	/**
	 * 计算报价数量
	 * by liujia
	 */
	public static String calcQuoteQuantity(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String quoteId = request.getParameter("quoteId");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		
		int quoteItemquantity = 0;
		int custRequestItemquantity = 0;
		int currentQuoteItemquantity = 0;
		String productId = "";
		String quoteItemSeqId = "";
		MathContext mc = new MathContext(5, RoundingMode.DOWN);
		try {
			List<GenericValue> quoteItems = delegator.findByAnd("QuoteItem", UtilMisc.toMap("quoteId", quoteId));
			for (GenericValue quoteItem : quoteItems) {
				//quoteItemquantity = quoteItem.getBigDecimal("quantity").intValue();
				String custRequestId = quoteItem.getString("custRequestId");
				String custRequestItemSeqId = quoteItem.getString("custRequestItemSeqId");
				GenericValue custRequestItem = delegator.findByPrimaryKey("CustRequestItem", UtilMisc.toMap("custRequestId", custRequestId,"custRequestItemSeqId",custRequestItemSeqId));
				if (custRequestItem!=null&&!custRequestItem.isEmpty()) {
					custRequestItemquantity = custRequestItem.getBigDecimal("quantity").intValue();
					
					productId = quoteItem.getString("productId");
					quoteItemSeqId = quoteItem.getString("quoteItemSeqId");
					BigDecimal totalCustRequestProductNumber = getcustRequestProductNumber(delegator, productId);
					BigDecimal TotalRequirementQuantityApprovedNumber = getTotalRequirementQuantityApprovedNumber(delegator, productId);
					
					if(!TotalRequirementQuantityApprovedNumber.equals(BigDecimal.ZERO)&&TotalRequirementQuantityApprovedNumber.compareTo(totalCustRequestProductNumber)==-1){
						//由于审批数没有订货总数多 则按比例计算数量并更新记录
						currentQuoteItemquantity = new BigDecimal(custRequestItemquantity).divide(totalCustRequestProductNumber,mc).multiply(TotalRequirementQuantityApprovedNumber).intValue();
						dispatcher.runSync("updateQuoteItem", UtilMisc.toMap("userLogin", userLogin,
								"quoteId",quoteId,"quoteItemSeqId", quoteItemSeqId, "quantity",new BigDecimal(currentQuoteItemquantity)));
					}
				}
			}
			
			
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","计算失败,请于管理员联系");
			return "error";
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","记录更新失败,请与管理员联系");
			return "error";
		}
		
		return "success";
	}
	
	/**
	 * 取商品部总需求数量 为已审批的
	 * by liujia
	 */
	private static BigDecimal getTotalRequirementQuantityApprovedNumber(Delegator delegator,
			String productId) throws GenericEntityException {
		int totalQuantity = 0;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
            try {
            	String groupHelperName = delegator.getGroupHelperName("org.ofbiz");  
				connection = ConnectionFactory.getConnection(groupHelperName);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String sql = null;
            stmt = connection.createStatement();
	
            sql = "select ROUND(sum(r.QUANTITY),0) from requirement r "+
            		"where r.PRODUCT_ID = '" + productId + "' and r.STATUS_ID = 'REQ_APPROVED' and r.FACILITY_ID = 'ZUCZUG_CLOTHESFACILITY' group by r.PRODUCT_ID";

            rs = stmt.executeQuery(sql);
            
            if(rs.next()) {
            	totalQuantity = Integer.valueOf(rs.getString(1));
            }
            rs.close();
		}catch (SQLException e) {
			Debug.logWarning(e, "Error closing statement in sequence util", module);
        }finally {
                try {
                    if (stmt != null) stmt.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "Error closing statement in sequence util", module);
                }
                try {
                    if (connection != null) connection.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "Error closing connection in sequence util", module);
                }
        }
		
//        List<EntityExpr> exprs = FastList.newInstance();
//        exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
//        exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "REQ_APPROVED"));
//        exprs.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, "ZUCZUG_CLOTHESFACILITY"));
//		List<GenericValue> requirements = delegator.findList("Requirement", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
//		for (GenericValue requirement : requirements) {
//			int singleQuantity = requirement.getBigDecimal("quantity").intValue();
//			totalQuantity += singleQuantity;
//		}
		return new BigDecimal(totalQuantity);
	}
	
	/**
	 * 取原始custRequest中所有商品总数
	 * by liujia
	 */
	private static BigDecimal getcustRequestProductNumber(Delegator delegator,
			String productId) throws GenericEntityException {
		int totalQuantity = 0;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
            try {
            	String groupHelperName = delegator.getGroupHelperName("org.ofbiz");  
				connection = ConnectionFactory.getConnection(groupHelperName);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String sql = null;
            stmt = connection.createStatement();
	
            sql = "select ROUND(sum(cri.QUANTITY),0) from cust_request_item cri "+
            		"left join cust_request cr on cri.CUST_REQUEST_ID = cr.CUST_REQUEST_ID and cr.STATUS_ID <> 'CRQ_REJECTED' "+
            		"where cri.PRODUCT_ID = '" + productId + "' group by cri.PRODUCT_ID ";

            rs = stmt.executeQuery(sql);
            
            if(rs.next()) {
            	totalQuantity = Integer.valueOf(rs.getString(1));
            }
            rs.close();
		}catch (SQLException e) {
			Debug.logWarning(e, "Error closing statement in sequence util", module);
        }finally {
                try {
                    if (stmt != null) stmt.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "Error closing statement in sequence util", module);
                }
                try {
                    if (connection != null) connection.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "Error closing connection in sequence util", module);
                }
        }
//      List<EntityExpr> exprs = FastList.newInstance();
//		List<GenericValue> custRequestItemList = delegator.findByAnd("CustRequestItem", UtilMisc.toMap("productId", productId));
//		for (GenericValue custRequestItem : custRequestItemList) {
//			GenericValue quote = delegator.findByPrimaryKey("CustRequest", UtilMisc.toMap("custRequestId", custRequestItem.getString("custRequestId")));
//			if (!quote.getString("statusId").equals("CRQ_REJECTED")) {
//				int singleQuantity = custRequestItem.getBigDecimal("quantity").intValue();
//				totalQuantity += singleQuantity;
//			}
//		}
		return new BigDecimal(totalQuantity);
	}
	
	/**
	 * 查找未生成订单的报价项
	 * by liujia
	 */
	public static Map<String, Object> findQuoteItemsNoSales(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		
		String quoteId = (String) context.get("quoteId");
		List<Map> theQuoteItems = FastList.newInstance();
        List<GenericValue> quoteItems=null;
        try {
			quoteItems = delegator.findByAnd("FindQuoteItemNoSalesOrder", UtilMisc.toMap("quoteId", quoteId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if(quoteItems!=null&&quoteItems.size()>0){
			for (GenericValue quoteItem : quoteItems) {
				Map<String, Object> theQuoteItem = FastMap.newInstance();
				theQuoteItem.put("quoteId", quoteItem.getString("quoteId")); 
				theQuoteItem.put("quoteItemSeqId", quoteItem.getString("quoteItemSeqId")); 
				theQuoteItem.put("quoteUnitPrice", quoteItem.getBigDecimal("quoteUnitPrice"));
				String productId = quoteItem.getString("productId");
				theQuoteItem.put("productId", productId);
				BigDecimal quantity = quoteItem.getBigDecimal("quantity");
				theQuoteItem.put("quantity", quantity);
				theQuoteItem.put("orderId", quoteItem.getString("orderId"));
				theQuoteItems.add(theQuoteItem);
			}
		}
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("theQuoteItemsNoSales", theQuoteItems);
		
		return resultMap;
	}
	
	/**
	 * 查找有报价关联的销售订单
	 * by liujia
	 */
	public static Map<String, Object> findQuoteSalesOrders(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		
		String quoteId = (String) context.get("quoteId");
		List<Map> theSalesOrders = FastList.newInstance();
        List<GenericValue> salesOrders=null;
        try {
        	salesOrders = delegator.findByAnd("FindQuoteSalesOrder", UtilMisc.toMap("attrValue", quoteId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if(salesOrders!=null&&salesOrders.size()>0){
			for (GenericValue salesOrder : salesOrders) {
				Map<String, Object> theSalesOrder = FastMap.newInstance();
				theSalesOrder.put("orderId", salesOrder.getString("orderId")); 
				theSalesOrder.put("partyId", salesOrder.getString("partyId")); 
				theSalesOrder.put("quoteId", salesOrder.getString("attrValue"));
				theSalesOrder.put("orderDate", salesOrder.getString("orderDate"));
				theSalesOrders.add(theSalesOrder);
			}
		}
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		resultMap.put("theQuoteSalesOrders", theSalesOrders);
		
		return resultMap;
	}

	/**
	 * 报价导入至订单
	 * by liujia
	 */
	public static String quoteImportSalesOrder(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String shipContactMechId = (String) request.getParameter("shipContactMechId");
		String shipId = (String) request.getParameter("shipId");
		String paymentType = (String) request.getParameter("paymentType");
		String flag = "success";
		String quoteId = (String) request.getParameter("quoteId");
		int rowCount = UtilHttp.getMultiFormRowCount(request);
        if (rowCount < 1) {
			request.setAttribute("_ERROR_MESSAGE_","no row to choose");
			return "error";
        }
        //取选中的quoteItemId 生成List
        List<Map<String, Object>> quoteItemList = new ArrayList();
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
            Map<String, Object> quoteItem = new HashMap();
            quoteItem.put("quoteItemSeqId", request.getParameter("quoteItemSeqId"+curSuffix));
            quoteItem.put("productId", request.getParameter("productId"+curSuffix));
            if(request.getParameter("quantity"+curSuffix).equals("0")){
            	request.setAttribute("_ERROR_MESSAGE_",request.getParameter("productId"+curSuffix)+"该商品需求数量为0不能导入订单");
    			return "error";
            }
            quoteItem.put("quantity", request.getParameter("quantity"+curSuffix));
            quoteItem.put("quoteUnitPrice", request.getParameter("quoteUnitPrice"+curSuffix));
            quoteItemList.add(quoteItem);
        }
        if(quoteItemList==null||quoteItemList.size()==0){
			request.setAttribute("_ERROR_MESSAGE_","记录没有选择,请选择要加入的报价项");
			return "error";
        }
        //生成订单
        SalesOrderServices salesOrderServices = new SalesOrderServices();
        flag = salesOrderServices.importSalesOrder(request, response, quoteItemList, quoteId,shipContactMechId,shipId,paymentType);
        
		return flag;
	}
	
	/**
	 * 根据产品SKU查找报价
	 * by liujia
	 */
	public static Map<String, Object> findSkuQuotes(DispatchContext ctx, Map<String, ? extends Object> context) {
		//Object delegator is used for communicating with the database.
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		Map<String, Object> resultMap = ServiceUtil.returnSuccess();
		String productId = (String) context.get("productId");
		List<Map> theSkuQuotes = FastList.newInstance();
        List<GenericValue> skuQuotes=null;
        Map<String, Object> skuFields = FastMap.newInstance();
        if(productId!=null&&!productId.equals("")){
        	skuFields.put("productId", productId);
        }
        try {
        	skuQuotes = delegator.findByAnd("FindSkuQuotes", skuFields);
			if(skuQuotes!=null&&skuQuotes.size()>0){
				for (GenericValue skuQuote : skuQuotes) {
					Map<String, Object> theSkuQuote = FastMap.newInstance();
					theSkuQuote.put("quoteItemSeqId", skuQuote.getString("quoteItemSeqId")); 
					theSkuQuote.put("quoteId", skuQuote.getString("quoteId")); 
					theSkuQuote.put("productId", skuQuote.getString("productId"));
					BigDecimal quantity = skuQuote.getBigDecimal("quantity");
					theSkuQuote.put("quantity", quantity);
					theSkuQuote.put("storeName", skuQuote.getString("storeName")); 
					theSkuQuote.put("partyId", skuQuote.getString("partyId")); 
					BigDecimal totalRequirementQuantityApprovedNumber = getTotalRequirementQuantityApprovedNumber(delegator, skuQuote.getString("productId"));
					theSkuQuote.put("totalRequirementQuantityApprovedNumber", totalRequirementQuantityApprovedNumber);
					BigDecimal totalRequirementQuantity = getTotalRequirementQuantity(delegator, skuQuote.getString("productId"));
					theSkuQuote.put("totalRequirementQuantity", totalRequirementQuantity);
					theSkuQuotes.add(theSkuQuote);
				}
			}
			resultMap.put("theSkuQuotes", theSkuQuotes);
		} catch (GenericEntityException e) {
			e.printStackTrace();
			resultMap = ServiceUtil.returnError(e.getMessage());
			return resultMap;
		}
		return resultMap;
	}
	
	/**
	 * 更新报价项数量
	 * by liujia
	 */
	public static String updateQuoteQuantity(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String quoteId = (String) request.getParameter("quoteId");
		String quoteItemSeqId = (String) request.getParameter("quoteItemSeqId");
		BigDecimal quantity = new BigDecimal((String)request.getParameter("quantity"));

        try {
			dispatcher.runSync("updateQuoteItem", UtilMisc.toMap("userLogin", userLogin,
					"quoteId",quoteId,"quoteItemSeqId", quoteItemSeqId, "quantity",quantity));
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","修改失败,请联系管理员");
			return "error";
		}

        return "success";
	}
	
	/**
	 * 计算某个SKU的所有报价数量
	 * by liujia
	 */
	public static String calcSkuQuoteQuantity(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String productId = request.getParameter("productId");
		
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		
		int quoteItemquantity = 0;
		int custRequestItemquantity = 0;
		int currentQuoteItemquantity = 0;
		String quoteItemSeqId = "";
		String quoteId = "";
		MathContext mc = new MathContext(5, RoundingMode.DOWN);
		try {
			List<GenericValue> quoteItems = delegator.findByAnd("QuoteItem", UtilMisc.toMap("productId", productId));
			for (GenericValue quoteItem : quoteItems) {
				//quoteItemquantity = quoteItem.getBigDecimal("quantity").intValue();
				productId = quoteItem.getString("productId");
				quoteItemSeqId = quoteItem.getString("quoteItemSeqId");
				quoteId = quoteItem.getString("quoteId");
				String custRequestId = quoteItem.getString("custRequestId");
				String custRequestItemSeqId = quoteItem.getString("custRequestItemSeqId");
				GenericValue custRequestItem = delegator.findByPrimaryKey("CustRequestItem", UtilMisc.toMap("custRequestId", custRequestId,"custRequestItemSeqId",custRequestItemSeqId));
				if (!custRequestItem.isEmpty()) {
					custRequestItemquantity = custRequestItem.getBigDecimal("quantity").intValue();
				}
				BigDecimal totalCustRequestProductNumber = getcustRequestProductNumber(delegator, productId);
				BigDecimal TotalRequirementQuantityApprovedNumber = getTotalRequirementQuantityApprovedNumber(delegator, productId);
				
				if(!TotalRequirementQuantityApprovedNumber.equals(BigDecimal.ZERO)&&TotalRequirementQuantityApprovedNumber.compareTo(totalCustRequestProductNumber)==-1){
					//由于审批数没有订货总数多 则按比例计算数量并更新记录
					currentQuoteItemquantity = new BigDecimal(custRequestItemquantity).divide(totalCustRequestProductNumber,mc).multiply(TotalRequirementQuantityApprovedNumber).intValue();
					dispatcher.runSync("updateQuoteItem", UtilMisc.toMap("userLogin", userLogin,
							"quoteId",quoteId,"quoteItemSeqId", quoteItemSeqId, "quantity",new BigDecimal(currentQuoteItemquantity)));
				}
			}
			
			
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","计算失败,请与管理员联系");
			return "error";
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","记录更新失败,请与管理员联系");
			return "error";
		}
		
		return "success";
	}
	
	/**
	 * 创建报价项
	 * by liujia
	 */
	@SuppressWarnings("unchecked")
	public static String createQuoteItem(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String quoteId = request.getParameter("quoteId");
		String productGroup = request.getParameter("productGroup");
		try {
			List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%"+productGroup+"%"));
				exprs.add(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "N"));
				exprs.add(EntityCondition.makeCondition("isVariant", EntityOperator.EQUALS, "Y"));
			List<GenericValue> productRealViewList = delegator.findList("ProductRealView", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
			if(productRealViewList!=null&&productRealViewList.size()>0){
				for(int i=0;i<productRealViewList.size();i++){
					String sku = (String) productRealViewList.get(i).get("productId");
					String quantity = request.getParameter("quantity_"+sku);
					String price = request.getParameter("price_"+sku);
					if(quantity==null||quantity.equals("0")||quantity.equals("")){
						
					}
					else{
						BigDecimal unitPrice = BigDecimal.ZERO;
						GenericValue quote = EntityUtil.getFirst(delegator.findByAnd("Quote", UtilMisc.toMap("quoteId",quoteId)));
			        	if(UtilValidate.isEmpty(price)){
			        		//获取默认价格
				        	Map<String, Object> priceContext = FastMap.newInstance();
							priceContext.put("partyId", quote.get("partyId"));
							priceContext.put("quantity", new BigDecimal(1));
							priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",sku)));
				            priceContext.put("currencyUomId", "CNY");
				            priceContext.put("productStoreId", quote.get("productStoreId"));
				            
							Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
							
							if (ServiceUtil.isError(priceResult)) {
				                try {
									throw new CartItemModifyException("There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
								} catch (CartItemModifyException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            }
				            Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
				            if (!validPriceFound.booleanValue()) {
				                try {
									throw new CartItemModifyException("Could not find a valid price for the product with ID [" + sku + "] and supplier with ID [" + quote.get("partyId") + "], not adding to cart.");
								} catch (CartItemModifyException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            }
				            unitPrice = (BigDecimal) priceResult.get("basePrice");
			        	}else{
			        		unitPrice = new BigDecimal(price);
			        	}
						Map<String, Object> createQuoteItemForm = dispatcher.runSync("createQuoteItem", UtilMisc.<String, Object>toMap("quoteId", quoteId,"productId",sku,
								"quantity",new BigDecimal(quantity),"quoteUnitPrice",unitPrice,"userLogin", userLogin));
					}					
				}
			}
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","记录新增失败,请与管理员联系");
			return "error";
		}catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","记录新增失败,请与管理员联系");
			return "error";
		}
        
		return "success";
	}
	
	/**
	 * 更新报价审批状态
	 * by liujia
	 */
	public static String updateQuoteApprovedStatus(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String quoteId = (String) request.getParameter("quoteId");

        try {
			dispatcher.runSync("updateQuote", UtilMisc.toMap("userLogin", userLogin,
					"quoteId",quoteId,"statusId", "QUO_APPROVED"));
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","修改失败,请联系管理员");
			return "error";
		}

        return "success";
	}
	
	/**
	 * 取产品的标价
	 * by liujia
	 */
	public static BigDecimal getProductListPrice(DispatchContext ctx,String productId,String custId,String productStoreId,String cUom) throws Exception{
		BigDecimal unitPrice = BigDecimal.ZERO;
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		try{
			Map<String, Object> priceContext = FastMap.newInstance();
			priceContext.put("partyId", custId);
			priceContext.put("quantity", new BigDecimal(1));//只计算单价
			priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",productId)));
		    priceContext.put("currencyUomId", cUom);
		    priceContext.put("productStoreId", productStoreId);
		    
			Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
			
			if (ServiceUtil.isError(priceResult)) {
		        throw new Exception("getProductListPrice was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
		    }
		    unitPrice = (BigDecimal) priceResult.get("listPrice");
		}catch(Exception e){
			throw new Exception("getProductListPrice was an error");
		}
	    return unitPrice;
	}
	
	
}
