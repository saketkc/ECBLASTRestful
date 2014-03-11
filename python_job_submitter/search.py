#!/usr/bin/env python
# Author: Saket Choudhary <saketkc@gmail.com>
import argparse
import re
import logging
import sys
import os
import ntpath

# Directoru on Farm node where all files get uploaded
# Each job gets uploaded as a folder with its folder name as it jobID
__farm_upload_directory__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
__search_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar  -j search"
__tomcat_jobs_log_directory__ = "/home/saket/LOGS/"

__job_submitted_re__ = re.compile("Job [^]+ is submitted to queue [^]+")


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
    query = query.replace("\"", "")
    if query_format == "SMI":
        query = "\"" + query.replace("\"", "") + "\""
    elif query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)
    # For text
    common_cmd = __search_cmd_line__ + " -Q " + query_format + \
        " -q " + query + " -s " + search_type + " -c " + hits
    cmd = cd_path + " && " + common_cmd +  \
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
        return False
    return True


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", help="Unique ID", type=str, required=True)
    parser.add_argument("--directory", help="User directory as on farm", type=str, required=True)
    parser.add_argument("--q", help="Smiles query or absolute path to RXN file", type=str, required=True)
    parser.add_argument("--Q", help="Query format, SMI/RXN", type=str, required=True)
    parser.add_argument("--c", help="No of hits", type=str, required=True)
    parser.add_argument("--s", help="Searhc type bond/centre/structure", type=str, required=True)

    args = parser.parse_args(argv)
    queryfile = args.q
    queryformat = args.Q
    uuid = args.uuid
    c = args.c
    type = args.s
    directory = args.directory
    status = run_search(uuid, directory, queryformat, queryfile, type, c)
    if status is True:
        print "123"
    else:
        print "ERROR"
if __name__ == "__main__":
    main(sys.argv[1:])
