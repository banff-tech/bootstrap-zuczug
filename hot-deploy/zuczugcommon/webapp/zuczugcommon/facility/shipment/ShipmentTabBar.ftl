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
<#if requestAttributes.uiLabelMap?exists>
<#assign uiLabelMap = requestAttributes.uiLabelMap>
</#if>
<#assign selected = tabButtonItem?default("void")>
<#if shipmentId?has_content>
    <div class="button-bar tab-bar">
        <ul>
            <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId!='TRANSFER'>
            	 <li<#if selected="EditShipment"> class="selected"</#if>><a href="<@ofbizUrl>EditShipment?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.CommonEdit}</a></li>
            	<li<#if selected="EditShipmentPlan"> class="selected"</#if>><a href="<@ofbizUrl>EditShipmentPlan?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductShipmentPlan}</a></li>
            </#if>
            <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId='TRANSFER'>
            	 <li<#if selected="EditDirectShipment"> class="selected"</#if>><a href="<@ofbizUrl>EditDirectShipment?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.CommonEdit}</a></li>
            </#if>
            <!--<li<#if selected="AddItemsFromOrder"> class="selected"</#if>><a href="<@ofbizUrl>AddItemsFromOrder?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductOrderItems}</a></li>-->
            <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId='TRANSFER'>
            	 <li<#if selected="EditShipmentItems"> class="selected"</#if>><a href="<@ofbizUrl>EditShipmentItems?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductItems}</a></li>
            </#if>
        </ul>
        <br />
        <br />
    </div>
</#if>
