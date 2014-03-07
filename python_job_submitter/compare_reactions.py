from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import run_compare_reactions
from fabfile import submit_bsub
import argparse
import sys
import re
from config import username, host, password
__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", help="Unique ID", type=str, required=True)
    parser.add_argument("--directory", help="User upload direcotry as on tomcat", type=str, required=True)
    parser.add_argument("--q", help="Smiles query or reaction file path", type=str, required=True)
    parser.add_argument("--Q", help="Query format[SMI/RXN]", type=str, required=True)
    parser.add_argument("--t", help="Smiles target or reaction file path", type=str, required=True)
    parser.add_argument("--T", help="Target format[SMI/RXN]", type=str, required=True)

    args = parser.parse_args(argv)
    queryfile = args.q
    queryformat = args.Q
    targetfile = args.t
    targetformat = args.T
    uuid = args.uuid
    directory = args.directory
    login = username + "@" + host
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string=login,
                  password=password):
        run_compare_reactions(uuid, directory, queryformat,
                              queryfile, targetformat, targetfile)
        stdout = submit_bsub(uuid)
        if __job_submitted_re__.search(stdout):
            print stdout.split("<")[1].split(">")[0]
        else:
            print "error"

if __name__ == "__main__":
    main(sys.argv[1:])
