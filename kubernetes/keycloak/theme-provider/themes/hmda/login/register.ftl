<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
  <#if section = "title">
    ${msg("registerWithTitle",(realm.displayName!''))}
  <#elseif section = "header">
    ${msg("registerWithTitleHtml",(realm.displayNameHtml!''))}
  <#elseif section = "form">
    <form id="kc-register-form" class="usa-form" action="${url.registrationAction}" method="post">
      <fieldset>
        <input type="text" readonly value="this is not a login form" style="display: none;">
        <input type="password" readonly value="this is not a login form" style="display: none;">
        <legend class="usa-drop_text">Create a test account</legend>
        <span>or <a href="${url.loginUrl}">go back to login</a></span>
        <div class="usa-alert usa-alert-warning">
          <div class="usa-alert-body">
            <p class="usa-alert-text">Any test account created during the beta period will be removed from the system when the filing period opens in January 2019.</p>
          </div>
        </div>
        <#if !realm.registrationEmailAsUsername>
          <label for="username" class="${properties.kcLabelClass!}">${msg("username")}</label>
          <input type="text" id="username" class="${properties.kcInputClass!}" name="username" value="${(register.formData.username!'')}" />
        </#if>

        <label for="firstName">${msg("firstName")}</label>
        <input type="text" id="firstName" class="${properties.kcInputClass!}" name="firstName" value="${(register.formData.firstName!'')}" autofocus />

        <label for="lastName">${msg("lastName")}</label>
        <input type="text" id="lastName" name="lastName" value="${(register.formData.lastName!'')}" />

        <label for="email">${msg("email")}</label>
        <span class="usa-form-hint">The provided email address will be used to notify you of any HMDA related technology updates.</span>
        <input type="text" id="email" name="email" value="${(register.formData.email!'')}" />

        <div id="institutions"></div>

        <input id="user.attributes.institutions" name="user.attributes.institutions" class="usa-skipnav" hidden style="display:none;"/>

        <div class="usa-alert usa-alert-info">
          <div class="usa-alert-body">
            <div class="usa-alert-text">
              <p>Passwords must:</p>
              <ul id="validation_list">
                <li data-validator="length">
                  <img class="check" src="${url.resourcesPath}/img/correct9.svg">
                  <img class="missing" src="${url.resourcesPath}/img/close.svg">
                  Be at least 12 characters
                </li>
                <li data-validator="uppercase">
                  <img class="check" src="${url.resourcesPath}/img/correct9.svg">
                  <img class="missing" src="${url.resourcesPath}/img/close.svg">
                  Have at least 1 uppercase character
                </li>
                <li data-validator="lowercase">
                  <img class="check" src="${url.resourcesPath}/img/correct9.svg">
                  <img class="missing" src="${url.resourcesPath}/img/close.svg">
                  Have at least 1 lowercase character
                </li>
                <li data-validator="numerical">
                  <img class="check" src="${url.resourcesPath}/img/correct9.svg">
                  <img class="missing" src="${url.resourcesPath}/img/close.svg">
                  Have at least 1 numerical character
                </li>
                <li data-validator="special">
                  <img class="check" src="${url.resourcesPath}/img/correct9.svg">
                  <img class="missing" src="${url.resourcesPath}/img/close.svg">
                  Have at least 1 special character
                </li>
                <li data-validator="username">
                  <img class="check" src="${url.resourcesPath}/img/correct9.svg">
                  <img class="missing" src="${url.resourcesPath}/img/close.svg">
                  Not be the same as your username
                </li>
              </ul>
            </div>
          </div>
        </div>

        <#if passwordRequired>
          <label for="password">${msg("password")}</label>
          <input type="password" id="password" name="password" />

          <label for="password-confirm">${msg("passwordConfirm")}</label>
          <span class="usa-input-error-message" id="password-confirm-error-message" role="alert">Passwords do not match</span>
          <input type="password" id="password-confirm" name="password-confirm" />
        </#if>

        <#if recaptchaRequired??>
          <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
        </#if>

        <input name="register" id="kc-register" type="submit" value="${msg("doRegister")}"/>

        <div id="submit-loader" class="LoadingIconWrapper">
          <div class="LoadingIcon"></div>
        </div>
      </fieldset>

      <p class="usa-text-small">Having trouble? Please contact <a href="mailto:${properties.supportEmailTo!}?subject=${properties.supportEmailSubject!}">${properties.supportEmailTo!}</a></p>
    </form>
  </#if>
</@layout.registrationLayout>
<script>
var HMDA = {}
HMDA.institutionSearchUri = "${properties.institutionSearchUri!}/institutions"
HMDA.supportEmailTo = "${properties.supportEmailTo!}"
HMDA.supportEmailSubject = "${properties.supportEmailSubject!}"
HMDA.enterEmailMessage = "${msg("hmdaEnterEmailAddress", (properties.supportEmailTo!''))}"
HMDA.resources = "${url.resourcesPath}"
</script>
<script src="${url.resourcesPath}/js/passwordRules.js"></script>
<script src="${url.resourcesPath}/js/register.js"></script>
