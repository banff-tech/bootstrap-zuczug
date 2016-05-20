import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*


/**
*
* 查询有缺少的数据，并且补全
*
*/

def delegator;
def dispatcher;
def userLogin;

public Map fixData(){
	result = ServiceUtil.returnSuccess();
	delegator = this.delegator;
	dispatcher = this.dispatcher;

	//fixZuczugCode();//面辅料的素然面料编号
	//fixFAYSuppliers();//面辅料的供应商
	//fixFormalData();	//修复正式商品的变型组别、季、系列、款型
	//createAllPersonByErpVip()
	updateAllPersonTimecardAccount()
	return result;
}
//根据erpVip信息,生成会员信息
def createAllPersonByErpVip(){
	userLogin = delegator.findOne("UserLogin",[userLoginId:"system"],true)
	conds = EntityCondition.makeCondition(EntityOperator.OR,
			EntityCondition.makeCondition("customerId", EntityOperator.EQUALS, "EC00"),
			EntityCondition.makeCondition("customerId", EntityOperator.LIKE, "R%"))
	erpVipList = delegator.findList("ErpVip", conds, null, null, null, false)
/*
	erpVip = delegator.findOne("ErpVip", [vipcode:"00007040"],false)
*/
	erpVipList.each { erpVip ->
		Debug.log("#######load erpVip[" + erpVip.name + "] start")
		personList = delegator.findByAnd("Person", [cardId: erpVip.vipcode])
		if (UtilValidate.isNotEmpty(personList)) return
		gender = erpVip.sex == '男' ? 'M' : 'F'
		java.sql.Date birthDate = java.sql.Date.valueOf(erpVip.birthdate)
		if("沈洁<瘦＞"==erpVip.name) erpVip.name="沈洁瘦"
		personResult = dispatcher.runSync("createPerson",
				[cardId   : erpVip.vipcode,
				 firstName: erpVip.name,
				 gender   : gender,
				 memberId : erpVip.customerId,
				 birthDate: birthDate,
				 userLogin: userLogin
				])
		dispatcher.runSync("createPartyRole",
				[partyId   : personResult.partyId,
				 roleTypeId: "CUSTOMER",
				 userLogin : userLogin
				])
		contactNumber = UtilValidate.isNotEmpty(erpVip.mobiletel) ? erpVip.mobiletel : erpVip.mobiletel
		if (contactNumber) {
			dispatcher.runSync("createPartyTelecomNumber",
					[partyId                 : personResult.partyId,
					 contactMechPurposeTypeId: "PHONE_MOBILE",
					 contactNumber           : contactNumber,
					 userLogin               : userLogin
					])
		}
		dispatcher.runSync("createPartyClassification",
				[partyId                   : personResult.partyId,
				 partyClassificationGroupId: "VipClassification",
				 userLogin                 : userLogin
				])
		//获取积分账号
		if (UtilValidate.isNotEmpty(erpVip.integralAmount) && erpVip.integralAmount.compareTo(BigDecimal.ZERO) > 0) {
			timeCardAccountList = delegator.findByAnd("TimeCardAccount", ["ownerPartyId": personResult.partyId])
			if (UtilValidate.isEmpty(timeCardAccountList)) {
				dispatcher.runSync("createTimeCardAccount",
						["timecardAccountTypeId": "Store",
						 "ownerPartyId"         : personResult.partyId,
						 "userLogin"            : userLogin])
				timeCardAccountList = delegator.findByAnd("TimeCardAccount", ["ownerPartyId": personResult.partyId])
			}
			if (UtilValidate.isNotEmpty(timeCardAccountList)) {
				timeCardAccount = EntityUtil.getFirst(timeCardAccountList);
				//如果积分帐号不为空，记录订单付款成功后积分明细
				timecardAccountId = timeCardAccount.timecardAccountId
				actualBalanceTotal = timeCardAccount.actualBalanceTotal//当前实际积分
				if (UtilValidate.isEmpty(actualBalanceTotal)) {
					actualBalanceTotal = BigDecimal.ZERO;
					timecardAccountItemId = delegator.getNextSeqId("TimeCardAccountItem");
					timeCardAccountItem = delegator.makeValue("TimeCardAccountItem",
							["timecardAccountId"    : timecardAccountId,
							 "timecardAccountItemId": timecardAccountItemId])
					timeCardAccountItem.set("actualBalance", erpVip.integralAmount);
					timeCardAccountItem.set("availableBalance", erpVip.integralAmount);
					timeCardAccountItem.create();
					actualBalanceTotal = actualBalanceTotal.add(erpVip.integralAmount);
					//更新实际积分
					timeCardAccount.set("actualBalanceTotal", actualBalanceTotal);
					timeCardAccount.set("availableBalanceTotal", actualBalanceTotal);
					timeCardAccount.store();
				}
			}
		}
		Debug.log("#######create new person[" + erpVip.name + "] success")
	}

}
//根据erpVip信息,生成会员信息
def updateAllPersonTimecardAccount(){
	userLogin = delegator.findOne("UserLogin",[userLoginId:"system"],true)
	cond = EntityCondition.makeCondition("integralAmount",EntityOperator.GREATER_THAN,BigDecimal.ZERO);
	erpVipList = delegator.findList("ErpVip", cond, null, null, null, false)
	erpVipList.each { erpVip ->
		personList = delegator.findByAnd("Person",[cardId: erpVip.vipcode])
		if(UtilValidate.isNotEmpty(personList)){
			//获取积分账号
			if (UtilValidate.isNotEmpty(erpVip.integralAmount) && erpVip.integralAmount.compareTo(BigDecimal.ZERO) > 0) {
				timeCardAccountList = delegator.findByAnd("TimeCardAccount", ["ownerPartyId": personList[0].partyId])
				if (UtilValidate.isEmpty(timeCardAccountList)) {
					dispatcher.runSync("createTimeCardAccount",
							["timecardAccountTypeId": "Store",
							 "ownerPartyId"         : personList[0].partyId,
							 "userLogin"            : userLogin])
					timeCardAccountList = delegator.findByAnd("TimeCardAccount", ["ownerPartyId": personList[0].partyId])
					timeCardAccount = EntityUtil.getFirst(timeCardAccountList);
					//如果积分帐号不为空，记录订单付款成功后积分明细
					timecardAccountId = timeCardAccount.timecardAccountId
					actualBalanceTotal = timeCardAccount.actualBalanceTotal//当前实际积分
					if (UtilValidate.isEmpty(actualBalanceTotal)) {
						actualBalanceTotal = BigDecimal.ZERO;
						timecardAccountItemId = delegator.getNextSeqId("TimeCardAccountItem");
						timeCardAccountItem = delegator.makeValue("TimeCardAccountItem",
								["timecardAccountId"    : timecardAccountId,
								 "timecardAccountItemId": timecardAccountItemId])
						timeCardAccountItem.set("actualBalance", erpVip.integralAmount);
						timeCardAccountItem.set("availableBalance", erpVip.integralAmount);
						timeCardAccountItem.create();
						actualBalanceTotal = actualBalanceTotal.add(erpVip.integralAmount);
						//更新实际积分
						timeCardAccount.set("actualBalanceTotal", actualBalanceTotal);
						timeCardAccount.set("availableBalanceTotal", actualBalanceTotal);
						timeCardAccount.store();
					}
				}
			}
		}
		Debug.log("#######update person timecard account[" + erpVip.name + "] success")
	}

}

