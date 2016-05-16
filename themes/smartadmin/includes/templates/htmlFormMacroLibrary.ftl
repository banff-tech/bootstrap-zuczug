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

<#macro renderField text>
  <#if text??>
    ${text}<#lt/>
  </#if>
</#macro>

<#macro renderDisplayField type imageLocation idName description title class alert inPlaceEditorUrl="" inPlaceEditorParams="">
  <#if type?has_content && type=="image">
    <img src="${imageLocation}" alt=""><#lt/>
  <#else>
    <#if inPlaceEditorUrl?has_content || class?has_content || alert=="true" || title?has_content>
      <span <#if idName?has_content>id="cc_${idName}"</#if> <#if title?has_content>title="${title}"</#if> <@renderClass class alert />><#t/>
    </#if>

    <#if description?has_content>
        ${description?replace("\n", "<br />")}
      <#t/>
    <#else>
      &nbsp;<#t/>
    </#if>
    <#if inPlaceEditorUrl?has_content || class?has_content || alert=="true">
      </span><#lt/>
    </#if>
    <#if inPlaceEditorUrl?has_content && idName?has_content>
      <script language="JavaScript" type="text/javascript"><#lt/>
        ajaxInPlaceEditDisplayField('cc_${idName}', '${inPlaceEditorUrl}', ${inPlaceEditorParams});<#lt/>
      </script><#lt/>
    </#if>
  </#if>
</#macro>
<#macro renderHyperlinkField></#macro>

<#macro renderTextField name className alert value textSize maxlength id event action disabled clientAutocomplete ajaxUrl ajaxEnabled mask placeholder="">
  <#if mask?has_content>
    <script type="text/javascript">
      jQuery(function($){jQuery("#${id}").mask("${mask}");});
    </script>
  </#if>
  <input type="text" name="${name?default("")?html}"<#t/>
    <#if className?has_content && className?index_of("required") != -1>
         required="true"  style="background-color: #eeeeee;"
    </#if>
  	<#assign fieldclass = className?replace("required","") + " form-control">
    <#if fieldclass?has_content && fieldclass?index_of("listDisplay")!=-1>style="margin-top: -25px;"</#if>
    <@renderClass fieldclass alert />
    <#if value?has_content> value="${value}"</#if><#rt/>
    <#if textSize?has_content> size="${textSize}"</#if><#rt/>
    <#if maxlength?has_content> maxlength="${maxlength}"</#if><#rt/>
    <#if disabled?has_content && disabled> disabled="disabled"</#if><#rt/>
    <#if id?has_content> id="${id}"</#if><#rt/>
    <#if event?has_content && action?has_content> ${event}="${action}"</#if><#rt/>
    <#if clientAutocomplete?has_content && clientAutocomplete=="false"> autocomplete="off"</#if><#rt/>
    <#if placeholder?has_content> placeholder="${placeholder}"</#if><#rt/>
  /><#t/>
  <#if ajaxEnabled?has_content && ajaxEnabled>
    <#assign defaultMinLength = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.autocompleter.defaultMinLength")>
    <#assign defaultDelay = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.autocompleter.defaultDelay")>
    <script language="JavaScript" type="text/javascript">ajaxAutoCompleter('${ajaxUrl}', false, ${defaultMinLength!2}, ${defaultDelay!300});</script><#lt/>
  </#if>
</#macro>

<#macro renderTextIconField name className alert value textSize maxlength id event action disabled clientAutocomplete ajaxUrl ajaxEnabled mask placeholder="">
  <#if mask?has_content>
    <script type="text/javascript">
      jQuery(function($){jQuery("#${id}").mask("${mask}");});
    </script>
  </#if>
  <input type="text" name="${name?default("")?html}"<#t/>
  	<#assign fieldclass = className + " form-control">
    <@renderClass fieldclass alert />
    <#if value?has_content> value="${value}"</#if><#rt/>
    <#if textSize?has_content> size="${textSize}"</#if><#rt/>
    <#if maxlength?has_content> maxlength="${maxlength}"</#if><#rt/>
    <#if disabled?has_content && disabled> disabled="disabled"</#if><#rt/>
    <#if id?has_content> id="${id}"</#if><#rt/>
    <#if event?has_content && action?has_content> ${event}="${action}"</#if><#rt/>
    <#if clientAutocomplete?has_content && clientAutocomplete=="false"> autocomplete="off"</#if><#rt/>
    <#if placeholder?has_content> placeholder="${placeholder}"</#if><#rt/>
  /><#t/>
  <i class="ace-icon fa fa-leaf blue"></i>
  <#if ajaxEnabled?has_content && ajaxEnabled>
    <#assign defaultMinLength = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.autocompleter.defaultMinLength")>
    <#assign defaultDelay = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.autocompleter.defaultDelay")>
    <script language="JavaScript" type="text/javascript">ajaxAutoCompleter('${ajaxUrl}', false, ${defaultMinLength!2}, ${defaultDelay!300});</script><#lt/>
  </#if>
</#macro>

<#macro renderTextareaField name className alert cols rows id readonly value visualEditorEnable buttons language="">
  <textarea name="${name}"<#t/>
            <#if className?has_content && className?index_of("required") != -1>required="true"; style="background-color: #eeeeee;" </#if> <#assign fieldclass = className?replace("required","") + " form-control"> <@renderClass fieldclass alert />
    <@renderClass fieldclass alert />
    <#if cols?has_content> cols="${cols}"</#if><#rt/>
    <#if rows?has_content> rows="${rows}"</#if><#rt/>
    <#if id?has_content> id="${id}"</#if><#rt/>
    <#if readonly?has_content && readonly=='readonly'> readonly="readonly"</#if><#rt/>
    <#if maxlength?has_content> maxlength="${maxlength}"</#if><#rt/>
    ><#t/>
    <#if value?has_content>${value}</#if><#t/>
  </textarea><#lt/>
  <#if visualEditorEnable?has_content>
    <script language="javascript" src="/images/jquery/plugins/elrte-1.3/js/elrte.min.js" type="text/javascript"></script><#rt/>
    <#if language?has_content && language != "en">
      <script language="javascript" src="/images/jquery/plugins/elrte-1.3/js/i18n/elrte.${language!"en"}.js" type="text/javascript"></script><#rt/>
    </#if>
    <link href="/images/jquery/plugins/elrte-1.3/css/elrte.min.css" rel="stylesheet" type="text/css">
    <script language="javascript" type="text/javascript">
      var opts = {
         cssClass : 'el-rte',
         lang     : '${language!"en"}',
         toolbar  : '${buttons?default("maxi")}',
         doctype  : '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">', //'<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN">',
         cssfiles : ['/images/jquery/plugins/elrte-1.3/css/elrte-inner.css']
      }
      jQuery('#${id?default("")}').elrte(opts);
    </script>
  </#if>
</#macro>

<#macro renderDateTimeField name className alert title value size maxlength id dateType shortDateInput timeDropdownParamName defaultDateTimeString localizedIconTitle timeDropdown timeHourName classString hour1 hour2 timeMinutesName minutes isTwelveHour ampmName amSelected pmSelected compositeType formName mask="" event="" action="" step="" timeValues="">
<div class="form-group">
    <div class="input-group" style="margin: 0px 13px 0px 13px;">
        <input  type="text" <#if className?has_content && className?index_of("required") != -1>required="true";  </#if>  <#if name?has_content> name="${name}_fld0_value"</#if> placeholder="开始时间" value="${(value)!}" class="datepicker" data-dateformat="yy/mm/dd" style="
          width: 100%;
          height: 32px;
          padding: 6px 12px;
          border: 1px solid #ccc;
          float: left;
          <#if className?has_content && className?index_of("required") != -1>
                  background-color: #eeeeee;
          </#if>
          ">
        <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
    </div>
