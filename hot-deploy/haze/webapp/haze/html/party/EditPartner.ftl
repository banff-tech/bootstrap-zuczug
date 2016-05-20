
<form method="post" action="<#if party?has_content>updatePartner?partyId=${(parameters.partyId)!}<#else>createNewPartner</#if> " id="NewAccount" class="basic-form" onsubmit="javascript:submitFormDisableSubmits(this)" name="NewAccount">
  <input type="hidden" name="accountType" id="NewAccount_accountType">
  <input type="hidden" name="phoneTitle" id="NewAccount_phoneTitle">
  <input type="hidden" name="emailAddressTitle" id="NewAccount_emailAddressTitle">
  <table cellspacing="0" class="basic-table">
    <tbody>
      <tr>
        <td class="label">
            ${uiLabelMap.CompanyName}
        </td>
        <td><input type="text" name="groupName" value="${(partyGroup.groupName)!}" class="required"/></td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.FaRen}
        </td>
        <td>
        	<#if parameters.partyId?has_content>
	        	<#assign relationships = delegator.findByAnd("PartyRelationship",{"partyIdFrom":parameters.partyId,"roleTypeIdFrom":"ORGANIZATION_ROLE","roleTypeIdTo":"LEGAL_PERSON","partyRelationshipTypeId":"LEGAL_PERSON"})?if_exists>
        	</#if>
        	<@htmlTemplate.lookupField value="${(relationships[0].partyIdTo)!}" formName="NewAccount" name="corporatePartyId"  fieldFormName="LookupPartyName"/>
        	<input type="checkbox" value="Y" name="newCorporatePartyId" id="newfaren"/><label for="newfaren">添加的新的法人信息<label><span class="tooltip">如果是第一次添加法人信息，可以直接在输入框中输入姓名，如果不是第一次，请尝试搜索框</span>
        </td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.TheBusinesslicense}
        </td>
        <td>
        	<#assign businessLicense = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"BUSINESS_LICENSE")?if_exists>
        	<input type="text" name="businessLicense" value="${(businessLicense)!}" class=""/>
        </td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.ShuiWuDengJiHao}
        </td>
        <td>
        	<#assign taxNumber = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"TAX_NUM")?if_exists>
        	<input type="text" name="taxNumber" value="${(taxNumber)!}" class=""/>
        </td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.YiBanNaShuiRenZiGe}
        </td>
        <td>
        	<#assign taxpayer = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"TAXINFO")?if_exists>
        	<input type="text" name="taxpayer" value="${(taxpayer)!}" class=""/>
        </td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.ZhuCeZiJin}
        </td>
        <td>
        <#assign registeredCapital = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"REG_CAPITAL")?if_exists>
        <input type="text" name="registeredCapital" value="${(registeredCapital)!}" class=""/></td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.YingYeFanWei}
        </td>
        <td>
        	<#assign businessScope = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"BUSINESS_SCOPE")?if_exists>
        	<textarea rows="5" name="businessScope" cols="50">${(businessScope)!}</textarea>
        </td>
      </tr>
      <#if !(party?has_content)><#-- 只有新增的时候才会选择这个-->
      <tr>
        <td class="group-label">
            ${uiLabelMap.AccountingInvoiceRoles}
        </td>
        <td> </td>
      </tr>
      <tr>
      <td class="label">${uiLabelMap.Default}${uiLabelMap.AccountingInvoiceRoles}</td>
      <td>
        <select name="roleTypeId" class="">
        	<option value="">${uiLabelMap.PleaseSelectOne}</option>
        		<!-- assign des="RoleType.description."+ role.roleTypeId/ -->
        		<#-- option value="${(role.roleTypeId)!}">${(uiLabelMap[des])!}</option -->
        		<option value="CUSTOMER">${uiLabelMap['PartyRelationshipType.partyRelationshipName.CUSTOMER_REL']}</option>
        		<option value="SUPPLIER">${uiLabelMap['PageTitleListSupplierProduct']}</option>
        		<option value="PARTNER">${uiLabelMap['PartyRelationshipType.partyRelationshipName.PARTNERSHIP']}</option>
        		<option value="COMPETITOR">${uiLabelMap['RoleType.description.COMPETITOR']}</option>
        </select>
      </td>
    </tr>
  		<tr>
        <td class="group-label">
            ${uiLabelMap.ContactAddress}
        </td>
        <td> </td>
      </tr>
      <tr>
      <td class="label">${uiLabelMap.ContactPeople}</td>
      <td>
        <input type="text" size="50" maxlength="100" name="toName" class="" value="${(mechMap.postalAddress.toName)?default(request.getParameter('toName')?if_exists)}" />
      </td>
    </tr>
    <tr>   
      <td class="label">${uiLabelMap.CommonCountry}</td>
      
      <td>     
        <select name="countryGeoId" id="countryGeoId" class="">
			<option selected="selected" value="CHN">${uiLabelMap.China}</option>
          <#if countryGeoList?has_content>
          	<#list countryGeoList as geo>
          		<#assign getLabel = "Geo.geoName." + geo.geoId />
        		<option value="geo.geoId">${(uiLabelMap[getLabel])!}</option>
          	</#list>
          </#if>  
        </select>
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyState}</td>
      <td>
        <select name="stateProvinceGeoId" id="stateProvinceGeoId" class="">
        	<option>${uiLabelMap.PleaseSelectOne}</option>
        	<#if stateProvinceList?has_content>
        	<#list stateProvinceList as state>
	        	<option value="${(state.geoId)!}">${(state.geoName)!}</option>
        	</#list>
        	</#if>
        </select>
        <select name="cityGeoId" id="cityGeoId" class="">
        	<#if mechMap?has_content && (mechMap.postalAddress?exists) && (mechMap.postalAddress.cityGeoId?exists)>
        		<#assign cityGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.cityGeoId})?if_exists/>
        		<option value="${(cityGeo.geoId)!}">${(cityGeo.geoName)!}</option>
        	</#if>
        </select>
        <select name="countyGeoId" id="countyGeoId" class="">
        	<#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.countyGeoId?exists)>
        		<#assign countyGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.countyGeoId})?if_exists/>
        		<option value="${(countyGeo.geoId)!}">${(countyGeo.geoName)!}</option>
        	</#if>
        </select>
        <input type="hidden" name="city" value="_"/>
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.DetailedAddress}</td>
      <td>
        <input type="text" size="60" maxlength="255" name="address1" class="" value="${(mechMap.postalAddress.address1)?default(request.getParameter('address1')?if_exists)}" />
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyZipCode}</td>
      <td>
        <input type="text" size="30" maxlength="60" name="postalCode" class="" value="${(mechMap.postalAddress.postalCode)?default(request.getParameter('postalCode')?if_exists)}" />
      </td>
    </tr>
      <tr>
        <td class="group-label">
            ${uiLabelMap.ContactPhone}
        </td>
        <td>
          &nbsp;
        </td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap.PartyPhoneNumber}
        </td>
        <td>
          <input type="text" name="countryCode" size="4" maxlength="10" id="NewAccount_countryCode"> - <input type="text" name="areaCode" size="4" maxlength="10" id="NewAccount_areaCode"> - <input type="text" name="contactNumber" size="15" maxlength="15" id="NewAccount_contactNumber"> - <input type="text" name="extension" size="6" maxlength="10" id="NewAccount_extension">
        </td>
      </tr>
      <tr>
        <td class="label">
        	&nbsp;
        </td>
        <td>
          ${uiLabelMap.PhoneNumberRemark}
        </td>
      </tr>
      <tr>
        <td class="group-label">
            ${uiLabelMap['CommunicationEventType.description.EMAIL_COMMUNICATION']}
        </td>
        <td>
          &nbsp;
        </td>
      </tr>
      <tr>
        <td class="label">
            ${uiLabelMap['CommunicationEventType.description.EMAIL_COMMUNICATION']}
        </td>
        <td>
          <input type="text" name="emailAddress" size="50" maxlength="60" id="NewAccount_emailAddress">
        </td>
      </tr>
      </#if>
      <tr>
        <td class="label">
          &nbsp;
        </td>
        <td colspan="4">
          <input type="submit" class="smallSubmit" name="submitButton" value="${uiLabelMap.CommonSave}">
        </td>
      </tr>
    </tbody>
  </table>
