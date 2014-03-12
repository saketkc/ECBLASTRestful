#!/usr/bin/env python
# Author: Saket Choudhary <saketkc@gmail.com>

import argparse
import logging
import sys
import os
import ntpath

# Directoru on Farm node where all files get uploaded
# Each job gets uploaded as a folder with its folder name as it jobID
__farm_upload_directory__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'
# Command Line static to run atom atom mapping
__generic_matching_cmd_line__ = "/nfs/research2/thornton/www/databases/cgi-bin/ecblast/jdk/jre/bin/java -Xmx10G  -jar /nfs/research2/thornton/www/databases/cgi-bin/ecblast/jar/RXNDecoder.jar -g -j transform -f BOTH"
# Folder location on which tomcat logs are uploaded
__tomcat_jobs_log_directory__ = "/nfs/research2/thornton/www/databases/cgi-bin/ecblast/logs"


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
    query = query.replace("\"", "")
    if query_format == "RXN":
        filename = get_base_filename(query)
        query = os.path.join(job_directory_on_farm, filename)
    elif query_format == "SMI":
        query = "\"" + query.replace("\"", "") + "\""

    if not is_strict:
        common_cmd = __generic_matching_cmd_line__ + " -Q " + \
            query_format + " -q " + query + " -c " + hits
        cmd = cd_path + " && " + common_cmd + \
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
        cmd = cd_path + " && " + common_cmd + " -r " + \
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
        return False
    return True


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--uuid", help="Unique ID", type=str, required=True)
    parser.add_argument("--directory", help="Absolute path to user uploaded stuff", type=str, required=True)
    parser.add_argument("--q", help="Smiles query or reaction file path as on tomcat", type=str, required=True)
    parser.add_argument("--Q", help="Query format[SMI/RXN]", type=str, required=True)
    parser.add_argument("--c", help="No. of hits", type=str, required=True)
    parser.add_argument("--type", help="Type", type=str, required=True)

    args = parser.parse_args(argv)
    queryfile = args.q
    queryformat = args.Q
    uuid = args.uuid
    c = args.c
    type = args.type
    directory = args.directory
    status = run_matching(uuid, directory, queryformat, queryfile, c, type)
    if status is True:
        print "123"
    else:
        print "ERROR"

if __name__ == "__main__":
    main(sys.argv[1:])