</div>
</#macro>

<#macro renderDropDownField name className alert id multiple formName otherFieldName event action size firstInList currentValue explicitDescription allowEmpty options fieldName otherFieldName otherValue otherFieldSize dDFCurrent ajaxEnabled noCurrentSelectedKey ajaxOptions frequency minChars choices autoSelect partialSearch partialChars ignoreCase fullSearch>
    <select name="${name?default("")}<#rt/>"
    <#if className?has_content && className?index_of("required") != -1>required="true" style="background-color: #eeeeee;" </#if> <#assign fieldclass = className?replace("required","") + " form-control"> <@renderClass fieldclass alert />
  <#if id?has_content> id="${id}"</#if><#if multiple?has_content> multiple="multiple"</#if><#if otherFieldSize gt 0> onchange="process_choice(this,document.${formName}.${otherFieldName})"</#if><#if event?has_content> ${event}="${action}"</#if>>
      <#if firstInList?has_content && currentValue?has_content && !multiple?has_content>
        <option selected="selected" value="${currentValue}">${explicitDescription}</option><#rt/>
        <option value="${currentValue}">---</option><#rt/>
      </#if>
      <#if allowEmpty?has_content || !options?has_content>
        <option value="">&nbsp;</option>
      </#if>
      <#list options as item>
        <#if multiple?has_content>
          <option<#if currentValue?has_content && item.selected?has_content> selected="${item.selected}" <#elseif !currentValue?has_content && noCurrentSelectedKey?has_content && noCurrentSelectedKey == item.key> selected="selected" </#if> value="${item.key}">${item.description}</option><#rt/>
        <#else>
          <option<#if currentValue?has_content && currentValue == item.key && dDFCurrent?has_content && "selected" == dDFCurrent> selected="selected"<#elseif !currentValue?has_content && noCurrentSelectedKey?has_content && noCurrentSelectedKey == item.key> selected="selected"</#if> value="${item.key}">${item.description}</option><#rt/>
        </#if>
      </#list>
    </select>
  <#if otherFieldName?has_content>
    <noscript><input type='text' name='${otherFieldName}' /></noscript>
    <script type='text/javascript' language='JavaScript'><!--
      disa = ' disabled';
      if(other_choice(document.${formName}.${fieldName}))
        disa = '';
      document.write("<input type='text' name='${otherFieldName}' value='${otherValue?js_string}' size='${otherFieldSize}'"+disa+" onfocus='check_choice(document.${formName}.${fieldName})' />");
      if(disa && document.styleSheets)
      document.${formName}.${otherFieldName}.style.visibility  = 'hidden';
    //--></script>
  </#if>

  <#if ajaxEnabled>
    <script language="JavaScript" type="text/javascript">
      ajaxAutoCompleteDropDown();
      jQuery(function() {
        jQuery("#${id}").combobox();
      });
    </script>
  </#if>
</#macro>

<#macro renderCheckField items className alert id allChecked currentValue name event action>
  <#list items as item>
    <label <@renderClass className+"checkbox-inline" alert />><#rt/>
      <input <#if className?has_content && className?index_of("required") != -1>required="true" style="background-color: #eeeeee;" </#if> type="checkbox"<#if (item_index == 0)> id="${id}"</#if><#rt/>
        <#if allChecked?has_content && allChecked> checked="checked" <#elseif allChecked?has_content && !allChecked>
          <#elseif currentValue?has_content && currentValue==item.value> checked="checked"</#if>
          name="${name?default("")?html}" value="${item.value?default("")?html}"<#if event?has_content> ${event}="${action}"</#if> class="checkbox style-0"/><#rt/>
        <span>${item.description?default("")}</span>
    </label>
  </#list>
</#macro>

<#macro renderRadioField items className alert currentValue noCurrentSelectedKey name event action>
  <#list items as item>
    <span <@renderClass className alert />><#rt/>
        <label class="radio radio-inline">
          <input type="radio" <#if className?has_content && className?index_of("required") != -1>required="true" style="background-color: #eeeeee;" </#if> <#assign fieldclass = className?replace("required","")> <@renderClass fieldclass alert /> <#if currentValue?has_content><#if currentValue==item.key> checked="checked"</#if>
            <#elseif noCurrentSelectedKey?has_content && noCurrentSelectedKey == item.key> checked="checked"</#if>
            name="${name?default("")?html}" value="${item.key?default("")?html}"<#if event?has_content> ${event}="${action}"</#if> class="radiobox"/><#rt/>
          <span>${item.description}</span>
        </label>
    </span>
  </#list>
</#macro>

<#macro renderSubmitField buttonType className alert formName title name event action imgSrc confirmation containerId ajaxUrl>
  <#if buttonType=="text-link">
    <a <@renderClass className alert /> href="javascript:document.${formName}.submit()" <#if confirmation?has_content>onclick="return confirm('${confirmation?js_string}');"</#if>><#if title?has_content>${title}</#if> </a>
  <#elseif buttonType=="image">

    <input type="image" src="${imgSrc}" <@renderClass className alert /><#if name?has_content> name="${name}"</#if>
    <#if title?has_content> alt="${title}"</#if><#if event?has_content> ${event}="${action}"</#if>
    <#if confirmation?has_content>onclick="return confirm('${confirmation?js_string}');"</#if>/>
  <#else>
    <input type="<#if containerId?has_content>button<#else>submit</#if>" <#if className?has_content><@renderClass className alert /><#else>class="btn btn-sm btn-primary"</#if>
    <#if name??> name="${name}"</#if><#if title?has_content> value="${title}"</#if><#if event?has_content> ${event}="${action}"</#if>
    <#if containerId?has_content> onclick="<#if confirmation?has_content>if (confirm('${confirmation?js_string}')) </#if>ajaxSubmitFormUpdateAreas('${containerId}', '${ajaxUrl}')"
      <#else><#if confirmation?has_content> onclick="return confirm('${confirmation?js_string}');"</#if>
    </#if>/>
  </#if>
</#macro>

<#macro renderResetField className alert name title>
  <input type="reset" <@renderClass className alert /> name="${name}"<#if title?has_content> value="${title}"</#if>/>
</#macro>

<#macro renderHiddenField name value id event action>
  <input type="hidden" name="${name}"<#if value?has_content> value="${value}"</#if><#if id?has_content> id="${id}"</#if><#if event?has_content && action?has_content> ${event}="${action}"</#if>/>
</#macro>

<#macro renderIgnoredField></#macro>

<#macro renderFieldTitle style title id fieldHelpText="" for="" >

  <label <#if style?has_content && style?index_of("listDisplay")!=-1>style="width: 100%;margin-left: -13px;"</#if> <#if for?has_content>for="${for}"</#if> <#if fieldHelpText?has_content> title="${fieldHelpText}"</#if> class="col-md-2 control-label"<#if id?has_content> id="${id}"</#if>><#t/>
    ${title}<#t/>
  </label><#t/>
