<script>
$(function(){
	$("#destinationFacilityId").on('input',function(){
		var facilityId = $(this).val();
		$.post("<@ofbizUrl>findShipAddressList</@ofbizUrl>",{facilityId:facilityId},function(result){
			if("addressList" in result){
				var list = result["addressList"];
				$("#destinationContactMechId").html("");
				var html="";
				$(list).each(function(k,v){
					html+="<option value='" + v.contactMechId + "'>" + v.address1 + "</option>";
				});
				//console.log(html);
				$("#destinationContactMechId").append(html);
			}else{
				alert("载入收货地址失败");
			}
		});
	});
});
</script>