//面辅料的素然内部编号
def fixZuczugCode(){
	productType = ["FABRIC","ACCESSORIES","YARN"];

	for(fayType in productType) { //循环面料、辅料和纱线
		//找到所有的面料的变型商品
		inMap = [productFeatureId:fayType,isVariant:"Y"];
		fayVariantList = delegator.findByAnd("AvailableProductSupplierView",inMap); //找到所有的变型商品
		fayVariantList.each{fay->
			virtualProduct = findProductVirtual(fay.getString("productId"));	//得到虚拟商品
			if(virtualProduct){
				//先看当前变型是否有素然物料编号
				variantGi = delegator.findOne("GoodIdentification",false,[productId:fay.getString("productId"),goodIdentificationTypeId:"ZUCZUG_CODE"]);
				goodIdentificationTypeIds = ["ZUCZUG_CODE"];
				if(!variantGi){//当不存在的时候，就复制虚拟的到变型，存在就跳过
					//调用服务更新那个变型商品的素然面料编号
					dispatcher.runSync("copyProductIdValuesToVariant",[userLogin:userLogin,virtualProductId:virtualProduct.getString("productId"),variantProductId:fay.getString("productId"),goodIdentificationTypeIds:goodIdentificationTypeIds]);
					//println("需要补充素然面料编号的有："+  "fay=" + fay + " ===="+fay.getString("productId"))
				}
			}
		}
	}	
}

