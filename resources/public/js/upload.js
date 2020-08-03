$("#submit-button").click(function(){
    $('#download-section').hide();
    //$('#check-download').hide();
    var data = new FormData();
    var tag = "processed_";
    var fileName = tag + document.getElementById("fileselect").files[0].name;
    console.log('received this file: '+fileName);
    $("#download").attr("download", fileName);
    data.append("file", document.getElementById("fileselect").files[0]);
    $.ajax({
        url: "/upload",
        headers: {"x-csrf-token": $("#csrf-token").val()},
        data: data,
        cache: true,
        contentType: false,
        processData: false,
        type: "POST",
        xhr: function() {
          $('#input-file').hide();
          $('#status-message').hide();
          $('#download-check').hide();
          $('#loading-txt').show();
          $('#loader').show();
            xhr = $.ajaxSettings.xhr();
            if(xhr.upload){
                xhr.upload.addEventListener("progress",
                function(e) {
                  var pc = parseInt(e.loaded / e.total * 100);
                  $("#upload-progress")
                  .css("width", pc +"%")
                  .attr("aria-valuenow", 100 - pc)
                  .html(pc + "%");
                },
                false);
            }
            return xhr;
        },
        error: function(data) {
          $("#status-message")
          .show()
          .removeClass("alert-success")
          .toggleClass("alert-danger")
          .html("Processing error");
        },
        success: function(data){
          //$('#check-download').show();
          //$('#download-check').show();
          console.log('received this response: '+data);
          $("#response").html(data);
          $("#status-message")
          .show()
          .removeClass("alert-danger")
          .toggleClass("alert-success")
          .html("Processing success");
          $('#loading-txt').hide();
          $('#loader').hide();
          $('#download-section').show();
          $('#input-file').show();
          $("#fileselect").trigger("reset");
          
        }});
    return false;
});
