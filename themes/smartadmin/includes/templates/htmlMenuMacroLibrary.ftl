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

<#macro renderMenuBegin boundaryComment="" id="" style="" title="">
  <#if boundaryComment?has_content>
<!-- ${boundaryComment} -->
  </#if>

  <#if style == "button-bar tab-bar">
  	<#if id?has_content && id == 'app-navigation'>
  		<nav <#if id?has_content> id="${id}"</#if>>
  			<ul>
  				<li>
  					<#if title?has_content>
	  					<li class="menuTitle">
	  						<a href="#">
	  							<i class="fa fa-lg fa-fw fa-home"></i>
	  							<span class="menu-item-parent">${title}</span>
	  						</a>
	  					</li>
	  				</#if>
  				</li>
  	<#else>
  		<div<#if id?has_content> id="${id}"</#if> class="navbar navbar-default" style="margin-bottom: 12px;min-height: 42px;">
  			<ul class="nav nav-tabs bordered">
  				<#if title?has_content>
  				<li class="menuTitle"><a href="#">${title}</a></li>
  				</#if>
  	</#if>
		
	<#elseif style == "button-bar button-style-2">
		<#-- <nav class="navbar navbar-default button-bar button-style-2" role="navigation"> -->
  			<#-- <ul class="demo-btns"> -->
  			<ul class="nav nav-tabs bordered">
  	<#elseif style?has_content>
  		<ul class="${style}">
	<#else>
		<nav class="navbar navbar-default" role="navigation">
  			<ul class="nav navbar-nav">
  </#if>
	
</#macro>

<#macro renderMenuEnd boundaryComment="" style="">
	<#if style == "button-bar tab-bar">
				</ul>
			</div>
		<#elseif style == "button-bar button-style-2">
			</ul>
		<#else>
			</ul>
	</#if>
	
<#if boundaryComment?has_content>
<!-- ${boundaryComment} -->
</#if>
</#macro>

<#macro renderImage src id style width height border>
<img src="${src}"<#if id?has_content> id="${id}"</#if><#if style?has_content> class="${style}"</#if><#if width?has_content> width="${width}"</#if><#if height?has_content> height="${height}"</#if><#if border?has_content> border="${border}"</#if> />
</#macro>

<#macro renderLink linkUrl parameterList targetWindow uniqueItemName actionUrl linkType="" id="" style="" name="" height="" width="" text="" imgStr="">
  <#if linkType?has_content && "hidden-form" == linkType>
<form method="post" action="${actionUrl}"<#if targetWindow?has_content> target="${targetWindow}"</#if> onsubmit="javascript:submitFormDisableSubmits(this)" name="${uniqueItemName}"><#rt/>
    <#list parameterList as parameter>
<input name="${parameter.name}" value="${parameter.value}" type="hidden"/><#rt/>
    </#list>
</form><#rt/>
  </#if>
<#if (linkType?has_content && "hidden-form" == linkType) || linkUrl?has_content>
<a<#if id?has_content> id="${id}"</#if><#if style?has_content> class="${style}"</#if><#if name?has_content> name="${name}"</#if><#if targetWindow?has_content> target="${targetWindow}"</#if> href="<#if "hidden-form"==linkType>javascript:document.${uniqueItemName}.submit()<#else>${linkUrl}</#if>"><#rt/>
</#if>
<#if imgStr?has_content>${imgStr}</#if><#if text?has_content>${text}</#if><#rt/>
<#if (linkType?has_content && "hidden-form" == linkType) || linkUrl?has_content></a><#rt/></#if>
</#macro>

<#macro renderMenuItemBegin style toolTip linkStr containsNestedMenus>
	<#if style == "buttontext create">
		<li role="presentation" <#if toolTip?has_content> title="${toolTip}"</#if>><#if linkStr?has_content>${linkStr}</#if><#if containsNestedMenus><ul></#if><#rt/>
		<script type="text/javascript">
	      jQuery(".navbar-pills.pull-right li a.buttontext.create").each(function(){
	      	var linkText = jQuery(this).html();
	      	var data = '<span class="glyphicon glyphicon-plus"></span> '+linkText;
	      	jQuery(this).removeClass('buttontext').removeClass('create').addClass("btn btn-primary btn-sm");
	      	jQuery(this).html(data);
	      	});
	    </script>
	<#elseif style == "selected">
		<li class="active"<#if toolTip?has_content> title="${toolTip}"</#if>><#if linkStr?has_content>${linkStr}</#if><#if containsNestedMenus><ul></#if><#rt/>
	<#else>
        <li<#if style?has_content> class="${style}"</#if><#if toolTip?has_content> title="${toolTip}"</#if>><#if linkStr?has_content>${linkStr}</#if><#if containsNestedMenus><ul></#if><#rt/>
    </#if>
</#macro>

<#macro renderMenuItemEnd containsNestedMenus>
<#if containsNestedMenus></ul></#if></li>
</#macro>
