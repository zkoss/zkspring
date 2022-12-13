# Release Process

## Freshly
`./release`
It automatically appends a date string into the version in `pom.xml`.

## Official
`./release official`
It automatically removes `-SNAPSHOT` from the version in `pom.xml`.


# Publish to repository
[jenkins2 > PBFUM](http://jenkins2/view/Maven_update/job/PBFUM/)