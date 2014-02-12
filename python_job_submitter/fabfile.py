from fabric.api import run
from fabric.api import put
__node_file_path__ = "/nfs/nobackup2/research/thornton/ecblast/webservices"
import sys


def copy_to_node(path):
    """ Copy the whole file/folder as passed in path
    to the farm node
    Params:
        path: path location to

    """
    run("mkdir -p " + __node_file_path__)
    try:
        put(path, __node_file_path__)
    except:
        sys.exit("ERROR")

def submit_bsub(job_cmd="python /homes/saket/say_hello.py 1>stdout.log 2>stderr.log", queue="research-rh6"):
    cmd = "bsub -q " + queue + " " + "\""+job_cmd+"\""
    job_id = run(cmd)
    return job_id


def is_job_finished(job_id):
    pass


