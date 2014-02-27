from fabric.api import run
from fabric.api import put
import logging
import sys
import os
import ntpath

__farm_upload_directory__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
__atom_atom_mapping_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G  -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j AAM"
__compare_reactions_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j compare"
__generic_matching_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j transform"
__search_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j search"
__update_job_status_cmd_line__ = "python /homes/saketc/python_job_checker/bsub_status.py "
__tomcat_jobs_log_directory__ = "/home/saket/LOGS/"


def get_base_filename(path):
    head, tail = ntpath.split(path)
    return tail or ntpath.basename(head)


def run_atom_atom_mapping_rxn(
        uuid,
        user_upload_directory,
        user_uploaded_file,
        file_format="RXN"):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
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
    file_contents = renamed_filepath
    logging.info("User renamed filename as on farm" + renamed_filepath)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    cmd = cd_path + " && " + __atom_atom_mapping_cmd_line__ + " -Q " + file_format + " -q " + \
         file_contents + \
        " 1>%s 2>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")
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


def run_atom_atom_mapping_smi(
        uuid,
        user_upload_directory,
        smile_query,
        file_format="SMI"):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
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
        "__run_atom_atom_mapping.sh")
    logging.info("Job bash file local location " + job_bash_file)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    cmd = cd_path + " && " + __atom_atom_mapping_cmd_line__ + " -Q " + file_format + " -q " + \
         smile_query + \
        " 1>%s 2>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")
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


def run_search(
        uuid,
        user_upload_directory,
        file_format,
        smile_query_or_path,
        search_type, hits):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    #smile_query_or_path = smile_query_or_path.replace("\"","")
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
    logging.info(
        "Called run_atom_atom_mapping function, Beginning logs for JobId:" +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid +
        "__run_atom_atom_mapping.sh")
    logging.info("Job bash file local location " + job_bash_file)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    cd_path = "cd " + job_directory_on_farm
    smile_query = ""
    if file_format=="RXN":
        with open(smile_query_or_path, "rb") as f:
            smile_query+="\""+f.read().strip()+"\""
    else:
        smile_query = smile_query_or_path
    cmd = cd_path + " && " + __search_cmd_line__ + " -Q " + file_format + " -q " + \
         smile_query + " -s " + search_type +" -c " + hits + \
        " 1>%s 2>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")
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

def run_compare_reactions(
        uuid,
        user_upload_directory,
        user_uploaded_query_format,
        user_uploaded_query_file,
        user_uploaded_target_format,
        user_uploaded_target_file):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    user_uploaded_target_file = user_uploaded_target_file.replace("\"","")
    user_uploaded_query_file = user_uploaded_query_file.replace("\"","")

    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
    logging.info(
        "Called run_atom_atom_mapping function, Beginning logs for JobId:" +
        uuid)
    job_directory_on_farm = os.path.join(__farm_upload_directory__, uuid)
    query_filename = get_base_filename(user_uploaded_query_file)
    target_filename = get_base_filename(user_uploaded_target_file)
    job_bash_file = os.path.join(
        user_upload_directory,
        uuid +
        "__run_atom_atom_mapping.sh")
    logging.info(
        "User uploaded filename " +
        query_filename +
        " ; " +
        target_filename)
    query_file_contents = ""
    target_file_contents =  ""
    logging.info("Job bash file local location " + job_bash_file)
    query_renamed_filepath = os.path.join(
        job_directory_on_farm,
        query_filename)
    target_renamed_filepath = os.path.join(
        job_directory_on_farm,
        target_filename)
    logging.info("User renamed filename as on farm" + query_renamed_filepath)
    job_prefix = os.path.join(job_directory_on_farm, uuid)
    if user_uploaded_query_format == "SMI":
        query_file_contents =  "\"" + user_uploaded_query_file +"\""
    elif user_uploaded_query_format == "RXN":
        query_file_contents = query_renamed_filepath

    if user_uploaded_target_format == "SMI":
        target_file_contents =  "\"" + user_uploaded_target_file + "\""
    elif user_uploaded_target_format == "RXN":
        target_file_contents = target_renamed_filepath

    cd_path = "cd " + job_directory_on_farm
    cmd = cd_path + " && " + __compare_reactions_cmd_line__ + " -Q " + user_uploaded_query_format + " -q " + \
        query_file_contents + " -T " + user_uploaded_target_format + " -t " + \
        target_file_contents + \
        " 1>%s 2>%s" % (
            job_prefix + "__stdout.log",
            job_prefix + "__stderr.log")
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
        file_format,
        user_uploaded_file,
        hits, is_strict=False):
    """Copy the whole file/folder as passed in path
    to the farm node

    Params:
        path: path location to
    """
    __logfile__ = os.path.join(__tomcat_jobs_log_directory__, uuid + ".log")
    logging.basicConfig(filename=__logfile__, level=logging.INFO)
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

    if file_format == "RXN":
        file_contents =  renamed_filepath
    elif file_format == "SMI":
        with open(user_uploaded_file, "rb") as f:
            file_contents = "\""+ f.read().strip().replace("\n","") + "\""

    if not is_strict:
        cmd = cd_path + " && " + __generic_matching_cmd_line__ + " -Q " + file_format + " -q " + \
            file_contents + " -c " + hits + \
            "  1>%s 2>%s" % (
                job_prefix + "__stdout.log",
                job_prefix + "__stderr.log")
    else:
        cmd = cd_path + " && " + __generic_matching_cmd_line__ + " -Q " + file_format + " -q " + \
            file_contents + " -c " + hits + " -s " + \
            "  1>%s 2>%s" % (
                job_prefix + "__stdout.log",
                job_prefix + "__stderr.log")

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
    bash_file = os.path.join(
        __farm_upload_directory__,
        uuid +
        "__run_atom_atom_mapping.sh")
    user_directory = os.path.join(__farm_upload_directory__, uuid)
    bash_file = os.path.join(
        user_directory,
        uuid +
        "__run_atom_atom_mapping.sh")
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
