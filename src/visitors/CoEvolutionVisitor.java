package visitors;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import models.DayOfWork;

public class CoEvolutionVisitor implements CommitVisitor {
	
	private Map<Integer, DayOfWork> workDone;
	
	@Override
	public void initialize(SCMRepository repo, PersistenceMechanism writer) {
		this.workDone = Collections.synchronizedMap(new HashMap<Integer, DayOfWork>());
	}

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		synchronized (this.workDone) {
			commit.getModifications().stream()
			.filter(mod -> mod.fileNameEndsWith(".java"))
			.forEach(mod -> {
				Calendar date = commit.getDate();
				int dateId = date.get(Calendar.DAY_OF_YEAR);
				DayOfWork workDay = this.workDone.get(dateId);
				if (workDay == null) {
					workDay = new DayOfWork();
				}
				
				if (mod.getFileName().toLowerCase().contains("test")) {
					workDay.incrementalTestFiles(mod.getAdded());
				} else {
					workDay.incrementProductionFiles(mod.getAdded());
				}
				workDay.addSeenFile(mod.getFileName());
				this.workDone.put(dateId, workDay);
			});
		}
	}

	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		synchronized (this.workDone) {
			this.workDone.entrySet().stream()
			.forEach(e -> {
				DayOfWork day = e.getValue();
				writer.write(
					repo.getPath(),
					e.getKey(),
					day.getTestFiles(),
					day.getProductionFiles()
				);
			});
		}
	}
}