</form>


<script>
	$(function(){
		$("#countryGeoId").change(function(){
			findNextGeoByParentId($("#countryGeoId").val(),'REGIONS');
		});
		$("#stateProvinceGeoId").change(function(){
			findNextGeoByParentId($("#stateProvinceGeoId").val(),'PROVINCE_CITY');
		});
		$("#cityGeoId").change(function(){
			findNextGeoByParentId($("#cityGeoId").val(),'CITY_COUNTY');
		});
		
		jQuery("#NewAccount").validate({
		     submitHandler:
		         function(form) {
		         form.submit();
		     }
		});
	});

	function findNextGeoByParentId(geoIdFrom,geoAssocTypeId){
		$.post("<@ofbizUrl>getNextAssocGeoList</@ofbizUrl>",{"geoIdFrom":geoIdFrom,"geoAssocTypeId":geoAssocTypeId},function(result){
			if("list" in result){
				var list = result['list'];
				if(geoAssocTypeId=="REGIONS"){
					processProvince(list,"stateProvinceGeoId");
				}else if(geoAssocTypeId=="PROVINCE_CITY"){
					processProvince(list,"cityGeoId");
				}else if(geoAssocTypeId=="CITY_COUNTY"){
					processProvince(list,"countyGeoId");
				}
			}
		});
	}
	
	function processProvince(data,domId){
		$("#" + domId).html("<option>-请选择-</option>");
		$(data).each(function(k,v){
			$("<option value='" + v['geoId'] + "'>" + v['geoName'] + "</option>").appendTo($("#" + domId));
		});
		
	}
</script>
