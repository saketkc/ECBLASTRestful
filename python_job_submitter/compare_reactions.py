import argparse
import sys
import re
import ntpath
import os
import logging

__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")

__tomcat_jobs_log_directory__ = "/home/saket/LOGS/"

__farm_upload_directory__ = "/home/saket/UPLOADS/"

__compare_reactions_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j compare -f BOTH"


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
    query = query.replace("\"", "")
    target = target.replace("\"", "")
    if query_format == "SMI":
        query = "\"" + query.replace("\"", "") + "\""
    elif query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)
    if target_format == "SMI":
        target = "\"" + target.replace("\"", "") + "\""
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
        return False
    return True


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
    status = run_compare_reactions(uuid, directory, queryformat,
                                   queryfile, targetformat, targetfile)
    if status is True:
        print "123"
    else:
        print "ERROR"

if __name__ == "__main__":
    main(sys.argv[1:])
