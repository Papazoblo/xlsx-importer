$(document).ready(function () {

    $('.btn-search').on('click', function () {
        window.location.href = '/inn-exclude?' + getJsonFilter();
    });

    function getJsonFilter() {
        var inn = $('#innFilter').val();
        var createDateFrom = formatDate($('#createDateFromFilter').val());
        var createDateTo = formatDate($('#createDateToFilter').val());
        var bank;

        var options = $('#bankFilter')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                bank = options[j].value;
            }
        }

        return new URLSearchParams({
            "inn": inn,
            "createDateFrom": createDateFrom,
            "createDateTo": createDateTo,
            "bank": bank,
        }).toString();
    }

    function formatDate(dateString) {
        if (dateString !== undefined && dateString.length > 0) {
            var splitAll = dateString.split(" ");
            var splitDate = splitAll[0].split(".");
            return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0] + "T" + splitAll[1];
        }
        return "";
    }

    $('#btnLoadInn').on('click', function () {
        $('input[name=action]').val('INN_DOWNLOAD');
    });

    $('#btnDeleteInn').on('click', function () {
        $('input[name=action]').val('INN_DELETE');
    });

    $("#formSendFile").submit(function (e) {
        e.preventDefault();
        var data = new FormData($('#formSendFile')[0]);
        data.append('file', $('#file')[0].files[0]);

        var options = $('#bankLoad')[0].selectedOptions;
        var bank;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                bank = options[j].value;
            }
        }

        $.ajax({
            url: '/inn-exclude/' + bank,
            data: data,
            dataType: 'text',
            cache: false,
            contentType: false,
            processData: false,
            type: $('input[name=action]').val() === 'INN_DOWNLOAD' ? 'POST' : 'DELETE',
            success: function (data) {
                alert("Файл успешно обработан");
                window.location.replace("/inn-exclude");
            },
            error: function (e) {
                if (e.status !== 200) {
                    alert("Невозможно загрузить файл. \n" + e.responseText);
                }
            }
        });
    });
});