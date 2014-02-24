from fabric.api import run
from fabric.api import put
import logging
import sys
import os
import ntpath
import shutil

__farm_upload_directory__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
__atom_atom_mapping_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j AAM"
__compare_reactions_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j compare"
__matching_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j transform"
__update_job_status_cmd_line__ = "python /homes/saketc/python_job_checker/bsub_status.py "
__tomcat_jobs_log_directory__ = "/home/saket/LOGS/"


def get_base_filename(path):
    head, tail = ntpath.split(path)
    return tail or ntpath.basename(head)


def run_atom_atom_mapping(
        uuid,
        user_upload_directory,
        user_uploaded_file,
        file_format):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.baseconfig(filename=__logfile__, level=logging.DEBUG)
    logging.info(
        "Called run_atom_atom_mapping function, Beginning logs for JobId:" +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    filename = get_base_filename(user_uploaded_file)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid +
        "__run_atom_atom_mapping.sh")
    logging.info("User uploaded filename " + filename)
    logging.info("Job bash file local location " + job_bash_file)
    renamed_filepath = os.path.join(job_directory_on_farm, filename)
    logging.info("User renamed filename as on farm" + renamed_filepath)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    cmd = cd_path + " && " + __atom_atom_mapping_cmd_line__ + " -Q " + file_format + " -q " + \
        renamed_filepath + " 1>%s 2>%s" % (job_prefix + "__stdout.log", job_prefix + "__stderr.log")
    try:
        logging.info("Attempting to write to bash file locally")
        with open(job_bash_file, 'w') as f:
            f.write("#!/bin/bash \n")
            f.write(cmd)
        logging.info("Successfully created bash file at " + job_bash_file)
        logging.info("Command line written on bash file : " + cmd)
    except Exception as e:
        logging.error("UNABLE TO CREATE BASH FILE!" + str(e))

    try:
        logging.info(
            "Attempting to create job_direcotry on farm at location :" +
            job_directory_on_farm)
        run("mkdir -p " + job_directory_on_farm)
        logging.debug(
            "Created directory " +
            job_directory_on_farm +
            " Successfully")
    except Exception as e:
        logging.error("Error creating directory on farm" + str(e))

    try:
        logging.info("Attempting to transfer contents to the farm")
        put(user_upload_directory, __farm_upload_directory__)
        logging.debug("Transfer to farm completed successfully")
        return renamed_filepath
    except:
        logging.error("FAILED TO TRANSFER FILE TO SERVER")
        sys.exit("ERROR")


def submit_bsub(uuid, path):
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.baseconfig(filename=__logfile__, level=logging.DEBUG)
    user_directory = os.path.join(
        __farm_upload_directory__,
        uuid +
        "__run_atom_atom_mapping.sh")
    filepath = os.path.join(user_directory, uuid)
    cd_path = "cd " + user_directory
    cmd = "bsub -q production-rh6 -J " + \
        uuid + " \" bash " + filepath + ".sh\""
    logging.info(
        "Attempting to submit the follwoing bsub command on the farm" +
        cmd)
    job_id = None
    try:
        job_id = run(cmd)
        logging.debug("Submitted job on farm, the response was" + job_id)
    except Exception as e:
        logging.error("JOB COULD NOT BE SUBMITTED ON FARM" + str(e))
        sys.exit("ERROR")
    if job_id:
        job_id = job_id.strip()
    try:
        job_id = int(job_id)
    except:
        logging.error(
            "RETURNED job ID was no string, got this instead: " +
            str(job_id))
        sys.exit("ERROR")
    return job_id
