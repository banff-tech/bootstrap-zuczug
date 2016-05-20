<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#if !mechMap.facilityContactMech?exists && mechMap.contactMech?exists>
  <p><h3>${uiLabelMap.PartyContactInfoNotBelongToYou}.</h3></p>
  &nbsp;<a href="<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonGoBack}</a>
<#else>
  <#if !mechMap.contactMech?exists>
    <#-- When creating a new contact mech, first select the type, then actually create -->
    <#if !preContactMechTypeId?has_content>
    <h1>${title}</h1>
    <div class="button-bar">
      <a href='<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>
    </div>
    <form method="post" action='<@ofbizUrl>facility.EditContactMech</@ofbizUrl>' name="createcontactmechform">
      <input type='hidden' name='facilityId' value='${facilityId}' />
      <input type='hidden' name='DONE_PAGE' value='${donePage?if_exists}' />
      <table width="50%" class="basic-table" cellspacing="0">
        <tr>
          <td class="label">${uiLabelMap.PartySelectContactType}</td>
          <td>
            <select name="preContactMechTypeId" >
              <option value="POSTAL_ADDRESS">${uiLabelMap['ContactAddress']}</option>
              <option value="TELECOM_NUMBER">${uiLabelMap['ContactMechType.description.TELECOM_NUMBER']}</option>
              <option value="EMAIL_ADDRESS">${uiLabelMap['ContactMechType.description.EMAIL_ADDRESS']}</option>
              <option value="WEB_ADDRESS">${uiLabelMap['ContactMechType.description.WEB_ADDRESS']}</option>
            </select>&nbsp;<a href="javascript:document.createcontactmechform.submit()" class="buttontext">${uiLabelMap.CommonCreate}</a>
          </td>
        </tr>
      </table>
    </form>
    </#if>
  </#if>

  <#if mechMap.contactMechTypeId?has_content>
    <#if !mechMap.contactMech?has_content>
      <h1>${title}</h1>
      <div class="button-bar">
        <a href='<@ofbizUrl>${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>
      </div>
      <#if contactMechPurposeType?exists>
        <div><span class="label">(${uiLabelMap.PartyMsgContactHavePurpose}</span>"${contactMechPurposeType.get("description",locale)?if_exists}")</div>
      </#if>
      <table width="90%" class="basic-table" cellspacing="0">
        <form method="post" action='<@ofbizUrl>${"facility."+mechMap.requestName}</@ofbizUrl>' name="editcontactmechform" id="editcontactmechform">
        <input type='hidden' name='DONE_PAGE' value='${donePage}' />
        <input type='hidden' name='contactMechTypeId' value='${mechMap.contactMechTypeId}' />
        <input type='hidden' name='facilityId' value='${facilityId}' />
        <#if preContactMechTypeId?exists><input type='hidden' name='preContactMechTypeId' value='${preContactMechTypeId}' /></#if>
        <#if contactMechPurposeTypeId?exists><input type='hidden' name='contactMechPurposeTypeId' value='${contactMechPurposeTypeId?if_exists}' /></#if>

        <#if paymentMethodId?exists><input type='hidden' name='paymentMethodId' value='${paymentMethodId}' /></#if>

        <tr>
          <td class="label">${uiLabelMap.PartyContactPurposes}</td>
          <td>
            <select name='contactMechPurposeTypeId' class="required">
              <option></option>
              <#list mechMap.purposeTypes as contactMechPurposeType>
                <option value='${contactMechPurposeType.contactMechPurposeTypeId}'>${contactMechPurposeType.get("description",locale)}</option>
               </#list>
            </select>
          *</td>
        </tr>
    <#else>
      <h1>${title}</h1>
      <div class="button-bar">
        <a href='<@ofbizUrl>${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>
        <a href="<@ofbizUrl>facility.EditContactMech?facilityId=${facilityId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductNewContactMech}</a>
      </div>
      <table class="basic-table" cellspacing="0">
        <#if mechMap.purposeTypes?has_content>
        <tr>
          <td valign="top" class="label">${uiLabelMap.PartyContactPurposes}</td>
          <td>
            <table class="basic-table" cellspacing="0">
            <#if mechMap.facilityContactMechPurposes?has_content>
              <#assign alt_row = false>
              <#list mechMap.facilityContactMechPurposes as facilityContactMechPurpose>
                <#assign contactMechPurposeType = facilityContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                  <td>
                      <#if contactMechPurposeType?has_content>
                        <b>${contactMechPurposeType.get("description",locale)}</b>
                      <#else>
                        <b>${uiLabelMap.PartyMechPurposeTypeNotFound}: "${facilityContactMechPurpose.contactMechPurposeTypeId}"</b>
                      </#if>
                      (${uiLabelMap.CommonSince}: ${facilityContactMechPurpose.fromDate})
                      <#if facilityContactMechPurpose.thruDate?has_content>(${uiLabelMap.CommonExpires}: ${facilityContactMechPurpose.thruDate.toString()}</#if>
                      <a href="javascript:document.getElementById('deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}').submit();" class="buttontext">${uiLabelMap.CommonDelete}</a>
                  </td>
                </tr>
                <#-- toggle the row color -->
                <#assign alt_row = !alt_row>
                <form id="deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}" method="post" action="<@ofbizUrl>facility.deleteFacilityContactMechPurpose</@ofbizUrl>">
                  <input type="hidden" name="facilityId" value="${facilityId?if_exists}" />
                  <input type="hidden" name="contactMechId" value="${contactMechId?if_exists}" />
                  <input type="hidden" name="contactMechPurposeTypeId" value="${(facilityContactMechPurpose.contactMechPurposeTypeId)?if_exists}" />
                  <input type="hidden" name="fromDate" value="${(facilityContactMechPurpose.fromDate)?if_exists}" />
                  <input type="hidden" name="DONE_PAGE" value="${donePage?if_exists}" />
                  <input type="hidden" name="useValues" value="true" />
                </form>
              </#list>
              </#if>
              <tr>
                <td>
                  <form method="post" action='<@ofbizUrl>facility.createFacilityContactMechPurpose?DONE_PAGE=${donePage}&amp;useValues=true</@ofbizUrl>' name='newpurposeform'>
                  <input type="hidden" name='facilityId' value='${facilityId}' />
                  <input type="hidden" name='contactMechId' value='${contactMechId?if_exists}' />
                    <select name='contactMechPurposeTypeId'>
                      <option></option>
                      <#list mechMap.purposeTypes as contactMechPurposeType>
                        <option value='${contactMechPurposeType.contactMechPurposeTypeId}'>${contactMechPurposeType.get("description",locale)}</option>
                      </#list>
                    </select>
                    &nbsp;<a href='javascript:document.newpurposeform.submit()' class='buttontext'>${uiLabelMap.PartyAddPurpose}</a>
                  </form>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        </#if>
        <form method="post" action='<@ofbizUrl>${"facility."+mechMap.requestName}</@ofbizUrl>' name="editcontactmechform" id="editcontactmechform">
        <input type="hidden" name="contactMechId" value='${contactMechId}' />
        <input type="hidden" name="contactMechTypeId" value='${mechMap.contactMechTypeId}' />
        <input type="hidden" name='facilityId' value='${facilityId}' />
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
      <td class="label">${uiLabelMap.DetailedAddress}</td>
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
        &nbsp;分机&nbsp;<input type="text" size="6" maxlength="10" name="extension" value="${(mechMap.facilityContactMech.extension)?default(request.getParameter('extension')?if_exists)}" />
      </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>${uiLabelMap.PhoneNumberRemark}</td>
    </tr>
  <#elseif "EMAIL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${uiLabelMap.PartyEmailAddress}</td>
      <td>
          <input type="text" class="required" size="60" maxlength="255" name="emailAddress" value="${(mechMap.contactMech.infoString)?default(request.getParameter('emailAddress')?if_exists)}" />
      *</td>
    </tr>
  <#else>
    <tr>
      <td class="label">${mechMap.contactMechType.get("description",locale)}</td>
      <td>
          <input type="text" class="required" size="60" maxlength="255" name="infoString" value="${(mechMap.contactMech.infoString)?if_exists}" />
      *</td>
    </tr>
  </#if>
    <tr>
      <td>&nbsp;</td>
      <td>
        <a href="javascript:document.editcontactmechform.submit()" class="buttontext">${uiLabelMap.CommonSave}</a>
      </td>
    </tr>
  </form>
  </table>
  </#if>
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
