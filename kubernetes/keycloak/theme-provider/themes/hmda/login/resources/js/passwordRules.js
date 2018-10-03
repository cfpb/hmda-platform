/* eslint-env browser, jquery */

function initRules() {
  var email = $('#email')
  var password = $('#password')
  var passwordConfirm = $('#password-confirm')
  var validationList = $('#validation_list > li')
  var pwdMatchError = $('#password-confirm-error-message')
  if (!password.length) password = $('#password-new')

  //Password checking function list
  var checkFunctions = [
    function atLeastTwelve(val) {
      return val.length > 11
    },

    function hasUppercase(val) {
      return !!val.match(/[A-Z]/)
    },

    function hasLowercase(val) {
      return !!val.match(/[a-z]/)
    },

    function hasNumber(val) {
      return !!val.match(/[0-9]/)
    },

    function hasSpecial(val) {
      return !!val.match(/[^a-zA-Z0-9]/)
    },

    function notUsername(password, username) {
      return password !== username
    }
  ]

  //Mark password rules as completed when typing and possible adjust password matching error
  password.on('keyup', function(e) {
    validationList.each(function(i, el) {
      if (checkFunctions[i](password.val(), email.val())) {
        el.className = 'complete'
      } else {
        el.className = ''
      }
    })

    var passVal = password.val()
    var confirmVal = passwordConfirm.val()
    if (confirmVal) {
      if (passVal !== confirmVal) showMatchingError()
      else hideMatchingError()
    }
  })

  //Display checks for unmet password rules
  password.on('blur', function(e) {
    validationList.each(function(i, el) {
      if (checkFunctions[i](password.val(), email.val())) {
        el.className = 'complete'
      } else {
        el.className = 'missing'
      }
    })
  })

  //show or hide confirm as needed
  //<= ensures it doesn't show too early
  passwordConfirm.on('keyup', function(e) {
    var passVal = password.val()
    var confirmVal = passwordConfirm.val()
    if (passVal === confirmVal) {
      hideMatchingError()
    } else if (passVal.length <= confirmVal.length) {
      showMatchingError()
    }
  })

  //show or hide confirm, on blur will also show errors if confirmation text is shorter than password
  passwordConfirm.on('blur', function(e) {
    if (passwordConfirm.val() === password.val()) {
      hideMatchingError()
    } else {
      showMatchingError()
    }
  })
  //Util for showing error text when password and confirm field don't match
  function showMatchingError() {
    pwdMatchError.css('display', 'block')
    pwdMatchError.prev().css('font-weight', 'bold')
  }

  //Util for hiding error text when password and confirm field match
  function hideMatchingError() {
    pwdMatchError.css('display', 'none')
    pwdMatchError.prev().css('font-weight', 'normal')
  }
}