//同步虚拟商品的所有供应商信息到变型商品
def fixFAYSuppliers(){
	productType = ["FABRIC","ACCESSORIES","YARN"];
	for(fayType in productType) { //循环面料、辅料和纱线
		inMap = [productFeatureId:fayType,isVariant:"Y"];
		fayVariantList = delegator.findByAnd("AvailableProductSupplierView",inMap); //找到所有的变型商品
		fayVariantList.each{fay->
			virtualProduct = findProductVirtual(fay.getString("productId"));	//得到虚拟商品
			if(virtualProduct){
				//调用services 复制所有的供应商到变形商品
				dispatcher.runSync("copyProductSupplierToVariants",["virtualProductId":virtualProduct.getString("productId"),variantProductId:fay.getString("productId")]);
			}
		}
	}	

}

//补全正式商品中变型商品缺失字段，当前有组别（包季、系列）、款型
def fixFormalData(){
	//找到所有的正式商品的变型
	cond = [productTypeId:"FINISHED_GOOD",isVariant:"Y",isVirtual:"N"];
	allFormalVariant = delegator.findByAnd("Product",cond);
	allFormalVariant.each{ variant->
		//找到这个商品的虚拟
		virtualProduct = findProductVirtual(variant.getString("productId"));	//得到虚拟商品
		if(virtualProduct){
			//同步组别 //同步款型(因为组别和款型都是STAND_FEATURE，所以可以调用这个服务来同步)
			inMap = [
			variantProductId:variant.getString("productId"),
			virtualProductId:virtualProduct.getString("productId"),
			userLogin:userLogin
			];
			dispatcher.runSync("copyStandardFeaturesToVariants",inMap);
		}
	}
}

//补全正式商品虚拟的部分信息，比如组别等 ***未实现***
def fixDesignProductData(){
	//找到所有的正式商品的变型
	cond = [productTypeId:"FINISHED_GOOD",isVariant:"N",isVirtual:"Y"];
	allFormalVirtual = delegator.findByAnd("Product",cond);
	allFormalVirtual.each{ virtual->
		//找到这个商品的虚拟
		designProduct = findDesignProduct(virtual.getString("productId"));	//得到虚拟商品
		if(designProduct){
			//同步组别 //同步款型(因为组别和款型都是STAND_FEATURE，所以可以调用这个服务来同步)
			inMap = [
			variantProductId:virtual.getString("productId"),
			virtualProductId:virtualProduct.getString("productId"),
			userLogin:userLogin
			];
			dispatcher.runSync("copyStandardFeaturesToVariants",inMap);
		}
	}
}

//补充辅料的CustomCalcMethod字段
def fixAccessoryBomCalcMethod(){
	result = ServiceUtil.returnSuccess();
	//定义一个VIEW，查询所有是[辅料]、类型是[ENGINEER_COMPONENT]或者[MANUF_COMPONENT]、并且商品的quantityUomId是Len_m 的的BOM
	allAccessoryBom = delegator.findList("AllAccessoryBom", null, null, null, null, false);
	allAccessoryBom.each{ bom ->

		if(UtilValidate.isEmpty(bom.getString("estimateCalcMethod"))){
			
			cond = [productId:bom.getString("productId"),
					productIdTo:bom.getString("productIdTo"),
					productAssocTypeId:bom.getString("productAssocTypeId"),
					fromDate:bom.getTimestamp("fromDate")
					];
			oldBom = delegator.findOne("ProductAssoc",false,cond);
			oldBom.set("estimateCalcMethod","CM_TO_METER_FORMULA");
			oldBom.store();
		}	

	}

	return result;
}

//找到一个商品的虚拟商品
def findProductVirtual(productId){
	paList = delegator.findByAnd("ProductAssoc",[productIdTo:productId,productAssocTypeId:"PRODUCT_VARIANT"]);
	return EntityUtil.getFirst(paList);
}

//找到一个商品的设计商品
def findDesignProduct(productId){
	paList = delegator.findByAnd("ProductAssoc",[productIdTo:productId,productAssocTypeId:"UNIQUE_ITEM"]);
	return EntityUtil.getFirst(paList);
}

