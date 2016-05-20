import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;

/**
*把参数中的 goodIdentificationTypeId的复制到变型商品 by sven
*/
public Map copyProductIdValuesToVariant(){
	result = ServiceUtil.returnSuccess();
	// 获取该虚拟商品的所有变型商品
	def productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", virtualProductId, "productAssocTypeId", "PRODUCT_VARIANT"));
	productAssocs = EntityUtil.filterByDate(productAssocs);
	//循环改变每一个变型商品
	productAssocs.each{pa->
		//goodIdentificationTypeId，如果没有，就是所有的
		if(goodIdentificationTypeIds){
			goodIdentificationTypeIds.each{ type ->
				virtualGi = delegator.findOne("GoodIdentification",false,[productId:virtualProductId,goodIdentificationTypeId:type]);
				if(virtualGi){
					removeAndMakeNewGoodIdentification(delegator, pa.getString("productIdTo"),type,virtualGi.getString("idValue"));
				}
			}
		}else{
			def virtualGis = delegator.findByAnd("GoodIdentification",[productId:virtualProductId]);
			virtualGis.each{virtualGi->
				removeAndMakeNewGoodIdentification(delegator, pa.getString("productIdTo"),virtualGi.getString("goodIdentificationTypeId"),virtualGi.getString("idValue"));
			}
		}
	}
	return result;
}

//删除原来的 giType 类型的idValue，并且新增 giType类型的idValue by sven
public static removeAndMakeNewGoodIdentification(delegator, productId,giType,idValue){
	//println("=====removeAndMakeNewGoodIdentification" + productId);
	delegator.removeByAnd("GoodIdentification", [productId:productId,goodIdentificationTypeId:giType]);
	def newGi = delegator.makeValue("GoodIdentification");
	newGi.set("idValue",idValue);
	newGi.set("productId",productId);
	newGi.set("goodIdentificationTypeId",giType);
	newGi.create();
}

/*
创建或者更新一个goodIdentification by sven
*/
public Map createOrUpdateGoodIdentification(){
	result = ServiceUtil.returnSuccess();
	println("===============idValue" + idValue);
	if(idValue){
		giList = delegator.findByAnd("GoodIdentification",[productId:productId,goodIdentificationTypeId:goodIdentificationTypeId]);
		if(giList){
			serviceName="updateGoodIdentification";
		}else{
			serviceName="createGoodIdentification";
		}
		dispatcher.runSync(serviceName,[productId:productId,goodIdentificationTypeId:goodIdentificationTypeId,idValue:idValue,userLogin:userLogin]);		

	}else{
		delegator.removeByAnd("GoodIdentification",[productId:productId,goodIdentificationTypeId:goodIdentificationTypeId]);
		//如果idValue没有，就删掉
		//dispatcher.runSync("",[productId:productId,goodIdentificationTypeId:goodIdentificationTypeId,userLogin:userLogin]);
	}
	
	return result;
}

//在lookup的时候，找到变型商品所有颜色的4码，过滤掉其他的 by sven
public Map findProductVariantMiddleSize(){
	result = ServiceUtil.returnSuccess();
	EntityFindOptions findOptions = new EntityFindOptions();
	if(searchValue){
		productId=searchValue;
		productName=searchValue;
	}
    if(productId || productName){
    	productConds = [];
	    productConds.add(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, "Y"));
	    productConds.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
		if(searchValue){
			tempCond = [];
	    	tempCond.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE, productId + "%"));
	    	tempCond.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + productName + "%"));
			productConds.add(EntityCondition.makeCondition(tempCond, EntityOperator.OR));
			
			
		    findOptions.setMaxRows(5);
		    findOptions.setDistinct(true);
		}else{
			if(productId){
		    	productConds.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE, productId + "%"));
			}
			if(productName){
				productConds.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + productName + "%"));
			}
		}	   
	    
		
		products = delegator.findList("Product", EntityCondition.makeCondition(productConds, EntityOperator.AND), UtilMisc.toSet("productId"), null, findOptions, false);
		variantProductIds = [];
		if(products){
			products.each{product->
				virtualProductId = product.getString("productId");
				//找到这个商品的所有颜色
				features = delegator.findByAndCache("ProductDesignFeatureWithType",[productId:virtualProductId,"productFeatureTypeId":"COLOR"]);
				features = EntityUtil.filterByDate(features);
				if(features){
					features.each{feature->
						if(feature.getString("productFeatureId").indexOf("GENERAL_COLOR") < 0){
							
							//找到这个颜色的中间码
							variantProductId = com.zuczug.product.ZuczugProductUtils.checkProductMiddleSize(virtualProductId,feature.getString("productFeatureId"),delegator,dispatcher);
							if(variantProductId){
								variantProductIds.add(variantProductId);
							}
						}
					}
				}
			}
			listIt = [];
			variantProductIds.each{variantProductId->
				product = delegator.findOne("Product",false,[productId:variantProductId]);
				listIt.add(product);
			}
			println("==========listIt " + listIt.size());
			result.put("listIt",listIt);
		}	
    }
    

	return result;
}

