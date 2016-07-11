package com.zuczug.sales;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class UpdateSaleStoreServices {
	
	public static final String module = UpdateSaleStoreServices.class.getName();
	
	/**
	 * 更新门店
	 * by liujia
	 */
	public static Map<String, Object> updateSaleStore(DispatchContext ctx, Map<String, ? extends Object> context) throws GenericEntityException {
		Delegator delegator = ctx.getDelegator();
		LocalDispatcher dispatcher = ctx.getDispatcher();
		Map<String, Object> resultMap = ServiceUtil.returnError("");
		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String productStoreId = (String) context.get("productStoreId");
		String facilityId = (String) context.get("facilityId");
		String storeName = (String) context.get("storeName");
		String payToPartyId = (String) context.get("payToPartyId");
		String city = (String) context.get("city");
		String address1 = (String) context.get("address1");
		String askForName = (String) context.get("askForName");
		String fax = (String) context.get("fax");
		String phone = (String) context.get("phone");
		String mobile = (String) context.get("mobile");
		String email = (String) context.get("email");

		String addressMechId = (String) context.get("addressMechId");
		String faxMechId = (String) context.get("faxMechId");
		String mobileMechId = (String) context.get("mobileMechId");
		String phoneMechId = (String) context.get("phoneMechId");
		String emailMechId = (String) context.get("emailMechId");

		String postalCode = (String) context.get("postalCode");
		String shippingAddress = (String) context.get("shippingAddress");
		String shippingAddressMechId = (String) context.get("shippingAddressMechId");
		
		BigDecimal area = (BigDecimal) context.get("area");
		String storeTypeId = (String) context.get("storeTypeId");
		String storeStyleId = (String) context.get("storeStyleId");
		String storeLevelId = (String) context.get("storeLevelId");
		String storeFeatureId = (String) context.get("storeFeatureId");
		String storeOwnerRegionId = (String) context.get("storeOwnerRegionId");
		String storeBusinessModelId = (String) context.get("storeBusinessModelId");
		String regionManager = (String) context.get("regionManager");
		
		String oldStoreTypeId = "";
		if (UtilValidate.isNotEmpty(context.get("oldStoreTypeId"))) {
			oldStoreTypeId = (String) context.get("oldStoreTypeId");
        }
		Timestamp oldTypeFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldTypeFromDate"))) {
			oldTypeFromDate = (Timestamp) context.get("oldTypeFromDate");
        }
		String oldStoreStyleId = "";
		if (UtilValidate.isNotEmpty(context.get("oldStoreStyleId"))) {
			oldStoreStyleId = (String) context.get("oldStoreStyleId");
        }
		Timestamp oldStyleFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldStyleFromDate"))) {
			oldStyleFromDate = (Timestamp) context.get("oldStyleFromDate");
        }
		String oldStoreLevelId = "";
		if (UtilValidate.isNotEmpty(context.get("oldStoreLevelId"))) {
			oldStoreLevelId = (String) context.get("oldStoreLevelId");
        }
		Timestamp oldLevelFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldLevelFromDate"))) {
			oldLevelFromDate = (Timestamp) context.get("oldLevelFromDate");
        }
		String oldStoreFeatureId = "";
		if (UtilValidate.isNotEmpty(context.get("oldStoreFeatureId"))) {
			oldStoreFeatureId = (String) context.get("oldStoreFeatureId");
        }
		Timestamp oldFeatureFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldFeatureFromDate"))) {
			oldFeatureFromDate = (Timestamp) context.get("oldFeatureFromDate");
        }
		String oldStoreOwnerRegionId = "";
		if (UtilValidate.isNotEmpty(context.get("oldStoreOwnerRegionId"))) {
			oldStoreOwnerRegionId = (String) context.get("oldStoreOwnerRegionId");
        }
		Timestamp oldOwnerRegionFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldOwnerRegionFromDate"))) {
			oldOwnerRegionFromDate = (Timestamp) context.get("oldOwnerRegionFromDate");
        }
		String oldStoreBusinessModelId = "";
		if (UtilValidate.isNotEmpty(context.get("oldStoreBusinessModelId"))) {
			oldStoreBusinessModelId = (String) context.get("oldStoreBusinessModelId");
        }
		Timestamp oldBusinessModelFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldBusinessModelFromDate"))) {
			oldBusinessModelFromDate = (Timestamp) context.get("oldBusinessModelFromDate");
        }
		String oldRegionManager = "";
		if (UtilValidate.isNotEmpty(context.get("oldRegionManager"))) {
			oldRegionManager = (String) context.get("oldRegionManager");
        }
		Timestamp oldStoreRoleFromDate = UtilDateTime.nowTimestamp();
		if (UtilValidate.isNotEmpty(context.get("oldStoreRoleFromDate"))) {
			oldStoreRoleFromDate = (Timestamp) context.get("oldStoreRoleFromDate");
        }
		
		
        Timestamp openedDate = null;
        if (UtilValidate.isNotEmpty(context.get("openedDate"))) {
        	openedDate = (Timestamp) context.get("openedDate");
        }
        Timestamp closedDate = null;
        if (UtilValidate.isNotEmpty(context.get("closedDate"))) {
        	closedDate = (Timestamp) context.get("closedDate");
        }
		
		
		GenericValue facility = null;
		
		try{
			//更新商店
			dispatcher.runSync("updateProductStore", UtilMisc.toMap("userLogin", userLogin, "productStoreId", productStoreId,
						"storeName", storeName, "payToPartyId",
						payToPartyId));

			//更新场所实体
			dispatcher.runSync("updateFacility", UtilMisc.toMap("userLogin", userLogin,
					"facilityName", storeName, 
					"ownerPartyId", payToPartyId,
					"facilityId", facilityId,"facilitySize",area,"openedDate",openedDate,"closedDate",closedDate));
			
			//更新Party
			dispatcher.runSync("updatePartyGroup", UtilMisc.toMap("userLogin", userLogin,
					"partyId", productStoreId,
					"ownerPartyId", payToPartyId));
			
			dispatcher.runSync("updatePartyGroup", UtilMisc.toMap("userLogin", userLogin,
					"partyId", productStoreId,
					"ownerPartyId", payToPartyId));
			
			
			//更新地址
			if ( UtilValidate.isEmpty(addressMechId) ) {
				//新增地址
				if ( !UtilValidate.isEmpty(address1) ) {
					if(postalCode==null){
			        	postalCode = "0";
			        }
			        Map<String, Object> invReqResult = dispatcher.runSync("createFacilityPostalAddress",  
							UtilMisc.toMap("userLogin", userLogin,
							"facilityId", facilityId, "fromDate",new Timestamp(new Date().getTime()),
							"address1", address1,
							"city", city, "postalCode", postalCode ));
			        if (!invReqResult.isEmpty()){
						String contactMechId = (String) invReqResult.get("contactMechId");
						dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
								"contactMechId", contactMechId, "facilityId",facilityId,
								"contactMechPurposeTypeId", "PRIMARY_LOCATION"));
					}
				}
			} else {
		        if(postalCode==null){
		        	postalCode = "0";
		        }
				dispatcher.runSync("updateFacilityPostalAddress",  
						UtilMisc.toMap("userLogin", userLogin,
						"facilityId", facilityId, "fromDate",new Timestamp(new Date().getTime()),
						"address1", address1,
						"city", city, "postalCode", postalCode,"contactMechId", addressMechId));
			}
			
			//更新收货地址
			if ( UtilValidate.isEmpty(shippingAddressMechId) ) {
				//新增收货地址
				if ( !UtilValidate.isEmpty(shippingAddress) ) {
					if(postalCode==null){
			        	postalCode = "0";
			        }
			        Map<String, Object> invReqResult = dispatcher.runSync("createFacilityPostalAddress",  
							UtilMisc.toMap("userLogin", userLogin,
							"facilityId", facilityId, "fromDate",new Timestamp(new Date().getTime()),
							"address1", shippingAddress,
							"city", city, "postalCode", postalCode ));
			        if (!invReqResult.isEmpty()){
						String contactMechId = (String) invReqResult.get("contactMechId");
						dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
								"contactMechId", contactMechId, "facilityId",facilityId,
								"contactMechPurposeTypeId", "SHIPPING_LOCATION"));
					}
				}
			} else {
		        if(postalCode==null){
		        	postalCode = "0";
		        }
				dispatcher.runSync("updateFacilityPostalAddress",  
						UtilMisc.toMap("userLogin", userLogin,
						"facilityId", facilityId, "fromDate",new Timestamp(new Date().getTime()),
						"address1", shippingAddress,
						"city", city, "postalCode", postalCode,"contactMechId", shippingAddressMechId));
			}
			
			//更新传真
			if ( UtilValidate.isEmpty(faxMechId) ) {
				//新增传真
				if ( !UtilValidate.isEmpty(fax) ) {
					Map<String, Object> invReqResult = dispatcher.runSync("createFacilityTelecomNumber",  
							UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
							"contactNumber", fax,"askForName",askForName));
					if (!invReqResult.isEmpty()){
						String contactMechId = (String) invReqResult.get("contactMechId");
						dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
								"contactMechId", contactMechId, "facilityId",facilityId,
								"contactMechPurposeTypeId", "FAX_NUMBER"));
					}
				}
			} else {
				dispatcher.runSync("updateFacilityTelecomNumber",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId, 
						"fromDate",new Timestamp(new Date().getTime()),
						"contactNumber", fax,"askForName",askForName, "contactMechId", faxMechId ));
			}
			
			//更新电话号
			if ( UtilValidate.isEmpty(phoneMechId) ) {
				//新增电话号
				if ( !UtilValidate.isEmpty(phone) ) {
					Map<String, Object> invReqResult = dispatcher.runSync("createFacilityTelecomNumber",  
							UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
							"contactNumber", phone,"askForName",askForName));
					if (!invReqResult.isEmpty()){
						String contactMechId = (String) invReqResult.get("contactMechId");
						dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
								"contactMechId", contactMechId, "facilityId",facilityId,
								"contactMechPurposeTypeId", "PHONE_WORK"));
					}
				}
			} else {
				dispatcher.runSync("updateFacilityTelecomNumber",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId, 
						"fromDate",new Timestamp(new Date().getTime()),
						"contactNumber", phone,"askForName",askForName, "contactMechId", phoneMechId ));
			}
			
			//更新mobile
			if ( UtilValidate.isEmpty(mobileMechId) ) {
				//新增mobile
				if (!UtilValidate.isEmpty(mobile)){
					Map<String, Object> invReqResult = dispatcher.runSync("createFacilityTelecomNumber",  
							UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
							"contactNumber", mobile,"askForName",askForName));
					if (!invReqResult.isEmpty()){
						String contactMechId = (String) invReqResult.get("contactMechId");
						dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
								"contactMechId", contactMechId, "facilityId",facilityId,
								"contactMechPurposeTypeId", "PHONE_MOBILE"));
					}
				}
			} else {
				dispatcher.runSync("updateFacilityTelecomNumber",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId, 
						"fromDate",new Timestamp(new Date().getTime()),
						"contactNumber", mobile,"askForName",askForName, "contactMechId", mobileMechId ));
			}
			
			//更新email
			if ( UtilValidate.isEmpty(emailMechId) ) {
				//新增email
				if (!UtilValidate.isEmpty(email)){
					Map<String, Object> invReqResult = dispatcher.runSync("createFacilityEmailAddress",
							UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
							"emailAddress", email));
					if (!invReqResult.isEmpty()){
						String contactMechId = (String) invReqResult.get("contactMechId");
						dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
								"contactMechId", contactMechId, "facilityId",facilityId,
								"contactMechPurposeTypeId", "PRIMARY_EMAIL"));
					}
				}
			} else {
				dispatcher.runSync("updateFacilityEmailAddress",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId, 
						"emailAddress", email, "contactMechId", emailMechId ));
			}
			
			if (!UtilValidate.isEmpty(storeTypeId)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", oldStoreTypeId,"fromDate",oldTypeFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreGroupMember",
							UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeTypeId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			if (!UtilValidate.isEmpty(storeStyleId)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", oldStoreStyleId,"fromDate",oldStyleFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreGroupMember",
							UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeStyleId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			if (!UtilValidate.isEmpty(storeLevelId)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", oldStoreLevelId,"fromDate",oldLevelFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreGroupMember",
							UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeLevelId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			if (!UtilValidate.isEmpty(storeFeatureId)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", oldStoreFeatureId,"fromDate",oldFeatureFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreGroupMember",
							UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeFeatureId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			if (!UtilValidate.isEmpty(storeOwnerRegionId)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", oldStoreOwnerRegionId,"fromDate",oldOwnerRegionFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreGroupMember",
							UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeOwnerRegionId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			if (!UtilValidate.isEmpty(storeBusinessModelId)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", oldStoreBusinessModelId,"fromDate",oldBusinessModelFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreGroupMember",
							UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeBusinessModelId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			//更新区域负责人
			if (!UtilValidate.isEmpty(regionManager)){
				Map<String, Object> invReqResult = dispatcher.runSync("deleteProductStoreRole",
						UtilMisc.toMap("partyId", oldRegionManager,"roleTypeId", "REGION_MANAGER",
								"productStoreId",productStoreId,"fromDate", oldStoreRoleFromDate,"userLogin", userLogin));
				if (!invReqResult.isEmpty()){
					dispatcher.runSync("createProductStoreRole",
							UtilMisc.toMap("partyId", regionManager,"roleTypeId", "REGION_MANAGER",
									"productStoreId",productStoreId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
				}
			}
			resultMap = ServiceUtil.returnSuccess("修改成功");
		} catch (GenericServiceException e) {
			// TODO: handle exception
			Debug.logError("updateSaleStore:" + e.getMessage(), UpdateSaleStoreServices.class.getName());
			resultMap = ServiceUtil.returnError(e.getMessage());
			delegator.rollback();
		}
		return resultMap;
	}
	
	/**
	 * 创建门店
	 * by liujia
	 */
	public static Map<String, Object> createSaleStore(DispatchContext ctx, Map<String, ? extends Object> context) {
		Delegator delegator = ctx.getDelegator();
		Map<String, Object> resultMap = ServiceUtil.returnSuccess("新增成功!");
		LocalDispatcher dispatcher = ctx.getDispatcher();
		
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String productStoreId = (String) context.get("productStoreId");
		String facilityId = (String) context.get("facilityId");
		String storeName = (String) context.get("storeName");
		String payToPartyId = (String) context.get("payToPartyId");
		String city = (String) context.get("city");
		String address1 = (String) context.get("address1");
		String askForName = (String) context.get("askForName");
		String fax = (String) context.get("fax");
		String phone = (String) context.get("phone");
		String mobile = (String) context.get("mobile");
		String email = (String) context.get("email");
		String postalCode = (String) context.get("postalCode");
		String shippingAddress = (String) context.get("shippingAddress");
		BigDecimal area = (BigDecimal) context.get("area");
		String storeTypeId = (String) context.get("storeTypeId");
		String storeStyleId = (String) context.get("storeStyleId");
		String storeLevelId = (String) context.get("storeLevelId");
		String storeFeatureId = (String) context.get("storeFeatureId");
		String storeOwnerRegionId = (String) context.get("storeOwnerRegionId");
		String storeBusinessModelId = (String) context.get("storeBusinessModelId");
		String regionManager = (String) context.get("regionManager");
        Timestamp openedDate = null;
        if (UtilValidate.isNotEmpty(context.get("openedDate"))) {
        	openedDate = (Timestamp) context.get("openedDate");
        }
        Timestamp closedDate = null;
        if (UtilValidate.isNotEmpty(context.get("closedDate"))) {
        	closedDate = (Timestamp) context.get("closedDate");
        }
		GenericValue facility = null;
		GenericValue productStore = null;

		
		try {
			//判断id
//			if ("Company".equals(payToPartyId)) {
//				productStoreId = "R" + delegator.getNextSeqId("ProductStore");
//			} else {
//				productStoreId = "F" + delegator.getNextSeqId("ProductStore");
//			}
			//判断店铺ID是否已存在，存在则跳出不创建
			List<GenericValue> ProductStores = delegator.findByAnd("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
			if(ProductStores.size()>0){
				resultMap = ServiceUtil.returnError("门店代码已存在,请尝试更换门店代码！");
				return resultMap;
			}
			
			//新增收款账户关联
			facilityId = productStoreId;
			//创建场所实体 先创建场所后建店铺 是因为在店铺中有个inventoryFacilityId要调用场所信息
			facility = delegator.makeValue("Facility",
					UtilMisc.toMap(
							"facilityId",facilityId,"facilityName", storeName, "ownerPartyId",payToPartyId,
							"defaultInventoryItemTypeId","NON_SERIAL_INV_ITEM","description",storeName,"facilityTypeId","RETAIL_STORE",
							"ownerPartyId", payToPartyId ,"facilitySize",area,"openedDate",openedDate,"closedDate",closedDate
							));
			delegator.createOrStore(facility);
			
			//新增店铺信息
			productStore = delegator.makeValue("ProductStore",
					UtilMisc.toMap("productStoreId", productStoreId,"inventoryFacilityId",productStoreId,
							"storeName", storeName, "payToPartyId",
							payToPartyId));
			delegator.createOrStore(productStore);
			//默认把店铺放到零售组中(为了区分注销 和在其他地方显示)
			dispatcher.runSync("createProductStoreGroupMember",
					UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", "RETAIL","fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));

//			Map<String, Object> reqResult = dispatcher.runSync("createFacility", UtilMisc.toMap("userLogin", userLogin,
//					"facilityName", storeName,"facilityId",facilityId,"defaultInventoryItemTypeId","NON_SERIAL_INV_ITEM","description",storeName,
//					"facilityTypeId", "RETAIL_STORE",
//					"ownerPartyId", payToPartyId));	

			//查询门店与场所的关联，已有就不创建
			if(getResultStatus(delegator,productStoreId)){
					dispatcher.runSync("createProductStoreFacility", UtilMisc.toMap("userLogin", userLogin,
							"productStoreId", productStoreId, "facilityId",facilityId,
							"fromDate", new Timestamp(new Date().getTime())));
			}
			//创建门店支付方式 默认现金与银行卡
			dispatcher.runSync("createProductStorePaymentSetting", UtilMisc.toMap("userLogin", userLogin,
					"productStoreId", productStoreId, "paymentMethodTypeId","CASH",
					"paymentServiceTypeEnumId", "PRDS_PAY_EXTERNAL","applyToAllProducts","Y"));
			dispatcher.runSync("createProductStorePaymentSetting", UtilMisc.toMap("userLogin", userLogin,
					"productStoreId", productStoreId, "paymentMethodTypeId","CREDIT_CARD",
					"paymentServiceTypeEnumId", "PRDS_PAY_EXTERNAL","applyToAllProducts","Y"));
			//创建门店物流方式 默认no ship _NA_与EXPRESS	shunfeng		
			dispatcher.runSync("createProductStoreShipMeth", UtilMisc.toMap("userLogin", userLogin,
					"productStoreId", productStoreId,"partyId","_NA_","roleTypeId","CARRIER","shipmentMethodTypeId","NO_SHIPPING"));
			dispatcher.runSync("createProductStoreShipMeth", UtilMisc.toMap("userLogin", userLogin,
					"productStoreId", productStoreId,"partyId","SHUNFENG","roleTypeId","CARRIER","shipmentMethodTypeId","EXPRESS"));
			
			//创建party相关信息
			dispatcher.runSync("createPartyGroup", UtilMisc.toMap("userLogin", userLogin,
					"partyId", productStoreId,"groupName",storeName,"statusId","PARTY_ENABLED","preferredCurrencyUomId","CNY",
					"partyTypeId","PARTY_GROUP"));
			
			dispatcher.runSync("createPartyRole", UtilMisc.toMap("userLogin", userLogin,
					"partyId", productStoreId,"roleTypeId","BULK_CUSTOMER"));
			dispatcher.runSync("createPartyRole", UtilMisc.toMap("userLogin", userLogin,
					"partyId", productStoreId,"roleTypeId","OTHER_ORGANIZATION_U"));
			dispatcher.runSync("createPartyRole", UtilMisc.toMap("userLogin", userLogin,
					"partyId", productStoreId,"roleTypeId","REQ_REQUESTER"));
			
			dispatcher.runSync("createPartyRelationship", UtilMisc.toMap("userLogin", userLogin,
					"partyIdFrom", payToPartyId,"partyIdTo",productStoreId,"partyRelationshipTypeId","GROUP_ROLLUP","roleTypeIdFrom","PARENT_ORGANIZATION",
					"roleTypeIdTo","OTHER_ORGANIZATION_U","fromDate",new Timestamp(new Date().getTime())));

			//新增联系人电话
			if (!UtilValidate.isEmpty(phone)){
				Map<String, Object> invReqResult = dispatcher.runSync("createFacilityTelecomNumber",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
						"contactNumber", phone,"askForName",askForName));
				if (!invReqResult.isEmpty()){
					String contactMechId = (String) invReqResult.get("contactMechId");
					dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
							"contactMechId", contactMechId, "facilityId",facilityId,
							"contactMechPurposeTypeId", "PHONE_WORK"));
				}
			}
			//新增联系人FAX
			if (!UtilValidate.isEmpty(fax)){
				Map<String, Object> invReqResult = dispatcher.runSync("createFacilityTelecomNumber",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
						"contactNumber", fax,"askForName",askForName));
				if (!invReqResult.isEmpty()){
					String contactMechId = (String) invReqResult.get("contactMechId");
					dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
							"contactMechId", contactMechId, "facilityId",facilityId,
							"contactMechPurposeTypeId", "FAX_NUMBER"));
				}
			}
			//新增联系人mobile
			if (!UtilValidate.isEmpty(mobile)){
				Map<String, Object> invReqResult = dispatcher.runSync("createFacilityTelecomNumber",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
						"contactNumber", mobile,"askForName",askForName));
				if (!invReqResult.isEmpty()){
					String contactMechId = (String) invReqResult.get("contactMechId");
					dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
							"contactMechId", contactMechId, "facilityId",facilityId,
							"contactMechPurposeTypeId", "PHONE_MOBILE"));
				}
			}
			//新增联系人email
			if (!UtilValidate.isEmpty(email)){
				Map<String, Object> invReqResult = dispatcher.runSync("createFacilityEmailAddress",  
						UtilMisc.toMap("userLogin", userLogin,"facilityId", facilityId,
						"emailAddress", email));
				if (!invReqResult.isEmpty()){
					String contactMechId = (String) invReqResult.get("contactMechId");
					dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
							"contactMechId", contactMechId, "facilityId",facilityId,
							"contactMechPurposeTypeId", "PRIMARY_EMAIL"));
				}
			}
			
			//新增地址
			if ( !UtilValidate.isEmpty(address1) ) {
//				List<EntityCondition> conditionList = FastList.newInstance();
//		        conditionList.add(EntityCondition.makeCondition("cityName", city));
//		        EntityCondition cond = EntityCondition.makeCondition(conditionList);
//		        GenericValue geoProvinceAndCity = null;
//		        try {
//		        	geoProvinceAndCity = EntityUtil.getFirst(delegator.findList("GeoAssocAndGeoTo", cond, null, null, null, true));
//		        } catch (GenericEntityException e) {
//		            Debug.logError(e, module);
//		        }
//		        if (geoProvinceAndCity != null) {
//		            city = geoProvinceAndCity.getString("cityName");
//		            stateProvinceGeoId = geoProvinceAndCity.getString("provinceId");
//		        }
		        if(postalCode==null){
		        	postalCode = "0";
		        }
		        Map<String, Object> invReqResult = dispatcher.runSync("createFacilityPostalAddress",  
						UtilMisc.toMap("userLogin", userLogin,
						"facilityId", facilityId, "fromDate",new Timestamp(new Date().getTime()),
						"address1", address1,
						"city", city, "postalCode", postalCode ));
		        if (!invReqResult.isEmpty()){
					String contactMechId = (String) invReqResult.get("contactMechId");
					dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
							"contactMechId", contactMechId, "facilityId",facilityId,
							"contactMechPurposeTypeId", "PRIMARY_LOCATION"));
				}
			}
			
			//新增收货地址
			if ( !UtilValidate.isEmpty(shippingAddress) ) {
//				List<EntityCondition> conditionList = FastList.newInstance();
//		        conditionList.add(EntityCondition.makeCondition("cityName", city));
//		        EntityCondition cond = EntityCondition.makeCondition(conditionList);
//		        GenericValue geoProvinceAndCity = null;
//		        try {
//		        	geoProvinceAndCity = EntityUtil.getFirst(delegator.findList("GeoAssocAndGeoTo", cond, null, null, null, true));
//		        } catch (GenericEntityException e) {
//		            Debug.logError(e, module);
//		        }
//		        if (geoProvinceAndCity != null) {
//		            city = geoProvinceAndCity.getString("cityName");
//		            stateProvinceGeoId = geoProvinceAndCity.getString("provinceId");
//		        }
		        if(postalCode==null){
		        	postalCode = "0";
		        }
		        Map<String, Object> invReqResult = dispatcher.runSync("createFacilityPostalAddress",  
						UtilMisc.toMap("userLogin", userLogin,
						"facilityId", facilityId, "fromDate",new Timestamp(new Date().getTime()),
						"address1", shippingAddress,
						"city", city, "postalCode", postalCode ));
		        if (!invReqResult.isEmpty()){
					String contactMechId = (String) invReqResult.get("contactMechId");
					dispatcher.runSync("createFacilityContactMechPurpose", UtilMisc.toMap("userLogin", userLogin,
							"contactMechId", contactMechId, "facilityId",facilityId,
							"contactMechPurposeTypeId", "SHIPPING_LOCATION"));
				}
			}
			
			//新增店铺类型
			if (!UtilValidate.isEmpty(storeTypeId)){
				dispatcher.runSync("createProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeTypeId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}

			if (!UtilValidate.isEmpty(storeStyleId)){
				dispatcher.runSync("createProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeStyleId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}

			if (!UtilValidate.isEmpty(storeLevelId)){
				dispatcher.runSync("createProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeLevelId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}

			if (!UtilValidate.isEmpty(storeFeatureId)){
				dispatcher.runSync("createProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeFeatureId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}

			if (!UtilValidate.isEmpty(storeOwnerRegionId)){
				dispatcher.runSync("createProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeOwnerRegionId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}

			if (!UtilValidate.isEmpty(storeBusinessModelId)){
				dispatcher.runSync("createProductStoreGroupMember",
						UtilMisc.toMap("productStoreId", productStoreId,"productStoreGroupId", storeBusinessModelId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}
			
			//新增区域负责人
			if (!UtilValidate.isEmpty(regionManager)){
				dispatcher.runSync("createProductStoreRole",
						UtilMisc.toMap("partyId", regionManager,"roleTypeId", "REGION_MANAGER",
								"productStoreId",productStoreId,"fromDate",new Timestamp(new Date().getTime()),"userLogin", userLogin));
			}
		} catch (GenericServiceException e) {
			// TODO: handle exception
			Debug.logError("createSaleStore:" + e.getMessage(), UpdateSaleStoreServices.class.getName());
			resultMap = ServiceUtil.returnError(e.getMessage());
			
		} catch (GenericEntityException e) {
			Debug.logError("createSaleStore:" + e.getMessage(), UpdateSaleStoreServices.class.getName());
			resultMap = ServiceUtil.returnError(e.getMessage());
			delegator.rollback();
		}		
		return resultMap;
    }
	
	/**
	 * 更新销售订单项
	 * by liujia
	 */
	 public static Map<String, Object> updateSalesOrderItems(DispatchContext dctx, Map<String, ? extends Object> context) {
		  		Delegator delegator = dctx.getDelegator();
		  		Map<String, Object> resultMap = ServiceUtil.returnSuccess("更新成功！");
		 try {
		 		LocalDispatcher dispatcher = dctx.getDispatcher();
		      
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String orderId = (String) context.get("orderId");
		        BigDecimal quantity = (BigDecimal) context.get("quantity");
		        BigDecimal oldQuantity = (BigDecimal) context.get("oldQuantity");
		        String changeShipGroupSeqId = (String) context.get("changeShipGroupSeqId");
		        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
		        String orderItemSeqId = (String) context.get("orderItemSeqId");
		        
		        GenericValue orderItemShipGroupAssoc = null;
		        GenericValue orderItem = null;
		        //如果输入订单输入数量为0，删除订单项
		        if (quantity.intValue() == 0) {
		        	dispatcher.runSync("cancelOrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "shipGroupSeqId", shipGroupSeqId, "cancelQuantity", oldQuantity,"userLogin", userLogin));
		        } else {
		        	//如果没有改变货运组只更新数量
			        if (shipGroupSeqId.equals(changeShipGroupSeqId)) {
			        	orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "quantity", quantity));
			        	orderItemShipGroupAssoc = delegator.makeValue("OrderItemShipGroupAssoc", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "quantity", quantity, "shipGroupSeqId", shipGroupSeqId));
			        	delegator.createOrStore(orderItem);
			        	delegator.createOrStore(orderItemShipGroupAssoc);
			        	
			        } else {
			        //改变货运组更新货运组
			        	orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "quantity", quantity));
			        	orderItemShipGroupAssoc = delegator.makeValue("OrderItemShipGroupAssoc", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "quantity", quantity, "shipGroupSeqId", changeShipGroupSeqId));
			        	delegator.createOrStore(orderItem);
			        	delegator.createOrStore(orderItemShipGroupAssoc);
			        	delegator.removeByAnd("OrderItemShipGroupAssoc", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "shipGroupSeqId", shipGroupSeqId));
			        } 		
		        }
		        
		 	} catch (GenericEntityException e) {
				// TODO: handle exception
				Debug.logError("updateSalesOrderItems:" + e.getMessage(), UpdateSaleStoreServices.class.getName());
				resultMap = ServiceUtil.returnError(e.getMessage());
				delegator.rollback();				
			} catch (GenericServiceException e) {
				Debug.logError("updateSalesOrderItems:" + e.getMessage(), UpdateSaleStoreServices.class.getName());
				resultMap = ServiceUtil.returnError(e.getMessage());
				delegator.rollback();
			}
	        return resultMap;
	 }

	/**
	 * 取对应的门店场所数量
	 * by liujia
	 */
	public static Boolean getResultStatus(Delegator delegator, String productStoreId) {
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
	
            sql = "select count(*) from product_store_facility psf "+
            		"where psf.PRODUCT_STORE_ID = '"+ productStoreId +"'";
            rs = stmt.executeQuery(sql);
            String str = "";
            if(rs.next()) {
            	str = (String)rs.getString(1);
            }
            rs.close();
            if(str.equals("0")){
            	return true;
            }
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
		return false;
	}

	/**
	 * 更新报价数量
	 * by liujia
	 */
	public static Map<String, Object> updateQuoteQuantityAjax(DispatchContext dctx, Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> resultMap = ServiceUtil.returnError("");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		
		String quoteId = (String) context.get("quoteId");
		String quoteItemSeqId = (String) context.get("quoteItemSeqId");
        BigDecimal quantity = new BigDecimal((String)context.get("quantity"));
        //String quantity = b1.toString();
        try {
			dispatcher.runSync("updateQuoteItem", UtilMisc.toMap("userLogin", userLogin,
					"quoteId",quoteId,"quoteItemSeqId", quoteItemSeqId, "quantity",quantity));
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String successMsg = "successfully";
        return ServiceUtil.returnSuccess(successMsg);
	}
	
	/**
	 * 检查营业执照是否有重复
	 * by liujia
	 */
	public static Map<String, Object> checkNewBusiness(Delegator delegator,String idValue) {
    	Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		String str = "";
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
	
            sql = "select count(*) from party_identification pi "+
            		"where pi.ID_VALUE = '"+ idValue +"'";
            rs = stmt.executeQuery(sql);
           
            if(rs.next()) {
            	str = (String)rs.getString(1);
            }
            rs.close();
            if(!str.equals("0")){
                return ServiceUtil.returnError("营业执照不能有重复");
            }
		}catch (SQLException e) {
			Debug.logWarning(e, "营业执照重复", module);
        }finally {
                try {
                    if (stmt != null) stmt.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "营业执照重复", module);
                }
                try {
                    if (connection != null) connection.close();
                } catch (SQLException sqle) {
                    Debug.logWarning(sqle, "营业执照重复", module);
                }
        }
        return ServiceUtil.returnSuccess();
    }

	/**
	 * 注销产品店铺 删除该店铺关联产品目录
	 * by liujia
	 */
	public static String cancelStoreGroup(HttpServletRequest request, HttpServletResponse response){
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String productStoreId = request.getParameter("productStoreId");	//店铺ID
		try {
			List<GenericValue> productStoreCatalogs = delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId", productStoreId));
			if(productStoreCatalogs.size()>0){
				for(int i=0;i<productStoreCatalogs.size();i++){
					dispatcher.runSync("deleteProductStoreCatalog", UtilMisc.toMap("prodCatalogId", productStoreCatalogs.get(i).get("prodCatalogId"), "productStoreId", productStoreId, "fromDate", productStoreCatalogs.get(i).get("fromDate"),"userLogin", userLogin));
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","注销店铺产品目录失败");
			return "error";
		} catch (GenericServiceException e) {
			e.printStackTrace();
			request.setAttribute("_ERROR_MESSAGE_","注销店铺产品目录失败");
			return "error";
		}
		request.setAttribute("_EVENT_MESSAGE_","注销成功");
		return "success";
	}
}