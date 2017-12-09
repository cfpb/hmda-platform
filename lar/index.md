---
layout: default
title: "HMDA Platform API - LAR"
---

<hgroup>
  <h1>LAR</h1>
  <h2>Test a single LAR for parsing and validation</h2>
  <p class="usa-font-lead">The <code>/lar</code> endpoint allows testing of a single LAR row. It can used to test for formatting (parsing), edit validation, or both.</p>
</hgroup>

---

<hgroup>
  <h3 id="parse">Parse</h3>
  <p class="usa-font-lead">Query the list of known institutions on the HMDA Platform.</p>
  <p>Using the <code>/institutions</code> endpoint you can provide email domain and get a response containing a list of insitutions that use that email domain.</p>
</hgroup>

<h4>Example</h4>
{% highlight PowerShell %}
GET /institutions?domain=bank0.com
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>GET</code>

<h4>Parameters</h4>
<p class="usa-text-small">Passed in as a query string.</p>
<table>
<thead>
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>Description</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td><code>domain</code></td>
    <td><code>string</code></td>
    <td>Any email domain.</td>
  </tr>
</tbody>
</table>

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "institutions": [
    {
      "id": 0,
      "name": false,
      "domains": ["test@bank0.com"],
      "externalIds": [
        {
          "value": "1234",
          "name": "occ-charter-id"
        },
        {
          "value": "1234",
          "name": "ncua-charter-id"
        }
      ]
    }
  ]
}
{% endhighlight %}
</section>

---

<hgroup>
  <h3 id="modified-lar">Modified LAR</h3>
  <p class="usa-font-lead">Return a modified LAR, in CSV format for a given institution and filing period.</p>
  <p>Using the <code>/institutions</code> endpoint you can provide an institution id and filing period (year) and get the modified LAR.</p>
</hgroup>

<div class="usa-alert usa-alert-info">
  <div class="usa-alert-body">
    <h3 class="usa-alert-heading">Note!</h3>
    <p class="usa-alert-text">The modified LAR may not be available until 30 days after the filing period.</p>
  </div>
</div>

<h4>Example</h4>
{% highlight PowerShell %}
GET /institutions/:institutionId/filings/:period/lar
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>GET</code>

<h4>Parameters</h4>
<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>institutionId</code></td>
      <td><code>string</code></td>
      <td>The institution id.</td>
    </tr>
    <tr>
      <td><code>period</code></td>
      <td><code>string</code></td>
      <td>The filing period, eg <code>2017</code>.</td>
    </tr>
  </tbody>
</table>

<h4>Example Response</h4>
<p>The response is in <code>CSV</code> format. The schema is as follows:
{% highlight PowerShell %}
id
respondent_id
agency_code
preapprovals
action_taken_type
purchaser_type
rate_spread
hoepa_status
lien_status
loan_type
property_type
purpose
occupancy
amount
msa
state
county
tract
ethnicity
co_ethnicity
race1
race2
race3
race4
race5
co_race1
co_race2
co_race3
co_race4
co_race5
sex
co_sex
income
denial_reason1
denial_reason2
denial_reason3
period
{% endhighlight %}
</p>
<p class="usa-text-small">For a definition of these fields, please consult the <a href="http://www.consumerfinance.gov/data-research/hmda/static/for-filers/2017/2017-HMDA-FIG.pdf" title="HMDA Filing Instructions Guide">HMDA Filing Instructions Guide</a>.</p>
<p class="usa-text-small">Please note that the Modified LAR does not include the following fields listed in the HMDA Filing Instructions Guide:
<ul>
<li><code>Loan Application Number</code></li>
<li><code>Date Application Received</code></li>
<li><code>Date of Action</code></li>
</ul>
</p>