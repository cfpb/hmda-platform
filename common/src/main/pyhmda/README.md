# HMDA Python Tools

## Setup

Create a python environment using **python3.9** or later.

```
$ python -m venv .venv
$ source .venv/bin/activate
(.venv) $ pip install -r requirements.txt
Collecting pandas (from -r requirements.txt (line 1))
  Using cached pandas-2.2.1-cp312-cp312-macosx_10_9_x86_64.whl.metadata (19 kB)
...
$
```
## Tools

### parse_cll_file

```
(.venv) $ parse_cll_file.py -h
usage: parse_cli_file [-h] [-v] [-o OUTPUT_FILE] cllfile

positional arguments:
  cllfile

options:
  -h, --help            show this help message and exit
  -v, --verbose
  -o OUTPUT_FILE, --output-file OUTPUT_FILE
(.venv) $ 
```

This utility parses Conforming Loan Limit (CLL) files produced annually by the Federal Housing Finance Agency (FHFA). It should be run with the output file option to generate FullCountyLoanLimitList{YYYY}.txt files suitable for HMDA ingest. The whole annual process can be performed with these steps.

1. Download the FHFA CLL file for the current year in Excel format from [here](https://www.fhfa.gov/data/conforming-loan-limit-cll-values).
1. Open the file in Excel and save it in CSV file format.
1. Run parse_cll_file.py on the CSV file.
1. Commit the output file to the hmda-platform repo and process as normal.

**NOTE:** The utility strips input file header lines and performs validations and transformations based on expected line and field formats. It uses an all-or-nothing approach, and so any unexpected input data will likely trigger an abort with some useful error message. Line and field formats and their year-to-year changes are wholely owned and determined by FHFA (and not anyone at CFPB.)
