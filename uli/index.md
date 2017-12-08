---
layout: default
title: "HMDA Platform API - Check Digit"
---

<hgroup>
  <h1>ULI</h1>
  <h2>Create a check digit and validate a ULI.</h2>
  <p class="usa-font-lead">The <code>/uli</code> endpoint allows for the creation of a check digit and validation of a ULI. These endpoints help to ensure you are submitting valid HMDA data.</p>
</hgroup>

---

<hgroup>
  <h3 id="generate">Generate a check digit</h3>
  <p class="usa-font-lead">Generate a check digit and full ULI from an application/loan id.</p>
  <p>Using the <code>/uli/checkDigit</code> endpoint provide an application/loan id and get the check digit and full ULI in a response.</p>
</hgroup>

<h4>Example</h4>
{% highlight PowerShell %}
POST /uli/checkDigit
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
      <td><code>loanId</code></td>
      <td><code>string</code></td>
      <td>A valid loan id.</td>
    </tr>
  </tbody>
</table>

<h4>Example payload</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "loanId": "10Bx939c5543TqA1144M999143X"
}
{% endhighlight %}
</section>

<h4>Example Response</h4>
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

---

<hgroup>
  <h3 id="generate-batch">Generate Batch</h3>
  <p class="usa-font-lead">Generate batch check digits and full ULIs from a file.</p>
  <p>Using the <code>/uli/checkDigit/csv</code> endpoint provide a file with a list of loan ids, each on a new line.</p>
</hgroup>

<h4>Example</h4>
{% highlight PowerShell %}
POST /uli/checkDigit/csv
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

<h4>Example file contents</h4>
<section class="code-block">
{% highlight PowerShell %}
10Cx939c5543TqA1144M999143X
10Bx939c5543TqA1144M999143X
{% endhighlight %}
</section>

<h4>Example Response in <code>CSV</code> format</h4>
<section class="code-block">
<code>CSV</code>
{% highlight PowerShell %}
loanId,checkDigit,uli
10Bx939c5543TqA1144M999143X,38,10Bx939c5543TqA1144M999143X38
10Cx939c5543TqA1144M999143X,10,10Cx939c5543TqA1144M999143X10
{% endhighlight %}
</section>

---

<hgroup>
  <h3 id="validate">Validate</h3>
  <p class="usa-font-lead">Validates that a ULI has the correct check digit.</p>
  <p>Using the <code>/uli/validate</code> endpoint you can provide a ULI and get the response of valid or not.</p>
</hgroup>

<h4>Example</h4>
{% highlight PowerShell %}
POST /uli/validate
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
      <td><code>uli</code></td>
      <td><code>string</code></td>
      <td>A ULI.</td>
    </tr>
  </tbody>
</table>

<h4>Example payload</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "uli": "10Bx939c5543TqA1144M999143X38"
}
{% endhighlight %}
</section>

<h4>Example Response</h4>
<section class="code-block">
<code>JSON</code>
{% highlight json %}
{
  "isValid": true
}
{% endhighlight %}
</section>

---

<hgroup>
  <h3 id="validate-batch">Validate Batch</h3>
  <p class="usa-font-lead">Validates that a batch of ULIs have the correct check digit.</p>
  <p>Using the <code>/uli/validate/csv</code> endpoint you can provide a list of ULIs and get the response of valid or not for each.</p>
</hgroup>

<h4>Example</h4>
{% highlight PowerShell %}
POST /uli/validate/csv
{% endhighlight %}

<h4>Allowed Methods</h4>
<code>POST</code>

<h4>Example file content</h4>
<section class="code-block">
{% highlight PowerShell %}
10Cx939c5543TqA1144M999143X10
10Bx939c5543TqA1144M999143X38
10Bx939c5543TqA1144M999133X38
{% endhighlight %}
</section>

<h4>Example Response in <code>CSV</code> format</h4>
<section class="code-block">
<code>CSV</code>
{% highlight PowerShell %}
uli,isValid
10Cx939c5543TqA1144M999143X10,true
10Bx939c5543TqA1144M999143X38,true
10Bx939c5543TqA1144M999133X38,false
{% endhighlight %}
</section>