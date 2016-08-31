/**
 * @author Leo
 */
var recommendationTable;
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
    var formData = new FormData();
    formData.append('file', file);
    formData.append('action', action);
    $.ajax({
        url: 'modelService/upload',
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response){
            alert('file upload complete!\nurl: ' + repsonse.responseText);
        	resultUrl = "";
        	recommendationTable.ajax.url("modelService/recommendationResults").load();
        },
        error: function(response){
            var error = "error";
            if (response.status === 409){
                error = response.responseText;
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

$(document).ready(function() {
	recommendationTable = $('#recommendationTable').DataTable( {
    	//支持下载
    	dom: 'lBrtip',
    	buttons: [
    	          {
    	              extend: 'excel',
    	              text: '下载表格',
    	          }
    	      ],
		language: {
			"search": "telephone number:"
		},
		ajax: {
			"dataSrc": "",// 返回数组对象
			//"url" : "modelService/predict",
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
});

function planify(data){
	data.resultUrl = resultUrl;
}