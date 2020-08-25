package com.nokia.netguard.adapter.test.ngagent;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Vector;

public class Finder extends SimpleFileVisitor<Path> {

	private final PathMatcher matcher;
	private Vector<String> matchVector = new Vector<String>();
	private final FileVisitResult fileVisitResult;

	public Finder(String pattern, FileVisitResult fileVisitResult) {
		this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		this.fileVisitResult = fileVisitResult;
	}

	private boolean find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			matchVector.add(file.toString());
			return true;
		}
		return false;
	}

	public Vector<String> getMatchList() {
		return matchVector;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		if (find(file)) {
			return fileVisitResult;
		} else {
			return FileVisitResult.CONTINUE;
		}
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		find(dir);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}
}