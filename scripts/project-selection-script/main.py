import json
import math

import requests
import time

if __name__ == '__main__':
    with open('data/vuln-repos.json') as f:
        vulnerable_repos = json.loads(f.read())

    new_vulnerable_repos = []
    for repo in vulnerable_repos:
        value = repo['repository']
        if 'github' in value:
            if value.startswith('git:ssh://git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[25:]}")
            elif value.startswith('git://git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[21:]}")
            elif value.startswith('scm:git:git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[23:]}".strip())
            elif value.startswith('scm:git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[19:]}".strip())
            elif value.startswith('git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[15:]}".strip())
            elif value.startswith('scm:git'):
                new_vulnerable_repos.append(value[8:])
            elif value.startswith('scm'):
                new_vulnerable_repos.append(value[4:].strip())
            elif value.startswith('https://git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[23:]}")
            elif value.startswith('git:git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[19:]}")
            elif value.startswith('git:ssh://git@github.com'):
                new_vulnerable_repos.append(f"https://github.com/{value[25:]}")
            elif value.startswith('git://github.com'):
                new_vulnerable_repos.append(f"https://{value[6:]}")
            elif value.startswith('git:'):
                new_vulnerable_repos.append(value[4:])
            else:
                new_vulnerable_repos.append(value.strip())

    repo_data = []
    for repo in new_vulnerable_repos:
        aux_repo = repo
        if repo.startswith('https://github.com/'):
            repo = repo.replace("https://github.com/", "")

        elif repo.startswith('http://github.com/'):
            repo = repo.replace("http://github.com/", "")

        elif repo.startswith('github.com/'):
            repo = repo.replace("github.com/", "")

        elif repo.startswith('http://www.github.com/'):
            repo = repo.replace("http://www.github.com/", "")

        else:
            repo = repo.replace("https://www.github.com/", "")

        split_repo_link = repo.split('/')
        user = split_repo_link[0]
        project = split_repo_link[1]
        if project.endswith('.git'):
            project = project[:len(project)-4]
        repo_data.append((aux_repo, f"{user}/{project}"))

    print(len(repo_data))
    project_set = []
    processed_repos = []

    for data in repo_data:
        repo = data[1]

        print(f'Currently processing repo {repo}')

        headers = {
            'Content-Type': 'application/json',
            'Authorization': 'token <ADD YOUR PAT GITHUB TOKEN HERE>'
        }
        response = requests.get(
            url=f'https://api.github.com/search/code?q=filename:dependabot.yml+repo:{repo}',
            headers=headers
        )
        print(f'Response code is {response.status_code}')

        uses_dependabot = False
        if response.status_code == 200:
            r_json = response.json()
            if r_json['total_count'] > 0:
                uses_dependabot = True
                print(f'Repo {repo} uses Dependabot')
                project_set.append(repo)
            else:
                print(f'Repo {repo} does not use DependaBot. Total count is {r_json["total_count"]}')

        with open('data/processed_repos.txt', 'a') as f:
            f.write(f"{data[0]}, {repo}, {uses_dependabot}, {response.status_code}\n")

        time.sleep(15)

def get_dependabot_using_projects_github():
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'token <ADD YOUR PAT GITHUB TOKEN HERE>'
    }
    initial_request = requests.get(
        url='https://api.github.com/search/code?q=filename:dependabot.yml&per_page=100',
        headers=headers
    )
    total_count = initial_request.json()['total_count']
    total_iter = math.ceil(total_count / 100)
    time.sleep(5)

    for i in range (0, total_iter):
        print(f'Currently at page {i}')
        response = requests.get(
        url=f'https://api.github.com/search/code?q=filename:dependabot.yml&page={i}&per_page=100',
        headers=headers
        )
        print(f'Response status is {response.status_code}')

        r_json = response.json()
        print(r_json)

        if response.status_code == 200:
            for item in r_json['items']:
                with open('data/dependabot-repos.csv', 'a') as f:
                    f.write(f"{item['repository']['id']}, {item['repository']['full_name']}, {item['repository']['html_url']}, {response.status_code}\n")

        time.sleep(15)
