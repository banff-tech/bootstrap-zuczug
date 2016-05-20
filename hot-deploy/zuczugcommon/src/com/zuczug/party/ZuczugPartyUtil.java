package com.zuczug.party;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

/**
 * @author Sven Wong
 * @version 创建时间：2016年3月1日 上午11:24:34
 * @description：
 */

public class ZuczugPartyUtil {
	public static String module = ZuczugPartyUtil.class.getName();
	/**
	 * 获取一个party的role，以文字形式返回，如果是多个，就拼接分好 by王亮
	 * @param delegator
	 * @param partyId
	 * @return
	 */
	public static String getPartyRolesString(Delegator delegator,String partyId){
		String roleStr = "";
		
		try {
			List<GenericValue> roletypes = delegator.findByAnd("PartyRoleAndPartyDetail", UtilMisc.toMap("partyId",partyId));
			if(UtilValidate.isNotEmpty(roletypes)){
				for (GenericValue par : roletypes) {
					GenericValue roleType = delegator.findOne("RoleType",false ,UtilMisc.toMap("roleTypeId",par.getString("roleTypeId")));
					roleStr+=roleType.get("description",Locale.CHINESE)+";";
				}
			}
			if(roleStr.length() > 0){
				roleStr = roleStr.substring(0,roleStr.length()-1);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return roleStr;
	}
	
	/**
	 * 获取一个party的某个目的的地址，取第一条，返回完整中文字符串 by王亮
	 * @param delegator
	 * @param partyId
	 * @param partyContactMechPurpose
	 * @return
	 */
	public static Map<String, Object> getPartyPostalAddress(Delegator delegator,String partyId,String partyContactMechPurpose){
		try {
			List<GenericValue> list = delegator.findByAnd("PartyAddress", UtilMisc.toMap("partyId",partyId,"contactMechPurposeTypeId",partyContactMechPurpose));
			if(UtilValidate.isNotEmpty(list)){
				return getPostalAddressString(delegator,list.get(0),Locale.CHINA,false);
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * 收货地址 完整的字符串 by王亮
     * @param delegator
     * @param addr
     * @param locale
     * @param hasPhone
     * @return
     */
    public static Map<String, Object> getPostalAddressString(Delegator delegator, GenericValue addr, Locale locale, boolean hasPhone){
    	Map<String, Object> result = FastMap.newInstance();
    	if(UtilValidate.isEmpty(addr))return result;
    	result = addr.getAllFields();
    	
    	String country = getDescription(delegator,"Geo","geoId",addr.getString("countryGeoId"),"geoName",null);
        result.put("country", country);
        
        String province = getDescription(delegator,"Geo","geoId",addr.getString("stateProvinceGeoId"),"geoName",null);
        result.put("province", province);
        
        String city = getDescription(delegator,"Geo","geoId",addr.getString("cityGeoId"),"geoName",null);
        result.put("city", city);
        
        String county = getDescription(delegator, "Geo", "geoId", addr.getString("countyGeoId"), "geoName", null);
        result.put("county", county);
        
        String address1 = UtilValidate.isNotEmpty(addr.get("address1"))?addr.getString("address1"):"";
        result.put("address1", address1);
        
        String address = province +" "+ city  +" "+ county +" "+ address1;
        if(UtilValidate.isNotEmpty(country)) address = country +" "+ address;
        if(UtilValidate.isNotEmpty(addr.get("address2"))) address += " "+ addr.getString("address2");
        if(UtilValidate.isNotEmpty(addr.get("postalCode"))) address += " ["+ addr.getString("postalCode") +"]";
        
        if(hasPhone){
            String phoneNumber = UtilValidate.isNotEmpty(addr.get("phoneNumber"))?addr.getString("phoneNumber"):"";
            String mobilePhone = UtilValidate.isNotEmpty(addr.get("mobilePhone"))?addr.getString("mobilePhone"):"";

            if(UtilValidate.isNotEmpty(addr.get("phoneNumber")) && UtilValidate.isNotEmpty(addr.get("mobilePhone"))){
                address += " ["+ phoneNumber +"/"+ mobilePhone +"]";
            }
            else if (UtilValidate.isNotEmpty(addr.get("phoneNumber"))){
                address += " ["+ phoneNumber +"]";
            }
            else if (UtilValidate.isNotEmpty(addr.get("mobilePhone"))){
                address += " ["+ mobilePhone +"]";
            }
        }
        result.put("fullAddress", address);
        return result;
    }
    
    /**
     * 返回 指定数据的description值 by王亮
     * @param delegator
     * @param entityName
     * @param pkKey
     * @param pkValue
     * @param descKey
     * @param locale
     * @return
     */
    public static String getDescription(Delegator delegator,
                String entityName, String pkKey, String pkValue, String descKey, Locale locale){
        String result = "";
        if(UtilValidate.isEmpty(entityName) || UtilValidate.isEmpty(pkKey) || UtilValidate.isEmpty(pkValue) || UtilValidate.isEmpty(descKey)) return result;
        try {
            GenericValue ent = delegator.findOne(entityName, UtilMisc.toMap(pkKey, pkValue),true);
            if(UtilValidate.isNotEmpty(ent) && UtilValidate.isNotEmpty(ent.get(descKey))){
                if(UtilValidate.isNotEmpty(locale)){
                	result = (String) ent.get(descKey,locale);
                }else{
                    result = String.valueOf(ent.get(descKey));
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, e.getMessage(), module);
        }
        return result;
    }
    
    
    /**
     * 根据关联类型，获取下一级的Geo列表 by王亮
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getNextAssocGeoList(DispatchContext dctx, Map<String, ? extends Object> context){
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();

        String geoIdFrom = (String) context.get("geoIdFrom");
        String geoAssocTypeId = (String) context.get("geoAssocTypeId");

        List<GenericValue> list = FastList.newInstance();
        try {
            list = delegator.findByAnd("GeoAssocAndGeoTo",
                    UtilMisc.toMap("geoIdFrom",geoIdFrom, "geoAssocTypeId",geoAssocTypeId), UtilMisc.toList("geoId"));
        } catch (GenericEntityException e) {
            Debug.logError(e, e.getMessage(), module);
        }
        result.put("list", list);
        return result;
    }
    
    /**
     * 获取party的attribute by王亮
     * @param delegator
     * @param partyId
     * @param attrName
     * @return
     */
    public static String getPartyAttribute(Delegator delegator,String partyId,String attrName){
        String attrValue = "";
        
        try {
			GenericValue pa = delegator.findOne("PartyAttribute", false, UtilMisc.toMap("partyId",partyId,"attrName",attrName));
			if(UtilValidate.isNotEmpty(pa)){
				attrValue = pa.getString("attrValue");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return attrValue;
    }
    
    
}
