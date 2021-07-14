
# Steps for providing inputs of the Main program:

- dependency manifests + depfile-info.csv <- run /scripts/depbot-script/subdir-script.rb
- project information <- Github API requests <- FASTEN Database (The query is known)

## Lima statements
Run [RetrieveLimaStatements.py](https://github.com/jakub014/CG-dependency-analyzer/blob/master/scripts/extracting-lima-statements/RetrieveLimaStatements.py) and provide the directory which entails the lima statements:

`python RetrieveLimaStatements.py "C:\lima-statements"`

This script produces two JSON files: `limaStatements.json` and `limaStatementsGroupedByPackage.json`
