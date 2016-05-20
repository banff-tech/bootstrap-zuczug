import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;

module = "EditVirtualProduct.groovy"; 
groupMaps = FastList.newInstance();
groupMaps2 = FastList.newInstance();
productMap = FastMap.newInstance();

if (UtilValidate.isNotEmpty(productId)) {
	productMap.put("productId", productId);
	product = delegator.findByPrimaryKey("Product", [productId:productId]);
	if (UtilValidate.isNotEmpty(product.internalName)) {
		productMap.put("internalName", product.internalName);
	}
	if(product.isVariant=="Y") {
		productMap.put("productCategoryId", product.primaryProductCategoryId);		
	} else {
		productMap.put("productCategoryId", productCategoryId);		
	}
	productMap.put("comments", product.comments);
	productMap.put("isVariant", product.isVariant)
	updateMode="UPDATE";
} else {
	productMap.put("isVariant", 'N')
	productMap.put("productCategoryId", productCategoryId);
	if (UtilValidate.isNotEmpty(internalName)) {
		productMap.put("internalName", internalName);
	}
	productMap.put("comments", comments);
	updateMode="CREATE";
}

if (UtilValidate.isNotEmpty(virtualProductId)) {
	product = delegator.findByPrimaryKey("Product", [productId:virtualProductId]);
	if (UtilValidate.isNotEmpty(product)) {
		if (UtilValidate.isNotEmpty(product.internalName)) {
			productMap.put("internalName", product.internalName);
		}
		productMap.put("productTypeId", product.productTypeId);
		productMap.put("productCategoryId", product.primaryProductCategoryId);
		productMap.put("comments", product.comments);
	}	
}

if(UtilValidate.isEmpty(productCategoryId)) {
	product = delegator.findByPrimaryKey("Product", [productId:productId]);
	if (product.isVariant == "Y") {
		virtualProductAssocs = delegator.findByAnd("ProductAssoc", [productIdTo:productId, productAssocTypeId: "PRODUCT_VARIANT"]);
		virtualProductAssoc = EntityUtil.getFirst(virtualProductAssocs);
		vProduct = delegator.findByPrimaryKey("Product", [productId:virtualProductAssoc.productId]);
		productCategoryId = vProduct.primaryProductCategoryId;
		productMap.put("productCategoryId", productCategoryId);
	} else {
		productCategoryId = product.primaryProductCategoryId;
		productMap.put("productCategoryId", productCategoryId);
	}
	
}

productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", [productCategoryId:productCategoryId,thruDate:null]);
productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", [productCategoryId:productCategoryId,thruDate:null]);

productFeatureCatGrpAppls.each { productFeatureCatGrpAppl ->	
	productFeatureGroup = productFeatureCatGrpAppl.getRelatedOne("ProductFeatureGroup");
	groupMap = FastMap.newInstance();
	groupMap.put("productFeatureGroup", productFeatureGroup);
	productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", [productFeatureGroupId:productFeatureCatGrpAppl.productFeatureGroupId,thruDate:null]);
	productFeatures = FastList.newInstance();
	productFeatureGroupAppls.each { productFeatureGroupAppl ->
		productFeature = delegator.findByPrimaryKey("ProductFeature", [productFeatureId:productFeatureGroupAppl.productFeatureId]);
		productFeatures.add(productFeature);
	}
	groupMap.put("productFeatures", productFeatures);
	groupMaps.add(groupMap);
	if (UtilValidate.isNotEmpty(productId)) {
		productFeatureAppls = ZuczugProductUtils.findFeatureByGroup(delegator, productId, productFeatureCatGrpAppl.getString("productFeatureGroupId"));
		if (UtilValidate.isNotEmpty(productFeatureAppls)) {
			productFeatureAppl = EntityUtil.getFirst(productFeatureAppls);
			productMap.put(productFeatureCatGrpAppl.getString("productFeatureGroupId"), productFeatureAppl.getString("productFeatureId"));
		}
	}
}

productFeatureCategoryAppls.each { productFeatureCategoryAppl ->
	productFeatureCategory = productFeatureCategoryAppl.getRelatedOne("ProductFeatureCategory");
	groupMap2 = FastMap.newInstance();
	groupMap2.put("productFeatureCategory", productFeatureCategory);
	productFeatureAppls = delegator.findByAnd("ProductFeature", [productFeatureCategoryId:productFeatureCategory.productFeatureCategoryId]);
	productFeatures = FastList.newInstance();
	productFeatureAppls.each { productFeatureAppl ->
		productFeatures.add(productFeatureAppl);
	}
	groupMap2.put("productFeatures", productFeatures);
	groupMaps2.add(groupMap2);
	if (UtilValidate.isNotEmpty(productId)) {
		productFeatureAppls = ZuczugProductUtils.findFeatureByCategory(delegator, productId, productFeatureCategoryAppl.getString("productFeatureCategoryId"));
		if (UtilValidate.isNotEmpty(productFeatureAppls)) {
			productFeatureAppl = EntityUtil.getFirst(productFeatureAppls);
			productMap.put("productFeatureCategory_" + productFeatureCategoryAppl.getString("productFeatureCategoryId"), productFeatureAppl.getString("productFeatureId"));
		}
	}
}

context.groupMaps = groupMaps;
context.groupMaps2 = groupMaps2;
context.productMap = productMap;
context.updateMode = updateMode;