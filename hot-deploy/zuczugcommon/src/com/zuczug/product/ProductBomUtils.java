package com.zuczug.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.ofbiz.base.util.Debug;
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
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;


public class ProductBomUtils {

	public static final String module = ProductBomUtils.class.getName();

	// 计算某个面辅料，如果根据成衣requirement，然后通过bom数据，计算得出该面辅料需要多少量，返回值按组别进行分类计算
	public static Map<String, BigDecimal> calculateProductRequirementByBom(LocalDispatcher dispatcher, Delegator delegator, String productId, GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		BigDecimal totalRequiredQuantity = BigDecimal.ZERO;
		Map<String, BigDecimal> groupRequiredQuantity = FastMap.newInstance();
		
		// get all products that has this product as bom
		List<Map<String, Object>> bomOfProducts = getBomOfProducts(dispatcher, delegator, productId, userLogin);
		
		// 获取每个商品的需求量Requirement，条件是INTERNAL，并且是ZUCZUG_CLOTHESFACILITY这个facility的requirement
		for (Map<String, Object> bomOfProduct : bomOfProducts) {
			// 如果是BOM，但是没有数量，那也不用算什么
			if (UtilValidate.isEmpty(bomOfProduct.get("quantity"))) {
				continue;
			}
			// 判断这个商品所属组别
			Map<String, String> productBasicInfo = ZuczugProductUtils.getVariantGroupCategorys(delegator, (String) bomOfProduct.get("productId"));
			if (UtilValidate.isEmpty(productBasicInfo.get("groupId"))) {
				continue;
			}
			groupRequiredQuantity.put(productBasicInfo.get("groupId"), BigDecimal.ZERO);
			BigDecimal groupQuantity = groupRequiredQuantity.get(productBasicInfo.get("groupId"));
			if (UtilValidate.isEmpty(groupQuantity)) {
				groupQuantity = BigDecimal.ZERO;
			}
	        EntityConditionList<EntityExpr> statusCondition = EntityCondition.makeCondition(UtilMisc.toList(
	                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "REQ_CREATED"),
	                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "REQ_APPROVED")), EntityOperator.OR);
	        EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(UtilMisc.toList(
	        		statusCondition,
	                EntityCondition.makeCondition("productId", EntityOperator.EQUALS, bomOfProduct.get("productId")),
	                EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, "ZUCZUG_CLOTHESFACILITY"),
	                EntityCondition.makeCondition("requirementTypeId", EntityOperator.EQUALS, "INTERNAL_REQUIREMENT")),
	                EntityOperator.AND);
	        
			List<GenericValue> requirements = delegator.findList("Requirement", conditions, null, null, null, false);
			for (GenericValue requirement : requirements) {
				// 总的加一下
				totalRequiredQuantity = totalRequiredQuantity.add(requirement.getBigDecimal("quantity").multiply((BigDecimal) bomOfProduct.get("quantity")));
				// 各个组别的加一下
				groupQuantity = groupQuantity.add(requirement.getBigDecimal("quantity").multiply((BigDecimal) bomOfProduct.get("quantity")));
			}
			groupRequiredQuantity.put(productBasicInfo.get("groupId"), groupQuantity);
		}
		// return totalRequiredQuantity;
		return groupRequiredQuantity;
	}

	public static BigDecimal getProductQuoteByBom(LocalDispatcher dispatcher, Delegator delegator, String productId, GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		BigDecimal totalQuoteQuantity = BigDecimal.ZERO;
		
		// get all products that has this product as bom
		List<Map<String, Object>> bomOfProducts = getBomOfProducts(dispatcher, delegator, productId, userLogin);
		
		// 获取每个商品的需求量Quote，条件是PRODUCT_QUOTE
		for (Map<String, Object> bomOfProduct : bomOfProducts) {
			// 如果是BOM，但是没有数量，那也不用算什么
			if (UtilValidate.isEmpty(bomOfProduct.get("quantity"))) {
				continue;
			}
	        EntityConditionList<EntityExpr> statusCondition = EntityCondition.makeCondition(UtilMisc.toList(
	                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "QUO_CREATED"),
	                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "QUO_APPROVED")), EntityOperator.OR);
	        EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(UtilMisc.toList(
	        		statusCondition,
	                EntityCondition.makeCondition("productId", EntityOperator.EQUALS, bomOfProduct.get("productId")),
	                EntityCondition.makeCondition("quoteTypeId", EntityOperator.EQUALS, "PRODUCT_QUOTE")),
	                EntityOperator.AND);
	        
			List<GenericValue> quoteAndItemViews = delegator.findList("QuoteAndItemView", conditions, null, null, null, false);
			for (GenericValue quoteAndItemView : quoteAndItemViews) {
				BigDecimal thisQuantity = quoteAndItemView.getBigDecimal("quantity").multiply((BigDecimal) bomOfProduct.get("quantity"));
				totalQuoteQuantity = totalQuoteQuantity.add(thisQuantity);
				// TODO 不应该光算quantity，还要算损耗率
			}
		}
		return totalQuoteQuantity;
	}

	public static BigDecimal getProductOrderByBom(LocalDispatcher dispatcher, Delegator delegator, String productId, GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		BigDecimal totalOrderQuantity = BigDecimal.ZERO;
		
		// get all products that has this product as bom
		List<Map<String, Object>> bomOfProducts = getBomOfProducts(dispatcher, delegator, productId, userLogin);
		
		// 获取每个商品的需求量Requirement，条件是INTERNAL，并且是ZUCZUG_CLOTHESFACILITY这个facility的requirement
		for (Map<String, Object> bomOfProduct : bomOfProducts) {
			// 如果是BOM，但是没有数量，那也不用算什么
			if (UtilValidate.isEmpty(bomOfProduct.get("quantity"))) {
				continue;
			}
	        EntityConditionList<EntityExpr> statusCondition = EntityCondition.makeCondition(UtilMisc.toList(
	                EntityCondition.makeCondition("itemStatusId", EntityOperator.EQUALS, "ITEM_CREATED"),
	                EntityCondition.makeCondition("itemStatusId", EntityOperator.EQUALS, "ITEM_APPROVED")), EntityOperator.OR);
	        EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(UtilMisc.toList(
	        		statusCondition,
	                EntityCondition.makeCondition("productId", EntityOperator.EQUALS, bomOfProduct.get("productId")),
	                EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER")),
	                EntityOperator.AND);
	        
			List<GenericValue> orderHeaderAndItems = delegator.findList("OrderHeaderAndItems", conditions, null, null, null, false);
			for (GenericValue orderHeaderAndItem : orderHeaderAndItems) {
				totalOrderQuantity = totalOrderQuantity.add(orderHeaderAndItem.getBigDecimal("quantity").multiply((BigDecimal) bomOfProduct.get("quantity")));
				// TODO 不应该光算quantity，还要算损耗率
			}
		}
		return totalOrderQuantity;
	}

	// 获取所有将该product作为BOM的商品。注意多级BOM的情况
	private static List<Map<String, Object>> getBomOfProducts(LocalDispatcher dispatcher, Delegator delegator, String productId, GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		List<Map<String, Object>> bomOfProducts = FastList.newInstance();
        EntityConditionList<EntityExpr> typeCondition = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("productAssocTypeId", EntityOperator.EQUALS, "MANUF_COMPONENT"),
                EntityCondition.makeCondition("productAssocTypeId", EntityOperator.EQUALS, "ENGINEER_COMPONENT")), EntityOperator.OR);
        EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(UtilMisc.toList(
        		typeCondition,
                EntityCondition.makeCondition("productIdTo", EntityOperator.EQUALS, productId)),
                EntityOperator.AND);
        
		List<GenericValue> productAssocs = delegator.findList("ProductAssoc", conditions, null, null, null, false);

		if (UtilValidate.isNotEmpty(productAssocs)) {
			productAssocs = EntityUtil.filterByDate(productAssocs);
		}
		// 有可能许多是设计商品的BOM，所以，要找到对应的正式商品，如果正式商品有BOM，则忽略该设计商品，如果正式商品没有BOM，则需要将该正式商品加进来
		for (GenericValue productAssoc : productAssocs) {
			Map<String, Object> quantityResult = dispatcher.runSync("getProductBomRealQuantity", UtilMisc.toMap("productId", productAssoc.getString("productId"),
																"productIdTo", productAssoc.getString("productIdTo"), "productAssocTypeId", productAssoc.getString("productAssocTypeId"),
																"fromDate", productAssoc.getTimestamp("fromDate"), "userLogin", userLogin));
			BigDecimal realQuantity = (BigDecimal) quantityResult.get("realQuantity");
			Map<String, Object> bomOfProduct = FastMap.newInstance();
			GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productAssoc.getString("productId")));
			if (product.getString("productTypeId").equals("WIP") && productAssoc.getString("productAssocTypeId").equals("ENGINEER_COMPONENT")) { // 如果是设计商品，就要找到对应的正式商品
				List<GenericValue> uniqueAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", product.getString("productId"), "productAssocTypeId", "UNIQUE_ITEM"));
				uniqueAssocs = EntityUtil.filterByDate(uniqueAssocs);
				if (UtilValidate.isNotEmpty(uniqueAssocs)) { // 找到对应的正式商品了
					String finishedProductId = EntityUtil.getFirst(uniqueAssocs).getString("productIdTo");
					List<GenericValue> bomAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", finishedProductId, "productAssocTypeId", "MANUF_COMPONENT"));
					if (UtilValidate.isEmpty(bomAssocs)) { // 如果正式商品没有任何BOM信息，代表设计BOM没有copy到正式的生产BOM，所以，就用设计BOM来代替正式的生产BOM
						bomOfProduct.put("productId", finishedProductId);
						// bomOfProduct.put("quantity", productAssoc.getBigDecimal("quantity"));
						bomOfProduct.put("quantity", realQuantity);
						bomOfProduct.put("scrapFactor", productAssoc.getBigDecimal("scrapFactor"));
						bomOfProducts.add(bomOfProduct);
					}
					// 如果有BOM信息，那等待for到正式BOM，这个设计BOM就要被忽略掉了。
				}
				// 如果没有找到正式商品，代表这个设计商品没有被采用，也就不理他了。
			} else if (product.getString("productTypeId").equals("FINISHED_GOOD") && productAssoc.getString("productAssocTypeId").equals("MANUF_COMPONENT")) { // 如果是正式商品，那原本的productAssoc就是我们要的bom信息
				bomOfProduct.put("productId", productAssoc.getString("productId"));
				// bomOfProduct.put("quantity", productAssoc.getBigDecimal("quantity"));
				bomOfProduct.put("quantity", realQuantity);
				bomOfProduct.put("scrapFactor", productAssoc.getBigDecimal("scrapFactor"));
				bomOfProducts.add(bomOfProduct);
			}
		}
		// TODO 如何处理多级BOM
		return bomOfProducts;
	}
	
	// 获取某个组别下所有的面辅料，传入参数groupnameId是组别的productCategoryId，返回的bom数据过滤掉了quantity为0或者没有的，以及productIdTo为virtual的数据。
	public static List<String> getBomByGroupname(Delegator delegator, String groupnameId) throws GenericEntityException {
		List<String> bomProductIds = FastList.newInstance();
		Set<String> bomProductIdSet = FastSet.newInstance();
		
		// 获取该组别下所有正式商品
		List<String> productIds = ZuczugProductUtils.getVariantProductIdsByGroup(delegator, groupnameId, "FINISHED_GOOD");
		// 获取每个商品的bom，如果该正式商品没有bom，则获取其对应的设计商品的bom
		for (String productId : productIds) {
			List<GenericValue> productAssocs = getProductOrDesignBom(delegator, productId);
			if (UtilValidate.isEmpty(productAssocs)) {
				continue;
			}
			for (GenericValue productAssoc : productAssocs) {
				bomProductIdSet.add(productAssoc.getString("productIdTo"));
			}
		}
		bomProductIds.addAll(bomProductIdSet);
		return bomProductIds;
	}

	// 获取每个商品的bom，如果该正式商品没有bom，则获取其对应的设计商品的bom，传入的是正式商品的变型productId，返回的是ProductAssoc的List
	public static List<GenericValue> getProductOrDesignBom(Delegator delegator, String productId) throws GenericEntityException {
		List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "MANUF_COMPONENT"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		productAssocs = filterNoQuantityAndVirtualBom(productAssocs);
		if (UtilValidate.isNotEmpty(productAssocs)) {
			return productAssocs;
		}
		String designProductId = ZuczugProductUtils.getDesignProductIdByProductId(delegator, productId);
		if (UtilValidate.isEmpty(designProductId)) {
			return null;
		}
		productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", designProductId, "productAssocTypeId", "ENGINEER_COMPONENT"));
		productAssocs = EntityUtil.filterByDate(productAssocs);
		if (UtilValidate.isNotEmpty(productAssocs)) {
			return productAssocs;
		}
		return null;
	}

	private static List<GenericValue> filterNoQuantityAndVirtualBom(List<GenericValue> productAssocs) throws GenericEntityException {
		if (UtilValidate.isEmpty(productAssocs)) {
			return productAssocs;
		}
		List<GenericValue> removedProductAssocs = FastList.newInstance();
		for (GenericValue productAssoc : productAssocs) {
			// 去除quantity为0或者没有quantity数据的productAssoc
			if (UtilValidate.isEmpty(productAssoc.getBigDecimal("quantity")) || productAssoc.getBigDecimal("quantity").equals(BigDecimal.ZERO)) {
				removedProductAssocs.add(productAssoc);
				continue;
			}
			// 去除productIdTo为virtual的productAssoc
			GenericValue product = productAssoc.getRelatedOne("Assoc");
			if ("Y".equals(product.getString("isVirtual"))) {
				removedProductAssocs.add(productAssoc);
			}
		}
		productAssocs.removeAll(removedProductAssocs);
		return productAssocs;
	}
	
	// 获取某个面辅料被哪些组别用过。传入的productId为面辅料的sku
	public static List<String> getGroupUseTheBom (LocalDispatcher dispatcher, Delegator delegator, String productId, String seasonId, GenericValue userLogin) throws GenericEntityException, GenericServiceException {
		List<String> groupIds = FastList.newInstance();
		
		// get all products that has this product as bom
		List<Map<String, Object>> bomOfProducts = getBomOfProducts(dispatcher, delegator, productId, userLogin);

		for (Map<String, Object> bomOfProduct : bomOfProducts) {
			String theProductId = (String) bomOfProduct.get("productId");
			// 判断这个商品所属组别
			Map<String, String> productBasicInfo = ZuczugProductUtils.getVariantGroupCategorys(delegator, theProductId);
			if (UtilValidate.isNotEmpty(seasonId) && seasonId.equals(productBasicInfo.get("seasonId"))) {
				String groupId = productBasicInfo.get("groupId");
				if (!groupIds.contains(groupId)) {
					groupIds.add(groupId);
				}
			}
		}
		return groupIds;
	}

	public static BigDecimal getProductBomRealQuantity(LocalDispatcher dispatcher,GenericValue productAssoc,GenericValue userLogin){
		BigDecimal realQuantity = BigDecimal.ZERO;
		String serviceName = null;
		if (productAssoc != null && productAssoc.getString("estimateCalcMethod") != null) {
	        try {
	            GenericValue genericService = productAssoc.getRelatedOne("CustomMethod");
	            if (genericService != null && genericService.getString("customMethodName") != null) {
	                serviceName = genericService.getString("customMethodName");
	            }
	            
	            if (serviceName != null) {
	                Map<String, Object> resultContext = null;
	                Map<String, Object> arguments = UtilMisc.<String, Object>toMap("neededQuantity", productAssoc.getBigDecimal("quantity"));
	                
	                Map<String, Object> inputContext = UtilMisc.<String, Object>toMap("arguments", arguments, "userLogin", userLogin);
	                try {
	                    resultContext = dispatcher.runSync(serviceName, inputContext);
	                    realQuantity = (BigDecimal)resultContext.get("quantity");
	                    
	                } catch (GenericServiceException e) {
	                    Debug.logError(e, "Problem calling the getManufacturingComponents service", ZuczugProductServices.module);
	                }
	            } else {
	            	realQuantity = productAssoc.getBigDecimal("quantity");
	            }
	        } catch (Exception exc) {
	        	exc.printStackTrace();
	        }
	    }
		
		return realQuantity;
	}
	
	/**
	 * 获取一个生产计划中，某一个步骤，某一个料的所有预计消耗量
	 * @author sven
	 * @param delegator
	 * @param routingGroupId
	 * @param stepId
	 * @param productId
	 * @return
	 */
	public static BigDecimal getRoutingGroupStepBomTotalQuantity(Delegator delegator,String routingGroupId,String stepId,String productId){
		
		BigDecimal totalQuantity = BigDecimal.ZERO;
		try {
			List<GenericValue> productionRunList = delegator.findByAndCache("WorkEffort", UtilMisc.toMap("workEffortParentId",routingGroupId));
			for (GenericValue proRun : productionRunList) {
				//找到这个SKU下面的具体bom
				List<GenericValue> bomList = delegator.findByAnd("WorkEffortTaskAndBOMList", UtilMisc.toMap("workEffortParentId",proRun.getString("workEffortId"),"productId",productId));
				if(UtilValidate.isNotEmpty(bomList)){
					String taskId =  EntityUtil.getFirst(bomList).getString("workEffortId");
					GenericValue assoc = EntityUtil.getFirst(delegator.findByAndCache("WorkEffortAssoc",UtilMisc.toMap("workEffortIdFrom",stepId,"workEffortIdTo",taskId,"workEffortAssocTypeId","WORK_EFF_TEMPLATE")));
					if(UtilValidate.isNotEmpty(assoc)){
						totalQuantity = totalQuantity.add(EntityUtil.getFirst(bomList).getBigDecimal("estimatedQuantity"));
					}
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return totalQuantity;
	}
	
	/**
	 * 找到所有的退料总数
	 * @author sven
	 * @param delegator
	 * @param routingGroupId
	 * @param stepId
	 * @param productId
	 * @return
	 */
	public static BigDecimal getRoutingGroupStepBomReturnQuantity(Delegator delegator,String routingGroupId,String stepId,String productId){
		
		BigDecimal totalReturned = BigDecimal.ZERO;
		try {
			List<GenericValue> productionRunList = delegator.findByAndCache("WorkEffort", UtilMisc.toMap("workEffortParentId",routingGroupId));
			for (GenericValue proRun : productionRunList) {
				//找到这个SKU下面的具体bom
				List<GenericValue> bomList = delegator.findByAnd("WorkEffortTaskAndBOMList", UtilMisc.toMap("workEffortParentId",proRun.getString("workEffortId"),"productId",productId));
				if(UtilValidate.isNotEmpty(bomList)){
					String taskId =  EntityUtil.getFirst(bomList).getString("workEffortId");
					GenericValue assoc = EntityUtil.getFirst(delegator.findByAndCache("WorkEffortAssoc",UtilMisc.toMap("workEffortIdFrom",stepId,"workEffortIdTo",taskId,"workEffortAssocTypeId","WORK_EFF_TEMPLATE")));
					if(UtilValidate.isNotEmpty(assoc)){//如果是的当前的task，就找到退货
						List<GenericValue> returns = delegator.findByAnd("WorkEffortAndInventoryProduced", UtilMisc.toMap("workEffortId",taskId , "productId" , productId));
		                for (GenericValue returned : returns) {
		                    GenericValue returnDetail = EntityUtil.getFirst(delegator.findByAnd("InventoryItemDetail",UtilMisc.toMap("inventoryItemId" ,returned.getString("inventoryItemId")),UtilMisc.toList("inventoryItemDetailSeqId")));
		                    totalReturned= totalReturned.add(returnDetail.getBigDecimal("quantityOnHandDiff"));
		                }
					}
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return totalReturned;
	}
	
	
	public static String matialeAlertMsg(Delegator delegator,String routingGroupId,String stepId){
		
		String msg = "当前步骤需要投入的物料如下：\\n";
		List<GenericValue> totalList = new ArrayList<GenericValue>();
		Map<String,BigDecimal> totalQuantityMap = new HashMap<String,BigDecimal>();
		try {
			List<GenericValue> productionRunList = delegator.findByAndCache("WorkEffort", UtilMisc.toMap("workEffortParentId",routingGroupId));
			for (GenericValue proRun : productionRunList) {
				String productionRunId = proRun.getString("workEffortId");
				List<GenericValue> taskAndStepList = delegator.findByAnd("WorkEffortTaskAndStep", UtilMisc.toMap("workEffortParentId",productionRunId,"workEffortIdFrom",stepId));
				if(UtilValidate.isNotEmpty(taskAndStepList)){
					String currentTaskId = EntityUtil.getFirst(taskAndStepList).getString("workEffortId");
					List<GenericValue> list = delegator.findByAnd("WorkEffortGoodStandard",UtilMisc.toMap("workEffortId",currentTaskId,"workEffortGoodStdTypeId","PRUNT_PROD_NEEDED"));
					totalList.addAll(list);
					
				}
			}
			for (GenericValue gen : totalList) {
				
				String productId=gen.getString("productId");
				if(totalQuantityMap.containsKey(productId)){
					BigDecimal existQuantity =  totalQuantityMap.get(productId);
					existQuantity = existQuantity.add(gen.getBigDecimal("estimatedQuantity"));
					totalQuantityMap.put(productId, existQuantity);
				}else{
					totalQuantityMap.put(productId, gen.getBigDecimal("estimatedQuantity"));
				}
			}
			
			for (Entry<String, BigDecimal> entry : totalQuantityMap.entrySet()) {  
				GenericValue pro = delegator.findOne("Product", true, UtilMisc.toMap("productId",entry.getKey()));
			    msg+="(" + pro.getString("productId") + ")" + pro.getString("internalName") + "，【数量：" + entry.getValue() + "】\\n";
			}  
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		
		return msg;
	}
}
