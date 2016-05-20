import org.ofbiz.base.util.*
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import com.zuczug.product.ZuczugProductUtils;
import com.zuczug.product.ProductBomUtils;

module = "QueryProductCategory.groovy"; 
productCategoryId = "";

if (UtilValidate.isNotEmpty(productIdTo)) {
	product = delegator.findByPrimaryKey("Product", [productId:productIdTo]);
	
	if (product.isVariant=="N") {
		productCategoryId = product.primaryProductCategoryId;
		
	} else {
		productAssocs = FastMap.newInstance();		
		productAssocs = delegator.findByAnd("ProductAssoc", [productIdTo:productIdTo, productAssocTypeId:"PRODUCT_VARIANT"]);
		productAssoc = EntityUtil.getFirst(productAssocs);
		
		virtualProductId = productAssoc.productId;
		virtualProduct = delegator.findByPrimaryKey("Product", [productId:virtualProductId]);
		productCategoryId = virtualProduct.primaryProductCategoryId;		
	}
}
context.productCategoryId = productCategoryId;