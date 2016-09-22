/**
 * @author Leo
 */
var STATE_READY = "READY";
var STATE_TRAINING = "TRAINING";
var state = STATE_READY;
var TRAIN_ACTION_ONE = "";
var TRAIN_ACTION_ALL = "train_all";
var action = TRAIN_ACTION_ONE;

function progress(e) {
	if (e.lengthComputable) {
		$('#progress_percent').text(Math.floor((e.loaded * 100) / e.total));
		$('progress').attr({value:e.loaded,max:e.total});
	}
}

function upload(){
	if(state === STATE_TRAINING){
		alert("training is not finished!\nwait a moment.");
		return;
	}
	else{
		state = STATE_TRAINING;
	}
	var file = $('input[name="upload_file"]').get(0).files[0];
	if(file == null){
		alert("choose file first!");
		return;
	}
	var formData = new FormData();
	formData.append('file', file);
	algoName = $("#trainAlgoName").val();
	if(algoName === ""){
		action = TRAIN_ACTION_ALL;
	}
	formData.append('action', action);
	formData.append('algoName', algoName);
	
	$.ajax({
		url: 'rest/modelService/train',
		type: 'POST',
		data: formData,
		cache: false,
		contentType: false,
		processData: false,
		success: function(response){
			alert('model training succeed!');
			state = STATE_READY;
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
			state = STATE_READY;
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
