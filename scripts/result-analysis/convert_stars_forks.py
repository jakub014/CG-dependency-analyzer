import json
import pandas as pd
import re

# Use this to count any repo attribute, such as stars or forks.
# Edit the field on line 24 for desired field.

negative_projects_url = 'input/custom-projects.txt'
project_data_url = 'input/output.json'
write_path = 'output/custom-stars.json'

negative_projects_file = open(negative_projects_url, )
read_file = open(project_data_url, errors="ignore")
project_data = json.load(read_file)
# dataframe = pd.DataFrame(project_data)
write_file = open(write_path, 'w')

data = {}
for project in negative_projects_file:
    split = project.split(':')
    user_repo = split[0] + ':' + split[1]
    user_repo = user_repo.replace('\n','')
    try:
        stars = project_data[user_repo]["stars"]
        data[user_repo] = stars
    except KeyError:
        print("No entry for: " + user_repo)

write_file.write(json.dumps(data, indent=4))


write_file.close()
negative_projects_file.close()