package com.zuczug.product;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.manufacturing.bom.BOMNode;
import org.ofbiz.manufacturing.bom.BOMTree;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;




public class ZuczugProductServices {
	
	public static final String module = ZuczugProductServices.class.getName();
    public static final String resource = "ZuczugCommonUiLabels";
	
    /**
     * 将虚拟商品所有standard feature拷贝到所有变形商品。如果virtualProductId参数有值，则忽略variantProductId，如果virtualProductId无值，则通过variantProductId来找到virtualProductId
     */
    public static Map<String, Object> copyStandardFeaturesToVariants(DispatchContext dctx, Map<String, ? extends Object> context){
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String virtualProductId = (String) context.get("virtualProductId");
        String variantProductId = (String) context.get("variantProductId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        try{
	        if (UtilValidate.isEmpty(virtualProductId)) {
	        	if (UtilValidate.isNotEmpty(variantProductId)) {
	        		virtualProductId = ZuczugProductUtils.getVirtualProductIdByVariantProductId(delegator, variantProductId);
	        	}
	        }
	        
	        if (UtilValidate.isEmpty(virtualProductId)) { //如果即没有传入virtualProductId参数，且从variantProductId中也获取不到virtualProductId，则直接返回空
	        	return result;
	        }

	        // 获取该虚拟商品的所有变型商品，这些商品是要被传入standard feature的
	        List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"), UtilMisc.toList("productId"));
    		productAssocs = EntityUtil.filterByDate(productAssocs);
    		
    		// 获取虚拟商品的所有standard feature
    		List<GenericValue> productFeatureAndAppls = delegator.findByAnd("ProductFeatureAndAppl",
    				UtilMisc.toMap("productId", virtualProductId, "productFeatureApplTypeId", "STANDARD_FEATURE"));
    		productFeatureAndAppls = EntityUtil.filterByDate(productFeatureAndAppls);
    		
    		// 循环每一个feature，设置到所有的变型商品中
    		for (GenericValue productFeatureAndAppl:productFeatureAndAppls) {
    			String productFeatureId = productFeatureAndAppl.getString("productFeatureId");
    			String productFeatureTypeId = productFeatureAndAppl.getString("productFeatureTypeId");
	    		for (GenericValue productAssoc:productAssocs) {
	    			List<GenericValue> productFeatureAppl = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap(
	    						"productId", productAssoc.getString("productIdTo"),
	    						"productFeatureApplTypeId", "STANDARD_FEATURE",
	    						"productFeatureId", productFeatureId
	    					));
	    			if (UtilValidate.isEmpty(productFeatureAppl)) {
		    			updateProductFeature(delegator, productAssoc.getString("productIdTo"), productFeatureTypeId, productFeatureId);
	    			}
	    		}
    		}
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    // 更新或新增某个商品的某个feature，注意了，这个feature只能是STANDARD_FEATURE,因为同一个类型的STANDARD_FEATURE只会有一个，所以会删除原来老得
    private static void updateProductFeature(Delegator delegator, String productId, String productFeatureTypeId, String productFeatureId)
    		throws GenericEntityException {
        List<GenericValue> productFeatures = delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureTypeId", productFeatureTypeId/*,"productFeatureId", productFeatureId*/));
        productFeatures = EntityUtil.filterByDate(productFeatures);
        // remove the old one with the type
        if (UtilValidate.isNotEmpty(productFeatures)) {
        	//modify by sven ，delete all old feature
        	for (GenericValue productFeatureAndAppl : productFeatures) {
        		//GenericValue productFeatureAndAppl = EntityUtil.getFirst(productFeatures);
            	GenericValue productFeatureAppl = delegator.findByPrimaryKey("ProductFeatureAppl",
            			UtilMisc.toMap("productFeatureId", productFeatureAndAppl.getString("productFeatureId"),
            					"productId", productFeatureAndAppl.getString("productId"), "fromDate", productFeatureAndAppl.getTimestamp("fromDate")));
            	delegator.removeValue(productFeatureAppl);
			}
        } 
        GenericValue productFeatureAppl = delegator.makeValue("ProductFeatureAppl", UtilMisc.toMap("productId", productId,
        			"productFeatureId", productFeatureId, "fromDate", UtilDateTime.nowTimestamp(), "productFeatureApplTypeId", "STANDARD_FEATURE"));
        delegator.createOrStore(productFeatureAppl);
    }

    /** It reads the product's bill of materials,
     * if necessary configures it, and it returns its (possibly configured) components in
     * a List of {@link BOMNode}).
     * @param dctx the distach context
     * @param context the context 
     * @return return the list of manufacturing components
     */
    public static Map<String, Object> getProductComponents(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue)context.get("userLogin");
        String productId = (String) context.get("productId");
        BigDecimal quantity = (BigDecimal) context.get("quantity");
        BigDecimal amount = (BigDecimal) context.get("amount");
        String fromDateStr = (String) context.get("fromDate");
        String productAssocTypeId = (String) context.get("productAssocTypeId");
        Boolean excludeWIPs = (Boolean) context.get("excludeWIPs");
        Locale locale = (Locale) context.get("locale");

        if (quantity == null) {
            quantity = BigDecimal.ONE;
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        Date fromDate = null;
        if (UtilValidate.isNotEmpty(fromDateStr)) {
            try {
                fromDate = Timestamp.valueOf(fromDateStr);
            } catch (Exception e) {
            }
        }
        if (fromDate == null) {
            fromDate = new Date();
        }
        if (excludeWIPs == null) {
            excludeWIPs = Boolean.TRUE;
        }

        //
        // Components
        //
        BOMTree tree = null;
        List<BOMNode> components = FastList.newInstance();
        try {
            tree = new BOMTree(productId, productAssocTypeId, fromDate, BOMTree.EXPLOSION_SINGLE_LEVEL, delegator, dispatcher, userLogin);
            tree.setRootQuantity(quantity);
            tree.setRootAmount(amount);
            tree.print(components, excludeWIPs.booleanValue());
            if (components.size() > 0) components.remove(0);
        } catch (GenericEntityException gee) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "ManufacturingBomErrorCreatingBillOfMaterialsTree", UtilMisc.toMap("errorString", gee.getMessage()), locale));
        }
        //
        // Product routing
        //
        String workEffortId = null;
        try {
            Map<String, Object> routingInMap = UtilMisc.toMap("productId", productId, "ignoreDefaultRouting", "Y", "userLogin", userLogin);
            Map<String, Object> routingOutMap = dispatcher.runSync("getProductRouting", routingInMap);
            GenericValue routing = (GenericValue)routingOutMap.get("routing");
            if (routing == null) {
                // try to find a routing linked to the virtual product
                routingInMap = UtilMisc.toMap("productId", tree.getRoot().getProduct().getString("productId"), "userLogin", userLogin);
                routingOutMap = dispatcher.runSync("getProductRouting", routingInMap);
                routing = (GenericValue)routingOutMap.get("routing");
            }
            if (routing != null) {
                workEffortId = routing.getString("workEffortId");
            }
        } catch (GenericServiceException gse) {
            Debug.logWarning(gse.getMessage(), module);
        }
        if (workEffortId != null) {
            result.put("workEffortId", workEffortId);
        }
        result.put("components", components);

        // also return a componentMap (useful in scripts and simple language code)
        List<Map<String, Object>> componentsMap = FastList.newInstance();
        for(BOMNode node : components) {
            Map<String, Object> componentMap = FastMap.newInstance();
            componentMap.put("product", node.getProduct());
            componentMap.put("quantity", node.getQuantity());
            componentsMap.add(componentMap);
        }
        result.put("componentsMap", componentsMap);
        return result;
    }
    
    // 根据组别的productCategoryId来获取该组别下所有虚拟商品ID。其中，带入的productTypeId参数如果没有，就代表所有WIP和FINISHED_GOOD
    public static Map<String, Object> getVirtualProductIdsByGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
        String productFeatureId = (String) context.get("productFeatureId");
        String productTypeId = (String) context.get("productTypeId");
		try {
	        List<String> productIds = ZuczugProductUtils.getVirtualProductIdsByGroup(delegator, productFeatureId, productTypeId);
			result.put("productIds", productIds);
			return result;
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}

	// 根据组别的productFeatureId来获取该组别下所有变型商品ID。其中，带入的productTypeId参数如果没有，就代表所有WIP和FINISHED_GOOD
    // 注意：变型商品是属于哪个组别的逻辑，并不是变型商品本身是否有这个组别的productFeatureId，而是看这个变型商品对应的虚拟商品，是否有这个组别的productFeatureId
    public static Map<String, Object> getVariantProductIdsByGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
        String productFeatureId = (String) context.get("productFeatureId");
        String productTypeId = (String) context.get("productTypeId");
		try {
	        List<String> productIds = ZuczugProductUtils.getVariantProductIdsByGroup(delegator, productFeatureId, productTypeId);
			result.put("productIds", productIds);
			return result;
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}

    // 根据设计商品ID，获取正式商品ID。传入的productId可以是虚拟的，也可以是变型的，传入虚拟返回虚拟，传入变型返回变型
    public static Map<String, Object> getProductIdByDesignProductId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
        String designProductId = (String) context.get("designProductId");
		try {
	        String productId = ZuczugProductUtils.getProductIdByDesignProductId(delegator, designProductId);
	        if (UtilValidate.isNotEmpty(productId)) {
				result.put("productId", productId);
	        }
			return result;
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}

    // 根据正式商品ID，获取设计商品ID。传入的productId可以是虚拟的，也可以是变型的，传入虚拟返回虚拟，传入变型返回变型
    public static Map<String, Object> getDesignProductIdByProductId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        // LocalDispatcher dispatcher = dctx.getDispatcher();
        String productId = (String) context.get("productId");
		try {
	        String designProductId = ZuczugProductUtils.getDesignProductIdByProductId(delegator, productId);
	        if (UtilValidate.isNotEmpty(designProductId)) {
				result.put("designProductId", designProductId);
	        }
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
	}
    
    /**
     * 查询某个虚拟商品下是否有完全一致特征的物料
     * 逻辑上主要是通过 查询出关联关系的集合 去遍历匹配 相同则删除 最后来判断是否重复的
     * @author jiaqi.Dai
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> checkProductRepeatByFeature(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> checkProduct = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<String> productFeatures = (List) context.get("productFeatures");
        String productId = (String) context.get("productId");
        int sameCount = 0;
    	try {     		
    		if (productFeatures.get(0).trim().equals("") && productFeatures.get(1).trim().equals("") ) {
    			productFeatures.remove(0);
    			productFeatures.remove(0);
    		}
			//查询用户所选择的虚拟商品所有对应的变形商品
			List<GenericValue> varinatProducts = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId));
			//
			List<GenericValue> virtualProductFeatures = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", productId));
			
			for (int i = 0; i < virtualProductFeatures.size(); i++) {
				if (virtualProductFeatures.get(i).get("productFeatureApplTypeId").equals("STANDARD_FEATURE")) {
					productFeatures.add((String)virtualProductFeatures.get(i).get("productFeatureId"));
				}
			}
			for (int i = 0; i < productFeatures.size(); i++) {
                for (int j = productFeatures.size() - 1 ; j > i; j--) {
                	if(productFeatures.get(i) != null && productFeatures.get(j) != null) {
	                    if (productFeatures.get(i).equals(productFeatures.get(j))) {
	                    	productFeatures.remove(j);
	                    }
	                    if (productFeatures.get(i).trim().equals("")) {
	                    	productFeatures.remove(i);
	                    }
                	}
                    

                }
            }
			
			for (int i = 0; i < varinatProducts.size(); i++) {
				//查询变形商品所有的feature
				List<GenericValue> featureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId",varinatProducts.get(i).get("productIdTo")));
				//循环变形商品的feature
				sameCount = 0;
				for (int j = 0; j < featureAppls.size(); j++) {
					//循环用户所选择的feature
					for (int k = 0; k < productFeatures.size(); k++) {
						//把这些Feature与用户选择的作对比
						if (featureAppls.get(j).get("productFeatureId").equals(productFeatures.get(k))) {
							//比对一致则记数
							sameCount++;
							break;
						}
						
					}   
				}
				//如果记数数等译特征数量 表示特征完全一致 不等于 就不一致    				
				if (sameCount != productFeatures.size()) {
						varinatProducts.remove(i);
						i--;	   						
				} else if (productFeatures.size() != featureAppls.size()) {
						varinatProducts.remove(i);
						i--;
				}
				
			}
			if (varinatProducts.size() == 0) {
				checkProduct = ServiceUtil.returnSuccess("新增成功！");
				checkProduct.put("checkProduct", "noRepeate");
			} else {
				//删完还有1个说明重复了 把重复的商品ID返回出去
				if (varinatProducts.size() == 1) {
					checkProduct = ServiceUtil.returnSuccess("查询到数据库中存在此商品，商品ID为：" + varinatProducts.get(0).get("productIdTo"));
					checkProduct.put("checkProduct", varinatProducts.get(0).get("productIdTo"));
				} else {
					//删完了还有多个 说明出问题了
					String str = "";
					for (int i = 0; i < varinatProducts.size(); i++) {
						str +=" / " + varinatProducts.get(i).get("productIdTo") + " / ";
					}
					checkProduct = ServiceUtil.returnError("查询到多个重复商品,ID为：" + str + ",请联系IT部门处理此数据！");
				}
			}
    		
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return checkProduct;
    }
    /**
     * 新逻辑下 查询某个虚拟物料中是否存在相同特征的变型物料
     * @author jiaqi.Dai
     * @param dctx
     * @param context
     * @return 如果重复就返回重复物料ID 不重复 返回字符串“noRepeate”
     */
    public static Map<String, Object> checkProductRepeatByFeatureInVirtual(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> checkProduct = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<String> productFeatures = (List<String>) context.get("productFeatures");
        String viratualProductId = (String) context.get("viratualProductId");
        
        try {
        	List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap(
        				"productId", viratualProductId,
        				"productAssocTypeId", "PRODUCT_VARIANT"
        			));
	        int count = productFeatures.size();
	        int index = 0;
	        //查询所有变形商品
        	for (GenericValue productAssoc : productAssocs) {
        		//查询变形商品的特征
        		List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", productAssoc.get("productIdTo"),"productFeatureApplTypeId", "STANDARD_FEATURE"));
        		for (GenericValue productFeatureAppl : productFeatureAppls) {
    				for (String productFeature : productFeatures) {
    					//比较是否一致
    					if (productFeature.equals(productFeatureAppl.get("productFeatureId"))) {
    						index++;
    						break;
    					} 
    				}
    			}
        		if (index == count) {
        			checkProduct = ServiceUtil.returnSuccess("查询到数据库中存在此商品，商品ID为：" + productAssoc.get("productIdTo"));
					checkProduct.put("checkProduct", productAssoc.get("productIdTo"));
					return checkProduct;
        		}
        		index = 0;
        	}
        	checkProduct.put("checkProduct", "noRepeate");
        } catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return checkProduct;
    }
    
    /**
     * 查询在一个类别中是否存在相同特征的辅料
     * @author jiaqi.Dai
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> checkProductFeatureRepeatByCatagory(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> checkProduct = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<String> productFeatures = (List<String>) context.get("productFeatures");
        String productCategoryId = (String) context.get("productCategoryId");
        String productTypeId = (String) context.get("productTypeId");
        List<GenericValue> RepeatProduct = new ArrayList<GenericValue>();
        boolean hit = false;
        int sameCount = 0;
    	try {     		
			//查询用户所选择的虚拟商品所有对应的变形商品
    		List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("primaryProductCategoryId",EntityOperator.EQUALS, productCategoryId ));
				exprs.add(EntityCondition.makeCondition("isVariant", EntityOperator.EQUALS, "N"));
				exprs.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, productTypeId));
				List<GenericValue> products = delegator.findList("Product", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);	
//			List<GenericValue> products = delegator.findByAnd("Product", UtilMisc.toMap("primaryProductCategoryId", productCategoryId,"isVirtual" ,"Y"));
			
			for (GenericValue product : products) {
				List<GenericValue> selectProductFeatures =  delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", product.get("productId")));
				for (int i = 0; i < selectProductFeatures.size(); i++) {
					for (int j = 0; j < productFeatures.size(); j++) {
						if (selectProductFeatures.get(i).get("productFeatureId").equals(productFeatures.get(j))) {
							sameCount++;
							break;
						}
					}
				}
				if (sameCount != productFeatures.size()) {
					products.remove(product);
				}
				sameCount = 0;
			}
			checkProduct.put("checkProduct", products);
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return checkProduct;
    }
    
    /**
     * 把某个组别的所有特征关联给另一个特征组别
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getFeatureToApplByGroupName(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> checkProduct = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String productFeatureGroupId = (String) context.get("productFeatureGroupId");
        String targetProductFeatureGroupId = (String) context.get("targetProductFeatureGroupId");
        String newProductFeatureTitle = (String) context.get("newProductFeatureTitle");
        try {
			List<GenericValue> featureList = delegator.findByAnd("ProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", productFeatureGroupId));
			for (int i = 0; i < featureList.size(); i++) {
				GenericValue newFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", newProductFeatureTitle + featureList.get(i).get("productFeatureId")));
				if (UtilValidate.isEmpty(newFeature)) {
					GenericValue feature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureList.get(i).get("productFeatureId")));
					dispatcher.runSync("createProductFeature", UtilMisc.toMap(
							"productFeatureId", newProductFeatureTitle + feature.get("productFeatureId"),
							"description", feature.get("description"),
							"productFeatureTypeId", feature.get("productFeatureTypeId"),
							"idCode", featureList.get(i).get("idCode"),
							"abbrev", featureList.get(i).get("abbrev"),
							"userLogin", context.get("userLogin")
							));
					dispatcher.runSync("createProductFeatureGroupAppl", UtilMisc.toMap("productFeatureGroupId", targetProductFeatureGroupId,
							"productFeatureId", newProductFeatureTitle + feature.get("productFeatureId"),
							"userLogin", context.get("userLogin")));
				}				
			}
			
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
		return checkProduct;    	
    }
    
    /**
     * 查询在所有在虚拟商品的变型商品中特征完全一样的
     * @author jiaqi.Dai
     * @param dctx
     * @param context
     * @return Product List
     */
    public static Map<String, Object> findFeatureRepeatProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> checkProduct = ServiceUtil.returnSuccess();
    	List<String> tempList = new ArrayList<String>();
    	Map<String, Object> str = new HashMap<String, Object>();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> productFeatures =null;
        LocalDispatcher dispatcher = dctx.getDispatcher();
        int count = 0;
        String retStr = "";
        String temp ="";
        try {
			List<GenericValue> products = delegator.findByAnd("Product", UtilMisc.toMap("isVariant", "N"));
			for (int i = 0; i < products.size(); i++) {
				if(!"ACCE_ZIPPER".equals(products.get(i).get("primaryProductCategoryId"))) {
					tempList.add((String)products.get(i).get("productId"));
				}				
			}
			HashSet set = new HashSet(tempList);
			tempList.clear();
			tempList.addAll(set);
			//查询虚拟商品
			for (int i = 0; i < tempList.size(); i++) {
				String productId = (String) tempList.get(i); 
				//按照商品ID查询Assoc
				List<GenericValue> tempProductAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId,"productAssocTypeId", "PRODUCT_VARIANT"));
				for (int j = 0; j < tempProductAssocs.size(); j++) {
					retStr = "";
					//查询Assoc的商品所拥有的Feature
					for (int k = tempProductAssocs.size() - 1; k > j ; k--) {
						List<GenericValue> featureAppls = delegator.findByAnd("ProductFeatureAppl",
								UtilMisc.toMap("productId",tempProductAssocs.get(j).get("productIdTo")));
						List<GenericValue> tempFeatureAppls = delegator.findByAnd("ProductFeatureAppl",
								UtilMisc.toMap("productId",tempProductAssocs.get(k).get("productIdTo")));
						count = 0;
						for (int l = 0; l < featureAppls.size(); l++) {							
							for (int m = 0; m < tempFeatureAppls.size(); m++) {
									if(featureAppls.get(l).get("productFeatureId").equals(tempFeatureAppls.get(m).get("productFeatureId"))) {									
										count++;										
									}
															
							}
						}
						if (featureAppls.size() == count) {
							if (featureAppls.size() == tempFeatureAppls.size()) {
								if (tempProductAssocs.get(k).get("productIdTo").toString().length() <= 6 && tempProductAssocs.get(j).get("productIdTo").toString().length() <= 6) {
									retStr += tempProductAssocs.get(k).get("productIdTo") + "|" + (String)tempProductAssocs.get(j).get("productIdTo") + "|";
								}
								
								
								tempProductAssocs.remove(k);
							}							
						}
					}
					if (!retStr.equals("")) {
						retStr += "[OVER]";
						str.put(productId, retStr);
					}
					
				}
				
			}
			checkProduct.put("checkProduct", str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.getLocalizedMessage();
			e.getMessage();
			e.printStackTrace();
		}    	
    	return checkProduct;
    }
    
    /**
     * 获取更新虚拟商品时新的虚拟商品的描述信息
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getUpdateProductDescription(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> description = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String variationProductId = (String) context.get("variationProductId");
        String newDescription = (String) context.get("newDescription");
        try {
			GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",variationProductId));
			String oldDescription = (String) product.get("description");
			description.put("description", oldDescription);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return description;    
    }
    
    /**
     * 为没有供应商数据的变型商品设定供应商数据 （FixData用）
     * @author jiaqi.Dai
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> setProductOfNoSupplierProductData(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String, Object> description = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
    	LocalDispatcher dispatcher = dctx.getDispatcher();
    	String successId = "";
    	String errorId = "";
    	try {
    		//查询所有非虚拟商品
			List<GenericValue> variantProducts = delegator.findByAnd("Product", UtilMisc.toMap("isVirtual", "N","productTypeId","RAW_MATERIAL"));
			for (GenericValue variantProduct : variantProducts) {
				if ("ACCE_ZIPPER".equals(variantProduct.get("primaryProductCategoryId"))) {
					String str = setZipperProductOfNoSupplierProductData(variantProduct,delegator,context,dispatcher);
					errorId += str;
					continue;
				}
				List<GenericValue> variantSupplierProducts = delegator.findByAnd("SupplierProduct", 
						UtilMisc.toMap("productId", variantProduct.get("productId")));
				//存在SupplierProduct数据直接退出
				if (!UtilValidate.isEmpty(variantSupplierProducts)) {
					continue;
				}
				//查询他的虚拟商品，找到虚拟商品的SupplierProduct数据复制给非虚拟商品
				List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", 
						UtilMisc.toMap("productIdTo", variantProduct.get("productId"),"productAssocTypeId", "PRODUCT_VARIANT"));
				//没找到关联的虚拟商品直接退出
				if (UtilValidate.isEmpty(productAssocs)) {
					errorId += "|没有找到关联的虚拟产品：" + variantProduct.get("productId");
					continue;					
				}
				GenericValue productAssoc = EntityUtil.getFirst(productAssocs);
				//查询虚拟商品的SupplierProduct数据
				List<GenericValue> virtualSupplierProducts = delegator.findByAnd("SupplierProduct", 
						UtilMisc.toMap("productId", productAssoc.get("productId")));
				//在虚拟商品没有找到数据
				if (UtilValidate.isEmpty(virtualSupplierProducts)) {										
					errorId += "|此虚拟商品没有SupplierProduct数据:" + productAssoc.get("productId");
					continue;															
				}
				for (GenericValue virtualSupplierProduct : virtualSupplierProducts) {
					//根据查询出的虚拟商品的SupplierProduct数据创建新的SupplierProduct productId为变形商品					
					dispatcher.runSync("createSupplierProduct", 
							UtilMisc.toMap("agreementId", virtualSupplierProduct.get("agreementId"),
							"agreementItemSeqId", virtualSupplierProduct.get("agreementItemSeqId"),
							"availableFromDate", virtualSupplierProduct.get("availableFromDate"),
							"availableThruDate", virtualSupplierProduct.get("availableThruDate"),
							"canDropShip", virtualSupplierProduct.get("canDropShip"),
							"comments", virtualSupplierProduct.get("comments"),
							"currencyUomId", virtualSupplierProduct.get("currencyUomId"),
							"lastPrice", virtualSupplierProduct.get("lastPrice"),
							"minimumOrderQuantity", virtualSupplierProduct.get("minimumOrderQuantity"),
							"orderQtyIncrements", virtualSupplierProduct.get("orderQtyIncrements"),
							"partyId", virtualSupplierProduct.get("partyId"),
							"productId", variantProduct.get("productId"),
							"quantityUomId", virtualSupplierProduct.get("quantityUomId"),
							"shippingPrice", virtualSupplierProduct.get("shippingPrice"),
							"standardLeadTimeDays", virtualSupplierProduct.get("standardLeadTimeDays"),
							"supplierPrefOrderId", virtualSupplierProduct.get("supplierPrefOrderId"),
							"supplierProductId", virtualSupplierProduct.get("supplierProductId"),
							"supplierProductName", virtualSupplierProduct.get("supplierProductName"),
							"supplierRatingTypeId", virtualSupplierProduct.get("supplierRatingTypeId"),
							"unitsIncluded", virtualSupplierProduct.get("unitsIncluded"),
							"userLogin", context.get("userLogin")));
										
				}
				
				//成功完成的ID 方便核对
				successId += "|" + variantProduct.get("productId");
			}				
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	description.put("successId", successId);
    	description.put("errorId", errorId);
    	return description; 
    }
    
    private static String setZipperProductOfNoSupplierProductData(GenericValue variantProduct, Delegator delegator, Map<String, ? extends Object> context, LocalDispatcher dispatcher) {
    	String errorId = "";
    	List<GenericValue> variantSupplierProducts;
		try {
			variantSupplierProducts = delegator.findByAnd("SupplierProduct", 
					UtilMisc.toMap("productId", variantProduct.get("productId")));
			//存在SupplierProduct数据直接退出
			if (UtilValidate.isNotEmpty(variantSupplierProducts)) {
				return "";
			}
			//查询中层的虚拟商品，找到虚拟商品的SupplierProduct数据复制给下层非虚拟商品
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", 
					UtilMisc.toMap("productIdTo", variantProduct.get("productId"),"productAssocTypeId", "PRODUCT_VARIANT"));
			//没找到关联的虚拟商品直接退出循环
			if (UtilValidate.isEmpty(productAssocs)) {
				errorId += "|没有找到关联的虚拟产品：" + variantProduct.get("productId");
				return errorId;					
			}
			GenericValue productAssoc = EntityUtil.getFirst(productAssocs);
			//查询虚拟商品的SupplierProduct数据
	    	List<GenericValue> zipperVariantSupplierProducts = delegator.findByAnd("SupplierProduct", 
					UtilMisc.toMap("productId", productAssoc.get("productId")));
	    	//中层没有找到SupplierProduct数据
	    	if (UtilValidate.isEmpty(zipperVariantSupplierProducts)) {	
	    		//查询最上层拉链数据
				List<GenericValue> zipperProductAssocs = delegator.findByAnd("ProductAssoc", 
						UtilMisc.toMap("productIdTo", productAssoc.get("productId"),"productAssocTypeId", "PRODUCT_VARIANT"));						
				//没有最上层就结束
				if (UtilValidate.isNotEmpty(zipperProductAssocs)) {
					GenericValue zipperProductAssoc = EntityUtil.getFirst(zipperProductAssocs);
					List<GenericValue> firstZipperVariantSupplierProducts = delegator.findByAnd("SupplierProduct", 
							UtilMisc.toMap("productId", zipperProductAssoc.get("productId")));
					if (UtilValidate.isNotEmpty(firstZipperVariantSupplierProducts)) {
						//查询拉链虚拟商品
						List<GenericValue> zipperSupplierProducts = delegator.findByAnd("SupplierProduct", 
								UtilMisc.toMap("productId", zipperProductAssoc.get("productId")));					
						for (GenericValue virtualSupplierProduct : zipperSupplierProducts) {
							//根据查询出的虚拟商品的SupplierProduct数据创建新的SupplierProduct productId为中层的商品							
							dispatcher.runSync("createSupplierProduct", 
									UtilMisc.toMap("agreementId", virtualSupplierProduct.get("agreementId"),
									"agreementItemSeqId", virtualSupplierProduct.get("agreementItemSeqId"),
									"availableFromDate", virtualSupplierProduct.get("availableFromDate"),
									"availableThruDate", virtualSupplierProduct.get("availableThruDate"),
									"canDropShip", virtualSupplierProduct.get("canDropShip"),
									"comments", virtualSupplierProduct.get("comments"),
									"currencyUomId", virtualSupplierProduct.get("currencyUomId"),
									"lastPrice", virtualSupplierProduct.get("lastPrice"),
									"minimumOrderQuantity", virtualSupplierProduct.get("minimumOrderQuantity"),
									"orderQtyIncrements", virtualSupplierProduct.get("orderQtyIncrements"),
									"partyId", virtualSupplierProduct.get("partyId"),
									"productId", productAssoc.get("productId"),
									"quantityUomId", virtualSupplierProduct.get("quantityUomId"),
									"shippingPrice", virtualSupplierProduct.get("shippingPrice"),
									"standardLeadTimeDays", virtualSupplierProduct.get("standardLeadTimeDays"),
									"supplierPrefOrderId", virtualSupplierProduct.get("supplierPrefOrderId"),
									"supplierProductId", virtualSupplierProduct.get("supplierProductId"),
									"supplierProductName", virtualSupplierProduct.get("supplierProductName"),
									"supplierRatingTypeId", virtualSupplierProduct.get("supplierRatingTypeId"),
									"unitsIncluded", virtualSupplierProduct.get("unitsIncluded"),
									"userLogin", context.get("userLogin")));							
						}
					} else {
						errorId += "|此虚拟商品没有SupplierProduct数据:" +zipperProductAssoc.get("productId");
					}
				} else {
					errorId += "|此虚拟商品没有SupplierProduct数据:" + productAssoc.get("productId");
				}															
			}
			//查询中层拉链数据
			List<GenericValue> zipperProductAssocs = delegator.findByAnd("ProductAssoc", 
					UtilMisc.toMap("productIdTo", productAssoc.get("productId"),"productAssocTypeId", "PRODUCT_VARIANT"));						
			//没有中层就结束
			if (UtilValidate.isNotEmpty(zipperProductAssocs)) {
				GenericValue zipperProductAssoc = EntityUtil.getFirst(zipperProductAssocs);
				//查询拉链虚拟商品
				List<GenericValue> zipperSupplierProducts = delegator.findByAnd("SupplierProduct", 
						UtilMisc.toMap("productId", zipperProductAssoc.get("productId")));					
				for (GenericValue virtualSupplierProduct : zipperSupplierProducts) {
					//根据查询出的虚拟商品的SupplierProduct数据创建新的SupplierProduct productId为变形商品							
					dispatcher.runSync("createSupplierProduct", 
							UtilMisc.toMap("agreementId", virtualSupplierProduct.get("agreementId"),
							"agreementItemSeqId", virtualSupplierProduct.get("agreementItemSeqId"),
							"availableFromDate", virtualSupplierProduct.get("availableFromDate"),
							"availableThruDate", virtualSupplierProduct.get("availableThruDate"),
							"canDropShip", virtualSupplierProduct.get("canDropShip"),
							"comments", virtualSupplierProduct.get("comments"),
							"currencyUomId", virtualSupplierProduct.get("currencyUomId"),
							"lastPrice", virtualSupplierProduct.get("lastPrice"),
							"minimumOrderQuantity", virtualSupplierProduct.get("minimumOrderQuantity"),
							"orderQtyIncrements", virtualSupplierProduct.get("orderQtyIncrements"),
							"partyId", virtualSupplierProduct.get("partyId"),
							"productId", variantProduct.get("productId"),
							"quantityUomId", virtualSupplierProduct.get("quantityUomId"),
							"shippingPrice", virtualSupplierProduct.get("shippingPrice"),
							"standardLeadTimeDays", virtualSupplierProduct.get("standardLeadTimeDays"),
							"supplierPrefOrderId", virtualSupplierProduct.get("supplierPrefOrderId"),
							"supplierProductId", virtualSupplierProduct.get("supplierProductId"),
							"supplierProductName", virtualSupplierProduct.get("supplierProductName"),
							"supplierRatingTypeId", virtualSupplierProduct.get("supplierRatingTypeId"),
							"unitsIncluded", virtualSupplierProduct.get("unitsIncluded"),
							"userLogin", context.get("userLogin")));							
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		return errorId;
    }


    public static Map<String,Object> getProductBomRealQuantity(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId");
        String productIdTo = (String) context.get("productIdTo");
        String productAssocTypeId = (String) context.get("productAssocTypeId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        
        BigDecimal realQuantity = BigDecimal.ZERO;
		try {
			GenericValue pa  = delegator.findOne("ProductAssoc", false, UtilMisc.toMap("productId",productId,"productIdTo",productIdTo,"productAssocTypeId",productAssocTypeId,"fromDate",fromDate));
			realQuantity = ProductBomUtils.getProductBomRealQuantity(dispatcher, pa, userLogin);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        
        result.put("realQuantity", realQuantity);
    	return result;
    }
    
    /**
     * 按照条件查询所有非虚拟物料
     * @author jiaqi.Dai
     * @param ctx
     * @param context
     * @param productCategoryId 分类ID
     * @param idValue 素然物料编号
     * @param internalName 物料内部名称
     * @param description 规格
     * @param keyword 关键字
     * @return 商品LIST
     */
    public static Map<String,Object> queryNoVirtualProduct(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //分类编号
        String productCategoryId = (String) context.get("productCategoryId");
        //素然物料编号
        String idValue = (String) context.get("idValue");
        //商品名称
        String internalName = (String) context.get("internalName");
        //规格
        String description = (String) context.get("description");
        //关键字
        String keyword = (String) context.get("keyword");
        
        String comments = (String) context.get("comments");
        
        String isFind = (String) context.get("isFind");
        List<GenericValue> resultList = new ArrayList<GenericValue>();

        if (UtilValidate.isNotEmpty(isFind)) {
	        Set<GenericValue> keywordProduct = new HashSet<GenericValue>();
	        
	        try {
	        	List<GenericValue> noVirtualNoVariantProducts = delegator.findByAnd("NoVirtualNoVariantAccessocView", UtilMisc.toMap(        			
	        			"isVariant", "N",
	        			"isVirtual", "N",
	        			"productCategoryId", productCategoryId
	        			));
	        	for (GenericValue noVirtualNoVariantProduct : noVirtualNoVariantProducts) {
	        		//通过参数去对应的列作比较 如果符合放入返回集合中					
					if(UtilValidate.isNotEmpty(internalName)) {
						 if( ((String) noVirtualNoVariantProduct.get("internalName")).contains(internalName) ) {
							 resultList.add(noVirtualNoVariantProduct);						 
						 }
						 continue;
					}
					if(UtilValidate.isNotEmpty(description)) {
						if( ((String) noVirtualNoVariantProduct.get("description")).contains(description) ) {
							 resultList.add(noVirtualNoVariantProduct);						 
						 }
						continue;
					}
					if(UtilValidate.isNotEmpty(comments)) {
						if( ((String) noVirtualNoVariantProduct.get("comments")).contains(comments) ) {
							 resultList.add(noVirtualNoVariantProduct);						 
						 }
						continue;
					}
					if(UtilValidate.isNotEmpty(idValue)) {
						//查询素然物料编号
						GenericValue goodIdentification = EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap(
								"productId", noVirtualNoVariantProduct.get("productId"),
								"goodIdentificationTypeId", "ZUCZUG_CODE"
								)));
						if( ((String) goodIdentification.get("idValue")).contains(idValue) ) {
							 resultList.add(noVirtualNoVariantProduct);						 
						}
						continue;
					}
					if(UtilValidate.isNotEmpty(keyword)) {
						keywordProduct = new HashSet<GenericValue>();
						List<GenericValue> productKeyWords = delegator.findByAnd("ProductKeyword", UtilMisc.toMap(
									"productId", noVirtualNoVariantProduct.get("productId"),
									"keywordTypeId", "KWT_TAG"
								));
						for (GenericValue productKeyWord : productKeyWords) {
							if( ((String) productKeyWord.get("keyword")).contains(keyword) ) {
								keywordProduct.add(noVirtualNoVariantProduct);	
								break;
							}
						}
						if (keywordProduct.size() > 0) {
							Iterator<GenericValue> it = keywordProduct.iterator();
							while(it.hasNext()) {
								resultList.add(it.next());						
							}
							
						}
						continue;
					}
					
					resultList.add(noVirtualNoVariantProduct);
	        	}
	        	//虚拟-->变形查询----------------------------------------------------------------------------------------------------------------------------------------
	        	//查询所有参数分类下的虚拟物料
				List<GenericValue> virtualProducts = delegator.findByAnd("Product", UtilMisc.toMap("isVirtual", "Y", "productTypeId", "RAW_MATERIAL", "primaryProductCategoryId", productCategoryId));
				for (GenericValue virtualProduct : virtualProducts) {
					//查询虚拟物料下的所有变形物料
					List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProduct.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT", "thruDate", null));
					for (GenericValue productAssoc : productAssocs) {
						//查询变形物料
						GenericValue variantProduct = delegator.findByPrimaryKey("ZuczugProduct", UtilMisc.toMap("productId", productAssoc.get("productIdTo")));
						//通过参数去对应的列作比较 如果符合放入返回集合中					
						if(UtilValidate.isNotEmpty(internalName)) {
							 if( ((String) variantProduct.get("internalName")).contains(internalName) ) {
								 resultList.add(variantProduct);							 
							 }
							 continue;
						}
						if(UtilValidate.isNotEmpty(description)) {
							if( ((String) variantProduct.get("description")).contains(description) ) {
								 resultList.add(variantProduct);							 
							}
							continue;
						}
						if(UtilValidate.isNotEmpty(comments)) {
							if( ((String) variantProduct.get("comments")).contains(comments) ) {
								 resultList.add(variantProduct);						 
							 }
							continue;
						}
						if(UtilValidate.isNotEmpty(idValue)) {
							//查询素然物料编号
							GenericValue goodIdentification = EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap(
									"productId", variantProduct.get("productId"),
									"goodIdentificationTypeId", "ZUCZUG_CODE"
									)));
							if( ((String) goodIdentification.get("idValue")).contains(idValue) ) {
								 resultList.add(variantProduct);							 
							}
							continue;
						}
						
						if(UtilValidate.isNotEmpty(keyword)) {
							keywordProduct = new HashSet<GenericValue>();
							List<GenericValue> productKeyWords = delegator.findByAnd("ProductKeyword", UtilMisc.toMap(
										"productId", variantProduct.get("productId"),
										"keywordTypeId", "KWT_TAG"
									));
							for (GenericValue productKeyWord : productKeyWords) {
								if( ((String) productKeyWord.get("keyword")).contains(keyword) ) {
									keywordProduct.add(variantProduct);
									break;
								}							
							}
							continue;
						}
						resultList.add(variantProduct);
					}
				}
				if (keywordProduct.size() > 0) {
					Iterator<GenericValue> it = keywordProduct.iterator();
					while(it.hasNext()) {
						resultList.add(it.next());
					}				
				}
				result.put("listIt", resultList);
	        } catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		return result;
    }
    
    /**
     * 按照款号查询商品和他的变型商品的所有类型为【productContentTypeId】的图片 每个商品只会选取按照创建时间排序的一张图片
     * @author jiaqi.Dai
     * @param ctx
     * @param context
     * @param productId
     * @param productContentTypeId
     * @return 图片集合
     */
    public static Map<String,Object> queryProductAllImage(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        
        //分类编号
        String productId = (String) context.get("productId");
        String variantProductId = (String) context.get("variantProductId");
        String productContentTypeId = (String) context.get("productContentTypeId");
        List<GenericValue> resultList = new ArrayList<GenericValue>();
        List<String> orderBy = UtilMisc.toList("-createdDate");
        try {
        	if (UtilValidate.isEmpty(variantProductId)) {
        		variantProductId = productId;
        	}
        	GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId",variantProductId));
        	List<GenericValue> productAssocs = new ArrayList<GenericValue>();
        	if(product.get("isVirtual").equals("Y")) {
        		productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap(        			
            			"productId", productId,
            			"productAssocTypeId", "PRODUCT_VARIANT"
            			));
        		EntityCondition where =  EntityCondition.makeCondition(EntityOperator.AND,
            			EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, productContentTypeId),
                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
            			EntityCondition.makeCondition(EntityOperator.OR,
    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_APPROVED"),
    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_PENDING")
                                )
                                
            			);
            	
            	List<GenericValue> ProductContentAndInfos = delegator.findList("ProductContentAndInfo",where , null, orderBy, null, false); 
            	resultList.add(EntityUtil.getFirst(ProductContentAndInfos));
            	
            	for (GenericValue productAssoc : productAssocs) {
        	    	where =  EntityCondition.makeCondition(EntityOperator.AND,
                			EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, productContentTypeId),
                            EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productAssoc.get("productIdTo")),
                			EntityCondition.makeCondition(EntityOperator.OR,
        	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_APPROVED"),
        	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_PENDING")
                                    )
                                    
                			);
                	
                	ProductContentAndInfos = delegator.findList("ProductContentAndInfo",where , null, orderBy, null, false); 
                	GenericValue ProductContentAndInfo = EntityUtil.getFirst(ProductContentAndInfos);
                	resultList.add(ProductContentAndInfo);
            	}
        	} else if (product.get("isVariant").equals("N")){
        		EntityCondition where =  EntityCondition.makeCondition(EntityOperator.AND,
            			EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, productContentTypeId),
                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
            			EntityCondition.makeCondition(EntityOperator.OR,
    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_APPROVED"),
    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_PENDING")
                                )
                                
            			);
            	
    	    	List<GenericValue> ProductContentAndInfos = delegator.findList("ProductContentAndInfo",where , null, orderBy, null, false); 
    	    	GenericValue ProductContentAndInfo = EntityUtil.getFirst(ProductContentAndInfos);
            	resultList.add(ProductContentAndInfo);
        	} else {
        		String colorId = (String) EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", variantProductId, "productFeatureTypeId", "COLOR"))).get("productFeatureId");
        		String virtualProductId = (String) EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", variantProductId,"productAssocTypeId", "PRODUCT_VARIANT"))).get("productId");
        		List<GenericValue> virtualProductAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId,"productAssocTypeId", "PRODUCT_VARIANT"));
        		for (GenericValue virtualProductAssoc : virtualProductAssocs) {
        			String variantColorId = (String) EntityUtil.getFirst(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", virtualProductAssoc.get("productIdTo"), "productFeatureTypeId", "COLOR"))).get("productFeatureId");
        			if (colorId.equals(variantColorId)) {
        				productAssocs.add(virtualProductAssoc);
        			}
        		}
        		for (GenericValue productAssoc : productAssocs) {
	    	    	EntityCondition where =  EntityCondition.makeCondition(EntityOperator.AND,
                			EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, productContentTypeId),
	                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productAssoc.get("productIdTo")),
	            			EntityCondition.makeCondition(EntityOperator.OR,
	    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_APPROVED"),
	    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_PENDING")
	                                )
	                                
	            			);
	            	
	    	    	List<GenericValue> ProductContentAndInfos = delegator.findList("ProductContentAndInfo",where , null, orderBy, null, false); 
	    	    	GenericValue ProductContentAndInfo = EntityUtil.getFirst(ProductContentAndInfos);
                	resultList.add(ProductContentAndInfo);
	        	}
        	}

        	result.put("productContentAndInfoList", resultList);
        } catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
    }
    
    /**
     * 按照款号查询商品和他的变型商品的所有款式图（正面）图片 每个商品只会选取按照创建时间排序的一张图片
     * @author jiaqi.Dai
     * @param ctx
     * @param context
     * @param productId 商品ID
     * @return 图片集合
     */
    public static Map<String,Object> queryAllVariantProductImage(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //分类编号
        String productId = (String) context.get("productId");
        List<GenericValue> resultList = new ArrayList<GenericValue>();
        List<String> orderBy = UtilMisc.toList("-createdDate");
        try {
        	List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap(        			
        			"productId", productId,
        			"productAssocTypeId", "PRODUCT_VARIANT"
        			));
        	
        	EntityCondition where =  EntityCondition.makeCondition(EntityOperator.AND,
        			EntityCondition.makeCondition("productContentTypeId", EntityOperator.LIKE, "%IMAGE"),
                    EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
        			EntityCondition.makeCondition(EntityOperator.OR,
	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_APPROVED"),
	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_PENDING")
                            )
                            
        			);
        	
        	List<GenericValue> ProductContentAndInfos = delegator.findList("ProductContentAndInfo",where , null, null, null, false); 
        	resultList.add(EntityUtil.getFirst(ProductContentAndInfos));
    	    for (GenericValue productAssoc : productAssocs) {
    	    	where =  EntityCondition.makeCondition(EntityOperator.AND,
            			EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, "MODELS_POSITIVE_IMAGE"),
                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productAssoc.get("productIdTo")),
            			EntityCondition.makeCondition(EntityOperator.OR,
    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_APPROVED"),
    	                            EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("statusId"), EntityOperator.EQUALS, "IM_PENDING")
                                )
                                
            			);
            	
            	ProductContentAndInfos = delegator.findList("ProductContentAndInfo",where , null, orderBy, null, false); 
            	GenericValue ProductContentAndInfo = EntityUtil.getFirst(ProductContentAndInfos);
            	resultList.add(ProductContentAndInfo);
        	}
        	
        	result.put("productContentAndInfoList", resultList);
        } catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
    }

    // 获取某个分类下所有商品的所有原料供应商，返回是供应商partyId的List
    public static Map<String, Object> getMatSuppliersByProductCategory(DispatchContext dctx, Map<String, ? extends Object> context) {

        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productCategoryId = (String) context.get("productCategoryId");
        Set<String> supplierIds = new HashSet();
        
        try {
            // 先获取分类下的所有finished good
			List<GenericValue> productAndCategoryMembers = delegator.findByAnd("ProductAndCategoryMember",
					UtilMisc.toMap("productCategoryId", productCategoryId, "productTypeId", "FINISHED_GOOD"));
			if (UtilValidate.isEmpty(productAndCategoryMembers)) {
				Debug.logInfo("------------------------------ productAndCategoryMembers is empty", module);
				return result;
			}
			for (GenericValue productAndCategoryMember : productAndCategoryMembers) {
				Debug.logInfo("--------------------------------------- " + productAndCategoryMember.getString("productId"), module);
				Set<String> moreSupplierIds = ZuczugProductUtils.getMatSupplierIdsByProductId(delegator, productAndCategoryMember.getString("productId"));
				supplierIds.addAll(moreSupplierIds);
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
        List<String> partyIds = new ArrayList(supplierIds);
        result.put("partyIds", partyIds);
        return result;
    }

    // 某个供应商为某个分类，提供的所有原材料，返回的信息包括某个sku以及这个供应商的所有原材料
    public static Map<String, Object> getSupplierMaterialByCat(DispatchContext dctx, Map<String, ? extends Object> context) {

        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productCategoryId = (String) context.get("productCategoryId");
        String partyId = (String) context.get("partyId");
        List<Map> skuMatMaps = FastList.newInstance();
        
        try {
            // 先获取分类下的所有finished good
			List<GenericValue> productAndCategoryMembers = delegator.findByAnd("ProductAndCategoryMember",
					UtilMisc.toMap("productCategoryId", productCategoryId, "productTypeId", "FINISHED_GOOD"));
			if (UtilValidate.isEmpty(productAndCategoryMembers)) {
				return result;
			}
			for (GenericValue productAndCategoryMember : productAndCategoryMembers) {
				List<Map> moreSkuMatMaps = ZuczugProductUtils.getSupplierMaterialByProductId(delegator, partyId, productAndCategoryMember.getString("productId"));
				skuMatMaps.addAll(moreSkuMatMaps);
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
        result.put("skuMatMaps", skuMatMaps);
        return result;
    }
    
    /**
     * 查询所有正式商品的变型商品以及非虚拟非变形的正式商品
     * @author jiaqi.Dai
     * @param dctx
     * @param context
     * @return 返回一个Map Key:listIt 类型：List[Product]
     */
    public static Map<String, Object> findAllRealProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List<GenericValue> resultList = new ArrayList<GenericValue>();
        String productId = (String) context.get("productId");
        String waveId = (String) context.get("waveId");
        
        try {
        	EntityCondition where =  EntityCondition.makeCondition(EntityOperator.AND,
                             EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productId + "%")),
                             EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "N"),
                             EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
        	
        	EntityListIterator products =  delegator.find("RealProductSkuView", where, null, null, null, null);
        	
			while(products.hasNext()) {
				GenericValue product = products.next();
				if (product.get("isVariant").equals("Y")) {
					List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", product.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT"));
					if (UtilValidate.isNotEmpty(productAssocs)) {
						if(UtilValidate.isNotEmpty(waveId)&&UtilValidate.isNotEmpty(product.get("waveId"))) {
							if(product.get("waveId").equals(waveId)) {
								resultList.add(product);
							}
						} else {
							resultList.add(product);
						}
						
					}
				} else {
					if(UtilValidate.isNotEmpty(waveId)&&UtilValidate.isNotEmpty(product.get("waveId"))) {
						if(product.get("waveId").equals(waveId)) {
							resultList.add(product);
						}
					} else {
						resultList.add(product);
					}
				}
			}
			result.put("listIt", resultList);
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
    }
    
    /**
     * 拷贝虚拟商品的所有供应商到变型商品
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> copyProductSupplierToVariants(DispatchContext dctx, Map<String, ? extends Object> context){
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String virtualProductId = (String) context.get("virtualProductId");
        String variantProductId = (String) context.get("variantProductId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        try{
	        if (UtilValidate.isEmpty(virtualProductId)) {
	        	if (UtilValidate.isNotEmpty(variantProductId)) {
	        		virtualProductId = ZuczugProductUtils.getVirtualProductIdByVariantProductId(delegator, variantProductId);
	        	}
	        }
	        
	        if (UtilValidate.isEmpty(virtualProductId)) { //如果即没有传入virtualProductId参数，且从variantProductId中也获取不到virtualProductId，则直接返回空
	        	return result;
	        }

	        // 获取该虚拟商品的所有变型商品
	        List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"), UtilMisc.toList("productId"));
    		productAssocs = EntityUtil.filterByDate(productAssocs);
    		
    		// 获取虚拟商品的所有供应商
    		List<GenericValue> supplierProducts = delegator.findByAnd("SupplierProduct",
    				UtilMisc.toMap("productId", virtualProductId));

    		//所有变形商品
    		for(GenericValue productAssoc:productAssocs)
    		{
    			String productIdTo = productAssoc.getString("productIdTo");
    	        List<GenericValue> variantSupplierProducts = delegator.findByAnd("SupplierProduct", UtilMisc.toMap("productId", productIdTo));
    	
    	        for (GenericValue variantSupplierProduct:variantSupplierProducts) {
    	        	delegator.removeValue(variantSupplierProduct);
    	        }
    	        //循环每一个feature，设置到所有的变型商品中
        		for (GenericValue supplierProduct:supplierProducts) {
        			supplierProduct.set("productId", productIdTo);
        	        delegator.createOrStore(supplierProduct);
        	        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productIdTo));
        	        if ("ACCE_ZIPPER".equals(product.get("primaryProductCategoryId"))) {
        	        	dispatcher.runSync("copyProductSupplierToVariants", UtilMisc.toMap("virtualProductId",productIdTo));
        	        }
        		}
    		}
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return result;
    }
    /**
     * zhoulei
     * 修复产品数据，153有些数据没有关联到系列
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> repairProductSeries(DispatchContext dctx, Map<String, ? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        String productId = (String)context.get("productId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productHead=productId.substring(0,1);
        Timestamp fromDate = UtilDateTime.nowTimestamp();
        Debug.log("+++++++++++++++1:"+productId);
        try {
        	List<GenericValue> productCategoriesRaw=delegator.findByAnd("ProductCategoryAndMember",
            		UtilMisc.toMap("productId",productId,"productCategoryTypeId","SERIES"));
            if (UtilValidate.isEmpty(productCategoriesRaw)) {
    			if (productHead.equals("0")) {
    				delegator.create("ProductCategoryMember",UtilMisc.toMap("productId", productId, "productCategoryId", "SERIES_0","fromDate",fromDate));
    			}else if (productHead.equals("E")) {
    				delegator.create("ProductCategoryMember",UtilMisc.toMap("productId", productId, "productCategoryId", "SERIES_EXTRA","fromDate",fromDate));
    			}else if (productHead.equals("B")) {
    				delegator.create("ProductCategoryMember",UtilMisc.toMap("productId", productId, "productCategoryId", "SERIES_BLUE","fromDate",fromDate));
    			}else if (productHead.equals("S")) {
    				delegator.create("ProductCategoryMember",UtilMisc.toMap("productId", productId, "productCategoryId", "SERIES_HAND","fromDate",fromDate));
    			}else if (productHead.equals("Z")) {
    				Debug.log("+++++++++++++++1:"+productId);
    				delegator.create("ProductCategoryMember",UtilMisc.toMap("productId", productId, "productCategoryId", "SERIES_Z","fromDate",fromDate));
    			}
    		}
        	return result;
		} catch (Exception e) {
			e.printStackTrace();
		}return result;
    }
    
    /**
     * 按照条件查询所有非虚拟物料
     * @author jiaqi.Dai
     * @param ctx
     * @param context
     * @param productCategoryId 分类ID
     * @param idValue 素然物料编号
     * @param internalName 物料内部名称
     * @param description 规格
     * @param keyword 关键字
     * @return 商品LIST
     */
    public static Map<String,Object> queryNoVirtualProductByParty(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //分类编号
        String partyId = (String) context.get("partyId");
        //素然物料编号
        String idValue = (String) context.get("idValue");
        //商品名称
        String internalName = (String) context.get("internalName");
        //规格
        String description = (String) context.get("description");
        
        String groupName = (String) context.get("groupName");
        
        String noFind = (String) context.get("noFind");
        
        String productTypeId = (String) context.get("productTypeId");
        
        String prodCatalogId = (String) context.get("prodCatalogId");
        
        String productCategoryId = (String) context.get("productCategoryId");
        List<GenericValue> resultList = new ArrayList<GenericValue>();
        Set<GenericValue> keywordProduct = new HashSet<GenericValue>();
        List<GenericValue> noVirtualNoVariantProducts = null;
        if (UtilValidate.isNotEmpty(noFind)) {
        	try {
        		if (UtilValidate.isEmpty(productCategoryId)) {
        			noVirtualNoVariantProducts = delegator.findByAnd("ZuczugSupplierProductAndProductView", UtilMisc.toMap(        			
                			"isVariant", "N",
                			"isVirtual", "N",
                			"partyId", partyId,
                			"groupName", groupName,
                			"productTypeId", productTypeId,
                			"prodCatalogId", prodCatalogId
                			));
        		} else {
        			noVirtualNoVariantProducts = delegator.findByAnd("ZuczugSupplierProductAndProductView", UtilMisc.toMap(        			
                			"isVariant", "N",
                			"isVirtual", "N",
                			"partyId", partyId,
                			"groupName", groupName,
                			"productTypeId", productTypeId,
                			"prodCatalogId", prodCatalogId,
                			"primaryProductCategoryId", productCategoryId
                			));
        		}
            	
            	for (GenericValue noVirtualNoVariantProduct : noVirtualNoVariantProducts) {
            		//通过参数去对应的列作比较 如果符合放入返回集合中					
    				if(UtilValidate.isNotEmpty(internalName)) {
    					 if( ((String) noVirtualNoVariantProduct.get("internalName")).contains(internalName) ) {
    						 resultList.add(noVirtualNoVariantProduct);						 
    					 }
    					 continue;
    				}
    				if(UtilValidate.isNotEmpty(description)) {
    					if( ((String) noVirtualNoVariantProduct.get("description")).contains(description) ) {
    						 resultList.add(noVirtualNoVariantProduct);						 
    					 }
    					continue;
    				}
    				if(UtilValidate.isNotEmpty(idValue)) {
    					//查询素然物料编号
    					GenericValue goodIdentification = EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap(
    							"productId", noVirtualNoVariantProduct.get("productId"),
    							"goodIdentificationTypeId", "ZUCZUG_CODE"
    							)));
    					if( ((String) goodIdentification.get("idValue")).contains(idValue) ) {
    						 resultList.add(noVirtualNoVariantProduct);						 
    					}
    					continue;
    				}
    				
    				
    				resultList.add(noVirtualNoVariantProduct);
            	}
            	//虚拟-->变形查询----------------------------------------------------------------------------------------------------------------------------------------
            	//查询所有参数分类下的虚拟物料
    			List<GenericValue> virtualProducts = null;
    			if (UtilValidate.isEmpty(productCategoryId)) {
    				virtualProducts = delegator.findByAnd("ZuczugSupplierProductAndProductView", UtilMisc.toMap(        			
                			"isVirtual", "Y",
                			"partyId", partyId,
                			"groupName", groupName,
                			"productTypeId", productTypeId,
                			"prodCatalogId", prodCatalogId
                			));
    			} else {
    				virtualProducts = delegator.findByAnd("ZuczugSupplierProductAndProductView", UtilMisc.toMap(        			
                			"isVirtual", "Y",
                			"partyId", partyId,
                			"groupName", groupName,
                			"productTypeId", productTypeId,
                			"prodCatalogId", prodCatalogId,
                			"primaryProductCategoryId", productCategoryId
                			));
    			}
    			
    			for (GenericValue virtualProduct : virtualProducts) {
    				//查询虚拟物料下的所有变形物料
    				List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProduct.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT", "thruDate", null));
    				for (GenericValue productAssoc : productAssocs) {
    					//查询变形物料
    					GenericValue variantProduct = delegator.findByPrimaryKey("ZuczugProduct", UtilMisc.toMap("productId", productAssoc.get("productIdTo")));
    					//通过参数去对应的列作比较 如果符合放入返回集合中					
    					if(UtilValidate.isNotEmpty(internalName)) {
    						 if( ((String) variantProduct.get("internalName")).contains(internalName) ) {
    							 resultList.add(variantProduct);							 
    						 }
    						 continue;
    					}
    					if(UtilValidate.isNotEmpty(description)) {
    						if( ((String) variantProduct.get("description")).contains(description) ) {
    							 resultList.add(variantProduct);							 
    						}
    						continue;
    					}
    					if(UtilValidate.isNotEmpty(idValue)) {
    						//查询素然物料编号
    						GenericValue goodIdentification = EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap(
    								"productId", variantProduct.get("productId"),
    								"goodIdentificationTypeId", "ZUCZUG_CODE"
    								)));
    						if( ((String) goodIdentification.get("idValue")).contains(idValue) ) {
    							 resultList.add(variantProduct);							 
    						}
    						continue;
    					}
    					
    					
    					resultList.add(variantProduct);
    				}
    			}
    			if (keywordProduct.size() > 0) {
    				Iterator<GenericValue> it = keywordProduct.iterator();
    				while(it.hasNext()) {
    					resultList.add(it.next());
    				}				
    			}
    			result.put("listIt", resultList);
            } catch (GenericEntityException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        
		return result;
    }
    
    /**
     * 按照条件查询所有成品
     * @author jiaqi.Dai
     * @param ctx
     * @param context
     * @param productCategoryId 分类ID
     * @param idValue 素然物料编号
     * @param internalName 物料内部名称
     * @param description 规格
     * @param keyword 关键字
     * @return 商品LIST
     */
    public static Map<String,Object> queryNoVirtualRealProductByParty(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //分类编号
        String partyId = (String) context.get("partyId");
        //商品名称
        String internalName = (String) context.get("internalName");
        //规格
        String description = (String) context.get("description");
        
        String groupName = (String) context.get("groupName");
        
        String noFind = (String) context.get("noFind");
        List<GenericValue> resultList = new ArrayList<GenericValue>();
        Set<GenericValue> keywordProduct = new HashSet<GenericValue>();
        
        if (UtilValidate.isNotEmpty(noFind)) {
        	try {
            	List<GenericValue> noVirtualNoVariantProducts = delegator.findByAnd("ZuczugSupplierProductAndRealProductView", UtilMisc.toMap(        			
            			"isVariant", "N",
            			"isVirtual", "N",
            			"partyId", partyId,
            			"groupName", groupName
            			));
            	for (GenericValue noVirtualNoVariantProduct : noVirtualNoVariantProducts) {
            		//通过参数去对应的列作比较 如果符合放入返回集合中					
    				if(UtilValidate.isNotEmpty(internalName)) {
    					 if( ((String) noVirtualNoVariantProduct.get("internalName")).contains(internalName) ) {
    						 resultList.add(noVirtualNoVariantProduct);						 
    					 }
    					 continue;
    				}
    				if(UtilValidate.isNotEmpty(description)) {
    					if( ((String) noVirtualNoVariantProduct.get("description")).contains(description) ) {
    						 resultList.add(noVirtualNoVariantProduct);						 
    					 }
    					continue;
    				}
    				resultList.add(noVirtualNoVariantProduct);
            	}
            	//虚拟-->变形查询----------------------------------------------------------------------------------------------------------------------------------------
            	//查询所有参数分类下的虚拟物料
    			List<GenericValue> virtualProducts = delegator.findByAnd("ZuczugSupplierProductAndRealProductView", UtilMisc.toMap(        			
            			"isVirtual", "Y",
            			"partyId", partyId,
            			"groupName", groupName
            			));
    			for (GenericValue virtualProduct : virtualProducts) {
    				//查询虚拟物料下的所有变形物料
    				List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProduct.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT", "thruDate", null));
    				for (GenericValue productAssoc : productAssocs) {
    					//查询变形物料
    					GenericValue variantProduct = delegator.findByPrimaryKey("ZuczugProduct", UtilMisc.toMap("productId", productAssoc.get("productIdTo")));
    					//通过参数去对应的列作比较 如果符合放入返回集合中					
    					if(UtilValidate.isNotEmpty(internalName)) {
    						 if( ((String) variantProduct.get("internalName")).contains(internalName) ) {
    							 resultList.add(variantProduct);							 
    						 }
    						 continue;
    					}
    					if(UtilValidate.isNotEmpty(description)) {
    						if( ((String) variantProduct.get("description")).contains(description) ) {
    							 resultList.add(variantProduct);							 
    						}
    						continue;
    					}
    					resultList.add(variantProduct);
    				}
    			}
    			if (keywordProduct.size() > 0) {
    				Iterator<GenericValue> it = keywordProduct.iterator();
    				while(it.hasNext()) {
    					resultList.add(it.next());
    				}				
    			}
    			result.put("listIt", resultList);
            } catch (GenericEntityException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        
		return result;
    }
    
    public static Map<String, Object> classifyProductByCategory(DispatchContext dctx, Map<String, ? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> fromProductList =  (List<GenericValue>) context.get("fromProductList");
        List<GenericValue> resultProductList = new ArrayList<GenericValue>();
        List<GenericValue> noCategoryProductList = new ArrayList<GenericValue>();
        Map<String, List<GenericValue>> classifyMap = new HashMap<String, List<GenericValue>>();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
	        for (GenericValue product : fromProductList) {
	        	String productCategoryId = "";
	        	if ("Y".equals(product.get("isVariant"))) {	 
	        		String productIdTo = "";
	        		if (UtilValidate.isEmpty(product.get("productId"))) {
	        			productIdTo = product.get("productIdTo").toString();
	        		} else {
	        			productIdTo = product.get("productId").toString();
	        		}
					List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo" ,productIdTo, "productAssocTypeId","PRODUCT_VARIANT"));
					String virtualProductId = EntityUtil.getFirst(productAssocs).get("productId").toString();
					GenericValue virtualProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", virtualProductId));
					if(UtilValidate.isNotEmpty(virtualProduct.get("primaryProductCategoryId"))) {
						productCategoryId = virtualProduct.get("primaryProductCategoryId").toString();
					} else {
						noCategoryProductList.add(product);
					}
	        	} else {
	        		if(UtilValidate.isNotEmpty(product.get("primaryProductCategoryId"))) {
	        			productCategoryId = product.get("primaryProductCategoryId").toString();
	        		} else {
	        			noCategoryProductList.add(product);
	        		}
	        		
	        	}
	        	List<GenericValue> groupList = classifyMap.get(productCategoryId);
	        	if (UtilValidate.isEmpty(groupList)) {
	        		groupList = new ArrayList<GenericValue>();
	        	}
	        	groupList.add(product);
	        	classifyMap.put(productCategoryId, groupList);
	        }
	        
	        for (List<GenericValue> values : classifyMap.values()) {  
	        	for (GenericValue product : values) {
	        		resultProductList.add(product);
	        	}	          
	        }  
	        for (GenericValue product : noCategoryProductList) {
        		resultProductList.add(product);
        	}	
	        result.put("listIt", resultProductList);
        } catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;    
    }
    
    public static Map<String,Object> queryProductInCatalog(DispatchContext ctx, Map<String, Object> context){
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //分类编号
        String productCategoryId = (String) context.get("productCategoryId");
        
        String isFind = (String) context.get("isFind");
        List<GenericValue> resultList = new ArrayList<GenericValue>();

        if (UtilValidate.isNotEmpty(isFind)) {
	        Set<GenericValue> keywordProduct = new HashSet<GenericValue>();
	        
	        try {
	        	List<GenericValue> noVirtualNoVariantProducts = delegator.findByAnd("NoVirtualNoVariantAccessocView", UtilMisc.toMap(        			
	        			"isVariant", "N",
	        			"isVirtual", "N",
	        			"productCategoryId", productCategoryId
	        			));
	        	
        		//通过参数去对应的列作比较 如果符合放入返回集合中
        		resultList = checkProduct(delegator, noVirtualNoVariantProducts, resultList, context, keywordProduct);
	        	//虚拟-->变形查询----------------------------------------------------------------------------------------------------------------------------------------
	        	//查询所有参数分类下的虚拟物料
				List<GenericValue> virtualProducts = delegator.findByAnd("ZuczugProduct", UtilMisc.toMap("isVirtual", "Y", "productTypeId", "RAW_MATERIAL", "primaryProductCategoryId", productCategoryId));
				
				resultList= checkProduct(delegator, virtualProducts, resultList, context, keywordProduct);
				
				for (GenericValue virtualProduct : virtualProducts) {
					//查询虚拟物料下的所有变形物料
					List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProduct.get("productId"), "productAssocTypeId", "PRODUCT_VARIANT", "thruDate", null));
					List<GenericValue> products = new ArrayList<GenericValue>();
					for (GenericValue productAssoc : productAssocs) {
						//查询变形物料
						GenericValue variantProduct = delegator.findByPrimaryKey("ZuczugProduct", UtilMisc.toMap("productId", productAssoc.get("productIdTo")));
						products.add(variantProduct);						
					}
					resultList= checkProduct(delegator, products, resultList, context, keywordProduct);
				}
				if (keywordProduct.size() > 0) {
					Iterator<GenericValue> it = keywordProduct.iterator();
					while(it.hasNext()) {
						resultList.add(it.next());
					}				
				}
				result.put("listIt", resultList);
	        } catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		return result;
    }

	private static List<GenericValue> checkProduct(Delegator delegator, List<GenericValue> noVirtualNoVariantProducts, List<GenericValue> resultList,
			Map<String, Object> context, Set<GenericValue> keywordProduct) throws GenericEntityException {
		String idValue = (String) context.get("idValue");
        //商品名称
        String internalName = (String) context.get("internalName");
        //规格
        String description = (String) context.get("description");
        //关键字
        String keyword = (String) context.get("keyword");
        
        String comments = (String) context.get("comments");
        for (GenericValue noVirtualNoVariantProduct : noVirtualNoVariantProducts) {
        	if(UtilValidate.isNotEmpty(internalName)) {
	   			 if( ((String) noVirtualNoVariantProduct.get("internalName")).contains(internalName) ) {
	   				 resultList.add(noVirtualNoVariantProduct);						 
	   			 }
	   			 continue;
	   		}
	   		if(UtilValidate.isNotEmpty(description)) {
	   			if( ((String) noVirtualNoVariantProduct.get("description")).contains(description) ) {
	   				 resultList.add(noVirtualNoVariantProduct);						 
	   			 }
	   			continue;
	   		}
	   		if(UtilValidate.isNotEmpty(comments)) {
	   			if( ((String) noVirtualNoVariantProduct.get("comments")).contains(comments) ) {
	   				 resultList.add(noVirtualNoVariantProduct);						 
	   			 }
	   			continue;
	   		}
	   		if(UtilValidate.isNotEmpty(idValue)) {
	   			//查询素然物料编号
	   			GenericValue goodIdentification = EntityUtil.getFirst(delegator.findByAnd("GoodIdentification", UtilMisc.toMap(
	   					"productId", noVirtualNoVariantProduct.get("productId"),
	   					"goodIdentificationTypeId", "ZUCZUG_CODE"
	   					)));
	   			if( ((String) goodIdentification.get("idValue")).contains(idValue) ) {
	   				 resultList.add(noVirtualNoVariantProduct);						 
	   			}
	   			continue;
	   		}
	   		if(UtilValidate.isNotEmpty(keyword)) {
	   			keywordProduct = new HashSet<GenericValue>();
	   			List<GenericValue> productKeyWords = delegator.findByAnd("ProductKeyword", UtilMisc.toMap(
	   						"productId", noVirtualNoVariantProduct.get("productId"),
	   						"keywordTypeId", "KWT_TAG"
	   					));
	   			for (GenericValue productKeyWord : productKeyWords) {
	   				if( ((String) productKeyWord.get("keyword")).contains(keyword) ) {
	   					keywordProduct.add(noVirtualNoVariantProduct);	
	   					break;
	   				}
	   			}
	   			if (keywordProduct.size() > 0) {
	   				Iterator<GenericValue> it = keywordProduct.iterator();
	   				while(it.hasNext()) {
	   					resultList.add(it.next());						
	   				}
	   				
	   			}
	   			continue;
	   		}
	   		
	   		resultList.add(noVirtualNoVariantProduct);
        }
		
		return resultList;
	}
}
