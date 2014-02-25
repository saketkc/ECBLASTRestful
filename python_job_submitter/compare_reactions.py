from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import run_compare_reactions
from fabfile import submit_bsub

import argparse
import sys
import re
__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", type=str, required=True)
    parser.add_argument("--directory", type=str, required=True)
    parser.add_argument("--q", type=str, required=True)
    parser.add_argument("--Q", type=str, required=True)
    parser.add_argument("--t", type=str, required=True)
    parser.add_argument("--T", type=str, required=True)


    args = parser.parse_args(argv)
    queryfile = args.q
    queryformat = args.Q
    targetfile = args.t
    targetformat = args.T
    uuid = args.uuid
    directory = args.directory
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string="saketc@172.21.22.5",
                  password="uzfmTjX7"):
        run_compare_reactions(uuid, directory, queryformat,
                              queryfile, targetformat, targetfile)
        stdout = submit_bsub(uuid)
        if __job_submitted_re__.search(stdout):
            print stdout.split("<")[1].split(">")[0]
        else:
            print "error"

if __name__ == "__main__":
    main(sys.argv[1:])
