---
layout: default
title: "HMDA Platform API - Institutions"
---

<h1>Institutions</h1>
<hgroup>
<h2 id="search">Search</h2>
<h3>Query the list of known institutions on the HMDA Platform.</h3>
<h4>Using the <code>/institutions</code> endpoint you can provide email domain and get a response containing a list of insitutions that use that email domain.</h4>
</hgroup>
<h5>Allowed Methods</h5>
<code>GET</code>
<h5>Parameters</h5>
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
<h5>Example</h5>
{% highlight PowerShell %}
GET /institutions?domain=bank0.com
{% endhighlight %}
<h5>Example Response</h5>
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
<hgroup>
<h2 id="modified-lar">Modified LAR</h2>
<h3>Return a modified LAR, in CSV format for a given institution and filing period.</h3>
<h4>Using the <code>/institutions</code> endpoint you can provide an institution id and filing period (year) and get the modified LAR.</h4>
</hgroup>
<h5>Allowed Methods</h5>
<code>GET</code>
<h5>Parameters</h5>
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
  <td>The filing period.</td>
</tr>
</tbody>
</table>
<h5>Example</h5>
{% highlight PowerShell %}
GET /institutions/:institutionId/filings/:period/lar
{% endhighlight %}
<h5>Example Response</h5>
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