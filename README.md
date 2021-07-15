
# Steps for providing inputs of the Main program:

- dependency manifests + depfile-info.csv <- run /scripts/depbot-script/subdir-script.rb
## Repositories
Retrieve the repositories through [this query](https://github.com/jakub014/CG-dependency-analyzer/blob/master/scripts/RetrieveProjectsFastenDB.sql) on the FASTEN database ([schema](https://github.com/fasten-project/fasten/wiki/Metadata-Database-Schema)). Store the results in a txt file. 

For example `queryResults.txt`:
```
https://github.com/jakub014/CG-dependency-analyzer
https://github.com/user/exampleRepositoryName
...
git@github.com:anotherUser/anotherRepo.git
https://github.com/fasten-project/fasten
```

Then run the ProcessDatabaseResults program to obtain a file containing github hosted repositories.

Provide the list of GitHub repositories to the `retrieveDependencyManifests.rb` to download the corresponding dependency manifests.


## Lima statements
Run [RetrieveLimaStatements.py](https://github.com/jakub014/CG-dependency-analyzer/blob/master/scripts/extracting-lima-statements/RetrieveLimaStatements.py) and provide the directory which entails the lima statements:

`python RetrieveLimaStatements.py "C:\lima-statements"`

This script produces two JSON files: `limaStatements.json` and `limaStatementsGroupedByPackage.json`
