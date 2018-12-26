package org.zkoss.spring.test.config;

public class TestBean {

	private final String scopeName;
	private final long timestamp;

	public TestBean(String scopeName) {
		this.scopeName = scopeName;
		this.timestamp = System.nanoTime();
	}

	public String getValue() {
		return scopeName + "-" + timestamp;
	}

	public String getScopeName() {
		return scopeName;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
