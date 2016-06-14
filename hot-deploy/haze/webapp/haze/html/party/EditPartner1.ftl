
<form method="post" action="<#if party?has_content>updatePartner?partyId=${(parameters.partyId)!}<#else>createNewPartner</#if> " id="NewAccount" class="form-horizontal" onsubmit="javascript:submitFormDisableSubmits(this)" name="NewAccount">
  <input type="hidden" name="accountType" id="NewAccount_accountType">
  <input type="hidden" name="phoneTitle" id="NewAccount_phoneTitle">
  <input type="hidden" name="emailAddressTitle" id="NewAccount_emailAddressTitle">

    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.CompanyName}</label>
        <div class="col-sm-4">
            <input type="text" name="groupName" value="${(partyGroup.groupName)!}" class="form-control" required="true"/>
        </div>
    </div>

    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.FaRen}</label>
        <div class="col-sm-4">
        <#if parameters.partyId?has_content>
          <#assign relationships = delegator.findByAnd("PartyRelationship",{"partyIdFrom":parameters.partyId,"roleTypeIdFrom":"ORGANIZATION_ROLE","roleTypeIdTo":"LEGAL_PERSON","partyRelationshipTypeId":"LEGAL_PERSON"})?if_exists>
        </#if>
        <@htmlTemplate.lookupField value="${(relationships[0].partyIdTo)!}" formName="NewAccount" name="corporatePartyId"  fieldFormName="LookupPartyName"/>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">添加的新的法人信息</label>
        <div class="col-sm-4">
            <input type="checkbox" value="Y" class="form-control" name="newCorporatePartyId" id="newfaren"/>
            <p class="note"><strong>Note:</strong>如果是第一次添加法人信息，可以直接在输入框中输入姓名，如果不是第一次，请尝试搜索框</p>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.TheBusinesslicense}</label>
        <div class="col-sm-4">
          <#assign businessLicense = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"BUSINESS_LICENSE")?if_exists>
            <input type="text" name="businessLicense" value="${(businessLicense)!}" class="form-control"/>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.ShuiWuDengJiHao}</label>
        <div class="col-sm-4">
            <#assign taxNumber = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"TAX_NUM")?if_exists>
            <input type="text" name="taxNumber" value="${(taxNumber)!}" class="form-control"/>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.YiBanNaShuiRenZiGe}</label>
        <div class="col-sm-4">
            <#assign taxpayer = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"TAXINFO")?if_exists>
            <input type="text" name="taxpayer" value="${(taxpayer)!}" class="form-control"/>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.ZhuCeZiJin}</label>
        <div class="col-sm-4">
        <#assign registeredCapital = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"REG_CAPITAL")?if_exists>
            <input type="text" name="registeredCapital" value="${(registeredCapital)!}" class="form-control"/></td>
        </div>
    </div>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">${uiLabelMap.YingYeFanWei}</label>
        <div class="col-sm-4">
        <#assign businessScope = Static["com.zuczug.party.ZuczugPartyUtil"].getPartyAttribute(delegator,parameters.partyId,"BUSINESS_SCOPE")?if_exists>
            <textarea class=" form-control" cols="60" rows="3" name="businessScope">${(businessScope)!}</textarea>
        </div>
    </div>

    <#if !(party?has_content)><#-- 只有新增的时候才会选择这个-->
      <hr/>
      <div class="form-group">
          <label class="col-md-2 control-label" id="" style="text-align: left"><b>${uiLabelMap.AccountingInvoiceRoles}</b></label>
          <div class="col-sm-4">
              &nbsp;
          </div>
      </div>

      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.Default}${uiLabelMap.AccountingInvoiceRoles}</label>
          <div class="col-sm-4">
              <select name="roleTypeId" class="form-control">
                  <option value="">${uiLabelMap.PleaseSelectOne}</option>
                  <!-- assign des="RoleType.description."+ role.roleTypeId/ -->
              <#-- option value="${(role.roleTypeId)!}">${(uiLabelMap[des])!}</option -->
                  <option value="CUSTOMER">${uiLabelMap['PartyRelationshipType.partyRelationshipName.CUSTOMER_REL']}</option>
                  <option value="SUPPLIER">${uiLabelMap['PageTitleListSupplierProduct']}</option>
                  <option value="PARTNER">${uiLabelMap['PartyRelationshipType.partyRelationshipName.PARTNERSHIP']}</option>
                  <option value="COMPETITOR">${uiLabelMap['RoleType.description.COMPETITOR']}</option>
              </select>
          </div>
      </div>
      <hr/>
      <div class="form-group">
          <label class="col-md-2 control-label" id="" style="text-align: left;"><b>${uiLabelMap.ContactAddress}</b></label>
          <div class="col-sm-4">
              &nbsp;
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.ContactPeople}</label>
          <div class="col-sm-4">
              <input type="text" size="50" maxlength="100" name="toName"  class="form-control" value="${(mechMap.postalAddress.toName)?default(request.getParameter('toName')?if_exists)}" />
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.CommonCountry}</label>
          <div class="col-sm-4">
              <select name="countryGeoId" id="countryGeoId" class="form-control">
                  <option selected="selected" value="CHN">${uiLabelMap.China}</option>
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
                          <select name="stateProvinceGeoId" id="stateProvinceGeoId" class="form-control" style="width: 100%">
                              <option>${uiLabelMap.PleaseSelectOne}</option>
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
                          <select name="cityGeoId" id="cityGeoId" class="form-control">
                            <#if mechMap?has_content && (mechMap.postalAddress?exists) && (mechMap.postalAddress.cityGeoId?exists)>
                              <#assign cityGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.cityGeoId})?if_exists/>
                                <option value="${(cityGeo.geoId)!}">${(cityGeo.geoName)!}</option>
                            </#if>
                          </select>
                      </label>
                  </section>
                  <section class="col col-4" style="float: left;width: 33.33%;">
                      <label class="select" style="width: 100%;">
                          <select name="countyGeoId" id="countyGeoId" class="form-control">
                            <#if (mechMap.postalAddress?exists) && (mechMap.postalAddress.countyGeoId?exists)>
                              <#assign countyGeo = delegator.findOne("Geo",true, {"geoId":mechMap.postalAddress.countyGeoId})?if_exists/>
                                <option value="${(countyGeo.geoId)!}">${(countyGeo.geoName)!}</option>
                            </#if>
                          </select>
                      </label>
                  </section>
              </div>
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.DetailedAddress}</label>
          <div class="col-sm-4">
              <input type="text" size="60" maxlength="255" name="address1" class="form-control" value="${(mechMap.postalAddress.address1)?default(request.getParameter('address1')?if_exists)}" />
          </div>
      </div>
      <div class="form-group">
          <label class="col-md-2 control-label" id="">${uiLabelMap.PartyZipCode}</label>
          <div class="col-sm-4">
              <input type="text" size="30" maxlength="60" name="postalCode" class="form-control" value="${(mechMap.postalAddress.postalCode)?default(request.getParameter('postalCode')?if_exists)}" />
          </div>
      </div>
      <hr>
      <div class="form-group">
        <label class="col-md-2 control-label" id="" style="text-align: left;"><b>${uiLabelMap.ContactPhone}</b></label>
        <div class="col-sm-4">
            &nbsp;
        </div>
      </div>
        <div class="form-group">
            <label class="col-md-2 control-label" id="">${uiLabelMap.PartyPhoneNumber}</label>
            <div class="col-sm-4">
                <div class="row" style="padding-left: 13px;">
                    <section class="col col-2" style="float: left;width: 15%">
                        <label class="input">
                            <#--<input type="text" placeholder="2/12" class="form-control">-->
                            <input type="text" name="countryCode" id="NewAccount_countryCode" class="form-control">
                        </label>
                    </section>
                    <section class="col col-2" style="float: left;width: 15%;margin-left: 1px;">
                        <label class="input">
                            <#--<input type="text" placeholder="2/12" class="form-control">-->
                            <input type="text" name="areaCode" id="NewAccount_areaCode" class="form-control">
                        </label>
                    </section>
                    <section class="col col-2" style="float: left;margin-left: 1px;">
                        <label class="input">
                            <#--<input type="text" placeholder="2/12" class="form-control">-->
                            <input type="text" name="contactNumber" id="NewAccount_contactNumber" class="form-control">
                        </label>
                    </section>
                    <section class="col col-2" style="float: left;width: 15%;margin-left: 1px;">
                        <label class="input">
                            <input type="text" name="extension" id="NewAccount_extension" class="form-control">
                            <#--<input type="text" placeholder="2/12" class="form-control">-->
                        </label>
                    </section>

                </div>
                <p class="note"><strong>Note:</strong>${uiLabelMap.PhoneNumberRemark}</p>
            </div>
        </div>
        <hr>
        <div class="form-group">
            <label class="col-md-2 control-label" id="" style="text-align: left;"><b>${uiLabelMap['CommunicationEventType.description.EMAIL_COMMUNICATION']}</b></label>
            <div class="col-sm-4">
                &nbsp;
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-2 control-label" id="">${uiLabelMap['CommunicationEventType.description.EMAIL_COMMUNICATION']}</label>
            <div class="col-sm-4">
                <input type="text" name="emailAddress" class="form-control" id="NewAccount_emailAddress">
            </div>
        </div>
    </#if>
    <div class="form-group">
        <label class="col-md-2 control-label" id="">&nbsp;</label>
        <div class="col-sm-4">
            <input type="submit" class="btn btn-sm btn-primary" name="submintButton" value="${uiLabelMap.CommonSave}">
        </div>
    </div>
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
