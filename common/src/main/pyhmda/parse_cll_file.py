#!/usr/bin/env python

import argparse
import logging
import pandas as pd
import os
import re
import sys

from utils import *

parser = argparse.ArgumentParser("parse_cli_file")
parser.add_argument("cllfile")
parser.add_argument('-v', '--verbose', action='store_true')
parser.add_argument("-o", "--output-file")
args = parser.parse_args()

loglevel = logging.DEBUG if args.verbose else logging.INFO
logging.basicConfig(level=loglevel)

def conv_state(val: str) -> str:
    if not re.match(r"^[A-Z]{2}$", val):
        raise ValueError(f"invalid state: \"{val}\"")
    return val


def conv_limit(val: str) -> int:
    return int(val.translate(str.maketrans("", "", " $,")))
    
        
columns=["FIPSStateCode", "FIPSCountyCode", "CountyName", "State", "CBSANumber",
         "One-UnitLimit", "Two-UnitLimit", "Three-UnitLimit", "Four-UnitLimit"]

converters = {"FIPSStateCode": conv_dgstr, "FIPSCountyCode": conv_dgstr, "CountyName": conv_str,
              "State": conv_state, "CBSANumber": conv_optdgstr, "One-UnitLimit": conv_limit,
              "Two-UnitLimit": conv_limit, "Three-UnitLimit": conv_limit,
              "Four-UnitLimit": conv_limit}

root, ext = os.path.splitext(args.cllfile)
prepared_file = f"{root}-prepared{ext}"
output_file = args.output_file if args.output_file \
    else f"{os.path.splitext(args.cllfile)[0]}-parsed.txt"
data_pattern = r'\d+,\d+,[^,]+,\w+,\d*,"[,\s\$\d]+","[,\s\$\d]+","[,\s\$\d]+","[,\s\$\d]+"'
prepare_file(args.cllfile, prepared_file, data_pattern)
df = pd.read_csv(prepared_file, sep=',', header=None, names=columns, converters=converters)
logging.info(f"Parsed {prepared_file}")
df.to_csv(output_file, sep='|', index=False)
logging.info(f"Wrote output file {output_file}")
os.remove(prepared_file)
