## Usage

To use the script for running newman multiple times:

`./run_newman.sh <num_times> <num_rows>`

For example, this will do a 100 row filing 2 times:

`./run_newman.sh 2 100`

## Notes

- `num_times` is `0` based
- The api test files have been modified to allow more wait time when loading large files.
