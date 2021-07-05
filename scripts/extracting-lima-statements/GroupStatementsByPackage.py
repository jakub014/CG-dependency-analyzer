import json
import re

read_path = 'vulnJson/cves.json'
write_path = 'vulnJson/pkg_cves2.json'

read_file = open(read_path, )
cve_data = json.load(read_file)
write_file = open(write_path, 'w')

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