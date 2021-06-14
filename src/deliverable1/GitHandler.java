package deliverable1;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitHandler {
	 
	private GitHandler() {}
	
	public static Git clone(String path, String projName) throws GitAPIException, IOException{
		Git git;
		try {
			git = Git.cloneRepository().setURI("https://github.com/apache/"+ projName).setDirectory(new File(path)).call();
		} catch (JGitInternalException e) {
			git = Git.open(new File(path));
		}
		return git;
	}
	
	public static List<RevCommit> getCommits(Git git) throws GitAPIException, IOException{
		List<RevCommit> commits = new ArrayList<>();
		Iterable<RevCommit> log = git.log().all().call();
		
		for (RevCommit commit : log) {
			LogController.getSingletonInstance().saveMess("Commit = " + commit.getFullMessage());
			commits.add(commit);
		}
		return commits;		
	}
	
	public static LocalDateTime getFixDate(List<RevCommit> commits, String key) throws SecurityException, IOException {
		int i;
		Collections.reverse(commits);
		LocalDateTime dateTime = null;
		
		for (i = 0; i < commits.size(); i++) {
			RevCommit commit = commits.get(i);
			//LogController.getSingletonInstance().saveMess("messaggio: " + commit.getFullMessage()); 
			if(commit.getFullMessage().contains(key)) {
				dateTime = Instant.ofEpochSecond(commit.getCommitTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
				LogController.getSingletonInstance().saveMess("Commit for ticket: " + key);
			}
		}

		return dateTime;
	}
	
}
