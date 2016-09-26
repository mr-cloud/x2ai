/**
 * @author Leo
 */
var recommendationTable = null;
var resultUrl;
var action = 'predict';
// spin
var opts = {
		  lines: 13 // The number of lines to draw
		, length: 28 // The length of each line
		, width: 14 // The line thickness
		, radius: 42 // The radius of the inner circle
		, scale: 1 // Scales overall size of the spinner
		, corners: 1 // Corner roundness (0..1)
		, color: '#000' // #rgb or #rrggbb or array of colors
		, opacity: 0.25 // Opacity of the lines
		, rotate: 0 // The rotation offset
		, direction: 1 // 1: clockwise, -1: counterclockwise
		, speed: 1 // Rounds per second
		, trail: 60 // Afterglow percentage
		, fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
		, zIndex: 2e9 // The z-index (defaults to 2000000000)
		, className: 'spinner' // The CSS class to assign to the spinner
		, top: '50%' // Top position relative to parent
		, left: '50%' // Left position relative to parent
		, shadow: false // Whether to render a shadow
		, hwaccel: false // Whether to use hardware acceleration
		, position: 'absolute' // Element positioning
		}
var target = null;
var spinner = null;

$(document).ready(function () {
	target = document.getElementById("spin");
});

function progress(e) {
	if (e.lengthComputable) {
		$('#progress_percent').text(Math.floor((e.loaded * 100) / e.total));
		$('progress').attr({value:e.loaded,max:e.total});
	}
}

function upload(){
	var file = $('input[name="upload_file"]').get(0).files[0];
	if(file == null){
		alert("choose file first!");
		return;
	}
	var formData = new FormData();
	formData.append('file', file);
	formData.append('action', action);
	algoName = $("#predictAlgoName").val();
	formData.append('algoName', algoName);
	spinner = new Spinner(opts).spin(target);
	$.ajax({
		url: 'rest/modelService/upload',
		type: 'POST',
		data: formData,
		cache: false,
		contentType: false,
		processData: false,
		success: function(response){
			alert('file uploaded and prediction finished!\nresults url: ' + response);
			resultUrl = response;
			getRecomms();
		},
		error: function(response){
			console.log(response);
			var error = "error";
			console.log(response.status);
			console.log(response.responseText);
			if (response.status === 400 || response.status === 409 || response.status === 403 || response.status === 500){
				error = response.status + " " + response.responseText;
			}
			alert(error);
		},
		xhr: function() {
			var myXhr = $.ajaxSettings.xhr();
			if (myXhr.upload) {
				myXhr.upload.addEventListener('progress', progress, false);
			} else {
				console.log('Upload progress is not supported.');
			}
			return myXhr;
		},
		complete: function(){
			spinner.stop();
		}
	});
}

function getRecomms() {
	if(recommendationTable != null){
		recommendationTable.ajax.reload();
	}
	else{
		recommendationTable = $('#recommendationTable').DataTable( {
			//支持下载
			dom: 'lBfrtip',
			buttons: [
			          {
			        	  extend: 'excel',
			        	  text: 'download',
			          }
			          ],
			          language: {
			        	  "search": "phone number:"
			          },
			          "processing": true,
			          "order": [[1, 'desc']],
			          ajax: {
			        	  "dataSrc": "",// 返回数组对象
			        	  "url" : "rest/modelService/recommendationResults",
			        	  "data" : function(data) {
			        		  // 添加其他参数
			        		  planify(data)
			        	  }
			          },
			          columns: [
			                    { data: "telephoneNumber" },
			                    { data: "score" },
			                    ],
			                    'order': [[1, 'desc']],
		} );
	}
}

function planify(data){
	data.resultUrl = resultUrl;
}