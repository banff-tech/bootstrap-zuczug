<tr>
  <td class="label">联系人</td>
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
    		<option>-请选择-</option>
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
  <td class="label">详细地址</td>
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