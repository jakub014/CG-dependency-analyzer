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
    #"password" => ""
    "password" => ""
  }]

# Full name of the GitHub repo you want to create pull requests for.

# Directory where the base dependency files are.
directory = "/"

# Name of the dependency you'd like to update. (Alternatively, you could easily
# modify this script to loop through all the dependencies returned by
# `parser.parse`.)


csv = File.open("depfile-info.csv", "a")
counter = 0
repos_file = File.open("vuln-repos-git-root.txt").read
repos_file.gsub!(/\r\n?/, "\n")
repos_file.each_line do |line|
  
  repo_name = line.gsub(/\n/, "")
  repo_split = repo_name.split("/")
  group_id = repo_split[0]
  package_name = repo_split[1]
  puts "checked: #{counter}: #{repo_name}"
  counter = counter + 1

  source = Dependabot::Source.new(
    provider: "github",
    repo: repo_name,
    directory: directory,
    branch: nil
  )

  ##############################
  # Try process build.gradle
  ##############################
  begin
  
	fetcher = Dependabot::FileFetchers.for_package_manager("maven").
			  new(source: source, credentials: credentials)
	files = fetcher.files
	
	filecount = 0
	for file in files
		out = File.open("depfiles/#{group_id}__#{package_name}__#{filecount}__pom.xml", "a")
		out.write(file.content)
		
		# Log depfile info
		csv.write("depfiles/#{group_id}__#{package_name}__#{filecount}__pom.xml,#{repo_name},#{file.path},maven\n")
		
		filecount = filecount + 1
	end
  rescue Dependabot::DependencyFileNotFound
	##############################
	# Try process pom.xml
	##############################
	begin
		fetcher = Dependabot::FileFetchers.for_package_manager("gradle").
				  new(source: source, credentials: credentials)
		files = fetcher.files
		
		filecount = 0
		for file in files
			out = File.open("depfiles/#{group_id}__#{package_name}__#{filecount}__build.gradle", "a")
			out.write(file.content)
			
			# Log depfile info
			csv.write("depfiles/#{group_id}__#{package_name}__#{filecount}__build.gradle,#{repo_name},#{file.path},gradle\n")
			
			filecount = filecount + 1
		end
	# Skip if no dependency file found
	rescue Dependabot::DependencyFileNotFound
		next
	rescue Dependabot::RepoNotFound
		next
	rescue TypeError
		next
	end
	
  # Skip if no repo found or type error
  rescue Dependabot::RepoNotFound
    next
  rescue TypeError
    next
  end
end



