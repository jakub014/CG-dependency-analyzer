import json
import os
import re
import sys

#v_dir = '/mnt/fasten/vuln/consumer/lima-statements'

v_dir = sys.argv[1]
limaStatementsPath = 'limaStatements.json'
groupedStatementsPath = 'limaStatementsGroupedByPackage.json'

def retrieve_limastatements():

    data_list = []

    for v_filename in os.listdir(v_dir):
        # For each file in v_dir, open and load data
        v_filepath = os.path.join(v_dir, v_filename)
        v_file = open(v_filepath, "r")
        data = None
        try :
            data = json.load(v_file)
        except:
            continue


        # If vulnerable_fasten_uris is non-empty, append contents to output file
        if data['vulnerable_fasten_uris'] is not None \
                and len(data['vulnerable_fasten_uris']) > 0:
            v_obj = { "id":data['id'], 'severity': data['severity'], 'vulnerable_purls': data['vulnerable_purls'], 'vulnerable_fasten_uris': data['vulnerable_fasten_uris'], 'references': data['references']}
            data_list.append(v_obj)
        v_file.close()

    o_file = open(limaStatementsPath, 'w')
    o_file.write(json.dumps(data_list, indent=4))
    o_file.close()

def groupByPackage():


    read_file = open(limaStatementsPath, )
    cve_data = json.load(read_file)
    write_file = open(groupedStatementsPath, 'w')

    data = {}
    pkg_pattern = re.compile("^pkg:maven/(.+?)@")
    for cve in cve_data:

        # Get unique package names
        pkg_list = cve['vulnerable_purls']
        pkg_matches = []
        for pkg_url in pkg_list:
            pkg_match = pkg_pattern.search(pkg_url)
            if pkg_match is not None and pkg_match.group(1) not in pkg_matches:
                pkg_matches.append(pkg_match.group(1))

        if len(pkg_matches) > 0:
            for pkg_match in pkg_matches:
                pkg_name = pkg_match.replace('/', ':')
                if pkg_name in data:
                    data[pkg_name].append(cve)
                else:
                    data[pkg_name] = [cve]

    write_file.write(json.dumps(data, indent=4))


    write_file.close()
    read_file.close()



retrieve_limastatements()
groupByPackage()