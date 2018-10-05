<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        ${msg("updatePasswordTitle")}
    <#elseif section = "header">
        ${msg("updatePasswordTitle")}
    <#elseif section = "form">
        <div class="usa-alert usa-alert-info">
          <div class="usa-alert-body">
            <div class="usa-alert-text">
              <p>Your new password must:</p>
              <ul>
                <li data-validator="username">Not be the same as your username</li>
                <li data-validator="same">Not be the same as any of your last ten passwords</li>
              </ul>
              <p>Passwords must also:</p>
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
              </ul>
              
            </div>
          </div>
        </div>
        <form id="kc-passwd-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <input type="text" readonly value="this is not a login form" style="display: none;">
            <input type="password" readonly value="this is not a login form" style="display: none;">

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="password-new" class="${properties.kcLabelClass!}">${msg("passwordNew")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="password" id="password-new" name="password-new" class="${properties.kcInputClass!}" autofocus autocomplete="off" />
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="password-confirm" class="${properties.kcLabelClass!}">${msg("passwordConfirm")}</label>
                </div>

                <span class="usa-input-error-message" id="password-confirm-error-message" role="alert">Passwords do not match</span>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="password" id="password-confirm" name="password-confirm" class="${properties.kcInputClass!}" autocomplete="off" />
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
                </div>
            </div>
        </form>
        <script src="${url.resourcesPath}/js/passwordRules.js"></script>
        <script>
          $(document).ready(initRules)
        </script>
    </#if>
</@layout.registrationLayout>
