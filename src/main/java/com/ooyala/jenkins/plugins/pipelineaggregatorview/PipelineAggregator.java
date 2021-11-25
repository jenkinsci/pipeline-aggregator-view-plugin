package com.ooyala.jenkins.plugins.pipelineaggregatorview;

import hudson.Extension;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.security.Permission;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by paul on 20/09/16.
 */
@SuppressWarnings("unused")
@ExportedBean
public class PipelineAggregator extends View {

   private String viewName;

   private int fontSize;

   private int buildHistorySize;

   private int refreshInterval;

   private boolean useCondensedTables;
   
   private boolean onlyLastBuild;

   private boolean useScrollingCommits;

   private String filterRegex;

   private boolean showCommitInfo;
   private boolean showBuildNumber;
   private boolean showBuildTime;
   private boolean showBuildDuration;

   @DataBoundConstructor
   public PipelineAggregator(String name, String viewName) {
      super(name);
      this.viewName = viewName;
      this.fontSize = 16;
      this.buildHistorySize = 16;
      this.refreshInterval = 15;
      this.useCondensedTables = false;
	  this.onlyLastBuild = false;
      this.filterRegex = null;

      this.showCommitInfo = true;
      this.showBuildNumber = true;
      this.showBuildTime = true;
      this.showBuildDuration = true;
   }

   protected Object readResolve() {
      if (fontSize == 0)
         fontSize = 16;
      if (buildHistorySize == 0)
         buildHistorySize = 16;
      if (refreshInterval == 0)
         refreshInterval = 15;
      return this;
   }

   @Override
   public Collection<TopLevelItem> getItems() {
      return new ArrayList<TopLevelItem>();
   }

   public int getFontSize() {
      return fontSize;
   }

   public int getBuildHistorySize() {
      return buildHistorySize;
   }

   public boolean isUseCondensedTables() {
      return useCondensedTables;
   }

    public boolean isShowCommitInfo() {
        return showCommitInfo;
    }

    public void setShowCommitInfo(boolean showCommitInfo) {
        this.showCommitInfo = showCommitInfo;
    }

    public boolean isShowBuildNumber() {
        return showBuildNumber;
    }

    public void setShowBuildNumber(boolean showBuildNumber) {
        this.showBuildNumber = showBuildNumber;
    }

    public boolean isShowBuildTime() {
        return showBuildTime;
    }

    public void setShowBuildTime(boolean showBuildTime) {
        this.showBuildTime = showBuildTime;
    }

    public boolean isShowBuildDuration() {
        return showBuildDuration;
    }

    public void setShowBuildDuration(boolean showBuildDuration) {
        this.showBuildDuration = showBuildDuration;
    }

	public boolean isUseScrollingCommits() {
		return useScrollingCommits;
	}

	public void setUseScrollingCommits(boolean useScrollingCommits) {
		this.useScrollingCommits = useScrollingCommits;
	}

	public boolean isOnlyLastBuild() {
		return onlyLastBuild;
	}

	public void setOnlyLastBuild(boolean onlyLastBuild) {
		this.onlyLastBuild = onlyLastBuild;
	}

   public String getTableStyle() {
      return useCondensedTables ? "table-condensed" : "";
   }

   public String getFilterRegex() {
      return filterRegex;
   }

   public int getRefreshInterval() {
      return refreshInterval;
   }

   public void setRefreshInterval(int refreshInterval) {
      this.refreshInterval = refreshInterval;
   }

   @Override
   protected void submit(StaplerRequest req) throws ServletException, IOException {
      JSONObject json = req.getSubmittedForm();
      this.fontSize = json.getInt("fontSize");
      this.buildHistorySize = json.getInt("buildHistorySize");
      this.refreshInterval = json.getInt("refreshInterval");
      this.useCondensedTables = json.getBoolean("useCondensedTables");
      this.useScrollingCommits = json.getBoolean("useScrollingCommits");
      this.onlyLastBuild = json.getBoolean("onlyLastBuild");

      this.showCommitInfo = json.getBoolean("showCommitInfo");
      this.showBuildNumber = json.getBoolean("showBuildNumber");
      this.showBuildTime = json.getBoolean("showBuildTime");
      this.showBuildDuration = json.getBoolean("showBuildDuration");

      if (json.get("useRegexFilter") != null) {
         String regexToTest = req.getParameter("filterRegex");
         try {
            Pattern.compile(regexToTest);
            this.filterRegex = regexToTest;
         } catch (PatternSyntaxException x) {
            Logger.getLogger(ListView.class.getName()).log(Level.WARNING, "Regex filter expression is invalid", x);
         }
      } else {
         this.filterRegex = null;
      }
      save();
   }

