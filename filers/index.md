---
layout: default
title: "HMDA Platform API - Filers"
---

<hgroup>
  <h1>Filers</h1>
  <p class="usa-font-lead">The <code>/filers</code> endpoint allows access to the full list of institutions who filed HMDA data.</p>
</hgroup>

---

<h4>Allowed Methods</h4>
<code>GET</code>

<h4>Example</h4>
{% highlight bash %}
curl https://ffiec-api.cfpb.gov/public/filers
{% endhighlight %}

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "institutions": [
    {
      "institutionId": "0",
      "name": "bank-0 National Association",
      "period": "2017",
      "respondentId": "Bank0_RID"
    },
    {
      "institutionId": "1",
      "name": "Bank 1",
      "period": "2016",
      "respondentId": "Bank1_RID"
    }
  ]
}
{% endhighlight %}
</section>

---

<hgroup>
  <h1 id="by-filing-period">By filling period</h1>
  <p class="usa-font-lead">The <code>/filers/:filingPeriod</code> endpoint allows access to the full list of institutions who filed HMDA data in a given filing period.</p>
</hgroup>

<h4>Allowed Methods</h4>
<code>GET</code>

<h4>Example</h4>
{% highlight bash %}
curl https://ffiec-api.cfpb.gov/public/filers/2017
{% endhighlight %}

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "institutions": [
    {
      "institutionId": "0",
      "name": "bank-0 National Association",
      "period": "2017",
      "respondentId": "Bank0_RID"
    }
  ]
}
{% endhighlight %}
</section>

---
