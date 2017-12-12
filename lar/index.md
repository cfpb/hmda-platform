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
  <p class="usa-font-lead">Check whether a single LAR parses.</p>
  <p>Using the <code>/lar/parse</code> endpoint you can check whether or not a single LAR contains formatting errors.</p>
</hgroup>

<h4>Example</h4>
{% highlight PowerShell %}
POST https://ffiec-api.cfpb.gov/public/lar/parse
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

<h4>Example Body</h4>
{% highlight PowerShell %}
2|0|1|10164|20170224|1|1|3|1|21|3|1|20170326|45460|18|153|0501.00|2|2|5| | | | |5| | | | |1|2|31|0| | | |NA   |2|1
{% endhighlight %}

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "respondentId": "0",
  "applicant": {
    "coSex": 2,
    "coRace5": "",
    "coEthnicity": 2,
    "race2": "",
    "coRace2": "",
    "coRace1": 5,
    "race4": "",
    "race3": "",
    "race1": 5,
    "sex": 1,
    "coRace3": "",
    "income": "31",
    "coRace4": "",
    "ethnicity": 2,
    "race5": ""
  },
  "hoepaStatus": 2,
  "agencyCode": 1,
  "actionTakenType": 1,
  "denial": {
    "reason1": "",
    "reason2": "",
    "reason3": ""
  },
  "rateSpread": "NA",
  "loan": {
    "applicationDate": "20170224",
    "propertyType": 1,
    "amount": 21,
    "purpose": 3,
    "id": "10164",
    "occupancy": 1,
    "loanType": 1
  },
  "id": 2,
  "actionTakenDate": 20170326,
  "geography": {
    "msa": "45460",
    "state": "18",
    "county": "153",
    "tract": "0501.00"
  },
  "lienStatus": 1,
  "preapprovals": 3,
  "purchaserType": 0
}
{% endhighlight %}
</section>

<h4>Example Error Response (failed to parse)</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "lineNumber": 0,
  "errorMessages": [
    "An incorrect number of data fields were reported: 38 data fields were found, when 39 data fields were expected."
  ]
}
{% endhighlight %}
</section>

---

<hgroup>
  <h3 id="validate">Validation</h3>
  <p class="usa-font-lead">Check whether a single LAR passes validation.</p>
  <p>Using the <code>/lar/validate</code> endpoint you can test whether or not a single LAR passes edit checks.</p>
</hgroup>

<div class="usa-alert usa-alert-info">
  <div class="usa-alert-body">
    <h3 class="usa-alert-heading">Note!</h3>
    <p class="usa-alert-text">This endpoint omits certain edits that are not relevant to a single LAR. Edits that are omitted: macro edits, TS-only edits (e.g. Q130), and the following: Q022, S025, S270.</p>
  </div>
</div>

<h4>Example</h4>
{% highlight PowerShell %}
POST https://ffiec-api.cfpb.gov/public/lar/validate?check=syntactical
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

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
      <td><code>check</code></td>
      <td><code>string</code></td>
      <td><code>syntactical</code>, <code>validity</code>, or <code>quality</code>. (If left blank, or any other text is entered, defaults to run all checks.)</td>
    </tr>
  </tbody>
</table>

<h4>Example Body</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "respondentId": "0",
  "applicant": {
    "coSex": 2,
    "coRace5": "",
    "coEthnicity": 2,
    "race2": "",
    "coRace2": "",
    "coRace1": 5,
    "race4": "",
    "race3": "",
    "race1": 5,
    "sex": 1,
    "coRace3": "",
    "income": "31",
    "coRace4": "",
    "ethnicity": 2,
    "race5": ""
  },
  "hoepaStatus": 2,
  "agencyCode": 1,
  "actionTakenType": 1,
  "denial": {
    "reason1": "",
    "reason2": "",
    "reason3": ""
  },
  "rateSpread": "NA",
  "loan": {
    "applicationDate": "20170224",
    "propertyType": 1,
    "amount": 21,
    "purpose": 3,
    "id": "10164",
    "occupancy": 1,
    "loanType": 1
  },
  "id": 2,
  "actionTakenDate": 20170326,
  "geography": {
    "msa": "45460",
    "state": "18",
    "county": "153",
    "tract": "0501.00"
  },
  "lienStatus": 1,
  "preapprovals": 3,
  "purchaserType": 0
}
{% endhighlight %}
</section>

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "syntactical": {
    "errors": []
  },
  "validity": {
    "errors": []
  },
  "quality": {
    "errors": []
  }
}
{% endhighlight %}
</section>

---

<hgroup>
  <h3 id="parseAndValidate">Parse and Validate</h3>
  <p class="usa-font-lead">Check whether a single LAR parses and passes validation.</p>
  <p>Using the <code>/lar/parseAndValidate</code> endpoint you can test whether a single LAR is formatted correctly AND wheter or not it passes many of the edits.</p>
</hgroup>

<div class="usa-alert usa-alert-info">
  <div class="usa-alert-body">
    <h3 class="usa-alert-heading">Note!</h3>
    <p class="usa-alert-text">This endpoint omits certain edits that are not relevant to a single LAR. Edits that are omitted: macro edits, TS-only edits (e.g. Q130), and the following: Q022, S025, S270.</p>
  </div>
</div>

<h4>Example</h4>
{% highlight PowerShell %}
POST https://ffiec-api.cfpb.gov/public/lar/parseAndValidate?check=syntactical
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

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
      <td><code>check</code></td>
      <td><code>string</code></td>
      <td><code>syntactical</code>, <code>validity</code>, or <code>quality</code>. (If left blank, or any other text is entered, defaults to run all checks.)</td>
    </tr>
  </tbody>
</table>

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "syntactical": {
    "errors": []
  },
  "validity": {
    "errors": []
  },
  "quality": {
    "errors": []
  }
}
{% endhighlight %}
</section>