$(document).ready(function(){

	$("#setGoogle").click(function(){
		var sheet = {}
		var infoModal = $('#myModal');
    	sheet["sheetID"] = $("#sheetID").val();
		$.ajax({
			type: "POST",
			url:'/setGoogleSheetID',
			contentType: "application/json",
			data: JSON.stringify(sheet),
			success: function(response) {
				console.log(response);
				var htmlData = '';
                htmlData += response;
                htmlData += '';
                infoModal.find('#modal-body')[0].innerHTML = htmlData;
                infoModal.modal();
			}
		});
	});
	
	$("#saveXLSX").click(function(){
		var infoModal = $('#saveXLSX');
		$.ajax({
			type: "GET",
			url:'/save',
			success: function(response) {
				console.log(response);
				console.log(infoModal);
				
				var htmlData = '';
                htmlData += response;
                htmlData += '';
                infoModal.find('#modal-body-xlsx').innerHTML = htmlData;
                infoModal.modal();
			}
		});
	});	
	
	$("#refreshFileButton").click(function(){
		$.ajax({
			url: '/listfiles',
		}).done(function(data){
			console.dir(data);
			var fileHTML = "";
			for(file of data) {
				fileHTML += '<li class="list-group-item"><img src="' + file.thumbnailLink + '">' 
				+ file.name + ' (FileID : ' + file.id + ')' 
				+ '<button onclick="makePublic(\'' + file.id + '\')">Make Public</button>' 
				+ '<button onclick="deleteFile(\'' + file.id + '\')">Delete</button></li>';
			}
			
			$("#fileListContainer").html(fileHTML);
		});
	});
	
	$("#createFolderButton").click(function(){
		var folderName = prompt('Please enter folder name.');
		$.ajax({
			url: '/createfolder/' + folderName
		}).done(function(data){
			console.dir(data);
		})
	});
	
	$("#uploadFileInFolder").click(function(){
		$.ajax({
			url: '/uploadinfolder'
		}).done(function(data){
			alert(data.fileID);
		});
	});
	
});

function deleteFile(id) {
	$.ajax({
		url: '/deletefile/' + id,
		method: 'DELETE'
	}).done(function(){
		alert('File has been deleted. Please refresh the list.');
	});
}

function makePublic(id) {
	$.ajax({
		url: '/makepublic/' + id,
		method: 'POST'
	}).done(function(){
		alert('File can be viewed by anyone on internet.');
	});
}