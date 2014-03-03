#!/usr/bin/env python
# Author: Saket Choudhary <saketkc@gmail.com>

from fabric.api import run
from fabric.api import put
import logging
import sys
import os
import ntpath

# Directoru on Farm node where all files get uploaded
# Each job gets uploaded as a folder with its folder name as it jobID
__farm_upload_directory__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
# Command Line static to run atom atom mapping
__atom_atom_mapping_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G  -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j AAM -f BOTH"
# Command Line to compare two reactions
__compare_reactions_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j compare -f BOTH"
# Command line for transformation
__generic_matching_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j transform -f BOTH"
# Command line to run search
__search_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j search"
# Python command to run once the job is complete
__update_job_status_cmd_line__ = "python /homes/saketc/python_job_checker/bsub_status.py "
# Folder location on which tomcat logs are uploaded
__tomcat_jobs_log_directory__ = "/home/saket/LOGS/"


def get_base_filename(path):
    """ Get filename from an absolute filepath

    Parameters
    ----------
    path : String
        Absolute path to the file/folder

    Returns
    -------
    filename : String

    """
    head, tail = ntpath.split(path)
    return tail or ntpath.basename(head)

# Run atom atom mapping reaction


def run_atom_atom_mapping_rxn(
        uuid,
        user_upload_directory,
        query_format,
        query):
    """ Run atom atom mapping reaction
    Params:
    ------
        uuid: String
            jobID
        user_upload_directory:  String
            Absolute path to directory where user folder is uploaded
        query: String
            Filepath OR Smiles Query for running atom atom mapping
        query_format: String
            Query format for query [RXN|SMI]

    Result:
    -------
        None or "error"
    """
    #assert (query_format == "SMI" or query_format == "RXN")
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
    logging.info(
        "Called run_atom_atom_mapping function, Beginning logs for JobId: " +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid +
        "__run.sh")
    logging.info("Job bash file local location: " + job_bash_file)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    if query_format == "SMI":
        # Read query as quoted string
        query = "\"" + query.replace("\"","") + "\""
    elif query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)

    # For text
    common_cmd = __atom_atom_mapping_cmd_line__ + \
        " -Q " + query_format + " -q " + query + \
        " 1>>%s 2>>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")

    cmd = cd_path + " && " + common_cmd
    """
    cmd = cd_path + " && " + common_cmd + " -f text -m -p " + \
        " 1>>%s 2>>%s" % (
            job_prefix + "__text.log",
            job_prefix + "__texterr.log")
    # For xml
    cmd += " && " + common_cmd + " -f xml -m -p " + \
        " 1>>%s 2>>%s" % (
            job_prefix + "__xml.log",
            job_prefix + "__xmlerr.log")
    """
    try:
        logging.info("Attempting to write to bash file locally")
        with open(job_bash_file, 'w') as f:
            f.write("#!/bin/bash\n")
            f.write(cmd)
        logging.info("Successfully created bash file at: " + job_bash_file)
        logging.info("Command line written on bash file: " + cmd)
    except Exception as e:
        logging.error("UNABLE TO CREATE BASH FILE!: " + str(e))
        return "Unable to create bash file"

    try:
        logging.info(
            "Attempting to create job_directory on farm at location: " +
            job_directory_on_farm)
        run("mkdir -p " + job_directory_on_farm)
        logging.debug(
            "Created directory " +
            job_directory_on_farm +
            " Successfully.")
    except Exception as e:
        logging.error("Error creating directory on farm: " + str(e))

    try:
        logging.info("Attempting to transfer contents to the farm")
        put(user_upload_directory, __farm_upload_directory__)
        logging.debug("Transfer to farm completed successfully")
        return True
    except Exception as e:
        logging.error("FAILED TO TRANSFER FILE TO SERVER:" + str(e))
        return "Unable to upload"


def run_search(
        uuid,
        user_upload_directory,
        query_format,
        query,
        search_type, hits):
    """ Run search
    Params:
    ------
        uuid: String
            jobID
        user_upload_directory:  String
            Absolute path to directory where user folder is uploaded
        query: String
            Filepath OR Smiles Query for running atom atom mapping
        query_format: String
            Query format for query [RXN|SMI]

    Result:
    -------
        None or "error"
    """
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
    logging.info(
        "Called run_atom_atom_mapping function, Beginning logs for JobId:" +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid +
        "__run.sh")
    logging.info("Job bash file local location " + job_bash_file)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    if query_format == "SMI":
        query = "\"" + query.replace("\"","") + "\""
    elif query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)
    # For text
    common_cmd = __search_cmd_line__ + " -Q " + query_format + \
        " -q " + query +  " -s " + search_type + " -c " + hits
    cmd = cd_path + " && " + common_cmd + " -m -p " + \
        " 1>>%s 2>>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")
    # For xml
    """
    cmd += " && " + common_cmd + " -f xml -m -p " + \
        " 1>>%s 2>>%s" % (
            job_prefix + "__xml.log",
            job_prefix + "__xmlerr.log")
    """
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
        return True
    except:
        logging.error("FAILED TO TRANSFER FILE TO SERVER")
        return "Unable to upload"