</#macro>

<#macro renderSingleFormFieldTitle style title id fieldHelpText="" for="" >
  <label <#if for?has_content>for="${for}"</#if> <#if fieldHelpText?has_content> title="${fieldHelpText}"</#if> class="col-sm-2 control-label no-padding-right"<#if id?has_content> id="${id}"</#if>><#t/>
    ${title}<#t/>
  </label><#t/>
</#macro>

<#macro renderFormOpen linkUrl formType targetWindow containerId containerStyle autocomplete name viewIndexField viewSizeField viewIndex viewSize useRowSubmit>
  <form method="post" role="form" action="${linkUrl}"<#if formType=="upload"> enctype="multipart/form-data"</#if><#if targetWindow?has_content> target="${targetWindow}"</#if><#if containerId?has_content> id="${containerId}"</#if> class=<#if containerStyle?has_content>"${containerStyle}"<#else>"form-horizontal"</#if> onsubmit="javascript:submitFormDisableSubmits(this)"<#if autocomplete?has_content> autocomplete="${autocomplete}"</#if> name="${name}"><#lt/>
  	<div style='margin-bottom: 25px;'></div>
    <#if useRowSubmit?has_content && useRowSubmit>
      <input type="hidden" name="_useRowSubmit" value="Y"/>
      <#if linkUrl?index_of("VIEW_INDEX") &lt;= 0 && linkUrl?index_of(viewIndexField) &lt;= 0>
        <input type="hidden" name="${viewIndexField}" value="${viewIndex}"/>
      </#if>
      <#if linkUrl?index_of("VIEW_SIZE") &lt;= 0 && linkUrl?index_of(viewSizeField) &lt;= 0>
        <input type="hidden" name="${viewSizeField}" value="${viewSize}"/>
      </#if>
    </#if>
</#macro>
<#macro renderFormClose focusFieldName formName containerId hasRequiredField>
  </form><#lt/>
  <#if focusFieldName?has_content>
    <script language="JavaScript" type="text/javascript">
      var form = document.${formName};
      form.${focusFieldName}.focus();
      <#-- enable the validation plugin for all generated forms
      only enable the validation if min one field is marked as 'required' -->
      if (jQuery(form).find(".required").size() > 0) {
        jQuery(form).validate();
      }
    </script><#lt/>
  </#if>
  <#if containerId?has_content && hasRequiredField?has_content>
    <script type="text/javascript">
      jQuery("#${containerId}").validate({
        submitHandler:
          function(form) {
            form.submit();
          }
      });
    </script>
  </#if>
</#macro>
<#macro renderMultiFormClose>
  </form><#lt/>
</#macro>

<#macro renderFormatListWrapperOpen formName style columnStyles>
<div  style="margin: -13px -13px 0;">
  <table cellspacing="0" class="<#if style?has_content>${style}<#else>table table-striped table-bordered table-hover dataTable no-footer</#if>">
</#macro>

<#macro renderFormatListWrapperClose formName>
  </table>
</div>
</#macro>

<#macro renderFormatHeaderRowOpen style>
  <thead>
  <tr <#if style?has_content>class="${style}"</#if> role="row" style="border-collapse:inherit">
</#macro>
<#macro renderFormatHeaderRowClose>
  </tr>
  </thead>
</#macro>
<#macro renderFormatHeaderRowCellOpen style positionSpan>
  <td <#if positionSpan?has_content && positionSpan gt 1 >colspan="${positionSpan}"</#if><#if style?has_content>class="${style}"</#if>>
</#macro>
<#macro renderFormatHeaderRowCellClose>
  </td>
</#macro>

<#macro renderFormatHeaderRowFormCellOpen style>
  <td <#if style?has_content>class="${style}"</#if>>
</#macro>
<#macro renderFormatHeaderRowFormCellClose>
  </td>
</#macro>
<#macro renderFormatHeaderRowFormCellTitleSeparator style isLast>
  <#if style?has_content><span class="${style}"></#if> - <#if style?has_content></span></#if>
</#macro>

<#macro renderFormatItemRowOpen formName itemIndex altRowStyles evenRowStyle oddRowStyle>
  <tr <#if itemIndex?has_content><#if itemIndex%2==0><#if evenRowStyle?has_content>class="${evenRowStyle}<#if altRowStyles?has_content> ${altRowStyles}</#if>"<#elseif altRowStyles?has_content>class="${altRowStyles}"</#if><#else><#if oddRowStyle?has_content>class="${oddRowStyle}<#if altRowStyles?has_content> ${altRowStyles}</#if>"<#elseif altRowStyles?has_content>class="${altRowStyles}"</#if></#if></#if> >
</#macro>
<#macro renderFormatItemRowClose formName>
  </tr>
</#macro>
<#macro renderFormatItemRowCellOpen fieldName style positionSpan>
  <td <#if positionSpan?has_content && positionSpan gt 1>colspan="${positionSpan}"</#if><#if style?has_content>class="${style}"</#if>>
</#macro>
<#macro renderFormatItemRowCellClose fieldName>
  </td>
</#macro>
<#macro renderFormatItemRowFormCellOpen style>
  <td<#if style?has_content> class="${style}"</#if>>
</#macro>
<#macro renderFormatItemRowFormCellClose>
  </td>
</#macro>

<#macro renderFormatSingleWrapperOpen formName style>
  <#-- <table cellspacing="0" <#if style?has_content>class="${style}"</#if>> -->
</#macro>
<#macro renderFormatSingleWrapperClose formName>
  <#--  </table> -->
</#macro>

<#macro renderFormatFieldRowOpen>
	<div class="form-group">
</#macro>
<#macro renderFormatFieldRowClose>
  </div>
</#macro>
<#macro renderFormatFieldRowTitleCellOpen style>
	<!-- <div class="col-sm-2"> -->
</#macro>
<#macro renderFormatFieldRowTitleCellClose>
	<!-- </div> -->
</#macro>
<#macro renderFormatFieldRowSpacerCell></#macro>
<#macro renderFormatFieldRowWidgetCellOpen positionSpan style>
	<#-- <div class="col-sm-10 col-lg-5<#if style?has_content> ${style}</#if>"> -->

    <#if style?has_content && style?index_of("submit") != -1 >
      <label class="col-md-2 control-label" id="addFacility_facilityId_title">&nbsp;<#-- 这个是为了给提交按钮前面有一段空格 --></label>
    </#if>
	<div class="col-sm-4">


</#macro>
<#macro renderFormatFieldRowWidgetCellClose>
	<#-- </div> -->
    </div>
</#macro>

<#--
    Initial work to convert table based layout for "single" form to divs.
<#macro renderFormatSingleWrapperOpen style> <div <#if style?has_content>class="${style}"</#if> ></#macro>
<#macro renderFormatSingleWrapperClose> </div></#macro>

<#macro renderFormatFieldRowOpen>  <div></#macro>
<#macro renderFormatFieldRowClose>  </div></#macro>
<#macro renderFormatFieldRowTitleCellOpen style>   <div class="<#if style?has_content>${style}<#else>label</#if>"></#macro>
<#macro renderFormatFieldRowTitleCellClose></div></#macro>
<#macro renderFormatFieldRowSpacerCell></#macro>
<#macro renderFormatFieldRowWidgetCellOpen positionSpan style>   <div<#if positionSpan?has_content && positionSpan gt 0> colspan="${1+positionSpan*3}"</#if><#if style?has_content> class="${style}"</#if>></#macro>
<#macro renderFormatFieldRowWidgetCellClose></div></#macro>

