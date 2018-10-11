<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "title">
        ${msg("errorTitle")}
    <#elseif section = "header">
        ${msg("errorTitleHtml")}
    <#elseif section = "form">
        <div id="kc-error-message">
            <p class="instruction">${message.summary}</p>
            <p><a class="usa-button" id="backToApplication" href="${properties.filingAppUrl}/institutions">${msg("doLogIn")}</a></p>
        </div>
    </#if>
</@layout.registrationLayout>
