/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import org.ofbiz.base.util.*
import java.sql.Timestamp;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.base.util.UtilValidate;

module = "GetDesignProductColor.groovy"
context.nowDate = UtilDateTime.nowDate();
context.nowTimestampString = UtilHttp.encodeBlanks(UtilDateTime.nowTimestamp().toString());
if (parameters.userLogin) {
    userLogin = parameters.userLogin;
} 
boolean useValues = true;
if (request.getAttribute("_ERROR_MESSAGE_")) useValues = false;

context.productId = parameters.productId;
colorId = parameters.colorId;

if (UtilValidate.isEmpty(colorId)) { //如果没有colorId，那就什么也做不了
	Debug.logInfo("+++++++++++++++++++++++++++++++++++++++no color and size",module);
	useValues = false;
	context.useValues = useValues;
	return;
} else { //否则就根据颜色查找size为4的变型商品，记住：设计师的商品就是固定4码的。
	productColorSizes = delegator.findByAnd("ProductColorSizeView", [virtualProductId:productId, colorId:colorId, sizeId:"SIZE_4"]);	
	if (UtilValidate.isNotEmpty(productColorSizes)) {
		productColorSize = EntityUtil.getFirst(productColorSizes);
		variantProductId = productColorSize.productId;		
	} else { //不存在该变型商品，所以要添加
		
	}
}

bomConfirm = delegator.findByAnd("ProductAttribute", [productId:variantProductId, attrName:"BOM_CONFIRM"]);
if (UtilValidate.isEmpty(bomConfirm) ) {
	isBomConfirm = 'false';
} else {
	isBomConfirm = 'true';
}

colorId = parameters.colorId;
productAssocs = delegator.findByAnd("ProductAssoc", [productId:productId, productAssocTypeId:"PRODUCT_VARIANT"]);
for(productAssoc in productAssocs) {
	productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", [productId:productAssoc.productIdTo, productFeatureId:colorId]);
	if (UtilValidate.isNotEmpty(productFeatureAppls)) {
		bomConfirm = delegator.findByAnd("ProductAttribute", [productId:productAssoc.productIdTo, attrName:"BOM_CONFIRM"]);
		if (UtilValidate.isNotEmpty(bomConfirm) ) {
			isBomConfirm = 'true';
			break;
		}
	}
}
variantProduct = delegator.findByPrimaryKey("Product", [productId:variantProductId]);

context.isBomConfirm = isBomConfirm;
context.variantProductId = variantProductId;
context.variantProduct = variantProduct;
context.colorId = colorId;

productIdTo = parameters.productIdTo;
updateMode = parameters.UPDATE_MODE;

if (productIdTo) context.productIdTo = productIdTo;

productAssocTypeId = parameters.productAssocTypeId;
if (productAssocTypeId) context.productAssocTypeId = productAssocTypeId;

fromDateStr = parameters.fromDate;

Timestamp fromDate = null;
if (fromDateStr) fromDate = Timestamp.valueOf(fromDateStr) ?: (Timestamp)request.getAttribute("ProductAssocCreateFromDate");;
context.fromDate = fromDate;