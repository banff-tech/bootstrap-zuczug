package com.zuczug.product;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.minilang.method.entityops.FindByAnd;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class ProductEvents {

    public static final String module = ProductEvents.class.getName();
    public static final String resource = "ProductErrorUiLabels";
    
    /**
     * 创建虚拟或非虚拟非变型商品并设置用户选择的特征
     * @author jiaqi.Dai
     * @param request
     * @param productCategoryId 分类ID	
     * @param internalName 商品内部名称
     * @param productTypeId 商品类型
     * @param productId 商品ID
     * @param toVirtual 是否需要创建虚拟商品
     * @param virtualProductId 商品的虚拟商品Id
     * @param zuczugId 素然物料编号
     * @param updateMode 执行模式 更新 或 创建
     * @param comments 备注
     * @param isNoVirtualNoVarient 是否需要创建非虚拟非变形 （为了设计商品）
     * @param response
     * @return String
     */
    public static String editVirtualProductWithAllFeature(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        
        String errMsg=null;
        
        String updateMode = request.getParameter("updateMode");
        if (UtilValidate.isEmpty(updateMode)) {
        	return "success";
        }
        String productCategoryId = request.getParameter("productCategoryId");
        String internalName = request.getParameter("internalName");
        String productTypeId = request.getParameter("productTypeId");
        String productId = delegator.getNextSeqId("Product");
        String toVirtual = request.getParameter("toVirtual");
        String virtualProductId = request.getParameter("virtualProductId");
        String zuczugId = request.getParameter("productZuczugId");
        String comments = request.getParameter("comments");
        String isNoVirtualNoVarient = request.getParameter("isNoVirtualNoVarient");
        String message = "";
        String isCheckFeatureCount = request.getParameter("isCheckFeatureCount");
        String description = "";
        Pattern p = Pattern.compile("[0-9a-zA-Z-_]*");
        Matcher m = p.matcher(zuczugId);
        boolean b = m.matches();
        if (!b) {
        	request.setAttribute("_ERROR_MESSAGE_", "不可输入除“-”或“_”外的符号和中文字符");
    		return "error";
        }
        boolean noVirtualNoVarient = true;
        if (UtilValidate.isEmpty(internalName)) {
        	internalName = "_NA_";
        }
        try {
        	if(UtilValidate.isEmpty(zuczugId)) {
	        	GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
	        	zuczugId = ((String) productCategory.get("description")) + "-" + productId;
	        } else {
	        	List<GenericValue> goodIdValue =  delegator.findByAnd("GoodIdentification", UtilMisc.toMap("idValue",zuczugId, "goodIdentificationTypeId", "ZUCZUG_CODE"));
	    		if (UtilValidate.isNotEmpty(goodIdValue)) {
	        		request.setAttribute("_ERROR_MESSAGE_", "素然物料编号重复，请重新输入！");
	        		return "error";
	        	}
	        }
	        if ("CREATE".equals(updateMode)) { // 创建一个新产品
	        	Map result = null;
	        	if("Y".equals(toVirtual)) {//创建虚拟商品
	        		result = dispatcher.runSync("createProduct", UtilMisc.toMap("productId", productId, "internalName", internalName, "productTypeId", productTypeId, "primaryProductCategoryId", productCategoryId,"comments",comments, "isVirtual", "Y", "widthUomId", "LEN_cm", "userLogin", userLogin));
	        	}else {
	        		result = dispatcher.runSync("createProduct", UtilMisc.toMap("productId", productId, "internalName", internalName, "productTypeId", productTypeId, "primaryProductCategoryId", productCategoryId,"comments",comments, "widthUomId", "LEN_cm","userLogin", userLogin));
	        	}	   	        	
	        	if (UtilValidate.isNotEmpty(result.get("productId"))) {
	        		productId = (String) result.get("productId");
	        	}
	        	if (UtilValidate.isNotEmpty(virtualProductId)) {
	        		//设定关联
	        		dispatcher.runSync("createProductAssoc", UtilMisc.toMap("productId", virtualProductId , "productIdTo", productId, "productAssocTypeId", "PRODUCT_VARIANT", "fromDate", UtilDateTime.nowTimestamp(), "userLogin", userLogin));
	        	}
	        	dispatcher.runSync("addProductToCategory", UtilMisc.toMap(
	        			"productId", productId,
	        			"productCategoryId", productCategoryId,
	        			"fromDate", nowTimestamp,
	        			"userLogin",userLogin
	        			));
	        }	
	        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId",productId));
	        
	        dispatcher.runSync("createOrUpdateGoodIdentification", UtilMisc.toMap("productId", productId, "idValue", zuczugId, "goodIdentificationTypeId", "ZUCZUG_CODE","userLogin",userLogin));

	        // 获取这个productCategoryId的所有productFeatureGroup
	        List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
	        List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
	        for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
	        	// GenericValue productFeatureGroup = productFeatureCatGrpAppl.getRelatedOne("ProductFeatureGroup");
	        	String productFeatureId = request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
	        	if (!productFeatureId.equals("NONE")) {
	        		// 检查这个productFeatureGroup是否已经存在于这个product
		        	List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByGroup(delegator, productId, productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
		        		for (GenericValue productFeatureAppl : productFeatureAppls) {
		        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));
		        		}
		        	}
		        	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
		        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureId));
	        		GenericValue ProductFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrpAppl.getString("productFeatureGroupId")));
	        		description = description + ProductFeatureGroup.get("description") + ":" + productFeature.get("description") + " ";
	        	}   	
	        }
	        int count = productFeatureCategoryAppls.size();
	        int index = 0;
	        for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
	        	String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
	        	if (!productFeatureCategoryId.equals("NONE")) {
	        		List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, productId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
		        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
		        		for (GenericValue productFeatureAppl : productFeatureAppls) {
		        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));	        			
		        		}
		        	}
		        	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
		        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureCategoryId));
	        		GenericValue ProductFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.getString("productFeatureCategoryId")));
	        		description = description + ProductFeatureCategory.get("description") + ":" + productFeature.get("description") + " ";
	        		index++;
	        	}  else {
	        		noVirtualNoVarient = false;	        		
	        	}
	        }
	        if (UtilValidate.isEmpty(isCheckFeatureCount)) {
	        	if (index > 0 && index != count) {
		        	request.setAttribute("_ERROR_MESSAGE_", "可变特征没有选择完全，如想创建虚拟物料请勿选择可变特征");
	        		return "error";
		        }
	        }
	        
	        product.set("description", description);
	        if (UtilValidate.isEmpty(isNoVirtualNoVarient)) {
	        	if(UtilValidate.isEmpty(productFeatureCategoryAppls)) {
		        	product.set("isVirtual", "N");
		        	product.set("isVariant", "N");
		        } else {
		        	if(noVirtualNoVarient) {		        		
			        	product.set("isVirtual", "N");
			        	product.set("isVariant", "N");
			        }
		        }
	        } else {
	        	product.set("productTypeId", "PROPOSED_GOOD");
	        }
	        product.set("comments",comments);
	        delegator.store(product);
	        message = "新增成功，编号为:" + zuczugId;
	        request.setAttribute("isVirtual", product.get("isVirtual"));
	        request.setAttribute("isVariant", product.get("isVariant"));
        } catch (GenericEntityException e) {
        	e.printStackTrace();
        	return "error";
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		}
        request.setAttribute("productId", productId);
        request.setAttribute("idValue", zuczugId);
        
        request.setAttribute("_EVENT_MESSAGE_", message);
        return "success";
    }
    
    
    /**
     * Updates/adds keywords for all products
     * 创建或更新一个Product 并设定特征
     * @author jiaqi.Dai
     * @param request HTTPRequest object for the current request
     * @param response HTTPResponse object for the current request
     * @param productCategoryId 分类ID
     * @param internalName 商品内部名称
     * @param productTypeId 商品类型
     * @param productId 商品ID
     * @param zuczugId 素然物料编号
     * @param comments 备注
     * @param updateMode 运行模式 UPDATE 或 CREATE
     * @return String specifying the exit status of this event
     */
    public static String editProduct(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

        String errMsg=null;
        
        String updateMode = request.getParameter("updateMode");
        if (UtilValidate.isEmpty(updateMode)) {
        	return "success";
        }
        String productCategoryId = request.getParameter("productCategoryId");
        String internalName = request.getParameter("internalName");
        String productTypeId = request.getParameter("productTypeId");
        String productId = request.getParameter("productId");
        String zuczugId = request.getParameter("zuczugId");
        String comments = request.getParameter("comments");
        String description = "";        
        try {        		
	        if ("CREATE".equals(updateMode)) { // 创建一个新产品
	        	Map result = dispatcher.runSync("createProduct", UtilMisc.toMap("productId", productId, "internalName", internalName, "productTypeId", productTypeId, "primaryProductCategoryId", productCategoryId, "userLogin", userLogin));
	        	productId = (String) result.get("productId");
	        	List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
		        for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
		        	String productFeatureId = request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		        	// 检查这个productFeatureGroup是否已经存在于这个product
		        	List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByGroup(delegator, productId, productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
		        		for (GenericValue productFeatureAppl : productFeatureAppls) {
		        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin),0,true);
		        		}
		        	}
		        	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin),0,true);
		        }
	        }
	        
	        if(UtilValidate.isEmpty(zuczugId)) {
	        	GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
	        	zuczugId = ((String) productCategory.get("description")) + "-" + productId;
	        	
	        }
	        List<GenericValue> productGoodIdValue =  delegator.findByAnd("GoodIdentification", UtilMisc.toMap("productId", productId, "goodIdentificationTypeId", "ZUCZUG_CODE"));
	        GenericValue productGoodId = EntityUtil.getFirst(productGoodIdValue);
	        List<GenericValue> goodIdValue =  delegator.findByAnd("GoodIdentification", UtilMisc.toMap("idValue",zuczugId, "goodIdentificationTypeId", "ZUCZUG_CODE"));
        	if (!productGoodId.get("idValue").equals(zuczugId)) {
        		if (UtilValidate.isEmpty(goodIdValue)) {
            		dispatcher.runSync("createOrUpdateGoodIdentification", UtilMisc.toMap("productId", productId, "idValue", zuczugId, "goodIdentificationTypeId", "ZUCZUG_CODE","userLogin",userLogin));
            	} else {
            		request.setAttribute("_ERROR_MESSAGE_", "素然物料编号重复，请重新输入！");
            		return "error";
            	}
        	}
	        
	        
	        // 获取这个productCategoryId的所有productFeatureGroup
	        //查询所有选择特征
	        List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
	      
        	List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
        	for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
	        	String productFeatureCatGrp = (String) request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));	        	
	        	List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByGroup(delegator, productId, productFeatureCatGrpAppl.getString("productFeatureGroupId"));
	        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
	        		for (GenericValue productFeatureAppl : productFeatureAppls) {
	        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));
	        		}
	        	}
	        	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureCatGrp, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
	        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureCatGrp));
	        	GenericValue ProductFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrpAppl.getString("productFeatureGroupId")));
	        	if (UtilValidate.isNotEmpty(ProductFeatureGroup) && UtilValidate.isNotEmpty(productFeature)) {
	        		description = description + ProductFeatureGroup.get("description") + ":" + productFeature.get("description") + " ";	        		        	        	
	        	}
	        }
	        for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {       
	        	String productFeatureId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
	        	List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, productId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
	        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
	        		for (GenericValue productFeatureAppl : productFeatureAppls) {
	        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin),0,true);
	        		}
	        	}
	        	dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin),0,true);
	        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureId));
        		GenericValue ProductFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.getString("productFeatureCategoryId")));
        		if (UtilValidate.isNotEmpty(ProductFeatureCategory) && UtilValidate.isNotEmpty(productFeature)) {
        			description = description + ProductFeatureCategory.get("description") + ":" + productFeature.get("description") + " ";	        
        		}
	        }
	        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
	        if (UtilValidate.isNotEmpty(comments)) {	        	
		        product.set("comments", comments);		        
	        }
	        product.set("description", description);
	        delegator.store(product);
        } catch (GenericEntityException e) {
        	e.printStackTrace();
        	return "error";
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		}

        
        return "success";
    }
    
    /**
     * 为一个分类创建一个标准特征组
     * @author jiaqi.Dai
     * @param request
     * @param productCategoryId 分类ID
     * @param productFeatureGroupId 特征组Id
     * @param productFeatureTypeId 特征类型
     * @param description 特征组描述
     * @param productFeatureDescs 特征值字符串
     * @param response
     * @return
     */
    public static String createProductFeatureCatGrpAppl(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        
        String productCategoryId = request.getParameter("productCategoryId");
        String description = request.getParameter("description");
        String productFeatureTypeId = request.getParameter("productFeatureTypeId");
        String productFeatureDescStr = request.getParameter("productFeatureDescs");
        StringTokenizer descToken = new StringTokenizer(productFeatureDescStr, "\n\r");
        
        try {
        	List<GenericValue> productFeatureCatGrps = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
        	for(GenericValue productFeatureCatGrp : productFeatureCatGrps) {
        		GenericValue catFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrp.get("productFeatureGroupId")));
        		if (UtilValidate.isNotEmpty(catFeatureGroup) && catFeatureGroup.get("description").equals(description) == true) {
        			request.setAttribute("_ERROR_MESSAGE_", "分类中包含此特征组！");
            		return "error";
        		}
        	}
        	
        	Map result = dispatcher.runSync("createProductFeatureGroup", UtilMisc.toMap("description", description, "userLogin", userLogin));
        	String productFeatureGroupId = (String) result.get("productFeatureGroupId");
        	dispatcher.runSync("createProductFeatureCatGrpAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId, "productCategoryId", productCategoryId, "userLogin", userLogin));
        	while (descToken.hasMoreTokens()) {
        		String productFeatureDesc = descToken.nextToken();
        		if (productFeatureDesc.contains(" ")) {
        			String productFeatureDescription = productFeatureDesc.substring(0, productFeatureDesc.indexOf(" "));
            		String productFeatureIdCode = productFeatureDesc.substring(productFeatureDesc.indexOf(" ") + 1, productFeatureDesc.length());
            		List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDescription, "idCode", productFeatureIdCode, "productFeatureTypeId", productFeatureTypeId));
    	        	if (UtilValidate.isEmpty(productFeatures)) {
    	        		result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDescription, "idCode", productFeatureIdCode, "productFeatureTypeId", productFeatureTypeId, "userLogin", userLogin));
    	        	}            		
        		} else {
    	        	result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "userLogin", userLogin));    	   
        		}
        			
	        	String productFeatureId = (String) result.get("productFeatureId");
	        	dispatcher.runSync("createProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId, "productFeatureId", productFeatureId, "userLogin", userLogin));
        	}
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return "success";
    }
    
    /**
     * 为一个分类创建一个可选特征组 特征值按照回车符分隔一次添加多个
     * @author jiaqi.Dai
     * @param request
     * @param productCategoryId 分类ID
     * @param productFeatureGroupId 特征组Id
     * @param productFeatureTypeId 特征类型
     * @param description 特征组描述
     * @param productFeatureDescs 特征值字符串
     * @param response
     * @return
     */
    public static String createProductFeatureCategory(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        
        String productCategoryId = request.getParameter("productCategoryId");
        String description = request.getParameter("description");
        String productFeatureTypeId = request.getParameter("productFeatureTypeId");
        String productFeatureDescStr = request.getParameter("productFeatureDescs");
        StringTokenizer descToken = new StringTokenizer(productFeatureDescStr, "\n\r");
        
        try {
        	List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
        	for(GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
        		GenericValue productFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.get("productFeatureCategoryId")));
        		if (UtilValidate.isNotEmpty(productFeatureCategory) && productFeatureCategory.get("description").equals(description) == true) {
        			request.setAttribute("_ERROR_MESSAGE_", "分类中包含此特征组！");
            		return "error";
        		}
        	}
        	
        	Map result = dispatcher.runSync("createProductFeatureCategory", UtilMisc.toMap("description", description, "userLogin", userLogin));
        	String productFeatureCategoryId = (String) result.get("productFeatureCategoryId");
        	dispatcher.runSync("createProductFeatureCategoryAppl", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryId, "productCategoryId", productCategoryId, "userLogin", userLogin));
        	while (descToken.hasMoreTokens()) {
        		String productFeatureDesc = descToken.nextToken();
        		if (productFeatureDesc.contains(" ")) {
        			String productFeatureDescription = productFeatureDesc.substring(0, productFeatureDesc.indexOf(" "));
            		String productFeatureIdCode = productFeatureDesc.substring(productFeatureDesc.indexOf(" ") + 1, productFeatureDesc.length());
            		List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId));
    	        	if (UtilValidate.isEmpty(productFeatures)) {
    	        		result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDescription,"idCode",productFeatureIdCode,  "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId, "userLogin", userLogin));
    	        	}
        		} else {
        			List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId));
    	        	if (UtilValidate.isEmpty(productFeatures)) {
    	        		result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId, "userLogin", userLogin));
    	        	}
        		}
        		
	        	
        	}
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return "success";
    }
    
    /**
     * 创建可选特征，按照用户输入的文本 按照回车符分隔 一次添加多个
     * @author jiaqi.Dai
     * @param request
     * @param productFeatureCategoryId 分类ID
     * @param productFeatureDescStr 特征值
     * @param response
     * @return
     */
    public static String createProductSelectableFeature(HttpServletRequest request, HttpServletResponse response) {
        String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String productFeatureCategoryId = request.getParameter("productFeatureCategoryId");
        String productFeatureDescStr = request.getParameter("productFeatureDescs");
        StringTokenizer descToken = new StringTokenizer(productFeatureDescStr, "\n\r");
        
        try {
        	List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryId));
        	if (UtilValidate.isEmpty(productFeatures)) {
        		errMsg  = errMsg + "No features existed in this productFeatureCateogry";
        		return "error";
        	}
        	GenericValue oneProductFeature = EntityUtil.getFirst(productFeatures);
        	String productFeatureTypeId = oneProductFeature.getString("productFeatureTypeId");
        	while (descToken.hasMoreTokens()) {
        		
        		String productFeatureDesc = descToken.nextToken();
        		if (productFeatureDesc.contains(" ")) {
        			String productFeatureDescription = productFeatureDesc.substring(0, productFeatureDesc.indexOf(" "));
            		String productFeatureIdCode = productFeatureDesc.substring(productFeatureDesc.indexOf(" ") + 1, productFeatureDesc.length());
            		List<GenericValue> productFeature = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId));
    	        	if (UtilValidate.isEmpty(productFeature)) {
    	        		Map result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDescription,"idCode",productFeatureIdCode, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId, "userLogin", userLogin));
    	        	}
        		} else {
        			List<GenericValue> productFeature = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId));
    	        	if (UtilValidate.isEmpty(productFeature)) {
    	        		Map result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "productFeatureCategoryId", productFeatureCategoryId, "userLogin", userLogin));
    	        	}
        		}
        		
	        	
        	}
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        return "success";
    }
    
    /**
     * 为一个分类删除一个固定特征组
     * @author jiaqi.Dai
     * @param request 
     * @param response 
     * @param productCategoryId 分类ID
     * @param productFeatureGroupId 特征组Id
     * @return
     */
    public static String deleteProductFeatureCatGrpAppl(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Boolean flag = true;
        String featureName = "";
        String productCategoryId = request.getParameter("productCategoryId");
        String productFeatureGroupId = request.getParameter("productFeatureGroupId");
        
        try {
        	List<GenericValue> ProductFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId",productFeatureGroupId));
        	for (GenericValue ProductFeatureGroupAppl : ProductFeatureGroupAppls) {
        		List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", ProductFeatureGroupAppl.get("productFeatureId")));
        		List<GenericValue> ProdCatalogCategory = null;
        		for (GenericValue productFeatureAppl : productFeatureAppls) {
        			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productFeatureAppl.get("productId")));
        			String checkProductCategoryId = "";
        			if (product.get("isVariant").equals("N")) {        				
        				checkProductCategoryId = (String) product.get("primaryProductCategoryId");
        			} else {
        				GenericValue productAssoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", product.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT")));
        				GenericValue virtualProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productAssoc.get("productId")));
        				checkProductCategoryId = (String) virtualProduct.get("primaryProductCategoryId");
        			}
        			ProdCatalogCategory = delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", "MATERIAL_CATALOG", "productCategoryId", checkProductCategoryId));
        		}
        		if (UtilValidate.isNotEmpty(ProdCatalogCategory)) {
        			GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", ProductFeatureGroupAppl.get("productFeatureId")));
        			featureName = (String) productFeature.get("description");
        			flag = false;
        			break;
        		}
        	}
        	if (flag) {
        		List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId, "productFeatureGroupId", productFeatureGroupId));
            	if (UtilValidate.isEmpty(productFeatureCatGrpAppls)) {
            		return "success";
            	}
            	// 先删除productCategory和productFeatureGroup的关联
            	for (GenericValue productFeatureCatGrpAppl:productFeatureCatGrpAppls) {
            		dispatcher.runSync("removeProductFeatureCatGrpAppl",
            				UtilMisc.toMap("productCategoryId", productFeatureCatGrpAppl.get("productCategoryId"), "productFeatureGroupId", productFeatureCatGrpAppl.get("productFeatureGroupId"),
            						"fromDate", productFeatureCatGrpAppl.get("fromDate"), "userLogin", userLogin));
            	}
        	} else {
        		request.setAttribute("_ERROR_MESSAGE_", "特征组中[" + featureName + "]已被关联无法删除特征组");
        		return "error";
        	}
        	
        	
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        return "success";
    }
    
    /**
     * 为一个分类删除一个可选特征组,如果特征组中特征被使用 无法删除
     * @author jiaqi.Dai
     * @param request
     * @param productCategoryId 分类ID
     * @param productFeatureCategoryId 特征组ID
     * @param response
     * @return
     */
    public static String deleteProductFeatureCategoryAppl(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Boolean flag = true;
        
        String productCategoryId = request.getParameter("productCategoryId");
        String productFeatureCategoryId = request.getParameter("productFeatureCategoryId");
        String featureName = "";
        try {
        	List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureCategoryId",productFeatureCategoryId));
        	for (GenericValue productFeature : productFeatures) {
        		List<GenericValue> productFeatureAppl = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", productFeature.get("productFeatureId")));
        		if (UtilValidate.isNotEmpty(productFeatureAppl)) {
        			featureName = (String) productFeature.get("description");
        			flag = false;
        			break;
        		}
        	}
        	
        	if (flag) {
        		List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId, "productFeatureCategoryId", productFeatureCategoryId));
            	if (UtilValidate.isEmpty(productFeatureCategoryAppls)) {
            		return "success";
            	}
            	// 先删除productCategory和productFeatureGroup的关联
            	for (GenericValue productFeatureCategoryAppl:productFeatureCategoryAppls) {
            		dispatcher.runSync("removeProductFeatureCategoryAppl",
            				UtilMisc.toMap("productCategoryId", productFeatureCategoryAppl.get("productCategoryId"), "productFeatureCategoryId", productFeatureCategoryAppl.get("productFeatureCategoryId"),
            						"fromDate", productFeatureCategoryAppl.get("fromDate"), "userLogin", userLogin));
            	}
            	// 如果这个productFeatureCategory没有被其它productCategory关联，则删除这个productFeatureCategory
            	// ZuczugProductUtils.deleteEmptyProductFeatureCategory(delegator, dispatcher, productFeatureCategoryId, userLogin);
        	} else {
        		request.setAttribute("_ERROR_MESSAGE_", "特征组中[" + featureName + "]已被关联无法删除特征组");
        		return "error";
        	}
        	
        	
        	
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        return "success";
    }
    
    /**
     * 删除可选特征组中的特征
     * @author jiaqi.Dai
     * @param request
     * @param productFeatureId 特征ID
     * @param productFeatureGroupId 特征组ID
     * @param response
     * @return
     */
    public static String deleteProductFeatureGroupAppl(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        
        String productFeatureId = request.getParameter("productFeatureId");
        String productFeatureGroupId = request.getParameter("productFeatureGroupId");
        
        try {
        	List<GenericValue> productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureId", productFeatureId, "productFeatureGroupId", productFeatureGroupId));
        	if (UtilValidate.isEmpty(productFeatureGroupAppls)) {
        		return "success";
        	}
        	List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", productFeatureId));
        	if (UtilValidate.isNotEmpty(productFeatureAppls)) {
        		for (GenericValue productFeatureAppl : productFeatureAppls) {
        			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productFeatureAppl.get("productId")));
        			if (product.get("productTypeId").equals("RAW_MATERIAL")) {
        				request.setAttribute("_ERROR_MESSAGE_", "该特征已被物料所选择,无法删除！");
                		return "error";
					}
        		}
        		
        	}
        	// 先删除productCategory和productFeatureGroup的关联
        	for (GenericValue productFeatureGroupAppl:productFeatureGroupAppls) {
        		dispatcher.runSync("updateProductFeatureGroupAppl",
        				UtilMisc.toMap("productFeatureId", productFeatureGroupAppl.get("productFeatureId"), "productFeatureGroupId", productFeatureGroupAppl.get("productFeatureGroupId"),
        						"fromDate", productFeatureGroupAppl.get("fromDate"),"thruDate",nowTimestamp, "userLogin", userLogin));
        	}
        	
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        return "success";
    }

    /**
     * 删除某个productFeatureCategory的productFeature。
     * 事实上不存在所谓的删除，因为productFeatureCategoryId本身是productFeature的一个字段。
     * 所以所谓的删除，就是将这个字段设为null
     * @author jiaqi.Dai
     * @param request
     * @param productFeatureId 特征ID
     * @param productFeatureCategoryId 特征组ID
     * @param response
     * @return
     */
    public static String deleteProductSelectableFeature(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        String productFeatureId = request.getParameter("productFeatureId");
        String productFeatureCategoryId = request.getParameter("productFeatureCategoryId");
        
        try {
        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureId));
        	if (UtilValidate.isEmpty(productFeature) || UtilValidate.isEmpty(productFeature.getString("productFeatureCategoryId"))) {
        		return "success";
        	}
        	List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", productFeatureId));
        	if (UtilValidate.isNotEmpty(productFeatureAppls)) {
        		for (GenericValue productFeatureAppl : productFeatureAppls) {
        			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productFeatureAppl.get("productId")));
        			if (product.get("productTypeId").equals("RAW_MATERIAL")) {
        				request.setAttribute("_ERROR_MESSAGE_", "该特征已被物料所选择,无法删除！");
                		return "error";
					}
        		}
        		
        	}
        	if (productFeature.getString("productFeatureCategoryId").equals(productFeatureCategoryId)) {
        		productFeature.setString("productFeatureCategoryId", null);
        		productFeature.store();
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        return "success";
    }
    
    /**
     * 为可选特征组添加特征方法 按照回车符分隔一次添加多个
     * @author jiaqi.Dai
     * @param request
     * @param productFeatureGroupId 特征组ID
     * @param productFeatureDescStr 特征值
     * @param response
     * @return
     */
    public static String createProductFeatureGroupAppl(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");

        String errMsg=null;
        
        String productFeatureGroupId = request.getParameter("productFeatureGroupId");
        String productFeatureDescStr = request.getParameter("productFeatureDescs");
        StringTokenizer descToken = new StringTokenizer(productFeatureDescStr, "\n\r");
        
        try {
        	List<GenericValue> productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId));
        	if (UtilValidate.isEmpty(productFeatureGroupAppls)) {
        		errMsg  = errMsg + "No features existed in this productFeatureGroup";
        		return "error";
        	}
        	GenericValue oneProductFeatureGroupAppl = EntityUtil.getFirst(productFeatureGroupAppls);
        	GenericValue oneProductFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", oneProductFeatureGroupAppl.getString("productFeatureId")));
        	String productFeatureTypeId = oneProductFeature.getString("productFeatureTypeId");
        	while (descToken.hasMoreTokens()) {
        		String productFeatureDesc = descToken.nextToken();
        		Map result = null;
        		if (productFeatureDesc.contains(" ")) {
        			String productFeatureDescription = productFeatureDesc.substring(0, productFeatureDesc.indexOf(" "));
            		String productFeatureIdCode = productFeatureDesc.substring(productFeatureDesc.indexOf(" ") + 1, productFeatureDesc.length());
            		List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDescription, "idCode", productFeatureIdCode, "productFeatureTypeId", productFeatureTypeId));
        			GenericValue hasGroup = null;
        			for (GenericValue productFeature : productFeatures) {
    	        		hasGroup = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId ,"productFeatureId", productFeature.get("productFeatureId"),"thruDate", null)));
    	        		if (UtilValidate.isNotEmpty(hasGroup)) {
    	        			break;
    	        		}
        			}
            		if (UtilValidate.isEmpty(hasGroup)) {
    	        		result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDescription, "idCode", productFeatureIdCode, "productFeatureTypeId", productFeatureTypeId, "userLogin", userLogin));
    	        	}
        		} else {
        			List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId));
        			GenericValue hasGroup = null;
        			for (GenericValue productFeature : productFeatures) {
    	        		hasGroup = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId ,"productFeatureId", productFeature.get("productFeatureId"), "thruDate", null)));
    	        		if (UtilValidate.isNotEmpty(hasGroup)) {
    	        			break;
    	        		}
        			}
        			if (UtilValidate.isEmpty(hasGroup)) {
    	        		result = dispatcher.runSync("createProductFeature", UtilMisc.toMap("description", productFeatureDesc, "productFeatureTypeId", productFeatureTypeId, "userLogin", userLogin));
    	        	}
        		}
        		if (UtilValidate.isNotEmpty(result)) {
        			String productFeatureId = (String) result.get("productFeatureId");
    	        	dispatcher.runSync("createProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId, "productFeatureId", productFeatureId, "userLogin", userLogin));
        		}
	        	
        	}
        } catch (GenericServiceException e) {
			e.printStackTrace();
			return "error";
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return "error";
		}
        return "success";
    }
    
    /**
     * 为分类添加商品
     * @author jiaqi.Dai
     * @param request
     * @param zuczugId 素然物料编号
     * @param fromDate 时间外键
     * @param productCategoryId 分类ID
     * @param response
     * @return
     */
	public static String addCategoryProductMember(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
	    LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	    HttpSession session = request.getSession();
	    GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");	    
	    String zuczugId = request.getParameter("productId");
	    String fromDate = request.getParameter("fromDate");
	    String productCategoryId = request.getParameter("productCategoryId");
	    List<GenericValue> goods;
		try {
			goods = delegator.findByAnd("GoodIdentification", UtilMisc.toMap("idValue", zuczugId, "goodIdentificationTypeId", "ZUCZUG_CODE"));
			String productId = "";
	        if(goods.size() == 1) {
	        	GenericValue good = EntityUtil.getFirst(goods);
	        	productId = (String) good.get("productId");
	        	List<GenericValue> ProductCategoryMembers = delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap(
	        				"productId", productId,
	        				"productCategoryId", productCategoryId
	        			));
	        	if (UtilValidate.isEmpty(ProductCategoryMembers)) {
		        	dispatcher.runSync("addProductToCategory", UtilMisc.toMap("productId", productId, "fromDate", fromDate,"productCategoryId",productCategoryId, "userLogin", userLogin));
	        	} else {
	        		request.setAttribute("_ERROR_MESSAGE_", "此分类中已存在" + zuczugId + "，请误重复添加");
		        	return "error";
	        	}
	        } else if(goods.size() > 1){
	        	request.setAttribute("_ERROR_MESSAGE_", "存在多个素然物料编号为" + zuczugId + "的商品，请维护后重新添加");
	        	return "error";
	        }
	        
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return "success";
	}
}
