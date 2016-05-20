import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;

module = "EditProductFeatureCatGrpAppl.groovy"; 
productFeatureGroups = FastList.newInstance();

productFeatureCatGrpAppls = delegator.findByAnd("ProductFeatureCatGrpAppl", [productCategoryId:productCategoryId]);
productFeatureCatGrpAppls.each { productFeatureCatGrpAppl ->
	productFeatureGroup = delegator.findByPrimaryKey("ProductFeatureGroup", [productFeatureGroupId:productFeatureCatGrpAppl.productFeatureGroupId]);
	productFeatureGroups.add(productFeatureGroup);
}
context.productFeatureGroups = productFeatureGroups;