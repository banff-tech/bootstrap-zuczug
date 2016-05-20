package com.zuczug.product;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ZuczugProductUtils {

	public static final String module = ZuczugProductUtils.class.getName();
	public static final String IS_CONFIRMED_ATTR = "isConfirmed";
	public static final String COLOR_CONFIRMED_VALUE = "Y";
	public static final String COLOR_NOT_CONFIRMED_VALUE = "N";
	public static final Map<String, String> INTERNAL_SIZE_MAPPING = FastMap.newInstance();
	static {
		INTERNAL_SIZE_MAPPING.put("0", "SIZE0");
		INTERNAL_SIZE_MAPPING.put("2", "SIZE2");
		INTERNAL_SIZE_MAPPING.put("4", "SIZE4");
		INTERNAL_SIZE_MAPPING.put("6", "SIZE6");
		INTERNAL_SIZE_MAPPING.put("8", "SIZE8");
		INTERNAL_SIZE_MAPPING.put("25", "SIZE0");
		INTERNAL_SIZE_MAPPING.put("26", "SIZE2");
		INTERNAL_SIZE_MAPPING.put("27", "SIZE4");
		INTERNAL_SIZE_MAPPING.put("28", "SIZE6");
		INTERNAL_SIZE_MAPPING.put("29", "SIZE8");
		INTERNAL_SIZE_MAPPING.put("F", "SIZE4");
	}
	
	
	// 获取某个变型商品的颜色productFeatureId
	public static String getProductColorId(Delegator delegator, String productId) {
		String colorId = null;
		try {
			List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureTypeId", "COLOR", "productFeatureApplTypeId", "STANDARD_FEATURE"));
			productFeatureAndAppls = EntityUtil.filterByDate(productFeatureAndAppls);
			if (UtilValidate.isNotEmpty(productFeatureAndAppls)) {
				GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatureAndAppls);
				colorId = productFeatureAndAppl.getString("productFeatureId");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return colorId;
	}

	// 获取某个变型商品的颜色idCode
	public static String getProductColorCode(Delegator delegator, String productId) {
		String colorCode = null;
		try {
			List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureTypeId", "COLOR", "productFeatureApplTypeId", "STANDARD_FEATURE"));
			productFeatureAndAppls = EntityUtil.filterByDate(productFeatureAndAppls);
			if (UtilValidate.isNotEmpty(productFeatureAndAppls)) {
				GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatureAndAppls);
				colorCode = productFeatureAndAppl.getString("idCode");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return colorCode;
	}

	// 获取某个变型商品的尺码productFeatureId
	public static String getProductSizeId(Delegator delegator, String productId) {
		String sizeId = null;
		try {
			List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureTypeId", "SIZE", "productFeatureApplTypeId", "STANDARD_FEATURE"));
			productFeatureAndAppls = EntityUtil.filterByDate(productFeatureAndAppls);
			if (UtilValidate.isNotEmpty(productFeatureAndAppls)) {
				GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatureAndAppls);
				sizeId = productFeatureAndAppl.getString("productFeatureId");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return sizeId;
	}

	// 获取某个变型商品的尺码idCode
	public static String getProductSizeCode(Delegator delegator, String productId) {
		String sizeCode = null;
		try {
			List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureTypeId", "SIZE", "productFeatureApplTypeId", "STANDARD_FEATURE"));
			productFeatureAndAppls = EntityUtil.filterByDate(productFeatureAndAppls);
			if (UtilValidate.isNotEmpty(productFeatureAndAppls)) {
				GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatureAndAppls);
				sizeCode = productFeatureAndAppl.getString("idCode");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return sizeCode;
	}

	// 获取虚拟商品的某个变型商品，根据颜色的productFeatureId，尺寸的productFeatureId
	public static String getVariantProductWithColorAndSizeId(Delegator delegator, String virtualProductId, String colorId, String sizeId) {
		try {
			// 首先要检查虚拟商品是否有该颜色和改尺寸的特征，如果没有，即使其对应的变型商品有这两个特征，也不行
			List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", virtualProductId, "productFeatureId", colorId, "productFeatureApplTypeId", "SELECTABLE_FEATURE"));
			productFeatureAppls = EntityUtil.filterByDate(productFeatureAppls);
			if (UtilValidate.isEmpty(productFeatureAppls)) {
				return null;
			}
			productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", virtualProductId, "productFeatureId", sizeId, "productFeatureApplTypeId", "SELECTABLE_FEATURE"));
			productFeatureAppls = EntityUtil.filterByDate(productFeatureAppls);
			if (UtilValidate.isEmpty(productFeatureAppls)) {
				return null;
			}
			
			// 获取该虚拟商品的所有变型商品
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"));
			productAssocs = EntityUtil.filterByDate(productAssocs);
			if (UtilValidate.isEmpty(productAssocs)) {
				return null;
			}
			
			// 遍历每个变型商品，判断是否是这个颜色和尺码
			Iterator<GenericValue> it = productAssocs.iterator();
			while (it.hasNext()) {
				GenericValue productAssoc = (GenericValue) it.next();
				// 先检查颜色
				if (colorId.equals(getProductColorId(delegator, productAssoc.getString("productIdTo")))
						&& sizeId.equals(getProductSizeId(delegator, productAssoc.getString("productIdTo")))) {
					return productAssoc.getString("productIdTo");
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 根据组别的productCategoryId来获取该组别下所有虚拟商品ID。其中，带入的productTypeId参数如果没有，就代表所有WIP和FINISHED_GOOD
    public static List<String> getVirtualProductIdsByGroup(Delegator delegator, String productCategoryId, String productTypeId)
    		throws GenericEntityException {
        List<String> productIds = FastList.newInstance();
    	List<GenericValue> productAndCategoryMembers;
		if (UtilValidate.isEmpty(productTypeId)) {
			productAndCategoryMembers = delegator.findByAnd("ProductAndCategoryMember",
					UtilMisc.toMap("productCategoryId", productCategoryId, "isVirtual", "Y"));
			
		} else {
			productAndCategoryMembers = delegator.findByAnd("ProductAndCategoryMember",
					UtilMisc.toMap("productCategoryId", productCategoryId, "productTypeId", productTypeId, "isVirtual", "Y"));
		}
		productAndCategoryMembers = EntityUtil.filterByDate(productAndCategoryMembers);
		if (UtilValidate.isNotEmpty(productAndCategoryMembers)) {
			Iterator<GenericValue> it = productAndCategoryMembers.iterator();
			while (it.hasNext()) {
				GenericValue productAndCategoryMember = it.next();
				Debug.logInfo("productId: " + productAndCategoryMember.getString("productId"), module);
				productIds.add(productAndCategoryMember.getString("productId"));
			}
		}
		return productIds;
	}

	// 根据组别的productCategoryId来获取该组别下所有变型商品ID。其中，带入的productTypeId参数如果没有，就代表所有WIP和FINISHED_GOOD
    // 注意：变型商品是属于哪个组别的逻辑，并不是变型商品本身是否有这个组别的productCategoryId，而是看这个变型商品对应的虚拟商品，是否有这个组别的productCategoryId
    public static List<String> getVariantProductIdsByGroup(Delegator delegator, String productCategoryId, String productTypeId)
    		throws GenericEntityException {
    	List<String> productIds = FastList.newInstance();
    	// 先获取这个组的所有虚拟商品ID
        List<String> virtualProductIds = getVirtualProductIdsByGroup(delegator, productCategoryId, productTypeId);
        
        // 然后获取每个虚拟商品对应的变型商品ID
        for (String virtualProductId : virtualProductIds) {
        	productIds.addAll(getVariantProductIdByVirtualProductId(delegator, virtualProductId));
        }
		return productIds;
	}
    
    // 根据虚拟商品ID，获取所有的变型商品ID
    public static List<String> getVariantProductIdByVirtualProductId (Delegator delegator, String virtualProductId)
    		throws GenericEntityException {
    	List<String> variantProductIds = FastList.newInstance();
    	List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"));
    	productAssocs = EntityUtil.filterByDate(productAssocs);
    	if (UtilValidate.isNotEmpty(productAssocs)) {
	    	for (GenericValue productAssoc : productAssocs) {
	    		variantProductIds.add(productAssoc.getString("productIdTo"));
	    	}
    	}
    	return variantProductIds;
    }

    // 根据设计商品ID，获取正式商品ID。传入的productId可以是虚拟的，也可以是变型的，传入虚拟返回虚拟，传入变型返回变型
    // 正式商品颜色和设计商品颜色的对应关系，是在ProductFeatureApplAttr里。同理尺寸关系也在ProductFeatureApplAttr里
    public static String getProductIdByDesignProductId (Delegator delegator, String designProductId)
    		throws GenericEntityException {
    	GenericValue designProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", designProductId));
    	
    	// 如果是虚拟的话，直接通过ProductAssoc，找ProductAssocType为UNIQUE_ITEM的对应商品就行了
    	if ("Y".equals(designProduct.getString("isVirtual"))) {
    		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc",
    				UtilMisc.toMap("productId", designProductId, "productAssocTypeId", "UNIQUE_ITEM"));
    		productAssocs = EntityUtil.filterByDate(productAssocs);
    		if (UtilValidate.isNotEmpty(productAssocs)) {
    			return EntityUtil.getFirst(productAssocs).getString("productIdTo");
    		} else {
    			return null;
    		}
    	}
    	
    	// 如果是变型商品的话，先要找到其对应的虚拟商品
    	String designVirtualProductId = getVirtualProductIdByVariant(delegator, designProductId);
    	if (UtilValidate.isEmpty(designVirtualProductId)) { // 没有对应的虚拟，则无法找到对应的正式商品
            Debug.logError("no designVirtualProductId found!", module);
    		return null;
    	}
    	// 再通过虚拟商品找到对应的正式的虚拟商品
    	String virtualProductId = null;
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc",
				UtilMisc.toMap("productId", designVirtualProductId, "productAssocTypeId", "UNIQUE_ITEM"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		if (UtilValidate.isNotEmpty(productAssocs)) {
			virtualProductId = EntityUtil.getFirst(productAssocs).getString("productIdTo");
		} else { // 该设计虚拟商品还未关联到正式虚拟商品
            Debug.logError("no virtualProductId found!", module);
			return null;
		}
		
		// 获取设计商品的颜色和尺码
		String designColorId = getProductColorId(delegator, designProductId);
		String designSizeId = getProductSizeId(delegator, designProductId);
		if (UtilValidate.isEmpty(designColorId) || UtilValidate.isEmpty(designSizeId)) {
            Debug.logError("no designColorId or designSizeId found!", module);
			return null;
		}
		
		// 根据设计商品的颜色和正式虚拟商品的productId，能获取对应的正式商品的颜色productFeatureId
		List<GenericValue> productFeatureApplAttrs = delegator.findByAnd("ProductFeatureApplAttr",
				UtilMisc.toMap("productId", virtualProductId, "attrValue", designColorId));
		String colorId = null;
		if (UtilValidate.isNotEmpty(productFeatureApplAttrs)) {
			colorId = EntityUtil.getFirst(productFeatureApplAttrs).getString("productFeatureId");
		}
		if (UtilValidate.isEmpty(colorId)) {
            Debug.logError("no colorId found!", module);
			return null;
		}
		// 同样获取正式商品的尺寸productFeatureId
		productFeatureApplAttrs = delegator.findByAnd("ProductFeatureApplAttr",
				UtilMisc.toMap("productId", virtualProductId, "attrValue", designSizeId));
		String sizeId = null;
		if (UtilValidate.isNotEmpty(productFeatureApplAttrs)) {
			sizeId = EntityUtil.getFirst(productFeatureApplAttrs).getString("productFeatureId");
		}
		if (UtilValidate.isEmpty(sizeId)) {
            Debug.logError("no sizeId found!", module);
			return null;
		}
		String variantProductId = getVariantProductWithColorAndSizeId(delegator, virtualProductId, colorId, sizeId);
		
		return variantProductId;
    }

    // 根据正式商品ID，获取设计商品ID。传入的productId可以是虚拟的，也可以是变型的，传入虚拟返回虚拟，传入变型返回变型
    // 正式商品颜色和设计商品颜色的对应关系，是在ProductFeatureApplAttr里。同理尺寸关系也在ProductFeatureApplAttr里
    public static String getDesignProductIdByProductId (Delegator delegator, String productId)
    		throws GenericEntityException {
    	GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
    	if (UtilValidate.isEmpty(product)) {
    		return null;
    	}
    	
    	// 如果是虚拟的话，直接通过ProductAssoc，找ProductAssocType为UNIQUE_ITEM的对应商品就行了
    	if ("Y".equals(product.getString("isVirtual"))) {
    		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc",
    				UtilMisc.toMap("productIdTo", productId, "productAssocTypeId", "UNIQUE_ITEM"));
    		productAssocs = EntityUtil.filterByDate(productAssocs);
    		if (UtilValidate.isNotEmpty(productAssocs)) {
    			return EntityUtil.getFirst(productAssocs).getString("productId");
    		} else {
    			return null;
    		}
    	}
    	
    	// 如果是变型商品的话，先要找到其对应的虚拟商品
    	String virtualProductId = getVirtualProductIdByVariant(delegator, productId);
    	if (UtilValidate.isEmpty(virtualProductId)) { // 没有对应的虚拟，则无法找到对应的设计商品
            Debug.logError("no designVirtualProductId found!", module);
    		return null;
    	}
    	// 再通过虚拟商品找到对应的正式的虚拟商品
    	String designVirtualProductId = null;
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc",
				UtilMisc.toMap("productIdTo", virtualProductId, "productAssocTypeId", "UNIQUE_ITEM"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		if (UtilValidate.isNotEmpty(productAssocs)) {
			designVirtualProductId = EntityUtil.getFirst(productAssocs).getString("productId");
		} else { // 该设计虚拟商品还未关联到正式虚拟商品
            Debug.logError("no virtualProductId found!", module);
			return null;
		}
		
		// 获取正式商品的颜色和尺码
		String colorId = getProductColorId(delegator, productId);
		String sizeId = getProductSizeId(delegator, productId);
		if (UtilValidate.isEmpty(colorId) || UtilValidate.isEmpty(sizeId)) {
            Debug.logError("no colorId or sizeId found!", module);
			return null;
		}
		
		// 根据设计商品的颜色和正式虚拟商品的productId，能获取对应的正式商品的颜色productFeatureId
		List<GenericValue> productFeatureApplAttrs = delegator.findByAnd("ProductFeatureApplAttr",
				UtilMisc.toMap("productId", virtualProductId, "productFeatureId", colorId));
		String designColorId = null;
		if (UtilValidate.isNotEmpty(productFeatureApplAttrs)) {
			designColorId = EntityUtil.getFirst(productFeatureApplAttrs).getString("attrValue");
		}
		if (UtilValidate.isEmpty(designColorId)) {
            Debug.logError("no colorId found!", module);
			return null;
		}
		// 同样获取正式商品的尺寸productFeatureId
		productFeatureApplAttrs = delegator.findByAnd("ProductFeatureApplAttr",
				UtilMisc.toMap("productId", virtualProductId, "productFeatureId", sizeId));
		String designSizeId = null;
		if (UtilValidate.isNotEmpty(productFeatureApplAttrs)) {
			designSizeId = EntityUtil.getFirst(productFeatureApplAttrs).getString("attrValue");
		}
		if (UtilValidate.isEmpty(designSizeId)) {
            Debug.logError("no sizeId found!", module);
			return null;
		}
		String variantProductId = getVariantProductWithColorAndSizeId(delegator, designVirtualProductId, designColorId, designSizeId);
		
		return variantProductId;
    }

    // 根据虚拟商品ID，获取所有的变型商品ID
    public static String getVirtualProductIdByVariant (Delegator delegator, String variantProductId)
    		throws GenericEntityException {
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc",
				UtilMisc.toMap("productIdTo", variantProductId, "productAssocTypeId", "PRODUCT_VARIANT"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		if (UtilValidate.isNotEmpty(productAssocs)) {
			return EntityUtil.getFirst(productAssocs).getString("productId");
		}
		else {
			return null;
		}
    }

	// 获取虚拟商品的某个颜色的所有变型商品
	public static List<String> getVariantProductIdsByColor(Delegator delegator, String virtualProductId, String colorId) {
		List<String> productIds = FastList.newInstance();
		try {
			// 首先要检查虚拟商品是否有该颜色的特征，如果没有，即使其对应的变型商品有这个特征，也不行
			List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", virtualProductId, "productFeatureId", colorId, "productFeatureApplTypeId", "SELECTABLE_FEATURE"));
			productFeatureAppls = EntityUtil.filterByDate(productFeatureAppls);
			if (UtilValidate.isEmpty(productFeatureAppls)) {
				return null;
			}
			
			// 获取该虚拟商品的所有变型商品
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"));
			productAssocs = EntityUtil.filterByDate(productAssocs);
			if (UtilValidate.isEmpty(productAssocs)) {
				return null;
			}
			
			// 遍历每个变型商品，判断是否是这个颜色和尺码
			Iterator<GenericValue> it = productAssocs.iterator();
			while (it.hasNext()) {
				GenericValue productAssoc = (GenericValue) it.next();
				// 检查颜色
				if (colorId.equals(getProductColorId(delegator, productAssoc.getString("productIdTo")))) {
					productIds.add(productAssoc.getString("productIdTo"));
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 方法停用！ 通过feature查询组别信息的
	 * @param delegator
	 * @param productId
	 * @return
	 */
	public static Map<String,String> getVariantGroupFeatures(Delegator delegator,String productId){
		Map<String,String> result = FastMap.newInstance();
		
		try {
			GenericValue group = EntityUtil.getFirst( delegator.findByAnd("ProductDesignFeatureWithType", UtilMisc.toMap("productId",productId,"productFeatureTypeId","GROUPNAME")));
			if(UtilValidate.isNotEmpty(group)){
				result.put("groupId", group.getString("productFeatureId"));
				GenericValue pgv = EntityUtil.getFirst(delegator.findByAnd("ProductGroupView", UtilMisc.toMap("groupId",group.getString("productFeatureId"))));
				if(UtilValidate.isNotEmpty(pgv)){
					result.put("seasonId", pgv.getString("seasonId"));
					result.put("seriesId", pgv.getString("seriesId"));
					result.put("subSeriesId", pgv.getString("subSeriesId"));
				}
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 查询一个商品的组别等信息，如果是变型就查询他的虚拟
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @return （组别:groupId、系列:seriesId、子系列:subSeriesId、季:seasonId、波段:waveId）
	 */
	public static Map<String,String> getVariantGroupCategorys(Delegator delegator,String productId){
		Map<String,String> result = FastMap.newInstance();
		
		try {
			GenericValue product=delegator.findOne("Product", true, UtilMisc.toMap("productId",productId));
			if(UtilValidate.isNotEmpty(product) && "Y".equals(product.getString("isVariant"))){//如果是变形商品，就查询他的虚拟
				GenericValue assoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo",productId,"productAssocTypeId","PRODUCT_VARIANT")));
				if(UtilValidate.isNotEmpty(assoc)){
					productId = assoc.getString("productId");
				}
			}
			GenericValue group = EntityUtil.getFirst( delegator.findByAnd("ProductCategoryMemberView", UtilMisc.toMap("productId",productId,"productCategoryTypeId","GROUPNAME")));
			if(UtilValidate.isNotEmpty(group)){
				result.put("groupId", group.getString("productCategoryId"));
				GenericValue pgv = EntityUtil.getFirst(delegator.findByAndCache("ProductCategoryGroupView", UtilMisc.toMap("groupId",group.getString("productCategoryId"))));
				if(UtilValidate.isNotEmpty(pgv)){
					result.put("seasonId", pgv.getString("seasonId"));
					result.put("seriesId", pgv.getString("seriesId"));
					result.put("subSeriesId", pgv.getString("subSeriesId"));
				}
			}
			
			GenericValue wave = EntityUtil.getFirst(delegator.findByAndCache("ProductCategoryMemberView", UtilMisc.toMap("productId",productId,"productCategoryTypeId","WAVE")));
			if(UtilValidate.isNotEmpty(wave)){
				result.put("waveId", wave.getString("productCategoryId"));
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 获取商品的其他的GoodIdentificationCode
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @param idTypeId
	 * @return
	 */
	public static String getProductGoodIdentificationCode(Delegator delegator,String productId,String idTypeId){
		String code = "";
		
		try {
			GenericValue gi = delegator.findOne("GoodIdentification", false, UtilMisc.toMap("productId",productId,"goodIdentificationTypeId",idTypeId));
			if(UtilValidate.isNotEmpty(gi))
				code = gi.getString("idValue");
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return code;
	}
	
	/**
	 * 获取面辅料的组别信息，因为面辅料可以多个组别，所以要拼接
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @return
	 */
	public static String getFabricAccessoryGroupInfo(Delegator delegator,String productId){
		String groupName = "";
		try {
			List<GenericValue> list = delegator.findByAnd("ProductCategoryMemberView", UtilMisc.toMap("productId",productId,"productCategoryTypeId","GROUPNAME"));
			if(UtilValidate.isNotEmpty(list)){
				for (GenericValue member : list) {
					groupName+=member.getString("categoryName")+"/";
				}
			}
			if(groupName.length()>0){
				groupName = groupName.substring(0,groupName.length()-1);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return groupName;
	}
	
	/**
	 * 获取商品的供应商编号
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @param supplierPartyId
	 * @return
	 */
	public static String getProductSupplierCode(Delegator delegator,String productId,String supplierPartyId){
		String code = "";
		
		try {
			Map<String,String> cond = FastMap.newInstance();
			cond.put("productId",productId);
			if(UtilValidate.isNotEmpty(supplierPartyId)){
				cond.put("partyId", supplierPartyId);
			}
			List<GenericValue> spList = delegator.findByAnd("SupplierProduct", cond);
			spList = EntityUtil.filterByDate(spList, UtilDateTime.nowTimestamp(), "availableFromDate", "availableThruDate",false);
			if(UtilValidate.isNotEmpty(spList)){
				code = EntityUtil.getFirst(spList).getString("supplierProductId");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return code;
	}
	
	/**
	 * 检查某个商品是否有4码，如果没有就创建
	 * 现在是检查商品是否有中间码，4 或者 27
	 * 补充逻辑，如果商品是非虚拟非变型，直接返回他自己
	 * @author sven
	 * @param productId
	 * @param colorId
	 * @param delegator
	 * @param dispatcher
	 * @return
	 */
	public static String checkProductMiddleSize(String productId,String colorId,Delegator delegator,LocalDispatcher dispatcher){
		String skuId = "";
		try {
			GenericValue pro = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
			if(UtilValidate.areEqual(pro.getString("isVirtual"), "N") && UtilValidate.areEqual(pro.getString("isVariant"), "N")){
				return productId;
			}
			GenericValue colorFeature = delegator.findOne("ProductFeature", true, UtilMisc.toMap("productFeatureId",colorId));
			if(UtilValidate.isEmpty(colorFeature)){
				return skuId;
			}
			String size4Id = productId+"-"+colorFeature.getString("idCode")+"-4";
			String size27Id = productId+"-"+colorFeature.getString("idCode")+"-27";
			String sizeFId = productId+"-"+colorFeature.getString("idCode")+"-F";
			String size37Id = productId+"-"+colorFeature.getString("idCode")+"-37";
			GenericValue product = delegator.findOne("Product", true, UtilMisc.toMap("productId",size4Id));
			GenericValue product27 = delegator.findOne("Product", true, UtilMisc.toMap("productId",size27Id));
			GenericValue productF = delegator.findOne("Product", true, UtilMisc.toMap("productId",sizeFId));
			GenericValue product37 = delegator.findOne("Product", true, UtilMisc.toMap("productId",size37Id));
			if(UtilValidate.isNotEmpty(product)){
				if(UtilValidate.isNotEmpty(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",productId,"productIdTo",size4Id,"productAssocTypeId","PRODUCT_VARIANT")))){
					return size4Id;
				}
				Debug.log("productId:"+productId+"--color:"+colorId+",中间码存在，但是没有Assoc关联");
			}
			
			if(UtilValidate.isNotEmpty(product27)){
				if(UtilValidate.isNotEmpty(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",productId,"productIdTo",size27Id,"productAssocTypeId","PRODUCT_VARIANT")))){
					return size27Id;
				}
				Debug.log("productId:"+productId+"--color:"+colorId+",中间码存在，但是没有Assoc关联");
			}
			
			if(UtilValidate.isNotEmpty(product37)){
				if(UtilValidate.isNotEmpty(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",productId,"productIdTo",size37Id,"productAssocTypeId","PRODUCT_VARIANT")))){
					return size37Id;
				}
				Debug.log("productId:"+productId+"--color:"+colorId+",中间码存在，但是没有Assoc关联");
			}
			
			if(UtilValidate.isNotEmpty(productF)){
				if(UtilValidate.isNotEmpty(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",productId,"productIdTo",sizeFId,"productAssocTypeId","PRODUCT_VARIANT")))){
					return sizeFId;
				}
				Debug.log("productId:"+productId+"--color:"+colorId+",中间码存在，但是没有Assoc关联");
			}
			if(UtilValidate.isEmpty(skuId)){
				Debug.log("productId:"+productId+"--color:"+colorId+",没有中间码");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
		return skuId;
	}
	
	/**
	 * @author sven
	 * @description 原来的查4码方法，被新的覆盖了
	 * @param productId
	 * @param colorId
	 * @param delegator
	 * @param dispatcher
	 * @return
	 */
	public static String checkProductSize4AndCreate(String productId,String colorId,Delegator delegator,LocalDispatcher dispatcher){
		return checkProductMiddleSizeAndCreate(productId,colorId,delegator,dispatcher);
	}
	
	/**
	 * 获取中间码，限4码，只在设计那边用
	 * @author sven
	 * @param productId
	 * @param colorId
	 * @param delegator
	 * @param dispatcher
	 * @return
	 */
	public static String checkProductMiddleSizeAndCreate(String productId,String colorId,Delegator delegator,LocalDispatcher dispatcher){
		String skuId = "";
		try {
			GenericValue colorFeature = delegator.findOne("ProductFeature", true, UtilMisc.toMap("productFeatureId",colorId));
			if(UtilValidate.isEmpty(colorFeature)){
				return skuId;
			}
			String size4Id = productId+"-"+colorFeature.getString("idCode")+"-4";
			GenericValue product = delegator.findOne("Product", true, UtilMisc.toMap("productId",size4Id));
			if(UtilValidate.isEmpty(product)){
				GenericValue feature = delegator.findOne("ProductFeature", true, UtilMisc.toMap("productFeatureId",colorId));
				String colorCode = feature.getString("idCode");
				String sizeId = "SIZE_4";
				String sizeCode = "4";
				String variantProductId = productId + "-" + colorCode + "-" + sizeCode;
				
				GenericValue userLogin = delegator.findOne("UserLogin", true, UtilMisc.toMap("userLoginId","system"));
				
				Map<String,Object> result = dispatcher.runSync("quickAddVariant", UtilMisc.toMap("productFeatureIds",colorId + "|" + sizeId, "productId",productId, "productVariantId",variantProductId));
				List<GenericValue> originalSizes = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId",productId, "productFeatureId","SIZE_4"));
				if(UtilValidate.isEmpty(originalSizes)){
					dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productFeatureApplTypeId","SELECTABLE_FEATURE", "productId",productId,"productFeatureId",sizeId,"userLogin",userLogin));
					dispatcher.runSync("copyStandardFeaturesToVariants",UtilMisc.toMap("virtualProductId",productId,"variantProductId",variantProductId,"userLogin",userLogin));
				}
			}
			skuId = size4Id;
			if(UtilValidate.isEmpty(skuId)){
				Debug.log("productId:"+productId+"--color:"+colorId+",没有中间码");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		} 
		return skuId;
	}
	
	/**
	 * 添加或者修改商品的属性(这个不是service，但可以被createOrUpdateProductAttribute这个service替代，请不要再使用这个方法)
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @param value
	 * @return
	 */
	public static String createOrUpdateProductAttribute(Delegator delegator,String productId,String attrName,String attrValue){
		String isOk="Y";

		try {
			delegator.removeByAnd("ProductAttribute", UtilMisc.toMap("productId",productId,"attrName",attrName));
			if(UtilValidate.isNotEmpty(attrValue)){//如果新的不为空，就保存，否则删除就删除了
				GenericValue newEntity = delegator.makeValue("ProductAttribute");
				newEntity.set("productId", productId);
				newEntity.set("attrName",attrName);
				newEntity.set("attrValue", attrValue);
				newEntity.create();
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			isOk = "N";
		}
		return isOk;
	}
	
	/**
	 * 
	 * @author sven
	 * @description 获取一个product的某个attrName的attrValue 
	 * @param delegator
	 * @param productId
	 * @param attrName
	 * @return
	 */
	public static String getProductAttributeByName(Delegator delegator,String productId,String attrName){
		try {
			GenericValue pa = delegator.findOne("ProductAttribute",false, UtilMisc.toMap("productId",productId,"attrName",attrName));
			if(UtilValidate.isNotEmpty(pa)){
				return pa.getString("attrValue");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 创建一个面料缩率，和供应商相关
	 * @param delegator
	 * @param productId
	 * @param supplierPartyId
	 * @return
	 */
	public static String createOrUpdateMaterialLossrate(Delegator delegator,String productId,String supplierPartyId,String lossRate){
		String isOk = "Y";
		
		String attrName="LOSS_RATE"; //把供应商的编号拼到后面
		try {
			if(UtilValidate.isEmpty(supplierPartyId)){//如果供应商没传，就去找一个默认的
				List<GenericValue> spList = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId",productId));
				GenericValue sp = EntityUtil.getFirst( EntityUtil.filterByDate(spList,UtilDateTime.nowTimestamp(),"availableFromDate","availableThruDate",true) );
				if(UtilValidate.isNotEmpty(sp)){
					attrName += ("_"+sp.getString("partyId")); 
				}
			}else{
				attrName += ("_"+supplierPartyId);
			}
			delegator.removeByAnd("ProductAttribute", UtilMisc.toMap("productId",productId,"attrName",attrName));
			GenericValue newEntity = delegator.makeValue("ProductAttribute");
			newEntity.set("productId", productId);
			newEntity.set("attrName",attrName);
			newEntity.set("attrValue", lossRate);
			newEntity.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return isOk;
	}
	
	/**
	 * 
	 * @author sven
	 * @description 获取面料的损耗率 
	 * @param delegator
	 * @param productId
	 * @param supplierPartyId
	 * @return
	 */
	public static String getMaterialLossRate(Delegator delegator,String productId,String supplierPartyId){
		String attrName="LOSS_RATE"; //把供应商的编号拼到后面
		try {
			if(UtilValidate.isEmpty(supplierPartyId)){//如果供应商没传，就去找一个默认的
				List<GenericValue> spList = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId",productId));
				GenericValue sp = EntityUtil.getFirst( EntityUtil.filterByDate(spList,UtilDateTime.nowTimestamp(),"availableFromDate","availableThruDate",true) );
				if(UtilValidate.isNotEmpty(sp)){
					attrName += ("_"+sp.getString("partyId")); 
				}
			}else{
				attrName += ("_"+supplierPartyId);
			}
			GenericValue pa = delegator.findOne("ProductAttribute",false, UtilMisc.toMap("productId",productId,"attrName",attrName));
			if(UtilValidate.isNotEmpty(pa)){
				return pa.getString("attrValue");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获取某个商品的listPrice的第一条记录
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @return
	 * @throws GenericEntityException
	 */
	public static BigDecimal getListPrice(Delegator delegator, String productId) throws GenericEntityException {
		List<GenericValue> productPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "LIST_PRICE"));
		productPrices = EntityUtil.filterByDate(productPrices);
		if (UtilValidate.isNotEmpty(productPrices)) {
			return EntityUtil.getFirst(productPrices).getBigDecimal("price");
		} else {
			return null;
		}
	}
	
	/**
	 * 获取一个正式虚拟商品的主色款号(主色是中间码的那个变型商品上)
	 * @param delegator
	 * @param virtualProductId
	 * @return
	 */
	public static String getMasterColorProductId(Delegator delegator,String virtualProductId){
		String masterColorProductId=  "";
		try {
			GenericValue assoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",virtualProductId,"productAssocTypeId","PRODUCT_VARIANT"),UtilMisc.toList("sequenceNum")));
			if(UtilValidate.isNotEmpty(assoc)){
				masterColorProductId = assoc.getString("productIdTo");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return masterColorProductId;
	}
	
	/**
	 * 获取一个正式虚拟商品的主色的颜色featureId(主色是中间码的那个变型商品上)
	 * @param delegator
	 * @param virtualProductId
	 * @return
	 */
	public static String getProductMasterColorId(Delegator delegator,String virtualProductId){
		String colorId=  "";
		try {
			GenericValue assoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",virtualProductId,"productAssocTypeId","PRODUCT_VARIANT"),UtilMisc.toList("sequenceNum")));
			if(UtilValidate.isNotEmpty(assoc)){
				String productIdTo = assoc.getString("productIdTo");
				GenericValue pfa = EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId",productIdTo,"productFeatureTypeId","COLOR")));
				colorId=pfa.getString("productFeatureId");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return colorId;
	}
	
	/**
	 * 获取一个产品在Inventoryitem表中的总库存数
	 * @author sven
	 * @param delegator
	 * @param productId
	 * @return
	 * @throws GenericEntityException
	 */
	public static BigDecimal getProductInventoryQuantity(Delegator delegator, String productId) throws GenericEntityException {
		BigDecimal totalInventoryQuantity = BigDecimal.ZERO;
		
		List<GenericValue> inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));
		for (GenericValue inventoryItem : inventoryItems) {
			totalInventoryQuantity = totalInventoryQuantity.add(inventoryItem.getBigDecimal("quantityOnHandTotal"));
		}
		return totalInventoryQuantity;
	}

	public static BigDecimal getProductPurchaseQuantity(Delegator delegator, String productId) throws GenericEntityException {
		BigDecimal totalPuarchaseQuantity = BigDecimal.ZERO;
		
        EntityConditionList<EntityExpr> statusCondition = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("itemStatusId", EntityOperator.EQUALS, "ITEM_CREATED"),
                EntityCondition.makeCondition("itemStatusId", EntityOperator.EQUALS, "ITEM_APPROVED")), EntityOperator.OR);
        EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(UtilMisc.toList(
        		statusCondition,
                EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
                EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "PURCHASE_ORDER")),
                EntityOperator.AND);
        
		List<GenericValue> orderHeaderAndItems = delegator.findList("OrderHeaderAndItems", conditions, null, null, null, false);
		for (GenericValue orderHeaderAndItem : orderHeaderAndItems) {
			totalPuarchaseQuantity = totalPuarchaseQuantity.add(orderHeaderAndItem.getBigDecimal("quantity"));
		}
		return totalPuarchaseQuantity;
	}
	
	
	public static String getTransferAssocByInventoryItemId(Delegator delegator,String inventoryItemTo){
		String inventoryItemId=null;
		try {
			GenericValue assoc = EntityUtil.getFirst(delegator.findByAnd("InventoryTransferAssoc", UtilMisc.toMap("inventoryItemIdTo",inventoryItemTo)));
			if(UtilValidate.isNotEmpty(assoc)){
				inventoryItemId = assoc.getString("inventoryItemIdFrom");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return inventoryItemId;
	}

	// 根据productFeatureGroupId获取某个product的某个productFeature
	public static List<GenericValue> findFeatureByGroup(Delegator delegator, String productId, String productFeatureGroupId) throws GenericEntityException {
		List<GenericValue> resultFeatures = FastList.newInstance();
		// 获取这个product的所有productFeature
		List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", productId));
		for (GenericValue productFeatureAppl : productFeatureAppls) {
			List<GenericValue> productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl",
					UtilMisc.toMap("productFeatureId", productFeatureAppl.getString("productFeatureId"), "productFeatureGroupId", productFeatureGroupId));
			if (UtilValidate.isNotEmpty(productFeatureGroupAppls)) {
				resultFeatures.add(productFeatureAppl);
			}
		}
		return resultFeatures;
	}
	
	// 根据productFeatureCategoryId获取某个product的某个productFeature
		public static List<GenericValue> findFeatureByCategory(Delegator delegator, String productId, String productFeatureCategoryId) throws GenericEntityException {
			List<GenericValue> resultFeatures = FastList.newInstance();
			// 获取这个product的所有productFeature
			List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", productId));
			for (GenericValue productFeatureAppl : productFeatureAppls) {
				GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureAppl.get("productFeatureId")));
				if (productFeatureCategoryId.equals(productFeature.get("productFeatureCategoryId"))) {
					resultFeatures.add(productFeatureAppl);
				}
			}
			return resultFeatures;
		}
	
		/**
		 * 删除可选特征组
		 * @author jiaqi.Dai
		 * @param delegator
		 * @param dispatcher
		 * @param productFeatureGroupId
		 * @param userLogin
		 * @throws GenericEntityException
		 * @throws GenericServiceException
		 */
		public static void deleteEmptyProductFeatureGroup(Delegator delegator, LocalDispatcher dispatcher, String productFeatureGroupId, GenericValue userLogin) throws GenericEntityException, GenericServiceException {
			// 先检查这个productFeatureGroup没有被其它productCategory关联
	    	List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId));
	    	if (UtilValidate.isNotEmpty(productFeatureCatGrpAppls)) { // 这个productFeatureGroup被其它productCategory关联的，所以不能删除
	    		return;
	    	}
	    	// 要删除productFeatureGroup，先要删除其包含的productFeature，也就是productFeatureGroupAppl
	    	List<GenericValue> productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId));
	    	for (GenericValue productFeatureGroupAppl:productFeatureGroupAppls) {
	    		dispatcher.runSync("removeProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupAppl.get("productFeatureGroupId"),
	    				"productFeatureId", productFeatureGroupAppl.get("productFeatureId"), "fromDate", productFeatureGroupAppl.get("fromDate"), "userLogin", userLogin));
	    	}
	    	// 最后删除productFeatureGroup自己
	    	GenericValue productFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId));
	    	productFeatureGroup.remove();
		}
	
	/**
	 * 检查某个库存明细是否有在转移的状态
	 * @param inventoryItemId
	 * @param delegator
	 * @return
	 */
	public static String checkInventoryShipment(String inventoryItemId,Delegator delegator){
		String status = "";
		try {
			List<GenericValue> issuanceList = delegator.findByAnd("ItemIssuance", UtilMisc.toMap("inventoryItemId",inventoryItemId),UtilMisc.toList("-issuedDateTime"));
			if(UtilValidate.isNotEmpty(issuanceList)){
				for (GenericValue iss : issuanceList) {
					GenericValue shipment = delegator.findOne("Shipment", false, UtilMisc.toMap("shipmentId",iss.getString("shipmentId")));
					if(UtilValidate.isNotEmpty(shipment)){
						String statusId=shipment.getString("statusId");
						if("TRANSFER".equals(shipment.getString("shipmentTypeId"))){
							status = statusId;
							break;
						}
					}
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return status;
	}
	
	/**
	 * 查询所有正式商品
	 * 1.商品ID使用模糊查询参考代码已完成
	 * 2.用户一般都只输入款号，查询结果请精确到SKU
	 * 3.除第4条以外的情况，SKU如果找不到款(也就是在ProductAssoc中无法查询到虚拟产品)取消输出显示，说明此SKU以被取消
	 * 4.存在非虚拟非变形的商品，此商品需要显示(他们也是在ProductAssoc中无法查询到虚拟产品，注意判断) 
	 * @author zhouLei
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findRealProduct(DispatchContext dctx, Map<String, Object> context) {
//    	Map<String, Object> returnMap = ServiceUtil.returnSuccess(); 
    	Map<String, Object> result = ServiceUtil.returnSuccess(); 
		Delegator delegator = dctx.getDelegator();
		String productId = (String) context.get("productIdKeyword");
		List<EntityExpr> exprs = FastList.newInstance();
		//按照ID查询非虚拟正式商品例子
		if (UtilValidate.isNotEmpty(productId)) {
			exprs.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%"+ productId +"%"));
		}
	    exprs.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
	    exprs.add(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "N"));
		try {
			List<GenericValue> productList = delegator.findList("Product",EntityCondition.makeCondition(exprs, EntityOperator.AND) , null, null, null, false);
			
			if (UtilValidate.isNotEmpty(productList)) {
				List<GenericValue> adlist = FastList.newInstance();
				Debug.log("+++++++++++++++productList:"+productList);
				Debug.log("+++++++++++++++adlist:"+adlist);
				for (GenericValue product:productList) {
					
					List<GenericValue> productAssocList=delegator.findByAnd("ProductAssoc", 
							UtilMisc.toMap("productIdTo", product.getString("productId"),"productAssocTypeId","PRODUCT_VARIANT"));
					if (UtilValidate.isNotEmpty(productAssocList)) {
						adlist.add(product);
					}else if (product.getString("isVirtual").equals("N")&&
							product.getString("isVariant").equals("N")&&
							product.getString("productTypeId").equals("FINISHED_GOOD")) {
						adlist.add(product);
					}
				}

				Debug.log("+++++++++++++++adlist2:"+adlist);
				result.put("product", adlist);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
    }
	
	/**
	 * 更新商品所有特征
	 * @author jiaqi.Dai
	 * @param request
	 * @param response
	 * @param productCategoryId 分类ID
	 * @param productId 商品ID
	 * @param productWidth 幅宽
	 * @return
	 */
	public static String updateProductFeature(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String productCategoryId = request.getParameter("productCategoryId");
        String productId = request.getParameter("productId");
        String description = "";
        BigDecimal productWidth = null;
        BigDecimal productWeight = null;
        if (UtilValidate.isNotEmpty(request.getParameter("productWidth"))) {
        	productWidth = new BigDecimal(Double.parseDouble(request.getParameter("productWidth")));
        } else {
        	productWidth = new BigDecimal(0);
        }
        com.zuczug.product.ZuczugProductUtils.createOrUpdateProductAttribute(delegator, productId, "WEIGHT_G", request.getParameter("productWeight"));
		try {			
			
			Map<String, Object> result = dispatcher.runSync("updateProductAndFeature", UtilMisc.toMap(
					"productId", request.getParameter("productId"), 
					"internalName", request.getParameter("updateInternalName"),
					"idValue", request.getParameter("idValue"),
					"isVirtual", request.getParameter("isVirtual"),
					"isVariant", request.getParameter("isVariant"),
					"productTypeId", request.getParameter("productTypeId"),
					"description", request.getParameter("description"),
					"comments", request.getParameter("comments"),
					"longDescription", request.getParameter("longDescription"),
					"productFeatureId", request.getParameter("productFeatureId"),
					"quantityUomId", request.getParameter("quantityUomId"),
					"productWidth", productWidth,
					"primaryProductCategoryId", request.getParameter("primaryProductCategoryId"),
					"productFeatureApplTypeId", request.getParameter("productFeatureApplTypeId"),
					"userLogin",userLogin
					));
			if ("true".equals(result.get("idValueRepeat"))) {
				request.setAttribute("_ERROR_MESSAGE_", "素然物料编号重复，请确认!");
				return "error";
			}
	        GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId", productId));   
	        description = updateProductGroupFeature(productId, request, response);  
	        description += updateProductCategoryFeature(request, response);
	        description += "幅宽:" + productWidth;
	        product.set("description", description);
			delegator.store(product);
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId",productId, "productAssocTypeId","PRODUCT_VARIANT"));
			if (UtilValidate.isNotEmpty(productAssocs)) {
				for (GenericValue productAssoc : productAssocs) {
					updateProductGroupFeature((String) productAssoc.get("productIdTo"), request, response);
					updateProductDescription((String) productAssoc.get("productIdTo"), productCategoryId, delegator, dispatcher);
				}
			}
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();			
		}       
		return "success";
    }
	
	/**
	 * 更新某商品的可选特征
	 * @author jiaqi.Dai
	 * @param productId
	 * @param request
	 * @param response
	 * @return
	 */
	public static String updateProductGroupFeature(String productId, HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String productCategoryId = request.getParameter("productCategoryId");
        String description = "";
        List<GenericValue> productFeatureCatGrpAppls;
		try {
			productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
	        	String productFeatureId = request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
	        	if (UtilValidate.isNotEmpty(productFeatureId)&&!productFeatureId.equals("NONE")) {
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
	        	} else {
	        		List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByGroup(delegator, productId, productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		        	if (UtilValidate.isNotEmpty(productFeatureAppls)) { // 如果存在，就删掉
		        		for (GenericValue productFeatureAppl : productFeatureAppls) {
		        			dispatcher.runSync("removeFeatureFromProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureAppl.getString("productFeatureId"), "fromDate", productFeatureAppl.getTimestamp("fromDate"), "userLogin", userLogin));	        			
		        		}
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
    	
		return description;
    	
    }
    /**
     * 跟新某商品的固定特征
     * @author jiaqi.Dai
     * @param request
     * @param response
     * @return
     */
    public static String updateProductCategoryFeature(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
    	String productId = request.getParameter("productId");
        String productCategoryId = request.getParameter("productCategoryId");
        String description = "";
		try {
			 List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
	        	String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));		        	
	        	if (UtilValidate.isNotEmpty(productFeatureCategoryId)) {
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
	        	}
	        }
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		return description;   	
    } 
    
    /**
     * 生成某商品的最新规格
     * @author jiaqi.Dai
     * @param productId 商品ID
     * @param productCategoryId 分类ID
     * @param delegator
     * @param dispatcher
     * @return description 规格
     */
    public static String updateProductDescription(String productId, String productCategoryId, Delegator delegator,LocalDispatcher dispatcher) {
        DispatchContext dctx = dispatcher.getDispatchContext();
    	String description = "";
		try {
			List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
			
			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));	        
	        List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId",productId, "productFeatureApplTypeId", "STANDARD_FEATURE"));   
	    	for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
	    		List<GenericValue> productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrpAppl.get("productFeatureGroupId")));
	        	for (GenericValue productFeatureAppl : productFeatureAppls) {
	        		for(GenericValue productFeatureGroupAppl : productFeatureGroupAppls) {
	        			if (productFeatureAppl.get("productFeatureId").equals(productFeatureGroupAppl.get("productFeatureId"))) {
	        				GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureAppl.get("productFeatureId")));
	        	    		GenericValue ProductFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrpAppl.getString("productFeatureGroupId")));
	        	    		description = description + ProductFeatureGroup.get("description") + ":" + productFeature.get("description") + " ";
		        		}
	        		}	        		
	        	}
	        	
	        }
	        
	        for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
	        	List<GenericValue> productFeatures = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.get("productFeatureCategoryId")));
	        	for(GenericValue systemproductFeature : productFeatures) {
	        		for (GenericValue productFeatureAppl : productFeatureAppls) {
	        			if (productFeatureAppl.get("productFeatureId").equals(systemproductFeature.get("productFeatureId"))) {
	        	    		GenericValue ProductFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.getString("productFeatureCategoryId")));
	        	    		description = description + ProductFeatureCategory.get("description") + ":" + systemproductFeature.get("description") + " ";
	        			}
	        		}
	        	}	        	        	
	        }
	        
	        if (UtilValidate.isNotEmpty(product.get("productWidth"))) {
	        	BigDecimal productWidth = (BigDecimal) product.get("productWidth");	        	
	        	description += " 门幅:" + productWidth.doubleValue();
	        }
	        product.set("description", description);
	        delegator.store(product);
        } catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return description;
    }
    
    /**
     * 为虚拟商品创建变型商品
     * @author jiaqi.Dai
     * @param request
     * @param productId 虚拟商品ID
     * @param productCategoryId 分类ID
     * @param comments 备注
     * @param productZuczugId 输入的素然物料编号
     * @param response
     * @return
     */
    public static String createVariantProductByVirtualProduct(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        DispatchContext dctx = dispatcher.getDispatchContext();
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String productId = request.getParameter("productId");
        String productCategoryId = request.getParameter("productCategoryId");
        String comments = request.getParameter("comments");
        String newVariantProductId = "";
        List<String> vartualProductFeatures = new ArrayList<String>();
        String zuczugId = request.getParameter("productZuczugId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String description = "";
        GenericValue variantProduct = null;
    	try {
	        if (UtilValidate.isNotEmpty(productId)) {
	        	List<GenericValue> productFeatures = delegator.findByAnd("ProductFeatureAppl", 
						UtilMisc.toMap("productId", productId,"productFeatureApplTypeId", "STANDARD_FEATURE"));
	        	
	        	GenericValue virtualProduct;			
				virtualProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
				GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId",productCategoryId));
				if (virtualProduct.get("productTypeId").equals("FINISHED_GOOD")) {
					newVariantProductId = request.getParameter("virtualRealProductId");
				} else if (virtualProduct.get("productTypeId").equals("RAW_MATERIAL")) {
					newVariantProductId = delegator.getNextSeqId("Product");
				}
				
				// 获取这个productCategoryId的所有productFeatureGroup
		        List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
		        List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
		        
		        GenericValue virtualGoodIdentification = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("productId", productId,"goodIdentificationTypeId","ZUCZUG_CODE"));
		        
		        if ( UtilValidate.isNotEmpty(virtualGoodIdentification) && virtualGoodIdentification.get("idValue").equals(zuczugId) || UtilValidate.isEmpty(zuczugId)) {
		        	if (UtilValidate.isNotEmpty(productCategory.get("description"))) {
						zuczugId = productCategory.get("description") + "-" + newVariantProductId;
			        } else {
			        	zuczugId = delegator.getNextSeqId("Product");
			        }
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
				
				result = dispatcher.runSync("checkProductRepeatByFeatureInVirtual", UtilMisc.toMap("productFeatures", vartualProductFeatures, "viratualProductId", productId));
				//去重
				if(!result.get("checkProduct").equals("noRepeate")) {
					newVariantProductId = (String) result.get("checkProduct");
					
					GenericValue good = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("productId",newVariantProductId, "goodIdentificationTypeId","ZUCZUG_CODE"));
					
					GenericValue pa = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productIdTo", newVariantProductId)));
	        		if (UtilValidate.isEmpty(pa.get("thruDate"))) {
	        			if (virtualProduct.get("productTypeId").equals("RAW_MATERIAL")) {
	        				String errorMsg = "希望转换的变形商品已存在,编号为:" + good.get("idValue");
		        			request.setAttribute("_ERROR_MESSAGE_", errorMsg);
							return "error";
	        			} else if (virtualProduct.get("productTypeId").equals("FINISHED_GOOD")) {
	        				String errorMsg = (String) result.get("successMessage");
		        			request.setAttribute("_ERROR_MESSAGE_", errorMsg);
							return "error";
	        			}
	        			
	        		} else {
	        			dispatcher.runSync("updateProductAssoc", UtilMisc.toMap(
	        					"productId", productId,
	        					"productIdTo", newVariantProductId,
	        					"productAssocTypeId", "PRODUCT_VARIANT",
	        					"fromDate", pa.get("fromDate"),
	        					"thruDate", null,
	        					"userLogin", userLogin));	        			
	        		}
					
				} else {
					//按照虚拟商品复制一个商品
					dispatcher.runSync("createProduct", UtilMisc.toMap("userLogin", userLogin, 
							"productId", newVariantProductId,
							"productName", virtualProduct.get("productName"),
							"internalName", virtualProduct.get("internalName"),
							"productTypeId", virtualProduct.get("productTypeId"),
							"quantityUomId", virtualProduct.get("quantityUomId"),
							"widthUomId", "LEN_cm"
							));
					
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
					variantProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", newVariantProductId));
					//设定为变型商品
					variantProduct.set("isVariant", "Y");
					variantProduct.set("isVirtual", "N"); 
					dispatcher.runSync("copyProductCategoryMemberToProduct", UtilMisc.toMap("productIdFrom", productId, "productIdTo", newVariantProductId));
				}
		        for (GenericValue productFeatureCatGrpAppl : productFeatureCatGrpAppls) {
		        	String productFeatureId = request.getParameter(productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		        	if (UtilValidate.isNotEmpty(productFeatureId)) {
		        		if (UtilValidate.isNotEmpty(variantProduct)) {
		        			dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", newVariantProductId, "productFeatureId", productFeatureId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
		        		}
		        		
			        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureId));
		        		GenericValue ProductFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", UtilMisc.toMap("productFeatureGroupId", productFeatureCatGrpAppl.getString("productFeatureGroupId")));
		        		description = description + ProductFeatureGroup.get("description") + ":" + productFeature.get("description") + " ";
		        	}			       	
		        }
		        
		        for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
		        	String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
		        	if (!productFeatureCategoryId.equals("NONE")) {
		        		List<GenericValue> productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, productId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
		        		if (UtilValidate.isNotEmpty(variantProduct)) {
		        			dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", newVariantProductId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "STANDARD_FEATURE", "userLogin", userLogin));
		        		}		        		
			        	List<GenericValue> featureList = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "SELECTABLE_FEATURE"));
			        	if(UtilValidate.isEmpty(featureList)) {
			        		dispatcher.runSync("applyFeatureToProduct", UtilMisc.toMap("productId", productId, "productFeatureId", productFeatureCategoryId, "productFeatureApplTypeId", "SELECTABLE_FEATURE", "userLogin", userLogin));
			        	}				        	
			        	GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureCategoryId));
		        		GenericValue ProductFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", UtilMisc.toMap("productFeatureCategoryId", productFeatureCategoryAppl.getString("productFeatureCategoryId")));
		        		description = description + ProductFeatureCategory.get("description") + ":" + productFeature.get("description") + " ";
		        	}
		        }
		        if (UtilValidate.isNotEmpty(variantProduct)) {
		        	 variantProduct.set("description", description);
				     variantProduct.set("comments", comments);
					 delegator.store(variantProduct);
		        }
		       
				List<GenericValue> pas = delegator.findByAnd("ProductAssoc",UtilMisc.toMap(
						"productAssocTypeId","PRODUCT_VARIANT",
						"productId", productId,
						"productIdTo", newVariantProductId
						));
				//添加关联
				if (UtilValidate.isEmpty(pas)) {
					dispatcher.runSync("createProductAssoc", UtilMisc.toMap(
							"userLogin", userLogin,
							"productAssocTypeId", "PRODUCT_VARIANT", 
							"productId", productId, 
							"productIdTo", newVariantProductId, 
							"fromDate", UtilDateTime.nowTimestamp()));
				}
				
			}
			dispatcher.runSync("copyProductInfoToProduct", UtilMisc.toMap(
					"productIdFrom", productId,
					"productIdTo", newVariantProductId,
					"userLogin", userLogin));	
			
			return newVariantProductId;    
    	} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return "error";
			
    }
    
    public static String createVariantProductToBom(HttpServletRequest request, HttpServletResponse response) {
    	Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        DispatchContext dctx = dispatcher.getDispatchContext();
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String bomId = request.getParameter("variantProductId");
        String productId = request.getParameter("productId");
        String instruction = request.getParameter("instruction").toString();
        String quantityStr = "";
        if (UtilValidate.isEmpty(request.getParameter("quantity"))) {
        	quantityStr = "0";
        } else {
        	quantityStr = request.getParameter("quantity");
        }
        BigDecimal quantity = new BigDecimal(quantityStr);     
        Timestamp fromDate = Timestamp.valueOf(request.getParameter("fromDate"));//转换时间字符串为Timestamp
        try {
        	String newVariantProductId = createVariantProductByVirtualProduct(request, response);
        	if ("error".equals(newVariantProductId)) {        		
        		return "error";
        	}
			dispatcher.runSync("createProductAssoc", UtilMisc.toMap(
					"productId", bomId,
					"productIdTo", newVariantProductId,
					"instruction", instruction,
					"productAssocTypeId", "ENGINEER_COMPONENT",
					"fromDate", UtilDateTime.nowTimestamp(),
					"quantity", quantity,
					"userLogin", userLogin
					));
			GenericValue oldProductAssoc = delegator.findByPrimaryKey("ProductAssoc", UtilMisc.toMap(
					"productId", bomId,
					"productIdTo", productId,
					"productAssocTypeId", "ENGINEER_COMPONENT",
					"fromDate", fromDate
					));
			oldProductAssoc.set("thruDate", UtilDateTime.nowTimestamp());
			delegator.store(oldProductAssoc);
			return "success";
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        request.setAttribute("_ERROR_MESSAGE_", "加入Bom时发生未知错误，请联系IT部！");
        return "error";
    }

	// 根据某个productId，获取它生产Bom所有原料的供应商，返回的是partyId的Set。productId可以是虚拟的，也可以是非虚拟的
    public static Set<String> getMatSupplierIdsByProductId(Delegator delegator, String productId) throws GenericEntityException {
		Set<String> supplierIds = new HashSet();
		GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
		// 如果是虚拟商品，还需要找到它的变型
		if ("Y".equals(product.getString("isVirtual"))) {
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_VARIANT"));
			productAssocs = EntityUtil.filterByDate(productAssocs);
			for (GenericValue productAssoc : productAssocs) {
				Debug.logInfo("+++++++++++++++++++++++++++++++++++++ " + productAssoc.getString("productIdTo"), module);
				Set<String> moreSupplierIds = getMatSupplierIdsBySkuProductId(delegator, productAssoc.getString("productIdTo"));
				supplierIds.addAll(moreSupplierIds);
			}
		} else {
			Set<String> moreSupplierIds = getMatSupplierIdsBySkuProductId(delegator, productId);
			supplierIds.addAll(moreSupplierIds);
		}
		return supplierIds;
	}

	// 根据某个sku的productId，获取它生产Bom所有原料的供应商，返回的是partyId的Set。productId是非虚拟的
	public static Set<String> getMatSupplierIdsBySkuProductId(Delegator delegator, String productId) throws GenericEntityException {
		Set<String> supplierIds = new HashSet();
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "MANUF_COMPONENT"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		for (GenericValue productAssoc : productAssocs) {
			Debug.logInfo("=================================================== " + productAssoc.getString("productIdTo"), productId);
			List<GenericValue> supplierProducts = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId", productAssoc.getString("productIdTo")));
			//supplierProducts = EntityUtil.filterByDate(supplierProducts);
			for (GenericValue supplierProduct : supplierProducts) {
				supplierIds.add(supplierProduct.getString("partyId"));
			}
		}
		return supplierIds;
	}

	public static String getProductFabricAccessoriesType(Delegator delegator, String productId) throws GenericEntityException {
		String type="";
		List<GenericValue> fabricAppl = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId",productId,"productFeatureId","FABRIC"));
		List<GenericValue> yarnAppl = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId",productId,"productFeatureId","YARN"));
		List<GenericValue> accAppl = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId",productId,"productFeatureId","ACCESSORIES"));
		
		if(UtilValidate.isNotEmpty(fabricAppl)){
			type = "面料";
		}else if(UtilValidate.isNotEmpty(yarnAppl)){
			type = "纱线";
		}else if(UtilValidate.isNotEmpty(accAppl)){
			GenericValue pro = delegator.findOne("Product", false, UtilMisc.toMap("productId",productId));
			if(UtilValidate.isNotEmpty(pro) && UtilValidate.isNotEmpty(pro.getRelatedOne("PrimaryProductCategory"))){
				type = pro.getRelatedOne("PrimaryProductCategory").getString("categoryName");
			}
		}
		return type;
	}
	
	/**
	 * 根据变型商品的ID获取虚拟商品的商品ID
	 * @author jiaqi.Dai
	 * @param delegator
	 * @param variantProductId 变型商品ID
	 * @return String
	 * @throws GenericEntityException
	 */
	public static String getVirtualProductIdByVariantProductId(
			Delegator delegator, String variantProductId) throws GenericEntityException {
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", variantProductId, "productAssocTypeId", "PRODUCT_VARIANT"), UtilMisc.toList("productId"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		if (UtilValidate.isNotEmpty(productAssocs)) {
			GenericValue productAssoc = EntityUtil.getFirst(productAssocs);
			return productAssoc.getString("productId");
		}
		return null;
	}

	// 根据某个productId和某个供应商partyId，获取它生产Bom所有该供应商供应的原料，返回的是Map（sku，原料productId）。productId可以是虚拟的，也可以是非虚拟的
	public static List<Map> getSupplierMaterialByProductId(Delegator delegator, String partyId, String productId) throws GenericEntityException {
        List<Map> skuMatMaps = FastList.newInstance();
		GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
		// 如果是虚拟商品，还需要找到它的变型
		if ("Y".equals(product.getString("isVirtual"))) {
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_VARIANT"));
			productAssocs = EntityUtil.filterByDate(productAssocs);
			for (GenericValue productAssoc : productAssocs) {
				List<String> materials = getSupplierMaterialBySkuProductId(delegator, partyId, productAssoc.getString("productIdTo"));
				if (UtilValidate.isEmpty(materials)) {
					continue;
				}
				Map<String, List<String>> skuMatMap = UtilMisc.toMap(productAssoc.getString("productIdTo"), materials);
				skuMatMaps.add(skuMatMap);
			}
		} else {
			List<String> materials = getSupplierMaterialBySkuProductId(delegator, partyId, productId);
			Map<String, List<String>> skuMatMap = UtilMisc.toMap(productId, materials);
			skuMatMaps.add(skuMatMap);
		}
		return skuMatMaps;
	}

	// 根据某个sku的productId和某个供应商partyId，获取它生产Bom所有该供应商供应的原料，返回的是原料productId的List。productId是非虚拟的
	private static List<String> getSupplierMaterialBySkuProductId(Delegator delegator, String partyId, String productId) throws GenericEntityException {
        List<String> materialProductIds = FastList.newInstance();
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "MANUF_COMPONENT"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		for (GenericValue productAssoc : productAssocs) {
			List<GenericValue> supplierProducts = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId", productAssoc.getString("productIdTo")));
			supplierProducts = EntityUtil.filterByDate(supplierProducts, UtilDateTime.nowTimestamp(), "availableFromDate", "availableThruDate", true);
			for (GenericValue supplierProduct : supplierProducts) {
				if (supplierProduct.getString("partyId").equalsIgnoreCase(partyId)) {
					if (!materialProductIds.contains(supplierProduct.getString("productId"))) {
						materialProductIds.add(supplierProduct.getString("productId"));
					}
				}
			}
		}
		return materialProductIds;
	}
	
	/**
	 * 新商品管理模块的的创建正式商品
	 * @author jiaqi.Dai
	 * @param request
	 * @param productId 商品ID 去重
	 * @param productName 商品名称
	 * @param quantityUomId 数量单位
	 * @param comments 备注
	 * @param longDescription 描述
	 * @param productCategoryId 商品分类ID
	 * @param groupId 商品组别ID
	 * @param response
	 * @return
	 */
	public static String createRealProduct(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();        
        String productId = request.getParameter("productId");
        String productName = request.getParameter("productName");
        String quantityUomId = request.getParameter("quantityUomId");
        String comments = request.getParameter("comments");
        String longDescription = request.getParameter("longDescription");
        String productCategoryId = request.getParameter("productCategoryId");
        String groupId = request.getParameter("groupId");
        String description = "";
        Pattern p = Pattern.compile("[0-9a-zA-Z-_]*");
        Matcher m = p.matcher(productId);
        boolean b = m.matches();
        if (!b) {
        	request.setAttribute("_ERROR_MESSAGE_", "不可输入除“-”或“_”外的符号和中文字符");
    		return "error";
        }
        
        
        boolean noVirtualNoVarient = true;
        try {
        	List<GenericValue> productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId));

            int count = productFeatureCategoryAppls.size();
            int index = 0;
            for (GenericValue productFeatureCategoryAppl : productFeatureCategoryAppls) {
            	String productFeatureCategoryId = request.getParameter("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"));
            	if (!productFeatureCategoryId.equals("NONE")) {            		
            		index++;
            	}
            }
            if (index > 0 && index != count) {
            	request.setAttribute("_ERROR_MESSAGE_", "可变特征没有选择完全，如想创建虚拟物料请勿选择可变特征");
        		return "error";
            }
        	
        	GenericValue tempProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        	if (UtilValidate.isNotEmpty(tempProduct)) {
        		request.setAttribute("_ERROR_MESSAGE_", "商品编号重复，请确认!");
				return "error";
        	}
        	//创建商品
			dispatcher.runSync("createProduct", UtilMisc.toMap(
						"productId", productId,
						"internalName", productName,
						"productName", productName,
						"isVirtual", "Y",
						"isVariant", "N",
						"productTypeId", "FINISHED_GOOD",
						"comments", comments,
						"longDescription", longDescription,
						"quantityUomId", quantityUomId,
						"primaryProductCategoryId", productCategoryId,
						"userLogin", userLogin
					));
			//添加分类
			dispatcher.runSync("addProductToCategory", UtilMisc.toMap(
					"productId", productId,
					"productCategoryId", productCategoryId,
					"userLogin", userLogin,
					"fromDate", nowTimestamp
					));
			//保存组别
			dispatcher.runSync("saveProductDesignGroup", UtilMisc.toMap(
						"productId", productId,
						"groupId", groupId,
						"userLogin", userLogin
					));
			
	        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId",productId));

			
			// 获取这个productCategoryId的所有productFeatureGroup
	        List<GenericValue> productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", UtilMisc.toMap("productCategoryId", productCategoryId));
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
	        	}  else {
	        		noVirtualNoVarient = false;	        		
	        	}
	        }
	        if (index > 0 && index != count) {
	        	request.setAttribute("_ERROR_MESSAGE_", "可变特征没有选择完全，如想创建虚拟物料请勿选择可变特征");
        		return "error";
	        }
	        product.set("description", description);

        	if(UtilValidate.isEmpty(productFeatureCategoryAppls)) {
	        	product.set("isVirtual", "N");
	        	product.set("isVariant", "N");
	        } else {
	        	if(noVirtualNoVarient) {
		        	product.set("isVirtual", "N");
		        	product.set("isVariant", "N");
		        }
	        }
	        
	        delegator.store(product);
	        request.setAttribute("_EVENT_MESSAGE_", "新增成功,商品编号为:" + productId);
	        return "success";
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return productId;		
	}
}
