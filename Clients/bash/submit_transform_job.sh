#!/bin/bash

## Type=strict
curl -i -X POST -F "q=[#8]P([#8])(=O)[#8]P([#8])([#8])=O>>[#8]P([#8])([#8])=O" -F "Q=SMI" -F "c=10" http://localhost:8080/ecblast-rest/transform



## Type=generic
curl -i -X POST -F "q=[#8]P([#8])(=O)[#8]P([#8])([#8])=O>>[#8]P([#8])([#8])=O" -F "Q=SMI" -F "c=10" -F "type=generic" http://localhost:8080/ecblast-rest/transform
