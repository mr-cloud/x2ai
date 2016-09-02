/**
 * @author Leo
 */
var recommendationTable = null;
var resultUrl;
var action = 'predict';

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
	$.ajax({
		url: 'rest/modelService/upload',
		type: 'POST',
		data: formData,
		cache: false,
		contentType: false,
		processData: false,
		success: function(response){
			alert('file upload complete!\nurl: ' + response);
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
			        	  text: '下载表格',
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