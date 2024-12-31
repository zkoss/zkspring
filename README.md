# ZK Spring
Provide Spring framework integration features. Please read [ZK Spring Essentials](http://books.zkoss.org/wiki/ZK_Spring_Essentials).

# Release Process

1. Update [release note](zkdoc/release-note) according to https://tracker.zkoss.org/ > Releases
2. Run release command according to the edition (freshly/official)
3. Upload files under `target/to_upload` to file server > release folder
4. Set a tag for official version
5. Publish a release in Github
6. Change to the next version with `-SNAPSHOT`. Run `upVer oldversion newVersion FL`
7. Update ZK Spring Essentials

## Release Command
### Freshly
`./release`

It automatically appends a date string into the version in `pom.xml`.

### Official
`./release official`

It automatically removes `-SNAPSHOT` from the version in `pom.xml`.


# Publish to repository
[jenkins2 > PBFUM](http://jenkins2/view/Maven_update/job/PBFUM/)


6.4.2
	/**
	 * Associates a list of {@link RequestMatcher} instances with the
	 * {@link AbstractConfigAttributeRequestMatcherRegistry}
	 * @param requestMatchers the {@link RequestMatcher} instances
	 * @return the object that is chained after creating the {@link RequestMatcher}
	 */
	public C requestMatchers(RequestMatcher... requestMatchers) {
		Assert.state(!this.anyRequestConfigured, "Can't configure requestMatchers after anyRequest");
		return chainRequestMatchers(Arrays.asList(requestMatchers));
	}

original:

```java
                .requestMatchers(new AntPathRequestMatcher("/secure/**")).hasRole("USER")
```