//copyIntroductionDateToReleaseDate
def copyIntroductionDateToReleaseDate(){
	result = ServiceUtil.returnSuccess();
	
	cond = EntityCondition.makeCondition("introductionDate",EntityOperator.NOT_EQUAL,null);
	productList = delegator.findList("Product", cond, null, null, null, false);
	if(productList){
		productList.each{product->
			product.set("releaseDate",product.getTimestamp("introductionDate"));
			//product.set("introductionDate",null); 原来的暂时不删除了
			product.store();
		}
	}

	return result;
}


def removeAllThruProductFeature(){
	result = ServiceUtil.returnSuccess();
	
	cond = EntityCondition.makeCondition("thruDate",EntityOperator.LESS_THAN,org.ofbiz.base.util.UtilDateTime.nowTimestamp());
	list = delegator.findList("ProductFeatureAppl", cond, null, null, null, false);
	if(list){
		list.each{pfl->
			//println("========pfl.productId" + pfl.productId + "==" + pfl.productFeatureId+"==="+pfl.thruDate);
			delegator.removeByAnd("ProductFeatureApplAttr",[productFeatureId:pfl.productFeatureId,productId:pfl.productId,fromDate:pfl.fromDate,attrName:'DESIGN_FEATURE']);
			delegator.removeByAnd("ProductFeatureApplAttr",[productFeatureId:pfl.productFeatureId,productId:pfl.productId,fromDate:pfl.fromDate,attrName:'COLOR_DESCRIPTION']);
			
			pfl.remove();
		}
	}

	return result;
}

def FindSystemNotice(){
	comList =  delegator.findByAnd("CommunicationEvent",['communicationEventTypeId':'SYS_NOTIC']);
	com = EntityUtil.getFirst(EntityUtil.filterByDate(comList,org.ofbiz.base.util.UtilDateTime.nowTimestamp(),"datetimeStarted","datetimeEnded",true));
	
	if(com && com.getString("note")){
		request.setAttribute("notices",com.getString("note"));
	}
}

def removeProductAssocThruDate(){
	result = ServiceUtil.returnSuccess();
	
	cond = EntityCondition.makeCondition("thruDate",EntityOperator.LESS_THAN,org.ofbiz.base.util.UtilDateTime.nowTimestamp());
	list = delegator.findList("ProductAssoc", cond, null, null, null, false);
	if(list){
		list.each{pa->
			pa.remove();
		}
	}

	return result;
}

def fixOrderInvoiceAndShipmentData(){
	result = ServiceUtil.returnSuccess();
	
	findIdResult = dispatcher.runSync("findAllOrderId",[userLogin:userLogin]);//获取所有问题的数据
	orderIdList = findIdResult.get("wrongOrderIdList");
	orderIdList.each{orderId->
		orderheader = delegator.findOne("OrderHeader",false,[orderId:orderId]);
		statusId = orderheader.getString("statusId");
		/*
			修复Payment数据 
			！！！所有的金额都以有效订单项合计为准（但是由于ofbiz会把多接收的部分增加到订单项中）
			1、删除原来 OrderPaymentPreference，取消 OrderPaymentPreference关联的Payment
			2、取消原来对应的Payment的Application
			3、创建一个新的 OrderPaymentPreference，并创建一个新的 Payment，关联一下
			4、主要一个问题，应该是先修复Payment的数据，还是先修复Invoice的数据
		 **/


		/* 
		对已经产生Invoice和shipment的数据
		1、完成的订单
			1.1 取消原来的Invoice，删除OrderItemBilling表对应的值
			1.2 通过 createInvoicesFromShipment 这个service，来创建新的invoice，并且生成paymentApplication
		2、批准的订单
			2.1 取消原来的Invoice，删除OrderItemBilling表对应的值
			2.2 如果货运状态是RECEIVED（由收货的BUG引起），就改成SHIPPED，再继续收货完成的时候，就会由ofbiz调整成RECEIVED
		*/
		orderItemBillingList = delegator.findByAnd("OrderItemBilling",[orderId:orderId]);
		if(orderItemBillingList){
			if("ORDER_COMPLETED"==statusId || "ORDER_APPROVED"==statusId){
				orderItemBillingList.each{oib->
					dispatcher.runSync("cancelInvoice",[userLogin:userLogin,invoiceId:oib.invoiceId]);
				}
				delegator.removeAll(orderItemBillingList);
			}
		}

		//如果已经开始收货了，找到这个订单的所有的shipment
		shipmentList = delegator.findByAnd("Shipment",[primaryOrderId:orderId]);
		shipmentIds=[:];
		//避免重复，把shipmentId作为key，放到Map中
		shipmentList.each{shipment->
			shipmentId = shipment.getString("shipmentId");
			shipmentIds.put(shipmentId,shipment);
		}

		shipmentIds.each{k,v->
			if("ORDER_COMPLETED"==statusId){
				//如果是已完成的，就重新创建一个发票（这一步会把有效的发票和有效的Payment关联，所以我认为应该先修复Payment数据）
				dispatcher.runSync("createInvoicesFromShipment",[shipmentId:k,userLogin:userLogin]);
			}else if("ORDER_APPROVED"==statusId){
				//如果是批准的订单，就检查他得shipment状态，如果是RECEIVED，就改成shiped
				if(v.getString("statusId")=="PURCH_SHIP_RECEIVED"){
					v.set("statusId","PURCH_SHIP_SHIPPED");
					v.store();
				}
			}
		}		
	}

	return result;
}

