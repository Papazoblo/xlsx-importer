$(document).ready(function () {

    $('#btnLogout').on('click', function (e) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open("GET", '/logout', false);
        xmlHttp.send(null);
        return xmlHttp.responseText;
    });

    $("#parseXlsx").on('click', function (e) {

        var result = new Map();
        var elements = $('.SELECT');
        var textProjectId = $('#textProjectId').val();

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

        var usrTags = $('input[name=USR_TAGS]').get(0);
        if (usrTags.value.length > 0) {
            usrTags = usrTags.value.split(';');
        } else {
            usrTags = undefined;
        }

        var orgTags = $('input[name=ORG_TAGS]').get(0);
        if (orgTags.value.length > 0) {
            orgTags = orgTags.value.split(';');
        } else {
            orgTags = undefined;
        }

        if (result.get('USR_FIO') === undefined || result.get('USR_INN') === undefined ||
            result.get('USR_PHONE') === undefined || textProjectId.length <= 0) {
            alert("Необходимо заполнить обязательные поля");
            return;
        }

        var data = JSON.stringify({
            "projectCode": textProjectId,
            "fieldLinks": Object.fromEntries(result),
            "usrTags": usrTags,
            "orgTags": orgTags
        });

        $("#parseXlsx").attr('disabled', 'disabled');

        $.ajax({
            url: '/xlsx/import',
            data: data,
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            type: 'POST',
            success: function (data) {
                $("#parseXlsx").removeAttr('disabled');
                alert("Контакты успешно импортированы");
            },
            error: function (e) {
                $("#parseXlsx").removeAttr('disabled');
                alert("Невозможно импортировать контакты");
            }
        });
    });

    $("#formSendFile").submit(function (e) {
        e.preventDefault();
        var data = new FormData($('#formSendFile')[0]);
        data.append('file', $('#file')[0].files[0]);

        $.ajax({
            url: '/xlsx/upload',
            data: data,
            dataType: 'text',
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: function (data) {
                window.location.replace("/xlsx/import");
            }
        });
    });

    $(".onlyNumbers").keyup(function (e) {
        if (/\D/g.test(this.value)) {
            this.value = this.value.replace(/\D/g, '');
        }
    });
})
;