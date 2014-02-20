from fabric.api import run
from fabric.api import put
__node_file_path__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
import sys
#from ConfigParser import ConfigParser
import os
java_cmd_line = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar "
update_job_status_cmd_line = "python /homes/saketc/python_job_checker/bsub_status.py "
#present_directory = os.path.abspath(os.path.join(os.path.dirname( __file__ ),
#'..', 'src/java/uk/ac/ebi/ecblast/ecblastWS/parser/'))
#config_file = os.path.join(present_directory, 'config.ini')


def copy_to_node(uuid, path):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    user_directory = os.path.join(__node_file_path__, uuid)
    filepath = os.path.join(user_directory, uuid)
    filename = path.split("/")[-1]
    cd_path = "cd " + user_directory
    renamed_filepath = os.path.join(user_directory, filename)
    cmd = cd_path +  " && "+java_cmd_line + renamed_filepath + " 1>%s 2>%s"%(filepath + "-stdout.log",  filepath + "-stderr.log")
        #+ " ; " + update_job_status_cmd_line + " --uuid=" + uuid
    configfile = os.path.abspath(path +"/..") + "/" + uuid + ".sh"
    with open(os.path.abspath(path +"/..") + "/" + uuid + ".sh", 'w') as f:
        f.write(cmd)

    run("mkdir -p " +  user_directory)
    try:
        put(path, user_directory)
        put(configfile, user_directory)
        return renamed_filepath
    except:
        sys.exit("ERROR")


def submit_bsub(uuid, path):
    user_directory = os.path.join(__node_file_path__, uuid)
    filepath = os.path.join(user_directory, uuid)
    cd_path = "cd " + user_directory
    cmd = "bsub -q research-rh6 -J " + uuid + " \" bash " + filepath + ".sh\""
    job_id = run(cmd)
    #print cmd
    return job_id


def is_job_finished(job_id):
    pass

