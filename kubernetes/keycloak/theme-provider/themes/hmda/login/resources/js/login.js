/* eslint-env browser, jquery */
$(document).ready(function() {
  var loading = $('#submit-loader')
  var form = $('#kc-form-login')

  form.on('submit', function(e) {
    loading.css('display', 'block')
  })
})