def setDescription(agrs){
	if(parameters.productId || parameters.productName){
		agrs.description="true";
	}
}

//单独添加素然物料编号，因为不能重复，变型的忽略 by sven
public Map createZuczugCodeGoodIdentification(){
	result = ServiceUtil.returnSuccess();
	
	checkList = delegator.findByAnd("GoodIdentification",[goodIdentificationTypeId:"ZUCZUG_CODE",idValue:idValue]);
	isCreate =true;
	if(checkList && checkList.size()>0){
		checkList.each{gi->
			assocs = delegator.findByAnd("ProductAssoc",[productId:productId,productIdTo:gi.getString("productId"),"productAssocTypeId":"PRODUCT_VARIANT"]);

			if(assocs.size()<=0){
				isCreate=false;
				
			}
		}
	}
	if(isCreate){
		dispatcher.runSync("createGoodIdentification", [productId:productId,goodIdentificationTypeId:"ZUCZUG_CODE",idValue:idValue,userLogin:userLogin]);
	}else{
		return ServiceUtil.returnError("素然物料编号[" + idValue + "]不能重复");
	}

	return result;
}

//在更新素然CODE的时候，检查是否有重复 by sven
public Map updateZuczugCodeGoodIdentification(){
	result = ServiceUtil.returnSuccess();
	
	checkList = delegator.findByAnd("GoodIdentification",[goodIdentificationTypeId:"ZUCZUG_CODE",idValue:idValue]);
	isUpdate = true;
	if(checkList && checkList.size()>0){
		checkList.each{gi->
			assocs = delegator.findByAnd("ProductAssoc",[productId:productId,productIdTo:gi.getString("productId"),"productAssocTypeId":"PRODUCT_VARIANT"]);
			//如果重复数据的不等于当前更新的，也不是当前更新的变形，也报错
			if(assocs.size()<=0 && gi.getString("productId")!=productId){
				isUpdate=false;
			}
		}
	}
	if(isUpdate){
		removeAndMakeNewGoodIdentification(delegator,productId,"ZUCZUG_CODE",idValue);
	}else{
		return ServiceUtil.returnError("素然物料编号[" + idValue + "]不能重复");
	}
	
	return result;
}

//把属性拷贝过去，如果存在就跳过，没有就新增
public Map copyProductAttributeToVariantNoCover(){
	result = ServiceUtil.returnSuccess();
	
	virtualList = delegator.findByAnd("ProductAttribute",[productId:virtualProductId]);
	assocs = delegator.findByAnd("ProductAssoc",[productId:virtualProductId,"productAssocTypeId":"PRODUCT_VARIANT"]);
	assocs.each{assoc->
		virtualList.each{attr->
			newAttr = attr.clone();
			checkAttr = delegator.findByAnd("ProductAttribute",[productId:assoc.getString("productIdTo"),attrName:attr.getString("attrName")]);
			if(!checkAttr){
				newAttr.set("productId",assoc.getString("productIdTo"));
				newAttr.create();
			}
		}
	}
	
	return result;
}

//创建或者更新一个ProductAttribute
public Map createOrUpdateProductAttribute(){
	result = ServiceUtil.returnSuccess();
	
	attr = delegator.findByAnd("ProductAttribute",[productId:productId,attrName:attrName]);
	if(attr){
		serviceName = "updateProductAttribute";
	}else{
		serviceName = "createProductAttribute";
	}
	
	dispatcher.runSync(serviceName,[productId:productId,attrName:attrName,attrValue:attrValue,userLogin:userLogin]);		
	return result;
}





