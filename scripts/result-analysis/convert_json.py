import json
import pandas as pd
import re


negative_projects_url = 'negative-maven-projects.txt'
project_data_url = 'vulnerableProjectData.json'
write_path = 'output.json'

# negative_projects_file = open(negative_projects_url, )
read_file = open(project_data_url, errors="ignore")
project_data = json.load(read_file)
# dataframe = pd.DataFrame(project_data)
write_file = open(write_path, 'w')

data = []
for project in project_data:
    user = project['user']
    repo = project['repository']

    if user is not None and repo is not None:
        data[user + '/' + repo] = project

write_file.write(json.dumps(data, indent=7))


write_file.close()
# negative_projects_file.close()