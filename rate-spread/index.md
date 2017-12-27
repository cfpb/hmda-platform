---
layout: default
title: "HMDA Platform API - Rate Spread"
---

<hgroup>
  <h1>Rate Spread</h1>
  <h2>Calculate the rate spread.</h2>
  <p class="usa-font-lead">The <code>/rateSpread</code> endpoint you can provide information about a loan and have the rate spread calculated.</p>
</hgroup>

---

<h4>Example</h4>
{% highlight bash %}
curl https://ffiec-api.cfpb.gov/public/rateSpread \
  -d '{ 
    "actionTakenType": 1,
    "amortizationType": 30,
    "rateType": "FixedRate",
    "apr": 6.0,
    "lockinDate": "2017-11-20",
    "reverseMortgage": 2
  }' \
  -X POST \
  -H "Content-Type: application/json"
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

<h4>Valid values</h4>
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
      <td><code>actionTakenType</code></td>
      <td><code>integer</code></td>
      <td><code>1</code>, <code>2</code>, or <code>8</code></td>
    </tr>
    <tr>
      <td><code>amortizationType</code></td>
      <td><code>integer</code></td>
      <td>Range from <code>1</code> to <code>50</code> years</td>
    </tr>
    <tr>
      <td><code>rateType</code></td>
      <td><code>string</code></td>
      <td><code>FixedRate</code> or <code>VariableRate</code></td>
    </tr>
    <tr>
      <td><code>apr</code></td>
      <td><code>double</code></td>
      <td>The Annual Percentage Rate on the loan, eg <code>6.0</code></td>
    </tr>
    <tr>
      <td><code>reverseMortgage</code></td>
      <td><code>integer</code></td>
      <td><code>1</code> or <code>2</code></td>
    </tr>
  </tbody>
</table>

<h4>Example payload</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "actionTakenType": 1,
  "amortizationType": 30,
  "rateType": "FixedRate",
  "apr": 6.0,
  "lockinDate": "2017-11-20",
  "reverseMortgage": 2
}
{% endhighlight %}
</section>

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "rateSpread": "2.01"
}
{% endhighlight %}
</section>
<p class="use-text-small">The reponse is either a number representing the rate spread or "NA".</p>

---

<hgroup>
  <h3 id="batch">Batch</h3>
</hgroup>

<h4>Example</h4>
{% highlight bash %}
curl https://ffiec-api.cfpb.gov/public/rateSpread/csv \
  -X POST \
  -F file=@yourFile.txt
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

<h4>Example file contents in <code>CSV</code> format</h4>
<section class="code-block">
<code>CSV</code>
{% highlight bash %}
1,30,FixedRate,6.0,2017-11-20,2
1,30,VariableRate,6.0,2017-11-20,2
{% endhighlight %}
</section>

<p class="use-text-small">The contents of this file include the <code>Action Taken Type</code>, <code>Amortization Term</code>, <code>Rate Type</code>, <code>APR</code>, <code>Lockin Date</code> and <code>Reverse Mortgage</code>.</p>

<h4>Example Response in <code>CSV</code> format</h4>
<section class="code-block">
<code>CSV</code>
{% highlight bash %}
action_taken_type,amortization_type,rate_type,apr,lockin_date,reverse_mortgage,rate_spread
1,30,FixedRate,6.0,2017-11-20,2,2.01
1,30,VariableRate,6.0,2017-11-20,2,2.15
{% endhighlight %}
</section>

<p class="use-text-small">The response appends the calculate <code>rate_spread</code> to each row of the orginal <code>CSV</code>.</p>