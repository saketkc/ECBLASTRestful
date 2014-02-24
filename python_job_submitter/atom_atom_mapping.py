from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import run_atom_atom_mapping
from fabfile import submit_bsub

import argparse
import sys
import re
__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", type=str, required=True)
    parser.add_argument("--directory", type=str, required=True)
    parser.add_argument("--file", type=str, required=True)
    parser.add_argument("--filetype", type=str, required=True)
    args = parser.parse_args(argv)
    directory = args.directory
    uuid = args.uuid
    file = args.file
    filetype = args.filetype
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string="saketc@172.21.22.5",
                  password="uzfmTjX7"):
        rxn_filepath = run_atom_atom_mapping(uuid, directory, file, filetype)
        stdout = submit_bsub(uuid, rxn_filepath)
        if __job_submitted_re__.search(stdout):
            print stdout.split("<")[1].split(">")[0]
        else:
            print "error"

if __name__ == "__main__":
    main(sys.argv[1:])