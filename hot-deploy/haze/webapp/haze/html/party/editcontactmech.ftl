
<#if !mechMap.contactMech?exists>
  <#-- When creating a new contact mech, first select the type, then actually create -->
  <#if !preContactMechTypeId?has_content>
    <h1>${uiLabelMap.PartyCreateNewContact}</h1>
    <form method="post" action="<@ofbizUrl>editcontactmech</@ofbizUrl>" name="createcontactmechform">
      <input type="hidden" name="partyId" value="${partyId}" />
      <table class="basic-table" cellspacing="0">
        <tr>
          <td class="label">${uiLabelMap.PartySelectContactType}</td>
          <td>
            <select name="preContactMechTypeId">
              <#-- list mechMap.contactMechTypes as contactMechType>
                <option value="${contactMechType.contactMechTypeId}">${contactMechType.get("description",locale)}</option>
              </#list -->
              <option value="POSTAL_ADDRESS">${uiLabelMap['ContactAddress']}</option>
              <option value="TELECOM_NUMBER">${uiLabelMap['ContactMechType.description.TELECOM_NUMBER']}</option>
              <option value="EMAIL_ADDRESS">${uiLabelMap['ContactMechType.description.EMAIL_ADDRESS']}</option>
              <option value="WEB_ADDRESS">${uiLabelMap['ContactMechType.description.WEB_ADDRESS']}</option>
            </select>
            <a href="javascript:document.createcontactmechform.submit()" class="smallSubmit">${uiLabelMap.CommonCreate}</a>
          </td>
        </tr>
      </table>
    </form>
    </#if>
