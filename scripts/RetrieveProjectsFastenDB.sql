SELECT DISTINCT pac2.repository
FROM    dependencies AS dep,
        packages AS pac1,
        package_versions AS ver,
        packages AS pac2
WHERE
  -- All vulnerable dependencies.
        dep.dependency_id IN (
        SELECT package_id
        FROM package_versions, packages
        WHERE package_versions.package_id = packages.id AND
                    metadata -> 'vulnerabilities' IS NOT NULL
    ) AND

        dep.dependency_id = pac1.id AND
        dep.package_version_id = ver.id AND
        ver.package_id = pac2.id AND
    pac2.repository IS NOT NULL;