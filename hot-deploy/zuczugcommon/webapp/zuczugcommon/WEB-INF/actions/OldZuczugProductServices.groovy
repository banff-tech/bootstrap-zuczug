import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;

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