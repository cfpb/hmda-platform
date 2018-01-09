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
    "loanTerm": 30,
    "amortizationType": "FixedRate",
    "apr": 6.0,
    "lockInDate": "2017-11-20",
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
      <td>
        <ul class="usa-unstyled-list">
          <li><code>1</code> = Originated</li>
          <li><code>2</code> = Approved Not Accepted</li>
          <li><code>8</code> = Pre-approval request approved but not Accepted</li>
          <li class="na-response"><code>3</code>, <code>4</code>, <code>5</code>, <code>6</code> or <code>7</code> will result in <code>NA</code></li>
        </ul>
      </td>
    </tr>
    <tr>
      <td><code>loanTerm</code></td>
      <td><code>integer</code></td>
      <td>Range from <code>1</code> to <code>50</code> years</td>
    </tr>
    <tr>
      <td><code>amortizationType</code></td>
      <td><code>string</code></td>
      <td><code>FixedRate</code> or <code>VariableRate</code></td>
    </tr>
    <tr>
      <td><code>apr</code></td>
      <td><code>double</code></td>
      <td>The Annual Percentage Rate on the loan, eg <code>6.0</code></td>
    </tr>
    <tr>
      <td><code>lockInDate</code></td>
      <td><code>date</code></td>
      <td><code>yyyy-mm-dd</code></td>
    </tr>
    <tr>
      <td><code>reverseMortgage</code></td>
      <td><code>integer</code></td>
      <td>
        <ul class="usa-unstyled-list">
          <li><code>2</code> = false</li>
          <li class="na-response"><code>1</code> = true, will result in <code>NA</code></li>
        </ul>
      </td>
    </tr>
  </tbody>
</table>

<h4>Example payload</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{ 
  "actionTakenType": 1,
  "loanTerm": 30,
  "amortizationType": "FixedRate",
  "apr": 6.0,
  "lockInDate": "2017-11-20",
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

<p class="use-text-small">The contents of this file include the <code>Action Taken Type</code>, <code>Loan Term</code>, <code>Amoritization Type</code>, <code>APR</code>, <code>Lock in Date</code> and <code>Reverse Mortgage</code>.</p>

<h4>Example Response in <code>CSV</code> format</h4>
<section class="code-block">
<code>CSV</code>
{% highlight bash %}
action_taken_type,loan_term,amortization_type,apr,lock_in_date,reverse_mortgage,rate_spreadrate_spread
1,30,FixedRate,6.0,2017-11-20,2,2.01
1,30,VariableRate,6.0,2017-11-20,2,2.15
{% endhighlight %}
</section>

<p class="use-text-small">The response appends the calculate <code>rate_spread</code> to each row of the orginal <code>CSV</code>.</p>