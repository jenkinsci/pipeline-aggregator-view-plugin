function format_date(dt) {
   return dt.getFullYear() + '-' + (dt.getMonth() < 9 ? '0' : '') + (dt.getMonth() + 1) + '-' + (dt.getDate() < 10 ? '0' : '') + dt.getDate() + ' ' + (dt.getHours() < 10 ? '0' : '') + dt.getHours() + ':' + (dt.getMinutes() < 10 ? '0' : '') + dt.getMinutes() + ':' + (dt.getSeconds() < 10 ? '0' : '') + dt.getSeconds();
}
$.ajaxSetup({
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
   if (iv > 1000)
      ivStr += ' ' + Math.floor(iv / 1000) + 's';
   return ivStr;
}


function reload_jenkins_build_history(tableSelector, viewUrl, buildHistorySize) {
   $.getJSON(viewUrl + 'api/json', function (data) {
      // Remove all existing rows
      $(tableSelector + ' tbody').find('tr').remove();
      i = 0;
      $.each(data.builds, function (key, val) {
         i++;
         if (i > buildHistorySize) {
            return;
         }
         dt = new Date(val.startTime + val.duration);
         authors='<ul>';
         buildName = val.buildName.replace(/(.*) #.*/, '$1');
         url = val.buildUrl;
         bame = '<a role="button" href="'+jenkinsUrl+'/job/'+buildName+'/'+val.number+'/" class="btn">'+buildName+'</a>';
         stages = '<div class="btn-group" role="group">'
         $.getJSON(url+"wfapi/describe", function (data) {
               if (typeof data.stages !== 'undefined' && data.stages.length > 0) {
                  authors='<ul class="list-unstyled">';
                  if(typeof data._links.changesets !== 'undefined' )
                  {
                     $.getJSON(jenkinsUrl+data._links.changesets.href.replace('jenkins',''),function(data){
                        for(change in data){
                           for(commit in data[change].commits){
                              text = '<strong>'+data[change].commits[commit].authorJenkinsId+'</strong> '+data[change].commits[commit].message;
                              authors+='<li class="small">'+text+'</li>';
                           }
                        }

                     });
                  }else {
                     authors+='<li class="small">No Changes</li>'
                  }
                  authors+='</ul>';
                  for (stage in data.stages) {
                     switch (data.stages[stage].status) {
                        case 'SUCCESS':
                           classes = 'btn-success';
                           tableClass='sucess'
                           break;
                        case 'FAILED':
                           classes='btn-danger'
                           break;
                        case 'ABORTED':
                           classes = 'btn-warning';
                           tableClass='warning'
                        case 'UNSTABLE':
                           classes = 'btn-warning';
                           tableClass='danger'
                           break;
                        case 'IN_PROGRESS':
                           classes = 'info invert-text-color';
                           tableClass='info'
                           break;
                        default:
                           classes = '';
                     }

                     stages += '<button type="button" class="btn ' + classes + '">' + data.stages[stage].name + '</button>';
                  }
               }
            stages += '</div>'

            newRow = '<tr><td class="text-left">' + bame + '</td><td class="text-left">'+stages+'</td><td>' +authors+ '</td><td>' + val.number + '</td><td>' + format_date(dt) + '</td><td>' + format_interval(val.duration) + '</td></trcla>';
            $(tableSelector + ' tbody').append(newRow);
         });

      });
   });
}

