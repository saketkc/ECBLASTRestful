
from fabric.api import run
from fabric.api import put
__node_file_path__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
__node_zip_path__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/ZIPS'

import sys
#from ConfigParser import ConfigParser
import os
update_job_status_cmd_line = "python /homes/saketc/python_job_checker/bsub_status.py "
#present_directory = os.path.abspath(os.path.join(os.path.dirname( __file__ ),
#'..', 'src/java/uk/ac/ebi/ecblast/ecblastWS/parser/'))
#config_file = os.path.join(present_directory, 'config.ini')
server_upload_path = "/home/saket/INCOMINGUPLOADS/"
import zipfile

def zipdir(path, zip):
    for root, dirs, files in os.walk(path):
        for file in files:
            zip.write(os.path.join(root, file))

def copy_to_server(uuid):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    directory = os.path.join(__node_file_path__, uuid)
    directory_zip = os.path.join(__node_zip_path__, uuid + ".zip")
    zipf = zipfile.ZipFile(directory_zip, 'w')
    zipdir(directory, zipf)
    zipf.close()
    run("mkdir -p " +  server_upload_path)
    try:
        put(directory_zip, server_upload_path)
        put(directory, server_upload_path)
    except:
        sys.exit("ERROR")


