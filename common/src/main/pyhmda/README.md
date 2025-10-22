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

### parse_census_file

```
(.venv) $ parse_census_file.py -h
usage: parse_census_file [-h] [-v] [-o OUTPUT_FILE] censusfile delineationfile

positional arguments:
  censusfile
  delineationfile

options:
  -h, --help            show this help message and exit
  -v, --verbose
  -o OUTPUT_FILE, --output-file OUTPUT_FILE
(.venv) $
```

This utility parses Census Flat Files produced annually by the Federal Financial Institutions Examination Council (FFIEC) and Delineation Files produced by the US Census Bureau on a less frequent and irregular basis. Small subsets of data from both files are captured and joined. The tool should be run with the output file option to generate ffiec_census_{YYYY}.txt files suitable for HMDA ingest. The whole annual process can be performed with these steps.

1. Download the zip archive for the current year Census Flat File from [here](https://www.ffiec.gov/data/census/flat-files). Extract the CSV format file from the archive. 
 - Check the Documentation to ensure there are no schema changes. If there are, make sure the code accounts for them. 
2. Download the most recent Delineation File in Excel format from either [here](https://www.census.gov/geographies/reference-files/time-series/demo/metro-micro/delineation-files.html) or [here](https://www2.census.gov/programs-surveys/metro-micro/geographies/reference-files/). For a given year, multiple Delineation File variants are made available. The tool requires the variant for CBSAs and CSAs. This information will be noted on the download page or the file header.
3. Open the Delineation File in Excel and save it in CSV file format.
4. Run parse_census_file.py on the CSV files.
5. Commit the output file to the [resources](https://github.com/cfpb/hmda-platform/tree/master/common/src/main/resources) folder of the `hmda-platform` repo and process as normal.

Future note: per the 2025 FFIEC Census File documentation, "Indices 86, 87, 874, and 875 were expected to be available in the DHC but are no longer available in any file. They have been marked "NA" to preserve index compatibility and will be removed at a future date if they are still unavailable." In years after 2025, we may need to adjust the code to account for the index removal. 

For 2025 it was necessary to include the `skipfooter = 5` argument in the `parse_census_file.py` file because of an upstream issue with the `CensusFlatFile2025.csv` provided by the FFIEC that inadvertently included metadata. 