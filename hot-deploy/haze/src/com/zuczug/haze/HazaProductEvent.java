package com.zuczug.haze;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import com.zuczug.product.ZuczugProductUtils;


/**
 * @author Sven Wong
 * @version 创建时间：2016年1月15日 下午3:39:16
 * @description：
 */

public class HazaProductEvent {
	public static final String module = HazaProductEvent.class.getName();
    public static final String resource = "ProductErrorUiLabels";
    
    public static String applyProductFeatures(HttpServletRequest request, HttpServletResponse response){
    	 Delegator delegator = (Delegator) request.getAttribute("delegator");
         LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
         HttpSession session = request.getSession();
         GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
         String productId = request.getParameter("productId");
         try {
			GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
			String productCategoryId = product.getString("primaryProductCategoryId");
			 
			 // 获取这个productCategoryId的所有productFeatureGroup
			List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
				// GenericValue productFeatureGroup = productFeatureCatGrpAppl.getRelatedOne("ProductFeatureGroup");
				String productFeatureId = request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
				if(UtilValidate.isNotEmpty(productFeatureId)){
					// 检查这个productFeatureGroup是否已经存在于这个product
					List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByGroup(delegator, productId, productFeatureCatGrpAppl.getString("productFeatureGroupId"));
					if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
						for (GenericValue productFeatureAppl : productFeatureAppls) {
							dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));
						}
					}
					dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
				}
			}
			
			for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
				String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
				if(UtilValidate.isNotEmpty(productFeatureCategoryId)){
					List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, productId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
			    	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
			    		for (GenericValue productFeatureAppl : productFeatureAppls) {
			    			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));	        			
			    		}
			    	}
			    	//不等于NONE的，就添加
			    	if (!productFeatureCategoryId.equals("NONE")) {
				    	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
					}
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
     * 为一个虚拟商品创建变型
     * @author sven
     * @param request
     * @param response
     * @return
     * @throws GenericTransactionException
     */
    public static String createVariantFromVirtual(HttpServletRequest request, HttpServletResponse response) throws GenericTransactionException{
   	 	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String productId = request.getParameter("productId");
        String newproductId = request.getParameter("newproductId");
        
        try {
        	TransactionUtil.begin();
			GenericValue product = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
			GenericValue newProduct = delegator.findOne("Product", false, UtilMisc.toMap("productId",newproductId));
			
			if(UtilValidate.isNotEmpty(newProduct)){
				request.setAttribute("_ERROR_MESSAGE_", "编号"+newproductId+"已存在");
				return "error";
			}
			Map<String,Object> inMap = FastMap.newInstance();
			inMap.put("oldProductId", product.getString("productId"));
			inMap.put("productId", newproductId);
			inMap.put("duplicateAttributes", "Y");
			inMap.put("duplicateCategoryMembers", "Y");
			inMap.put("duplicateFeatureAppls", "Y");
			inMap.put("duplicateIDs", "Y");
			inMap.put("duplicatePrices", "Y");
			inMap.put("newDescription", product.getString("description"));
			inMap.put("newInternalName", product.getString("internalName"));
			inMap.put("newLongDescription", product.getString("longDescription"));
			inMap.put("newProductName", product.getString("productName"));
			inMap.put("userLogin", userLogin);
			dispatcher.runSync("duplicateProduct", inMap);
			
			newProduct = delegator.findOne("Product", false, UtilMisc.toMap("productId",newproductId));
			newProduct.set("isVariant", "Y");
			newProduct.set("isVirtual", "N");
			newProduct.store();
			String productCategoryId = newProduct.getString("primaryProductCategoryId");
			
			List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
				String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
				if(UtilValidate.isNotEmpty(productFeatureCategoryId)){
					List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, newproductId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
			    	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
			    		for (GenericValue productFeatureAppl : productFeatureAppls) {
			    			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", newproductId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));	        			
			    		}
			    	}
			    	//不等于NONE的，就添加
			    	if (!productFeatureCategoryId.equals("NONE")) {
				    	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", newproductId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
					}
				}
			}
			
			dispatcher.runSync("createProductAssoc", UtilMisc.toMap("productId",productId,"productIdTo",newproductId,"productAssocTypeId","PRODUCT_VARIANT","fromDate",UtilDateTime.nowTimestamp(),"userLogin",userLogin));
			TransactionUtil.commit();
			
			request.setAttribute("_EVENT_MESSAGE_", "变型成功");
        } catch (GenericEntityException e) {
			e.printStackTrace();
			TransactionUtil.rollback();
		} catch (GenericServiceException e) {
			e.printStackTrace();
			TransactionUtil.rollback();
		} catch(Exception e){
			e.printStackTrace();
			TransactionUtil.rollback();
		}
        
        return "success";
    }
}
