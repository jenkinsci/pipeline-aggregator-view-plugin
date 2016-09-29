package com.ooyala.jenkins.plugins.pipelineaggregatorview;

import hudson.Extension;
import hudson.model.*;
import hudson.security.Permission;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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

   private boolean useCondensedTables;

   private String filterRegex;

   @DataBoundConstructor
   public PipelineAggregator(String name, String viewName) {
      super(name);
      this.viewName = viewName;
      this.fontSize = 16;
      this.buildHistorySize = 16;
      this.useCondensedTables = false;
      this.filterRegex = null;
   }

   protected Object readResolve() {
      if (fontSize == 0)
         fontSize = 16;
      if (buildHistorySize == 0)
         buildHistorySize = 16;
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

   public String getTableStyle() {
      return useCondensedTables ? "table-condensed" : "";
   }

   public String getFilterRegex() {
      return filterRegex;
   }


   @Override
   protected void submit(StaplerRequest req) throws ServletException, IOException {
      JSONObject json = req.getSubmittedForm();
      this.fontSize = json.getInt("fontSize");
      this.buildHistorySize = json.getInt("buildHistorySize");
      this.useCondensedTables = json.getBoolean("useCondensedTables");
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
            "PipelineAggregator";
      }
   }

   public Api getApi() {
      return new Api(this);
   }

   @Exported(name = "builds")
   public Collection<Build> getBuildHistory() {
      List<WorkflowJob> jobs = Jenkins.getInstance().getAllItems(WorkflowJob.class);
      RunList builds = new RunList(jobs).limit(buildHistorySize);
      ArrayList<Build> l = new ArrayList<Build>();
      Pattern r = filterRegex != null ? Pattern.compile(filterRegex) : null;
      for (Object b : builds) {
         Run build = (Run) b;
         Job job = build.getParent();
         // If filtering is enabled, skip jobs not matching the filter
         if (r != null && !r.matcher(job.getName()).find())
            continue;
         Result result = build.getResult();
         l.add(new Build(job.getName(),
            build.getFullDisplayName(),
            build.getNumber(),
            build.getStartTimeInMillis(),
            build.getDuration(),
            result == null ? "BUILDING" : result.toString()));
      }
      return l;
   }


   @ExportedBean(defaultVisibility = 999)
   public static class Build {
      @Exported
      public String jobName;
      @Exported
      public String buildName;
      @Exported
      public int number;
      @Exported
      public long startTime;
      @Exported
      public long duration;
      @Exported
      public String result;

      public Build(String jobName, String buildName, int number, long startTime, long duration, String result) {
         this.jobName = jobName;
         this.buildName = buildName;
         this.number = number;
         this.startTime = startTime;
         this.duration = duration;
         this.result = result;
      }
   }

}
