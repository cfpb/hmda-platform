#!/usr/bin/env python

import argparse
import logging
import pandas as pd
import os
import re
import sys

from utils import *

parser = argparse.ArgumentParser("parse_census_file")
parser.add_argument("censusfile")
parser.add_argument("delineationfile")
parser.add_argument('-v', '--verbose', action='store_true')
parser.add_argument("-o", "--output-file")
args = parser.parse_args()

loglevel = logging.DEBUG if args.verbose else logging.INFO
logging.basicConfig(level=loglevel)

def conv_scf(val: str) -> str:
    val = val.strip()
    if val not in ["T", "S", "I"]: raise ValueError(f"invalid scf: \"{val}\"")
    return val    

census_file_columns = {
    0: ["Collection Year", conv_num], 1: ["MSA/MD", conv_dgstr], 2: ["State", conv_dgstr],
    3: ["County", conv_dgstr], 4: ["Census Tract", conv_dgstr],
    13: ["FFIEC Median Family Income", conv_num], 22: ["Population", conv_optnum],
    28: ["Minority Population %", conv_optpct], 879: ["Number of Owner Occupied Units", conv_optnum],
    899: ["Number of 1 to 4 Family Units", conv_optnum], 585: ["Tract MFI", conv_optnum],
    12: ["Tract to MSA Income %", conv_optpct], 1057: ["Median Age", conv_optnum],
    6: ["Small County", conv_scf]
}
cfkeys = census_file_columns.keys()
cfcolnames = {k: v[0] for k, v in census_file_columns.items()}
cfconverters = {k: v[1] for k, v in census_file_columns.items()}
parsed_census_df = pd.read_csv(args.censusfile, sep=',', header=None, usecols=cfkeys,
    converters=cfconverters)[cfkeys].rename(cfcolnames, axis=1)
parsed_census_df = apply_authorized_modifications(census_file_authorized_modifications,
                                                  parsed_census_df)
logging.info(f"Parsed {args.censusfile}")

root, ext = os.path.splitext(args.delineationfile)
prepared_file = f"{root}-prepared{ext}"
data_pattern = r'\d+,\d*,\d*,"[^"]+",[\w\s]+,("[^"]+")?,("[^"]+")?,[^,]+,[\w\s]+,\d+,\d+,\w+'
prepare_file(args.delineationfile, prepared_file, data_pattern)

delineation_file_columns = {
    3: ["CBSATitle", conv_str], 5: ["MDTitle", conv_optstr],
    9: ["State", conv_dgstr], 10: ["County", conv_dgstr]
}
dfkeys = delineation_file_columns.keys()
dfcolnames = {k: v[0] for k, v in delineation_file_columns.items()}
dfconverters = {k: v[1] for k, v in delineation_file_columns.items()}
parsed_delin_df = pd.read_csv(prepared_file, sep=',', header=None, usecols=dfkeys,
                              converters=dfconverters).rename(dfcolnames, axis=1)
logging.info(f"Parsed {prepared_file}")

parsed_delin_df["MSA/MD Name"] = parsed_delin_df.apply(lambda row:
    row.MDTitle if pd.notna(row.MDTitle) else row.CBSATitle, axis=1)
parsed_delin_df.drop(columns=["CBSATitle", "MDTitle"], inplace=True)
logging.info("Calculated MSA/MD Names")

output_file = args.output_file if args.output_file \
    else f"{os.path.splitext(args.censusfile)[0]}-parsed.txt"
output_df = parsed_census_df.merge(parsed_delin_df,
    how="left", on=["State", "County"])
output_df["MSA/MD Name"] = output_df.apply(lambda row:
    "" if row["MSA/MD"] == "99999" else row["MSA/MD Name"], axis=1)
output_df.to_csv(output_file, sep='|', index=False)
logging.info(f"Wrote output file {output_file}")
os.remove(prepared_file)
