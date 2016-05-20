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



public class OldZuczugProductServices {
	
	public static final String module = OldZuczugProductServices.class.getName();
    public static final String resource = "ZuczugCommonUiLabels";
	
    /**
     * 拷贝虚拟商品的所有GoodIdentitification到变型商品 作者未知
     * @param dctx
     * @param context
     * @return
     */
	public static Map<String, Object> copyGoodIdToVariants(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String,Object> result = ServiceUtil.returnSuccess();
		
		LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String virtualProductId = (String) context.get("virtualProductId");
        String variantProductId = (String) context.get("variantProductId");
        
        try {
        	List<GenericValue> ids = delegator.findByAnd("GoodIdentification", UtilMisc.toMap("productId",virtualProductId));
    
			// 获取该虚拟商品的所有变型商品
			List<GenericValue> productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"), UtilMisc.toList("productId"));
			productAssocs = EntityUtil.filterByDate(productAssocs);
		
			for (GenericValue id : ids) {
				for (GenericValue pa : productAssocs) {
					//先删除变型商品对应的idType
					delegator.removeByAnd("GoodIdentification", UtilMisc.toMap("productId",pa.getString("productIdTo"),"goodIdentificationTypeId",id.getString("goodIdentificationTypeId")));
					
					Map<String,Object> newId = FastMap.newInstance();
					newId.put("productId", pa.getString("productIdTo"));
					newId.put("goodIdentificationTypeId", id.getString("goodIdentificationTypeId"));
					newId.put("idValue", id.getString("idValue"));
					newId.put("userLogin", context.get("userLogin"));
					dispatcher.runSync("createGoodIdentification", newId);
					
				}
			}
			
        } catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		
		return result;
	}



    

   
}