-->


<#macro renderFormatEmptySpace><#-- &nbsp; --></#macro>

<#macro renderTextFindField name value defaultOption opEquals opBeginsWith opContains opIsEmpty opNotEqual className alert size maxlength autocomplete titleStyle hideIgnoreCase ignCase ignoreCase>
    <#assign fieldclass = className + " form-control">
    <div class="row">
      <div class="col-sm-12">
          <div class="input-group">
              <div class="input-group-btn">
                  <select <#if name?has_content>name="${name}_op"</#if> class="btn btn-default" style="height: 32px;-webkit-appearance: none;">
                      <option value="equals"<#if defaultOption=="equals"> selected="selected"</#if>>${opEquals}</option><#rt/>
                      <option value="like"<#if defaultOption=="like"> selected="selected"</#if>>${opBeginsWith}</option><#rt/>
                      <option value="contains"<#if defaultOption=="contains"> selected="selected"</#if>>${opContains}</option><#rt/>
                      <option value="empty"<#rt/><#if defaultOption=="empty"> selected="selected"</#if>>${opIsEmpty}</option><#rt/>
                      <option value="notEqual"<#if defaultOption=="notEqual"> selected="selected"</#if>>${opNotEqual}</option><#rt/>
                  </select>
              </div>
              <input type="text" <@renderClass fieldclass alert /> name="${name}"<#if value?has_content> value="${value}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if><#if autocomplete?has_content> autocomplete="off"</#if>/><#rt/>
          </div>
      </div>
    </div>

    <#if titleStyle?has_content><span class="${titleStyle}" ><#rt/></#if>
      <input type="hidden" name="${name}_ic" value="Y"/><#rt/>
    <#if titleStyle?has_content></span>
    </#if>
</#macro>

<#macro renderTextFindOldField name value defaultOption opEquals opBeginsWith opContains opIsEmpty opNotEqual className alert size maxlength autocomplete titleStyle hideIgnoreCase ignCase ignoreCase>
  <#if opEquals?has_content>
    <select <#if name?has_content>name="${name}_op"</#if>    class="selectBox"><#rt/>
      <option value="equals"<#if defaultOption=="equals"> selected="selected"</#if>>${opEquals}</option><#rt/>
      <option value="like"<#if defaultOption=="like"> selected="selected"</#if>>${opBeginsWith}</option><#rt/>
      <option value="contains"<#if defaultOption=="contains"> selected="selected"</#if>>${opContains}</option><#rt/>
      <option value="empty"<#rt/><#if defaultOption=="empty"> selected="selected"</#if>>${opIsEmpty}</option><#rt/>
      <option value="notEqual"<#if defaultOption=="notEqual"> selected="selected"</#if>>${opNotEqual}</option><#rt/>
    </select>
  <#else>
    	<input type="hidden" name=<#if name?has_content> "${name}_op"</#if>    value="${defaultOption}"/><#rt/>
  </#if>
    	<input type="text" <@renderClass className alert /> name="${name}"<#if value?has_content> value="${value}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if><#if autocomplete?has_content> autocomplete="off"</#if>/><#rt/>
    <#if titleStyle?has_content><span class="${titleStyle}" ><#rt/></#if>
    <#if hideIgnoreCase>
      <input type="hidden" name="${name}_ic" value=<#if ignCase>"Y"<#else> ""</#if>/><#rt/>
    <#else>
      		<input type="checkbox" name="${name}_ic" value="Y" <#if ignCase> checked="checked"</#if> /> ${ignoreCase}<#rt/>
    </#if>
    <#if titleStyle?has_content></span>
  </#if>
</#macro>

<#macro renderDateFindField className alert name localizedInputTitle value value2 size maxlength dateType formName defaultDateTimeString imgSrc localizedIconTitle titleStyle defaultOptionFrom defaultOptionThru opEquals opSameDay opGreaterThanFromDayStart opGreaterThan opGreaterThan opLessThan opUpToDay opUpThruDay opIsEmpty>

<div class="input-daterange input-group">
    <div class="form-group">
        <div class="input-group" style="margin: 0px 13px 0px 13px;">
            <input type="text" <#if name?has_content> name="${(name)!}_fld0_value"</#if> placeholder="开始时间" value="${(value)!}" class="datepicker" data-dateformat="yy/mm/dd" style="
              width: 100%;
              height: 32px;
              padding: 6px 12px;
              border: 1px solid #ccc;
              float: left;">
            <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                <span class="input-group-addon">
                    <i class="fa fa-exchange"></i>
                </span>
            <input class="datepicker" <#if name?has_content> name="${name}_fld1_value"</#if> type="text" value="${(value2)!}" placeholder="结束时间" style="width: 100%;
              height: 32px;
              padding: 6px 12px;
              border: 1px solid #ccc;
              float: left;"  data-dateformat="yy/mm/dd">
            <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
        </div>
    </div>
    <input type="hidden" <#if name?has_content> name="${name}_fld0_op"</#if> value="greaterThan"/>
    <input type="hidden" <#if name?has_content> name="${name}_fld1_op"</#if> value="opLessThan"/>
</div>
	<#rt/>
</#macro>

