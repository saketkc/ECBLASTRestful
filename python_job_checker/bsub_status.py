#!/homes/saketc/sw/bin/python2.7
import re
import subprocess
import unittest
import requests
import sys
import argparse
import time
from fabric.context_managers import settings
from fabric.context_managers import hide
from fabfile import copy_to_server
import json

#UPLOADS stores user uploads and bash files for the user job

__upload_path__ = '/nfs/nobackup2/research/thornton/ecblast/webservices/UPLOADS'

__pending__ = re.compile("PEND|PSUSP|USUSP|SSUSP|WAIT")
__running__ = re.compile("RUN")
__done__ = re.compile("DONE")
__failed__ = re.compile("EXIT|ZOMBI")
__unknown__ = re.compile("UNKWN")
__notfound__ = re.compile("Job [^ ]+ is not found")

"""

Local paths
__pending_url__ = 'http://172.22.68.115:8080/ecblast-rest/pending_jobs'
__queued_url__ = 'http://172.22.68.115:8080/ecblast-rest/queued_jobs'
__update_url__ = 'http://172.22.68.115:8080/ecblast-rest/updateJobStatus/'


"""

#ServerPaths

__pending_url__ = 'http://172.22.68.115:8080/ecblast-rest/pending_jobs'
__queued_url__ = 'http://172.22.68.115:8080/ecblast-rest/queued_jobs'
__update_url__ = 'http://172.22.68.115:8080/ecblast-rest/updateJobStatus/'


def bjobs_status(job_name=None, stdout=None, stderr=None, run_bjobs=False):
    if run_bjobs:
        if not job_name:
            return "NO JOB Name"
        cmd = "bjobs -a -J " + str(job_name)
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
        stdout, stderr = p.communicate()
    if stderr and not stdout:
        stdout = stderr
    if not stdout:
        return "unable_to_process"
    print stdout
    if __pending__.search(stdout):
        return "pending"
    elif __running__.search(stdout):
	#return "done"
        return "running"
    elif __done__.search(stdout):
        return "done"
    elif __failed__.search(stdout):
        return "failed"
    elif __unknown__.search(stdout):
        return "unknown"
    elif __notfound__.search(stdout):
        return "not_found"
    else:
        return "something went completely wrong"


class BjobsStatusTests(unittest.TestCase):
    def test_pending(self):
        pending_status =  """
        JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME
        5484420 saketc  PEND   research-r ebi-005     ebi5-232    *tderr.log Feb  6 13:28
        """
        status = bjobs_status(stdout=pending_status, run_bjobs=False)
        self.assertTrue(status=="PENDING")
    def test_done(self):
        done_status="""
        JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME
        5484420 saketc  DONE  research-r ebi-005     ebi5-232    *tderr.log Feb  6 13:28
        """
        status = bjobs_status(stdout=done_status, run_bjobs=False)
        self.assertTrue(status=="DONE")
    def test_failed(self):
        failed_status="""
        JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME
        5485039 saketc  EXIT  research-r ebi-005     ebi3-166    *tderr.log Feb  6 13:37
        """
        status = bjobs_status(stdout=failed_status, run_bjobs=False)
        self.assertTrue(status=="FAILED")

def update_status(uuid, job_type):
    status = bjobs_status(job_name=uuid, run_bjobs=True) 
    url = __update_url__ + uuid +'/'+status
    req = requests.get(url)
    if status == "done":
        with settings(hide('running', 'stdout', 'stderr'),host_string="saket@172.22.68.115",password="uzfmTjX9839"):
            rxn_filepath = copy_to_server(uuid, job_type)
	
def run_job(uuid):
    cmd = "bash "+ __upload_path__ + uuid + "/" + uuid + "__run.sh" 
    p = subprocess.Popen(cmd, shell=True)
    stdout,stderr = p.communicate()
    return 1
    
if __name__ == "__main__":
    content = requests.get(__pending_url__).content
    content = json.loads(content)
    response = content['response']
    pending_jobs = response.split(";")
    if len(pending_jobs)==0:
	print "no jobs" 
        
    for job in pending_jobs:
        if job=="":
            break;

        split_job = job.split("::")
        id = split_job[0]
        job_type = split_job[1]
        if id!="":
	    status = update_status(id, job_type) 
    
   
    content = requests.get(__queued_url__).content
    content = json.loads(content)
    response = content['response']
    queued_jobs = response.split(';')
    for job in queued_jobs:
        if job!="":
	    run_job(job)
            url = __update_url__ + job + '/' + 'pending'
            req = requests.get(url)
