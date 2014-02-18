from fabric.api import run
from fabric.api import put
__node_file_path__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
import sys
#from ConfigParser import ConfigParser
import os
java_cmd_line = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar "
#present_directory = os.path.abspath(os.path.join(os.path.dirname( __file__ ),
#'..', 'src/java/uk/ac/ebi/ecblast/ecblastWS/parser/'))
#config_file = os.path.join(present_directory, 'config.ini')


def copy_to_node(uuid, path):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    directory = os.path.join(__node_file_path__, uuid)
    extension = path.split(".")[-1]
    renamed_filepath = os.path.join(directory, uuid+"."+extension)
    run("mkdir -p " +  directory)
    try:
        put(path, renamed_filepath)
        return renamed_filepath
    except:
        sys.exit("ERROR")


def submit_bsub(uuid, path):
    filepath = os.path.join(__node_file_path__, uuid, uuid)

    cmd = "bsub -q research-rh6 " + "\"" + java_cmd_line + path + " 1>%s 2>%s"%(filepath + "-stdout.log",  filepath + "-stderr.log") + "\""
    job_id = run(cmd)
    return job_id


def is_job_finished(job_id):
    pass

