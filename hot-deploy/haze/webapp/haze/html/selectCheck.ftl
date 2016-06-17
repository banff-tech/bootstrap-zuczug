<script>
	function submitCheckForm(formId,action,target){
		if(target==null){
			target="_blank";
		}		
		var ids = getFormCheckBox(formId);
		if(ids){
			$("<form action='<@ofbizUrl>"+action+"</@ofbizUrl>' method='post' target='"+target+"'><input type='hidden' name='shipmentIdList' value='" +ids+ "'/></from>").appendTo($("body")).submit().remove();
		}
	}

	function getFormCheckBox(formId){
		var ids="";
		$("#"+formId).find("input:checked").each(function () {
			if($(this).attr("name")!="selectAll" && $(this).val().length>0){
				if(ids==""){
					ids=$(this).val();
				}else{
					ids+=","+$(this).val();
				}
			}
        });
        if(ids==""){
        	alert("您还没有选择");
        	return false;
        }	
        return ids;
	}
</script>