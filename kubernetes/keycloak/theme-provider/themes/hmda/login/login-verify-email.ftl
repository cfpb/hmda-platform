<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("emailVerifyTitle")}
    <#elseif section = "header">
        ${msg("emailVerifyTitle")}
    <#elseif section = "form">
      <div class="usa-width-one-whole margin-bottom-1">
        <div class="usa-alert usa-alert-${message.type}">
          <div class="usa-alert-body">
            <h3 style="margin-top: 0;">Verify your email to activate your account</h3>
            <p>You have been sent an email with a verification code.</p>
            <p>This code will expire in 60 minutes. If you would like a new code or if you haven't received the email, <strong><a style="text-transform: lowercase;" href="${url.loginAction}">${msg("doClickHere")}</a></strong> to send a new one.</p>
          </div>
        </div>

        <p>For help with account-related issues, please contact
          <strong><a href="mailto:${properties.supportEmailTo!}?subject=${properties.supportEmailSubject?url('UTF-8')}">${properties.supportEmailTo}</a></strong>.
        </p>
    </#if>
</@layout.registrationLayout>
