import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;

module = "EditProduct.groovy"; 
groupMaps = FastList.newInstance();
productMap = FastMap.newInstance();

if (UtilValidate.isNotEmpty(productId)) {
	productMap.put("productId", productId);
	product = delegator.findByPrimaryKey("Product", [productId:productId]);
	productMap.put("internalName", product.internalName);
	productMap.put("productTypeId", product.productTypeId);
	productMap.put("productCategoryId", product.primaryProductCategoryId);
	updateMode="UPDATE";
} else {
	productMap.put("productTypeId", productTypeId);
	productMap.put("productCategoryId", productCategoryId);
	updateMode="CREATE";
}
productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", [productCategoryId:product==null ? parameters.productCategoryId:product.primaryProductCategoryId]);

productFeatureCatGrpAppls.each { productFeatureCatGrpAppl ->
	productFeatureGroup = productFeatureCatGrpAppl.getRelatedOne("ProductFeatureGroup");
	groupMap = FastMap.newInstance();
	groupMap.put("productFeatureGroup", productFeatureGroup);
	productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", [productFeatureGroupId:productFeatureCatGrpAppl.productFeatureGroupId]);
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
context.groupMaps = groupMaps;
context.productMap = productMap;
context.updateMode = updateMode;