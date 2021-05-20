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
require_relative "JSONParse"


# GitHub credentials with write permission to the repo you want to update
# (so that you can create a new branch, commit and pull request).
# If using a private registry it's also possible to add details of that here.
credentials =
  [{
    "type" => "git_source",
    "host" => "github.com",
    "username" => "x-access-token",
    "password" => "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"
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
    vulnDepsList = returnVulnList()
    for dep in dependencies do
      #########################################
      # Get update details for the dependency #
      #########################################
      advisories = Array.new
      for vuln in vulnDepsList do
        if "#{vuln.dep_gid}:#{vuln.dep_aid}" == dep.name
            advisories << Dependabot::SecurityAdvisory.new(
                dependency_name: "#{vuln.dep_gid}:#{vuln.dep_aid}",
                package_manager: "maven",
                vulnerable_versions: vuln.dep_version
                )
        end
      end
      checker = Dependabot::UpdateCheckers.for_package_manager(package_manager).new(
        dependency: dep,
        dependency_files: files,
        credentials: credentials,
        security_advisories: advisories
      )
      puts "dep: #{dep_counter} #{dep.name}"
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
