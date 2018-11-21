<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))}
    <#elseif section = "form">
        <#if realm.password>
            <form id="kc-form-login" class="usa-form" action="${url.loginAction}" method="post">
                <fieldset>
                    <legend class="usa-drop_text">Sign in</legend>
                    <span>or <a href="${url.registrationUrl}">create an account</a></span>

                    <label for="username"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
                    <#if usernameEditDisabled??>
                        <input tabindex="1" id="username" name="username" type="text" autocapitalize="off" autocorrect="off" value="${(login.username!'')}" disabled>
                    <#else>
                        <input tabindex="1" id="username" name="username" value="${(login.username!'')}" type="text" autofocus />
                    </#if>

                    <label for="password">${msg("password")}</label>
                    <input tabindex="2" id="password" name="password" type="password" autocomplete="off" />

                    <input tabindex="4" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                    <div id="submit-loader" class="LoadingIconWrapper">
                      <div class="LoadingIcon"></div>
                    </div>

                    <#if realm.resetPasswordAllowed>
                        <p><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></p>
                    </#if>
                </fieldset>
                <p class="usa-text-small">Having trouble? Please contact <a href="mailto:${properties.supportEmailTo!}?subject=${properties.supportEmailSubject!}">${properties.supportEmailTo!}</a></p>
            </form>
        </#if>
    </#if>
</@layout.registrationLayout>
<script src="${url.resourcesPath}/js/login.js"></script>
