$(document).ready(function () {


    $("#btn-save").on('click', function (e) {
        var result = new Map();
        result.set('MAX_ERROR_NUMBER', $('#maxErrorNumber').val());

        $("#btn-save").attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/system-variables',
            data: JSON.stringify({
                "variables": Object.fromEntries(result)
            }),
            dataType: 'json',
            cache: false,
            contentType: "application/json; charset=utf-8",
            processData: false,
            type: 'POST',
            error: function (e) {
                if (e.status === 200) {
                    $("#btn-save").attr('disabled', 'disabled');
                    alert("Настройки сохранены");
                } else {
                    $("#btn-save").removeAttr('disabled');
                    alert("Не удается сохранить настройки");
                }
            }
        });
    });
});