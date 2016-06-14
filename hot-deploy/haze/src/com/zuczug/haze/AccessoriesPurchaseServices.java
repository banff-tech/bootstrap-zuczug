package com.zuczug.haze;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;


import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.CheckOutEvents;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.ServiceValidationException;

import com.zuczug.product.ZuczugProductUtils;


public class AccessoriesPurchaseServices {
    
	/**
	 * 更具虚拟商品创建变型商品
	 * @author jiaqi.Dai
	 * @param request
	 * @param response
	 * @return
	 */
	public static String createVariantProductByVirtualProductToBom(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        DispatchContext dctx = dispatcher.getDispatchContext();
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();        
        String productId = request.getParameter("productId");
        String productCategoryId = request.getParameter("productCategoryId");
        String comments = request.getParameter("comments");
        String newVariantProductId = "";
        List<String> vartualProductFeatures = new ArrayList<String>();
        String zuczugId = "";
        Map<String, Object> result = ServiceUtil.returnSuccess();
    	try {
	        if (UtilValidate.isNotEmpty(productId)) {
	        	List<GenericValue> productFeatures = delegator.findByAnd("ProductFeatureAppl", 
						UtilMisc.toMap("productId", productId,"productFeatureApplTypeId", "STANDARD_FEATURE"));
	        	
	        	GenericValue virtualProduct;			
				virtualProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
				GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId",productCategoryId));
				newVariantProductId = delegator.getNextSeqId("Product");
				// 获取这个productCategoryId的所有productFeatureGroup
		        List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
		        List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
		        
				if (UtilValidate.isNotEmpty(productCategory.get("description"))) {
					zuczugId = productCategory.get("description") + "-" + delegator.getNextSeqId("Product");
		        } else {
		        	zuczugId = delegator.getNextSeqId("Product");
		        }
				
				for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
		        	String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
		        	if ( "NONE".equals(productFeatureCategoryId)) {
		        		String errorMsg = "转换变形商品必须选择可变特征！";
		        		request.setAttribute("_ERROR_MESSAGE_", errorMsg);
		        		return "error";
		        	}
		        	vartualProductFeatures.add(productFeatureCategoryId);
				}
				
				for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {			        	
		        	vartualProductFeatures.add(request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId")));
				}
				result = dispatcher.runSync("checkProductRepeatByFeature", UtilMisc.toMap("productFeatures", vartualProductFeatures, "productId", productId));
				//去重
				if(!result.get("checkProduct").equals("noRepeate")) {
					newVariantProductId = (String) result.get("checkProduct");
					
					GenericValue good = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("productId",newVariantProductId, "goodIdentificationTypeId","ZUCZUG_CODE"));
					String errorMsg = "希望转换的变形商品已存在,编号为:" + good.get("idValue");
	        		request.setAttribute("_ERROR_MESSAGE_", errorMsg);
					return "error";
				} else {
					//按照虚拟商品复制一个商品
					dispatcher.runSync("duplicateProduct", UtilMisc.toMap("userLogin", userLogin, "oldProductId", productId, 
							"productId", newVariantProductId, "newProductName", virtualProduct.get("productName"),
							"newInternalName", virtualProduct.get("internalName"), "duplicateFeatureAppls", "Y"));
					
					dispatcher.runSync("createOrUpdateGoodIdentification", UtilMisc.toMap("productId", newVariantProductId, "idValue", zuczugId, "goodIdentificationTypeId", "ZUCZUG_CODE","userLogin",userLogin));
					
					//查询虚拟商品的供应商信息
					List<GenericValue> supplierProducts = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId", productId));
					//循环添加给 新的变型商品
					for (GenericValue supplierProduct: supplierProducts) {
						ModelService model = dctx.getModelService("createSupplierProduct");
						Map<String, Object> invokeCtx = model.makeValid(supplierProduct, ModelService.IN_PARAM);
						invokeCtx.put("productId", newVariantProductId);
						invokeCtx.put("userLogin", userLogin);
						dispatcher.runSync("createSupplierProduct", invokeCtx);
					}
					GenericValue variantProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", newVariantProductId));
					//设定为变型商品
					variantProduct.set("isVariant", "Y");
					variantProduct.set("isVirtual", "N"); 
					String description = "";
					
					for (GenericValue productFeature : productFeatures) {
						if (!productFeature.get("productFeatureId").equals("ACCESSORIES")) {
							dispatcher.runSync("removeFeatureFromProduct",UtilMisc.toMap(
									"productFeatureId",productFeature.get("productFeatureId"),
									"productId",newVariantProductId,
									"fromDate", productFeature.getTimestamp("fromDate"),
									"userLogin", userLogin
									));
						}						
					}
					
			        for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
			        	String productFeatureId = request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
			        	if (UtilValidate.isNotEmpty(productFeatureId)) {
			        		dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", newVariantProductId, "productFeatureId", productFeatureId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
				        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureId));
			        		GenericValue ProductFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrpAppl.getString("productFeatureGroupId")));
			        		description = description + ProductFeatureGroup.get("description") + ":" + productFeature.get("description") + " ";
			        	}			       	
			        }
			        
			        for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
			        	String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
			        	if (!productFeatureCategoryId.equals("NONE")) {
			        		List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, productId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
				        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
				        		for (GenericValue productFeatureAppl : productFeatureAppls) {
				        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));	        			
				        		}
				        	}
				        	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", newVariantProductId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
				        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureCategoryId));
			        		GenericValue ProductFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.getString("productFeatureCategoryId")));
			        		description = description + ProductFeatureCategory.get("description") + ":" + productFeature.get("description") + " ";
			        	}
			        }
			        
			        variantProduct.set("description", description);
			        variantProduct.set("comments", comments);
					delegator.store(variantProduct);

					//添加关联
					dispatcher.runSync("createProductAssoc", UtilMisc.toMap(
							"userLogin", userLogin,
							"productAssocTypeId", "PRODUCT_VARIANT", 
							"productId", productId, 
							"productIdTo", newVariantProductId, 
							"fromDate", UtilDateTime.nowTimestamp()));
				}
				List<GenericValue> productBomAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", productId, "productAssocTypeId", "ENGINEER_COMPONENT"));
				for (GenericValue productBomAssoc : productBomAssocs) {
					if (UtilValidate.isEmpty(productBomAssoc.get("thruDate"))) {
						dispatcher.runSync("createProductAssoc", UtilMisc.toMap(
								"productAssocTypeId", "ENGINEER_COMPONENT",
								"productId", productBomAssoc.get("productId"),
								"productIdTo", newVariantProductId,
								"fromDate", UtilDateTime.nowTimestamp(),
								"userLogin", userLogin
								));
						dispatcher.runSync("updateProductAssoc", UtilMisc.toMap(
								"productId", productBomAssoc.get("productId"),
								"productIdTo", productBomAssoc.get("productIdTo"),
								"productAssocTypeId", productBomAssoc.get("productAssocTypeId"),
								"fromDate", productBomAssoc.get("fromDate"),
								"thruDate", UtilDateTime.nowTimestamp(),
								"userLogin", userLogin
								));
					}
				}
	        }

    	} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return newVariantProductId;    	
    }
    
    /**
     * 为虚拟商品和变型商品之间的关联关系 设定失效时间 也就是删除关联关系
     * @author jiaqi.Dai
     * @param request
     * @param response
     * @return
     */
    public static String deleteProductAssoc(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String productId = request.getParameter("productIdTo");
        String virtualProductId = request.getParameter("virtualProductId");
        Timestamp fromDate = Timestamp.valueOf(request.getParameter("fromDate"));
        
        String productCategoryId = request.getParameter("productCategoryId");
        String productAssocTypeId = "PRODUCT_VARIANT";
        Boolean flag = true;        
		try {
			//按照分类查询可变特征组
			List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			//查询虚拟商品的所有变形商品
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", productAssocTypeId));
			productAssocs = EntityUtil.filterByDate(productAssocs);
			//循环特征组
			for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
				flag = true;
				//可变特征组ID
				String productFeatureCategoryId = productFeatureCategoryAppl.getString("productFeatureCategoryId");	   
				//获得变形商品可变特征类
				GenericValue productFeature = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureCategoryId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE")));
				//获得虚拟商品的其他变形商品
				for (GenericValue productAssoc : productAssocs) {
					if (!productAssoc.get("productIdTo").equals(productId)) {
						//查询其他变形商品的可变特征
						GenericValue tempProductFeature = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productAssoc.get("productIdTo"),"productFeatureCategoryId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE")));
						if (UtilValidate.isNotEmpty(tempProductFeature)&&UtilValidate.isNotEmpty(productFeature)&&tempProductFeature.get("productFeatureId").equals(productFeature.get("productFeatureId"))) {
							//存在相同不能删除
							flag = false;
							break;
						}
					}	       		
	        	}
				
				if (flag&&UtilValidate.isNotEmpty(productFeature)) {
					GenericValue pf = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", virtualProductId, "productFeatureId", productFeature.get("productFeatureId"))));
					dispatcher.runSync("removeFeatureFromProduct",UtilMisc.toMap(
							"productFeatureId", pf.get("productFeatureId"),
							"productId",virtualProductId,
							"fromDate", pf.get("fromDate"),
							"userLogin", userLogin
							));
				}
				
			}
			
			dispatcher.runSync("updateProductAssoc", UtilMisc.toMap(
					"productId", virtualProductId,
					"productIdTo", productId,
					"productAssocTypeId", productAssocTypeId,
					"fromDate", fromDate,
					"thruDate", UtilDateTime.nowTimestamp(),
					"userLogin", userLogin));
			
			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
			product.set("productTypeId", "CANCELED");
			Map<String, Object> parameters = product.getAllFields();
			parameters.remove("lastUpdatedTxStamp");
			parameters.remove("lastUpdatedStamp");
			parameters.remove("lastModifiedDate");
			parameters.remove("lastModifiedByUserLogin");
			parameters.remove("createdTxStamp");
			parameters.remove("createdStamp");
			parameters.remove("createdByUserLogin");
			parameters.remove("lastUpdatedTxStamp");
			parameters.remove("lastUpdatedStamp");
			parameters.remove("lastModifiedDate");
			parameters.remove("lastModifiedByUserLogin");
			parameters.remove("createdTxStamp");
			parameters.remove("createdStamp");
			parameters.remove("createdDate");
			
			parameters.put("userLogin", userLogin);
			
			dispatcher.runSync("updateProduct", parameters);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return productAssocTypeId;        
    }
    
    public static String changeToQuote(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String requirementId = request.getParameter("requirementId");
        
        try {
			dispatcher.runAsync("createQuoteFromCustRequest", UtilMisc.toMap("custRequestId", requirementId, "quoteTypeId", "PURCHASE_QUOTE","userLogin", userLogin));
		} catch (ServiceAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "success";
    	
    }
    
    public static String createOrder(HttpServletRequest request, HttpServletResponse response) {
    	ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
		cart.setOrderAttribute("FABRIC_PURCHASE_TYPE", "ACCESSORY_PURCHASE");
    	String requestString = CheckOutEvents.createOrder(request, response);    	
		return requestString;
    	
    }

    public static Map<String, Object> findRepeatFeatureAccessory(DispatchContext dctx, Map<String, Object> context) {
    	Delegator delegator = dctx.getDelegator();
    	LocalDispatcher dispatcher = dctx.getDispatcher();
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
        List<String> productFeatures = new ArrayList<String>();
        Map parameters = (Map) context.get("parameters");
        FastList productCategoryId = (FastList) parameters.get("productCategoryId");
        Map<String, Object> results = ServiceUtil.returnSuccess();
     // 获取这个productCategoryId的所有productFeatureGroup
        List<GenericValue> productFeatureCatGrpAppls;
		try {
			if (UtilValidate.isNotEmpty(productCategoryId)) {
				productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId.get(0)));
			
		        List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId.get(0)));
		        for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
		        	String productFeatureCatGrp = (String)parameters.get(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		        	if (!productFeatureCatGrp.equals("NONE")) {
		        		productFeatures.add(productFeatureCatGrp);
		        	}        	
		        }
		       
		        
		        if (UtilValidate.isNotEmpty(productCategoryId.get(0))) {
		        	Map<String, Object> result = dispatcher.runSync("checkProductFeatureRepeatByCatagory", UtilMisc.toMap("productCategoryId",productCategoryId.get(0) ,"productFeatures",productFeatures,"productTypeId","RAW_MATERIAL" ));
		        	results.put("listIt", result.get("checkProduct"));
		        } else {
		        	results.put("listIt", new ArrayList());
		        }
			} else {
				results.put("listIt", new ArrayList());
			}

	        return results;       
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> result = ServiceUtil.returnError("程序出错");
		return userLogin;
    }
}
