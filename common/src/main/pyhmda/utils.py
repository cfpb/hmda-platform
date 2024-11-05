
from datetime import datetime
import logging
import pandas as pd
import re
from typing import Any
import sys

"""
Column Conversion Functions

For use with pandas read functions as "converters".
Each of these should either return a conforming value or raise a ValueError.
Functions named with "opt" allow for missing values and return these as pd.NA.
"""

def normalize(val: str) -> str:
    return "" if val in ["NULL"] else val.strip()


def conv_str(val: str) -> str:
    val = normalize(val)
    if not val: raise ValueError(f"invalid str: \"{val}\"")
    return val    


def conv_optstr(val: str) -> Any:
    val = normalize(val)
    return val if val else pd.NA


def conv_dgstr(val: str) -> str:
    val = normalize(val)
    if re.match(r"^\d+$", val): return val
    raise ValueError(f"invalid dgstr: \"{val}\"")


def conv_optdgstr(val: str) -> Any:
    val = normalize(val)
    if not val: return pd.NA
    if re.match(r"^\d+$", val): return val
    raise ValueError(f"invalid dgstr: \"{val}\"")


def conv_num(val: str) -> int:
    return int(normalize(val))


def conv_optnum(val: str) -> Any:
    val = normalize(val)
    return int(val) if val else pd.NA


def conv_pct(val: str) -> float:
    return float(normalize(val))


def conv_optpct(val: str) -> Any:
    val = normalize(val)
    return float(val) if val else pd.NA


def prepare_file(read_file: str, write_file: str, pattern: str, expected_match: float=0.95) -> None:
    """Performs preprocessing on source files to insure clean reads by pandas.
    """
    lc, mc = 0, 0
    with open(read_file, 'r') as rf:
        with open(write_file, 'w') as wf:
            for line in rf.readlines():
                lc += 1
                if re.match(pattern, line):
                    mc += 1
                    wf.write(line)
    if mc < expected_match * lc:
        sys.exit(f"{read_file} pattern matched only {mc} of {lc} lines")
    logging.info(f"Prepared file {write_file}")


def apply_authorized_modifications(modmap: dict, df: pd.DataFrame) -> pd.DataFrame:
    """Applies modifications to non-CFPB-owned source data files on directions of outside agencies.
    """
    for mod_date in sorted(modmap.keys()):
        df = modmap[mod_date](df)
    return df


# Census Flat File Modifications

def replace_MedianAge_2002_values(df: pd.DataFrame) -> pd.DataFrame:
    df.loc[df["Median Age"] == 2002, "Median Age"] = 6
    return df


# Modifications to published Census Flat Files directed by the US Census Bureau.
census_file_authorized_modifications = {
    datetime(2024, 9, 1) : replace_MedianAge_2002_values
}

