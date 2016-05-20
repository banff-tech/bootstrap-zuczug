import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;

module = "ListProductFeatureCategoryAppl.groovy"; 
productFeatureCategories = FastList.newInstance();

productFeatureCategoryAppls = delegator.findByAnd("ProductFeatureCategoryAppl", [productCategoryId:productCategoryId]);
productFeatureCategoryAppls.each { productFeatureCategoryAppl ->
	productFeatureCategory = delegator.findByPrimaryKey("ProductFeatureCategory", [productFeatureCategoryId:productFeatureCategoryAppl.productFeatureCategoryId]);
	productFeatureCategories.add(productFeatureCategory);
}
context.productFeatureCategories = productFeatureCategories;