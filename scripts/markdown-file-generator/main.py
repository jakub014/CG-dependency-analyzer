from os import walk

import requests


README_TEMPLATE = """
| Path to file in this project | Name of method in this project | Parameters of method in this project | Called vulnerable dependency method | Path to vulnerable dependency file |
|------------------------------|--------------------------------|--------------------------------------|-------------------------------------|------------------------------------|
"""

if __name__=='__main__':
    _, _, file_names = next(walk('data'))

    for file_name in file_names:
        with open(f'data/{file_name}') as f:
            contents = f.read().split("\n")

            repository_name = contents[0]
            link = contents[1]
            vulnerable_dependency = contents[2][23:]

            with open(f'results/{file_name[0:len(file_name) - 4]}.md', 'a') as markdown_file:
                markdown_file.write(README_TEMPLATE)

            for i in range(4, len(contents)-3, 3):
                project_uri = requests.utils.unquote(contents[i][3:])
                project_uri = project_uri.replace(f'fasten://mvn!{repository_name}$', '')

                branch_name = project_uri.split('/')[0]
                full_project_path = project_uri.replace(f'{branch_name}/', '').split('(')[0].split('/')
                file_name_and_method = full_project_path[len(full_project_path)-1].split('.')
                file_name_in_project = file_name_and_method[0]
                method_name = file_name_and_method[1]

                reconstructed_path = ""
                for j in range(0, len(full_project_path)-1):
                    reconstructed_path += full_project_path[j] + '/'
                reconstructed_path += file_name_in_project

                method_arguments = project_uri.split('(')[1].split(')')[0]
                method_return = project_uri.split('(')[1].split(')')[1]

                vulnerability_uri = requests.utils.unquote(contents[i+1][7:])
                vulnerability_uri = vulnerability_uri.replace(f'fasten://mvn!{vulnerable_dependency}$', '')

                vulnerable_branch_name = vulnerability_uri.split('/')[0]
                vulnerable_full_project_path = vulnerability_uri.replace(f'{vulnerable_branch_name}/', '').split('(')[0].split('/')
                vulnerable_file_name_and_method = vulnerable_full_project_path[len(vulnerable_full_project_path)-1].split('.')
                if len (vulnerable_file_name_and_method) == 1:
                    vulnerable_file_name_and_method = vulnerable_full_project_path[len(vulnerable_full_project_path)-1].split('$')
                vulnerable_file_name = vulnerable_file_name_and_method[0]
                vulnerable_method_name = vulnerable_file_name_and_method[1]

                vulnerable_reconstructed_path = ""
                for j in range(0, len(vulnerable_full_project_path)-1):
                    vulnerable_reconstructed_path += vulnerable_full_project_path[j] + '/'
                vulnerable_reconstructed_path += vulnerable_file_name

                vulnerable_method_arguments = vulnerability_uri.split('(')[1].split(')')[0]
                vulnerable_method_return = vulnerability_uri.split('(')[1].split(')')[1]

                markdown_output = f"|  {reconstructed_path}  |  {method_name}  |  {method_arguments if method_arguments else 'void'}  |  {vulnerable_method_name} | {vulnerable_reconstructed_path} |\n"
                with open(f'results/{file_name[0:len(file_name)-4]}.md', 'a') as markdown_file:
                    markdown_file.write(markdown_output)