   @Override
   public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
      return Jenkins.getInstance().doCreateItem(req, rsp);
   }

   @Override
   public boolean contains(TopLevelItem item) {
      return false;
   }

   @Override
   public boolean hasPermission(final Permission p) {
      return true;
   }

   /**
    * This descriptor class is required to configure the View Page
    */
   @Extension
   public static final class DescriptorImpl extends ViewDescriptor {
      @Override
      public String getDisplayName() {
         return
            "Pipeline Aggregator View";
      }
   }

   public Api getApi() {
      return new Api(this);
   }

   @Exported(name = "builds")
   public Collection<Build> getBuildHistory() {
      Jenkins jenkins = Jenkins.getInstance();
      List<WorkflowJob> jobs = jenkins.getAllItems(WorkflowJob.class);
      Pattern r = filterRegex != null ? Pattern.compile(filterRegex) : null;
      List<WorkflowJob> fJobs = filterJobs(jobs, r);
      List<Build> l = new ArrayList();
	  List<WorkflowRun> wfr = new ArrayList<WorkflowRun>();
	  if( !this.onlyLastBuild ) {
		  RunList<WorkflowRun> builds = new RunList(fJobs).limit(buildHistorySize);
		  for ( WorkflowRun build : builds){
			  wfr.add(build);
		  }
	  } else {
		  for(WorkflowJob job : fJobs) {
			  wfr.add(job.getLastBuild());
		  }
	  }
	  if( wfr != null && wfr.size() > 0 ) {
		  for (WorkflowRun build : wfr) {
			  List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = ((WorkflowRun) build).getChangeSets();
			  Result result = build.getResult();
				l.add(new Build(build.getDisplayName(), build.getFullDisplayName(), build.getUrl(), build.getNumber(),
						build.getStartTimeInMillis(), build.getDuration(), result == null ? "BUILDING" : result.toString(),
						changeLogSets));
		  }
	  }
      return l;
   }

   public List<WorkflowJob> filterJobs(List<WorkflowJob> jobs, Pattern r) {
      if(r == null)
         return jobs;
      for (Iterator<WorkflowJob> iterator = jobs.iterator(); iterator.hasNext(); ) {
         WorkflowJob job = iterator.next();
         WorkflowRun run = job.getLastBuild();
         if (run != null) {
            if (!r.matcher(run.getFullDisplayName()).find()) {
               iterator.remove();
            }
         } else {
            iterator.remove();
         }
      }
      return jobs;
   }


   @ExportedBean(defaultVisibility = 999)
   public static class Build {
      @Exported
      public String jobName;
      @Exported
      public String buildName;
      @Exported
      public String url;
      @Exported
      public int number;
      @Exported
      public long startTime;
      @Exported
      public long duration;
      @Exported
      public String result;
      @Exported
      public List<ChangeLog> changeLogSet;

      public Build(String jobName, String buildName, String url, int number, long startTime, long duration, String result, List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets) {
         this.jobName = jobName;
         this.buildName = buildName;
         this.number = number;
         this.startTime = startTime;
         this.duration = duration;
         this.result = result;
         this.url = url;

         this.changeLogSet = processChanges(changeLogSets);
      }

      private List<ChangeLog> processChanges(List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets) {
         List<ChangeLog> changes = new ArrayList<>();
         for (ChangeLogSet<? extends ChangeLogSet.Entry> set : changeLogSets) {
            for (Object entry : set.getItems()) {
               ChangeLogSet.Entry setEntry = (ChangeLogSet.Entry) entry;
               String author = setEntry.getAuthor().getFullName();
               String message = setEntry.getMsg();
               changes.add(new ChangeLog(author, message));
            }

         }
         return changes;
      }
   }

   @ExportedBean(defaultVisibility = 999)
   public static class ChangeLog {
      @Exported
      public String author;
      @Exported
      public String message;

      public ChangeLog(String author, String message) {
         this.author = author;
         this.message = message;
      }
   }
}

