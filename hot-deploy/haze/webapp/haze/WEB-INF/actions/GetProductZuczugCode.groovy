
goodIdentification = delegator.findByPrimaryKey("GoodIdentification", [productId : productId, goodIdentificationTypeId : "ZUCZUG_CODE"]);
if (goodIdentification) {
	zuczugCode = goodIdentification.idValue;
	context.zuczugCode = zuczugCode;
}
