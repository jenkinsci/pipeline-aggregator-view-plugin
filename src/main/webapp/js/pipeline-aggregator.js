function format_date(dt) {
   return dt.getFullYear() + '-' + (dt.getMonth() < 9 ? '0' : '') + (dt.getMonth() + 1) + '-' + (dt.getDate() < 10 ? '0' : '') + dt.getDate() + ' ' + (dt.getHours() < 10 ? '0' : '') + dt.getHours() + ':' + (dt.getMinutes() < 10 ? '0' : '') + dt.getMinutes() + ':' + (dt.getSeconds() < 10 ? '0' : '') + dt.getSeconds();
}
jQuery3.ajaxSetup({
   async: false
});
function format_interval(iv) {
   if (iv < 1000) {
      return iv + 'ms';
   }

   ivStr = '';
   // Days
   if (iv > 86400000) {
      ivStr = Math.floor(iv / 86400000) + 'd';
      iv = iv - Math.floor(iv / 86400000) * 86400000;
   }
   // Hours
   if (iv > 3600000) {
      ivStr += ' ' + Math.floor(iv / 3600000) + 'h';
      iv = iv - Math.floor(iv / 3600000) * 3600000;
   }
   // Minutes
   if (iv > 60000) {
      ivStr += ' ' + Math.floor(iv / 60000) + 'm';
      iv = iv - Math.floor(iv / 60000) * 60000;
   }
   // Seconds
   if (iv > 1000) ivStr += ' ' + Math.floor(iv / 1000) + 's';
   return ivStr;
}

function escapeUntrustedHtml(str) {
    return jQuery3('<div>').text(str).html();
}

function reload_jenkins_build_history(tableSelector, viewUrl, buildHistorySize, useScrollingCommits, onlyLastBuild, showCommitInfo, showBuildNumber, showBuildTime, showBuildDuration) {
   jQuery3.getJSON(viewUrl + 'api/json', function (data) {
      i = 0;
      var newRows = [];
      jQuery3.each(data.builds, function (key, val) {
         i++;
         if (i > buildHistorySize) {
            return;
         }
         dt = new Date(val.startTime + val.duration);
         if (useScrollingCommits) {
            var height = jQuery3('.btn-group').height();
            if(height === null){
               height = '41px';
            }
            authors = '<div class="marqueeClass" style="height:'+height+'" >' + '<marquee direction="up" scrollamount="2">'
         } else {
            authors = '<div>'
         }
         buildName = val.buildName.replace(/(.*) #.*/, '$1');
         var url = val.url;
         bame = '<a href="' + url + '" class="job-title">' + escapeUntrustedHtml(buildName) + '</a>';
         stages = '<div class="btn-group" role="group">'
         jQuery3.getJSON(url + "wfapi/describe", function (data) {
            if (typeof data.stages !== 'undefined' && data.stages.length > 0) {
               var changeSet = val.changeLogSet;
               if (typeof data._links.changesets !== 'undefined') {
                  for (var i=0; i<changeSet.length; i++) {
                     text = '<strong>' + escapeUntrustedHtml(changeSet[i].author) + '</strong> ' + escapeUntrustedHtml(changeSet[i].message) + '</br>'
                     authors += text ;
                  }
               } else {
                  authors += 'No Changes'
               }
               if (useScrollingCommits) {
                  authors += '</marquee>' + '</div>';
               } else
                  authors += '</div>'
               for (stage in data.stages) {
                  switch (data.stages[stage].status) {
                     case 'SUCCESS':
                        classes = 'btn-success';
                        tableClass = 'sucess'
                        break;
                     case 'FAILED':
                        classes = 'btn-danger'
                        break;
                     case 'ABORTED':
                        classes = 'btn-warning';
                        tableClass = 'warning'
                     case 'UNSTABLE':
                        classes = 'btn-warning';
                        tableClass = 'danger'
                        break;
                     case 'IN_PROGRESS':
                        classes = 'info invert-text-color';
                        tableClass = 'info'
                        break;
                     default:
                        classes = '';
                  }

                  stages += '<button type="button" class="btn ' + classes + '">' + escapeUntrustedHtml(data.stages[stage].name) + '</button>';
               }
            }
            stages += '</div>'


            newRow = '<tr><td class="job-wrap text-left">' + bame + '</td><td class="text-left">' + stages + '</td>';
            if(showCommitInfo) {
                newRow += '<td>' + authors + '</td>';
            }
            if(showBuildNumber) {
                newRow += '<td>' + val.number + '</td>';
            }
            if(showBuildTime) {
                newRow += '<td>' + format_date(dt) + '</td>';
            }
            if(showBuildDuration) {
                newRow += '<td>' + format_interval(val.duration) + '</td>';
            }
            newRow += '</tr>';
            jQuery3(tableSelector + ' tbody').append(newRow);

            newRow = '<tr><td class="job-wrap text-left">' + bame + '</td><td class="text-left">' + stages + '</td><td>' + authors + '</td><td>' + val.number + '</td><td>' + format_date(dt) + '</td><td>' + format_interval(val.duration) + '</td></trcla>';
            newRows.push(jQuery3(newRow));

         });
      });
	  // Remove all existing rows
	  jQuery3(tableSelector + ' tbody').find('tr').remove();
	  jQuery3(tableSelector + ' tbody').append(newRows);
   });
}

document.addEventListener("DOMContentLoaded", () => {
   const viewData = document.querySelector(".view-data").dataset;
   const url = window.location.href;
   const refreshInterval = parseInt(viewData.refreshInterval);
   const buildHistorySize = parseInt(viewData.buildHistorySize);
   const useScrollingCommits = viewData.useScrollingCommits === "true";
   const onlyLastBuild = viewData.onlyLastBuild === "true";
   const showCommitInfo = viewData.showCommitInfo === "true";
   const showBuildNumber = viewData.showBuildNumber === "true";
   const showBuildTime = viewData.showBuildTime === "true";
   const showBuildDuration = viewData.showBuildDuration === "true";

   window.reload_info = (interval) => {
      reload_jenkins_build_history('#jenkinsBuildHistory', url, buildHistorySize, useScrollingCommits, onlyLastBuild, showCommitInfo, showBuildNumber, showBuildTime, showBuildDuration);
      setTimeout(function() { reload_info(interval); }, interval);
   }

   jQuery3.ajaxSetup({ cache: false });
   reload_info(refreshInterval);
});
