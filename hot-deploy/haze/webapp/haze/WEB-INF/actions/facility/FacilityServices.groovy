import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*
import org.ofbiz.service.*;


def HazeIssuanceShipment(){
	result = ServiceUtil.returnSuccess();
	shipment = delegator.findOne("Shipment",false,[shipmentId:shipmentId]);
	shipment.set("statusId","SHIPMENT_SCHEDULED");
	shipment.store();

	facilityId = shipment.getString("originFacilityId");

	shipmentItems = delegator.findByAnd("ShipmentItem",[shipmentId:shipmentId]);
	for(k=0;k<shipmentItems.size();k++){
	//shipmentItems.each{item->
		item = shipmentItems.get(k);
		productId = item.getString("productId");
		totalQuantity = item.getBigDecimal("quantity");
		shipmentItemSeqId = item.getString("shipmentItemSeqId");
		needIssueQuantity = totalQuantity; //剩余需要issue

		//先确定是否数量足够

		//按照一定的格式获取库存明细
		inventoryItemList = getInventoryItem(facilityId,productId,null);
		//inventoryItemList.each{invItem->
		for(i=0;i<inventoryItemList.size();i++){
			invItem = inventoryItemList.get(i);
			inventoryItemQuantity = invItem.getBigDecimal("availableToPromiseTotal");
			inventoryItemId = invItem.getString("inventoryItemId");
			if(inventoryItemQuantity>=needIssueQuantity){
				issueQuantity = needIssueQuantity;
			}else{
				issueQuantity = inventoryItemQuantity;
			}

			result = dispatcher.runSync("issueInventoryItemPromiseToShipment",[shipmentId:shipmentId,shipmentItemSeqId:shipmentItemSeqId,quantity:issueQuantity,totalIssuedQty:totalQuantity,inventoryItemId:inventoryItemId,userLogin:userLogin]);
			if(ServiceUtil.isError(result)){
				return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
			}
			needIssueQuantity = needIssueQuantity - issueQuantity; //再次得到剩余需要issue的
			if(needIssueQuantity <= 0){//如果小于或等于0了，就break
				break;
			}
		}
		println("还需要。。。。" + needIssueQuantity);
		
		if(needIssueQuantity > 0){//如果循环完了，还有，说明库存不足，报错回滚
			return ServiceUtil.returnError("仓库"+ item.getString("productId") +"库存不足");
		}
	}

	return result;
}

def getInventoryItem(facilityId,productId,mode){
	exprs = UtilMisc.toList(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
	exprs.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
	if(!mode || mode=="DEF"){//默认排序，按照先进先出
		exprs.add(EntityCondition.makeCondition("availableToPromiseTotal", EntityOperator.GREATER_THAN, BigDecimal.ZERO));
		exprs.add(EntityCondition.makeCondition("datetimeReceived", EntityOperator.NOT_EQUAL, null));
		exprs.add(EntityCondition.makeCondition("inventoryItemTypeId", EntityOperator.EQUALS, "NON_SERIAL_INV_ITEM"));				
	}
	inventoryItemList = delegator.findList("InventoryItem", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, UtilMisc.toList("datetimeReceived ASC"), null, false);
	return inventoryItemList;
}