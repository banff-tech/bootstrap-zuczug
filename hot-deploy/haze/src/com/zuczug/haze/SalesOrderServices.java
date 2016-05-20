package com.zuczug.haze;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.fortuna.ical4j.model.property.ProdId;

import org.ofbiz.accounting.payment.BillingAccountWorker;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.CheckOutHelper;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.ShoppingCartHelper;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.order.shoppingcart.WebShoppingCart;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.apache.tools.ant.taskdefs.optional.j2ee.HotDeploymentTool;


public class SalesOrderServices{
	public static final String module = SalesOrderServices.class.getName();
	private static String resource = "";

	/**
	 * 取原始custRequest中所有商品总数
	 * by liujia
	 */
	public static String addSalesOrderData(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String MODE = request.getParameter("MODE");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		Locale locale = UtilHttp.getLocale(request);
		String buyerId = request.getParameter("buyerId");	//买家ID
		String productStoreId = (String) request.getParameter("productStoreId");
		String orderName = request.getParameter("orderName");	//订单名称
		String externalId = request.getParameter("externalId");	//外部订单号
		String quantity = request.getParameter("quantity");		//数量
		String unitPriceStr = request.getParameter("unitPrice");//单价
		String productUomId = request.getParameter("productUomId");//产品的单位
		String productId = request.getParameter("productId");	//商品
		String purchaseType = request.getParameter("salesOrderType");	
		String itemDesiredDeliveryDateStr = request.getParameter("itemDesiredDeliveryDate");
		String orderNote = request.getParameter("orderNote");
		String groupName = request.getParameter("groupName");
		
		String shipAfterDateStr = request.getParameter("shipAfterDate"); //计划发货日期
		
		String orderTypeId = request.getParameter("orderTypeId");
		String shipId = request.getParameter("shipId");
		Timestamp itemDesiredDeliveryDate =  null;
		String billingAccountId = null;
		String defaultCurrencyUomId = "";
		String payToPartyId = "";
		String facilityId = "";
		try {
			

			ShoppingCart cart = ShoppingCartEvents.getCartObject(request);

			if("HEADER_INFO".equals(MODE)){//保存订单头信息
				cart.setOrderType("SALES_ORDER");
				cart.setBillToCustomerPartyId(buyerId);		//买家
				cart.setPlacingCustomerPartyId(buyerId);
				cart.setShipToCustomerPartyId(buyerId);
				cart.setEndUserCustomerPartyId(buyerId);
				
	            if (UtilValidate.isNotEmpty(shipAfterDateStr)) {
	                if (shipAfterDateStr.length() == 10) shipAfterDateStr += " 00:00:00.000";
	                cart.setDefaultShipAfterDate(java.sql.Timestamp.valueOf(shipAfterDateStr));
	            }
	            
	            // 店铺id需要验证，并根据此id获取配置参数
				if (productStoreId == null) {
					request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("productStoreId不存在", "productStoreId不存在", locale));
					return "error";
				} else {
					GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
					if (productStore != null) {
						defaultCurrencyUomId = productStore.getString("defaultCurrencyUomId");
						payToPartyId = productStore.getString("payToPartyId");
						facilityId = productStore.getString("inventoryFacilityId");
						//facilityId = (String) context.get("facilityId");
					} else {
						request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("productStoreId有异常 ,请联系系统管理员", "productStoreId有异常 ,请联系系统管理员", locale));
						return "error";
					}
				}
				if (UtilValidate.isNotEmpty(payToPartyId)) {
					cart.setBillFromVendorPartyId(payToPartyId); //账单来自于
					cart.addAdditionalPartyRole(payToPartyId, "BILL_FROM_VENDOR");
				}
				
				cart.addAdditionalPartyRole(buyerId, "BILL_TO_CUSTOMER");//账单给予
				cart.setFacilityId(facilityId);//设置仓库
				cart.setUserLogin(userLogin, dispatcher);// 设置当前登录用户 
				cart.setOrderAttribute("ORDER_TYPE", orderTypeId);				
				// cart.setChannelType("ALAND_SALES_CHANNEL");// 设置销售平台
				cart.setOrderName(orderName);//订单名称
				cart.setExternalId(externalId);//外部订单号
				cart.setProductStoreId(productStoreId);
				if(UtilValidate.isNotEmpty(orderNote)){
					request.getSession().setAttribute("orderNote", orderNote);	
				}
				//设置订单时间
				cart.setOrderDate(UtilDateTime.nowTimestamp());
				
				//获取买家的收货地址
				String buyerContactMechPurposeTypeId = "";
				String contactMechId = "";
				List<Map<String, Object>> buyerContactMechList = ContactMechWorker.getPartyContactMechValueMaps(delegator, buyerId, false);
				if(buyerContactMechList==null){
					request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("编号为" + buyerId +"地址是空的", "出现异常 ,请联系系统管理员", locale));
					return "error";
				}else{
					for(int i=0;i<buyerContactMechList.size();i++){
						List<GenericValue> buyerContactMechPurposesList = (List<GenericValue>) buyerContactMechList.get(i).get("partyContactMechPurposes");
						if(buyerContactMechPurposesList!=null&&buyerContactMechPurposesList.size()>0){
							for(int j=0;j<buyerContactMechPurposesList.size();j++){
								GenericValue buyerContactMechPurposes = buyerContactMechPurposesList.get(j);
								String tempContactMechPurposeTypeId = (String) buyerContactMechPurposes.get("contactMechPurposeTypeId");
								if(tempContactMechPurposeTypeId.equals("SHIPPING_LOCATION")){
									buyerContactMechPurposeTypeId = tempContactMechPurposeTypeId;
								}
							}
						}
						if(!buyerContactMechPurposeTypeId.equals("")){
							GenericValue buyerContactMech = (GenericValue) buyerContactMechList.get(i).get("contactMech");
							contactMechId = (String) buyerContactMech.get("contactMechId");
						}
					}
				}
				
