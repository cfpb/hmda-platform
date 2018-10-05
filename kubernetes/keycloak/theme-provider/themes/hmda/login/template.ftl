<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" class="${properties.kcHtmlClass!}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title><#nested "title"></title>
    <link rel="icon" href="${url.resourcesPath}/img/favicons/favicon.ico" />
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <!-- Google Tag Manager -->
    <script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
    new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
    j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
    'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
    })(window,document,'script','dataLayer','GTM-KDGB99D');</script>
    <!-- End Google Tag Manager -->
</head>

<body>
  <!-- Google Tag Manager (noscript) -->
  <noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-KDGB99D"
    height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
  <!-- End Google Tag Manager (noscript) -->
  <header class="usa-header usa-header-basic" role="banner">
    <div class="usa-banner">
      <header class="usa-banner-header">
        <div class="usa-grid usa-banner-inner">
          <img src="${properties.filingAppUrl!}/../img/favicons/favicon-57.png" alt="U.S. flag" />
          <p>An official website of the United States government</p>
        </div>
      </header>
    </div>
    <div class="usa-nav-container">
      <div class="usa-logo" id="logo">
        <em class="usa-logo-text">
          <a class="usa-nav-link" href="${properties.filingAppUrl!}">
              <img src="${properties.filingAppUrl!}../img/ffiec-logo.svg" height="32px" alt="FFIEC"/>HMDA Filing Platform
          </a>
        </em>
      </div>
      <nav role="navigation" class="Header usa-nav">
        <ul class="usa-nav-primary">
          <li>
            <a class="HomeLink usa-nav-link" href="${properties.filingAppUrl!}">Filing Home</a>
          </li>
        </ul>
      </nav>
    </div>
  </header>
  <#if realm.internationalizationEnabled>
    <div class="usa-grid">
      <div id="kc-locale" class="${properties.kcLocaleClass!}">
        <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
          <div class="kc-dropdown" id="kc-locale-dropdown">
            <a href="#" id="kc-current-locale-link">${locale.current}</a>
            <ul>
              <#list locale.supported as l>
                <li class="kc-dropdown-item"><a href="${l.url}">${l.label}</a></li>
              </#list>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </#if>

  <#assign className="usa-grid usa-grid-small"/>
  <#if (displayMessage && message?has_content && message.summary == "verifyEmailMessage")>
    <#assign className="usa-grid verifyEmailMessage"/>
  </#if>

  <div class="${className}">
    <div id="kc-content">
      <div id="kc-content-wrapper">
        <#if displayMessage && message?has_content && message.summary != "verifyEmailMessage">
          <div class="usa-width-one-whole margin-bottom-1">
            <div class="usa-alert usa-alert-${message.type}">
              <div class="usa-alert-body">
                <p class="usa-alert-text">${message.summary}</p>
                <p>For help with account-related issues, please contact
                    <strong><a href="mailto:${properties.supportEmailTo!}?subject=${properties.supportEmailSubject?url('UTF-8')}">${properties.supportEmailTo}</a></strong>.
                </p>
              </div>
            </div>
          </div>
        </#if>

        <div class="usa-width-one-whole">
            <#nested "form">
        </div>

        <#if displayInfo>
          <div class="usa-width-one-whole">
            <#nested "info">
          </div>
        </#if>
      </div>
    </div>
  </div>

  <footer class="usa-footer usa-footer-slim" role="contentinfo">
    <div class="usa-grid usa-footer-return-to-top">
      <a href="#">Return to top</a>
    </div>
    <div class="usa-footer-primary-section">
      <div class="usa-grid-full">
        <nav class="usa-footer-nav usa-width-one-half">
          <ul class="usa-unstyled-list">
            <li class="usa-footer-primary-content">
              <a class="usa-nav-link" href="${properties.filingAppUrl!}">
                <img src="${properties.filingAppUrl!}../img/ffiec-logo.svg" height="21px" alt="FFIEC"/>Home Mortgage Disclosure Act
              </a>
            </li>
          </ul>
        </nav>
        <div class="usa-width-one-half">
          <div class="usa-footer-primary-content">
            <a href="mailto:${properties.supportEmailTo!}?subject=${properties.supportEmailSubject?url('UTF-8')}">Questions?</a>
          </div>
        </div>
      </div>
    </div>
  </footer>
</body>
</html>
</#macro>
