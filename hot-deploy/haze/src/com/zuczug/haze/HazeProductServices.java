package com.zuczug.haze;

import java.util.Map;

import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class HazeProductServices {

	public static final String module = HazeProductServices.class.getName();
    public static final String resource = "HazeUiLabels";
	
    /**
     * 将product提交到素然新运营平台，也就是zuczug的tenant。
     */
    public static Map<String, Object> submitProductToZuczug(DispatchContext dctx, Map<String, ? extends Object> context){
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        return result;
    }
        
    /**
     * 将BOM提交到素然新运营平台，也就是zuczug的tenant。
     */
    public static Map<String, Object> submitBomToZuczug(DispatchContext dctx, Map<String, ? extends Object> context){
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId"); //这个product是成品，不是原料
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        return result;
    }
        
}
