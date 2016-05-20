import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;
productColorFeatures = FastList.newInstance();
	productSizeFeatures = FastList.newInstance();
	
productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", [productCategoryId:productCategoryId,thruDate:null]);



productFeatureCategoryAppls.each { productFeatureCategoryAppl ->
	productFeatureCategory = productFeatureCategoryAppl.getRelatedOne("ProductFeatureCategory");
	groupMap = FastMap.newInstance();
	groupMap.put("productFeatureCategory", productFeatureCategory);
	productFeatureAppls = delegator.findByAnd("ProductFeature", [productFeatureCategoryId:productFeatureCategory.productFeatureCategoryId]);
	
	productFeatureAppls.each { productFeatureAppl ->
		if (productFeatureAppl.productFeatureTypeId=="COLOR") {
			productColorFeatures.add(productFeatureAppl);
		} else if (productFeatureAppl.productFeatureTypeId=="SIZE") {
			productSizeFeatures.add(productFeatureAppl);
		}
		
	}
}

context.productSizeFeatures = productSizeFeatures;
context.productColorFeatures = productColorFeatures;