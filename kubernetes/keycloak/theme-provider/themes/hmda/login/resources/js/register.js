/* eslint-env browser, jquery */
/* global HMDA, initRules */
!(function() {
  //Given a list of institutions, create units of html for each of them
  function buildList(institutions) {
    $('#institutions')
      .empty()
      .append(
        makeInstitutionsLabel(institutions),
        makeInstitutionsList(institutions)
      )
    addInstitutionsToInput()
  }

  //Given a list of institutions, return a label element describing them
  function makeInstitutionsLabel(institutions) {
    var labelContent = 'Select your institution'
    if (institutions.length > 1) {
      labelContent =
        'Select all available institutions you wish to file for. You may select more than one.'
    }
    return $('<label>').text(labelContent)
  }

  function makeInstitutionsList(institutions) {
    var list = $('<ul class="usa-unstyled-list">')

    for (var i = 0; i < institutions.length; i++) {
      var li = $('<li>')
      var input = $('<input class="institutionsCheck">').attr({
        type: 'checkbox',
        name: 'institutions',
        id: institutions[i].id,
        value: institutions[i].id
      })
      var label = $('<label>').attr({
        for: institutions[i].id
      })
      var strong = $('<strong>').text(institutions[i].name)
      var dl = makeDataList(institutions[i].externalIds)
      label.append(strong, dl)
      li.append(input, label)
      list.append(li)
    }
    return list
  }

  //Create description list from a list of ids
  function makeDataList(externalIds) {
    var dl = $('<dl>').addClass('usa-text-small')
    for (var i = 0; i < externalIds.length; i++) {
      var dt = $('<dt>').text(externalIds[i].externalIdType.name + ':')
      var dd = $('<dd>').text(externalIds[i].value)
      dl.append(dt, dd)
    }

    return dl
  }

  //Get checked institutions' values and add them to a hidden input field to be submitted
  function addInstitutionsToInput() {
    var listOfInstitutions = []
    // add to the user.attributes.institutions input
    $('.institutionsCheck').each(function(index) {
      if ($(this).prop('checked')) {
        listOfInstitutions.push($(this).val())
      }
    })
    $('#user\\.attributes\\.institutions').val(listOfInstitutions.join(','))
  }

  //AJAX call to get data, calls buildList with returned institutions
  function getInstitutions(domain) {
    $.ajax({
      url: HMDA.institutionSearchUri,
      statusCode: {
        404: function() {
          $('#institutions')
            .empty()
            .append(
              $('<span class="hmda-error-message">').append(
                $(
                  "<span>Sorry, we couldn't find that email domain. For help getting registered, please contact </span>"
                ),
                getEmailLink(),
                $(
                  '<span> and provide your institution name, RSSD, and Tax ID. If your institution is a non-depository and does not have an RSSD, please provide your NMLS ID in place of the RSSD.</span>'
                )
              )
            )
        }
      },
      data: { domain: domain },
      beforeSend: function() {
        $('#institutions')
          .empty()
          .append(
            $('<div class="LoadingIconWrapper">').append(
              $('<div class="LoadingIcon">')
            )
          )
      }
    })
      .done(function(data, status, xhr) {
        buildList(data.institutions)
      })
      .fail(function(request, status, error) {
        $('#institutions')
          .empty()
          .append(
            $('<span class="hmda-error-message">').append(
              $('<span>Sorry, something went wrong. Please contact </span>'),
              getEmailLink(),
              $(
                '<span> for help getting registered or try again in a few minutes.</span>'
              )
            )
          )
      })
  }

  //email parsing util
  function emailToDomain(email) {
    return email.split('@', 2)[1]
  }

  //build email links from values provided at build time
  function getEmailLink() {
    return $('<a>')
      .attr({
        href:
          'mailto:' +
          HMDA.supportEmailTo +
          '?subject=' +
          HMDA.supportEmailSubject
      })
      .text(HMDA.supportEmailTo)
  }

  //Make a debounced version of the getInstitutions API call, passing in the desired delay
  function makeDebouncer(delay) {
    var timeout
    return function(domain) {
      clearTimeout(timeout)
      timeout = setTimeout(function() {
        getInstitutions(domain)
      }, delay)
    }
  }

  var debounceRequest = makeDebouncer(300)

  $(document).ready(function() {
    var email = $('#email')
    var emailExp = /[a-zA-Z0-9!#$%&'*+/=?^_`{|}~.-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-]+/
    var lastEmail = null

    //Process email and make debounced request when typing in email field
    email.on('blur keyup', checkEmail)

    checkEmail({})

    function checkEmail(e) {
      var emailVal = email.val().trim()
      if (emailVal === lastEmail) return
      else lastEmail = emailVal

      // keycode (tab key) used to not warn when first tabbing into the email field
      if ((emailVal === '' || emailVal === null) && e.keyCode !== 9) {
        $('#institutions').text('')
      } else {
        // e.keyCode will be 'undefined' on tab key
        // don't make the API call on tab keyup
        var domain = emailToDomain(emailVal)
        if (
          emailExp.test(emailVal) ||
          (e.type === 'blur' && domain !== '' && domain !== undefined)
        ) {
          debounceRequest(domain)
        }
      }
    }

    //Save institution to input when clicked
    $('#institutions').on('click', '.institutionsCheck', addInstitutionsToInput)
    var loading = $('#submit-loader')
    var form = $('#kc-register-form')

    form.on('submit', function(e) {
      loading.css('display', 'block')
    })

    initRules()
  })
})()
