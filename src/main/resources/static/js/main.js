$(document).ready(function () {

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

        elements = $('.INPUT');
        for (var i = 0; i < elements.length; i++) {
            var input = elements.get(i);
            var name = input.getAttribute('name');
            var value = input.value;
            if (value.length > 0) {
                result.set(name, value.split(';'));
            }
        }

        if (result.get('USR_FIO') === undefined || result.get('USR_INN') === undefined ||
            result.get('USR_PHONE') === undefined || textProjectId.length <= 0) {
            alert("Необходимо заполнить обязательные поля");
            return;
        }

        var data = JSON.stringify({
            "projectCode": textProjectId,
            "fieldLinks": Object.fromEntries(result)
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