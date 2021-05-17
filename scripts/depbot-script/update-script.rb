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
    #"password" => ""
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


  source = Dependabot::Source.new(
    provider: "github",
    repo: repo_name,
    directory: directory,
    branch: nil
  )

  ##############################
  # Fetch the dependency files #
  ##############################
  begin
    fetcher = Dependabot::FileFetchers.for_package_manager(package_manager).
              new(source: source, credentials: credentials)
    files = fetcher.files

    out = File.open("poms/#{group_id}__#{package_name}_pom.xml", "a+")
    out.write(files[0].content)
  rescue Dependabot::DependencyFileNotFound
    next
  rescue Dependabot::RepoNotFound
    next
  end
end



