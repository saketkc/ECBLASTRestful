import requests
import unittest
import sys
import argparse
from bs4 import BeautifulSoup as Soup

__base_url__ = "http://172.22.68.115:8080/ecblast-rest/"


class ECBlastRestClient:
    def __init__(self):
        self.base_url = __base_url__

    def check_status(self, job_id=None):
        if not job_id:
            raise ValueError("Job Id cannot be None")

        status_url = self.base_url + 'status' + '/' + job_id
        request = requests.get(status_url)
        content = request.content
        return content

    def submit_atom_atom_mapping(self, file_format, query):
        assert file_format in ['SMI', 'RXN']
        ##Create dummy file
        with open('/tmp/temp.txt', 'w') as f:
            f.write("dummy")
        aam_url = self.base_url + 'aam'
        files = None
        data = {'Q': file_format}
        if file_format is 'RXN':
            files = {'q': open(query, "rb")}
            request = requests.post(aam_url, data=data, files=files)
        elif file_format is 'SMI':
            data['q'] = query
            dummy_files = {'dummy': open('/tmp/temp.txt', 'r')}
            request = requests.post(aam_url, data=data, files=dummy_files)
        return request

    def compare(self, query_format, query, target_format, target):
        compare_url = self.base_url + "compare"
        assert query_format in ['SMI', 'RXN']
        assert target_format in ['SMI', 'RXN']
        data = {'Q': query_format, 'T': target_format}
        files = {}
        with open('/tmp/temp.txt', 'w') as f:
            f.write('dummy')
        if query_format == 'RXN':
            files['q'] = open(query, 'rb')
        elif query_format == 'SMI':
            data['q'] = query
        if target_format == 'RXN':
            files['t'] = open(target, 'rb')
        elif target_format == 'SMI':
            data['t'] = target
        if query_format == 'SMI' and target_format == 'SMI':
            files = {'dummy': open('/tmp/temp.txt', 'rb')}
        request = requests.post(compare_url, data=data, files=files)
        return request

    def transform(self, query_format, query, c=10, is_strict=False):
        assert query_format in ['SMI', 'RXN']
        assert query is not ""
        transform_url = self.base_url + 'transform'
        data = {'Q': query_format}
        files = {}
        data['c'] = c
        with open('/tmp/temp.txt', 'w') as f:
            f.write('dummy')
        if is_strict is False:
            data['type'] = 'generic'
        if query_format == 'RXN':
            files['q'] = open(query, 'rb')
        elif query_format == 'SMI':
            data['q'] = query
            files['dummy'] = open('/tmp/temp.txt', 'rb')
        request = requests.post(transform_url, data=data, files=files)
        return request
    def submit_search(self, query_format, query, search_type="bond", hits=10):
        search_url = self.base_url + 'search'
        data = {'Q': query_format}
        data['c'] = hits
        data['s'] = search_type
        files={}
        if query_format == 'RXN':
            files['q'] = open(query, 'rb')
        elif query_format == 'SMI':
            data['q'] = query
            files['dummy'] = open('/tmp/temp.txt', 'rb')
        request = requests.post(search_url, data=data, files=files)
        return request




    def get_status(self, job_id):
        status_url = self.base_url + "status" + "/" + job_id
        request = requests.get(status_url)
        return request

    def get_mapped_file(self, job_id):
        mapped_url = self.base_url + "result" + "/" + job_id + "/mapped"
        request = requests.get(mapped_url)
        return request

    def get_text_output(self, job_id):
        mapped_url = self.base_url + "result" + "/" + job_id + "/text"
        request = requests.get(mapped_url)
        return request

    def get_xml_file(self, job_id):
        mapped_url = self.base_url + "result" + "/" + job_id + "/xml"
        request = requests.get(mapped_url)
        return request


