# This script is designed to be copied into an interactive Ruby session, to
# give you an idea of how the different classes in Dependabot Core fit together.
#
# It's used regularly by the Dependabot team to manually debug issues, so should
# always be up-to-date.

require "dependabot/file_fetchers"
require "dependabot/file_parsers"
require "dependabot/update_checkers"
require "dependabot/file_updaters"
require "dependabot/pull_request_creator"
require "dependabot/omnibus"

# GitHub credentials with write permission to the repo you want to update
# (so that you can create a new branch, commit and pull request).
# If using a private registry it's also possible to add details of that here.
credentials =
  [{
    "type" => "git_source",
    "host" => "github.com",
    "username" => "x-access-token",
    "password" => ""
  }]

# Full name of the GitHub repo you want to create pull requests for.

# Directory where the base dependency files are.
directory = "/"

# Name of the dependency you'd like to update. (Alternatively, you could easily
# modify this script to loop through all the dependencies returned by
# `parser.parse`.)

# Name of the package manager you'd like to do the update for. Options are:
# - bundler
# - pip (includes pipenv)
# - npm_and_yarn
# - maven
# - gradle
# - cargo
# - hex
# - composer
# - nuget
# - dep
# - go_modules
# - elm
# - submodules
# - docker
# - terraform
package_manager = "maven"
advisories = '[{
  "dependency-name":"com.fasterxml.jackson.core:jackson-databind",
  "patched-versions":[],
  "unaffected-versions":[],
  "affected-versions":["2.0.0","2.0.0-RC1","2.0.0-RC2","2.0.0-RC3","2.0.1","2.0.2","2.0.4","2.0.5","2.0.6","2.1.0","2.1.1","2.1.2","2.1.3","2.1.4","2.1.5","2.10.0","2.10.0.pr1","2.10.0.pr2","2.10.0.pr3","2.10.1","2.10.2","2.2.0","2.2.0-rc1","2.2.1","2.2.2","2.2.3","2.2.4","2.3.0","2.3.0-rc1","2.3.1","2.3.2","2.3.3","2.3.4","2.3.5","2.4.0","2.4.0-rc1","2.4.0-rc2","2.4.0-rc3","2.4.1","2.4.1.1","2.4.1.2","2.4.1.3","2.4.2","2.4.3","2.4.4","2.4.5","2.4.5.1","2.4.6","2.5.0","2.5.0-rc1","2.5.1","2.5.2","2.5.3","2.5.5","2.6.0","2.6.0-rc1","2.6.0-rc2","2.6.0-rc3","2.6.0-rc4","2.6.1","2.6.2","2.6.3","2.6.4","2.6.5","2.6.6","2.6.7","2.6.7.1","2.6.7.2","2.6.7.3","2.7.0","2.7.0-rc1","2.7.0-rc2","2.7.0-rc3","2.7.1","2.7.1-1","2.7.2","2.7.3","2.7.4","2.7.5","2.7.6","2.7.7","2.7.8","2.7.9","2.7.9.1","2.7.9.2","2.7.9.3","2.7.9.4","2.7.9.5","2.7.9.6","2.8.0","2.8.0.rc1","2.8.0.rc2","2.8.1","2.8.10","2.8.11","2.8.11.1","2.8.11.2","2.8.11.3","2.8.11.4","2.8.2","2.8.3","2.8.4","2.8.5","2.8.6","2.8.7","2.8.8","2.8.8","2.8.8.1","2.8.9","2.9.0","2.9.0.pr1","2.9.0.pr2","2.9.0.pr3","2.9.0.pr4","2.9.1","2.9.10","2.9.10.1","2.9.10.2","2.9.2","2.9.3","2.9.4","2.9.5","2.9.6","2.9.7","2.9.8","2.9.9","2.9.9.1","2.9.9.2","2.9.9.3"]
}]'

