---
layout: default
title: "HMDA Platform API - Institutions"
---

<hgroup>
  <h1>Institutions</h1>
  <h2>Information about institutions and their filings</h2>
  <p class="usa-font-lead">The <code>/institutions</code> endpoint allows access to data about HMDA institutions and their filing data.</p>
</hgroup>

---

<hgroup>
  <h3 id="search-by-domain">Search by email domain</h3>
  <p class="usa-font-lead">Query the list of known institutions on the HMDA Platform.</p>
  <p>Using the <code>/institutions</code> endpoint you can provide an email domain and get a response containing a list of insitutions that use that email domain.</p>
</hgroup>

<h4>Allowed Methods</h4>
<code>GET</code>

<h4>Example</h4>
{% highlight bash %}
curl https://ffiec-api.cfpb.gov/public/institutions?domain=bank0.com
{% endhighlight %}

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
  <h3 id="search-by-id">Search by institution id</h3>
  <p class="usa-font-lead">Get details about an institution.</p>
  <p>Using the <code>/institutions/:institutionId</code> endpoint you can provide an institution id and get the details of an institution.</p>
</hgroup>

<div class="usa-alert usa-alert-info">
  <div class="usa-alert-body">
    <h3 class="usa-alert-heading">Note!</h3>
    <p class="usa-alert-text">The insitution id is the <abbr title="Replication Server System Database ID">RSSD ID</abbr> or, for non-depository institutions, the Federal Tax ID.</p>
  </div>
</div>

<h4>Allowed Methods</h4>
<code>GET</code>

<h4>Example</h4>
{% highlight bash %}
curl https://ffiec-api.cfpb.gov/public/institutions/:institutionId
{% endhighlight %}

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
    <td>An institutions <abbr title="Replication Server System Database ID">RSSD ID</abbr> or,<br />for non-depository institutions, the Federal Tax ID.</td>
  </tr>
</tbody>
</table>

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "id": "123",
  "agency": "CFPB",
  "activityYear": "2017",
  "institutionType": "bank",
  "cra": false,
  "externalIds": [{
    "id": "bank-id",
    "idType": "fdic-certificate-number"
  }],
  "emailDomains": [
    "email1",
    "email2"
  ],
  "respondent": {
    "externalId": {
      "id": "bank-id",
      "idType": "fdic-certificate-number"
    },
    "name": "bank 0",
    "state": "VA",
    "city": "City Name",
    "fipsStateNumber": "2"
  },
  "hmdaFilerFlag": true,
  "parent": {
    "respondentId": "12-3",
    "idRssd": 3,
    "name": "parent name",
    "city": "parent city",
    "state": "VA"
  },
  "assets": 123,
  "otherLenderCode": 0,
  "topHolder": {
    "idRssd": 4,
    "name": "top holder name",
    "city": "top holder city",
    "state": "VA",
    "country": "USA"
  }
}
{% endhighlight %}
</section>

---
