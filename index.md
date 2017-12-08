---
layout: default
title: "HMDA Platform API"
class: home
---

<hgroup>
  <h1>The HMDA Platform API <code>v1</code></h1>
  <h2>Help to prepare and test your HMDA data</h2>
</hgroup>

<p class="usa-font-lead">The HMDA Platform API is <strong><abbr title="Cross-Origin Resource Sharing">CORS</abbr></strong> enabled and requires <strong>no authentication</strong>. The API makes it easy to check a single LAR for formatting errors, generate/validate a check digit, calculate rate spread, and more.</p>

<p>Current status: <span class="usa-label usa-label-success">Up</span></p>

---

<h4>Important information</h4>

<h5>The API URL</h5>
<p>All API endpoints in this documentation are available at the following base URL.</p>
<pre><code class="language-bash">https://ffiec-api.cfpb.gov/public</code></pre>
<p>For example, if you wanted to generate a check digit for your LEI and Loan/Application ID you would use the following.</p>
<pre class="margin-bottom-0"><code class="language-bash">POST https://ffiec-api.cfpb.gov/public/uli/checkDigit</code></pre>
<p class="usa-text-small">For more details see the <a href="{{ "/check-digit#generate" | relative_url }}">check digit documentation</a>.</p>