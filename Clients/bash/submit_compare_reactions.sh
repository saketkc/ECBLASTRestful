#!/bin/bash
curl -X POST -F  "Q=SMI" -F "q=C[S+](CC[C@H]([NH3+])C([O-])=O)C[C@H]1O[C@H]([C@H](O)[C@@H]1O)N1C=NC2=C1N=CN=C2N.CC(C)=CCC\C(C)=C\CC\C(C)=C\CC[C@]1(C)CCC2=CC(O)=CC(C)=C2O1>>NC1=NC=NC2=C1N=CN2[C@@H]1O[C@H](CSCC[C@H]([NH3+])C([O-])=O)[C@@H](O)[C@H]1O.CC(C)=CCC\C(C)=C\CC\C(C)=C\CC[C@]1(C)CCC2=C(C)C(O)=CC(C)=C2O1.[H+]" -F"T=SMI" -F "t=C[S+](CC[C@H]([NH3+])C([O-])=O)C[C@H]1O[C@H]([C@H](O)[C@@H]1O)N1C=NC2=C1N=CN=C2N.CC(C)=CCC\C(C)=C\CC\C(C)=C\CC[C@]1(C)CCC2=CC(O)=CC(C)=C2O1>>NC1=NC=NC2=C1N=CN2[C@@H]1O[C@H](CSCC[C@H]([NH3+])C([O-])=O)[C@@H](O)[C@H]1O.CC(C)=CCC\C(C)=C\CC\C(C)=C\CC[C@]1(C)CCC2=C(C)C(O)=CC(C)=C2O1.[H+]" http://localhost:8080/ecblast-rest/compare

curl -X POST -F "Q=RXN" -F q=@R03200.rxn -F "T=RXN"  -F q=@R03200.rxn http://localhost:8080/ecblast-rest/compare

