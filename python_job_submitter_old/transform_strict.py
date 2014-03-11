#!/usr/bin/env python
# Author: Saket Choudhary <saketkc@gmail.com>

from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import run_matching
from fabfile import submit_bsub
from config import username, host, password
import argparse
import sys
import re
__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", help="Unique ID", type=str, required=True)
    parser.add_argument("--directory", help="Absolute path to user upload directory on tomcat", type=str, required=True)
    parser.add_argument("--q", help="Smiles query or absolute RXN file path as on tomcat", type=str, required=True)
    parser.add_argument("--Q", help="Query format [SMI/RXN]", type=str, required=True)
    parser.add_argument("--c", help="No. of hits", type=str, required=True)
    args = parser.parse_args(argv)
    queryfile = args.q
    queryformat = args.Q
    uuid = args.uuid
    c = args.c
    directory = args.directory
    login = username + '@' + host
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string=login,
                  password=password):
        run_matching(uuid, directory, queryformat, queryfile, c,
                     is_strict=True)
        stdout = submit_bsub(uuid)
        if __job_submitted_re__.search(stdout):
            print stdout.split("<")[1].split(">")[0]
        else:
            print "error"

if __name__ == "__main__":
    main(sys.argv[1:])
