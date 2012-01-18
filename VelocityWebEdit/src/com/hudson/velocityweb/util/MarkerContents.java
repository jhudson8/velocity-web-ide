package com.hudson.velocityweb.util;

public class MarkerContents {
	private String previousContents;
	private String postContents;
	private String contents;
	private int startPosition;
	private int endPosition;

	/**
	 * @return
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * @param contents
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}

	/**
	 * @return
	 */
	public int getEndPosition() {
		return endPosition;
	}

	/**
	 * @param endPosition
	 */
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	/**
	 * @return
	 */
	public String getPostContents() {
		return postContents;
	}

	/**
	 * @param postContents
	 */
	public void setPostContents(String postContents) {
		this.postContents = postContents;
	}

	/**
	 * @return
	 */
	public String getPreviousContents() {
		return previousContents;
	}

	/**
	 * @param previousContents
	 */
	public void setPreviousContents(String previousContents) {
		this.previousContents = previousContents;
	}

	/**
	 * @return
	 */
	public int getStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPosition
	 */
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
}