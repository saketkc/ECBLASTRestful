#!/usr/bin/env python
# Author: Saket Choudhary <saketkc@gmail.com>

from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import run_atom_atom_mapping_rxn
from fabfile import submit_bsub
from config import username, host, password
import argparse
import sys
import re
__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", help="unique ID", type=str, required=True)
    parser.add_argument("--directory", help="Path location where user uploads sit",
                        type=str, required=True)
    parser.add_argument("--Q", type=str, help="Query Type", required=True)
    parser.add_argument("--q", help="SMILES string or absolute path to RXN file",
                        type=str, required=True)
    args = parser.parse_args(argv)
    directory = args.directory
    uuid = args.uuid
    query = args.q.replace("\"", "")
    query_format = args.Q
    login = username + "@" + host
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string=login,
                  password=password):
        run_atom_atom_mapping_rxn(uuid, directory, query_format, query)
        stdout = submit_bsub(uuid)
        if __job_submitted_re__.search(stdout):
            print stdout.split("<")[1].split(">")[0]
        else:
            print "error"

if __name__ == "__main__":
    main(sys.argv[1:])