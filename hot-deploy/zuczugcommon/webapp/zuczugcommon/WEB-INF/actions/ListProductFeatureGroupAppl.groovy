import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;

module = "ListProductFeatureGroupAppl.groovy"; 
productFeatures = FastList.newInstance();

productFeatureGroupAppls = delegator.findByAnd("ProductFeatureGroupAppl", [productFeatureGroupId:productFeatureGroupId,thruDate:null]);
productFeatureGroupAppls.each { productFeatureGroupAppl ->
	productFeature = delegator.findByPrimaryKey("ProductFeature", [productFeatureId:productFeatureGroupAppl.productFeatureId]);
	productFeatures.add(productFeature);
}
context.productFeatures = productFeatures;