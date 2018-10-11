<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("emailForgotTitle")}
    <#elseif section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">
        <form id="kc-reset-password-form" class="usa-form" action="${url.loginAction}" method="post">
          <fieldset>
            <legend class="usa-drop_text">Reset password</legend>
            <span>or <a href="${url.loginUrl}">go back to login</a></span>

            <p>${msg("emailInstruction")}</p>

            <label for="username"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
            <input type="text" id="username" name="username" autofocus/>

            <input name="reset" id="kc-reset" type="submit" value="${msg("doSubmit")}"/>
          </fieldset>
        </form>
    </#if>
</@layout.registrationLayout>
