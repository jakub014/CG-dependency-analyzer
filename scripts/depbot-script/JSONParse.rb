#!/usr/bin/env ruby

require 'json'


class Dependency
    @@array = Array.new
    attr_accessor :dep_gid, :dep_aid, :dep_version

    def self.all_instances
        @@array
    end

    def initialize(gid, aid, version)
       @dep_gid = gid
       @dep_aid = aid
       @dep_version = version
       @@array << self
    end
 end

file = File.read('pkg_cves.json')

data_hash = JSON.parse(file)

keys = data_hash.keys

def getPurls(purl)
    info = purl.split("@", 2)
    info[1]

end

def getVulns(vuln) 
    purls = vuln.fetch("vulnerable_purls")
    versions = Array.new
    purls.each { |purl| versions << getPurls(purl) }
    versions
end

def getDependencies(key, data_hash) 
    allVersions = Array.new
    dep = data_hash.fetch(key)
    dep.each { |vuln| allVersions = (getVulns(vuln) | allVersions) }
    info1 = key.split(":",2)
    dep23 = Dependency.new(info1[0], info1[1], allVersions)
    
end

keys.each { |key| getDependencies(key, data_hash) }

dependencyList = Dependency.all_instances



