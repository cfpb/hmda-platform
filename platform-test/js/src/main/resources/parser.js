
var encoding = "UTF-8";

function addRows(lars) {
  var larTable = document.getElementById('larTable');

  for (var i = 0; i < lars.length; i++) {
    var lar = lars[i];
    var row = document.createElement('tr');
    var codeCell = document.createElement('th');
    var actionTakenDateCell = document.createElement('th');
    var respondentIdCell = document.createElement('th');
    var loanApplicationDateCell = document.createElement('th');
    var codeValue = document.createTextNode(lar.agencyCode);
    var actionTakenDateValue = document.createTextNode(lar.actionTakenDate);
    var respondentIdValue = document.createTextNode(lar.respondentId);
    var loanApplicationDateValue = document.createTextNode(lar.loan.applicationDate);

    codeCell.appendChild(codeValue);
    actionTakenDateCell.appendChild(actionTakenDateValue);
    respondentIdCell.appendChild(respondentIdValue);
    loanApplicationDateCell.appendChild(loanApplicationDateValue);
    row.appendChild(codeCell);
    row.appendChild(actionTakenDateCell);
    row.appendChild(respondentIdCell);
    row.appendChild(loanApplicationDateCell);
    larTable.appendChild(row);
  }

}

function fileSelectListener(evt) {
  var files = evt.target.files;
  if (files.length > 0) {
    var file = files[0];

    var reader = new FileReader();

    reader.onload = function(e) {
      var text = reader.result;
      var parser = hmda.parser.fi.FIDataDatParser();
      var fiData = parser.readAll(text);
      var ts = fiData.ts;

      document.getElementById('activityYear').innerHTML = ts.activityYear;
      document.getElementById('respondentName').innerHTML = ts.respondent.name;
      document.getElementById('contactEmail').innerHTML = ts.contact.email;

      var lars = fiData.lars;
      addRows(lars);
    };

    reader.readAsText(file, encoding);

  } else {
    console.log("No file selected");
  }
}

document.getElementById('files').addEventListener('change', fileSelectListener, false);

