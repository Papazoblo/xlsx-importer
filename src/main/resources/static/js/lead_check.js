$(document).ready(function () {


    $('#btnLogout').on('click', function (e) {
        e.preventDefault();
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open("GET", '/logout', false);
        xmlHttp.send(null);
        window.location.replace("/login");
    });

    $("#btnLaunchCheck").on('click', function (e) {

        var action = $('input[name=action]').val();
        var result = new Map();
        var elements = $('select[name=ORG_TAGS], .SELECT');
        var fileId = $('#fileId').val();

        for (var i = 0; i < elements.length; i++) {
            var select = elements.get(i);
            var name = select.getAttribute('name');
            var options = select.selectedOptions;
            var selectIndex = [];
            for (let j = 0; j < options.length; j++) {
                if (options[j] !== undefined) {
                    selectIndex.push(options[j].value);
                }
            }
            if (selectIndex.length > 0) {
                result.set(name, selectIndex);
            }
        }

        /*if (action === 'ACTION_LEAD_DOWNLOAD' && (result.get('USR_FIO') === undefined || result.get('ORG_INN') === undefined ||
            result.get('USR_PHONE') === undefined || result.get('ORG_PHONE') === undefined)) {
            alert("Необходимо заполнить обязательные поля");
            return;
        }*/


        var data = JSON.stringify({
            "fileId": fileId,
            "fieldLinks": Object.fromEntries(result)
        });

        $("#btnLaunchCheck").attr('disabled', 'disabled');

        $.ajax({
            url: '/xlsx/import/' + action,
            data: data,
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            type: 'POST',
            success: function (data) {
                $("#btnLaunchCheck").removeAttr('disabled');
                alert("Файл поставлен в очередь на обработку");
                window.location.replace("/xlsx/import/" + action);
            },
            error: function (e) {
                $("#btnLaunchCheck").removeAttr('disabled');
                if (e.status === 200) {
                    alert("Файл поставлен в очередь на обработку");
                    window.location.replace("/xlsx/import/" + action);
                } else {
                    alert("Ошибка");
                }
            }
        });
    });

    $("#formSendFile").submit(function (e) {
        e.preventDefault();
        var action = $('input[name=action]').val();
        var data = new FormData($('#formSendFile')[0]);
        data.append('file', $('#file')[0].files[0]);

        $.ajax({
            url: '/xlsx/upload/' + action,
            data: data,
            dataType: 'text',
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: function (data) {
                window.location.replace("/xlsx/import/" + action);
            },
            error: function (e) {
                if (e.status !== 200) {
                    alert("Невозможно загрузить файл. \n" + e.responseText);
                }
            }
        });
    });

    $(".onlyNumbers").keyup(function () {
        if (/\D/g.test(this.value)) {
            this.value = this.value.replace(/\D/g, '');
        }
    });

    $("#files .btn-delete").on('click', function (e) {
        var id = $(this).attr("id");
        var isDelete = confirm("Удалить файл с id = " + id + "?");

        if (isDelete) {
            var xmlHttp = new XMLHttpRequest();
            xmlHttp.open("DELETE", '/file-storage/' + id, false);
            xmlHttp.send(null);
            window.location.reload();
        }
    });
});