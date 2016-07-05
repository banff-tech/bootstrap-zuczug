
<#if !mechMap.contactMech?exists>
  <#-- When creating a new contact mech, first select the type, then actually create -->
  <#if !preContactMechTypeId?has_content>
  <h1>${uiLabelMap.PartyCreateNewContact}</h1>
  <form method="post" action='<@ofbizUrl>editcontactmech</@ofbizUrl>' name="createcontactmechform" class="form-horizontal">
      <input type="hidden" name="partyId" value="${partyId}" />
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.PartySelectContactType}</label>
          <div class="col-sm-4">
              <div style="float: left;">
                  <select name="preContactMechTypeId"  class=" form-control">
                      <option value="POSTAL_ADDRESS">${uiLabelMap['ContactAddress']}</option>
                      <option value="TELECOM_NUMBER">${uiLabelMap['ContactMechType.description.TELECOM_NUMBER']}</option>
                      <option value="EMAIL_ADDRESS">${uiLabelMap['ContactMechType.description.EMAIL_ADDRESS']}</option>
                      <option value="WEB_ADDRESS">${uiLabelMap['ContactMechType.description.WEB_ADDRESS']}</option>
                  </select>
              </div>
              <div style="float: left;    margin-left: 5px;">
                  <a href="javascript:document.createcontactmechform.submit()" class="btn btn-labeled btn-default"> <span class="btn-label"><i class="fa fa-clipboard"></i></span>${uiLabelMap.CommonCreate}</a>
              </div>
          </div>
      </div>
  </form>
    </#if>
