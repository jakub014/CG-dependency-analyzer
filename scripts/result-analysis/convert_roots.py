import json
import pandas as pd
import os

# Loops over method-level logs in `impacts_url`, and
# counts how many distinct external vulnerable methods
# were used.

impacts_url = 'input/mlvulns/'
write_path = 'output/roots.txt'

# negative_projects_file = open(negative_projects_url, )
# read_file = open(project_data_url, errors="ignore")
# project_data = json.load(read_file)
# dataframe = pd.DataFrame(project_data)
# write_file = open(write_path, 'w')

data = []
methods = []
for filename in os.listdir(impacts_url):
    file = open(os.path.join(impacts_url, filename))
    file_methods = []
    roots_count = 0
    for line in file:
        if "_______" in line and line not in file_methods:
            roots_count += 1
            file_methods.append(line)
    data.append(roots_count)
    file.close()

data.sort()
print(data)
# write_file.write(data)
# write_file.write(json.dumps(data, indent=7))


# write_file.close()
# negative_projects_file.close()