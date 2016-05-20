package com.zuczug.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import com.zuczug.product.ZuczugProductServices;

import freemarker.template.utility.DateUtil;

public class ZuczugFixDataServices {
	
	public static final String module = ZuczugFixDataServices.class.getName();
	
	/**
     * 商品特征转商品
     * @param dctx
     * @param context
     * @return
     * 1.查询所有ProductFeatureAppl的数据
     * 2.通过找到的ProductFeatureAppl中的FeatureId与FeatureTypeId查找ProductCategory中是否存在类型(特征)数据，如果存在则可以创建ProductCategoryMember数据
     * 3.在可以创建ProductCategoryMember的情况下，判断ProductFeatureAppl数据中的商品是否为变形商品(Product.isVariant=Y/N),只有是非变形商品才进行新增 
     * 4.商品为非变形商品，通过ProductFeatureAppl中的数据新增ProductCategoryMember数据
     * 5.新增结束后在代码最后删除ProductFeatureAppl数据,无论是否为变形商品
     */
    public static Map<String, Object> productFeatureToCategory(DispatchContext dctx, Map<String, ? extends Object> context){
    	 GenericValue userLogin = (GenericValue) context.get("userLogin");
    	 Map<String, Object> result = ServiceUtil.returnSuccess();
    	 String featureId = "";
    	 String featureTypeId = "";
    	 String productId = "";
    	 String isVariant = "";
    	 Timestamp fromDate = null;
    	 Timestamp thruDate = null;
    	 LocalDispatcher dispatcher = dctx.getDispatcher();
         Delegator delegator = dctx.getDelegator();
         try {
        	
        	 List<GenericValue> features = delegator.findByAnd("ProductFeatureAppl");
        	 List<GenericValue> categoryMember = null;
             List<GenericValue> category = null;
             GenericValue feature = null;
             GenericValue product = null;
             GenericValue productFeatureApp = null;
             for (int i = 0; i < features.size(); i++) {            	
            	 featureId = (String) features.get(i).get("productFeatureId");
            	 productId = (String) features.get(i).get("productId");
            	 fromDate = (Timestamp) features.get(i).get("fromDate");
            	 thruDate = (Timestamp) features.get(i).get("thruDate");
            	 //查询ProductFeature
            	 feature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureId));
            	 //获取FeatureTypeId
            	 featureTypeId = (String) feature.get("productFeatureTypeId");
            	 //用特征ID与特征类型ID去Category中查找 找不到则不新增
            	 category = delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId", featureId ,"productCategoryTypeId", featureTypeId));           	 
            	 if ( category.size() > 0 ) {
            		 //查询商品
            		 product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            		 if ( product != null ) {
            			 isVariant = (String) product.get("isVariant");
            			 //判断是否为变形商品
            			 if (isVariant != "Y") {
            				 //不是变形商品，新增数据
            				 categoryMember = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", featureId, "productId", productId)));
            				 if (categoryMember.size() == 0) {
            					 dispatcher.runSync("addProductToCategory",
                        				 UtilMisc.toMap(
                        						 "userLogin", userLogin,
                        						 "productCategoryId", featureId, 
                        						 "productId", productId,
                        						 "fromDate", fromDate,
                        						 "thruDate", thruDate
                        						 ));
            				 }
            				 
            			 }                		 
            		 }   
            		 productFeatureApp = delegator.findByPrimaryKey("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", featureId,"productId", productId, "fromDate", fromDate));
            		 fromDate = (Timestamp) productFeatureApp.get("fromDate");
            		 //删除ProductFeature
            		 dispatcher.runSync("removeProductFeatureApplAndAttr",  
     						UtilMisc.toMap(
     								"userLogin", userLogin,
     								"productFeatureId", featureId, 
     								"fromDate", fromDate ,
     								"productId", productId
     								));	
            		 
            	 }
             }
         } catch (Exception e) {
        	 Debug.logError(e.getMessage(), module);
		 }
         
         return result;  
    }
    
    /**
     * 将旧的波段特征数据转为新的波段类型数据
     * @param dctx
     * @param context
     * @return
     * 1.查询：在PRODUCT_FEATURE_APPL中查询出所有包含旧波段特征的商品
	 * 2.更新：查询出的些商品的旧的波段数据替换成新的波段数据并存入PRODUCT_CATEGORY_MEMBER中
	 * 3.删除旧数据。
     */
    public static Map<String, Object> oldWaveToNewWave(DispatchContext dctx, Map<String, ? extends Object> context){
	    	 LocalDispatcher dispatcher = dctx.getDispatcher();
	         Delegator delegator = dctx.getDelegator();	         
	    	 GenericValue userLogin = (GenericValue) context.get("userLogin");
	    	 Map<String, Object> result = ServiceUtil.returnSuccess();
	    	 List<GenericValue> productFeatureAppl = null;
	    	 GenericValue product = null;
	    	 String isVariant = "";
	    	 String productFeatureId = "";
	    	 
	    	 String oldWave[] = {"A_SPRING", "C_AUTUMN", "D_WINTER"};
	    	 String newWave[] = {"2016_SS_A", "2015_AW_C", "2015_AW_D"};
	    	 try {
	    		 //外层控制季节
		    	 for (int i = 0 ; i < oldWave.length ; i++) {
		    		 //内层控制10个波段
		    		 for (int j = 1; j <= 10 ; j++) {
		    			 //拼接旧的波段ID
		    			 productFeatureId = oldWave[i] + j + "";
		    			 //查询与旧的波段ID有关连的商品
		    			 productFeatureAppl = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", productFeatureId));
		    			 //遍历出特征数据关联，新增至类型关联中并删除旧数据
			    		 for (int k = 0; k < productFeatureAppl.size(); k++) {
			    			 //通过商品ID查询商品
			    			 product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productFeatureAppl.get(k).get("productId")));
			    			 if ( product != null ) {
		            			 isVariant = (String) product.get("isVariant");
		            			 //判断是否为变形商品
		            			 if (!"Y".equals(isVariant)) {
		            				 //新增数据
				    				 dispatcher.runSync("addProductToCategory",
			                				 UtilMisc.toMap(
			                						 "userLogin", userLogin,
			                						 "productCategoryId", newWave[i] + j, 
			                						 "productId", productFeatureAppl.get(k).get("productId"),
			                						 "fromDate", productFeatureAppl.get(k).get("fromDate"),
			                						 "thruDate", productFeatureAppl.get(k).get("thruDate")
			                						 ));				    				 	
		            			 }
			    		 }
			    		 //无论是否是变形商品都删除旧数据
		    			 dispatcher.runSync("removeProductFeatureApplAndAttr",  
		      						UtilMisc.toMap(
		      								"userLogin", userLogin,
		      								"productFeatureId", productFeatureId, 
		      								"fromDate", productFeatureAppl.get(k).get("fromDate") ,
		      								"productId", productFeatureAppl.get(k).get("productId")
		      								));	
			    		}		    			 	    			 
		    		 }
		    	 }
    	 	} catch (GenericEntityException e) {
    	 		Debug.logError(e.getMessage(), module);
				e.printStackTrace();
			} catch (GenericServiceException e) {
				Debug.logError(e.getMessage(), module);
			e.printStackTrace();
		}
    	 return result;  
    }
    
	 public static Map<String, Object> createProductToBomProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 LocalDispatcher dispatcher = dctx.getDispatcher();
         Delegator delegator = dctx.getDelegator();	         
    	 GenericValue userLogin = (GenericValue) context.get("userLogin");
    	 String productId = (String) context.get("productId");
    	 String BomProduct = (String) context.get("BomProduct");
    	 BigDecimal quantity = (BigDecimal) context.get("quantity");
    	 String instruction = (String) context.get("instruction");
    	 try {
    		List<EntityExpr> exprs = FastList.newInstance();
    		//按照ID查询变形商品
    	    exprs.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%"+BomProduct+"%"));
    	    exprs.add(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "N"));
    	    
			List<GenericValue> productList = delegator.findList("Product",EntityCondition.makeCondition(exprs, EntityOperator.AND) , null, null, null, false);
			for (GenericValue product : productList) {
				String productTypeId = (String)product.get("productTypeId");
				if ("WIP".equals(productTypeId)) {
					//关联给款的BOM
					dispatcher.runSync("addAccessoryBom", UtilMisc.toMap(
							"userLogin",userLogin,
							"variantProductId", product.get("productId"),
							"productIdTo", productId,
							"instruction", instruction));
					GenericValue existingProductAssoc = EntityUtil.getFirst(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", product.get("productId"),"productIdTo",productId)));
					//更新数量
					dispatcher.runSync("updateProductAssoc", UtilMisc.toMap("userLogin",userLogin,
							"productId", product.get("productId"), 
							"productIdTo", productId, 
							"productAssocTypeId", "ENGINEER_COMPONENT", 
							"fromDate", existingProductAssoc.get("fromDate"), 
							"quantity", quantity));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;		 
	 }
	 
	 public static Map<String, Object> FixZuczugCode(DispatchContext dctx, Map<String, ? extends Object> context) {
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 LocalDispatcher dispatcher = dctx.getDispatcher();
         Delegator delegator = dctx.getDelegator();	         
    	 GenericValue userLogin = (GenericValue) context.get("userLogin");
    	 try {
    		//查询所有原材料
			List<GenericValue> products = delegator.findByAnd("Product", UtilMisc.toMap("productTypeId", "RAW_MATERIAL"));
			for (GenericValue product : products) {
				//查询原材料的素然物料编号
				GenericValue good = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("productId", product.get("productId"),"goodIdentificationTypeId", "ZUCZUG_CODE"));
				//是否有素然物料编号
				if(UtilValidate.isEmpty(good)) {
					//没有复制productID给素然物料编号
					dispatcher.runSync("createGoodIdentification", UtilMisc.toMap(
							"productId",product.get("productId"),
							"goodIdentificationTypeId","ZUCZUG_CODE",
							"idValue", product.get("productId"),
							"userLogin",userLogin
							));
				}
			}
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
		 return result;
	 }
	 
	 /**
	  * 把N(N>=1)个商品（非变形）放入指定分类中
	  * @author jiaqi.Dai
	  * @param dctx
	  * @param context
	  * @param productId 指定商品ID
	  * @param productIdLike 指定商品ID中的某些字符（模糊）
	  * @param seasonId 季
	  * @param seriesId 系列
	  * @param productTypeId 商品类型
	  * @param toCategoryId 放入那个分类
	  * @return
	  */
	 public static Map<String, Object> putRealProductsInCategory(DispatchContext dctx, Map<String, ? extends Object> context) {
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 LocalDispatcher dispatcher = dctx.getDispatcher();
         Delegator delegator = dctx.getDelegator();	         
    	 GenericValue userLogin = (GenericValue) context.get("userLogin");
    	 //需要放入的商品ID
    	 String productId = (String) context.get("productId");
    	 //模糊查询的商品ID
    	 String productIdLike = (String) context.get("productIdLike");
    	 //季
    	 String seasonId = (String) context.get("seasonId");
    	 //系列
    	 String seriesId = (String) context.get("seriesId");
    	 //商品类型
    	 String productTypeId = (String) context.get("productTypeId");
    	 //目标分类
    	 String toCategoryId = (String) context.get("toCategoryId");
    	 
    	 try {
			List<GenericValue> productList = getPutProductList(delegator, productId, productTypeId, productIdLike, seasonId, seriesId);
			for (GenericValue product : productList) {
				//遍历放入指定分类中
				addProductToCategory(delegator, dispatcher, product, toCategoryId, userLogin);
			}
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 return result;
	 }

	private static void addProductToCategory(Delegator delegator,LocalDispatcher dispatcher, GenericValue product, String toCategoryId, GenericValue userLogin) throws GenericServiceException, GenericEntityException {		
		List<GenericValue> categoryMember = delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productId", product.get("productId"), "productCategoryId", toCategoryId));
		if (UtilValidate.isEmpty(EntityUtil.getFirst(categoryMember))) {
			dispatcher.runSync("addProductToCategory", UtilMisc.toMap("productId", product.get("productId"), "productCategoryId", toCategoryId, "userLogin", userLogin));
		}
		if (!toCategoryId.equals(product.get("primaryProductCategoryId"))) {
			product.set("primaryProductCategoryId", toCategoryId);
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
		}
	}

	private static List<GenericValue> getPutProductList(Delegator delegator,String productId, String productTypeId, String productIdLike,
			String seasonId, String seriesId) throws GenericEntityException {
		List<GenericValue> productList = new ArrayList<GenericValue>();
		//是否指定商品ID
		if (UtilValidate.isNotEmpty(productId)) {
			//查询指定商品放入返回集合
			GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId", productId));
			if (UtilValidate.isNotEmpty(product)) {
				productList.add(product);
			}
		} else {
			//按照季和系列查询所有组别		
			List<GenericValue> groupList = delegator.findByAnd("ProductCategoryGroupView", UtilMisc.toMap("seriesId" ,seriesId ,"seasonId" , seasonId));
			for (GenericValue group : groupList) {
				//根据组别查询所有关联商品
				List<GenericValue> productCategoryMemberList = delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", group.get("groupId")));
				for (GenericValue productCategoryMember : productCategoryMemberList) {
					//根据商品类型过滤查询结果，查询商品数据
					GenericValue queryProduct = EntityUtil.getFirst(delegator.findByAnd("Product", UtilMisc.toMap("productId", productCategoryMember.get("productId"), "productTypeId", productTypeId , "isVariant", "N")));
					if (UtilValidate.isNotEmpty(queryProduct)) {
						productList.add(queryProduct);
					}
				}
			}
			//需要指定批量的商品ID 进行字符串比较过滤
			if (UtilValidate.isNotEmpty(productIdLike)) {
				for (int i = 0; i < productList.size(); i++) {
					String queryProductId = (String) productList.get(i).get("productId");
					if(!queryProductId.matches(productIdLike)) {
						productList.remove(i);
						i--;
					}
				}
			}
			
		}
		return productList;
	}
}