</#if>
<#if mechMap.contactMechTypeId?has_content>
  <#if !mechMap.contactMech?has_content>
    <h1>${uiLabelMap.PartyCreateNewContact}</h1>
    <div id="mech-purpose-types">
    <#if contactMechPurposeType?exists>
      <p>(${uiLabelMap.PartyMsgContactHavePurpose} <b>"${contactMechPurposeType.get("description",locale)?if_exists}"</b>)</p>
    </#if>
      <form method="post" action="<@ofbizUrl>${mechMap.requestName}</@ofbizUrl>" name="editcontactmechform" id="editcontactmechform">
        <input type="hidden" name="DONE_PAGE" value="${donePage}" />
        <input type="hidden" name="contactMechTypeId" value="${mechMap.contactMechTypeId}" />
        <input type="hidden" name="partyId" value="${partyId}" />
        <#if cmNewPurposeTypeId?has_content><input type="hidden" name="contactMechPurposeTypeId" value="${cmNewPurposeTypeId}" /></#if>
        <#if preContactMechTypeId?exists><input type="hidden" name="preContactMechTypeId" value="${preContactMechTypeId}" /></#if>
        <#if contactMechPurposeTypeId?exists><input type="hidden" name="contactMechPurposeTypeId" value="${contactMechPurposeTypeId?if_exists}" /></#if>
        <#if paymentMethodId?has_content><input type='hidden' name='paymentMethodId' value='${paymentMethodId}' /></#if>
        <table class="basic-table" cellspacing="0">
  <#else>  
    <h1>${uiLabelMap.PartyEditContactInformation}</h1>
    <div id="mech-purpose-types">
      <table class="basic-table" cellspacing="0">
      <#if mechMap.purposeTypes?has_content>
        <tr>
          <td class="label">${uiLabelMap.PartyContactPurposes}</td>
          <td>
            <table class="basic-table" cellspacing="0">
              <#if mechMap.partyContactMechPurposes?has_content>
                <#list mechMap.partyContactMechPurposes as partyContactMechPurpose>
                  <#assign contactMechPurposeType = partyContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                  <tr>
                    <td>
                      <#if contactMechPurposeType?has_content>
                        ${contactMechPurposeType.get("description",locale)}
                      <#else>
                        ${uiLabelMap.PartyPurposeTypeNotFound}: "${partyContactMechPurpose.contactMechPurposeTypeId}"
                      </#if>
                      (${uiLabelMap.CommonSince}:${partyContactMechPurpose.fromDate.toString()})
                      <#if partyContactMechPurpose.thruDate?has_content>(${uiLabelMap.CommonExpire}: ${partyContactMechPurpose.thruDate.toString()}</#if>
                    </td>
                    <td class="button-col">
                      <form name="deletePartyContactMechPurpose_${partyContactMechPurpose.contactMechPurposeTypeId}" method="post" action="<@ofbizUrl>deletePartyContactMechPurpose</@ofbizUrl>" >
                         <input type="hidden" name="partyId" value="${partyId}" />
                         <input type="hidden" name="contactMechId" value="${contactMechId}" />
                         <input type="hidden" name="contactMechPurposeTypeId" value="${partyContactMechPurpose.contactMechPurposeTypeId}" />
                         <input type="hidden" name="fromDate" value="${partyContactMechPurpose.fromDate.toString()}" />
                         <input type="hidden" name="DONE_PAGE" value="${donePage?replace("=","%3d")}" />
                         <input type="hidden" name="useValues" value="true" />
                         <a href="javascript:document.deletePartyContactMechPurpose_${partyContactMechPurpose.contactMechPurposeTypeId}.submit()" class="buttontext">${uiLabelMap.CommonDelete}</a> 
                       </form>
                    </td>
                  </tr>
                </#list>
              </#if>
              <tr>
                  <td class="button-col">
                  	<form method="post" action="<@ofbizUrl>createPartyContactMechPurpose</@ofbizUrl>" name="newpurposeform">
                  	<input type="hidden" name="partyId" value="${partyId}" />
                  	<input type="hidden" name="DONE_PAGE" value="${donePage}" />
                  	<input type="hidden" name="useValues" value="true" />
                  	<input type="hidden" name="contactMechId" value="${contactMechId?if_exists}" />
                    <select name="contactMechPurposeTypeId">
                      <option></option>
                      <#list mechMap.purposeTypes as contactMechPurposeType>
                        <option value="${contactMechPurposeType.contactMechPurposeTypeId}">${contactMechPurposeType.get("description",locale)}</option>
                      </#list>
                    </select>
                    </form>
                  </td>
                
                <td><a href="javascript:document.newpurposeform.submit()" class="smallSubmit">${uiLabelMap.PartyAddPurpose}</a></td>
              </tr>
            </table>
          </tr>
      </#if>
      </table>
      <hr/>
      <form method="post" action="<@ofbizUrl>${mechMap.requestName}</@ofbizUrl>" name="editcontactmechform" id="editcontactmechform">
        <input type="hidden" name="contactMechId" value="${contactMechId}" />
        <input type="hidden" name="contactMechTypeId" value="${mechMap.contactMechTypeId}" />
        <input type="hidden" name="partyId" value="${partyId}" />
        <input type="hidden" name="DONE_PAGE" value="${donePage?if_exists}" />
        <table class="basic-table" cellspacing="0">
  </#if>
  <#if "POSTAL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${uiLabelMap.ContactPeople}</td>
      <td>
        <input type="text" size="50" maxlength="100" name="toName" class="required" value="${(mechMap.postalAddress.toName)?default(request.getParameter('toName')?if_exists)}" />
      </td>
    </tr>
    <tr>   
      <td class="label">${uiLabelMap.CommonCountry}</td>
      
      <td>     
        <select name="countryGeoId" id="countryGeoId" class="required">
          <#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.countryGeoId?exists)>
            <#assign defaultCountryGeoId = mechMap.postalAddress.countryGeoId>
          <#else>
           <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
          </#if>
			<option selected="selected" value="${defaultCountryGeoId}">
            <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
            ${countryGeo.get("geoName",locale)}
          </option>
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
        <select name="stateProvinceGeoId" id="stateProvinceGeoId" class="required">
        	<#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.stateProvinceGeoId?exists)>
        		<#assign stateProvinceGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.stateProvinceGeoId})?if_exists/>
        		<option value="${(stateProvinceGeo.geoId)!}">${(stateProvinceGeo.geoName)!}</option>
        	<#else>
        		<option>${uiLabelMap.PleaseSelectOne}</option>
        	</#if>
        	<#if stateProvinceList?has_content>
        	<#list stateProvinceList as state>
	        	<option value="${(state.geoId)!}">${(state.geoName)!}</option>
        	</#list>
        	</#if>
        </select>
        <select name="cityGeoId" id="cityGeoId" class="required">
        	<#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.cityGeoId?exists)>
        		<#assign cityGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.cityGeoId})?if_exists/>
        		<option value="${(cityGeo.geoId)!}">${(cityGeo.geoName)!}</option>
        	</#if>
        </select>
        <select name="countyGeoId" id="countyGeoId" class="required">
        	<#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.countyGeoId?exists)>
        		<#assign countyGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.countyGeoId})?if_exists/>
        		<option value="${(countyGeo.geoId)!}">${(countyGeo.geoName)!}</option>
        	</#if>
        </select>
        <input type="hidden" name="city" value="_"/>
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.ContactAddress}</td>
      <td>
        <input type="text" size="60" maxlength="255" name="address1" class="required" value="${(mechMap.postalAddress.address1)?default(request.getParameter('address1')?if_exists)}" />
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyZipCode}</td>
      <td>
        <input type="text" size="30" maxlength="60" name="postalCode" class="required" value="${(mechMap.postalAddress.postalCode)?default(request.getParameter('postalCode')?if_exists)}" />
      </td>
    </tr>
  <#elseif "TELECOM_NUMBER" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${uiLabelMap.PartyPhoneNumber}</td>
      <td>
        <input type="text" size="4" maxlength="10" name="countryCode" value="${(mechMap.telecomNumber.countryCode)?default(request.getParameter('countryCode')?if_exists)}" />
        -&nbsp;<input type="text" size="4" maxlength="10" name="areaCode" value="${(mechMap.telecomNumber.areaCode)?default(request.getParameter('areaCode')?if_exists)}" />
        -&nbsp;<input type="text" size="15" maxlength="15" name="contactNumber" value="${(mechMap.telecomNumber.contactNumber)?default(request.getParameter('contactNumber')?if_exists)}" />
        &nbsp;${uiLabelMap.PartyContactExt}&nbsp;<input type="text" size="6" maxlength="10" name="extension" value="${(mechMap.partyContactMech.extension)?default(request.getParameter('extension')?if_exists)}" />
      </td>
    </tr>
    <tr>
      <td class="label"></td>
      <td>${uiLabelMap.PhoneNumberRemark}</td>
    </tr>
  <#elseif "EMAIL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${mechMap.contactMechType.get("description",locale)}</td>
      <td>
        <input type="text" size="60" maxlength="255" name="emailAddress" value="${(mechMap.contactMech.infoString)?default(request.getParameter('emailAddress')?if_exists)}" />
      </td>
    </tr>
  <#else>
    <tr>
      <td class="label">${mechMap.contactMechType.get("description",locale)}</td>
      <td>
        <input type="text" size="60" maxlength="255" name="infoString" value="${(mechMap.contactMech.infoString)?if_exists}" />
      </td>
    </tr>
  </#if>
  </table>
  <div class="button-bar">
    <a href="<@ofbizUrl>EditPartnerContactMech?partyId=${(parameters.partyId)!}</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonGoBack}</a>
	<input type="submit" value="${uiLabelMap.CommonSave}"/>
  </div>
  </form>
  </div>
<#else>
  <a href="<@ofbizUrl>backHome</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonGoBack}</a>
</#if>

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
		
		jQuery("#editcontactmechform").validate({
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
