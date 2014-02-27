from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import run_search
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
    parser.add_argument("--c", type=str, required=True)
    parser.add_argument("--type", type=str, required=True)

    args = parser.parse_args(argv)
    queryfile = args.q
    queryformat = args.Q
    uuid = args.uuid
    c = args.c
    type=args.type
    directory = args.directory
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string="saketc@172.21.22.5",
                  password="uzfmTjX7"):
        run_search(uuid, directory, queryformat, queryfile, type, c)
        stdout = submit_bsub(uuid)
        if __job_submitted_re__.search(stdout):
            print stdout.split("<")[1].split(">")[0]
        else:
            print "error"

if __name__ == "__main__":
    main(sys.argv[1:])