<#macro renderDateFindOldField className alert name localizedInputTitle value size maxlength dateType formName defaultDateTimeString imgSrc localizedIconTitle titleStyle defaultOptionFrom defaultOptionThru opEquals opSameDay opGreaterThanFromDayStart opGreaterThan opGreaterThan opLessThan opUpToDay opUpThruDay opIsEmpty>
  <span class="view-calendar">
    <input id="${name?html}_fld0_value" type="text" <@renderClass className alert /><#if name?has_content> name="${name?html}_fld0_value"</#if><#if localizedInputTitle?has_content> title="${localizedInputTitle}"</#if><#if value?has_content> value="${value}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if>/><#rt/>
    <#if dateType != "time">
      <script type="text/javascript">
        <#if dateType == "date">
          jQuery("#${name?html}_fld0_value").datepicker({
        <#else>
          jQuery("#${name?html}_fld0_value").datetimepicker({
            showSecond: true,
            <#-- showMillisec: true, -->
            timeFormat: 'HH:mm:ss',
            stepHour: 1,
            stepMinute: 5,
            stepSecond: 10,
        </#if>
            showOn: 'button',
            buttonImage: '',
            buttonText: '',
            buttonImageOnly: false,
            dateFormat: 'yy-mm-dd'
          });
      </script>
      <#rt/>
    </#if>
    <#if titleStyle?has_content>
      <span class="${titleStyle}"><#rt/>
    </#if>
    <select<#if name?has_content> name="${name}_fld0_op"</#if> class="selectBox"><#rt/>
      <option value="equals"<#if defaultOptionFrom=="equals"> selected="selected"</#if>>${opEquals}</option><#rt/>
      <option value="sameDay"<#if defaultOptionFrom=="sameDay"> selected="selected"</#if>>${opSameDay}</option><#rt/>
      <option value="greaterThanFromDayStart"<#if defaultOptionFrom=="greaterThanFromDayStart"> selected="selected"</#if>>${opGreaterThanFromDayStart}</option><#rt/>
      <option value="greaterThan"<#if defaultOptionFrom=="greaterThan"> selected="selected"</#if>>${opGreaterThan}</option><#rt/>
    </select><#rt/>
    <#if titleStyle?has_content>
      </span><#rt/>
    </#if>
    <#rt/>
    <input id="${name?html}_fld1_value" type="text" <@renderClass className alert /><#if name?has_content> name="${name}_fld1_value"</#if><#if localizedInputTitle??> title="${localizedInputTitle?html}"</#if><#if value2?has_content> value="${value2}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if>/><#rt/>
    <#if dateType != "time">
      <script type="text/javascript">
        <#if dateType == "date">
          jQuery("#${name?html}_fld1_value").datepicker({
        <#else>
          jQuery("#${name?html}_fld1_value").datetimepicker({
            showSecond: true,
            <#-- showMillisec: true, -->
            timeFormat: 'HH:mm:ss',
            stepHour: 1,
            stepMinute: 5,
            stepSecond: 10,
        </#if>
            showOn: 'button',
            buttonImage: '',
            buttonText: '',
            buttonImageOnly: false,
            dateFormat: 'yy-mm-dd'
          });
      </script>
      <#rt/>
    </#if>
    <#if titleStyle?has_content>
      <span class="${titleStyle}"><#rt/>
    </#if>
    <select name=<#if name?has_content>"${name}_fld1_op"</#if> class="selectBox"><#rt/>
      <option value="opLessThan"<#if defaultOptionThru=="opLessThan"> selected="selected"</#if>>${opLessThan}</option><#rt/>
      <option value="upToDay"<#if defaultOptionThru=="upToDay"> selected="selected"</#if>>${opUpToDay}</option><#rt/>
      <option value="upThruDay"<#if defaultOptionThru=="upThruDay"> selected="selected"</#if>>${opUpThruDay}</option><#rt/>
      <option value="empty"<#if defaultOptionFrom=="empty"> selected="selected"</#if>>${opIsEmpty}</option><#rt/>
    </select><#rt/>
    <#if titleStyle?has_content>
      </span>
    </#if>
  </span>
	<script type="text/javascript">
	    jQuery(document).ready(function(){
	    	jQuery('.ui-datepicker-trigger').removeClass('ui-datepicker-trigger').addClass('btn btn-primary btn-sm').html('<span class="glyphicon glyphicon-calendar"></span>');
	    });
	</script>
</#macro>

<#macro renderRangeFindField className alert name value size maxlength autocomplete titleStyle defaultOptionFrom opEquals opGreaterThan opGreaterThanEquals opLessThan opLessThanEquals value2 defaultOptionThru>
  <input type="text" <@renderClass className alert /> <#if name?has_content>name="${name}_fld0_value"</#if><#if value?has_content> value="${value}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if><#if autocomplete?has_content> autocomplete="off"</#if>/><#rt/>
  <#if titleStyle?has_content>
    <span class="${titleStyle}" ><#rt/>
  </#if>
  <select <#if name?has_content>name="${name}_fld0_op"</#if> class="selectBox"><#rt/>
    <option value="equals"<#if defaultOptionFrom=="equals"> selected="selected"</#if>>${opEquals}</option><#rt/>
    <option value="greaterThan"<#if defaultOptionFrom=="greaterThan"> selected="selected"</#if>>${opGreaterThan}</option><#rt/>
    <option value="greaterThanEqualTo"<#if defaultOptionFrom=="greaterThanEqualTo"> selected="selected"</#if>>${opGreaterThanEquals}</option><#rt/>
  </select><#rt/>
  <#if titleStyle?has_content>
    </span><#rt/>
  </#if>
  <br /><#rt/>
  <input type="text" <@renderClass className alert /><#if name?has_content> name="${name}_fld1_value"</#if><#if value2?has_content> value="${value2}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if><#if autocomplete?has_content> autocomplete="off"</#if>/><#rt/>
  <#if titleStyle?has_content>
    <span class="${titleStyle}" ><#rt/>
  </#if>
  <select name=<#if name?has_content>"${name}_fld1_op"</#if> class="selectBox"><#rt/>
    <option value="lessThan"<#if defaultOptionThru=="lessThan"> selected="selected"</#if>>${opLessThan?html}</option><#rt/>
    <option value="lessThanEqualTo"<#if defaultOptionThru=="lessThanEqualTo"> selected="selected"</#if>>${opLessThanEquals?html}</option><#rt/>
  </select><#rt/>
  <#if titleStyle?has_content>
    </span>
  </#if>
</#macro>

<#--
@renderLookupField

Description: Renders a text input field as a lookup field.

Parameter: name, String, required - The name of the lookup field.
Parameter: formName, String, required - The name of the form that contains the lookup field.
Parameter: fieldFormName, String, required - Contains the lookup window form name.
Parameter: className, String, optional - The CSS class name for the lookup field.
Parameter: alert, String, optional - If "true" then the "alert" CSS class will be added to the lookup field.
Parameter: value, Object, optional - The value of the lookup field.
Parameter: size, String, optional - The size of the lookup field.
Parameter: maxlength, String or Integer, optional - The max length of the lookup field.
Parameter: id, String, optional - The ID of the lookup field.
Parameter: event, String, optional - The lookup field event that invokes "action". If the event parameter is not empty, then the action parameter must be specified as well.
Parameter: action, String, optional - The action that is invoked on "event". If action parameter is not empty, then the event parameter must be specified as well.
Parameter: readonly, boolean, optional - If true, the lookup field is made read-only.
Parameter: autocomplete, String, optional - If not empty, autocomplete is turned off for the lookup field.
Parameter: descriptionFieldName, String, optional - If not empty and the presentation parameter contains "window", specifies an alternate input field for updating.
Parameter: targetParameterIter, List, optional - Contains a list of form field names whose values will be passed to the lookup window.
Parameter: imgSrc, Not used.
Parameter: ajaxUrl, String, optional - Contains the Ajax URL, used only when the ajaxEnabled parameter contains true.
Parameter: ajaxEnabled, boolean, optional - If true, invokes the Ajax auto-completer.
Parameter: presentation, String, optional - Contains the lookup window type, either "layer" or "window".
Parameter: width, String or Integer, optional - The width of the lookup field.
Parameter: height, String or Integer, optional - The height of the lookup field.
Parameter: position, String, optional - The position style of the lookup field.
Parameter: fadeBackground, ?
Parameter: clearText, String, optional - If the readonly parameter is true, clearText contains the text to be displayed in the field, default is CommonClear label.
Parameter: showDescription, String, optional - If the showDescription parameter is true, a special span with css class "tooltip" will be created at right of the lookup button and a description will fill in (see setLookDescription in selectall.js). For now not when the lookup is read only.
Parameter: initiallyCollapsed, Not used.
Parameter: lastViewName, String, optional - If the ajaxEnabled parameter is true, the contents of lastViewName will be appended to the Ajax URL.
-->
<#macro renderLookupField name formName fieldFormName className="" alert="false" value="" size="" maxlength="" id="" event="" action="" readonly=false autocomplete="" descriptionFieldName="" targetParameterIter="" imgSrc="" ajaxUrl="" ajaxEnabled=javaScriptEnabled presentation="layer" width="" height="" position="" fadeBackground="true" clearText="" showDescription="" initiallyCollapsed="" lastViewName="main" >
  <#if Static["org.ofbiz.widget.ModelWidget"].widgetBoundaryCommentsEnabled(context)>
  <!-- @renderLookupField -->
  </#if>
  <#assign fieldclass = className + " col-lg-7">
  <#if (!ajaxUrl?has_content) && ajaxEnabled?has_content && ajaxEnabled>
    <#local ajaxUrl = requestAttributes._REQUEST_HANDLER_.makeLink(request, response, fieldFormName)/>
    <#local ajaxUrl = id + "," + ajaxUrl + ",ajaxLookup=Y" />
  </#if>
  <#if (!showDescription?has_content)>
    <#local showDescriptionProp = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.lookup.showDescription", "N")>
    <#if "Y" == showDescriptionProp>
      <#local showDescription = "true" />
    <#else>
      <#local showDescription = "false" />
    </#if>
  </#if>
  <#if ajaxEnabled?has_content && ajaxEnabled>
    <script type="text/javascript">
      jQuery(document).ready(function(){
        if (!jQuery('form[name="${formName}"]').length) {
          alert("Developer: for lookups to work you must provide a form name!")
        }
      });
    </script>
  </#if>
  <span class="field-lookup">
    <#if size?has_content && size=="0">
      <input type="hidden" <#if name?has_content> name="${name}"/></#if>
    <#else>
        <div class="form-group">
            <div class="input-group" style="margin: 0px 13px 0px 13px;">
                <input  type="text" <#if className?has_content && className?index_of("required") != -1>required="true";</#if><#if id?has_content> id="${id}"</#if>  <#if name?has_content> name="${name}"</#if> value="${(value)!}" style="
                        width: 100%;
                        height: 32px;
                        padding: 6px 12px;
                        border: 1px solid #ccc;
                        float: left;
                  <#if className?has_content && className?index_of("required") != -1>
                          background-color: #eeeeee;
                  </#if>
                        ">
                <div class="input-group-addon" <#if id?has_content> id="${id}_button"</#if> onclick="showDiv(this);"><i class="fa fa-search"></i></div>
            </div>
        </div>
    </#if>
    <#if presentation?has_content && descriptionFieldName?has_content && presentation == "window">
      <a href="javascript:call_fieldlookup3(document.${formName?html}.${name?html},document.${formName?html}.${descriptionFieldName},'${fieldFormName}', '${presentation}'<#rt/>
      <#if targetParameterIter?has_content>
        <#list targetParameterIter as item>
          ,document.${formName}.${item}.value<#rt>
        </#list>
      </#if>
      );"></a><#rt>
    <#elseif presentation?has_content && presentation == "window">
      <a href="javascript:call_fieldlookup2(document.${formName?html}.${name?html},'${fieldFormName}', '${presentation}'<#rt/>
      <#if targetParameterIter?has_content>
        <#list targetParameterIter as item>
          ,document.${formName}.${item}.value<#rt>
        </#list>
      </#if>
      );"></a><#rt>
    <#else>
      <#if ajaxEnabled?has_content && ajaxEnabled>
        <#assign defaultMinLength = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.autocompleter.defaultMinLength")>
        <#assign defaultDelay = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("widget.properties", "widget.autocompleter.defaultDelay")>
        <#local ajaxUrl = ajaxUrl + "&amp;_LAST_VIEW_NAME_=" + lastViewName />
        <#if !ajaxUrl?contains("searchValueFieldName=")>
          <#if descriptionFieldName?has_content && showDescription == "true">
            <#local ajaxUrl = ajaxUrl + "&amp;searchValueFieldName=" + descriptionFieldName />
          <#else>
            <#local ajaxUrl = ajaxUrl + "&amp;searchValueFieldName=" + name />
          </#if>
        </#if>
      </#if>
      <script type="text/javascript">
        jQuery(document).ready(function(){
          var options = {
            requestUrl : "${fieldFormName}",
            inputFieldId : "${id}",
            dialogTarget : document.${formName?html}.${name?html},
            dialogOptionalTarget : <#if descriptionFieldName?has_content>document.${formName?html}.${descriptionFieldName}<#else>null</#if>,
            formName : "${formName?html}",
            width : "${width}",
            height : "${height}",
            position : "${position}",
            modal : "${fadeBackground}",
            ajaxUrl : <#if ajaxEnabled?has_content && ajaxEnabled>"${ajaxUrl}"<#else>""</#if>,
            showDescription : <#if ajaxEnabled?has_content && ajaxEnabled>"${showDescription}"<#else>false</#if>,
            presentation : "${presentation!}",
            defaultMinLength : "${defaultMinLength!2}",
            defaultDelay : "${defaultDelay!300}",
            args :
              <#rt/>
                <#if targetParameterIter?has_content>
                  <#assign isFirst = true>
                  <#lt/>[<#rt/>
                  <#list targetParameterIter as item>
                    <#if isFirst>
                      <#lt/>document.${formName}.${item}<#rt/>
                      <#assign isFirst = false>
                    <#else>
                      <#lt/> ,document.${formName}.${item}<#rt/>
                    </#if>
                  </#list>
                  <#lt/>]<#rt/>
                <#else>[]
                </#if>
                <#lt/>
          };
          new Lookup(options);
        });
      </script>
    </#if>
    <#if readonly?has_content && readonly>
      <a id="${id}_clear" 
        style="background:none;margin-left:5px;margin-right:15px;" 
        class="clearField" 
        href="javascript:void(0);" 
        onclick="javascript:document.${formName}.${name}.value='';
          jQuery('#' + jQuery('#${id}_clear').next().attr('id').replace('_button','') + '_${id}_lookupDescription').html('');
          <#if descriptionFieldName?has_content>document.${formName}.${descriptionFieldName}.value='';</#if>">
          <#if clearText?has_content>${clearText}<#else>${uiLabelMap.CommonClear}</#if>
      </a>
    </#if>
  </span>
  <#if ajaxEnabled?has_content && ajaxEnabled && (presentation?has_content && presentation == "window")>
    <#if ajaxUrl?index_of("_LAST_VIEW_NAME_") < 0>
      <#local ajaxUrl = ajaxUrl + "&amp;_LAST_VIEW_NAME_=" + lastViewName />
    </#if>
    <script language="JavaScript" type="text/javascript">ajaxAutoCompleter('${ajaxUrl}', ${showDescription}, ${defaultMinLength!2}, ${defaultDelay!300});</script><#t/>
  </#if>
</#macro>

<#macro renderNextPrev paginateStyle paginateFirstStyle viewIndex highIndex listSize viewSize ajaxEnabled javaScriptEnabled ajaxFirstUrl firstUrl paginateFirstLabel paginatePreviousStyle ajaxPreviousUrl previousUrl paginatePreviousLabel pageLabel ajaxSelectUrl selectUrl ajaxSelectSizeUrl selectSizeUrl commonDisplaying paginateNextStyle ajaxNextUrl nextUrl paginateNextLabel paginateLastStyle ajaxLastUrl lastUrl paginateLastLabel paginateViewSizeLabel>
	<#if listSize gt viewSize>
    <div  style="margin: -13px -13px 0;">
      <div class="dt-toolbar">
          <div class="col-xs-12 col-sm-6">
              <#if listSize gt 0 && javaScriptEnabled>
                  <d style="padding-top: 9px;font-size: 13px;font-weight: 700;font-style: italic;color: #969696;">${(pageLabel)!}</d>
                  <select name="page" size="1" onchange="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxSelectUrl}')<#else>submitPagination(this, '${selectUrl}'+this.value)</#if>" style="width: auto;height: 32px;font: inherit;-webkit-border-radius: 0!important;background-color: #fff;border: 1px solid #ccc;"><#rt/>
                    <#assign x=(listSize/viewSize)?ceiling>
                    <#list 1..x as i>
                      <#if i == (viewIndex+1)><option selected="selected" value="<#else><option value="</#if>${i-1}">${i}</option>
                    </#list>
                  </select>
              </#if>
              <#if javaScriptEnabled>
                  <select name="pageSize" size="1" onchange="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxSelectSizeUrl}')<#else>submitPagination(this, '${selectSizeUrl}')</#if>" style="width: auto;height: 32px;font: inherit;-webkit-border-radius: 0!important;background-color: #fff;border: 1px solid #ccc;"><#rt/>
                    <#assign availPageSizes = [20, 30, 50, 100, 200]>
                    <#list availPageSizes as ps>
                        <option <#if viewSize == ps> selected="selected" </#if> value="${ps}">${ps}</option>
                    </#list>
                  </select>
                  <d style="padding-top: 9px;font-size: 13px;font-weight: 700;font-style: italic;color: #969696;">${paginateViewSizeLabel}</d>
              </#if>
              <d style="padding-top: 9px;font-size: 13px;font-weight: 700;font-style: italic;color: #969696;">${commonDisplaying}</d>
          </div>
          <div class="col-sm-6 col-xs-12 hidden-xs">
              <div class="dataTables_paginate paging_bootstrap">
                  <ul class="pagination">
                      <li class="prev <#if viewIndex lte 0>disabled</#if>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxFirstUrl}')<#else>submitPagination(this, '${firstUrl}')</#if>"><i class="fa fa-angle-double-left"></i></a></li>
                      <li class="prev <#if viewIndex lte 0>disabled</#if>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxPreviousUrl}')<#else>submitPagination(this, '${previousUrl}')</#if>"><i class="fa fa-angle-left"></i></a></li>
                      <li class="next <#if highIndex gte listSize>disabled</#if>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxNextUrl}')<#else>submitPagination(this, '${nextUrl}')</#if>"><i class="fa fa-angle-right"></i></a></li>
                      <li class="next <#if highIndex gte listSize>disabled</#if>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxLastUrl}')<#else>submitPagination(this, '${lastUrl}')</#if>"><i class="fa fa-angle-double-right"></i></a></li>
                  </ul>
              </div>
          </div>
      </div>
    </div>
	</#if>
</#macro>
<#macro renderNextPrevOld paginateStyle paginateFirstStyle viewIndex highIndex listSize viewSize ajaxEnabled javaScriptEnabled ajaxFirstUrl firstUrl paginateFirstLabel paginatePreviousStyle ajaxPreviousUrl previousUrl paginatePreviousLabel pageLabel ajaxSelectUrl selectUrl ajaxSelectSizeUrl selectSizeUrl commonDisplaying paginateNextStyle ajaxNextUrl nextUrl paginateNextLabel paginateLastStyle ajaxLastUrl lastUrl paginateLastLabel paginateViewSizeLabel>
  <#if listSize gt viewSize>
    <#-- <div class="${paginateStyle}">&nbsp; -->
      <nav>
      <ul class="pagination pull-left">
        <li class="${paginateFirstStyle}<#if viewIndex gt 0>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxFirstUrl}')<#else>submitPagination(this, '${firstUrl}')</#if>" title="${paginateFirstLabel}">${paginateFirstLabel}</a><#else>-disabled"><span class="glyphicon glyphicon-step-backward"></span></#if></li>
        <li class="${paginatePreviousStyle}<#if viewIndex gt 0>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxPreviousUrl}')<#else>submitPagination(this, '${previousUrl}')</#if>" title="${paginatePreviousLabel}">${paginatePreviousLabel}</a><#else>-disabled"><span class="glyphicon glyphicon-backward"></span></#if></li>
        <li class="${paginateNextStyle}<#if highIndex lt listSize>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxNextUrl}')<#else>submitPagination(this, '${nextUrl}')</#if>" title="${paginateNextLabel}"><span class="glyphicon glyphicon-forward"></a><#else>-disabled"><span class="glyphicon glyphicon-forward"></span></#if></li>
        <li class="${paginateLastStyle}<#if highIndex lt listSize>"><a href="javascript:void(0)" onclick="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxLastUrl}')<#else>submitPagination(this, '${lastUrl}')</#if>" title="${paginateLastLabel}"><span class="glyphicon glyphicon-step-forward"></span></a><#else>-disabled"><span class="glyphicon glyphicon-step-forward"></span></#if></li>
		</ul>
		<ul class="pagination pull-right">
        <#if listSize gt 0 && javaScriptEnabled><li class="nav-page-select">${pageLabel} <select style="margin:0px;font-size:100%;" name="page" size="1" onchange="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxSelectUrl}')<#else>submitPagination(this, '${selectUrl}'+this.value)</#if>"><#rt/>
          <#assign x=(listSize/viewSize)?ceiling>
            <#list 1..x as i>
              <#if i == (viewIndex+1)><option selected="selected" value="<#else><option value="</#if>${i-1}">${i}</option>
            </#list>
          </select></li>
        </#if>
        <#if javaScriptEnabled><li class="nav-pagesize"><select name="pageSize" size="1" onchange="<#if ajaxEnabled>ajaxUpdateAreas('${ajaxSelectSizeUrl}')<#else>submitPagination(this, '${selectSizeUrl}')</#if>"><#rt/>
            <#assign availPageSizes = [20, 30, 50, 100, 200]>
          <#list availPageSizes as ps>
            <option <#if viewSize == ps> selected="selected" </#if> value="${ps}">${ps}</option>
          </#list>
          </select> ${paginateViewSizeLabel}</li>
        </#if>
        <li class="nav-displaying">${commonDisplaying}</li>
      </ul>
      </nav>
    <#-- </div> -->
  </#if>
</#macro>

<#macro renderFileField className alert name value size maxlength autocomplete>
  <input type="file" class="btn btn-default" <#if className?has_content && className?index_of("required") != -1>required="true"; style="background-color: #eeeeee;" </#if> <#assign fieldclass = className?replace("required","") + " form-control"> <@renderClass fieldclass alert /><#if name?has_content> name="${name}"</#if><#if value?has_content> value="${value}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if><#if autocomplete?has_content> autocomplete="off"</#if>/><#rt/>
</#macro>
<#macro renderPasswordField className alert name value size maxlength id autocomplete>
  <input type="password" <#if className?has_content && className?index_of("required") != -1>required="true" style="background-color: #eeeeee;" </#if> <#assign fieldclass = className?replace("required","") + " form-control"> <@renderClass fieldclass alert /><#if name?has_content> name="${name}"</#if><#if value?has_content> value="${value}"</#if><#if size?has_content> size="${size}"</#if><#if maxlength?has_content> maxlength="${maxlength}"</#if><#if id?has_content> id="${id}"</#if><#if autocomplete?has_content> autocomplete="off"</#if> />
</#macro>
<#macro renderImageField value description alternate style event action><img<#if value?has_content> src="${value}"</#if><#if description?has_content> title="${description}"</#if> alt="<#if alternate?has_content>${alternate}"</#if><#if style?has_content> class="${style}"</#if><#if event?has_content> ${event?html}="${action}" </#if>/></#macro>

<#macro renderBanner style leftStyle rightStyle leftText text rightText>
  <table width="100%">
    <tr><#rt/>
      <#if leftText?has_content><td align="left"><#if leftStyle?has_content><div class="${leftStyle}"></#if>${leftText}<#if leftStyle?has_content></div></#if></td><#rt/></#if>
      <#if text?has_content><td align="center"><#if style?has_content><div class="${style}"></#if>${text}<#if style?has_content></div></#if></td><#rt/></#if>
      <#if rightText?has_content><td align="right"><#if rightStyle?has_content><div class="${rightStyle}"></#if>${rightText}<#if rightStyle?has_content></div></#if></td><#rt/></#if>
    </tr>
  </table>
</#macro>

<#macro renderContainerField id className><div id="${id}" class="${className}"/></#macro>

<#macro renderFieldGroupOpen style id title collapsed collapsibleAreaId collapsible expandToolTip collapseToolTip>
  <#if style?has_content || id?has_content || title?has_content><div class="fieldgroup<#if style?has_content> ${style}</#if>"<#if id?has_content> id="${id}"</#if>>
    <div class="fieldgroup-title-bar">
      <#if collapsible>
        <ul>
          <li class="<#if collapsed>collapsed">
                      <a onclick="javascript:toggleCollapsiblePanel(this, '${collapsibleAreaId}', '${expandToolTip}', '${collapseToolTip}');">
                    <#else>expanded">
                      <a onclick="javascript:toggleCollapsiblePanel(this, '${collapsibleAreaId}', '${expandToolTip}', '${collapseToolTip}');">
                    </#if>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<#if title?has_content>${title}</#if></a>
          </li>
        </ul>
      <#else>
        <#if title?has_content>${title}</#if>
      </#if><#rt/>
    </div>
    <div id="${collapsibleAreaId}" class="fieldgroup-body" <#if collapsed && collapsible> style="display: none;"</#if>>
  </#if>
</#macro>

<#macro renderFieldGroupClose style id title><#if style?has_content || id?has_content || title?has_content></div></div></#if></#macro>

<#macro renderHyperlinkTitle name title showSelectAll="N">
  <#if title?has_content>${title}<br /></#if>
  <#if showSelectAll="Y"><input type="checkbox" name="selectAll" value="Y" onclick="javascript:toggleAll(this, '${name}');"/></#if>
</#macro>

<#macro renderSortField style title linkUrl ajaxEnabled tooltip="">
  <a<#if style?has_content> class="${style}"</#if> href="<#if ajaxEnabled?has_content && ajaxEnabled>javascript:ajaxUpdateAreas('${linkUrl}')<#else>${linkUrl}</#if>"<#if tooltip?has_content> title="${tooltip}"</#if>>${title}</a>
</#macro>

<#macro formatBoundaryComment boundaryType widgetType widgetName><!-- ${boundaryType}  ${widgetType}  ${widgetName} --></#macro>

<#--
<#macro renderTooltip tooltip tooltipStyle>
  <#if tooltip?has_content><span class="<#if tooltipStyle?has_content>${tooltipStyle}<#else>tooltip</#if>">${tooltip}</span><#rt/></#if>
</#macro>
-->
<#macro renderTooltip tooltip tooltipStyle>
  <#if tooltip?has_content>
	
	<p class="note">
		<strong>Note:</strong>
		${tooltip}
	</p>
	
  </#if>
</#macro>

<#macro renderClass className="" alert="">
  <#if className?has_content || (alert?has_content && alert=="true")> class="${className}<#if alert?has_content && alert=="true"> alert</#if>" </#if>
</#macro>

<#macro renderAsterisks requiredField requiredStyle>
  <#-- <#if requiredField=="true"><#if !requiredStyle?has_content>*</#if></#if> -->
</#macro>

<#macro makeHiddenFormLinkForm actionUrl name parameters targetWindow>
  <form method="post" action="${actionUrl}" <#if targetWindow?has_content>target="${targetWindow}"</#if> onsubmit="javascript:submitFormDisableSubmits(this)" name="${name}">
    <#list parameters as parameter>
      <input name="${parameter.name}" value="${parameter.value}" type="hidden"/>
    </#list>
  </form>
</#macro>
<#macro makeHiddenFormLinkAnchor linkStyle hiddenFormName event action imgSrc description confirmation>
  <a <#if linkStyle?has_content>class="${linkStyle}"</#if> href="javascript:document.${hiddenFormName}.submit()"
    <#if action?has_content && event?has_content> ${event}="${action}"</#if>
    <#if confirmation?has_content> onclick="return confirm('${confirmation?js_string}')"</#if>>
      <#if imgSrc?has_content><img src="${imgSrc}" alt=""/></#if>${description}</a>
</#macro>
<#macro makeHyperlinkString linkStyle hiddenFormName event action imgSrc title alternate linkUrl targetWindow description confirmation>
  <#if linkStyle?has_content && linkStyle?index_of("link")!=-1>
          <a <#if linkStyle?has_content>class="btn btn-labeled btn-default ${linkStyle}"</#if>
             href="${linkUrl}"<#if targetWindow?has_content> target="${targetWindow}"</#if>
    <#if action?has_content && event?has_content> ${event}="${action}"</#if>
    <#if confirmation?has_content> onclick="return confirm('${confirmation?js_string}')"</#if>
    <#if imgSrc?length == 0 && title?has_content> title="${title}"</#if>>
    <#if imgSrc?has_content><img src="${imgSrc}" alt="${alternate}" title="${title}"/></#if>
    <#if linkStyle?has_content && linkStyle?index_of("link")!=-1>
    <span class="btn-label"><i class="glyphicon glyphicon-chevron-right"></i></span>
    </#if>
  ${description}
  </a>
  <#elseif linkStyle?has_content && linkStyle?index_of("button")!=-1>
          <a <#if linkStyle?has_content>class="btn btn-primary btn-sm${linkStyle}"</#if>
             href="${linkUrl}"<#if targetWindow?has_content> target="${targetWindow}"</#if>
    <#if action?has_content && event?has_content> ${event}="${action}"</#if>
    <#if confirmation?has_content> onclick="return confirm('${confirmation?js_string}')"</#if>
    <#if imgSrc?length == 0 && title?has_content> title="${title}"</#if>>
    <#if imgSrc?has_content><img src="${imgSrc}" alt="${alternate}" title="${title}"/></#if>
  ${description}
  </a>
  <#else>
          <a <#if linkStyle?has_content>class="${linkStyle}"</#if>
             href="${linkUrl}"<#if targetWindow?has_content> target="${targetWindow}"</#if>
    <#if action?has_content && event?has_content> ${event}="${action}"</#if>
    <#if confirmation?has_content> onclick="return confirm('${confirmation?js_string}')"</#if>
    <#if imgSrc?length == 0 && title?has_content> title="${title}"</#if>>
    <#if imgSrc?has_content><img src="${imgSrc}" alt="${alternate}" title="${title}"/></#if>
  ${description}
  </a>
  </#if>
  </#macro>