counter = 0
repos_file = File.open("vuln_repos.txt").read
repos_file.gsub!(/\r\n?/, "\n")
repos_file.each_line do |line|
  puts "checked: #{counter}"
  counter = counter + 1
  repo_name = line.gsub(/\n/, "")
  repo_split = repo_name.split("/")
  group_id = repo_split[0]
  package_name = repo_split[1]
  puts "repo: #{repo_name}"


  source = Dependabot::Source.new(
    provider: "github",
    repo: repo_name,
    directory: directory,
    branch: nil
  )

  ##############################
  # Fetch the dependency files #
  ##############################
  fetcher = []
  files = []
  begin
    fetcher = Dependabot::FileFetchers.for_package_manager(package_manager).
              new(source: source, credentials: credentials)
    files = fetcher.files
  rescue Dependabot::DependencyFileNotFound
    next
  rescue Dependabot::RepoNotFound
    next
  end


  unless fetcher.nil? 
    commit = fetcher.commit
    ##############################
    ## Parse the dependency files #
    ###############################
    parser = Dependabot::FileParsers.for_package_manager(package_manager).new(
      dependency_files: files,
      source: source,
      credentials: credentials,
    )

    dependencies = parser.parse
    dep_counter = 0
    for dep in dependencies do
      #########################################
      # Get update details for the dependency #
      #########################################
#       advisories = [Dependabot::SecurityAdvisory.new(
#         dependency_name: "com.fasterxml.jackson.core:jackson-databind",
#         package_manager: "maven",
#         vulnerable_versions: ["2.0.0","2.0.0-RC1","2.0.0-RC2","2.0.0-RC3","2.0.1","2.0.2","2.0.4","2.0.5","2.0.6","2.1.0","2.1.1","2.1.2","2.1.3","2.1.4","2.1.5","2.10.0","2.10.0.pr1","2.10.0.pr2","2.10.0.pr3","2.10.1","2.10.2","2.2.0","2.2.0-rc1","2.2.1","2.2.2","2.2.3","2.2.4","2.3.0","2.3.0-rc1","2.3.1","2.3.2","2.3.3","2.3.4","2.3.5","2.4.0","2.4.0-rc1","2.4.0-rc2","2.4.0-rc3","2.4.1","2.4.1.1","2.4.1.2","2.4.1.3","2.4.2","2.4.3","2.4.4","2.4.5","2.4.5.1","2.4.6","2.5.0","2.5.0-rc1","2.5.1","2.5.2","2.5.3","2.5.5","2.6.0","2.6.0-rc1","2.6.0-rc2","2.6.0-rc3","2.6.0-rc4","2.6.1","2.6.2","2.6.3","2.6.4","2.6.5","2.6.6","2.6.7","2.6.7.1","2.6.7.2","2.6.7.3","2.7.0","2.7.0-rc1","2.7.0-rc2","2.7.0-rc3","2.7.1","2.7.1-1","2.7.2","2.7.3","2.7.4","2.7.5","2.7.6","2.7.7","2.7.8","2.7.9","2.7.9.1","2.7.9.2","2.7.9.3","2.7.9.4","2.7.9.5","2.7.9.6","2.8.0","2.8.0.rc1","2.8.0.rc2","2.8.1","2.8.10","2.8.11","2.8.11.1","2.8.11.2","2.8.11.3","2.8.11.4","2.8.2","2.8.3","2.8.4","2.8.5","2.8.6","2.8.7","2.8.8","2.8.8","2.8.8.1","2.8.9","2.9.0","2.9.0.pr1","2.9.0.pr2","2.9.0.pr3","2.9.0.pr4","2.9.1","2.9.10","2.9.10.1","2.9.10.2","2.9.2","2.9.3","2.9.4","2.9.5","2.9.6","2.9.7","2.9.8","2.9.9","2.9.9.1","2.9.9.2","2.9.9.3"]
#       )]
      checker = Dependabot::UpdateCheckers.for_package_manager(package_manager).new(
        dependency: dep,
        dependency_files: files,
        credentials: credentials,
#        security_advisories: advisories
      )
      puts "dep: #{dep_counter}"
      dep_counter = dep_counter + 1
      puts "vulnerable: #{checker.vulnerable?}"
      puts checker.lowest_security_fix_version
      #checker.can_update?(requirements_to_unlock: :own)
      #updated_deps = checker.updated_dependencies(requirements_to_unlock: :own)
    end
  else
    puts "Fetcher nil"
  end
end
