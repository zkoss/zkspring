# ZK Spring
Provide ZK-Spring framework integration. Highlighted features include exposing Spring-managed beans within the ZK framework, ZK custom scopes for Spring-managed beans, and securing ZK components. Please read [ZK Spring Essentials](https://docs.zkoss.org/zk_spring_essentials/introduction).

## About ZK
ZK is a highly productive open source Java framework for building amazing enterprise web and mobile applications.

## ZK Resources
* [Core Framework](https://github.com/zkoss/zk)
* [Documentation](https://www.zkoss.org/documentation)
* [ZK Website](https://zkoss.org)

# Instructions for project admin
## Release Process

1. Update [release note](zkdoc/release-note) according to https://tracker.zkoss.org/ > Releases
2. Run release command according to the edition (freshly/official)
3. Upload files under `target/to_upload` to file server > release folder
4. Set a tag for official version
5. Publish a release in Github
6. Change to the next version with `-SNAPSHOT`. Run `upVer oldversion newVersion FL`
7. Update ZK Spring Essentials

### Release Command
#### Freshly
`./release`

It automatically appends a date string into the version in `pom.xml`.

#### Official
`./release official`

It automatically removes `-SNAPSHOT` from the version in `pom.xml`.


## Publish to repository
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