				//设置地址类型为收货地址
				if(contactMechId.equals("")){
					request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("编号为" + buyerId +"收货地址是空的", "出现异常 ,请联系系统管理员", locale));
					return "error";
				}
				cart.setShippingContactMechId(0, contactMechId);
				cart.setMaySplit(0, Boolean.FALSE);
				cart.setShipGroupFacilityId(0, cart.getFacilityId());
			}
			
			if("HEADER_SHIP".equals(MODE)){
				GenericValue shipInfo = EntityUtil.getFirst(delegator.findByAnd("ProductStoreShipmentMethView", UtilMisc.toMap("productStoreShipMethId",shipId)));
				cart.setOrderAttribute("ORDER_SHIP", (String)shipInfo.get("productStoreShipMethId"));	
				cart.setAllCarrierPartyId((String)shipInfo.get("partyId"));
				cart.setAllShipmentMethodTypeId((String)shipInfo.get("shipmentMethodTypeId"));
				cart.makeAllShipGroupInfos();
			}
			
			if("SAVE".equals(MODE)){
				return saveSalesOrder(request,response);
			}
			
			if("ADD_ITEM".equals(MODE)){
				buyerId = cart.getBillToCustomerPartyId();
				GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
				if(defaultCurrencyUomId.isEmpty()){
					defaultCurrencyUomId = "CNY";
				}
	        	cart.setCurrency(dispatcher, defaultCurrencyUomId);	//把货币类型设置购物车
	        	
	        	BigDecimal unitPrice = BigDecimal.ZERO;
	        	if(UtilValidate.isEmpty(unitPriceStr)){
	        		//获取默认价格
		        	Map<String, Object> priceContext = FastMap.newInstance();
					priceContext.put("partyId", buyerId);
					priceContext.put("quantity", new BigDecimal(quantity));
					priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",productId)));
		            priceContext.put("currencyUomId", cart.getCurrency());
		            priceContext.put("productStoreId", cart.getProductStoreId());
		            
					Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
					
					if (ServiceUtil.isError(priceResult)) {
		                throw new CartItemModifyException("There was an error while calculating the price: " + ServiceUtil.getErrorMessage(priceResult));
		            }
		            Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
		            if (!validPriceFound.booleanValue()) {
		                throw new CartItemModifyException("Could not find a valid price for the product with ID [" + product.getString("productId") + "] and supplier with ID [" + cart.getPartyId() + "], not adding to cart.");
		            }
		            unitPrice = (BigDecimal) priceResult.get("basePrice");
	        	}else{
	        		unitPrice = new BigDecimal(unitPriceStr);
	        	}
	        	
				//验证是否有一个有效价格
	        	Map<String, Object> priceContext = FastMap.newInstance();
				priceContext.put("partyId", buyerId);
				priceContext.put("product", delegator.findOne("Product", true, UtilMisc.toMap("productId",productId)));
				priceContext.put("quantity", new BigDecimal(quantity));
	            priceContext.put("amount", unitPrice);
	            priceContext.put("currencyUomId", cart.getCurrency());
	            priceContext.put("productStoreId", cart.getProductStoreId());
	            Map<String, Object> priceResult = dispatcher.runSync("calculateProductPrice", priceContext);
	            Boolean validPriceFound = (Boolean) priceResult.get("validPriceFound");
	            if (ServiceUtil.isError(priceResult)) {
	                Debug.log(ServiceUtil.getErrorMessage(priceResult));
					request.setAttribute("_ERROR_MESSAGE_",ServiceUtil.getErrorMessage(priceResult));
					return "error";
	            }
//	            if (!validPriceFound.booleanValue()) {
//	            	supplierProduct.set("minimumOrderQuantity", BigDecimal.ZERO);
//	            	supplierProduct.create();
//	            }
				
	            if(UtilValidate.isNotEmpty(itemDesiredDeliveryDateStr)){
					if(itemDesiredDeliveryDateStr.length() <= 10){
						itemDesiredDeliveryDateStr+=" 00:00:00";
					}
					
					itemDesiredDeliveryDate = UtilDateTime.stringToTimeStamp(itemDesiredDeliveryDateStr,"yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), request.getLocale());
				}
	            GenericValue orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("productId",productId,
	            															"itemDescription",UtilValidate.isEmpty(product.getString("productName"))?product.getString("internalName"):product.getString("productName"),
																			"unitPrice",unitPrice,
																			"quantity",new BigDecimal(quantity),
																			"estimatedDeliveryDate",itemDesiredDeliveryDate));
	            
//	            String secUom = request.getParameter("secUom");
//	            String secQuantity = request.getParameter("secQuantity");
	            Map<String,String> attributes = null;
//	            if(UtilValidate.isNotEmpty(secUom)){
//	            	//attributes = UtilMisc.toMap("secUom", secUom, "secQuantity", secQuantity);
//	            }
	            
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
	
	/**
	 * 新增订单项
	 * by liujia
	 */
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
		price = price.setScale(ShoppingCart.scale, ShoppingCart.rounding);
		

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("shipGroup", groupIdx);
		int idx = cart.addItemToEnd(productId, null, qty, price, null, attrs, null, null, dispatcher, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
		ShoppingCartItem cartItem = cart.findCartItem(idx);
		cartItem.setQuantity(qty, dispatcher, cart, false, false);
		cartItem.setQuoteId(orderItem.getString("quoteId"));
		cartItem.setQuoteItemSeqId(orderItem.getString("quoteItemSeqId"));
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
		
		if(UtilValidate.isNotEmpty(orderItem.getTimestamp("estimatedDeliveryDate"))){
			cartItem.setDesiredDeliveryDate(orderItem.getTimestamp("estimatedDeliveryDate"));
		}
		cart.setItemShipGroupQty(cartItem, qty, groupIdx);
		
		return "success";
	}
	
	private static String setSaleFacilityAndShippingAddress(String facilityId,ShoppingCart cart,Delegator delegator){
		//设置仓库以及仓库的收货地址
		cart.setFacilityId(facilityId);	
		//获取仓库的发货地址
		List<Map<String, Object>> listContactMech = ContactMechWorker.getFacilityContactMechValueMaps(delegator, facilityId, Boolean.FALSE, "POSTAL_ADDRESS");
		if(listContactMech==null || listContactMech.size()<=0){
			Debug.log("仓库的收获地址是空的");
			return "编号为" + facilityId +"的收货地址是空的";
		}
		GenericValue contactMechTemp = (GenericValue)listContactMech.get(0).get("contactMech");
		String contactMechId = contactMechTemp.getString("contactMechId");
		cart.setShippingContactMechId(0, contactMechId);
		cart.setMaySplit(0, Boolean.FALSE);
		cart.setAllCarrierPartyId("_NA_");
		cart.setAllShipmentMethodTypeId("NO_SHIPPING");
		cart.makeAllShipGroupInfos();

		
		return "";
	}
	

	/**
	 * 重新计算订单项
	 * by liujia
	 */
	public static String recalculatedSalesOrder(HttpServletRequest request, HttpServletResponse response){
		String[] quantitys = request.getParameterValues("quantity");
		String[] itemIndexs = request.getParameterValues("itemIndex");
		String[] unitPrices = request.getParameterValues("unitPrice");
		String[] estimatedDeliveryDates = request.getParameterValues("estimatedDeliveryDate");
		ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		try {
			int allSize = cart.items().size();
			for (int i = 0; i < allSize; i++) {
				ShoppingCartItem cartItem = cart.findCartItem(Integer.parseInt(itemIndexs[i]));
				if(UtilValidate.isNotEmpty(cartItem)){
					cartItem.setIsModifiedPrice(true);
					cartItem.setBasePrice(new BigDecimal(unitPrices[i]));
					cartItem.setQuantity(new BigDecimal(quantitys[i]), dispatcher, cart);
					if(estimatedDeliveryDates!=null){
						if(estimatedDeliveryDates.length > i){
							String estimatedDeliveryDateStr = estimatedDeliveryDates[i];
							if(estimatedDeliveryDateStr.length() <= 10){
								estimatedDeliveryDateStr+=" 00:00:00";
							}
							Timestamp estimatedDeliveryDate = UtilDateTime.stringToTimeStamp(estimatedDeliveryDateStr,"yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), request.getLocale());
							cartItem.setDesiredDeliveryDate(estimatedDeliveryDate);
						}
					}
				}
			}
		} catch (CartItemModifyException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
		}
		return "success";
	}
	
	/**
	 * 保存订单
	 * by liujia
	 */
	public static String saveSalesOrder(HttpServletRequest request, HttpServletResponse response){
		ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String productStoreId = (String) request.getParameter("productStoreId");
		Locale locale = UtilHttp.getLocale(request);
		String buyerId = request.getParameter("buyerId");	//买家ID
		String billingAccountId = null;
		String defaultCurrencyUomId = "";
		GenericValue productStore = null;
		// 店铺id需要验证，并根据此id获取配置参数
		if (productStoreId == null) {
			request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("productStoreId不存在", "productStoreId不存在", locale));
			return "error";
		} else {
			try {
				productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (productStore != null) {
				defaultCurrencyUomId = productStore.getString("defaultCurrencyUomId");
			} else {
				request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("productStoreId有异常 ,请联系系统管理员", "productStoreId有异常 ,请联系系统管理员", locale));
				return "error";
			}
		}

		//检查买家信用额度
		List<Map<String, Object>> billingAccountList = new ArrayList();
		try {
			billingAccountList = BillingAccountWorker.makePartyBillingAccountList(userLogin, defaultCurrencyUomId, buyerId,delegator, dispatcher);
		} catch (GeneralException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(billingAccountList==null || billingAccountList.size()<=0){
			request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("客户无信用额度", "客户无信用额度", locale));
			return "error";
		}				
		//获取可用的余额
		Map<String, Object> billingAccount = billingAccountList.get(0);
		billingAccountId = (String) billingAccount.get("billingAccountId");
		//当前订单金额
		BigDecimal totalamount = (BigDecimal)cart.getItemTotal();
		
		BigDecimal availableAmount = (BigDecimal) billingAccount.get("accountBalance");
		
		Debug.log("%%%%%%%%%%%%%%%%%%%%%"+buyerId+"=可用额度为"+availableAmount + "%%%%%%%订单金额为："+totalamount);
		/**
		 * 如果当前的订单总额大于 可用的支付余额，在报错
		 */
		if (totalamount.compareTo(new BigDecimal(0))==1 && totalamount.compareTo(availableAmount)==1) {
			System.out.println("创建订单失败!原因是当前订单的总价 ￥" + totalamount + " 已经超出您账户所能支付的总额!您当前账户可支付的总额为: ￥" + availableAmount);
			request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("信用额度不足", "信用额度不足", locale));
			return "error";
		}

		Debug.log("Creating CheckOutHelper.");
		CheckOutHelper checkout = new CheckOutHelper(dispatcher, delegator, cart);
		
		Map<String, Map<String, Object>> selectedPaymentMethods = FastMap.newInstance();
		selectedPaymentMethods.put("EXT_BILLACT", UtilMisc.<String, Object>toMap("amount", new BigDecimal(0)//加入订单金额
			, "securityCode", null));
		checkout.setCheckOutPaymentInternal(selectedPaymentMethods, null, billingAccountId);
		
		//orderedRequirement(cart,delegator,dispatcher,userLogin);//把需求单标记成ORDERED
		
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
				//boolean approved = OrderChangeHelper.approveOrder(dispatcher, userLogin, orderId);

				
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
			request.setAttribute("_EVENT_MESSAGE_",UtilProperties.getMessage("SalesManagerUiLabels","SalesOrderCreateSuccess",UtilMisc.toMap("orderId", orderId),locale));
		}
		return "successToList";
	}

	public static String quickInitPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        Locale locale = UtilHttp.getLocale(request);
        String supplierPartyId = request.getParameter("supplierPartyId_o_0");
        String currencyUomId = request.getParameter("currencyUomId_o_0");
        String purchaseType = request.getParameter("purchaseType_o_0");
        String facilityId = request.getParameter("facilityId_o_0");
        
        
        if (UtilValidate.isEmpty(supplierPartyId)) {
			request.setAttribute("_ERROR_MESSAGE_","供应商不存在");
        }
        
        // check the preferred currency of the supplier, if set, use that for the cart, otherwise use system defaults.
        ShoppingCart cart = null;
        try {
            GenericValue supplierParty = delegator.findOne("Party", UtilMisc.toMap("partyId", supplierPartyId), false);
            if (UtilValidate.isNotEmpty(currencyUomId)) {
                cart = new WebShoppingCart(request, locale, currencyUomId);
            } else {
                cart = new WebShoppingCart(request);
            }
        } catch (GenericEntityException e) {
        	e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
        }
        cart.setOrderAttribute("FABRIC_PURCHASE_TYPE", purchaseType);
        cart.addAdditionalPartyRole(supplierPartyId, "SUPPLIER_AGENT");
        cart.addAdditionalPartyRole(supplierPartyId, "SUPPLIER");
        
        String facilityResult = setSaleFacilityAndShippingAddress(facilityId,cart,delegator);
        if(!facilityResult.equals("")){
			request.setAttribute("_ERROR_MESSAGE_",facilityResult);
			return "error";
		}
        
        String billToCustomerPartyId = request.getParameter("billToCustomerPartyId_o_0");
        if(UtilValidate.isEmpty(billToCustomerPartyId))billToCustomerPartyId = "Company";
        
        cart.setBillToCustomerPartyId(billToCustomerPartyId);
        cart.setBillFromVendorPartyId(supplierPartyId);
        cart.setOrderPartyId(supplierPartyId);
        cart.setOrderType("PURCHASE_ORDER");

        session.setAttribute("shoppingCart", cart);
        session.setAttribute("productStoreId", cart.getProductStoreId());
        session.setAttribute("orderMode", cart.getOrderType());
        session.setAttribute("orderPartyId", cart.getOrderPartyId());

        return "success";
    }

	public static void orderedRequirement(ShoppingCart cart,Delegator delegator,LocalDispatcher dispatcher,GenericValue userLogin){
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
	
	/**
	 * 创建销售订单购物车 TEST用
	 * by liujia
	 */
	@SuppressWarnings("unchecked")
	public synchronized Map<String, Object> createSaleShoppingCart(Delegator delegator, LocalDispatcher dispatcher, Map<String, Object> context, boolean create) {
		List<String> orderImportSuccessMessageList = FastList.newInstance();
		List<String> orderImportFailureMessageList = FastList.newInstance();
		Map<String, Object> reusltMap = FastMap.newInstance();
		reusltMap = ServiceUtil.returnSuccess();
		String productStoreId = (String) context.get("productStoreId");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Locale locale = (Locale) context.get("locale");
		String isHistoryOrder = (String)context.get("isHistoryOrder");	//这次导入是否是历史订单
		String billingAccountId = null;									//客户的账单编号
		// 设置外单号
		String externalId = (String) context.get("externalId");
		// 买家的partyId
		String partyId = (String) context.get("partyId");
		if (UtilValidate.isEmpty(partyId)) {
			return ServiceUtil.returnError("客户id不能为空!");
		}
		
		try {
			
			GenericValue partyAttribute = EntityUtil.getFirst(delegator.findByAnd("Party", UtilMisc.toMap("partyId",partyId)));
			if (UtilValidate.isEmpty(partyAttribute)) {
				 Debug.log("订单id为" + externalId + "的订单中客户编码错误");
				 return ServiceUtil.returnError("订单id为" + externalId + "的订单中客户编码错误");
			}
			partyId = partyAttribute.getString("partyId");

			String defaultCurrencyUomId = null;
			String payToPartyId = null;
			String facilityId = null;

			// 店铺id需要验证，并根据此id获取配置参数
			if (productStoreId == null) {
				return ServiceUtil.returnError(UtilProperties.getMessage(resource, "", locale));
			} else {
				GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
				if (productStore != null) {
					defaultCurrencyUomId = productStore.getString("defaultCurrencyUomId");
					payToPartyId = productStore.getString("payToPartyId");
					//facilityId = productStore.getString("inventoryFacilityId");
					facilityId = (String) context.get("facilityId");
				} else {
					return ServiceUtil.returnError(UtilProperties.getMessage(resource, "ordersImportFromTaobao.productStoreIdIsMandatory", locale));
				}
			}

			//只有非历史订单需要验证信用额度
			if(isHistoryOrder==null){
				List<Map<String, Object>> billingAccountList = BillingAccountWorker.makePartyBillingAccountList(userLogin, defaultCurrencyUomId, partyId,delegator, dispatcher);
				if(billingAccountList==null || billingAccountList.size()<=0){
					//return ServiceUtil.returnFailure(UtilProperties.getMessage(resource, "余额不足", locale));
					Debug.log("请为"+partyId+"客户在系统中录入信用额度");
					return ServiceUtil.returnError("客户无信用额度");
				}
				// 获取可用的余额
				Map<String, Object> billingAccount = billingAccountList.get(0);
				billingAccountId = (String) billingAccount.get("billingAccountId");
				
				//当前订单金额
				String samountPaid = (String) context.get("amountPaid");
				double amountPaid = 0;
				if (UtilValidate.isNotEmpty(samountPaid)) {
					amountPaid = Double.parseDouble(samountPaid);
				}
				
				BigDecimal availableAmount = (BigDecimal) billingAccount.get("accountBalance");
				
				Debug.log("%%%%%%%%%%%%%%%%%%%%%"+partyId+"=可用额度为"+availableAmount + "%%%%%%%订单金额为："+amountPaid);
				/**
				 * 如果当前的订单总额大于 可用的支付余额，在报错
				 */
				if (amountPaid>0 && amountPaid > availableAmount.doubleValue()) {
					Debug.log("创建订单失败!原因是当前订单的总价 ￥" + amountPaid + " 已经超出您账户所能支付的总额!您当前账户可支付的总额为: ￥" + availableAmount.doubleValue());
					return ServiceUtil.returnError("信用额度不足");
				}
			}

			// 创建一个新的购物车
			ShoppingCart cart = new ShoppingCart(delegator, productStoreId, locale, defaultCurrencyUomId);

			if (UtilValidate.isNotEmpty(externalId)) {
				if (checkOrderExistByExternalId(delegator, externalId,"SALES_ORDER") != null && create) {
					Debug.log("外单号已经存在======externalId="+externalId);
					return ServiceUtil.returnError("外单号已经存在");
				}
				cart.setExternalId(externalId);
			} else {
				Debug.log("订单没有外单号");
				return ServiceUtil.returnError( "订单没有外单号");
			}

			// 设置订单类型为销售订单即SALES_ORDER
			cart.setOrderType("SALES_ORDER");
			// 设置销售平台
			// cart.setChannelType("ALAND_SALES_CHANNEL");
			// 设置当前登录用户
			cart.setUserLogin(userLogin, dispatcher);
			// 设置产品店铺的id
			cart.setProductStoreId(productStoreId);
			// 买家备注
			String orderNote = (String) context.get("orderNote");
			// 卖家备注
			String internalNote = (String) context.get("internalNote");
			if (UtilValidate.isNotEmpty(orderNote)) {
				// 设置买家加留言或买家备注
				cart.addOrderNote(orderNote);
			}
			if (UtilValidate.isNotEmpty(internalNote)) {
				// 设置卖家备注
				cart.addInternalOrderNote(internalNote);
			}
			if (UtilValidate.isNotEmpty(facilityId)) {
				// 设置仓库id
				cart.setFacilityId(facilityId);
			}

			// 金额类型
			//cart.addPaymentAmount("EXT_BILLACT", BigDecimal.valueOf(amountPaid), externalId, null, true, false, false);

			// 设置erp订单状态为订单的创建时间
			Timestamp orderDate = UtilDateTime.nowTimestamp();
			if (UtilValidate.isNotEmpty(context.get("createdDate"))) {
				Date dates = (Date) context.get("createdDate");
				orderDate = UtilDateTime.toTimestamp(dates);
			}
			cart.setOrderDate(orderDate);

			/**
			 * 处理订单项
			 */
			List<Map<String, Object>> orderItemList = (List<Map<String, Object>>) context.get("orderItemList");
			Iterator<Map<String, Object>> orderItemIter = orderItemList.iterator();
			while (orderItemIter.hasNext()) {
				Map<String, Object> orderItem = (Map<String, Object>) orderItemIter.next();
				String productCode = (String)orderItem.get("productId");
				String productId = null;

				
				
				// 增加订单项
				addItem(cart, orderItem, dispatcher, delegator, 0, orderImportFailureMessageList);
			}

			// 设置支付对象，即支付给卖家的卖家id
			if (UtilValidate.isNotEmpty(payToPartyId)) {
				cart.setBillFromVendorPartyId(payToPartyId);
			}
			
			// 创建订单
			if (create) {
				Debug.log("%%%%%%%%%% 开始导入订单...");

				GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));

				Map<String, Object> shippingAddressContextParams = (Map<String, Object>) context.get("shippingAddressContextParams");
				//是否可用 setSaleFacilityAndShippingAddress(facilityId,cart,delegator);
//				String contactMechId = UtilContactInfo.processShippingAddressContactMech(dispatcher, delegator, party, userLogin, shippingAddressContextParams);
//				UtilContactInfo.processPhoneContactMech(dispatcher, delegator, party, userLogin, shippingAddressContextParams);
//				UtilContactInfo.processEmailContactMech(dispatcher, delegator, party, userLogin, context);
				String contactMechId = "";
				Debug.log("Setting cart roles for party: " + partyId);
				cart.setBillToCustomerPartyId(partyId);
				cart.setPlacingCustomerPartyId(partyId);
				cart.setShipToCustomerPartyId(partyId);
				cart.setEndUserCustomerPartyId(partyId);

				Debug.log("Setting contact mech in cart: " + contactMechId);
				cart.setShippingContactMechId(0, contactMechId);
				cart.setMaySplit(0, Boolean.FALSE);

//				Debug.log("Setting shipment method: " + (String) shippingServiceSelectedContextParams.get("shippingService"));
//				setShipmentMethodType(cart, (String) shippingServiceSelectedContextParams.get("shippingService"), productStoreId, delegator);
//				cart.setShipGroupFacilityId(0, cart.getFacilityId());
//				cart.makeAllShipGroupInfos();

				Debug.log("Creating CheckOutHelper.");
				CheckOutHelper checkout = new CheckOutHelper(dispatcher, delegator, cart);
				
				Map<String, Map<String, Object>> selectedPaymentMethods = FastMap.newInstance();
				//设置账单支付 如果是历史订单，就是离线支付，如果是传统订单，就是账单支付 
				if(isHistoryOrder!=null && isHistoryOrder.equals("1")){
					selectedPaymentMethods.put("EXT_OFFLINE", UtilMisc.<String, Object>toMap("amount", null));
					checkout.setCheckOutPaymentInternal(selectedPaymentMethods, null, null);
					Debug.log("%%%%%%%%%%%%%%%%"+externalId+"支付方式:=EXT_OFFLINE");
				}else{
					selectedPaymentMethods.put("EXT_BILLACT", UtilMisc.<String, Object>toMap("amount", new BigDecimal(0)//加入订单金额
					, "securityCode", null));
					checkout.setCheckOutPaymentInternal(selectedPaymentMethods, null, billingAccountId);
					Debug.log("%%%%%%%%%%%%%%%%"+externalId+"支付方式:=EXT_BILLACT");
				}
				
				Debug.log("Creating order.");
				// 创建订单
				Map<?, ?> orderCreate = checkout.createOrder(userLogin);

				if ("error".equals(orderCreate.get("responseMessage"))) {
					//return ServiceUtil.returnError(errorMessageList);
					Debug.log("-----------------创建订单时系统错误");
					return reusltMap;
				}
				String orderId = (String) orderCreate.get("orderId");
				Debug.log("Created order with id: " + orderId);

				if (UtilValidate.isNotEmpty(orderId)) {
					String orderCreatedMsg = "成功导入订单,生成订单号为：" + orderId + " 关联外单号为: " + externalId;
					orderImportSuccessMessageList.add(orderCreatedMsg);
					reusltMap.put("resultSuccess", externalId);
					reusltMap.put("orderId", orderId);
				}

				// 批准订单
				if (UtilValidate.isNotEmpty(orderId)) {

					Debug.log("Approving order with id: " + orderId);
					boolean approved = OrderChangeHelper.approveOrder(dispatcher, userLogin, orderId);
					Debug.log("Order approved with result: " + approved);

					// 更具支付参数创建付款
					if (approved) {
						Debug.log("Creating payment for approved order.");
						createPaymentFromPaymentPreferences(delegator, dispatcher, userLogin, orderId, externalId, cart.getOrderDate(), partyId);
						Debug.log("Payment created.");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Debug.log("Exception in createShoppingCart: " + e.getMessage());
			return ServiceUtil.returnError(UtilProperties.getMessage(resource, "ordersImportFromTaobao.exceptionInCreateShoppingCart", locale) + ": "
					+ e.getMessage());
		}
		return reusltMap;

	}
	
	public static GenericValue makeOrderAdjustment(Delegator delegator, String orderAdjustmentTypeId, String orderId, String orderItemSeqId,
			String shipGroupSeqId, double amount, double sourcePercentage) {
		GenericValue orderAdjustment = null;

		try {
			if (UtilValidate.isNotEmpty(orderItemSeqId)) {
				orderItemSeqId = "_NA_";
			}
			if (UtilValidate.isNotEmpty(shipGroupSeqId)) {
				shipGroupSeqId = "_NA_";
			}

			Map<String, Object> inputMap = UtilMisc.toMap("orderAdjustmentTypeId", orderAdjustmentTypeId, "orderId", orderId, "orderItemSeqId", orderItemSeqId,
					"shipGroupSeqId", shipGroupSeqId, "amount", new BigDecimal(amount));
			if (sourcePercentage != 0) {
				inputMap.put("sourcePercentage", new Double(sourcePercentage));
			}
			orderAdjustment = delegator.makeValue("OrderAdjustment", inputMap);
		} catch (Exception e) {
			Debug.log(e, "Failed to made order adjustment for order " + orderId);
		}
		return orderAdjustment;
	}
	
	public static GenericValue checkOrderExistByExternalId(Delegator delegator, String externalId,String orderType) {
		Debug.log("Checking for existing externalId: " + externalId);
		List<GenericValue> orderHeaderList = null;
		try {
			orderHeaderList = delegator.findByAnd("OrderHeader", UtilMisc.toMap("externalId", externalId,"orderTypeId",orderType));
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if (UtilValidate.isNotEmpty(orderHeaderList)) {
			return EntityUtil.getFirst(orderHeaderList);
		}
		return null;
	}
	
	/**
	 * 模拟购物流程中的调用事件
	 */
	private synchronized static void addItem(ShoppingCart cart, Map<String, Object> orderItem, LocalDispatcher dispatcher, Delegator delegator, int groupIdx,
			List<String> orderImportFailureMessageList) throws GeneralException {
		String productId = (String) orderItem.get("productId");
		BigDecimal qty = new BigDecimal(orderItem.get("quantity").toString());
		String itemPrice = (String) orderItem.get("itemPrice");
		if (UtilValidate.isEmpty(itemPrice)) {
			itemPrice = (String) orderItem.get("amount");
		}
		BigDecimal price = new BigDecimal(itemPrice);
		price = price.setScale(ShoppingCart.scale, ShoppingCart.rounding);

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("shipGroup", groupIdx);
		/**
		 * String productId, BigDecimal amount, BigDecimal quantity, BigDecimal
		 * unitPrice, HashMap<String, GenericValue> features, HashMap<String,
		 * Object> attributes, String prodCatalogId, String itemType,
		 * LocalDispatcher dispatcher, Boolean triggerExternalOps, Boolean
		 * triggerPriceRules, Boolean skipInventoryChecks, Boolean
		 * skipProductChecks
		 */
		int idx = cart.addItemToEnd(productId, null, qty, null, null, attrs, null, null, dispatcher, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
		ShoppingCartItem cartItem = cart.findCartItem(idx);
		
		cartItem.setQuantity(qty, dispatcher, cart, true, false);
		// locate the price verify it matches the expected price
		BigDecimal cartPrice = cartItem.getBasePrice();
		cartPrice = cartPrice.setScale(ShoppingCart.scale, ShoppingCart.rounding);
		if (price.doubleValue() != cartPrice.doubleValue()) {
			// does not match; honor the price but hold the order for manual
			// review
			cartItem.setIsModifiedPrice(true);
			cartItem.setBasePrice(price);
			cart.setHoldOrder(true);
			cart.addInternalOrderNote("price received [" + price + "] (for item # " + productId
					+ ") from aland checkout does not match the price in the database [" + cartPrice + "]. Order is held for manual review.");
		}
		// assign the item to its ship group
		cart.setItemShipGroupQty(cartItem, qty, groupIdx);
	}
	
	@SuppressWarnings("deprecation")
	public static void setShipmentMethodType(ShoppingCart cart, String shippingService, String productStoreId, Delegator delegator) {
		String partyId = "_NA_";
		String shipmentMethodTypeId = "NO_SHIPPING";

		cart.setCarrierPartyId(partyId);
		cart.setShipmentMethodTypeId(shipmentMethodTypeId);
	}
	
	public static boolean createPaymentFromPaymentPreferences(Delegator delegator, LocalDispatcher dispatcher, GenericValue userLogin, String orderId,
			String externalId, Timestamp orderDate, String partyIdFrom) {
		List<GenericValue> paymentPreferences = null;
		try {
			Map<String, String> paymentFields = UtilMisc.toMap("orderId", orderId, "statusId", "PAYMENT_RECEIVED", "paymentMethodTypeId", "EXT_BILLACT");
			paymentPreferences = delegator.findByAnd("OrderPaymentPreference", paymentFields);

			if (UtilValidate.isNotEmpty(paymentPreferences)) {
				Iterator<GenericValue> i = paymentPreferences.iterator();
				while (i.hasNext()) {
					GenericValue pref = (GenericValue) i.next();
					boolean okay = createPayment(dispatcher, userLogin, pref, orderId, externalId, orderDate, partyIdFrom);
					if (!okay)
						return false;
				}
			}
		} catch (Exception e) {
			Debug.log(e, "Cannot get payment preferences for order #" + orderId);
			return false;
		}
		return true;
	}
	
	public static boolean createPayment(LocalDispatcher dispatcher, GenericValue userLogin, GenericValue paymentPreference, String orderId, String externalId,
			Timestamp orderDate, String partyIdFrom) {
		try {
			Delegator delegator = paymentPreference.getDelegator();

			// create the PaymentGatewayResponse
			String responseId = delegator.getNextSeqId("PaymentGatewayResponse");
			GenericValue response = delegator.makeValue("PaymentGatewayResponse");
			response.set("paymentGatewayResponseId", responseId);
			response.set("paymentServiceTypeEnumId", "PRDS_PAY_EXTERNAL");
			response.set("orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"));
			response.set("paymentMethodTypeId", paymentPreference.get("paymentMethodTypeId"));
			response.set("paymentMethodId", paymentPreference.get("paymentMethodId"));
			response.set("amount", paymentPreference.get("maxAmount"));
			response.set("referenceNum", externalId);
			response.set("transactionDate", orderDate);
			delegator.createOrStore(response);

			// create the payment
			Map<String, Object> results = dispatcher.runSync("createPaymentFromPreference", UtilMisc
					.toMap("userLogin", userLogin, "orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"), "paymentFromId", partyIdFrom,
							"paymentRefNum", externalId, "comments", "Payment receive"));

			if ((results == null)) {
				//Debug.log((String) results.get(ModelService.ERROR_MESSAGE));
				return false;
			}
			return true;
		} catch (Exception e) {
			Debug.log(e, "Failed to create the payment for order " + orderId);
			return false;
		}
	}
	
	/**
	 * 批准订单
	 * by liujia
	 */
	public static String changeOrderStatus(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		String MODE = request.getParameter("MODE");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		Locale locale = UtilHttp.getLocale(request);
		String orderId = request.getParameter("orderId");
		GenericValue order = null;
		// 批准订单
		if (UtilValidate.isNotEmpty(orderId)) {
			try {
				List<GenericValue> orderList = delegator.findByAnd("FindSalesOrderView", UtilMisc.toMap("orderId",orderId));
				order = EntityUtil.getFirst(orderList);
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			boolean approved = OrderChangeHelper.approveOrder(dispatcher, userLogin, orderId);

				//批准order
				//dispatcher.runSync("changeOrderStatus",UtilMisc.toMap("orderId", orderId, "statusId","ORDER_APPROVED","userLogin", userLogin));
				//批准orderItem
				//dispatcher.runSync("changeOrderItemStatus", UtilMisc.toMap("orderId", orderId, "statusId", "ITEM_APPROVED", "userLogin", userLogin));

			
			// 更具支付参数创建付款
//			if (approved && order !=null) {
//				Debug.log("Creating payment for approved order.");
//				createPaymentFromPaymentPreferences(delegator, dispatcher, userLogin, orderId, "", java.sql.Timestamp.valueOf((String)order.get("orderDate")), (String)order.get("partyId"));
//				Debug.log("Payment created.");
//			}
			if(approved == false){
				request.setAttribute("_ERROR_MESSAGE_",UtilProperties.getMessage("订单批准失败,请联系系统管理员", "", locale));
				return "error";
			}
		}
		return "success";
	}
	
	/**
	 * 从报价导入到销售订单
	 * by liujia
	 */
	public static String importSalesOrder(HttpServletRequest request, HttpServletResponse response,List<Map<String,Object>> quoteItemList,String quoteId,String shipContactMechId,String shipId,String paymentType){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		if(quoteId==null||quoteId.equals("")){
			quoteId = (String) request.getParameter("quoteId");
		}

		try {
			GenericValue quote = delegator.findByPrimaryKey("Quote", UtilMisc.toMap("quoteId", quoteId));
			String buyerId = (String)quote.get("partyId");	//买家partyId
			String productStoreId = (String)quote.get("productStoreId");//产品店铺
			if (UtilValidate.isEmpty(buyerId)) {
				request.setAttribute("_ERROR_MESSAGE_","partyId不能为空");
				return "error";
			}
			if (UtilValidate.isEmpty(productStoreId)) {
				request.setAttribute("_ERROR_MESSAGE_","productStoreId不能为空");
				return "error";
			}
			
			String defaultCurrencyUomId = null;
			String payToPartyId = null;
			String facilityId = null;

			// 店铺id需要验证，并根据此id获取配置参数
			GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
			if (productStore != null) {
				defaultCurrencyUomId = productStore.getString("defaultCurrencyUomId");
				payToPartyId = productStore.getString("payToPartyId");
				facilityId = productStore.getString("inventoryFacilityId");
			} else {
				request.setAttribute("_ERROR_MESSAGE_","产品店铺无对应");
				return "error";
			}
			
			ShoppingCart cart = new ShoppingCart(delegator, productStoreId, null, defaultCurrencyUomId);

			cart.setOrderType("SALES_ORDER");	//订单类型
			cart.setOrderAttribute("FROM_QUOTE", quoteId); //导入订单方式为QUOTE导入
			cart.setBillToCustomerPartyId(buyerId);	//买家
			cart.setPlacingCustomerPartyId(buyerId);
			cart.setShipToCustomerPartyId(buyerId);
			cart.setEndUserCustomerPartyId(buyerId);
			
			if (UtilValidate.isNotEmpty(payToPartyId)) {
				cart.setBillFromVendorPartyId(payToPartyId);	//账单来自于
				cart.addAdditionalPartyRole(payToPartyId, "BILL_FROM_VENDOR");
			}
			
			cart.addAdditionalPartyRole(buyerId, "BILL_TO_CUSTOMER");	//账单给予
			cart.setFacilityId(facilityId);	//设置仓库
			cart.setUserLogin(userLogin, dispatcher);	// 设置当前登录用户 
			cart.setProductStoreId(productStoreId);
			cart.setOrderDate(UtilDateTime.nowTimestamp());	//设置订单时间
			
			//获取买家的收货地址
//			String buyerContactMechPurposeTypeId = "";
//			String contactMechId = "";
//			List<Map<String, Object>> buyerContactMechList = ContactMechWorker.getPartyContactMechValueMaps(delegator, buyerId, false);
//			if(buyerContactMechList==null){
//				request.setAttribute("_ERROR_MESSAGE_","编号为" + buyerId +"地址是空的");
//				return "error";
//			}else{
//				for(int i=0;i<buyerContactMechList.size();i++){
//					List<GenericValue> buyerContactMechPurposesList = (List<GenericValue>) buyerContactMechList.get(i).get("partyContactMechPurposes");
//					if(buyerContactMechPurposesList!=null){
//						GenericValue buyerContactMechPurposes = buyerContactMechPurposesList.get(0);
//						String tempContactMechPurposeTypeId = (String) buyerContactMechPurposes.get("contactMechPurposeTypeId");
//						if(tempContactMechPurposeTypeId.equals("SHIPPING_LOCATION")){
//							buyerContactMechPurposeTypeId = tempContactMechPurposeTypeId;
//						}
//					}						
//					if(!buyerContactMechPurposeTypeId.equals("")){
//						GenericValue buyerContactMech = (GenericValue) buyerContactMechList.get(i).get("contactMech");
//						contactMechId = (String) buyerContactMech.get("contactMechId");
//					}
//				}
//			}
//			
//			//设置地址类型为收货地址
//			if(contactMechId.equals("")){
//				request.setAttribute("_ERROR_MESSAGE_","编号为" + buyerId +"收货地址是空的");
//				return "error";
//			}
//			cart.setShippingContactMechId(0, contactMechId);
//			cart.setMaySplit(0, Boolean.FALSE);
//			cart.setShipGroupFacilityId(0, cart.getFacilityId());
			//买家的收货地址从外部传入
			if(shipContactMechId.equals("")){
				request.setAttribute("_ERROR_MESSAGE_","编号为" + buyerId +"收货地址是空的");
				return "error";
			}
			cart.setShippingContactMechId(0, shipContactMechId);
			cart.setMaySplit(0, Boolean.FALSE);
			cart.setShipGroupFacilityId(0, cart.getFacilityId());
			
			//设置货运方式 默认取第一条
//			GenericValue shipInfo = EntityUtil.getFirst(delegator.findByAnd("ProductStoreShipmentMethView", UtilMisc.toMap("productStoreId",productStoreId)));
//			cart.setOrderAttribute("ORDER_SHIP", (String)shipInfo.get("productStoreShipMethId"));	
//			cart.setAllCarrierPartyId((String)shipInfo.get("partyId"));
//			cart.setAllShipmentMethodTypeId((String)shipInfo.get("shipmentMethodTypeId"));
//			cart.makeAllShipGroupInfos();
			//货运方式id从外部传入
			GenericValue shipInfo = EntityUtil.getFirst(delegator.findByAnd("ProductStoreShipmentMethView", UtilMisc.toMap("productStoreShipMethId",shipId)));
			cart.setOrderAttribute("ORDER_SHIP", (String)shipInfo.get("productStoreShipMethId"));	
			cart.setAllCarrierPartyId((String)shipInfo.get("partyId"));
			cart.setAllShipmentMethodTypeId((String)shipInfo.get("shipmentMethodTypeId"));
			cart.makeAllShipGroupInfos();
			
			//加载订单项 From QuoteItem
			if(quoteItemList!=null&&quoteItemList.size()>0){
				for(int i=0;i<quoteItemList.size();i++){
					Map<String,Object> quoteItem = quoteItemList.get(i);
					String productId = (String)quoteItem.get("productId");
					BigDecimal quantity = new BigDecimal((String)quoteItem.get("quantity"));
					String quoteUnitPrice = (String)quoteItem.get("quoteUnitPrice");
					quoteUnitPrice = quoteUnitPrice.replace(",", "");
					BigDecimal unitPrice = new BigDecimal(quoteUnitPrice);
					String quoteItemSeqId = (String)quoteItem.get("quoteItemSeqId");
					GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
					
		            GenericValue orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("productId",productId,
		            															"itemDescription",UtilValidate.isEmpty(product.getString("productName"))?product.getString("internalName"):product.getString("productName"),
																				"unitPrice",unitPrice,
																				"quantity",quantity,
																				"quoteId",quoteId,
																				"quoteItemSeqId",quoteItemSeqId
																				));
		            Map<String,String> attributes = null;
					addItem(cart,orderItem,dispatcher,delegator,0,attributes);
				}
			}else{
				List<GenericValue> allquoteItemList = delegator.findByAnd("QuoteItem", UtilMisc.toMap("quoteId",quoteId));
				if(allquoteItemList!=null&&allquoteItemList.size()>0){
					for(int i=0;i<allquoteItemList.size();i++){
						GenericValue quoteItem = allquoteItemList.get(i);
						String productId = (String)quoteItem.get("productId");
						BigDecimal quantity = (BigDecimal) quoteItem.get("quantity");
						BigDecimal unitPrice = (BigDecimal) quoteItem.get("quoteUnitPrice");
						GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
						
			            GenericValue orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("productId",productId,
			            															"itemDescription",UtilValidate.isEmpty(product.getString("productName"))?product.getString("internalName"):product.getString("productName"),
																					"unitPrice",unitPrice,
																					"quantity",quantity
																					));
			            Map<String,String> attributes = null;
						addItem(cart,orderItem,dispatcher,delegator,0,attributes);
					}
				}
			}
			
			//设置购物车货币类型
			if(defaultCurrencyUomId.isEmpty()){
				defaultCurrencyUomId = "CNY";
			}
        	cart.setCurrency(dispatcher, defaultCurrencyUomId);
			
        	String billingAccountId = null; 
			//检查买家信用额度
        	if(paymentType.equals("EXT_BILLACT")){
        		
    			List<Map<String, Object>> billingAccountList = new ArrayList();
    			try {
    				billingAccountList = BillingAccountWorker.makePartyBillingAccountList(userLogin, defaultCurrencyUomId, buyerId,delegator, dispatcher);
    			} catch (GeneralException e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
    			if(billingAccountList==null || billingAccountList.size()<=0){
    				request.setAttribute("_ERROR_MESSAGE_","客户无信用额度");
    				return "error";
    			}				
    			//获取可用的余额
    			Map<String, Object> billingAccount = billingAccountList.get(0);
    			billingAccountId = (String) billingAccount.get("billingAccountId");
    			
    			//当前订单金额
    			BigDecimal totalamount = (BigDecimal)cart.getItemTotal();
    			BigDecimal availableAmount = (BigDecimal) billingAccount.get("accountBalance");
    			
    			/**
    			 * 如果当前的订单总额大于 可用的支付余额，在报错
    			 */
    			if (totalamount.compareTo(new BigDecimal(0))==1 && totalamount.compareTo(availableAmount)==1) {
    				System.out.println("创建订单失败!原因是当前订单的总价 ￥" + totalamount + " 已经超出您账户所能支付的总额!您当前账户可支付的总额为: ￥" + availableAmount);
    				String errorReason = "创建订单失败!原因是当前订单的总价 ￥" + totalamount + " 已经超出您账户所能支付的总额!您当前账户可支付的总额为: ￥" + availableAmount;
    				request.setAttribute("_ERROR_MESSAGE_",errorReason);
    				return "error";
    			}
        	}
			
			

			
			//检查订单
			Debug.log("Creating CheckOutHelper.");
			CheckOutHelper checkout = new CheckOutHelper(dispatcher, delegator, cart);

			Map<String, Map<String, Object>> selectedPaymentMethods = FastMap.newInstance();
			selectedPaymentMethods.put(paymentType, UtilMisc.<String, Object>toMap("amount", new BigDecimal(0)//加入订单金额
				, "securityCode", null));
			
			checkout.setCheckOutPaymentInternal(selectedPaymentMethods, null, billingAccountId);

			// 创建订单
			Map<String, Object> orderCreate = checkout.createOrder(userLogin);
			if ("error".equals(orderCreate.get("responseMessage"))) {
				request.setAttribute("_ERROR_MESSAGE_",ServiceUtil.getErrorMessage(orderCreate));
				return "error";
			}
			String orderId = (String) orderCreate.get("orderId");
			
			String buyerFacilityId = "";
			//获取收货地场所信息 根据买家id获取
			GenericValue buyerProductStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", buyerId));
			if (buyerProductStore != null) {
				buyerFacilityId = buyerProductStore.getString("inventoryFacilityId");
			} else {
				request.setAttribute("_ERROR_MESSAGE_","买家收货地场所为空");
				return "error";
			}
			
        	//创建送货信息
        	Map<String, Object> serviceResult = dispatcher.runSync("createShipment",
        			UtilMisc.toMap("originFacilityId", facilityId, "destinationFacilityId", buyerFacilityId, 
        					"shipmentTypeId", "SALES_SHIPMENT", "statusId", "SHIPMENT_INPUT", "primaryOrderId",orderId,"userLogin", userLogin));
//        	String shipmentId = (String) serviceResult.get("shipmentId");
        	//创建送货子项计划信息
//        	serviceResult = dispatcher.runSync("createShipmentItem",
//        			UtilMisc.toMap("productId", productId, "quantity", quantity, "shipmentId", shipmentId, "userLogin", userLogin));
//        	String shipmentItemSeqId = (String) serviceResult.get("shipmentItemSeqId");
//        	
			//批准订单
			if (UtilValidate.isNotEmpty(orderId)) {
				//批准orderHeader
				boolean approved = OrderChangeHelper.approveOrder(dispatcher, userLogin, orderId);
				if(approved == false){
					request.setAttribute("_ERROR_MESSAGE_","订单批准失败,请联系系统管理员");
					return "error";
				}
		        ShoppingCartEvents.destroyCart(request, response);
			}
			
        	request.setAttribute("_EVENT_MESSAGE_","订单导入成功,生成订单号为"+orderId);
        	
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_",e.getMessage());
			return "error";
		}
		
		return "success";
	}
	
}
