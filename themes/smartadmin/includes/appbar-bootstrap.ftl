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
<#assign appModelMenu = Static["org.ofbiz.widget.menu.MenuFactory"].getMenuFromLocation(applicationMenuLocation,applicationMenuName)>
<#assign modelMenus = Static["org.ofbiz.widget.menu.MenuFactory"].getMenusFromLocation(applicationMenuLocation)>

<#assign menus = modelMenus.keySet()>
<#assign menuItemList = appModelMenu.menuItemList>
<#if menuItemList?has_content>
	<nav class="navbar navbar-default" role="navigation" id="app-navigation">
		<ul class="nav navbar-nav">
			<#list menuItemList as item>
				<#assign name = item.name>
				<#assign title = item.getTitle(context)>
				<#if (item.getLink().getTarget(context))?has_content>
					<#assign target = item.getLink().getTarget(context)>
					<#else>
						<#if item.getParentPortalPageId(context)?has_content>
							<#assign parentPortalPageId = item.getParentPortalPageId(context)>
							<#assign portalPages = Static["org.ofbiz.widget.PortalPageWorker"].getPortalPages(parentPortalPageId, context)>
							<#list portalPages as portalPage>
								<#assign name = portalPage.portalPageName>
								<#assign link = "showPortalPage?portalPageId=${portalPage.portalPageId}">
								<#if portalPage.parentPortalPageId?has_content>
									<#assign target = link+"&amp;parentPortalPageId=${portalPage.parentPortalPageId?if_exists}">
									<#else>
										<#assign target = link>
								</#if>
								<li>
									<a href="<@ofbizUrl>${target?if_exists}</@ofbizUrl>">${portalPage.get("portalPageName",locale)}</a>
								</li>
							</#list>
						</#if>
				</#if>
				<#-- Get TabBar submenu based on menu name -->
				<#assign subMenuName = "${name}TabBar">
				<#if menus?seq_contains("${subMenuName}")>
					<#assign subModelMenu = Static["org.ofbiz.widget.menu.MenuFactory"].getMenuFromLocation(applicationMenuLocation,subMenuName)>
					<#if subModelMenu?has_content>
						<#assign subMenuItemList = subModelMenu.menuItemList>
						<#if subMenuItemList?has_content>
							<li class="dropdown">
								<a href="<@ofbizUrl>${target?if_exists}</@ofbizUrl>" class="dropdown-toggle" data-toggle="dropdown">${title?if_exists} <span class="caret"></span></a>
								<ul class="dropdown-menu">
									<#list subMenuItemList as subMenuItem>
										<#assign name = subMenuItem.name>
										<#assign title = subMenuItem.getTitle(context)>
										<#assign target = subMenuItem.getLink().getTarget(context)>
										<li>
											<a href="<@ofbizUrl>${target?if_exists}</@ofbizUrl>">${title?if_exists}</a>
										</li>
									</#list>
								</ul>
							</li>
						</#if>
					</#if>
				<#elseif menus?seq_contains("${subMenuName?cap_first}")>
					<#assign subMenuName = "${subMenuName?cap_first}">
					<#assign subModelMenu = Static["org.ofbiz.widget.menu.MenuFactory"].getMenuFromLocation(applicationMenuLocation,subMenuName)>
					<#if subModelMenu?has_content>
						<#assign subMenuItemList = subModelMenu.menuItemList>
						<#if subMenuItemList?has_content>
							<li class="dropdown">
								<a href="<@ofbizUrl>${target}</@ofbizUrl>" class="dropdown-toggle" data-toggle="dropdown">${title?if_exists} <span class="caret"></span></a>
								<ul class="dropdown-menu">
									<#list subMenuItemList as subMenuItem>
										<#assign name = subMenuItem.name>
										<#assign title = subMenuItem.getTitle(context)>
										<#assign target = subMenuItem.getLink().getTarget(context)>
										<li>
											<a href="<@ofbizUrl>${target?if_exists}</@ofbizUrl>">${title?if_exists}</a>
										</li>
									</#list>
								</ul>
							</li>
						</#if>
					</#if>
				<#else>
					<#if name == "main">
						<li class="menuTitle">
							<a href="<@ofbizUrl>${target?if_exists}</@ofbizUrl>">${applicationTitle}&nbsp;&nbsp;<span class="glyphicon glyphicon-home"></span>&nbsp;</a>
						</li>
						<#else>
							<li>
								<a href="<@ofbizUrl>${target?if_exists}</@ofbizUrl>">${title?if_exists}</a>
							</li>
					</#if>
				</#if>
			</#list>
		</ul>
	</nav>
</#if>