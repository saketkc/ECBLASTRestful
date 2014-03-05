#!/bin/bash
## Gets the status of the submitted job

curl -X GET http://localhost:8080/ecblast-rest/status/$1
