#!/usr/bin/env python
# Author: Saket Choudhary <saketkc@gmail.com>

import argparse
import sys
import re
import logging
import os
import ntpath
__atom_atom_mapping_cmd_line__ = "/nfs/research2/thornton/saket/RXNDecoder/jre/bin/java -Xmx10G  -jar /nfs/research2/thornton/saket/RXNDecoder/RXNDecoder.jar -g -j AAM -f BOTH"
# Command Line to compare two reactions
__tomcat_jobs_log_directory__ = "/home/saket/LOGS/"

__farm_upload_directory__ = "/home/saket/UPLOADS/"
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
        query = "\"" + query.replace("\"", "") + "\""
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
        return False
    return True


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", help="unique ID", type=str, required=True)
    parser.add_argument("--directory", help="Path location where user uploads sit",
                        type=str, required=True)
    parser.add_argument("--Q", type=str, help="Query Type", required=True)
    parser.add_argument("--q", help="SMILES string or absolute path to RXN file",
                        type=str, required=True)
    args = parser.parse_args(argv)
    directory = args.directory
    uuid = args.uuid
    query = args.q.replace("\"", "")
    query_format = args.Q
    #login = username + "@" + host
    status = run_atom_atom_mapping_rxn(uuid, directory, query_format, query)
    if status is True:
        print "123"
    else:
        print "ERROR"
    #stdout = submit_bsub(uuid)
    #if __job_submitted_re__.search(stdout):
    #    print stdout.split("<")[1].split(">")[0]
    #else:
    #    print "error"

if __name__ == "__main__":
    main(sys.argv[1:])