/*
找到所有161的产品，把introductionDate时间全部设置成2015-7-30号
*/
def fix161ProductIntorductionDate(){
	result = ServiceUtil.returnSuccess();
	now = org.ofbiz.base.util.UtilDateTime.toTimestamp("07/30/2015 00:00:00");
	productCategoryMember = delegator.findByAnd("ProductCategoryMember",[productCategoryId:"SEASON_161"]);
	if(productCategoryMember){
		storeList=[];
		productCategoryMember.each{pcm->
			product = pcm.getRelatedOne("Product");
			if(product){
				if(product.getString("productTypeId")=="FINISHED_GOOD"){
					product.set("introductionDate",now);
					storeList.add(product);
					//找到变型
					variantList = delegator.findByAnd("ProductAssoc",[productId:product.getString("productId"),"productAssocTypeId":"PRODUCT_VARIANT"]);
					variantList.each{variant->
						variantProduct = variant.getRelatedOne("AssocProduct");
						variantProduct.set("introductionDate",now);
						storeList.add(variantProduct);
					}
				}
			}
		}
		println("====now" + now);
		println("成功了："+delegator.storeAll(storeList));
	}

	return result;
}

/*
 修复所有161的Quote中unitPrice为0的问题数据
 */
 def fix161QuoteUnitPrice(){
	 result = ServiceUtil.returnSuccess();
	 quoteItems = delegator.findByAnd("QuoteItem", [quoteUnitPrice:BigDecimal.ZERO]);
	 quoteItems.each{ quoteItem->
		 Debug.logInfo("quoteId: " + quoteItem.quoteId + ", quoteItemSeqId: " + quoteItem.quoteItemSeqId, "ZuczugDataHelper.groovy");
		 quote = delegator.findByPrimaryKey("Quote", [quoteId:quoteItem.quoteId]);
		 calPriceResult = dispatcher.runSync("calculateProductPriceByProductId", [partyId:quote.partyId, productId:quoteItem.productId, userLogin:userLogin]);
		 price = calPriceResult.price;
		 dispatcher.runSync("autoUpdateQuotePrice", [quoteId:quoteItem.quoteId, quoteItemSeqId:quoteItem.quoteItemSeqId, manualQuoteUnitPrice:price, userLogin:userLogin]);
	 }
 
	 return result;
 }
 
 


def removeAllRealProductEngineerBOM(){
	result = ServiceUtil.returnSuccess();
	
	allRealProductList = delegator.findByAnd("Product",[productTypeId:"FINISHED_GOOD",isVirtual:"N"]);
	allRealProductList.each{pro->
		delegator.removeByAnd("ProductAssoc",[productId:pro.getString("productId"),productAssocTypeId:"ENGINEER_COMPONENT"]);
	}
	return result;
}

