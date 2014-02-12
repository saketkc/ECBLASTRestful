import re
import subprocess
import unittest
__pending__ = re.compile("PEND|PSUSP|USUSP|SSUSP|WAIT")
__running__ = re.compile("RUN")
__done__ = re.compile("DONE")
__failed__ = re.compile("EXIT|ZOMBI")
__unknown__ = re.compile("UNKWN")
__notfound__ = re.compile("Job [^ ]+ is not found")


def bjobs_status(job_id=None, stdout=None, stderr=None, run_bjobs=False):
    if run_bjobs:
        if not job_id:
            return "NO JOB ID"
        cmd = "bjobs " + str(job_id)
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
    if stderr and not stdout:
        stdout = stderr
    if not stdout:
        return "UNABLE TO PROCESS"
    if __pending__.search(stdout):
        return "PENDING"
    elif __running__.search(stdout):
        return "RUNNING"
    elif __done__.search(stdout):
        return "DONE"
    elif __failed__.search(stdout):
        return "FAILED"
    elif __unknown__.search(stdout):
        return "UNKNOWN"
    elif __notfound__.search(stdout):
        return "NOT FOUND"
    else:
        return "WRONG"


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

if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(BjobsStatusTests)
    unittest.TextTestRunner(verbosity=2).run(suite)

