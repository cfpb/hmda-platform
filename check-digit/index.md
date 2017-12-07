---
layout: default
title: "HMDA Platform API - Check Digit"
---

<h1>Check digit</h1>
<hgroup>
  <h2 id="generate">Generate</h2>
  <h3>Generate a check digit and full ULI from a loan id.</h3>
  <h4>Using the <code>/uli/checkDigit</code> endpoint provide a loan id and get the check digit and full ULI in a response.</h4>
</hgroup>

<h5>Allowed Methods</h5>
<code>POST</code>

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
      <td><code>loanId</code></td>
      <td><code>string</code></td>
      <td>A valid loan id.</td>
    </tr>
  </tbody>
</table>

<h5>Example</h5>
{% highlight PowerShell %}
POST /uli/checkDigit
{% endhighlight %}

<h5>Example payload</h5>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "loanId": "10Bx939c5543TqA1144M999143X"
}
{% endhighlight %}
</section>

<h5>Example Response</h5>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "loanId": "10Cx939c5543TqA1144M999143X",
  "checkDigit": 10,
  "uli": "10Cx939c5543TqA1144M999143X10"
}
{% endhighlight %}
</section>

<hgroup>
  <h2 id="generate-batch">Generate Batch</h2>
  <h3>Generate batch check digits and full ULIs from a file.</h3>
  <h4>Using the <code>/uli/checkDigit/csv</code> endpoint provide a file with a list of loan ids, each on a new line.</h4>
</hgroup>

<h5>Allowed Methods</h5>
<code>POST</code>

<h5>Example</h5>
{% highlight PowerShell %}
POST /uli/checkDigit/csv
{% endhighlight %}

<h5>Example file contents</h5>
<section class="code-block">
{% highlight PowerShell %}
10Cx939c5543TqA1144M999143X
10Bx939c5543TqA1144M999143X
{% endhighlight %}
</section>

<h5>Example Response in <code>CSV</code> format</h5>
<section class="code-block">
<code>CSV</code>
{% highlight PowerShell %}
loanId,checkDigit,uli
10Bx939c5543TqA1144M999143X,38,10Bx939c5543TqA1144M999143X38
10Cx939c5543TqA1144M999143X,10,10Cx939c5543TqA1144M999143X10
{% endhighlight %}
</section>

<hgroup>
  <h2 id="validate">Validate</h2>
  <h3>Validates that a ULI has the correct check digit.</h3>
  <h4>Using the <code>/uli/validate</code> endpoint you can provide a ULI and get the response of valid or not.</h4>
</hgroup>

<h5>Allowed Methods</h5>
<code>POST</code>

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
      <td><code>uli</code></td>
      <td><code>string</code></td>
      <td>A ULI.</td>
    </tr>
  </tbody>
</table>

<h5>Example</h5>
{% highlight PowerShell %}
POST /uli/validate
{% endhighlight %}

<h5>Example payload</h5>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "uli": "10Bx939c5543TqA1144M999143X38"
}
{% endhighlight %}

<h5>Example Response</h5>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "isValid": true
}
{% endhighlight %}
</section>

<hgroup>
  <h2 id="validate-batch">Validate Batch</h2>
  <h3>Validates that a batch of ULIs have the correct check digit.</h3>
  <h4>Using the <code>/uli/validate/csv</code> endpoint you can provide a list of ULIs and get the response of valid or not for each.</h4>
</hgroup>

<h5>Allowed Methods</h5>
<code>POST</code>

<h5>Example</h5>
{% highlight PowerShell %}
POST /uli/validate/csv
{% endhighlight %}

<h5>Example file content</h5>
<section class="code-block">
{% highlight PowerShell %}
10Cx939c5543TqA1144M999143X10
10Bx939c5543TqA1144M999143X38
10Bx939c5543TqA1144M999133X38
{% endhighlight %}
</section>

<h5>Example Response in <code>CSV</code> format</h5>
<section class="code-block">
<code>CSV</code>
{% highlight PowerShell %}
uli,isValid
10Cx939c5543TqA1144M999143X10,true
10Bx939c5543TqA1144M999143X38,true
10Bx939c5543TqA1144M999133X38,false
{% endhighlight %}
</section>