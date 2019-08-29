---
layout: default
title: "HMDA Platform API"
class: home
---

<hgroup>
  <h1>The HMDA Platform API <code>v1</code></h1>
  <p class="usa-font-lead">The HMDA Platform API V1 is <strong><abbr title="Cross-Origin Resource Sharing">CORS</abbr></strong> enabled and requires <strong>no authentication</strong>. The API helps to prepare and test your HMDA data by allowing you to calculate rate spread, check a single LAR for formatting errors, generate/validate a check digit, and more.</p>
</hgroup>

---

<h3>The API URL</h3>
<p>All API endpoints in this documentation are available at the following base URL.</p>
{% highlight bash %}
https://ffiec-api.cfpb.gov/public
{% endhighlight %}

<p>For example, if you wanted to look up an institution by ID you would use the following.</p>

{% highlight bash %}
POST https://ffiec-api.cfpb.gov/public/uli/checkDigit
{% endhighlight %}
<p class="usa-text-small">For more details see the <a href="{{ "/check-digit#generate" | relative_url }}">check digit documentation</a>.</p>
