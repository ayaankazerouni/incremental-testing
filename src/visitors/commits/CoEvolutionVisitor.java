package visitors.commits;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import models.WorkSession;

public class CoEvolutionVisitor implements CommitVisitor {
	
	private Map<Integer, WorkSession> workDone;
	private Calendar lastTime;
	private int workSessionId;
	
	@Override
	public void initialize(SCMRepository repo, PersistenceMechanism writer) {
		this.workDone = Collections.synchronizedMap(new HashMap<Integer, WorkSession>());
		this.lastTime = null;
		this.workSessionId = 0;
	}

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		synchronized (this.workDone) {
			commit.getModifications().stream()
			.filter(mod -> mod.fileNameEndsWith(".java"))
			.forEach(mod -> {
				Calendar time = commit.getDate();
				long diff = lastTime == null ?
						0 :
						ChronoUnit.HOURS.between(lastTime.toInstant(), time.toInstant());
				
				WorkSession workSession = this.workDone.get(workSessionId);
				
				if (workSession == null) {
					workSession = new WorkSession(time);
				} else if (diff > 1) {
					workSession.setEndTime(lastTime);
					workSessionId++;
					workSession = new WorkSession(time);
				}
				
				if (mod.getFileName().toLowerCase().contains("test")) {
					workSession.incrementalTestFiles(mod.getAdded());
				} else {
					workSession.incrementProductionFiles(mod.getAdded());
				}
				
				this.lastTime = time;
				this.workDone.put(workSessionId, workSession);
			});
		}
	}

	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		synchronized (this.workDone) {
			WorkSession lastSession = this.workDone.get(this.workSessionId);
			lastSession.setEndTime(this.lastTime);
			this.workDone.put(this.workSessionId, lastSession);
			String[] pathSplit = repo.getPath().split("/");
			String[] project = pathSplit[pathSplit.length - 1].split("_");
			String projectUuid = project[0];
			String userId = project[1];
			String assignment = project[2];
			this.workDone.entrySet().stream()
			.forEach(e -> {
				WorkSession workSession = e.getValue();
				writer.write(
					projectUuid,
					userId,
					assignment,
					e.getKey(),
					workSession.getTestFiles(),
					workSession.getProductionFiles(),
					workSession.getStartTime().getTimeInMillis(),
					workSession.getEndTime().getTimeInMillis()
				);
			});
		}
	}
}
