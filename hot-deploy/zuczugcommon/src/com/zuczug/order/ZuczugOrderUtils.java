package com.zuczug.order;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.CheckOutHelper;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * @author Sven Wong
 * @version 创建时间：2015年5月29日 下午3:23:56
 * description：订单工具类
 */

public class ZuczugOrderUtils {
	public static final String module = ZuczugOrderUtils.class.getName();

	/**
	 * 获取某个订单的具体的备注信息 by sven
	 * @param delegator
	 * @param orderId
	 * @param noteName note类型
	 * @return
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
	 * 获取订单某个订单项的已收货的数量 by 王亮
	 * @param delegator
	 * @param orderId
	 * @param orderItemSeqId
	 * @param shipGroupSeqId
	 * @return
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
	 * 如果没有就添加
	 * by 王亮
	 */
	public static String checkAndCreateServiceSupplierDate(Delegator delegator,LocalDispatcher dispatcher,GenericValue userLogin,String currencyUomId,String productId,String agentSupplierId){
		try {
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
	
	/**
	 * 创建或者更新一个OrderAttrute by王亮
	 * @param delegator
	 * @param attrName
	 * @param attrValue
	 * @param orderId
	 */
	public static void createOrUpdateOrderAttribute(Delegator delegator,String attrName,String attrValue,String orderId){
		try {
			delegator.removeByAnd("OrderAttribute", UtilMisc.toMap("orderId",orderId,"attrName",attrName));
			GenericValue oa = delegator.makeValue("OrderAttribute");
			oa.set("orderId", orderId);
			oa.set("attrName", attrName);
			oa.set("attrValue", attrValue);
			oa.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置采购单的基本信息
	 * @author sven
	 * @param request
	 * @param response
	 * @return
	 */
	public static String addFabricPurchaseData(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String MODE = request.getParameter("MODE");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		Locale locale = UtilHttp.getLocale(request);
		String supplerPartyId = request.getParameter("partyId");	//供应商
		String agentSupplierId = request.getParameter("agentSupplierId");	//代理供应商
		if(UtilValidate.isEmpty(supplerPartyId))supplerPartyId = agentSupplierId;	//如果面料供应商不填，就和采购商一致
		String facilityId = request.getParameter("facilityId");	//仓库ID
		String orderName = request.getParameter("orderName");	//订单名称
		String itemDescription = request.getParameter("itemDescription");	//订单名称
		String externalId = request.getParameter("externalId");	//外部订单号
		String quantity = request.getParameter("quantity");		//数量
		String unitPriceStr = request.getParameter("unitPrice");//单价
		String productUomId = request.getParameter("productUomId");//产品的单位
		String autoAddSupplier = request.getParameter("AOTU_ADD_SUPPLIER");//如果产品没有供应商关联，自己加上去
		String idValue = request.getParameter("idValue");
		String productId = request.getParameter("productId");	//商品
		String routingGroupId = request.getParameter("routingGroupId");	//商品
		String purchaseType = request.getParameter("purchaseType");	//FAB_ACC_PURCHASE=面辅料采购单
		String estimatedShipDateStr = request.getParameter("estimatedShipDate");
		String orderNote = request.getParameter("orderNote");
		String groupName = request.getParameter("groupName");
		String keyWords = request.getParameter("keyWord");
		Timestamp estimatedShipDate =  null;
		try {
			if (UtilValidate.isNotEmpty(idValue)) {
				productId = (String) EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap("idValue", idValue, "goodIdentificationTypeId" , "ZUCZUG_CODE"))).get("productId");
			}
			ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
			
			if("HEADER_INFO".equals(MODE)){//保存订单头信息
				cart.setOrderType("PURCHASE_ORDER");
				cart.setBillToCustomerPartyId("Company");		//买家，内部组织，谁采购的
				cart.setBillFromVendorPartyId(agentSupplierId);  //账单发出的单位，应该是代理商（采购里的供应商）
				cart.setOrderPartyId(agentSupplierId);
				cart.getAdditionalPartyRoleMap().clear();
				cart.addAdditionalPartyRole(agentSupplierId, "SUPPLIER_AGENT");	//设置代理商
				cart.addAdditionalPartyRole(supplerPartyId, "SUPPLIER");	//设置实际供应商				
				String facilityResult = setPurchaseFacilityAndShippingAddress(facilityId,cart,delegator);
				if(!facilityResult.equals("")){
					request.setAttribute("_ERROR_MESSAGE_",facilityResult);
					return "error";
				}
				
				cart.setUserLogin(userLogin, dispatcher);// 设置当前登录用户 
				cart.setOrderAttribute("FABRIC_PURCHASE_TYPE", purchaseType);
				if("ROUTING_PURCHASE".equals(purchaseType)){
					cart.setOrderAttribute("ROUTING_GOURP_ID", routingGroupId);
				}
				if(UtilValidate.isNotEmpty(keyWords)) {
					cart.setOrderAttribute("KEYWORD", keyWords);
				}
				if(UtilValidate.isNotEmpty(groupName)) {
					cart.setOrderAttribute("GROUP_NAME", groupName);
				}

				cart.setOrderName(orderName);//订单名称
				cart.setExternalId(externalId);//外部订单号
				if(UtilValidate.isNotEmpty(orderNote)){
					request.getSession().setAttribute("orderNote", orderNote);	
				}
			}
			
			if("SAVE".equals(MODE)){
				return saveFabricPurchaseOrder(request,response);
			}
			
			GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
			if (UtilValidate.isEmpty(product)) {
				GenericValue zuczugCode = EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap(
						"idValue", productId,
						"goodIdentificationTypeId", "ZUCZUG_CODE"
						)));
				if (UtilValidate.isNotEmpty(zuczugCode)) {
					productId = (String) zuczugCode.get("productId");
					product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
				}
			}
			
			if("ADD_SERVICE".equals(MODE)){
				agentSupplierId = cart.getBillFromVendorPartyId();
				String checkResult = ZuczugOrderUtils.checkAndCreateServiceSupplierDate(delegator, dispatcher, userLogin, cart.getCurrency(), productId, agentSupplierId);
				if("error".equals(checkResult)){
					request.setAttribute("_ERROR_MESSAGE_","无法为服务商品和供应商创建关联");
					return "error";
				}
	        	MODE="ADD_ITEM";
			}
			
			if("ADD_ITEM".equals(MODE)){
				agentSupplierId = cart.getBillFromVendorPartyId();
				
				GenericValue supplierProduct = null;
				List<GenericValue> spList = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId",productId,"partyId",agentSupplierId));
	        	spList = EntityUtil.filterByDate(spList, UtilDateTime.nowTimestamp(), "availableFromDate", "availableThruDate",false);
	        	GenericValue agentSupplier = delegator.findOne("PartyGroup", true, UtilMisc.toMap("partyId",agentSupplierId));
	        	if(UtilValidate.isEmpty(spList)){
	        		if("Y".equals(autoAddSupplier)){
						//如果标记是Y，那么就自动增加供应商
	        			supplierProduct = createNewProductSupplier(delegator, agentSupplierId, productId);
					}else{
						Debug.log("spList＝＝＝＝is null，" +productId+ "产品没有对应的供应商");
						request.setAttribute("_ERROR_MESSAGE_","供应商 [" + agentSupplier.getString("groupName") +"（"+ agentSupplierId + "）]，没有有效的产品[" + productId + "]");
						return "error";
					}
	        	}else{
	        		supplierProduct = EntityUtil.getFirst(spList);
	        	}
	        	String defaultCurrencyUomId = supplierProduct.getString("currencyUomId"); 	//获取供应商里的货币类型
	        	if(UtilValidate.isEmpty(defaultCurrencyUomId)){
	        		defaultCurrencyUomId = "CNY";
	        	}
	        	cart.setCurrency(dispatcher, defaultCurrencyUomId);	//把货币类型设置购物车
	        	
	        	BigDecimal unitPrice = BigDecimal.ZERO;
	        	if(UtilValidate.isEmpty(unitPriceStr)){
	        		//获取默认价格
		        	Map<String, Object> priceContext = FastMap.newInstance();
					priceContext.put("partyId", agentSupplierId);
					priceContext.put("quantity", new BigDecimal(quantity));
		            priceContext.put("product", product);
		            priceContext.put("currencyUomId", cart.getCurrency());
					Map<String, Object> priceResult = dispatcher.runSync("calculatePurchasePrice", priceContext);
					
					if (ServiceUtil.isError(priceResult)) {
		                throw new CartItemModifyException("There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
		            }
		            Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
		            if (!validPriceFound.booleanValue()) {
		                throw new CartItemModifyException("Could not find a valid price for the product with ID [" + product.getString("productId") + "] and supplier with ID [" + cart.getPartyId() + "], not adding to cart.");
		            }
		            unitPrice = (BigDecimal) priceResult.get("price");
	        	}else{
	        		unitPrice = new BigDecimal(unitPriceStr);
	        	}
	        	
				//验证是否有一个有效价格
	        	Map<String, Object> priceContext = FastMap.newInstance();
				priceContext.put("partyId", agentSupplierId);
				priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",productId)));
				priceContext.put("quantity", new BigDecimal(quantity));
	            priceContext.put("amount", unitPrice);
	            priceContext.put("currencyUomId", cart.getCurrency());
	            Map<String, Object> priceResult = dispatcher.runSync("calculatePurchasePrice", priceContext);
	            Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
	            if (ServiceUtil.isError(priceResult)) {
	                Debug.log(ServiceUtil.getErrorMessage(priceResult));
					request.setAttribute("_ERROR_MESSAGE_",ServiceUtil.getErrorMessage(priceResult));
					return "error";
	            }
	            if (!validPriceFound.booleanValue()) {
	            	supplierProduct.set("minimumOrderQuantity", BigDecimal.ZERO);
	            	supplierProduct.create();
	            }
				
	            if(UtilValidate.isNotEmpty(estimatedShipDateStr)){
					if(estimatedShipDateStr.length() <= 10){
						estimatedShipDateStr+=" 00:00:00";
					}
					
					estimatedShipDate = UtilDateTime.stringToTimeStamp(estimatedShipDateStr,"yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), request.getLocale());
				}
	            if(UtilValidate.isEmpty(itemDescription)){
	            	itemDescription = UtilValidate.isEmpty(product.getString("productName"))?product.getString("internalName"):product.getString("productName");
	            }
	            GenericValue orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("productId",productId,
	            															"itemDescription",itemDescription,
																			"unitPrice",unitPrice,
																			"quantity",new BigDecimal(quantity),
																			"estimatedShipDate",estimatedShipDate));
	            
	            String secUom = request.getParameter("secUom");
	            String secQuantity = request.getParameter("secQuantity");
	            Map<String,String> attributes = null;
	            if(UtilValidate.isNotEmpty(secUom)){
	            	attributes = UtilMisc.toMap("secUom", secUom, "secQuantity", secQuantity);
	            }
	            
	            //setOrderItemAttribute
				// 增加订单项
				addItem(cart,orderItem,dispatcher,delegator,0,attributes);
			}
		} catch (CartItemModifyException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
			return "error";
		} catch (GeneralException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
			return "error";
		} catch (ParseException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
		}
		return "success";
	}
	
	private static String addItem(ShoppingCart cart, GenericValue orderItem, LocalDispatcher dispatcher, Delegator delegator, int groupIdx) throws GeneralException {
		String productId = orderItem.getString("productId");
		GenericValue product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), false);
		if (UtilValidate.isEmpty(product)) {// 第一次找不到商品ID
			if (productId != null && !productId.equals("") && !productId.equals((String) orderItem.getString("productId"))) {
				product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), false);
				if (UtilValidate.isEmpty(product)) {
					String productMissingMsg = "The product having ID (" + (String) orderItem.getString("productId") + ") and The transformed ID (" + productId+ ") is misssing in the system.";
					return productMissingMsg;
				}
			} else {
				String productMissingMsg = "The product having ID (" + (String) orderItem.getString("productId") + ") is misssing in the system.";
				return productMissingMsg;
			}
		}
		BigDecimal qty = orderItem.getBigDecimal("quantity");
		BigDecimal price = orderItem.getBigDecimal("unitPrice");
		price = price.setScale(ShoppingCart.scale, ShoppingCart.rounding);
		

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("shipGroup", groupIdx);
		int idx = cart.addItemToEnd(productId, null, qty, price, null, attrs, null, null, dispatcher, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
		ShoppingCartItem cartItem = cart.findCartItem(idx);
		cartItem.setQuantity(qty, dispatcher, cart, false, false);
		// locate the price verify it matches the expected price
		//BigDecimal cartPrice = cartItem.getBasePrice();
		cartItem.setName(orderItem.getString("itemDescription"));
		//cartPrice = cartPrice.setScale(ShoppingCart.scale, ShoppingCart.rounding);
		//if (price.doubleValue() != cartPrice.doubleValue()) {
			// does not match; honor the price but hold the order for manual
			// review
			cartItem.setIsModifiedPrice(true);
			cartItem.setBasePrice(price);
		//	cart.setHoldOrder(true);
		//	cart.addInternalOrderNote("Price received [" + price + "] (for item # " + productId + ") from Wechat Checkout does not match the price in the database [" + cartPrice
		//			+ "]. Order is held for manual review.");
		//}
		// assign the item to its ship group
		if(UtilValidate.isNotEmpty(orderItem.getTimestamp("estimatedShipDate"))){
			cartItem.setEstimatedShipDate(orderItem.getTimestamp("estimatedShipDate"));
		}
		cart.setItemShipGroupQty(cartItem, qty, groupIdx);
		
		return "success";
	}
	
	private static String addItem(ShoppingCart cart, GenericValue orderItem, LocalDispatcher dispatcher, Delegator delegator, int groupIdx,Map<String,String> itemAttribute) throws GeneralException {
		String productId = orderItem.getString("productId");
		GenericValue product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), false);
		if (UtilValidate.isEmpty(product)) {// 第一次找不到商品ID
			if (productId != null && !productId.equals("") && !productId.equals((String) orderItem.getString("productId"))) {
				product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), false);
				if (UtilValidate.isEmpty(product)) {
					String productMissingMsg = "The product having ID (" + (String) orderItem.getString("productId") + ") and The transformed ID (" + productId+ ") is misssing in the system.";
					return productMissingMsg;
				}
			} else {
				String productMissingMsg = "The product having ID (" + (String) orderItem.getString("productId") + ") is misssing in the system.";
				return productMissingMsg;
			}
		}
		BigDecimal qty = orderItem.getBigDecimal("quantity");
		BigDecimal price = orderItem.getBigDecimal("unitPrice");
		price = price.setScale(4, ShoppingCart.rounding);
		

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("shipGroup", groupIdx);
		int idx = cart.addItemToEnd(productId, null, qty, price, null, attrs, null, null, dispatcher, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
		ShoppingCartItem cartItem = cart.findCartItem(idx);
		cartItem.setQuantity(qty, dispatcher, cart, false, false);
		// locate the price verify it matches the expected price
		//BigDecimal cartPrice = cartItem.getBasePrice();
		cartItem.setName(orderItem.getString("itemDescription"));
		//cartPrice = cartPrice.setScale(ShoppingCart.scale, ShoppingCart.rounding);
		//if (price.doubleValue() != cartPrice.doubleValue()) {
			// does not match; honor the price but hold the order for manual
			// review
			cartItem.setIsModifiedPrice(true);
			cartItem.setBasePrice(price);
		//	cart.setHoldOrder(true);
		//	cart.addInternalOrderNote("Price received [" + price + "] (for item # " + productId + ") from Wechat Checkout does not match the price in the database [" + cartPrice
		//			+ "]. Order is held for manual review.");
		//}
		// assign the item to its ship group
		
		if(UtilValidate.isNotEmpty(itemAttribute)){
			for (String key : itemAttribute.keySet()) {  
				cartItem.setOrderItemAttribute(key, itemAttribute.get(key));
			} 
		}
			
		if(UtilValidate.isNotEmpty(orderItem.getTimestamp("estimatedShipDate"))){
			cartItem.setEstimatedShipDate(orderItem.getTimestamp("estimatedShipDate"));
		}
		cart.setItemShipGroupQty(cartItem, qty, groupIdx);
		
		return "success";
	}
	
	private static GenericValue createNewProductSupplier(Delegator delegator,String partyId,String productId) throws GenericEntityException{
		GenericValue supplierProduct = delegator.makeValue("SupplierProduct");
		supplierProduct.set("partyId", partyId);
		supplierProduct.set("productId", productId);
		supplierProduct.set("availableFromDate", UtilDateTime.nowTimestamp());
		supplierProduct.set("minimumOrderQuantity", BigDecimal.ZERO);
		supplierProduct.set("lastPrice", BigDecimal.ZERO);
		supplierProduct.set("currencyUomId", "CNY");
		supplierProduct.create();
		supplierProduct.refresh();
		return supplierProduct;
	}
	
	/**
	 * 保存面辅料的采购单
	 * @author sven
	 * @param request
	 * @param response
	 * @return
	 */
	public static String saveFabricPurchaseOrder(HttpServletRequest request, HttpServletResponse response){
		ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		Locale locale = request.getLocale();
		Debug.log("Creating CheckOutHelper.");
		CheckOutHelper checkout = new CheckOutHelper(dispatcher, delegator, cart);
		
		orderedRequirement(cart,delegator,dispatcher,userLogin);//把需求单标记成ORDERED
		
		// 创建订单
		Map<String, Object> orderCreate = checkout.createOrder(userLogin);
		if ("error".equals(orderCreate.get("responseMessage"))) {
			//List<String> errorMessageList = (List<String>) orderCreate.get("errorMessageList");
			request.setAttribute("_ERROR_MESSAGE_",ServiceUtil.getErrorMessage(orderCreate));
			return "error";
		}
		String orderId = (String) orderCreate.get("orderId");
		Debug.log("Created order with id: " + orderId);

		// 批准订单
		if (UtilValidate.isNotEmpty(orderId)) {
			try {
				//批准orderHeader 由于批准了就不能改了，刚创建的订单不要批准
				//dispatcher.runSync("changeOrderStatus",UtilMisc.toMap("orderId", orderId, "statusId","ORDER_APPROVED","userLogin", userLogin));
				//批准orderItem
				//dispatcher.runSync("changeOrderItemStatus", UtilMisc.toMap("orderId", orderId, "statusId", "ITEM_APPROVED", "userLogin", userLogin));
				//保存订单备注
				String orderNote = (String) request.getSession().getAttribute("orderNote");
				if(UtilValidate.isNotEmpty(orderNote)){
					dispatcher.runSync("createOrUpdateOrderInfoNote", UtilMisc.toMap("orderId", orderId,"internalNote","Y","noteInfo",orderNote,"noteName","ORDER_REMARKE","userLogin",userLogin));
				}
			} catch (GenericServiceException e) {
				e.printStackTrace();
			}
	        
	        ShoppingCartEvents.destroyCart(request, response);
	        request.getSession().removeAttribute("estimatedShipDateStr");
	        request.getSession().removeAttribute("orderNote");
			request.setAttribute("_EVENT_MESSAGE_",UtilProperties.getMessage("FabricDevelopmentUiLabels","FabricPurchaseOrderCreateSuccess",UtilMisc.toMap("orderId", orderId),locale));
		}
		return "successToList";
	}
	
	private static String setPurchaseFacilityAndShippingAddress(String facilityId,ShoppingCart cart,Delegator delegator){
		/**************** 设置仓库以及仓库的收货地址 start ***********/
		cart.setFacilityId(facilityId);	
		//获取仓库的发货地址
		List<Map<String, Object>> listContactMech = ContactMechWorker.getFacilityContactMechValueMaps(delegator, facilityId, Boolean.FALSE, "POSTAL_ADDRESS");
		if(listContactMech==null || listContactMech.size()<=0){
			Debug.log("仓库的收获地址是空的");
			return "编号为" + facilityId +"的仓库收货地址是空的";
		}
		GenericValue contactMechTemp = (GenericValue)listContactMech.get(0).get("contactMech");
		String contactMechId = contactMechTemp.getString("contactMechId");
		cart.setShippingContactMechId(0, contactMechId);
		cart.setMaySplit(0, Boolean.FALSE);
		cart.setCarrierPartyId(0,"_NA_");
		cart.setShipmentMethodTypeId(0,"NO_SHIPPING");
		cart.makeAllShipGroupInfos();
		/**************** 设置仓库以及仓库的收货地址 begin ***********/
		
		return "";
	}
	
	/**
	 * 如果是从需求来的采购单，把需求标识成完成的
	 */
	private static void orderedRequirement(ShoppingCart cart,Delegator delegator,LocalDispatcher dispatcher,GenericValue userLogin){
		// ----------
        // The status of the requirement associated to the shopping cart lines is set to "ordered".
        //
        for(ShoppingCartItem shoppingCartItem : cart.items()) {
            String requirementId = shoppingCartItem.getRequirementId();
            if (requirementId != null) {
                try {
                    Map<String, Object> inputMap = UtilMisc.<String, Object>toMap("requirementId", requirementId, "statusId", "REQ_ORDERED");
                    inputMap.put("userLogin", userLogin);
                    dispatcher.runSync("updateRequirement", inputMap);
                } catch (Exception e) {
                    String service = e.getMessage();
                    Debug.logError(e, "无法更新对应的需求单[" + requirementId + "]", module);
                }
            }
        }
        // ----------
	}
}