</#if>
<#if mechMap.contactMechTypeId?has_content>
  <#if !mechMap.contactMech?has_content>
    <h1>${uiLabelMap.PartyCreateNewContact}</h1>
    <div id="mech-purpose-types">
    <#if contactMechPurposeType?exists>
        <div><span class="label">(${uiLabelMap.PartyMsgContactHavePurpose}</span>"${contactMechPurposeType.get("description",locale)?if_exists}")</div>
    </#if>
      <form method="post" action="<@ofbizUrl>${mechMap.requestName}</@ofbizUrl>" class="form-horizontal" name="editcontactmechform" id="editcontactmechform">
        <input type="hidden" name="DONE_PAGE" value="${donePage}" />
        <input type="hidden" name="contactMechTypeId" value="${mechMap.contactMechTypeId}" />
        <input type="hidden" name="partyId" value="${partyId}" />
        <#if cmNewPurposeTypeId?has_content><input type="hidden" name="contactMechPurposeTypeId" value="${cmNewPurposeTypeId}" /></#if>
        <#if preContactMechTypeId?exists><input type="hidden" name="preContactMechTypeId" value="${preContactMechTypeId}" /></#if>
        <#if contactMechPurposeTypeId?exists><input type="hidden" name="contactMechPurposeTypeId" value="${contactMechPurposeTypeId?if_exists}" /></#if>
        <#if paymentMethodId?has_content><input type='hidden' name='paymentMethodId' value='${paymentMethodId}' /></#if>
  <#else>  
    <h1>${uiLabelMap.PartyEditContactInformation}</h1>
    <div id="mech-purpose-types">
      <#if mechMap.purposeTypes?has_content>
          <div class="form-horizontal">
              <div class="form-group" style="margin-top: 30px;">
                  <label class="col-md-2 control-label" id="">${uiLabelMap.PartyContactPurposes}</label>
                  <div class="col-sm-4" style="">
                    <#if mechMap.facilityContactMechPurposes?has_content>
                      <#assign alt_row = false>
                      <#list mechMap.facilityContactMechPurposes as facilityContactMechPurpose>
                        <#assign contactMechPurposeType = facilityContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                        <#if contactMechPurposeType?has_content>
                        ${contactMechPurposeType.get("description",locale)}
                        <#else>
                        ${uiLabelMap.PartyMechPurposeTypeNotFound}: "${facilityContactMechPurpose.contactMechPurposeTypeId}"
                        </#if>
                          (${uiLabelMap.CommonSince}: ${facilityContactMechPurpose.fromDate})
                        <#if facilityContactMechPurpose.thruDate?has_content>(${uiLabelMap.CommonExpires}: ${facilityContactMechPurpose.thruDate.toString()}</#if>
                          <!--<a href="javascript:document.getElementById('deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}').submit();" class="buttontext">${uiLabelMap.CommonDelete}</a>-->
                          <a href="javascript:document.getElementById('deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}').submit();" class="btn btn-labeled btn-danger"> <span class="btn-label"><i class="glyphicon glyphicon-remove"></i></span>${uiLabelMap.CommonDelete} </a>
                        <#assign alt_row = !alt_row>
                          <form id="deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}" method="post" action="<@ofbizUrl>deletePartyContactMechPurpose</@ofbizUrl>">
                              <input type="hidden" name="partyId" value="${partyId}" />
                              <input type="hidden" name="contactMechId" value="${contactMechId}" />
                              <input type="hidden" name="contactMechPurposeTypeId" value="${partyContactMechPurpose.contactMechPurposeTypeId}" />
                              <input type="hidden" name="fromDate" value="${partyContactMechPurpose.fromDate.toString()}" />
                              <input type="hidden" name="DONE_PAGE" value="${donePage?replace("=","%3d")}" />
                              <input type="hidden" name="useValues" value="true" />
                          </form>
                          <hr style="margin-top:10px;margin-bottom: 10px">
                          <form method="post" action='<@ofbizUrl>facility.createFacilityContactMechPurpose?DONE_PAGE=${donePage}&amp;useValues=true</@ofbizUrl>' name='newpurposeform'>
                              <input type="hidden" name='facilityId' value='${facilityId}' />
                              <input type="hidden" name='contactMechId' value='${contactMechId?if_exists}' />
                              <div class="input-group">
                                  <div style="float: left;">
                                      <select name='contactMechPurposeTypeId' class=" form-control">
                                          <option></option>
                                        <#list mechMap.purposeTypes as contactMechPurposeType>
                                            <option value='${contactMechPurposeType.contactMechPurposeTypeId}'>${contactMechPurposeType.get("description",locale)}</option>
                                        </#list>
                                      </select>
                                  </div>
                                  <div style="float: left;    margin-left: 5px;">
                                      <a href="javascript:document.newpurposeform.submit()" class="btn btn-labeled btn-default"> <span class="btn-label"><i class="fa fa-clipboard"></i></span>${uiLabelMap.PartyAddPurpose}</a>
                                  </div>
                              </div>
                          </form>
                      </#list>
                    </#if>
                  </div>
              </div>
          </div>
      </#if>
      <hr/>
      <form method="post" action="<@ofbizUrl>${mechMap.requestName}</@ofbizUrl>" name="editcontactmechform" class="form-horizontal" id="editcontactmechform">
        <input type="hidden" name="contactMechId" value="${contactMechId}" />
        <input type="hidden" name="contactMechTypeId" value="${mechMap.contactMechTypeId}" />
        <input type="hidden" name="partyId" value="${partyId}" />
        <input type="hidden" name="DONE_PAGE" value="${donePage?if_exists}" />
        <table class="basic-table" cellspacing="0">
  </#if>
  <#if "POSTAL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.ContactPeople}</label>
          <div class="col-sm-4">
              <input type="text" size="50" maxlength="100" name="toName" class="form-control" required="true" value="${(mechMap.postalAddress.toName)?default(request.getParameter('toName')?if_exists)}" />
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.CommonCountry}</label>
          <div class="col-sm-4">
              <select name="countryGeoId" id="countryGeoId" class="form-control" required="true" >
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
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.PartyState}</label>
          <div class="col-sm-4">
              <div class="">
                  <section class="col col-4" style="float: left;width: 33.33%;">
                      <label class="select" style="width: 100%;">
                          <select name="stateProvinceGeoId" id="stateProvinceGeoId" class="form-control valid" required = "true">
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
                      </label>
                  </section>
                  <section class="col col-4" style="float: left;width: 33.33%;">
                      <label class="select" style="width: 100%;">
                          <select name="cityGeoId" id="cityGeoId" class="form-control valid" required>
                            <#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.cityGeoId?exists)>
                              <#assign cityGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.cityGeoId})?if_exists/>
                                <option value="${(cityGeo.geoId)!}">${(cityGeo.geoName)!}</option>
                            </#if>
                          </select>
                      </label>
                  </section>
                  <section class="col col-4" style="float: left;width: 33.33%;">
                      <label class="select" style="width: 100%;">
                          <select name="countyGeoId" id="countyGeoId" class="form-control valid" required>
                            <#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.countyGeoId?exists)>
                              <#assign countyGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.countyGeoId})?if_exists/>
                                <option value="${(countyGeo.geoId)!}">${(countyGeo.geoName)!}</option>
                            </#if>
                          </select>
                      </label>
                  </section>
                  <input type="hidden" name="city" value="_"/>
              </div>
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.DetailedAddress}</label>
          <div class="col-sm-4">
              <input type="text" size="60" maxlength="255" name="address1" class="form-control" required value="${(mechMap.postalAddress.address1)?default(request.getParameter('address1')?if_exists)}" />
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.PartyZipCode}</label>
          <div class="col-sm-4">
              <input type="text" size="30" maxlength="60" name="postalCode" class="form-control" required value="${(mechMap.postalAddress.postalCode)?default(request.getParameter('postalCode')?if_exists)}" />
          </div>
      </div>

  <#elseif "TELECOM_NUMBER" = mechMap.contactMechTypeId?if_exists>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.PartyPhoneNumber}</label>
          <div class="col-sm-4">
              <div class="row" style="padding-left: 13px;">
                  <section class="col col-2" style="float: left;width: 15%">
                      <label class="input">
                          <input type="text" name="countryCode" class="form-control" value="${(mechMap.telecomNumber.countryCode)?default(request.getParameter('countryCode')?if_exists)}" />
                      </label>
                  </section>
                  <section class="col col-2" style="float: left;width: 15%;margin-left: 1px;">
                      <label class="input">
                      <#--<input type="text" placeholder="2/12" class="form-control">-->
                          <input type="text" name="areaCode" class="form-control" value="${(mechMap.telecomNumber.areaCode)?default(request.getParameter('areaCode')?if_exists)}" />
                      </label>
                  </section>
                  <section class="col col-2" style="float: left;margin-left: 1px;">
                      <label class="input">
                      <#--<input type="text" placeholder="2/12" class="form-control">-->
                          <input type="text" name="contactNumber" class="form-control" value="${(mechMap.telecomNumber.contactNumber)?default(request.getParameter('contactNumber')?if_exists)}" />
                      </label>
                  </section>
                  <section class="col col-2" style="float: left;width: 15%;margin-left: 1px;">
                      <label class="input">
                      <#--<input type="text" placeholder="2/12" class="form-control">-->
                          <input type="text" name="extension" class="form-control" value="${(mechMap.facilityContactMech.extension)?default(request.getParameter('extension')?if_exists)}" />
                      </label>
                  </section>
              </div>
              <p class="note"><strong>Note:</strong>${uiLabelMap.PhoneNumberRemark}</p>
          </div>
      </div>
  <#elseif "EMAIL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.PartyEmailAddress}</label>
          <div class="col-sm-4">
              <input type="text" class="form-control" required name="emailAddress" value="${(mechMap.contactMech.infoString)?default(request.getParameter('emailAddress')?if_exists)}" />
          </div>
      </div>
  <#else>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${mechMap.contactMechType.get("description",locale)}</label>
          <div class="col-sm-4">
          <#--<input type="text" class="form-control" required name="emailAddress" value="${(mechMap.contactMech.infoString)?default(request.getParameter('emailAddress')?if_exists)}" />-->
              <input type="text" class="form-control" required size="60" maxlength="255" name="infoString" value="${(mechMap.contactMech.infoString)?if_exists}" />
          </div>
      </div>
  </#if>
  </table>
  <div class="form-group">
      <label class="col-md-2 control-label" id="">&nbsp;</label>
      <div class="col-sm-4">
          <a href="<@ofbizUrl>${donePage}</@ofbizUrl>" class="btn btn-labeled btn-default"> <span class="btn-label"><i class="glyphicon glyphicon-chevron-left"></i></span>${uiLabelMap.CommonGoBack} </a>
          <input type="button" class="btn btn-sm btn-primary" name="submintButton" value="${uiLabelMap.CommonSave}" onclick="javascript:document.editcontactmechform.submit()">
	  <#--<input type="submit" value="${uiLabelMap.CommonSave}"/>-->
      </div>
  </div>
  </form>
  </div>
<#else>
  <div class="button-bar">
  <#--<a href='<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>-->
      <a href="<@ofbizUrl>${donePage}</@ofbizUrl>" class="btn btn-labeled btn-default"> <span class="btn-label"><i class="glyphicon glyphicon-chevron-left"></i></span>${uiLabelMap.CommonGoBack} </a>
  </div>
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