class TestECBlastFunctions(unittest.TestCase):

    def setUp(self):
        self.rxn_file_location = "/home/saket/Desktop/R03200.rxn"
        self.smi_reaction = "[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12])[CH3:6].[H:30][OH:14]>>[H:30][O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12].[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([OH:14])[CH3:6]"
        #self.smi_reaction = ""

    def test_submit_atom_mapping_smi(self):
        file_format = "SMI"
        client = ECBlastRestClient()
        response = client.submit_atom_atom_mapping(file_format,
                                                   self.smi_reaction)
        if response.status_code != 200:
            print " ERROR ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID Atom Atom Mapping: ", Soup(response.content).find('jobid').string

    def test_submit_atom_mapping_rxn(self):
        file_format = "RXN"
        client = ECBlastRestClient()
        response = client.submit_atom_atom_mapping(file_format,
                                                   self.rxn_file_location)
        if response.status_code != 200:
            print " ERROR in atom_atom_mapping  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID Atom Atom Mapping: ", Soup(response.content).find('jobid').string

    def test_compare_smi(self):
        query_format = 'SMI'
        query = self.smi_reaction
        client = ECBlastRestClient()
        response = client.compare(query_format, query, query_format, query)
        if response.status_code != 200:
            print " ERROR in compare SMI:  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID Compare SMI reactions: ", Soup(response.content).find('jobid').string

    def test_compare_rxn(self):
        query_format = 'RXN'
        query = self.rxn_file_location
        client = ECBlastRestClient()
        response = client.compare(query_format, query, query_format, query)
        if response.status_code != 200:
            print " ERROR in compare RXN:  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID Compare RXN reactions: ", Soup(response.content).find('jobid').string

    def test_transform_smi(self):
        query_format = 'SMI'
        query = self.smi_reaction
        client = ECBlastRestClient()
        response = client.transform(query_format, query)
        if response.status_code != 200:
            print " ERROR in transform SMI:  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID transform SMI: ", Soup(response.content).find('jobid').string

    def test_transform_rxn(self):
        query_format = 'RXN'
        query = self.rxn_file_location
        client = ECBlastRestClient()
        response = client.transform(query_format, query)
        if response.status_code != 200:
            print " ERROR in transform RXN:  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID transform RXN: ", Soup(response.content).find('jobid').string

    def test_search_rxn(self):
        query_format = 'RXN'
        query = self.rxn_file_location
        client = ECBlastRestClient()
        response = client.submit_search(query_format, query)
        if response.status_code != 200:
            print " ERROR in search RXN:  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID search RXN: ", Soup(response.content).find('jobid').string


    def test_search_smi(self):
        query_format = 'SMI'
        query = self.smi_reaction
        client = ECBlastRestClient()
        response = client.submit_search(query_format, query)
        if response.status_code != 200:
            print " ERROR in search SMI:  ", Soup(response.content).find('message').string
        elif response.status_code == 200:
            print "Job ID search SMI: ", Soup(response.content).find('jobid').string

if __name__ == "__main__":
    argparser = argparse.ArgumentParser()
    argparser.add_argument("--test", action="store_true")
    argparser.add_argument("--status", type=str, required=False)
    argparser.add_argument("--mapped", type=str, required=False)
    argparser.add_argument("--text", type=str, required=False)
    argparser.add_argument("--xml", type=str, required=False)

    parsed_args = argparser.parse_args(sys.argv[1:])

    if parsed_args.test:
        suite = unittest.TestLoader().loadTestsFromTestCase(TestECBlastFunctions)
        unittest.TextTestRunner(verbosity=1, failfast=True).run(suite)
    elif parsed_args.status:
        client = ECBlastRestClient()
        response = client.get_status(parsed_args.status)
        print response.content

    elif parsed_args.mapped:
        client = ECBlastRestClient()
        response = client.get_mapped_file(parsed_args.mapped)
        print response.content
    elif parsed_args.text:
        client = ECBlastRestClient()
        response = client.get_text_output(parsed_args.text)
        print response.content
    elif parsed_args.xml:
        client = ECBlastRestClient()
        response = client.get_xml_file(parsed_args.xml)
        print response.content



    """Submit Atom Atom Example
    >>client = ECBlastRestClient()
    >>client.submit_atom_atom_mapping(file_format="SMI", query="[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12])[CH3:6].[H:30][OH:14]>>[H:30][O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12].[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([OH:14])[CH3:6]")
    """

    """ Submit Compare reaction example
    >>client = ECBlastRestClient()
    >>client.
    """