def run_compare_reactions(
        uuid,
        user_upload_directory,
        query_format,
        query,
        target_format,
        target):
    """ Run compare reactions
    Params:
    ------
        uuid: String
            jobID
        user_upload_directory:  String
            Absolute path to directory where user folder is uploaded
        query: String
            Filepath OR Smiles Query for running atom atom mapping
        query_format: String
            Query format for query [RXN|SMI]
        target: String
            Filepath OR Smiles Target Query for running atom atom mapping
        target_format: String
            Query format for query [RXN|SMI]

    Result:
    -------
        None or "error"
    """
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
    logging.info(
        "Called run_compare_reactions function, Beginning logs for JobId:" +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid + "__run.sh")
    logging.info("Job bash file local location " + job_bash_file)
    if query_format == "SMI":
        query = "\"" + query.replace("\"","") + "\""
    elif query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)
    if target_format == "SMI":
        target = "\"" + target.replace("\"","") + "\""
    elif query_format == "RXN":
        filename = get_base_filename(target)
        target = os.path.join(job_directory_on_farm, filename)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    common_cmd = __compare_reactions_cmd_line__ + " -Q " + query_format + \
        " -q " + query + " -T " + target_format + " -t " + target
    cmd = cd_path + " && " + common_cmd +  \
        "  1>>%s 2>>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")
    """
    cmd += " && " + common_cmd + " -f xml " + \
        "  1>>%s 2>>%s" % (
            job_prefix + "__xml.log",
            job_prefix + "__xmlerr.log")
    """
    try:
        logging.info("Attempting to write to bash file locally")
        with open(job_bash_file, 'w') as f:
            f.write("#!/bin/bash \n")
            f.write(cmd)
        logging.info("Successfully created bash file at: " + job_bash_file)
        logging.info("Command line written on bash file: " + cmd)
    except Exception as e:
        logging.error("UNABLE TO CREATE BASH FILE! " + str(e))

    try:
        logging.info(
            "Attempting to create job_direcotry on farm at location: " +
            job_directory_on_farm)
        run("mkdir -p " + job_directory_on_farm)
        logging.debug(
            "Created directory " +
            job_directory_on_farm +
            " Successfully")
    except Exception as e:
        logging.error("Error creating directory on farm: " + str(e))

    try:
        logging.info("Attempting to transfer contents to the farm")
        put(user_upload_directory, __farm_upload_directory__)
        logging.debug("Transfer to farm completed successfully")
    except:
        logging.error("FAILED TO TRANSFER FILE TO SERVER")
        sys.exit("ERROR")


def run_matching(
        uuid,
        user_upload_directory,
        query_format,
        query,
        hits, is_strict=False):
    """ Run matching
    Params:
    ------
        uuid: String
            jobID
        user_upload_directory:  String
            Absolute path to directory where user folder is uploaded
        query: String
            Filepath OR Smiles Query for running atom atom mapping
        query_format: String
            Query format for query [RXN|SMI]

    Result:
    -------
        None or "error"
    """
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
    logging.info(
        "Called matching function, Beginning logs for JobId:" +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid +
        "__run.sh")
    logging.info("Job bash file local location " + job_bash_file)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm

    if query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)
    elif query_format == "SMI":
        query = "\"" + query.replace("\"","") + "\""

    if not is_strict:
        common_cmd = __generic_matching_cmd_line__ + " -Q " + \
            query_format + " -q " + query + " -c " + hits
        cmd = cd_path  + " && " + common_cmd + \
            "  1>%s 2>%s" % (
                job_prefix + "__stdout.log",
                job_prefix + "__stderr.log")
        """
        cmd += " && " + common_cmd + " -f xml " + \
            "  1>%s 2>%s" % (
                job_prefix + "__xml.log",
                job_prefix + "__xmlerr.log")
        """
    else:
        common_cmd = __generic_matching_cmd_line__ + " -Q " + \
            query_format + " -q " + query + " -c " + hits
        cmd = cd_path + " && " + common_cmd + " -s " + \
            "  1>%s 2>%s" % (
                job_prefix + "__stdout.log",
                job_prefix + "__stderr.log")
        """
        cmd += " && " + common_cmd + " -f xml -s " + \
            "  1>%s 2>%s" % (
                job_prefix + "__xml.log",
                job_prefix + "__xmlerr.log")
        """

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
    except:
        logging.error("FAILED TO TRANSFER FILE TO SERVER")
        sys.exit("ERROR")


def submit_bsub(uuid):
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.DEBUG)
    user_directory = os.path.join(__farm_upload_directory__, uuid)
    bash_file = os.path.join(
        user_directory,
        uuid +
        "__run.sh")
    cmd = "bsub -M 20000 -R \"rusage[mem=20000]\" -q production-rh6 -J " + \
        uuid + " \" bash " + bash_file + " \""
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
    """
    try:
        job_id = int(job_id)
    except:
        logging.error(
            "RETURNED job ID was no string, got this instead: " +
            str(job_id))
        sys.exit("ERROR")
    """
    return job_id
