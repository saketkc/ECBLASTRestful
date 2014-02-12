from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import copy_to_node
from fabfile import submit_bsub

import argparse
import sys
import re
__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", type=str, required=True)
    args = parser.parse_args(argv)
    path = args.path
    with settings(hide('running', 'stdout', 'stderr'),
                  host_string="saketc@172.21.22.5",
                  password="uzfmTjX7"):
        stdout = copy_to_node(path)
        if stdout:
            sys.exit("Error %s" % (stdout))
        stdout = submit_bsub()
        if __job_submitted_re__.search(stdout):
            print "Job ID: ", stdout.split("<")[1].split(">")[0]
        else:
            print "JOB not submitted"

if __name__ == "__main__":
    main(sys.argv[1:])
