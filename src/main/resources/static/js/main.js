$(document).ready(function () {

    $('#btnLogout').on('click', function (e) {
        e.preventDefault();
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open("GET", '/logout', false);
        xmlHttp.send(null);
        window.location.replace("/login");
    });

    $("#parseXlsx").on('click', function (e) {

        var result = new Map();
        var elements = $('select[name=ORG_TAGS], .SELECT');
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

        var orgTagsInput = $('input[name=ORG_TAGS]').get(0);
        if (orgTagsInput.value.length > 0) {
            orgTagsInput = orgTagsInput.value.split(';');
        } else {
            orgTagsInput = undefined;
        }

        if (result.get('USR_FIO') === undefined || result.get('ORG_INN') === undefined ||
            result.get('USR_PHONE') === undefined || result.get('ORG_PHONE') === undefined || textProjectId.length <= 0) {
            alert("Необходимо заполнить обязательные поля");
            return;
        }

        var data = JSON.stringify({
            "projectCode": textProjectId,
            "fieldLinks": Object.fromEntries(result),
            "orgTags": orgTagsInput,
            "enableWhatsAppLink": $('#enableWhatsAppLink').is(":checked")
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
                if (e.status === 200) {
                    alert("Контакты успешно импортированы");
                } else {
                    alert("Невозможно импортировать контакты");
                }
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

    $('label[for="enableWhatsAppLink"]').on('click', function () {
        if ($('#enableWhatsAppLink').is(":checked")) {
            $('select[name="ORG_HOST"]').multiselect('disable');
        } else {
            $('select[name="ORG_HOST"]').multiselect('enable');
        }
    });
});