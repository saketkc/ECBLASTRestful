import requests
import argparse

url = "http://localhost:8080/ECBLASTRESTful/aam/smi"
with open("/home/saket/ecRESTTest/test2.smile", "rb") as f:
    content = f.read().strip()