def replceSize4BomFromSize4(){
	result = ServiceUtil.returnSuccess();
	resultList = [];
	allData = delegator.findList("DesignProductVariantAndFabirc", null, null, null, null, false);
	if(allData){
		allData.each{bomInfo->
			//循环，如果不是4码，检查这个面料（假设叫A）是否是非虚拟非变型或者是虚拟，如果不是，就跳过，如果是，就找到这个设计商品对应的色号和4码
			//再循环4码的所有面料BOM，如果这个面料BOM是A的一个变型，说明版房从4拷贝到2的时候设计师还没有选择颜色，过了一段时间之后，设计师才去选择颜色
			if(bomInfo.getString("sizeId")!="4"){
				bomProduct = delegator.findOne("Product",false,[productId:bomInfo.getString("bomProductId")]);
				if(bomProduct.getString("isVirtual")=="Y" || (bomProduct.getString("isVirtual")=="N" && bomProduct.getString("isVariant")=="N")){
					size4VariantProductId = bomInfo.getString("virtualProductId")+"-"+bomInfo.getString("colorId")+"-4";
					println("==2码（" + bomInfo.getString("productId") + "）====对应的4码是" + size4VariantProductId);
					cond = EntityCondition.makeCondition("productId", EntityOperator.EQUALS, size4VariantProductId)
					size4BomList = delegator.findList("DesignProductVariantAndFabirc", cond, null, null, null, false);
					println("=====4码的面料BOM是" + size4BomList);
					size4BomList.each{size4Bom->
						//通过4码的BOM和2码的BOM查询关系，如果2码是虚拟，4码的那个是变型，则说明需要同步
						checkList = delegator.findByAnd("ProductAssoc",[productId:bomInfo.getString("bomProductId"),productIdTo:size4Bom.getString("bomProductId")]);
						if(checkList){
							oldBomList = delegator.findByAnd("ProductAssoc",[productId:bomInfo.getString("productId"),productIdTo:bomInfo.getString("bomProductId"),productAssocTypeId:"ENGINEER_COMPONENT"]);
							oldBom = EntityUtil.getFirst(EntityUtil.filterByDate(oldBomList,org.ofbiz.base.util.UtilDateTime.nowTimestamp(),"fromDate","thruDate",true));
							if(UtilValidate.isNotEmpty(oldBom) && oldBom!=null){
								println(oldBom);
								newSizeBom=oldBom.clone();
								newSizeBom.set("productIdTo",size4Bom.getString("bomProductId"));
								newSizeBom.create();
								oldBom.remove();
								resultList.add("商品" + bomInfo.getString("sizeId") + "码"+bomInfo.getString("productId")+"的BOM（" + bomInfo.getString("bomProductId") + "）是没有颜色的，对应的"+
									"4码(" + size4Bom.getString("productId") + ")的BOM（" + size4Bom.getString("bomProductId") + "）所以需要拷贝");
							}
						}
					}
				}
			}
		}
	}
	result.put("result",resultList);
	
	return result;
}

//把虚拟商品从波段分类中移除 by sven
def removeVariantProductFromWave(){
	result = ServiceUtil.returnSuccess();

	productList = delegator.findByAnd("ProductCategoryAndMember",[productCategoryTypeId:"WAVE"]);
	productList.each{pw->
		pro = delegator.findOne("Product",true,[productId:pw.getString("productId")]);
		if(pro && "Y".equals(pro.getString("isVariant"))){
			delegator.removeByAnd("ProductCategoryMember",[productCategoryId:pw.getString("productCategoryId"),productId:pw.getString("productId")]);
		}
	}	
	return result;
}

//rebuildZuczugClothingColorTable
def rebuildZuczugClothingColorTable(){
	result = ServiceUtil.returnSuccess();
	list = delegator.findByAnd("ProductFeatureGroupAndAppl",[productFeatureTypeId:"COLOR",productFeatureGroupId:"ZUCZUG_COLOR"]);
	list.each{item->
		feature = delegator.findOne("ProductFeature",false,[productFeatureId:item.getString("productFeatureId")]);
		feature.set("productFeatureCategoryId","CLOTHING_COLOR");
		feature.store();
	}
	return result;
}

def quickReceiveBOMWithSKU(){
	result = ServiceUtil.returnSuccess();
	bomlist = delegator.findByAnd("ProductAssoc",[productId:productId,productAssocTypeId:"MANUF_COMPONENT"])
	bomlist = EntityUtil.filterByDate(bomlist,org.ofbiz.base.util.UtilDateTime.nowTimestamp(),"fromDate","thruDate",true);
	bomlist.each{bom->
		bomId = bom.getString("productIdTo");
		dispatcher.runSync("receiveInventoryProduct",[userLogin:userLogin,facilityId:facilityId,ownerPartyId:"Company",inventoryItemTypeId:"NON_SERIAL_INV_ITEM",quantityRejected:0,productId:bomId,quantityAccepted:1000,itemDescription:"快速批量收货"]);
	}
	return result;
}


